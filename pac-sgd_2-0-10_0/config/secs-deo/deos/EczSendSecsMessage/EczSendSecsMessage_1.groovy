package EczSendSecsMessage

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.SecsService
import de.znt.zsecs.SecsMessage
import de.znt.zsecs.sml.SmlAsciiParser

@Deo(description='''
Send SECS raw message to equipment
''')
class EczSendSecsMessage_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="PrimarySml")
    private String primarySml

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        SmlAsciiParser parser = new SmlAsciiParser()
        SecsMessage primaryMessage = parser.parse(primarySml)

        def secsService = (SecsService) secsGemService
        def replyMessage = secsService.requestMessage(primaryMessage);
        logger.info replyMessage.getSML() + "<" + new String(replyMessage.getContentAsBinary()) + ">"
    }
    
}