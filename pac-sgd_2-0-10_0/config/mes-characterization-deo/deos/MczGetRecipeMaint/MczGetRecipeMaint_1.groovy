package MczGetRecipeMaint

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.GetRecipeMaintRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl;

@CompileStatic
@Deo(description='''
Mes Get recipe maintenance
''')
class MczGetRecipeMaint_1 {


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
        GetRecipeMaintRequest request = new GetRecipeMaintRequest()
        request.getInputData().getObjectToChange().setName(recipe)
        request.getInputData().getObjectToChange().setRev(recipeRev + "")
        
        def reply = cCamstarService.getRecipeMaint(request)
        if(reply.isSuccessful())
        {
            logger.info(reply.getResponseData().toXmlString())
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
    }
}