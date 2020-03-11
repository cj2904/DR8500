package EczDeleteAllRecipe

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S7F17DeleteProcessProgramSend
import de.znt.services.secs.dto.S7F18DeleteProcessProgramAcknowledge


@Deo(description='''
Delete all recipes
''')
class EczDeleteAllRecipe_1
{
	//EqpDeleteAllRecipe.Notification.Error.FailToDeleteAllReacipe.Description = Fail to delete all recipe from machine!
    private final String ERROR_FAIL_DELETE_ALL_RECIPE = "Fail to delete all recipe from machine!"

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        S7F17DeleteProcessProgramSend deletePPSend = new S7F17DeleteProcessProgramSend();
        S7F18DeleteProcessProgramAcknowledge deletePPAckn = secsGemService.sendSecsMessage(deletePPSend);
        byte result = deletePPAckn.getACKC7();
        if (result == 0x00)
        {
            logger.info("All recipe has been deleted!");
        }
        else
        {
            logger.error(ERROR_FAIL_DELETE_ALL_RECIPE);
        }
    }
}