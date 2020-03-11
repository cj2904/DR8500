package EapSendUnloadCommandToEqp

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsEvent
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager

@CompileStatic
@Deo(description='''
eap unload cassette from eqp
''')
class EapSendUnloadCommandToEqp_1
{

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="SecsEvent")
    private SecsEvent secsEvent

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

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
        def lot1, lot2
        def reports = secsEvent.getAssignedReports()
        for(report in reports)
        {
            lot1 = report.getPropertyContainer().getValueAsString("DV@CarrierID1", "")
            lot2 = report.getPropertyContainer().getValueAsString("DV@CarrierID2", "")
        }

        logger.info("Jimmy Lots: '$lot1', '$lot2'")

        def portList = cEquipment.getPortList()
        def batchId, eqpId
        def lot1Found = false, lot2Found = false

        for(port in portList)
        {
            def lotA = port.getPropertyContainer().getString(lot1, "")
            if(lotA.equalsIgnoreCase(lot1))
            {
                lot1Found = true
            }

            if(lot1Found)
            {
                lot2Found = true
                batchId = port.getPropertyContainer().getString("EqpBatchId", "")
                logger.info("Found Port: '$eqpId', batch: '$batchId'!!")
                break
            }
        }

        if(!lot1Found)
        {
            // skip
            // logger
            logger.info("Received Event: lot1: '$lot1' and lot2: '$lot2' not found in persistance!!! Is Track In Lot!!!")
            def msg = "Received Event 'Material Received'. Please proceed to Camstar Track In."

        }
        else if(!lot2Found)
        {
            // logger
            // do not perform sendUnload
            def msg = "Received Event: lot1: '$lot1' found in eqp: '$eqpId' but lot2: '$lot2' not found in eqp: '$eqpId'"
            logger.info(msg)
            sendTerminalMessage(msg)
        }
        else if(lot1Found && lot2Found)
        {
            if(lot1.length() > 0 && lot2.length() > 0)
            {
                sendUnloadCommand(batchId)
                def msg = "Received Event: Please unload lots: '$lot1' and '$lot2' for batchId '$batchId' from equipment: '$eqpId'."
                sendTerminalMessage(msg)
            }
            else
            {
                throw new Exception("Received Event with lots: '$lot1' and '$lot2'. There is empty lot value in received events. Please verify!!!")
            }
        }
    }

    void sendUnloadCommand(String batchId)
    {
        def request =  new S2F41HostCommandSend(new SecsAsciiItem("UNLOAD"))
        request.addParameter(new SecsAsciiItem("BATCHID") , new SecsAsciiItem(batchId))

        S2F42HostCommandAcknowledge response = secsGemService.sendS2F41HostCommandSend(request)
        logger.info "PPSelect command : " + response.getHCAckMessage()

        if(!response.isCommandAccepted())
        {
            throw new Exception("Equipment Reply Error Message: '" + response.getHCAckMessage() + "'")
        }
    }

    void sendTerminalMessage(String message)
    {
        SecsGemService secsService = (SecsGemService) cEquipment.getExternalService()
        secsService.sendTerminalMessage((byte)0, message)
    }
}