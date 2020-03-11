package EqpPingConnection

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.services.zwin.ZWinApiException
import sg.znt.services.zwin.ZWinApiServiceImpl
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.zsecs.composite.SecsAsciiItem

@CompileStatic
@Deo(description='''
Verify if the equipment connection is drop
''')
class EqpPingConnection_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="SecsGemService")
    private ZWinApiServiceImpl winApiService

    /**
     *
     */
    @DeoExecute
    public void execute() {
        if (winApiService.isConnected()) 
        {
            try 
            {
                S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("Ping"));
                S2F42HostCommandAcknowledge reply = winApiService.sendS2F41HostCommandSend(request);
                if(reply.isCommandAccepted()) 
                {
                    //OK
                }
            }
            catch(ZWinApiException e)
            {
                if (e.getMessage().indexOf("Undefined command")==-1)
                {
                    e.printStackTrace()
                    logger.info("Detect possible failure, perform reconnect!")
                    winApiService.reconnect()
                }
            }
            catch (Exception e) {
                e.printStackTrace()
                logger.info("Detect possible failure, perform reconnect!")
                winApiService.reconnect()
            }
        }
    }
}