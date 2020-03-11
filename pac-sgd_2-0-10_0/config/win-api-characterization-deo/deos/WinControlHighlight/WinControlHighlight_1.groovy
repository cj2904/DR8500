package WinControlHighlight

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.services.zwin.ZWinApiAction
import sg.znt.services.zwin.ZWinApiActionImpl
import sg.znt.services.zwin.ZWinApiService
import sg.znt.services.zwin.schema.ZWinApiSchema
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class WinControlHighlight_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="ZWinApiAction")
    private ZWinApiAction zWinApiAction
    
    @DeoBinding(id="ZWinApiService")
    private ZWinApiService zWinApiService
    
    @DeoBinding(id="ZWinApiSchema")
    private ZWinApiSchema _zWinApiSchema;
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def highlightAction = new ZWinApiActionImpl(_zWinApiSchema, "Ctrl_Highlight", zWinApiAction.getActionParamValues(), "")
        
        def result = zWinApiService.sendWinApiAction(highlightAction)
        if (!result.isSucessful())
        {
            throw new Exception(result.getErrorMessage())
        }
        else
        {
            logger.info(highlightAction.getActionName() + " executed")
            def resultMap = result.getVariableResults()
            for (var in resultMap) {
                logger.info("Result: " + var.getKey() + "=" + var.getValue())
            }
        }
    }
}