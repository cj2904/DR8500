package EqpDisconnect_W02

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.services.zwin.ZWinApiService
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem

@CompileStatic
@Deo(description='''
Issue disconnect command only if it's win api service
''')
class EqpDisconnect_W02_1 {


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
        if (secsGemService instanceof ZWinApiService)
        {
            S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("Disconnect"))
            secsGemService.sendS2F41HostCommandSend(request)
        }
        else
        {
            logger.info("Skip disconnect execution for '" + secsGemService.toString() + "'")
        }
    }
}