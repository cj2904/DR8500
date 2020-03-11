package LogModbusFloatValue

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Log Float Value
''')
class LogModbusFloatValue_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="Values")
    private float[] values

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    	logger.info("Value is:" + Arrays.toString(values))
    }
}