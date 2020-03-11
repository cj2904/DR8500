package MczCreateRecipeMaint

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import sg.znt.camstar.semisuite.service.dto.GetRecipeMaintNewRevRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl

@CompileStatic
@Deo(description='''
Create recipe in Camstar
''')
class MczCreateRecipeMaint_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="Recipe")
    private String recipe
    
    @DeoBinding(id="RecipeRev")
    private String recipeRev
    
    @DeoBinding(id="CCamstarServiceImpl")
    private CCamstarServiceImpl cCamstarService
    

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        GetRecipeMaintNewRevRequest request = new GetRecipeMaintNewRevRequest()
        request.createNewRev(recipe, recipeRev)
        
        def reply = cCamstarService.getRecipeMaintNewRev(request)
        if(reply.isSuccessful())
        {
            logger.info(reply.getResponseData().toXmlString())
        }
        else
        {
            def errDesc = reply.getExceptionData().getErrorDescription()
            def desc = "Recipe Base \"" + recipe + "\" not found"
            if(errDesc.equals(desc))
            {
                logger.info("Recipe base not found, create new recipe base")
                request = new GetRecipeMaintNewRevRequest()
                request.createNew(recipe, recipeRev + "")
                
                reply = cCamstarService.getRecipeMaintNewRev(request)
                if(reply.isSuccessful())
                {
                    logger.info(reply.getResponseData().toXmlString())
                }
            }
            CamstarMesUtil.handleNoChangeError(reply)
        }
    }
}