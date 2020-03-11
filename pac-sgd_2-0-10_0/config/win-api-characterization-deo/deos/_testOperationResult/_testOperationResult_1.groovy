package _testOperationResult

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.services.zwin.ZWinApiOperation
import sg.znt.services.zwin.schema.ZWinApiSchema
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class _testOperationResult_1 {


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
		def list = new HashMap<String, String>()
		list.put("t1","tt")
		list.put("x","yy")
		
		def op2 = new ZWinApiOperation("Operation2",zWinApiSchema,list) 
		String str2 = op2.toJsonObject().toJson()
		println str2
    }
}