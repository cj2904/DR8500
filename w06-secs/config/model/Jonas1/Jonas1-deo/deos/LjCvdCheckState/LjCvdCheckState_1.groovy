package LjCvdCheckState

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S1F3SelectedEquipmentStatusRequest
import de.znt.zsecs.composite.SecsComponent
import de.znt.zsecs.composite.SecsU1Item
import de.znt.zsecs.composite.SecsU4Item
import de.znt.zsecs.messages.S1F3
import de.znt.pac.deo.triggerprovider.secs.SecsEvent
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class LjCvdCheckState_1 {


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
		def svid = new SecsU4Item(410000)
		def request = new S1F3SelectedEquipmentStatusRequest(svid)
		def reply=secsGemService.sendS1F3SelectedEquipmentStatusRequest(request)
		SecsU1Item result= (SecsU1Item) reply.getData().getSV(0)
		def state=result.getNumber(0)
		if(state!=3)
        {
            throw new Exception("State fail!!")
			logger.error("State fail!!")
		}
		else
		{
			logger.info("WAITING FOR START!!")
		}
    }
}