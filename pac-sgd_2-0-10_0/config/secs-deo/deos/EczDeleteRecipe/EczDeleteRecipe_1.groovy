package EczDeleteRecipe

import static sg.znt.pac.resources.i18n.CustomI18n.localize

import org.apache.commons.logging.Log

import sg.znt.pac.SgdConfig
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S7F17DeleteProcessProgramSend
import de.znt.services.secs.dto.S7F18DeleteProcessProgramAcknowledge
import de.znt.zsecs.composite.SecsAsciiItem

@Deo(description='''
Delete a specific recipe
''')
class EczDeleteRecipe_1
{


    @DeoBinding(id="SecsService")
    public SecsGemService secsService

    @DeoBinding(id="Logger")
    public Log logger

    @DeoBinding(id="Recipe")
    private String recipe

    /**
     * Deletes all recipes. 
     * @param event to get the port no
     */
    @DeoExecute()
    public void deleteRecipe()
    {
        def recipeExt = SgdConfig.getEquipmentRecipeExt()
        def recipeIdWithoutExt = recipe.replaceAll(recipeExt , "")
        def recipeIdWithExt = recipeIdWithoutExt + recipeExt

        def formatedRecipe = recipeIdWithExt
        if(SgdConfig.trimRecipeExt("S7F17"))
        {
            formatedRecipe = recipeIdWithoutExt
        }
        S7F17DeleteProcessProgramSend deletePPSend = new S7F17DeleteProcessProgramSend();
        deletePPSend.getPpidList().addPPID(new SecsAsciiItem(formatedRecipe));

        S7F18DeleteProcessProgramAcknowledge deletePPAckn = secsService.sendSecsMessage(deletePPSend);
        byte result = deletePPAckn.getACKC7();
        if (result == 0x00)
        {
            logger.info("Recipe $recipe is deleted successfully")
        }
        else
        {
            throw new Exception("Fail to remove recipe $recipe from equpipment!")
        }
    }
}