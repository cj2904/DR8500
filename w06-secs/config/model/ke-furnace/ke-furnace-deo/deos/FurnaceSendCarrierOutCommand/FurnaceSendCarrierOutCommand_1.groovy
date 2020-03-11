package FurnaceSendCarrierOutCommand

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
eap send carrier out command to eqp
''')
class FurnaceSendCarrierOutCommand_1
{


    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def secsGemService = (SecsGemService) cEquipment.getExternalService()

        S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("CARRIER_OUT     "))

        def reply = secsGemService.sendS2F41HostCommandSend(request)
        if (reply.isCommandAccepted())
        {
            //ok
        }
        else
        {
            throw new Exception("Executing remote command CARRIER_OUT failed, reply message: " + reply.getHCAckMessage())
        }
    }
}