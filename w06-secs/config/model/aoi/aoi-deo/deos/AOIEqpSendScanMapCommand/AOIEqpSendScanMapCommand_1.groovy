package AOIEqpSendScanMapCommand

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem

import java.lang.String
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
AOI specific function:<br/>
<b>pac to send SCANMAP command to equipment</b>
''')
class AOIEqpSendScanMapCommand_1 {


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
        
        S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("SCANMAP"))
        request.addParameter(new SecsAsciiItem("PORTNO"), new SecsAsciiItem("1"))
        def reply = secsGemService.sendS2F41HostCommandSend(request)
        if (reply.isCommandAccepted())
        {
            //ok
        }
        else
        {
            throw new Exception("Executing remote command SCANMAP failed, reply message: " + reply.getHCAckMessage())
        }
    }
}