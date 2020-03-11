package EapSendCustomSecsMessage_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.SecsService
import de.znt.zsecs.SecsMessage
import de.znt.zsecs.sml.SmlAsciiParser
import groovy.transform.CompileStatic

@CompileStatic
@Deo(description='''
send customize secs message to eqp
''')
class EapSendCustomSecsMessage_Common_1
{

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="PrimarySml")
    private String primarySml

    @DeoBinding(id="Trim")
    private Boolean trim = true

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        SmlAsciiParser parser = new SmlAsciiParser()
        SecsMessage primaryMessage = parser.parse(primarySml, trim)

        def secsService = (SecsService) secsGemService
        def replyMessage = secsService.requestMessage(primaryMessage);
        logger.info replyMessage.getSML() + "<" + new String(replyMessage.getContentAsBinary()) + ">"
    }
}