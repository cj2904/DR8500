package EczStartMachine

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.zsecs.composite.SecsAsciiItem

@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EczStartMachine_1 
{
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
		def request =  new S2F41HostCommandSend(new SecsAsciiItem("START"))
		S2F42HostCommandAcknowledge response = secsGemService.sendS2F41HostCommandSend(request)
		logger.info "Start command : " + response.getHCAckMessage()
    }
}