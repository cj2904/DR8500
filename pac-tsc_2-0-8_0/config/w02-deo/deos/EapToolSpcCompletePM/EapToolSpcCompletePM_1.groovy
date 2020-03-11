package EapToolSpcCompletePM

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.CompleteMaintenanceRequest
import sg.znt.camstar.semisuite.service.dto.GetMaintenanceStatusesRequest
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EapToolSpcCompletePM_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="inputXml")
    private String inputXml

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        String maintenancestatus =""
        String pmUsed =""

        def request = new W02CompleteOutLotRequest(inputXml)
        pmUsed = request.getItemValue("tscPMUsedName")

        def eqId = request.getResourceName()
        def lotId = request.getContainerName()
        def virtualEqp = cEquipment.getVirtualSystemId()
        
        logger.info("outbound lotId id " + lotId)
        logger.info("outbound onhold " + request.isOnHold())
        logger.info("outbound eqp id " + eqId)
        logger.info("virtualEqp id " + virtualEqp)
        logger.info("realEqp id " + cEquipment.getRealSystemId())
        
        logger.info("lot isCancelTrackIn '" + request.isCancelTrackIn() + "'")
        if (request.isCancelTrackIn())
        {
            logger.info("lot '" + lotId + "' is Cancel TrackIn. Do Not Perform EapToolSpcCompletePM.")
            return
        }
        
        
        if (virtualEqp.equalsIgnoreCase(eqId))
        {
			String wipStatus = request.getItemValue("WIPStatus")
			if (!wipStatus.equalsIgnoreCase("fail"))
			{
				def requestmaineqreq = new GetMaintenanceStatusesRequest()
				requestmaineqreq.getInputData().setResource(cEquipment.getRealSystemId())
				def reply = cCamstarService.getMaintenanceStatuses(requestmaineqreq)
				if(reply.isSuccessful())
				{
					def items = reply.getAllMaintenanceRecord()
					while (items.hasNext())
					{
						def item = items.next()
						def itemRow = item.getRow()
						def dueTimeStamp = itemRow.getNextDateDue()
						def pastDueTimeStamp = itemRow.getNextDateLimit()
						def completed = itemRow.getCompleted()
						def due = itemRow.getDue();
						def pastDue = itemRow.getPastDue()
						def pmState = itemRow.getMaintenanceState()
						def key = itemRow.getMaintenanceReqName()

						logger.info("virtualEqp pmUsed " + pmUsed)
						logger.info("real eqp getMaintenanceReqName " + key)
						
						if (key.equalsIgnoreCase(pmUsed))
						{
							maintenancestatus = itemRow.getMaintenanceStatus()
							logger.info("real eqp maintenancestatus " + maintenancestatus)
							
							def requestCompletePM = new CompleteMaintenanceRequest()
							requestCompletePM.getInputData().getResource().setName(cEquipment.getRealSystemId())
							requestCompletePM.getInputData().setForceMaintenance("true")
							def maintenanceReq = requestCompletePM.getInputData().getMaintenanceReq()
							maintenanceReq.setName(pmUsed)
							maintenanceReq.setUseROR("true")
							def sdi = requestCompletePM.getInputData().getServiceDetails().addServiceDetailsItem()
							sdi.getMaintenanceStatus().setId(maintenancestatus)

							def replycompletePM = cCamstarService.completePMMaint(requestCompletePM)
							if(reply.isSuccessful())
							{
								def completionMsg =reply.getResponseData().getCompletionMsg()
								logger.info(completionMsg)
							}
							else
							{
								CamstarMesUtil.handleNoChangeError(reply)
							}
						}
					}
				}
			}
			else
			{
				logger.info("Wip status is 'FAIL'!")
			}
        }
    }
}