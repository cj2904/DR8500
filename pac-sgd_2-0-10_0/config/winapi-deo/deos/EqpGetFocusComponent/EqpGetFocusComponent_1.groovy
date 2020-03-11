package EqpGetFocusComponent

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledgeDto.Data.ParameterList
import de.znt.services.secs.dto.S2F42HostCommandAcknowledgeDto.Data.ParameterList.Parameter
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.TypeChecked;
import sg.znt.pac.SgdConfig

@Deo(description='''
Request gateway to find current focused component control id
''')
class EqpGetFocusComponent_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService
	
	@DeoBinding(id="UiIpAddress")
	private String uiIpAddr
	
	@DeoBinding(id="GatewayIpAddress")
	private String gatewayIpAddr

    /**
     *
     */
    @DeoExecute(result="ParameterList")
    @TypeChecked
    public String execute()
    {
		List<String> paramList = new ArrayList<>()
		
		boolean commandAccepted = false;
		if(uiIpAddr.equals(gatewayIpAddr) || SgdConfig.alwaysSwitchWindow())
		{
			S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("SwitchWindow"))
			def reply = secsGemService.sendS2F41HostCommandSend(request)
			if(reply.isCommandAccepted())
			{
				commandAccepted = true;
			}
		}
		else
		{
			commandAccepted = true;
		}
    	
		if(commandAccepted)
		{
			S2F41HostCommandSend request2 = new S2F41HostCommandSend(new SecsAsciiItem("GetFocusComponent"))
			def parameter = request2.getData().getParameterList().addParameter()
			parameter.setCPName(new SecsAsciiItem("FormTitle"));
			parameter.setCPValue(new SecsAsciiItem("[ACTIVE]"));
			
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
		}
		return paramList.toString()
    }
}