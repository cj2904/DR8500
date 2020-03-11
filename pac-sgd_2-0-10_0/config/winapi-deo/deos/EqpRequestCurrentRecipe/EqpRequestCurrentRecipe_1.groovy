package EqpRequestCurrentRecipe

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import sg.znt.pac.machine.CEquipment
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.services.secs.dto.S2F42HostCommandAcknowledgeDto.Data.ParameterList.Parameter
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.TypeChecked

@Deo(description='''
Request current recipe from equipment
''')
class EqpRequestCurrentRecipe_1 {


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
		S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("RequestCurrentRecipe"))
    	S2F42HostCommandAcknowledge reply = secsGemService.sendS2F41HostCommandSend(request)
		def parameterList = reply.getData().getParameterList()
		def param = parameterList.getParameter(0)
		if(param != null)
		{
			SecsAsciiItem value = (SecsAsciiItem) param.getCPAckComponent()
			if(!value.getString().contains(":"))
			{
				logger.info("Set equipment " + equipment.getName() + " current recipe to " + value.getString())
				equipment.setCurrentRecipe(value.getString())
			}
			else
			{
				if(value.getString().contains(":"))
				{
					String[] str = value.getString().split(":")
					if(str.length > 1)
					{
						logger.info("Set equipment " + equipment.getName() + " current recipe to " + Integer.parseInt(str[1]))
						equipment.setCurrentRecipe(str[1])
					}
				}
			}
		}
    }
}