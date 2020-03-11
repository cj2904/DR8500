package EqpCompleteLot

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.TypeChecked;

@Deo(description='''
End current job in equipment
''')
class EqpCompleteLot_1 {


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
        if (secsGemService != null)
        {
            S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("CompleteLot"))
            def reply = secsGemService.sendS2F41HostCommandSend(request)
            if(reply.isCommandAccepted())
            {
                //OK
            }
        }
    }
}