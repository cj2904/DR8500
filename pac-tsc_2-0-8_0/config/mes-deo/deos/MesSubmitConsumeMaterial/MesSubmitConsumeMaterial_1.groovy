package MesSubmitConsumeMaterial

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked
import sg.znt.camstar.semisuite.service.dto.SetConsumeMaterialsRequest
import sg.znt.camstar.semisuite.service.dto.SetConsumeMaterialsResponse
import sg.znt.camstar.semisuite.service.dto.SetConsumeMaterialsRequestDto.InputData.ServiceDetails.ServiceDetailsItem
import sg.znt.pac.domainobject.MachineDomainObjectManager
import sg.znt.pac.exception.ValidationFailureException
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.RequestProcessDataRequest

@Deo(description='''
Submit consume material
''')
class MesSubmitConsumeMaterial_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="MachineDomainObjectManager")
    private MachineDomainObjectManager manager

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager
	
    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
		RequestProcessDataRequest outbound = new RequestProcessDataRequest(inputXmlDocument)
		def lotId = outbound.getContainerName()
		def resourceName = outbound.getInputData().getOutboundRequestInfo().getResourceName()
		try
		{
			def cLot = cMaterialManager.getCLot(lotId)
					
			def consumeQty = cLot.getTrackOutQty()// + cLot.getRejectQty()
			performConsumeMaterial(cLot, resourceName, consumeQty)
		}
		catch(Exception e)
		{
			e.printStackTrace()
		}
    }
	
	private void performConsumeMaterial(CLot cLot, String resourceName, Integer consumeQty)
	{
		def mainMachine = manager.getMachineSet("Main")
		if(mainMachine.isMaterialConsumed())
		{
			logger.info("Consume material request has been submit successfully, not allow to re-submit")
			return
		}
		SetConsumeMaterialsRequest request = new SetConsumeMaterialsRequest()
		request.getInputData().setContainer(cLot.getId())
		request.getInputData().setEquipment(resourceName)
		
		List<Map<String, String>> allMaterials = mainMachine.getEQMaterials()
		for (mat in allMaterials)
		{
			logger.info("Entry: " + mat.toMapString())
			String consumeFactor = mat.get("EQMaterial_ConsumeFactor")
			float balanceToConsume = consumeQty
			balanceToConsume = balanceToConsume * Float.parseFloat(consumeFactor)

			logger.info("Quantity to consume: " + balanceToConsume)
			
			String materialLot = mat.get("EQMaterial_MaterialLot")
			String materialPartName = mat.get("EQMaterial_MaterialPartName")
			String materialType = mat.get("EQMaterial_MaterialType")
			
			if(materialType.equals("Dice"))
			{
				logger.info("Material type = " + materialType)
				int submissionCount = 2
				ServiceDetailsItem detailItem = request.getInputData().getServiceDetails().addServiceDetailsItem()
				detailItem.setMaterialLotName(materialLot)
				detailItem.getMaterialPart().setName(materialPartName)
				detailItem.getMaterialPart().setUseROR("true")
				String materialConsumeType = mat.get("EQMaterial_ConsumeType")
				balanceToConsume = balanceToConsume / submissionCount
				logger.info("Consume " + materialType + " with quantity: " + balanceToConsume)
				if(materialConsumeType.equals("QTY2"))
				{
					String materialLotQty2 = Float.parseFloat(mat.get("EQMaterial_MaterialLotQty2"))
					if(balanceToConsume >= Float.parseFloat(materialLotQty2))
					{
						logger.info("Material quantity is less than required consume quantity, use material quantity: " + materialLotQty2)
						detailItem.setQtyToConsume(Float.parseFloat(materialLotQty2) + "")
					}
					else
					{
						detailItem.setQtyToConsume(balanceToConsume + "")
					}
				}
				else
				{
					String materialLotQty = Float.parseFloat(mat.get("EQMaterial_MaterialLotQty"))
					if(balanceToConsume >= Float.parseFloat(materialLotQty))
					{
						logger.info("Material quantity is less than required consume quantity, use material quantity: " + materialLotQty)
						detailItem.setQtyToConsume(Float.parseFloat(materialLotQty) + "")
					}
					else
					{
						detailItem.setQtyToConsume(balanceToConsume + "")
					}
				}
			}
			else
			{
				logger.info("Material type = " + materialType)
				ServiceDetailsItem detailItem = request.getInputData().getServiceDetails().addServiceDetailsItem()
				detailItem.setMaterialLotName(materialLot)
				detailItem.getMaterialPart().setName(materialPartName)
				detailItem.getMaterialPart().setUseROR("true")
				String materialConsumeType = mat.get("EQMaterial_ConsumeType")
				detailItem.setQtyToConsume(balanceToConsume + "")
			}
		}
		
		SetConsumeMaterialsResponse reply = cCamstarService.setConsumeMaterials(request)
		if(reply.isSuccessful())
		{
			logger.info(reply.getResponseData().getCompletionMsg())
			mainMachine.setConsumedMaterialFlag(true)
		}
		else
		{
			throw new ValidationFailureException(cLot.getId(), reply.getExceptionData().getErrorDescription())
		}
	}
}