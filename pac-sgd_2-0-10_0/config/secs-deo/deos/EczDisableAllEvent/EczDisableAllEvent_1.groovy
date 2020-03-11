package EczDisableAllEvent

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F37EnableDisableEventReport;
import de.znt.services.secs.dto.S2F38EnableDisableEventReportAcknowledge;
import de.znt.zsecs.SecsMessage;

@Deo(description='''
Disable all events
''')
class EczDisableAllEvent_1 {


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
		def request = new S2F37EnableDisableEventReport();
		request.getData().setCEED(false);
		
		S2F38EnableDisableEventReportAcknowledge ack = secsGemService.sendS2F37EnableDisableEventReport(request);
		logger.info("Disable All Event Reply : " + ack.getERAck());	
    }
}