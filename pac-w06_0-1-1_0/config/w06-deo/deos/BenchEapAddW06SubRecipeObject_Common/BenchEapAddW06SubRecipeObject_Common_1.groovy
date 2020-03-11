package BenchEapAddW06SubRecipeObject_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.W06RecipeParameterManager
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.RecipeParameterUtil
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
W06 Bench specific logic:<br/>
<b>Add sub recipe domain object</b>
''')
class BenchEapAddW06SubRecipeObject_Common_1
{


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="W06RecipeParameterManager")
	private W06RecipeParameterManager w06RecipeParameterManager

	@DeoBinding(id="InputXmlDocument")
	private String inputXmlDocument

	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager
	/**
	 *
	 */
	@DeoExecute
	public void execute()
	{
		W02TrackInLotRequest request = new W02TrackInLotRequest(inputXmlDocument)
		def cLot = cMaterialManager.getCLot(request.getContainerName())
		if (cLot == null)
		{
			throw new Exception("Lot " + request.getContainerName() + " does not exist")
		}

		String subRecipes = RecipeParameterUtil.getSubRecipes(request.getRecipeParamList())

		if (subRecipes == null || subRecipes.length() == 0)
		{
			throw new Exception("Could not find SubRecipe in Camstar recipe parameter")
		}
		else
		{
			def w06Recipe = w06RecipeParameterManager.createAndAddNewDomainObject("ChamberRecipe")
			w06Recipe.setRecipeName(subRecipes)

			def recipeNameList = w06Recipe.getRecipeNameList()
			for (recipeName in recipeNameList)
			{
				def recipeParameter = w06Recipe.createAndAddChamberRecipeParameter(recipeName)
				def recipeParamList = request.getRecipeParamList()
				for (recipeParam in recipeParamList)
				{
					if (recipeParam.getParamName().startsWith(recipeName))
					{
						def fixValue = recipeParam.getParamValue()
						def minValue = recipeParam.getMinDataValue()
						def maxValue = recipeParam.getMaxDataValue()
						def values = fixValue + "|" + minValue + "|" + maxValue
						recipeParameter.addChamberRecipeParameter(recipeParam.getParamName(), values)
					}
				}
			}
		}
	}
}