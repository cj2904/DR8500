package EqpThrowAlarmMsg

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsAlarm
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.CompileStatic

@CompileStatic
@Deo(description='''
eqp throw alarm when there is error happen
''')
class EqpThrowAlarmMsg_1
{

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

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
        def alarmName = secsAlarm.getName()
        def alarmText = secsAlarm.getText()

        logger.error("Received Alarm: '$alarmName' with message: '$alarmText'!!!")

        secsGemService.sendTerminalMessage((byte)0, reserveStrLength(alarmText, 63))
        logger.info("'$alarmText' is sent")

        def request = new S2F41HostCommandSend(new SecsAsciiItem(reserveStrLength("BUZZER_ON1", 16)))

        def reply = secsGemService.sendS2F41HostCommandSend(request)
        if(reply.isCommandAccepted())
        {
            logger.info("Alarm is triggered")
        }
        else
        {
            logger.error("Alarm is not accepted!!!")
        }
    }


    private String reserveStrLength(String str, Integer length)
    {
        while(str.length() < length)
        {
            str += " ";
        }

        return str
    }
}