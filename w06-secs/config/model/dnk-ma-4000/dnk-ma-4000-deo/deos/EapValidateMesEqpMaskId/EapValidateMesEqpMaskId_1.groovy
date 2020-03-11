package EapValidateMesEqpMaskId

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsControl
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.SecsService
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.GetEquipmentSetupRequest
import sg.znt.pac.W06Constants
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.pac.util.EqpUtil
import sg.znt.pac.util.EqpUtil.VidType
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
Validate and crosscheck mask ID from camstar and from eqp
''')
class EapValidateMesEqpMaskId_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment
	
	@DeoBinding(id="InputXml")
	private String inputXml

	private SecsGemService secsGemService
	private SecsControl secsControl
	
	private final static String SECS_VID_MASK_ID = "MaskId"
	
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def camstarOutbound = new W02TrackInLotRequest(inputXml)
		def eqpId = camstarOutbound.getResourceName()
		def lotId = camstarOutbound.getContainerName()
		def skipMaskIdValidation = camstarOutbound.getItemValue(W06Constants.MES_LOT_ATTR_TSC_SKIP_MASK_ID_VALIDATION).toString()
		if (skipMaskIdValidation.equalsIgnoreCase("true") || skipMaskIdValidation.equalsIgnoreCase("yes") || skipMaskIdValidation.equalsIgnoreCase("1"))
		{
			logger.info("Skip Mask ID validation as Lot '$lotId' Attribute '" + W06Constants.MES_LOT_ATTR_TSC_SKIP_MASK_ID_VALIDATION + "' is '$skipMaskIdValidation'...")
			return
		}
		def request = new GetEquipmentSetupRequest()
		request.getInputData().setResource(eqpId)
		def reply = cCamstarService.getEquipmentSetup(request)
		if (!reply.isSuccessful())
		{
			CamstarMesUtil.handleNoChangeError(reply)
		}
		else
		{
			secsGemService = (SecsGemService) cEquipment.getExternalService()
			secsControl = cEquipment.getSecsControl()
			def camstarMaskId = reply.getResponseData().getMask().getName()
			if (camstarMaskId == null)
			{
				throw new Exception("Camstar Mask ID is not setup in Equipment '$eqpId'!")
			}
			def eqpMaskId = getEqpValue(SECS_VID_MASK_ID)
			if (!camstarMaskId.toString().equalsIgnoreCase(eqpMaskId.trim()))
			{
				throw new Exception("Mask ID not from Camstar '$camstarMaskId' is not matched with Equipement VID '' with value '$eqpMaskId'!")
			}
		}
    }
	
	private String getEqpValue(String parameterName)
    {
        VidType vid = EqpUtil.getVid(parameterName, secsControl)
        String value = EqpUtil.getStringSecsValue((SecsService)secsGemService, vid)
        
        return value
    }
}