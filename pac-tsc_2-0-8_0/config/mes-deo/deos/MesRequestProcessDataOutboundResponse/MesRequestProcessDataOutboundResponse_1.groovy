package MesRequestProcessDataOutboundResponse

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.outbound.RequestProcessDataRequest
import sg.znt.services.camstar.outbound.RequestProcessDataResponse

@Deo(description='''
Response TrackOutData outbound
''')
class MesRequestProcessDataOutboundResponse_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


	@DeoBinding(id="InputXmlDocument")
	private String inputXmlDocument
	
	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager
    /**
     *
     */
    @DeoExecute(result="ResultXmlDocument")
	@TypeChecked
    public String execute()
    {
		RequestProcessDataRequest request = new RequestProcessDataRequest(inputXmlDocument)
		def lotId = request.getContainerName()
		def cLot = cMaterialManager.getCLot(lotId)
		
		RequestProcessDataResponse response = new RequestProcessDataResponse(true)
		response.getResponseData().setCompletionMsg("Reply from PAC (Success)")
		response.setTrackOutQty(cLot.getTrackOutQty() + "")
		
		logger.info("ResultXmlDocument >>> " + response.createCamstarXmlDoc())
		
		return response.createCamstarXmlDoc()
    }
}