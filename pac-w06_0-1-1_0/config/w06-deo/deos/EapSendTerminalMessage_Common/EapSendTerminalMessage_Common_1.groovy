package EapSendTerminalMessage_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import groovy.transform.CompileStatic

@CompileStatic
@Deo(description='''
eap send a terminal message to eqp to notify end user
''')
class EapSendTerminalMessage_Common_1
{

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="Message")
    private String message

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        secsGemService.sendTerminalMessage((byte) 0, message)
        logger.info("Message '$message' is sent to equipment!!!")
    }
}