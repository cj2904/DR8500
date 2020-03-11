package EqpStartEquipment

import groovy.transform.TypeChecked

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.SecsServiceImpl
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.zsecs.composite.SecsAsciiItem

@Deo(description='''
Send remote start command to gateway
''')
class EqpStartEquipment_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="Parameters")
    private Map<String, Object> parameters
    
    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
		S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("START"))
    	S2F42HostCommandAcknowledge reply = secsGemService.sendS2F41HostCommandSend(request)
		if(reply.isCommandAccepted())
		{
			//OK
		}
        else
        {            
            def machineName = ""
            if (secsGemService instanceof SecsServiceImpl)
            {
                machineName = "[" + ((SecsServiceImpl)secsGemService).getZIdentifier().getZID() + "] "
            }
            throw new Exception(machineName + "Error sending start command to machine - " + reply.getHCAckMessage())       
        }
                
    }
}