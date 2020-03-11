package FurnaceSendIdleCommand

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
KE-Furnace specific function:<br/>
<b>pac to send "IDLE            " command to equipment</b>
''')
class FurnaceSendIdleCommand_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    private SecsGemService secsGemService
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        cEquipment.updateControlState()
        def processState = cEquipment.updateProcessState().getStateName()
        if(cEquipment.getControlState().isRemote() && (processState.equalsIgnoreCase("NORMAL-END") || processState.equalsIgnoreCase("ABNORMAL-END")))
        {
            secsGemService = (SecsGemService) cEquipment.getExternalService()

            S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("IDLE            "))

            def reply = secsGemService.sendS2F41HostCommandSend(request)
            if (reply.isCommandAccepted())
            {
                //ok
            }
            else
            {
                throw new Exception("Executing remote command IDLE failed, reply message: " + reply.getHCAckMessage())
            }
        }
    }
}