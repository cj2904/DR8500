package EapUpdateLastWorkflowWaferEqp

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.SetEquipmentMaintRequest
import sg.znt.pac.TscConstants
import sg.znt.pac.W06Constants
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest

@CompileStatic
@Deo(description='''
Update Last Workflow Wafer Equipment<br/>
<b>Upon complete lot outbound update last run Workflow</b>
''')
class EapUpdateLastWorkflowWaferEqp_1
{


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
		if (!outbound.isCancelTrackIn())
		{
			def eqpId = outbound.getResourceName()
			def curRunWorkflow = outbound.getItemValue("Workflow").toString()

			def request = new SetEquipmentMaintRequest(eqpId)
//			request.getInputData().getObjectChanges().initChildParameter(W06Constants.MES_WAFER_EQP_LAST_RUN_WORKFLOW)
//			request.getInputData().getObjectChanges().getChildParameter(W06Constants.MES_WAFER_EQP_LAST_RUN_WORKFLOW).setValue(curRunWorkflow)
			request.getInputData().getObjectChanges().initChildParameter("tscEqpReserved7")
			request.getInputData().getObjectChanges().getChildParameter("tscEqpReserved7").setValue(curRunWorkflow)

			def reply = cCamstarService.setEquipmentMaint(request)
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