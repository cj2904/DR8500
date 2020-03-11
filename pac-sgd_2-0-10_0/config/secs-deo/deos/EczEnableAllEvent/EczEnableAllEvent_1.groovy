package EczEnableAllEvent

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F37EnableDisableEventReport;
import de.znt.services.secs.dto.S2F38EnableDisableEventReportAcknowledge;
import de.znt.zsecs.SecsMessage;

@Deo(description='''
Enable All SECS Event
''')
class EczEnableAllEvent_1 {


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
    	S2F37EnableDisableEventReport request = new S2F37EnableDisableEventReport();
		request.getData().setCEED(true);
		
		S2F38EnableDisableEventReportAcknowledge ack = secsGemService.sendS2F37EnableDisableEventReport(request);
		logger.info("Enable All Event Reply : " + ack.getERAck());
    }
}