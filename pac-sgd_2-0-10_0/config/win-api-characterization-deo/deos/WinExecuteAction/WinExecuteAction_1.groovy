package WinExecuteAction

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.resources.i18n.CustomI18n
import sg.znt.services.zwin.ZWinApiAction
import sg.znt.services.zwin.ZWinApiService
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Execute the window action
''')
class WinExecuteAction_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="ZWinApiService")
    private ZWinApiService zWinApiService
    
    @DeoBinding(id="ZWinApiAction")
    private ZWinApiAction zWinApiAction
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        String actionName = zWinApiAction.getActionName()
        actionName = actionName.substring(actionName.indexOf(":") + 1)
        zWinApiAction.setActionName(actionName.trim()) // remove indent before firing action to gateway
        def result = zWinApiService.sendWinApiAction(zWinApiAction)
        if (!result.isSucessful())
        {
            throw new Exception(result.getErrorMessage())
        }
        else
        {
            logger.info(CustomI18n.localize("DEO.WinExecuteAction.Executed", zWinApiAction.getActionName()))
            def resultMap = result.getVariableResults()
            for (var in resultMap) {
                logger.info("Result: " + var.getKey() + "=" + var.getValue())
            }
        }
    }
}