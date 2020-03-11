package EqpStartMachine_Common

import java.util.Map

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import sg.znt.pac.TscConfig
import sg.znt.pac.machine.CEquipment
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.zsecs.composite.SecsAsciiItem

@CompileStatic
@Deo(description='''
W06 common function:<br/>
<b>Sending remote command to start equipment</b>
''')
class EqpStartMachine_Common_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="Parameters")
    private Map<String, Object> parameters
    
    private SecsGemService secsGemService
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def startCommand = TscConfig.getStringProperty("Secs.RemoteCommand.Start.Name", "START")
        
        logger.info("Sending " + startCommand + " to equipment")
        def request =  new S2F41HostCommandSend(new SecsAsciiItem(startCommand))
        
        for (var in parameters) 
        {
            String key = var.getKey()
            String value = var.getValue()
            
            request.addParameter(new SecsAsciiItem(key) , new SecsAsciiItem(value))
        }
        
        secsGemService = (SecsGemService) cEquipment.getExternalService()
        
        S2F42HostCommandAcknowledge reply = secsGemService.sendS2F41HostCommandSend(request)
        logger.info(startCommand + " command reply: " + reply.getHCAckMessage())
        if (!reply.isCommandAccepted())
        {
            throw new Exception("Fail to start equipment with error: " + reply.getHCAckMessage())
        }
    }
}