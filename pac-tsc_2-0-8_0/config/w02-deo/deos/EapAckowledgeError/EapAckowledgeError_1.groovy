package EapAckowledgeError

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.util.error.ErrorManager

@CompileStatic
@Deo(description='''
Acknowledge the error and not throwing
''')
class EapAckowledgeError_1 {


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
        ErrorManager.handleError(exception, this)
    }
}