package Test

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import sg.znt.services.camstar.CCamstarService
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
//import de.znt.services.camstar.CCamstarService
import de.znt.services.camstar.inbound.CamstarInboundRequest

@CompileStatic
@Deo(description='''
test
''')
class Test_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="CCamstarService")
	private CCamstarService cCamstarService
	
    @DeoBinding(id="ImpXml")
    private String impXml

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def inbound = new CamstarInboundRequest(impXml)
		def params = inbound.getInputData().getOutboundRequestInfo().getRequestParams()
		def valueStatus = params.getParameter("NewStatus").getValue()
		def valueReason = params.getParameter("NewReason").getValue()
		logger.info("Status:$valueStatus, Reason:$valueReason")
    }
}