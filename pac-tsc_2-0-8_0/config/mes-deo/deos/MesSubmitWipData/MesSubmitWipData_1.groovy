package MesSubmitWipData

import groovy.transform.TypeChecked

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.SetWIPDataRequest
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.machine.TscEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.RequestWipDataRequest
import de.znt.pac.deo.annotations.*

@Deo(description='''
Submit track out WIP data to MES
''')
class MesSubmitWipData_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="CCamstarService")
	private CCamstarService cCamstarService
	
	@DeoBinding(id="InputXmlDocument")
	private String inputXmlDocument
	
	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager
	
	@DeoBinding(id="TscEquipment")
	private CEquipment equipment
    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
		TscEquipment cEquipment= (TscEquipment) equipment
        cEquipment.getPropertyContainer().setString("WipDataMessage", "")
    	RequestWipDataRequest outbound = new RequestWipDataRequest(inputXmlDocument)
		def serviceName = outbound.getWipDataServiceName()
		def cLot = cMaterialManager.getCLot(outbound.getContainerName())
		if(cLot != null)
		{
			def wipDataMap = cLot.getWipDataMap(serviceName)
			if(wipDataMap != null && wipDataMap.size() > 0)
			{
				SetWIPDataRequest request = new SetWIPDataRequest()
				request.getInputData().setContainer(cLot.getId())
				request.getInputData().setEquipment(cEquipment.getSystemId())
				request.getInputData().setServiceName(serviceName)
				request.getInputData().setProcessType("NORMAL")
				
				for (entry in wipDataMap.entrySet()) 
				{
					def detailItem = request.getInputData().getDetails().addDetailsItem()
					detailItem.setWIPDataName(entry.getKey())
					detailItem.setWIPDataValue(entry.getValue())
				}
				
				def reply = cCamstarService.setWIPData(request)
				if(reply.isSuccessful())
				{
                    cEquipment.getPropertyContainer().setString("WipDataMessage", reply.getResponseData().getCompletionMsg())
					//ok
				}
				else
				{
					throw new Exception(reply.getExceptionData().getErrorDescription())
				}
			}
		}
		else
		{
            def exceptionMsg = "Lot " + outbound.getContainerName() + " not exist in pac"
			throw new Exception(exceptionMsg)
		}
    }
}