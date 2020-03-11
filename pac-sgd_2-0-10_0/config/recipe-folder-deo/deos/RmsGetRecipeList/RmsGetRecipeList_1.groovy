package RmsGetRecipeList

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked
import sg.znt.services.rms.RmsServiceImpl
import sg.znt.services.rms.RmsServiceImpl.ServiceRecipes


@Deo(description='''
Request recipe list from RMS
''')

class RmsGetRecipeList_1 {

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="RmsServiceImpl")
    private RmsServiceImpl rmsServiceImpl
	
	@DeoBinding(id="Filter")
	private String filter

    @DeoExecute(result="RecipeList")
	@TypeChecked
    public Collection<ServiceRecipes> requestRecipeListFromRMS() 
    {
        ArrayList<ServiceRecipes> servicesRecipe = new ArrayList<ServiceRecipes>()

        servicesRecipe = ((ArrayList<ServiceRecipes>) rmsServiceImpl.getServicesRecipes(filter))

        for(ServiceRecipes serviceRecipes : servicesRecipe) 
        {
            def serviceRecipeList = serviceRecipes.getRecipeList()
            removeDuplitcateRecipeName(serviceRecipeList)
            serviceRecipeList.each 
            {
                System.out.println(serviceRecipes.getServiceName() + ": " + it)
            }
        }
        return servicesRecipe;
    }
    
    private void removeDuplitcateRecipeName(Collection<String> recipeNames)
    {
        Collection<String> tempColl = new ArrayList<String>()
        for(String recipeName : recipeNames)
        {
            def singleRecipeName = recipeName
            if(!tempColl.contains(singleRecipeName))
            {
                tempColl.add(singleRecipeName)
            }
        }
        
        recipeNames.clear()
        
        for(String recipeName : tempColl)
        {
            recipeNames.add(recipeName)
        }
    }
}