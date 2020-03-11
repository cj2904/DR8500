package EqpUpdateTubeProcessState_Semco

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import OutboundRequest.CommonOutboundRequest
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.SecsService
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
eqp update process state for each semco tube
''')
class EqpUpdateTubeProcessState_Semco_1
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
        cEquipment.updateProcessState()
        def secsService = (SecsService)secsGemService
        def outbound = new CommonOutboundRequest(inputXml)
        def eqpId = outbound.getContainerName()

        def portList = cEquipment.getPortList()

        for(port in portList)
        {
            if(port.getPortId().equalsIgnoreCase(eqpId))
            {
                def status = port.isReady2Start()
            }
        }
    }
}