package W02PCTAlarmMesAddResourceComment

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsAlarm
import de.znt.pac.deo.triggerprovider.secs.SecsControl
import de.znt.services.secs.SecsGemService
import de.znt.pac.deo.triggerprovider.secs.SecsEvent

@CompileStatic
@Deo(description='''
add resource comment for Alarm
''')
class W02PCTAlarmMesAddResourceComment_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="SecsAlarm")
    private SecsAlarm secsAlarm

    @DeoBinding(id="SecsControl")
    private SecsControl secsControl

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="SecsEvent")
    private SecsEvent secsEvent

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def alarmId = secsAlarm.getId()
        def alarmName = secsAlarm.getName()

    }
}