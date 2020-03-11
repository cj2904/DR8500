package FurnaceSendIdleRecipeCommand

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsComposite
import de.znt.zsecs.composite.SecsU1Item
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.util.W06Util

@CompileStatic
@Deo(description='''
eap send idle recipe command to eqp when cancel track in or track out
''')
class FurnaceSendIdleRecipeCommand_1
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

        def port = ""
        def batchId = cEquipment.getPropertyContainer().getLong(cEquipment.getSystemId() + "_BatchID", new Long(-1)).longValue().toString()
        def batchLots = cEquipment.getPropertyContainer().getStringArray(cEquipment.getSystemId() + "_BatchTrackInLots", new String[0])
        def batchQty = batchLots.size()

        S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem(reserveStrLength("PP_SELECT")))
        //        request.addParameter(new SecsAsciiItem("0001"), new SecsAsciiItem(reserveStrLength("")))
        request.addParameter(new SecsAsciiItem("0002"), new SecsAsciiItem((reserveStrLength(""))))
        request.addParameter(new SecsAsciiItem("0003"), new SecsAsciiItem(reserveStrLength("IDLE")))
        request.addParameter(new SecsAsciiItem("0004"), new SecsU1Item((short) 0))

        SecsComposite lotList = new SecsComposite()

        while(lotList.size < 7)
        {
            lotList.add(new SecsAsciiItem(trimLotId("")))
        }
        request.addParameter(new SecsAsciiItem("0005"), lotList)

        SecsComposite reserve = new SecsComposite()
        request.addParameter(new SecsAsciiItem("0012"), reserve)

        def reply = secsGemService.sendS2F41HostCommandSend(request)
        if (!reply.isCommandAccepted())
        {
            throw new Exception("Executing remote command PP_SELECT failed, reply message: " + reply.getHCAckMessage())
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

    private String trimLotId(String containerName)
    {
        String trimContainerName = W06Util.getLotIdWithTrimWorkOrder(containerName)
        return reserveStrLength(trimContainerName)
    }
}