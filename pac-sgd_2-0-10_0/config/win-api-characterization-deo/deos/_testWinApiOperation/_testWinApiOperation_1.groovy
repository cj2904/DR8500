package _testWinApiOperation

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.services.zwin.ZWinApiOperation
import sg.znt.services.zwin.schema.ZWinApiSchema
import de.znt.pac.deo.annotations.*
import elemental.json.Json

@CompileStatic
@Deo(description='''
Test win api operation.
''')
class _testWinApiOperation_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="ZWinApiSchema")
    private ZWinApiSchema zWinApiSchema

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    	def op = new ZWinApiOperation("Operation2",zWinApiSchema)
		String str = op.toJsonObject().toJson()
		println str
		def msg = '''{"Operation":"Operation2","TID":"6","Actions":[{"Action":"SetTextField", "ErrorCode":"ABC Error!"},{"Action":"SetTextField","ErrorCode":"Error test!"},{"Action":"ReadTextField","Result":"32312"},{"Action":"ReadTextField","Result":"XXYY"}]}'''
		def obj = Json.parse(msg)
		def opResult = op.createResult(obj)
		println opResult.isSucessful()
		println opResult.getErrorMessage()
		def variableResults = opResult.getVariableResults()
		variableResults.each 
		{
			k,v->println "${k}:${v}"
        }
		
		def list = op.getActionList()
		for (var in list) 
		{
			println var.getVariableResult()
		}
    }
}