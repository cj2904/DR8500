package EczSelectRecipe

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.zsecs.composite.SecsAsciiItem

@Deo(description='''
Send select recipe to equipment in SML format
''')
class EczSelectRecipe_1
{
	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="SecsGemService")
	private SecsGemService secsGemService
	
	@DeoBinding(id="PPSelectCommand")
	private String command
	
    @DeoBinding(id="PPID")
    private String ppid

    /**
     *
     */
    @DeoExecute
    public void execute()
    {		
		def request =  new S2F41HostCommandSend(new SecsAsciiItem(command))
		request.addParameter(new SecsAsciiItem("PPID") , new SecsAsciiItem(ppid))
		//request.addParameter(new SecsAsciiItem("PPID") , new SecsU2Item(Integer.parseInt(ptn)))
		//request.addParameter(new SecsAsciiItem("LOTID") , new SecsAsciiItem(lotId))
		S2F42HostCommandAcknowledge response = secsGemService.sendS2F41HostCommandSend(request)
		logger.info "PPSelect command : " + response.getHCAckMessage()
	
    }
}