package _modbusTestSetRecipeParam

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.domainobject.RecipeParameter
import sg.znt.pac.material.CMaterialManager
import de.znt.pac.PacConfig;
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
test set recipe parameter
''')
class _modbusTestSetRecipeParam_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager

    /**
     *
     */
    @DeoExecute
    public void execute()
	{
		def lot = cMaterialManager.createCLot("TEST1")
		lot.setRecipe("YY1")
		lot.setEquipmentId("TESTEQ1")
		cMaterialManager.addCLot(lot)
		def recipe = recipeManager.createDomainObject("YY1")
		recipe.setContainSubRecipe(false)
		recipe.setEquipmentId("TESTEQ1")
		recipe.setEquipmentLogicalId("TESTEQ1")
		recipe.setMainRecipeName("YY1")
		def param1 = new RecipeParameter("R_MAIN")
		param1.setParameterValue("1")
		def param2 = new RecipeParameter("R_FIX")
		param2.setParameterValue("31")
		def param3 = new RecipeParameter("Duration1.1")
		param3.setParameterValue("360")
		def param4 = new RecipeParameter("Duration2.1")
		param4.setParameterValue("180")
		def param5 = new RecipeParameter("Duration3.1")
		param5.setParameterValue("30")
		recipe.addElement(param1)
		recipe.addElement(param2)
		recipe.addElement(param3)
		recipe.addElement(param4)
		recipe.addElement(param5)
		recipeManager.addDomainObject(recipe)
    }
}