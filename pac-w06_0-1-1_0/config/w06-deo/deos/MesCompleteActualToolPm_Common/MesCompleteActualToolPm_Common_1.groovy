package MesCompleteActualToolPm_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.CompleteMaintenanceRequest
import sg.znt.camstar.semisuite.service.dto.GetMaintenanceStatusesRequest
import sg.znt.pac.W06Constants
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest

@CompileStatic
@Deo(description='''
W06 common function:<br/>
<b>Complete actual tool PM upon Complete Lot Outbound Received</b>
''')
class MesCompleteActualToolPm_Common_1
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
		def request = new W02CompleteOutLotRequest(inputXml)
		def lotId = request.getContainerName()
		def outboundEqpId = request.getResourceName()
		def virtualEqpId = cEquipment.getVirtualSystemId()
		def actualEqpId = cEquipment.getRealSystemId()

		if (request.isCancelTrackIn())
		{
			logger.info("Cancel Track In for Lot ID '$lotId', skip perform MesCompleteActualToolPm_Common")
			return
		}
		
		if (virtualEqpId.equalsIgnoreCase(outboundEqpId))
		{
			String lotWipStatus = request.getItemValue(W06Constants.MES_LOT_WIP_STATUS)
			logger.info("Lot ID '$lotId' with '" + W06Constants.MES_LOT_WIP_STATUS + "' is '$lotWipStatus'")
			if (!lotWipStatus.equalsIgnoreCase("fail"))
			{
				def getMaintStatReq = new GetMaintenanceStatusesRequest()
				getMaintStatReq.getInputData().setResource(actualEqpId)
				def reply = cCamstarService.getMaintenanceStatuses(getMaintStatReq)
				if(reply.isSuccessful())
				{
					String lotAttrPmUsedName = request.getItemValue(W06Constants.MES_LOT_ATTR_TSC_PM_USED_NAME)
					logger.info("Lot ID '$lotId' lot attribute '" + W06Constants.MES_LOT_ATTR_TSC_PM_USED_NAME + "' value '$lotAttrPmUsedName'")
					def items = reply.getAllMaintenanceRecord()
					while (items.hasNext())
					{
						def item = items.next()
						def itemRow = item.getRow()
						def key = itemRow.getMaintenanceReqName()
						
						logger.info("Maintenance Requirement Name:'$key'")
						
						if (key.equalsIgnoreCase(lotAttrPmUsedName))
						{
							def maintStat = itemRow.getMaintenanceStatus()
							logger.info("Equipment ID '$actualEqpId' with PM Name '$lotAttrPmUsedName' status is '$maintStat'")
							
							def completePmReq = new CompleteMaintenanceRequest()
							completePmReq.getInputData().getResource().setName(actualEqpId)
							completePmReq.getInputData().setForceMaintenance("true")
							def maintenanceReq = completePmReq.getInputData().getMaintenanceReq()
							maintenanceReq.setName(lotAttrPmUsedName)
							maintenanceReq.setUseROR("true")
							def sdi = completePmReq.getInputData().getServiceDetails().addServiceDetailsItem()
							sdi.getMaintenanceStatus().setId(maintStat)

							def replycompletePM = cCamstarService.completePMMaint(completePmReq)
							if(replycompletePM.isSuccessful())
							{
								def completionMsg = replycompletePM.getResponseData().getCompletionMsg()
								logger.info(completionMsg)
								break
							}
							else
							{
								CamstarMesUtil.handleNoChangeError(replycompletePM)
							}
						}
					}
				}
				else
				{
					CamstarMesUtil.handleNoChangeError(reply)
				}
			}
		}
	}
}