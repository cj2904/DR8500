package EapCheckDummyDoseAlarm

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsAlarm
import groovy.transform.CompileStatic

@CompileStatic
@Deo(description='''
pac set and clear dummy dose flag
''')
class EapCheckDummyDoseAlarm_1
{

    @DeoBinding(id="SecsAlarm")
    private SecsAlarm secsAlarm

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def alarm = secsAlarm.getCode()
    }
}