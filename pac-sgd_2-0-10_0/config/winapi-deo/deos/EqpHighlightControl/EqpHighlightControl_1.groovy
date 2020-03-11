package EqpHighlightControl

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.TypeChecked;

@Deo(description='''
Request gateway to highlight selected control
''')
class EqpHighlightControl_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="FormTitle")
    private String formTitle

    @DeoBinding(id="ControlId")
    private String controlId

    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
    	S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("Highlight"))
		def parameter = request.getData().getParameterList().addParameter()
		parameter.setCPName(new SecsAsciiItem("FormTitle"))
		parameter.setCPValue(new SecsAsciiItem(formTitle))
		
		def parameter2 = request.getData().getParameterList().addParameter()
		parameter2.setCPName(new SecsAsciiItem("ControlId"))
		parameter2.setCPValue(new SecsAsciiItem(controlId))
		
		secsGemService.sendS2F41HostCommandSend(request)
    }
}