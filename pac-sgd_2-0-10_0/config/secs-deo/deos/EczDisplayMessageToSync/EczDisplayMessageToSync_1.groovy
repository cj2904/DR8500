package EczDisplayMessageToSync

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import de.znt.pac.deo.annotations.*

@Deo(description='''
Display message to perform synchronization in process navigator
''')
class EczDisplayMessageToSync_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        logger.error("Please perform synchronization through process navigator!")
    }
}