package EqpDownloadGatewayConfig

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.TypeChecked

@Deo(description='''
Transfer stored configuration to gateway
''')
class EqpDownloadGatewayConfig_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
		S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("DownloadGatewayConfiguration"))
		def reply = secsGemService.sendS2F41HostCommandSend(request)
		if(reply.isCommandAccepted())
		{
			//ok
		}
    }
}