package MesPrepareLotSetEquipmentRecipe

import static sg.znt.pac.resources.i18n.TscI18n.localize
import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.GetEquipmentSetupRequest
import sg.znt.camstar.semisuite.service.dto.SetEquipmentSetupRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.TrackInLotRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Update equipment recipe based on track in outbound. Return error if the recipe is required to load.
''')
class MesPrepareLotSetEquipmentRecipe_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    
    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outboundRequest = new TrackInLotRequest(inputXmlDocument)
        def requiredRecipe = outboundRequest.getRecipeName()
        def currentRecipe = "" //outboundRequest.getCurrentRecipeName() TODO: Fill in the recipe
        
        if (!requiredRecipe.equalsIgnoreCase(currentRecipe))
        {
            def recipeName= outboundRequest.getRecipeName()
            def recipeRev= outboundRequest.getRecipeRevision()
            def eqId = outboundRequest.getResourceName()
            def req = new GetEquipmentSetupRequest()
            req.getInputData().setResource(eqId)
            def request = new SetEquipmentSetupRequest()
            request.getInputData().setEquipment(eqId)
            request.getInputData().setResource(eqId)
            request.getInputData().getRecipe().setName(recipeName)
            request.getInputData().getRecipe().setRev(recipeRev)
            def reply = cCamstarService.setEquipmentSetup(request)
            if (reply.isSuccessful())
            {
                logger.info(reply.getResponseData().getCompletionMsg())
                throw new Exception(localize("MesPrepareLotSetEquipmentRecipe.Notification.Error.RecipeChange"))
            }
            else
            {
                CamstarMesUtil.handleNoChangeError(reply)
            }
        }
    }
}