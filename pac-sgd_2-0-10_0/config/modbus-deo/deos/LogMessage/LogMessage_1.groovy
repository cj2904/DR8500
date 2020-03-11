package LogMessage

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Test log message
''')
class LogMessage_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    	logger.info("Modbus Message Test")
    }
}