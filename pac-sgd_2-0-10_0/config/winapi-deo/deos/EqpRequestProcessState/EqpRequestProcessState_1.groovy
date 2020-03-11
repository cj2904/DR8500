package EqpRequestProcessState

import groovy.transform.TypeChecked

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.machine.CEquipment
import sg.znt.pac.util.WinApiEqpUtil
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem

@Deo(description='''
Request variable from equipment
''')
class EqpRequestProcessState_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


	@DeoBinding(id="SecsGemService")
	private SecsGemService secsGemService
	
	@DeoBinding(id="CEquipment")
	private CEquipment equipment
    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
        /**
		S1F3SelectedEquipmentStatusRequest request = new S1F3SelectedEquipmentStatusRequest(new SecsAsciiItem("ProcessState"))
		def reply = secsGemService.sendS1F3SelectedEquipmentStatusRequest(request)
		SecsAsciiItem vid = (SecsAsciiItem) reply.getData().getSV(0)
		if(vid != null && !vid.getString().contains(":"))
		{
			logger.info("Set equipment " + equipment.getName() + " process state to " + Integer.parseInt(vid.getString()))
			equipment.getProcessState().setState(Integer.parseInt(vid.getString()))
		}
		else
		{
			if(vid.getString().contains(":"))
			{
				String[] str = vid.getString().split(":")
				if(str.length > 1)
				{
					logger.info("Set equipment " + equipment.getName() + " process state to " + Integer.parseInt(str[1]))
					equipment.getProcessState().setState(Integer.parseInt(str[1]))
				}
			}
		}
		**/
        S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("RequestProcessState"))
        def reply = secsGemService.sendS2F41HostCommandSend(request)
        def parameterList = reply.getData().getParameterList()
        if (parameterList.getSize()>0)
        {            
            def param = parameterList.getParameter(0)
            SecsAsciiItem value = (SecsAsciiItem) param.getCPAckComponent()
            if(value != null)
            {
                def processSate = Integer.parseInt(WinApiEqpUtil.getWinApiParamValue(value))
                logger.info("Set equipment " + equipment.getName() + " process state to " + processSate)
                equipment.getProcessState().setState(processSate)
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