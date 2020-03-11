package WinGetFocusAction

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.services.zwin.ZWinApiService
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class WinGetFocusAction_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="ZWinApiService")
    private ZWinApiService zWinApiService
    
    /**
     *
     */
    @DeoExecute(result="ControlId")
    public String execute()
    {
        def controlId = ""
        def result = zWinApiService.sendWinApiAction("Ctrl_GetActiveControlFocus")
        if (!result.isSucessful())
        {
            throw new Exception(result.getErrorMessage())
        }
        else
        {
            def resultMap = result.getVariableResults()
            for (var in resultMap) {
                controlId = var.getValue()
                logger.info("Result: " + var.getKey() + "=" + var.getValue())
            }
        }
        return controlId
    }
}