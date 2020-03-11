package EczEnableAllAlarm

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService

@Deo(description='''
Enable all equipment alarm
''')
class EczEnableAllAlarm_1 {
    
    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="SecsGemService")	
    private SecsGemService secsGemService

    /**
     * Enable all equipment alarm
     */
    @DeoExecute
    public void enableAllAlarm()
    {
         secsGemService.enableAllAlarms(true)
         logger.info("All alarm is enabled!")
    }
}