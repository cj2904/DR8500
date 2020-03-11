package WczRequestProcessState

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.CompileStatic
import sg.znt.pac.util.WinApiEqpUtil
import sg.znt.services.zwin.ZWinApiService


@CompileStatic
@Deo(description='''
Request process state of the equipment
''')
class WczRequestProcessState_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="WinApiService")
    private ZWinApiService winApiService
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("RequestProcessState"))
        def reply = winApiService.sendS2F41HostCommandSend(request)
        def parameterList = reply.getData().getParameterList()
        def trackOutQty = -1
        def inputQty = 0
        if (parameterList.getSize()>0)
        {
            def param = parameterList.getParameter(0)
            SecsAsciiItem value = (SecsAsciiItem) param.getCPAckComponent()
            if(value != null)
            {
                def processSate = Integer.parseInt(WinApiEqpUtil.getWinApiParamValue(value))
                logger.info("RequestProcessState: " + processSate)
            }
            else
            {
                throw new Exception("Process state value not found!")
            }
        }
        else
        {
            throw new Exception("No process state found!")
        }
    }
}