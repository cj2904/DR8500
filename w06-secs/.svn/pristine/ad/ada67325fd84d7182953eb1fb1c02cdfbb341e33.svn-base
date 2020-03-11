package FurnaceSendLoadPdCommand

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
import sg.znt.pac.W06Constants
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
KE-Furnace specific function:<br/>
<b>pac to send "LOAD_PD         " command to equipment</b>
''')
class FurnaceSendLoadPdCommand_1
{

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    private SecsGemService secsGemService
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        W02TrackInLotRequest trackInLot = new W02TrackInLotRequest(inputXmlDocument)
        def outboundLot = trackInLot.getContainerName()

        String isMonitor = trackInLot.getItemValue(W06Constants.MES_LOT_ATTR_TSC_MONITOR_LOT)
        logger.info("Monitor Lot value is '$isMonitor'")
        if(isMonitor.equalsIgnoreCase("true") || isMonitor.equalsIgnoreCase("1") || isMonitor.equalsIgnoreCase("yes"))
        {
            cEquipment.getPropertyContainer().setString("MonitorLot", outboundLot)
            cEquipment.getPropertyContainer().setString("LoadType", "LOAD_PM")
            cEquipment.getPropertyContainer().setString("UnloadType", "UNLOAD_PM")
        }

        secsGemService = (SecsGemService) cEquipment.getExternalService()

        def batchId = cEquipment.getPropertyContainer().getLong(trackInLot.getResourceName() + "_BatchID", new Long(-1)).longValue().toString()
        def batchLots = cEquipment.getPropertyContainer().getStringArray(trackInLot.getResourceName() + "_BatchTrackInLots", new String[0])
        def batchQty = batchLots.size()

        def lastLot = batchLots.last()

        if(outboundLot.equalsIgnoreCase(lastLot))
        {
            logger.info("last lot in batch: '$lastLot'!!!")
            def monitorLot = cEquipment.getPropertyContainer().getString("MonitorLot", "")
            def loadType = cEquipment.getPropertyContainer().getString("LoadType", "LOAD_PD")
            S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem(reserveStrLength(loadType)))
            //TODO: temporary batch id, change it to Camstar batch id
            request.addParameter(new SecsAsciiItem("0002"), new SecsAsciiItem(reserveStrLength(batchId)))
            request.addParameter(new SecsAsciiItem("0004"), new SecsU1Item((short) batchQty)) //TODO: fill in number of Lots

            SecsComposite lotList = new SecsComposite() //TODO: add multiple Lot into list
            def batchLts = new ArrayList<String>()
            logger.info("Batch lot: '$batchLots'")
            for(batchLot in batchLots)
            {
                if(monitorLot.equalsIgnoreCase(batchLot))
                {
                    logger.info("Jimmy monitor lot : '$monitorLot'")
                    continue
                }
                else
                {
                    logger.info("Jimmy not monitor lot : '$batchLot'")
                    lotList.add(new SecsAsciiItem(trimLotId(batchLot)))
                    batchLts.add(batchLot)
                }
            }

            if(monitorLot.length() > 0)
            {
                lotList.add(new SecsAsciiItem(trimLotId(monitorLot)))
                batchLts.add(monitorLot)
            }

            String[] lotSeq = batchLts.toArray()
            cEquipment.getPropertyContainer().setStringArray("LotSeq", lotSeq)

            while(lotList.size < 7)
            {
                lotList.add(new SecsAsciiItem(trimLotId("")))
            }
            request.addParameter(new SecsAsciiItem("0005"), lotList)

            def mLot
            SecsComposite waferQtyList = new SecsComposite() //TODO: add multiple wafer quantity into list
            for(batchLot in batchLots)
            {
                if(monitorLot.equalsIgnoreCase(batchLot))
                {
                    mLot = cMaterialManager.getCLot(monitorLot)
                    continue
                }
                else
                {
                    def sLot = cMaterialManager.getCLot(batchLot)
                    def qty = sLot.getTrackInQty()
                    waferQtyList.add(new SecsBinary((byte) qty))
                }
            }

            if(monitorLot.length() > 0)
            {
                def qty = mLot.getTrackInQty()
                waferQtyList.add(new SecsBinary((byte) qty))
                batchLts.add(monitorLot)
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
                cEquipment.getPropertyContainer().removeProperty("MonitorLot")
                cEquipment.getPropertyContainer().removeProperty("LoadType")
            }
            else
            {
                cEquipment.getPropertyContainer().removeProperty("MonitorLot")
                cEquipment.getPropertyContainer().removeProperty("LoadType")
                cEquipment.getPropertyContainer().removeProperty("UnloadType")
                cEquipment.getPropertyContainer().removeProperty("LotSeq")
                throw new Exception("Executing remote command '" + loadType + "' failed, reply message: " + reply.getHCAckMessage())
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

    private String trimLotId(String containerName)
    {
        def index = containerName.indexOf("-")
        String trimContainerName = containerName.substring(index + 1)
        logger.info("Trim Container name: '$trimContainerName'")

        return reserveStrLength(trimContainerName)
    }
}