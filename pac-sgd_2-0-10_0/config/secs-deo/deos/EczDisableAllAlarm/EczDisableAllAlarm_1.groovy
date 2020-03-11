package EczDisableAllAlarm

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService

@Deo(description='''
Disable all equipment alarm
''')
class EczDisableAllAlarm_1 {
	
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
		 secsGemService.enableAllAlarms(false)
		 logger.info("All alarm is disabled!")
	}
}