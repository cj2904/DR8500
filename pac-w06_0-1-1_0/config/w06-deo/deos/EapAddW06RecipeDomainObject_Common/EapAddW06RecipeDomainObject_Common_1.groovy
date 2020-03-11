package EapAddW06RecipeDomainObject_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.W06Constants
import sg.znt.pac.domainobject.W06RecipeParameterManager
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.RecipeParameterUtil
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
W06 common function:<br/>
<b>Add recipe domain object</b>
''')
class EapAddW06RecipeDomainObject_Common_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
    
    @DeoBinding(id="W06RecipeParameterManager")
    private W06RecipeParameterManager w06RecipeParameterManager
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        W02TrackInLotRequest trackInLot = new W02TrackInLotRequest(inputXmlDocument)
        def cLot = cMaterialManager.getCLot(trackInLot.getContainerName())
        def eqpId = trackInLot.getResourceName()
        if (cLot == null)
        {
            throw new Exception("Lot " + trackInLot.getContainerName() + " does not exist")
        }
        
        String eqpRecipe = RecipeParameterUtil.getEqpRecipe(trackInLot.getRecipeParamList())

        if (eqpRecipe == null || eqpRecipe.length() == 0)
        {
            throw new Exception("Could not find EqpRecipe in Camstar recipe parameter")
        }
        else
        {
            def w06Recipe = w06RecipeParameterManager.createAndAddNewDomainObject(eqpId + "-" + eqpRecipe)
            w06Recipe.setRecipeName(eqpRecipe)
                
            def recipeNameList = w06Recipe.getRecipeNameList()
            for (recipeName in recipeNameList) 
            {
                def recipeParameter = w06Recipe.createAndAddRecipeParameter(recipeName)
                def recipeParamList = trackInLot.getRecipeParamList()
                for (recipeParam in recipeParamList) 
                {
					def recipeParamKey = recipeParam.getParamName()
                    if (recipeParamKey.startsWith(recipeName))
                    {
                        def fixValue = recipeParam.getParamValue()
                        def minValue = recipeParam.getMinDataValue()
                        def maxValue = recipeParam.getMaxDataValue()
                        def values = fixValue + "|" + minValue + "|" + maxValue
						
						recipeParamKey = recipeParamKey.replace(W06Constants.PAC_PROPERTY_CONTAINER_DOT_CHAR, W06Constants.PAC_PROPERTY_CONTAINER_DOT_CHAR_TOKEN_REPLACEMENT)
                        recipeParameter.addRecipeParameter(recipeParamKey, values)
                    }
                }
            }
        }
    }
}