package EqpUploadGatewayConfigRequest

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked
import sg.znt.pac.machine.CEquipment
import sg.znt.services.zwin.ZWinApiService

@Deo(description='''
Save upload config file in pac local drive
''')
class EqpUploadGatewayConfigRequest_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="Parameter")
	private Map parameter
	
	@DeoBinding(id="CEquipment")
	private CEquipment equipment
    
    @DeoBinding(id="ZWinApiService")
    private ZWinApiService winApiService
    
    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
		if(parameter.size() > 0)
		{
            winApiService.uploadGateWayConfigRequest(parameter.get("FileName", "") + "")
		}
    }
}