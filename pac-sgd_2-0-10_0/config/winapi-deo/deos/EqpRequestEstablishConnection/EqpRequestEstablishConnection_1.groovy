package EqpRequestEstablishConnection

import java.util.Map.Entry

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S1F13EstablishCommunicationsRequestByHost
import groovy.transform.TypeChecked
import sg.znt.pac.machine.CEquipment
import sg.znt.services.secs.dto.SgdS1F14EstablishCommunicationsRequestAcknowledgeByEquipment
import sg.znt.services.secs.dto.SgdS1F14EstablishCommunicationsRequestAcknowledgeByEquipment.SgdEquipmentInfo

@Deo(description='''
Request establish connection with gateway
''')
class EqpRequestEstablishConnection_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


	@DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService
	
	@DeoBinding(id="CEquipment")
	private CEquipment cEquipment

    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
		S1F13EstablishCommunicationsRequestByHost request = new S1F13EstablishCommunicationsRequestByHost()
    	SgdS1F14EstablishCommunicationsRequestAcknowledgeByEquipment reply = (SgdS1F14EstablishCommunicationsRequestAcknowledgeByEquipment) secsGemService.sendS1F13EstablishCommunicationsRequest(request)
		
		if(reply.getEquipmentInfo() != null)
		{
			SgdEquipmentInfo info = (SgdEquipmentInfo) reply.getEquipmentInfo()
			List<Map<String, Object>> infoList = info.getEquipmentInfos()
			for (Map<String, Object> item : infoList) 
			{
				for (Entry<String, Object> entry : item.entrySet()) 					
				{
					if(entry.getKey().equals("IPAddress"))
					{
						if(cEquipment.getGatewayIpAddr().length() == 0)
						{
							String ipAddr = entry.getValue()
							cEquipment.setGatewayIpAddr(ipAddr)
							break;
						}
					}
				}
			}
		}
    }
}