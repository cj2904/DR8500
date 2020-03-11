package WinExecuteOperation

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.services.zwin.ZWinApiOperation
import sg.znt.services.zwin.ZWinApiService
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Execute the operation
''')
class WinExecuteOperation_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="ZWinApiService")
    private ZWinApiService zWinApiService

    @DeoBinding(id="ZWinApiOperation")
    private ZWinApiOperation zWinApiOperation
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def result = zWinApiService.sendWinApiOperation(zWinApiOperation)
        if (!result.isSucessful())
        {
            throw new Exception(result.getErrorMessage())
        }
        else
        {
            logger.info(zWinApiOperation.getOperationName() + " executed")
            def resultMap = result.getVariableResults()
            for (var in resultMap) {
                logger.info("Result: " + var.getKey() + "=" + var.getValue())
            }
        }
    
    }
}