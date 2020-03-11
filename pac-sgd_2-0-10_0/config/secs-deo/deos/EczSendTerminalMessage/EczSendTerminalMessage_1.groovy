package EczSendTerminalMessage

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService

@Deo(description='''
Send terminal message to equipment
''')
class EczSendTerminalMessage_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="Message")
    private String message

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        secsGemService.sendTerminalMessage((byte)0, message)
        logger.info("$message is sent")
    }
}