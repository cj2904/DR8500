package MesEquipmentMaterialSetupOutboundResponse

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import de.znt.pac.deo.annotations.*
import de.znt.services.camstar.inbound.CamstarInboundResponse

@CompileStatic
@Deo(description='''
Response EquipmentMaterialsSetup outbound
''')
class MesEquipmentMaterialSetupOutboundResponse_1 {


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