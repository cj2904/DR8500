package EapSendTerminalMessageToEqp

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsEvent
import de.znt.services.secs.SecsGemService
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
eap send customize terminal message to eqp
''')
class EapSendTerminalMessageToEqp_1
{
    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="SecsEvent")
    private SecsEvent secsEvent

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def batchId
        def reports = secsEvent.getAssignedReports()
        for(report in reports)
        {
            batchId = report.getPropertyContainer().getValueAsString("DV@BatchID", "")
            logger.info("Jimmy batch id: '$batchId'")
        }

        def eqpId
        def portList = cEquipment.getPortList()
        for(port in portList)
        {
            if(port.getPropertyContainer().getString("EqpBatchId", "").equalsIgnoreCase(batchId))
            {
                eqpId = port.getPortId()
            }
        }

        def message = "Equipment: '$eqpId' process for batch: '$batchId' is finish. Please proceed!!!"
        sendTerminalMessage(message)
    }

    void sendTerminalMessage(String message)
    {
        SecsGemService secsService = (SecsGemService) cEquipment.getExternalService()
        secsService.sendTerminalMessage((byte)0, message)
    }
}