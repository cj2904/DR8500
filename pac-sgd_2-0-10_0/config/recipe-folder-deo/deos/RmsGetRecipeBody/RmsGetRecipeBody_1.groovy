package RmsGetRecipeBody

import java.util.Collection;

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import sg.znt.services.rms.RmsService
import sg.znt.services.rms.RmsServiceImpl;
import sg.znt.services.rms.RmsServiceOperation
import groovy.transform.TypeChecked


@Deo(description='''
Request recipe body from RMS
''')

class RmsGetRecipeBody_1 {

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="RmsService")
    private RmsService rmsService

    @DeoBinding(id="RecipeId")
    private String recipeId

    @DeoExecute(result="RecipeBody")
	@TypeChecked
    public byte[] requestRecipeBodyFromRMS() 
    {
        return rmsService.getRecipe(recipeId);
    }
}