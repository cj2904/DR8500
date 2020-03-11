package EqpUnloadLots

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest

@CompileStatic
@Deo(description='''
eap send unload command to eqp
''')
class EqpUnloadLots_1
{
    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="InputXml")
    private String inputXml

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outbound = new W02CompleteOutLotRequest(inputXml)
        def eqpId = outbound.getResourceName()
        def lotId = outbound.getContainerName()
        def lotList = outbound.getLotList()

        logger.info("This is the complete lot list: '$lotList'")

        def portList = cEquipment.getPortList()

        for(port in portList)
        {
            def portNum = port.getNumber().toString()
            if(port.getPortId().equalsIgnoreCase(eqpId))
            {
                def batchId = port.getPropertyContainer().getString("EqpBatchId", "")
                sendUnloadCommand(batchId)
                def msg = "Please proceed to unload all lots from port '$portNum'!!"
                logger.info(msg)
                sendTerminalMessage(msg)
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