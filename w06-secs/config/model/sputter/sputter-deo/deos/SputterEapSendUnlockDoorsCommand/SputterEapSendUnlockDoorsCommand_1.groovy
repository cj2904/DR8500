package SputterEapSendUnlockDoorsCommand

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsComposite
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
Sputter specific function:<br/>
<b>pac to send UNLOCK_DOORS command to equipment</b>
''')
class SputterEapSendUnlockDoorsCommand_1 {


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
        secsGemService = (SecsGemService) cEquipment.getExternalService()
        
        S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("UNLOCK_DOORS"))
        def reply = secsGemService.sendS2F41HostCommandSend(request)
        if (reply.isCommandAccepted())
        {
            //ok
        }
        else
        {
            throw new Exception("Executing remote command UNLOCK_DOORS failed, reply message: " + reply.getHCAckMessage())
        }
    }
}