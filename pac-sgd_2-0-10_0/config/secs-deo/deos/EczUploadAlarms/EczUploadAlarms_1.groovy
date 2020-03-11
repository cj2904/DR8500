package EczUploadAlarms

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S5F5ListAlarmsRequest

@Deo(description='''
Upload alarms from equipment
''')
class EczUploadAlarms_1
{


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
        def request = new S5F5ListAlarmsRequest()

        def s5f6AlarmData =  secsGemService.sendS5F5ListAlarmsRequest(request)
        def resultSml = s5f6AlarmData.buildMessage()
        logger.info("Alarms:" + resultSml.getSML())
    }
}