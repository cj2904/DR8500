package EapValidateLotState

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked;
import sg.znt.camstar.semisuite.service.dto.GetLotWIPMainRequest
import sg.znt.camstar.semisuite.service.dto.GetLotWIPMainResponse
import sg.znt.pac.exception.ValidationFailureException
import sg.znt.pac.machine.TscEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.CCamstarService

@Deo(description='''
Validate if Lot has been tracked in
''')
class EapValidateLotState_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="CCamstarService")
	private CCamstarService cCamstarService
	
	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager
	
	@DeoBinding(id="TscEquipment")
	private TscEquipment equipment
	
	@DeoBinding(id="Parameter")
	private Map parameter
	
	@DeoBinding(id="EventName")
	private String eventName
    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
		if(parameter.size() == 0)
		{
			throw new ValidationFailureException("", eventName + "::: No parameter found")
		}
			
		String lotId = parameter.get("LotName")
		CLot lot = cMaterialManager.getCLot(lotId)
		
		GetLotWIPMainRequest lotWipMainRequest = new GetLotWIPMainRequest(lotId)
		GetLotWIPMainResponse lotWipMainReply = cCamstarService.getLotWIPMain(lotWipMainRequest)
		if(lotWipMainReply.isSuccessful())
		{
			def eqList = lotWipMainReply.getResponseData().getEquipmentSelection().getEquipmentSelectionItems()
			boolean exist = false
			while(eqList.hasNext()) 
			{
				def eq = eqList.next()
				if(eq.getName().equals(equipment.getSystemId()))
				{
					exist = true
				}
			}
			if(!exist)
			{
				throw new ValidationFailureException(lotId, "Lot has not track in to machine")
			}
			else
			{
				def wipFlag = lotWipMainReply.getResponseData().getWIPFlagSelection()
				lot.setWIPFlagSelection(wipFlag)
				
				if(lot.getWipFlagValue(wipFlag).equals("tracked in"))
				{
					//ok
				}
				else
				{
					throw new ValidationFailureException(lotId, "Lot has not track in to machine")
				}
			}
		} 
		else
		{
			throw new ValidationFailureException(lotId, lotWipMainReply.getExceptionData().getErrorDescription())
		}
    }
}