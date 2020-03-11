package MesTrackInOutboundResponse

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.camstar.inbound.CamstarInboundResponse;
import groovy.transform.TypeChecked

@Deo(description='''
Response Track In Outbound
''')
class MesTrackInOutboundResponse_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    /**
     *
     */
    @DeoExecute(result="ResultXmlDocument")
	@TypeChecked
    public String execute()
    {
		CamstarInboundResponse response = new CamstarInboundResponse("Reply from PAC (Success)")
		
		logger.info("ResultXmlDocument >>> " + response.createCamstarXmlDoc())
		
		return response.createCamstarXmlDoc()//toXmlString()
    }
}