package EczSendAreYouThereRequest

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.SecsService
import de.znt.zsecs.SecsMessage
import de.znt.zsecs.sml.SmlAsciiParser

@Deo(description='''
Send are you there request to enquire equipment software and model
''')
class EczSendAreYouThereRequest_1 {


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
        //secsGemService.sendAreYouThereRequest()
        
        def parser = new SmlAsciiParser()
        String primarySml =
                '''
        S1F1 W <>.
        '''

        def primaryMessage = parser.parse(primarySml)

        def replyMessage = secsGemService.requestMessage(primaryMessage);
        logger.info "S1F2 sent with " + replyMessage.getSML()
    }
}