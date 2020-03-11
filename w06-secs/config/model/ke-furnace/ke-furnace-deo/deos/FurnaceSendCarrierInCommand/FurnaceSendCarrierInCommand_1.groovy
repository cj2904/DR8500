package FurnaceSendCarrierInCommand

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsEvent
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
eap send carrier in command to eqp
''')
class FurnaceSendCarrierInCommand_1
{

    @DeoBinding(id="SecsEvent")
    private SecsEvent secsEvent

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
        def reports = secsEvent.getAssignedReports()
        for(report in reports)
        {
            def placement = report.getPropertyContainer().getValueAsString("SV@PODSTAGEINF.L", "NotFound")
            if(placement.equalsIgnoreCase("NotFound"))
            {
                placement = report.getPropertyContainer().getValueAsString("SV@PODSTAGEINF.R", "")
            }
            if(placement.length() > 0)
            {
                def secsGemService = (SecsGemService) cEquipment.getExternalService()
                S2F41HostCommandSend request

                if(placement.equalsIgnoreCase("1"))
                {
                    request = new S2F41HostCommandSend(new SecsAsciiItem(reserveStrLength("CARRIER_IN")))
                }
                else
                {
                    request = new S2F41HostCommandSend(new SecsAsciiItem(reserveStrLength("CARRIER_OUT")))
                }


                def reply = secsGemService.sendS2F41HostCommandSend(request)
                if (!reply.isCommandAccepted())
                {
                    throw new Exception("Executing remote command  failed, reply message: " + reply.getHCAckMessage())
                }
            }
            else
            {
                logger.error("Triggered event don't have 'SV@PODSTAGEINF.L', 'SV@PODSTAGEINF.R' VID value!!!")
            }
        }
    }

    private String reserveStrLength(String str)
    {
        while(str.length() < 16)
        {
            str += " ";
        }

        return str
    }
}