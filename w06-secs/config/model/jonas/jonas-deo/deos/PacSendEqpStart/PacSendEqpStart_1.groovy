package PacSendEqpStart

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import java.lang.String
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.services.secs.dto.S7F17DeleteProcessProgramSendDto.PpidList
import de.znt.services.secs.dto.S7F19CurrentEPPDRequest
import de.znt.services.secs.dto.S7F5ProcessProgramRequest

@CompileStatic
@Deo(description='''
Pac Send Eqp start
''')
class PacSendEqpStart_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="Recipe")
    private String recipe

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def request1=new S7F19CurrentEPPDRequest()
		def reply1=secsGemService.sendS7F19CurrentEPPDRequest(request1)
		def ppidList=reply1.getPpidList()
		//logger.info(ppidList.getSize().toString())
		for(int i=0;i<ppidList.getSize();i++)
		{
			SecsAsciiItem ppidItem=(SecsAsciiItem)ppidList.getPPID(i)
			def recipeName=ppidItem.getString()
			//def request2=new S7F5ProcessProgramRequest(ppidItem)
			//def reply2=secsGemService.sendS7F5ProcessProgramRequest(request2)
			//SecsBinary ppBodyItem=(SecsBinary) reply2.getData().getPPBODY();
			//def content = new String(ppBodyItem.getBytes())
			logger.info("Recipe- $recipeName")
		}
    }
}