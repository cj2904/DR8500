package EapCheckDummyLotState

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintRequest
import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintResponse
import sg.znt.camstar.semisuite.service.dto.GetLotWIPMainRequest
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Verify V-DOUBLETHRUPUT state is in MoveIn state
''')
class EapCheckDummyLotState_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="InputXmlDocument")
	private String inputXmlDocument
	
	@DeoBinding(id="CCamstarService")
	private CCamstarService cCamstarService
	
	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		W02TrackInLotRequest outbound = new W02TrackInLotRequest(inputXmlDocument)
		def lotId = outbound.getContainerName()
        def dummyLotId = outbound.getDummyLotId()
        def lot = cMaterialManager.getCLot(lotId)

        def childEqp = ""
		def allRecipe = lot.getAllRecipeObj()
		for (recipe in allRecipe)
		{
			childEqp = recipe.getEquipmentLogicalId()
			def recipeThruputFactor = recipe.getThruputFactor()
			if (recipeThruputFactor>1)
			{
				def req = new GetEquipmentMaintRequest()
				req.getInputData().getObjectToChange().setName(childEqp)
				req.getRequestData().getObjectChanges().initChildParameter("tscEqpReserved2")
				GetEquipmentMaintResponse res = new GetEquipmentMaintResponse()
				res.getResponseData().getObjectChanges().initChildParameter("tscEqpReserved2")
				res = cCamstarService.getEquipmentMaint(req)
				dummyLotId = res.getResponseData().getObjectChanges().getChildParameter("tscEqpReserved2").getValue()
				logger.info("GetEquipmentMaintRequest tscEqpReserved2 " + dummyLotId )

				logger.info("lotId " + lotId)
				logger.info("childEqp " + childEqp)
				logger.info("dummyLotId " + dummyLotId)
				logger.info("recipeThruputFactor " + recipeThruputFactor)

				def wipMainRequest = new GetLotWIPMainRequest(dummyLotId)
				def wipMainReply = cCamstarService.getLotWIPMain(wipMainRequest)
				if (wipMainReply.isSuccessful())
				{
					def wipFlag = wipMainReply.getResponseData().getWIPFlagSelection()
					def isPendingMoveIn = wipFlag.equals(CCamstarService.WIPFlag.MOVEIN.getValue())
					
					if (!isPendingMoveIn)
					{
						throw new Exception("$dummyLotId is not in 'move-in' state")
					}
					else
					{
						logger.info("DummyLot $dummyLotId is in state '$wipFlag'")
					}
				}
				break
			}
		}
    }
}