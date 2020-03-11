package EczListAllRecipe

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.services.secs.dto.S7F19CurrentEPPDRequest

@Deo(description='''
Send S7F19 to equipment to list all available recipe
''')
class EczListAllRecipe_1 {


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
		def request = new S7F19CurrentEPPDRequest();
		def reply = secsGemService.sendS7F19CurrentEPPDRequest(request)
		def ppidList = reply.getPpidList()
		for(int i=0; i<ppidList.getSize(); i++)
		{
			SecsAsciiItem recipe = (SecsAsciiItem)ppidList.getPPID(i)
			def recipeId = recipe.getString()
			try
			{
				logger.info((i + 1) + ". Equipment recipe: $recipeId")
			}
			catch (Exception e)
			{
				de.znt.util.error.ErrorManager.handleError(e, this)
				logger.error(e.getMessage())
			}
		}
    }
}