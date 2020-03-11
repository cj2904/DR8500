package EqpMesTrackOutSuceed

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem


@CompileStatic
@Deo(description='''
Notify track out succeed
''')
class EqpMesTrackOutSuceed_1 {


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
    public void execute()
    {
        S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("MesTrackOutSucceed"))
        
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