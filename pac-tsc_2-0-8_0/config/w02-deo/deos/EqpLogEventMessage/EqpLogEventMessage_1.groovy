package EqpLogEventMessage

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked;

import java.lang.String

@Deo(description='''
Log event message
''')
class EqpLogEventMessage_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="EventName")
    private String eventName

    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
    	logger.info("Received event " + eventName)
    }
}