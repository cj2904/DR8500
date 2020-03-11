package EqpSelectRecipe_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.zsecs.composite.SecsAsciiItem
import sg.znt.pac.TscConfig
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
W06 common function:<br/>
<b>Sending remote command to load recipe, command to be send according to configuration key: <br>
'Secs.RemoteCommand.PpidSelect.Name'</b>
''')
class EqpSelectRecipe_Common_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="RecipeId")
    private String recipeId
    
    @DeoBinding(id="Parameters")
    private Map<String, Object> parameters
    
    private SecsGemService secsGemService
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def ppSelectCommand = TscConfig.getStringProperty("Secs.RemoteCommand.PpidSelect.Name", "PP_SELECT")
        
        logger.info("Sending " + ppSelectCommand + " with recipe ID '" + recipeId + "' to equipment")
        def request =  new S2F41HostCommandSend(new SecsAsciiItem(ppSelectCommand))
        
        for (var in parameters) 
        {
            String key = var.getKey()
            String value = var.getValue()
            
            request.addParameter(new SecsAsciiItem(key) , new SecsAsciiItem(value))
        }
        
        secsGemService = (SecsGemService) cEquipment.getExternalService()
        
        S2F42HostCommandAcknowledge reply = secsGemService.sendS2F41HostCommandSend(request)
        logger.info(ppSelectCommand + " command reply: " + reply.getHCAckMessage())
        if (!reply.isCommandAccepted())
        {
            throw new Exception("Fail to select recipe with error: " + reply.getHCAckMessage())
        }
    }
}