package MesCreateRecipeInCamstar

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked
import sg.znt.camstar.semisuite.service.dto.GetRecipeMaintNewRevRequest
import sg.znt.camstar.semisuite.service.dto.GetRecipeMaintRequest
import sg.znt.pac.util.RecipeUtil
import sg.znt.services.camstar.CCamstarService

@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class MesCreateRecipeInCamstar_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="RecipeName")
    private String recipeName

    @DeoBinding(id="CCamstarService")
    private CCamstarService camstarService
	
	@DeoBinding(id="EquipmentName")
	private String equipmentName
    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
		recipeName = RecipeUtil.getEqpRecipeFullNameWithoutExtension(recipeName, equipmentName)
		
		def revSeparatorIndex = recipeName.lastIndexOf("_")
		def recipe = recipeName.substring(0, recipeName.length())
		if(revSeparatorIndex > 0)
		{
			recipe = recipeName.substring(0, revSeparatorIndex)
		}
			
		def recipeRev = "0"
		if (revSeparatorIndex > 0)
		{
			recipeRev = recipeName.substring(revSeparatorIndex + 1, recipeName.length())
		}
		
		GetRecipeMaintRequest request = new GetRecipeMaintRequest()
		request.getInputData().getObjectToChange().setName(recipe)
		if(!recipe.equals(recipeRev))
		{
			request.getInputData().getObjectToChange().setRev(recipeRev + "")
		}
		
		def reply = camstarService.getRecipeMaint(request)
		if(reply.isSuccessful())
		{
			def msg = "Recipe exist in Camstar, skip adding new"
			logger.info(msg)
		}
		else
		{
			logger.info(reply.getExceptionData().getErrorDescription())
			logger.info("Recipe not found, create one")
			
			GetRecipeMaintNewRevRequest request2 = new GetRecipeMaintNewRevRequest()
			request2.createNewRev(recipe, recipeRev + "")
			
			def reply2 = camstarService.getRecipeMaintNewRev(request2)
			if(reply2.isSuccessful())
			{
				def msg2 = reply2.getResponseData().getCompletionMsg()
				logger.info(msg2)
			}
			else
			{
				def errDesc = reply2.getExceptionData().getErrorDescription()
				logger.error(errDesc)
				
				def desc = "Recipe Base \"" + recipe + "\" not found"
				logger.warn(desc)
				if(errDesc.equals(desc))
				{
					logger.info("Recipe base not found, create new recipe base")
					request2 = new GetRecipeMaintNewRevRequest()
					request2.createNew(recipe, recipeRev + "")
					
					reply2 = camstarService.getRecipeMaintNewRev(request2)
					if(reply2.isSuccessful())
					{
						def msg2 = reply2.getResponseData().getCompletionMsg()
						logger.info(msg2)
					}
					else
					{
						logger.error(reply2.getExceptionData().getErrorDescription())
						throw new Exception("Unable to create new recipe")
					}
				}
			}
		}
    }
}