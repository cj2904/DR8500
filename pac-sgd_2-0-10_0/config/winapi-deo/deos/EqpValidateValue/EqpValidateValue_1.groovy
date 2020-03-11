package EqpValidateValue

import java.util.Map;

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.TypeChecked


@Deo(description='''
Validate form UI value with require value
''')
class EqpValidateValue_1 {

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
        S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("ValidateValue"))
        
        def paramSet = parameters.entrySet()
        for (param in paramSet) 
        {
            def addParameter = request.getData().getParameterList().addParameter()
            addParameter.setCPName(new SecsAsciiItem(param.getKey()))
            addParameter.setCPValue(new SecsAsciiItem(param.getValue().toString()))
        }
        
        def reply = secsGemService.sendS2F41HostCommandSend(request)
        if(reply.isCommandAccepted())
        {
            //OK
        }
    }
}