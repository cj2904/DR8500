package MesGetAvailableRecipeList

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked
import sg.znt.camstar.semisuite.service.dto.GetRecipeMaintFilterRequest
import sg.znt.pac.util.RecipeUtil
import sg.znt.services.camstar.CCamstarService

@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class MesGetAvailableRecipeList_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="RecipeName")
    private String recipeName

    @DeoBinding(id="CCamstarService")
    private CCamstarService camstarService
	
	@DeoBinding(id="EquipmentName")
	private String equipmentName
	
	private List<String> availableRecipes

    /**
     *
     */
    @DeoExecute(result="AvailableRecipes")
	@TypeChecked
    public List<String> execute()
    {
    	availableRecipes = new ArrayList<String>()
		
		recipeName = RecipeUtil.getEqpRecipeFullNameWithoutExtension(recipeName, equipmentName)
		
		def revSeparatorIndex = recipeName.lastIndexOf("_")
		def recipe = ""
		if(revSeparatorIndex > -1)
		{
			recipe = recipeName.substring(0, revSeparatorIndex)
		}
		else
		{
			recipe = recipeName
		}
					
		GetRecipeMaintFilterRequest request = new GetRecipeMaintFilterRequest(recipe)
		def reply = camstarService.getRecipeMaintFilter(request)
		if(reply.isSuccessful())
		{
			def records = reply.getRecipeRecordSetItems()
			for (rec in records) 
			{
				availableRecipes.add(rec.getName() + " : " + rec.getRevision())
			}
		}
		else
		{
			logger.info(reply.getExceptionData().getErrorDescription())	
		}
		
		return availableRecipes
    }
}