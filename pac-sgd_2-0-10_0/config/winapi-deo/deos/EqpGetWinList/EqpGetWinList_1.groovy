package EqpGetWinList

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService;
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.services.secs.dto.S2F42HostCommandAcknowledgeDto.Data.ParameterList
import de.znt.services.secs.dto.S2F42HostCommandAcknowledgeDto.Data.ParameterList.Parameter
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.TypeChecked;

@Deo(description='''
Get active window list from gateway
''')
class EqpGetWinList_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	
	@DeoBinding(id="SecsGemService")
	private SecsGemService secsGemService
    /**
     *
     */
    @DeoExecute(result="ParameterList")
    @TypeChecked
    public List<String> execute()
    {
		List<String> paramList = new ArrayList<>()
		
		S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("GetWinList"))
		S2F42HostCommandAcknowledge reply = secsGemService.sendS2F41HostCommandSend(request)
		
		if(reply.isCommandAccepted() && reply.getData().getParameterList().getSize() > 0)
		{
			ParameterList parameterList = reply.getData().getParameterList()
			for (def i = 0; i < parameterList.getSize(); i++) 
			{
				Parameter parameter = parameterList.getParameter(i)
				SecsAsciiItem component = (SecsAsciiItem) parameter.getCPAckComponent()
				
				paramList.add(component.getString())
			}
		}
		
		return paramList
    }
}