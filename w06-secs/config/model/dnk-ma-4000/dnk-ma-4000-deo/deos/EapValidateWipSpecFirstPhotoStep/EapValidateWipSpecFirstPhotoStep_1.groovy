package EapValidateWipSpecFirstPhotoStep

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import sg.znt.pac.W06Constants
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import de.znt.pac.deo.annotations.*
import java.lang.String

@CompileStatic
@Deo(description='''
Validate WIP Spec for First Photo Step<br/>
<b>If step notes required last step to be check, it will counter check wth wafer equipment last process step</b>
''')
class EapValidateWipSpecFirstPhotoStep_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="InputXml")
    private String inputXml

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    	def outbound = new W02TrackInLotRequest(inputXml)
		def stepNotes = outbound.getItemValue("StepNotes").toString()
		if (stepNotes.length() > 0)
		{
			def lastRunWorkflow = outbound.getItemValue(W06Constants.MES_WAFER_EQP_LAST_RUN_WORKFLOW).toString()
			if (!stepNotes.equalsIgnoreCase(lastRunWorkflow))
			{
				throw new Exception("Equipment last run Workflow '$lastRunWorkflow' not matched with expected last run Workflow '$stepNotes'!")
			}
		}
    }
}