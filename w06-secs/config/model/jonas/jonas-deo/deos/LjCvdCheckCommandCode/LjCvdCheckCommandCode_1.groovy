package LjCvdCheckCommandCode

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S1F3SelectedEquipmentStatusRequest
import de.znt.zsecs.composite.SecsComponent
import de.znt.zsecs.composite.SecsU4Item
import de.znt.zsecs.messages.S1F3
import de.znt.pac.deo.triggerprovider.secs.SecsEvent

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class LjCvdCheckCommandCode_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService   

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
	   def svid = new SecsU4Item(300023l)
       def request = new S1F3SelectedEquipmentStatusRequest(svid)
	   def reply=secsGemService.sendS1F3SelectedEquipmentStatusRequest(request)
	   SecsU4Item result =(SecsU4Item) reply.getData().getSV(0)
	   def code=result.getNumber(0)
	   if(code!=0) 
       {
		   throw new Exception("Command fail due to code:$code") 
		   
	   }	 
	   logger.info(code.toString())  
    }
}