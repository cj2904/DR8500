package _testToken

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.services.zwin.ZWinApiOperation
import sg.znt.services.zwin.ZWinApiService
import sg.znt.services.zwin.schema.ZWinApiSchema
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class _testToken_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="ZWinApiService")
    private ZWinApiService zWinApiService
    
    @DeoBinding(id="ZWinApiSchema")
    private ZWinApiSchema zWinApiSchema
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def list = new HashMap<String, String>()
        list.put("MainViewWinTitle","Equipment Demo Software")
        list.put("SettingButton", "[CLASS:ThunderRT6CommandButton; INSTANCE:4]")
        
        def testActionOperation = new ZWinApiOperation("TestToken",zWinApiSchema,list)
        
        def result = zWinApiService.sendWinApiOperation(testActionOperation)
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