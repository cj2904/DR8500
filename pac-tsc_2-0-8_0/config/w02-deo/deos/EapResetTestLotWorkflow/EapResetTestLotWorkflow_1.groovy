package EapResetTestLotWorkflow

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.SetEquipmentMaintRequest
import sg.znt.camstar.semisuite.service.dto.SetEquipmentMaintResponse
import sg.znt.pac.TscConstants;
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
reset TestLotWorkflow field if wipstatus is successful
''')
class EapResetTestLotWorkflow_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="InputXml")
    private String inputXml

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment
	
	@DeoBinding(id="MaterialManager")
	private CMaterialManager materialManager

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        String maintenancestatus = ""
        String lastProcessEqp = ""

        def outbound = new W02CompleteOutLotRequest(inputXml)
        def eqpId = outbound.getResourceName()
		def lotId = outbound.getContainerName()

        CLot cLot = materialManager.getCLot(lotId)
        lastProcessEqp = cLot.getPropertyContainer().getString(TscConstants.LOT_MES_ATTR_LAST_PROCESS_EQP, "")

		if (lastProcessEqp.length()>0)
		{
			logger.info("outbound lotId id " + lotId)
			logger.info("outbound eqp id " + eqpId)

			if (outbound.isCancelTrackIn())
			{
				logger.info("lot '" + lotId + "' is Cancel TrackIn. Do not perform EapSetEqpMainRequest.")
				return
			}
			
			String wipStatus = outbound.getItemValue("WIPStatus")
			if (!wipStatus.equalsIgnoreCase("fail"))
			{
				
				SetEquipmentMaintRequest request = new SetEquipmentMaintRequest(lastProcessEqp)
								
				request.getInputData().getObjectChanges().initChildParameter("tscTestLotWorkflow")
				request.getInputData().getObjectChanges().getChildParameter("tscTestLotWorkflow").setValue("")
				
				SetEquipmentMaintResponse reply = cCamstarService.setEquipmentMaint(request)
				if (reply.isSuccessful())
				{
					logger.info(reply.getResponseData().getCompletionMsg())
				}
				else
				{
					throw new Exception(reply.getExceptionData().getErrorDescription())
				}
			}
			else
			{
				logger.warn("Wip status is 'FAIL'!")
			}
		}
		else
		{
			logger.info("tscLastProcessEqp field is empty. SetEquipmentMaintRequest will not be performed.")
		}
    }
}