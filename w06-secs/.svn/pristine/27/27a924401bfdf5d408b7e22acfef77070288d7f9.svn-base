package EapVerifyEqpAlarmStatus

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsAlarm
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
eap verify eqp alarm status is set or clear
''')
class EapVerifyEqpAlarmStatus_1
{

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

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
        def alarmId = secsAlarm.getId()
        def alarmName = secsAlarm.getName()
        logger.info("Alarm Id: '" + alarmId + "', Alarm Name: '" + alarmName + "'")
        if(alarmName.equalsIgnoreCase("DummyDose"))
        {
            if(secsAlarm.isSet())
            {
                cEquipment.getPropertyContainer().setBoolean("DoseLimitReset", false)
                logger.info("Set 'DoseLimitReset' key to false")
            }
            else
            {
                cEquipment.getPropertyContainer().setBoolean("DoseLimitReset", true)
                logger.info("Set 'DoseLimitReset' key to true")
            }
        }
        else
        {
            logger.info("Receive Alarm Id: '$alarmId', Alarm Name: '$alarmName'.")
        }
    }
}