package EapThrowError

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import java.lang.Throwable

@CompileStatic
@Deo(description='''
Throw Error to upper handler
''')
class EapThrowError_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="Exception")
    private Throwable exception

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    	throw exception
    }
}