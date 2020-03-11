package EapClearMesMaskIdInEqpSetup

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.GetEquipmentSetupRequest
import sg.znt.camstar.semisuite.service.dto.SetEquipmentSetupRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest

@CompileStatic
@Deo(description='''
clear mask id in eqp setup
''')
class EapClearMesMaskIdInEqpSetup_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService
	
	@DeoBinding(id="InputXml")
	private String inputXml

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def outbound = new W02CompleteOutLotRequest(inputXml)
		def eqpId = outbound.getResourceName()
		
		def getEqpSetupRequest = new GetEquipmentSetupRequest()
		getEqpSetupRequest.getInputData().setResource(eqpId)
		
		def getEqpSetupReply = cCamstarService.getEquipmentSetup(getEqpSetupRequest)
		if (!getEqpSetupReply.isSuccessful())
		{
			throw new Exception("GetEquipmentSetupRequest failed with error message:'" + getEqpSetupReply.getExceptionData().getErrorDescription() + "'!")
		}
		else
		{
			def resourceChangeCount = getEqpSetupReply.getResponseData().getResourceChangeCount()
			
			def setEqpSetupRequest = new SetEquipmentSetupRequest()
			setEqpSetupRequest.getInputData().setResource(eqpId)
			setEqpSetupRequest.getInputData().setMask("")
			setEqpSetupRequest.getInputData().setProcessType(CCamstarService.DEFAULT_PROCESS_TYPE)
			setEqpSetupRequest.getInputData().setResourceChangeCount(resourceChangeCount)

			def reply = cCamstarService.setEquipmentSetup(setEqpSetupRequest)
			if (reply.isSuccessful())
			{
				logger.info(reply.getResponseData().getCompletionMsg())
			}
			else
			{
				CamstarMesUtil.handleNoChangeError(reply)
			}
		}
    }
}