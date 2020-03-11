package MesSetWebUIAddressToEquipment

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import sg.znt.camstar.semisuite.service.dto.SetEquipmentMaintRequest
import sg.znt.pac.machine.TscEquipment
import sg.znt.services.camstar.CCamstarService

@Deo(description='''
Set pac web UI address to equipment
''')
class MesSetWebUIAddressToEquipment_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="TscEquipment")
    private TscEquipment cEquipment

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		String pacService = PacConfig.getStringProperty("PacService", "");
		String computerName = Inet4Address.getLocalHost().getHostName();
		String ipAddress = Inet4Address.getLocalHost().getHostAddress();
		String servicePort = PacConfig.getStringProperty("GUI.WebServer.Port", "");
		String serviceUrl = "http://" + computerName + ":" + servicePort + "/" + pacService + "\n" + "http://" + ipAddress + ":" + servicePort + "/" + pacService;
		
    	SetEquipmentMaintRequest request = new SetEquipmentMaintRequest(cEquipment.getSystemId())
		request.getInputData().getObjectChanges().setNotes(serviceUrl)
		
		def reply = cCamstarService.setEquipmentMaint(request)
		if(reply.isSuccessful())
		{
			logger.info(reply.getResponseData().getCompletionMsg())
		}
		else
		{
			logger.error(reply.getExceptionData().getErrorDescription())
		}
    }
}