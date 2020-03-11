package EapSendRemoteStart

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
send remote start command to eqp
''')
class EapSendRemoteStart_1
{

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def processState = mainEquipment.getProcessState()
        def process = processState.getStateName()

        if(processState.isReady2Start())
        {
            def request =  new S2F41HostCommandSend(new SecsAsciiItem("START"))
            S2F42HostCommandAcknowledge response = secsGemService.sendS2F41HostCommandSend(request)
            logger.info "Start command : " + response.getHCAckMessage()
        }
        else
        {
            throw new Exception("Equpment: '$mainEquipment' current is in '$process' state!")
        }
    }
}