package MesRequestWIPDataOutboundResponse

import groovy.transform.TypeChecked

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.TscConfig
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.machine.TscEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.CCamstarServiceImpl
import sg.znt.services.camstar.outbound.RequestWipDataRequest
import de.znt.pac.deo.annotations.*
import de.znt.services.camstar.inbound.CamstarInboundResponse
import de.znt.services.secs.SecsGemService

@Deo(description='''
Response Data Collection Outbound
''')
class MesRequestWIPDataOutboundResponse_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarServiceImpl")
    private CCamstarServiceImpl cCamstarServiceImpl

    @DeoBinding(id="MainEquipment")
    private CEquipment equipment
	
	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager
	
	@DeoBinding(id="InputXmlDocument")
	private String inputXmlDocument
    
    /**
     *
     */
    @DeoExecute(result="ResultXmlDocument")
	@TypeChecked
    public String execute()
    {
		TscEquipment mainEquipment =(TscEquipment) equipment 
		RequestWipDataRequest outbound = new RequestWipDataRequest(inputXmlDocument)
		def lotId = outbound.getContainerName()
		def cLot = cMaterialManager.getCLot(lotId)
		
		String msg = "Reply from PAC (Success)"

		def notSupportWipData = cLot.getPropertyContainer().getString("NotSupport_WIP_Data", "")
		if(notSupportWipData.length() > 0)
		{
			msg = "WIP Data is not returned from equipment, kindly enter manually: " + notSupportWipData
            def secsGemService = mainEquipment.getExternalService()
            if (secsGemService != null && secsGemService instanceof SecsGemService)
            {
                ((SecsGemService)secsGemService).sendTerminalMessage((byte)2, msg)
            }
		}
		
        def lastMsg = mainEquipment.getPropertyContainer().getString("WipDataMessage", "")
        if (lastMsg.length()>0)
        {
        	msg = lastMsg
			if(TscConfig.getBooleanProperty("Show.NotSupport.WIPData.Message", false))
			{
				msg = msg + " " + lastMsg
			}
        }
		CamstarInboundResponse response = new CamstarInboundResponse(msg)
		
		logger.info("ResultXmlDocument >>> " + response.createCamstarXmlDoc())
		
		return response.createCamstarXmlDoc()//toXmlString()
    }
}