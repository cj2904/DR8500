package EqpRequestVariable

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S1F3SelectedEquipmentStatusRequest
import de.znt.services.secs.dto.S1F4SelectedEquipmentStatusData
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsNumberItem
import de.znt.zsecs.composite.SecsU4Item;
import groovy.transform.TypeChecked

@Deo(description='''
Request variable from equipment
''')
class EqpRequestVariable_1 {


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())


	@DeoBinding(id="SecsGemService")
	private SecsGemService secsGemService

	@DeoBinding(id="VariableList")
	private List<String> variableList
	/**
	 *
	 */
	@DeoExecute(result="List")
	@TypeChecked
	public List execute()
	{
        if (secsGemService != null)
        {
            S1F3SelectedEquipmentStatusRequest request = new S1F3SelectedEquipmentStatusRequest()
            for (var in variableList)
            {
                request.addSVID(new SecsAsciiItem(var))
            }
            S1F4SelectedEquipmentStatusData reply = secsGemService.sendS1F3SelectedEquipmentStatusRequest(request)
            if(reply.getData().getSize() > 0)
            {
                List<String> value = new ArrayList<String>()
                for(int i = 0; i < reply.getData().getSize(); i++)
                {
                    def replyData = reply.getData().getSV(i)
                    if (replyData instanceof SecsNumberItem)
                    {
                        SecsNumberItem sv = (SecsNumberItem) reply.getData().getSV(i)
                        value.add(sv.getNumber(0).longValue() + "")
                    }
                    else
                    {
                        SecsAsciiItem sv = (SecsAsciiItem) reply.getData().getSV(i)
                        value.add(sv.getString())
                    }                    
                }
                
                return value
            }
        }		
		
		return null
	}
}