package LjCvdCheckCommandText


import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S1F3SelectedEquipmentStatusRequest
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsComponent
import de.znt.zsecs.composite.SecsU4Item
import de.znt.zsecs.messages.S1F3
import de.znt.pac.deo.triggerprovider.secs.SecsEvent

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class LjCvdCheckCommandText_1 {


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
		def svid = new SecsU4Item(300024l)
		def request = new S1F3SelectedEquipmentStatusRequest(svid)
		def reply=secsGemService.sendS1F3SelectedEquipmentStatusRequest(request)
		SecsAsciiItem result=(SecsAsciiItem) reply.getData().getSV(0)
		def text=result.getString()
		if(!text.equals("OK"))
		{
			throw new Exception("Command fail due to text:$text")
		}
		logger.info(text)
    }
}