package MesSetRecipeWipData

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest
import de.znt.camstar.semisuite.service.dto.AdhocWIPDataRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Set recipe at Adhoc wip data
''')
class MesSetRecipeWipData_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="InputXml")
    private String inputXml

    @DeoBinding(id="WipdataSetupName")
    private String wipdataSetupName

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outbound = new W02CompleteOutLotRequest(inputXml)
        def lotId = outbound.getContainerName()
        def eqId = outbound.getElementId()

        logger.info("lot isCancelTrackIn '" + outbound.isCancelTrackIn() + "'")
        if (outbound.isCancelTrackIn())
        {
            logger.info("lot '" + lotId + "' is Cancel TrackIn. Do Not Perform MesSetRecipeWipData.")
            return
        }

        def lot = cMaterialManager.getCLot(lotId)
        if (lot!=null)
        {
            def request = new AdhocWIPDataRequest(wipdataSetupName, "Lot", lotId)
            def eqItem = request.getInputData().getDetails().addDetailsItem()
            def recipeItem = request.getInputData().getDetails().addDetailsItem()
            def stepItem = request.getInputData().getDetails().addDetailsItem()
            eqItem.setWIPDataName("SysEquipment")
            eqItem.setWIPDataValue(lot.getEquipmentId())
            recipeItem.setWIPDataName("SysRecipe")
            recipeItem.setWIPDataValue(lot.getRecipe())
            stepItem.setWIPDataName("SysStep")
            stepItem.setWIPDataValue(lot.getStep())
            def reply = cCamstarService.sendAdhocWipData(request)
            if (reply.isSuccessful())
            {
                logger.info(reply.getResponseData().getCompletionMsg())
            }
            else
            {
                logger.info(reply.getExceptionData().getErrorDescription())
            }
        }
    }
}