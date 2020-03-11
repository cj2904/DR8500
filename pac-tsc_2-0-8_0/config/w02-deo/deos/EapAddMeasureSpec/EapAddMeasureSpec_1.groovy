package EapAddMeasureSpec

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.ModifyLotAttributesRequest
import sg.znt.pac.TscConstants
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import de.znt.camstar.semisuite.service.dto.ViewContainerStatusRequest
import de.znt.camstar.semisuite.service.dto.ViewContainerStatusResponse
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EapAddMeasureSpec_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new W02TrackInLotRequest(inputXmlDocument)
        def lotId = request.getContainerName()

        def workFlowStep = cMaterialManager.getCLot(lotId).getWorkflowSteps()
        def workFlowNotes =cMaterialManager.getCLot(lotId).getWorkflowNotes()

        def workFlowStepStr = ""
        for (int i=0; i<workFlowStep.length; i++)
        {
            if(workFlowNotes.contains(workFlowStep[i])==false)
            {
                if(workFlowStepStr.length()>0)
                {
                    workFlowStepStr = workFlowStepStr + ","
                }
                workFlowStepStr = workFlowStepStr + workFlowStep[i]
            }
        }

        String lotAttrMeasSpecAfter = getLotAttribute(lotId,TscConstants.LOT_MES_ATTR_PENDING_MEASUREMENT_SPEC)
        if(lotAttrMeasSpecAfter==null || lotAttrMeasSpecAfter.length()==0)
        {
            def attributePairValues = new HashMap<String, String>()
            attributePairValues.put(TscConstants.LOT_MES_ATTR_PENDING_MEASUREMENT_SPEC, workFlowStepStr)
            def attributeRequest = new ModifyLotAttributesRequest(false, lotId, attributePairValues)
            def reply = cCamstarService.setLotAttributes(attributeRequest)
            if (reply.isSuccessful())
            {
                logger.info(reply.getResponseData().toXmlString())
            }
            else
            {
                logger.error(reply.getExceptionData().getErrorDescription())
            }
        }
        //1. get pending measurement attribute value
        //2 set attribute workflow step
        //getListValue("WorkFlowSteps")
        //getItemValue("")
    }

    String getLotAttribute(String lotId, String attrName)
    {
        String result = ""
        ViewContainerStatusRequest request = new ViewContainerStatusRequest(lotId)
        request.getRequestData().getLotAttributes().initChildParameter(attrName, false)

        ViewContainerStatusResponse reply = new ViewContainerStatusResponse()
        reply.getResponseData().getLotAttributes().initChildParameter(attrName,false)
        reply =  cCamstarService.viewContainerStatus(request)
        if(reply.isSuccessful())
        {
            result = reply.getResponseData().getLotAttributes().getChildParameter(attrName).getValue()
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
        return result
    }
}