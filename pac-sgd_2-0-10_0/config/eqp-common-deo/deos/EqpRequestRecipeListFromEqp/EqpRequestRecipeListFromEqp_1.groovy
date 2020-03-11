package EqpRequestRecipeListFromEqp

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService;
import de.znt.services.secs.dto.S7F19CurrentEPPDRequest;
import de.znt.services.secs.dto.S7F20CurrentEPPDDataDto.PpidList
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.TypeChecked

@Deo(description='''
Retrieve recipe list from equipment shared folder
''')
class EqpRequestRecipeListFromEqp_1 
{

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    
	@DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService
	
    /**
     *
     */
    @DeoExecute(result="ResultList")
	@TypeChecked
    public Collection execute()
    {
		S7F19CurrentEPPDRequest request = new S7F19CurrentEPPDRequest()
    	def reply = secsGemService.sendS7F19CurrentEPPDRequest(request)
		
		List<String> files = new ArrayList<String>()
		PpidList ppidList = reply.getPpidList()
		for(int i=0; i<ppidList.getSize(); i++) 
		{
			SecsAsciiItem recipe = (SecsAsciiItem)ppidList.getPPID(i)
			files.add(recipe.getString())
		}
		
		return files
    }
}