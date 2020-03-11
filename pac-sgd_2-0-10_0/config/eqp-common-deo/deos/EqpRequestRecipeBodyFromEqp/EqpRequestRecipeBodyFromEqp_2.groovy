package EqpRequestRecipeBodyFromEqp

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S7F5ProcessProgramRequest
import de.znt.services.secs.dto.S7F6ProcessProgramData
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsBinary
import groovy.transform.TypeChecked

@Deo(description='''
Retrieve recipe body from equipment shared folder
''')
class EqpRequestRecipeBodyFromEqp_2
{
    private final String NOTIFICATION_INFO_RECIPE_FOUNDED = "EqpGetRecipeBody.Notification.Info.RecipeFounded.Description"
    private final String NOTIFICATION_INFO_RECIPE_NOT_FOUNDED = "EqpGetRecipeBody.Notification.Info.RecipeNotFounded.Description"

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	
    @DeoBinding(id="RecipeId")
    private String recipeId
	
	@DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    /**
     * Request recipe body from Equipment
     */
    @DeoExecute(result="RecipeBody")
	@TypeChecked
    public byte[] getRecipeBody()
    {
		S7F5ProcessProgramRequest request = new S7F5ProcessProgramRequest()
		request.setPPID(new SecsAsciiItem(recipeId))
		
        S7F6ProcessProgramData reply = secsGemService.sendS7F5ProcessProgramRequest(request)
		SecsBinary ppbody = (SecsBinary) reply.getData().getPPBODY()
		
		return ppbody.getBytes()
    }
}