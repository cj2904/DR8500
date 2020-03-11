package EqpUploadRecipeToCamstar

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Upload recipe body from equipment to Camstar
''')
class EqpUploadRecipeToCamstar_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    
    }
}