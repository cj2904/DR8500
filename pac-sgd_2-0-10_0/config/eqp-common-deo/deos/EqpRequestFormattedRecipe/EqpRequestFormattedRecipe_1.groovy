package EqpRequestFormattedRecipe

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S7F25FormattedProcessProgramRequest
import de.znt.zsecs.composite.SecsAsciiItem
import eqp.EqpUtil;
import groovy.transform.TypeChecked

import java.lang.String

@Deo(description='''
Fire S7F25 request formatted recipe from equipment
''')
class EqpRequestFormattedRecipe_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="RecipeId")
    private String recipeId

    /**
     *
     */
    @DeoExecute(result="ParameterMap")
	@TypeChecked
    public Map<String, String> execute()
    {
		def request = new S7F25FormattedProcessProgramRequest(new SecsAsciiItem(recipeId))
		def response = secsGemService.sendS7F25FormattedProcessProgramRequest(request)
		
		Map<String, String> formattedPP = new LinkedHashMap<String, String>()
		def commands = response.getData().getProcessCommands()
		for (int i = 0; i < commands.getSize(); i++)
		{
			def command = commands.getProcessCommand(i)
			def ccode = EqpUtil.getVariableData(command.getCCode())
					
			def parameters = command.getParameters()
			String value = null
			for (int j = 0; j < parameters.getSize(); j++)
			{
				if(value == null)
				{
					value = EqpUtil.getVariableData(parameters.getPParm(j))
					continue
				}
				value = value + "," + EqpUtil.getVariableData(parameters.getPParm(j))
			}
			
			formattedPP.put(ccode, value)
		}
		
		return formattedPP
    }
}