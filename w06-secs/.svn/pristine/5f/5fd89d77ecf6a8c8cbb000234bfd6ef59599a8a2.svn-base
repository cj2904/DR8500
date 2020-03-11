package FurnaceSendUnloadPdCommand

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsBinary
import de.znt.zsecs.composite.SecsComposite
import de.znt.zsecs.composite.SecsU1Item
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.W06Util

@CompileStatic
@Deo(description='''
KE-Furnace specific function:<br/>
<b>pac to send "UNLOAD_PD         " command to equipment</b>
''')
class FurnaceSendUnloadPdCommand_1
{


    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def secsGemService = (SecsGemService) cEquipment.getExternalService()

        def batchId = cEquipment.getPropertyContainer().getLong(cEquipment.getSystemId() + "_BatchID", new Long(-1)).longValue().toString()
        def batchLots = cEquipment.getPropertyContainer().getStringArray("LotSeq", new String[0])
        def batchQty = batchLots.size()

        def last = batchLots.last()
        logger.info("last lot in batch: '$last'!!!")

        def unloadType = cEquipment.getPropertyContainer().getString("UnloadType", "UNLOAD_PD")
        logger.info("Jimmy Unload Type: '$unloadType'")

        S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem(reserveStrLength(unloadType)))
        request.addParameter(new SecsAsciiItem("0002"), new SecsAsciiItem(reserveStrLength(batchId))) //TODO: temporary batch id, change it to Camstar batch id
        request.addParameter(new SecsAsciiItem("0004"), new SecsU1Item((short) batchQty)) //TODO: fill in number of Lots

        SecsComposite lotList = new SecsComposite() //TODO: add multiple Lot into list
        for(batchLot in batchLots)
        {
            lotList.add(new SecsAsciiItem(trimLotId(batchLot)))
        }

        while(lotList.size < 7)
        {
            lotList.add(new SecsAsciiItem(trimLotId("")))
        }
        request.addParameter(new SecsAsciiItem("0005"), lotList)

        SecsComposite waferQtyList = new SecsComposite() //TODO: add multiple wafer quantity into list
        for(batchLot in batchLots)
        {
            if(cMaterialManager.getCLot(batchLot) != null)
            {
                def sLot = cMaterialManager.getCLot(batchLot)
                def qty = sLot.getTrackInQty()
                waferQtyList.add(new SecsBinary((byte) qty))
            }
        }

        while(waferQtyList.size < 7)
        {
            waferQtyList.add(new SecsBinary((byte) 0))
        }
        request.addParameter(new SecsAsciiItem("0006"), waferQtyList)

        def reply = secsGemService.sendS2F41HostCommandSend(request)
        if (reply.isCommandAccepted())
        {
            //ok
            cEquipment.getPropertyContainer().removeProperty("UnloadType")
            cEquipment.getPropertyContainer().removeProperty("LotSeq")
        }
        else
        {
            throw new Exception("Executing remote command '" + unloadType + "' failed, reply message: " + reply.getHCAckMessage())
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