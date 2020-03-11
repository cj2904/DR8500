package EqpValidateLotRecipeAvailability

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.SecsServiceImpl
import de.znt.services.secs.dto.S7F19CurrentEPPDRequest
import de.znt.services.secs.dto.S7F20CurrentEPPDDataDto.PpidList
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.TypeChecked
import sg.znt.pac.exception.ValidationFailureException
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.RecipeUtil
import sg.znt.services.zwin.ZWinApiServiceImpl

@Deo(description='''
Validate if Camstar track in recipe is exist in equipment shared folder
''')
class EqpValidateLotRecipeAvailability_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


	@DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

	@DeoBinding(id="CMaterialManager")
	private CMaterialManager materialManager
    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
		def lotList = materialManager.getCLotList(new LotFilterAll())
		def lot = lotList.get(0)
        def recipeId = RecipeUtil.getEqpRecipeFullName(lot)
   	
		S7F19CurrentEPPDRequest request = new S7F19CurrentEPPDRequest()
		def reply = secsGemService.sendS7F19CurrentEPPDRequest(request)
		
		List<String> files = new ArrayList<String>();
		PpidList ppidList = reply.getPpidList()
        for(int i = 0; i < ppidList.getSize(); i++)
        {
            SecsAsciiItem recipe = (SecsAsciiItem)ppidList.getPPID(i)
            def singleRecipeName = recipe.getString()
            if(!files.contains(singleRecipeName))
            {
                files.add(singleRecipeName);
            }
        }
			
		def exist = false
		for (recipe in files) 
		{
			if(recipe.startsWith(recipeId))
			{
				exist = true
				break
			}
		}
		
		if(!exist)
		{
            String name = ""
            if (secsGemService instanceof SecsServiceImpl || secsGemService instanceof ZWinApiServiceImpl)
            {
                name = secsGemService.getName()
            }
            
			throw new ValidationFailureException(lot.getId(), "Recipe " + recipeId + " not found in " + name + " equipment")
		}
    }
}