package EqpShowTerminalMessage

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import groovy.transform.CompileStatic

@CompileStatic
@Deo(description='''
Send terminal message to equipment
''')
class EqpShowTerminalMessage_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="Parameter")
	private Map parameter
	
	@DeoBinding(id="SecsGemService")
	private SecsGemService secsGemService
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    	if(parameter.size() > 0)
    	{
			String message = parameter.get("Message")
			secsGemService.sendTerminalMessage((byte)0, message)
		}
    }
}