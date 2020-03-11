package WinExecuteActionName

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.services.zwin.ZWinApiService
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Execute action by name
''')
class WinExecuteActionName_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="ZWinApiService")
    private ZWinApiService zWinApiService
   
    @DeoBinding(id="ActionName")
    private String actionName
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {        
        def result = zWinApiService.sendWinApiAction(actionName)
        if (!result.isSucessful())
        {
            throw new Exception(result.getErrorMessage())
        }
        else
        {
            def resultMap = result.getVariableResults()
            for (var in resultMap) {
                logger.info("Result: " + var.getKey() + "=" + var.getValue())
            }
        }    
    }
}