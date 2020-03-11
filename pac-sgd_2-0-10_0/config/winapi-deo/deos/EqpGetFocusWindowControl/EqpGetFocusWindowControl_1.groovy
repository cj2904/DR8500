package EqpGetFocusWindowControl

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledgeDto.Data.ParameterList
import de.znt.services.secs.dto.S2F42HostCommandAcknowledgeDto.Data.ParameterList.Parameter
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.TypeChecked;

@Deo(description='''
Request gateway to return current focus window control
''')
class EqpGetFocusWindowControl_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

	@DeoBinding(id="FormTitle")
	private String formTitle
    /**
     *
     */
    @DeoExecute(result="List")
    @TypeChecked
    public List execute()
    {
		List<String> paramList = new ArrayList<>()
		
		S2F41HostCommandSend request2 = new S2F41HostCommandSend(new SecsAsciiItem("GetFocusComponent"))
		def parameter = request2.getData().getParameterList().addParameter()
		parameter.setCPName(new SecsAsciiItem("FormTitle"));
		parameter.setCPValue(new SecsAsciiItem(formTitle));
		
		def reply2 = secsGemService.sendS2F41HostCommandSend(request2)
		if(reply2.isCommandAccepted() && reply2.getData().getParameterList().getSize() > 0)
		{
			ParameterList parameterList = reply2.getData().getParameterList()
			for (def i = 0; i < parameterList.getSize(); i++)
			{
				Parameter param = parameterList.getParameter(i)
				SecsAsciiItem component = (SecsAsciiItem) param.getCPAckComponent()
				
				paramList.add(component.getString())
			}
		}
		
		return paramList
    }
}