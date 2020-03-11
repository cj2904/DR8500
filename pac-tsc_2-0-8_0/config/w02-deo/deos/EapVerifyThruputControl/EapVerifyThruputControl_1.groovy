package EapVerifyThruputControl

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Verify thruputcontrol
''')
class EapVerifyThruputControl_1 {

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())
	
	@DeoBinding(id="MaterialManager")
	private CMaterialManager materialManager
	
	@DeoBinding(id="InputXmlDocument")
	private String inputXmlDocument
	
	@DeoBinding(id="CEquipment")
	private CEquipment cEquipment
	
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		W02TrackInLotRequest request = new W02TrackInLotRequest(inputXmlDocument)
		def allLot = materialManager.getCLotList(new LotFilterAll())
		def container = cEquipment.getPropertyContainer()
		for (lot in allLot)
		{
			def equipmentId = cEquipment.getName()
			if (lot.getEquipmentId().equalsIgnoreCase(equipmentId))
			{
				def lotProcessCapabilityValue = lot.getProcessCapability()
				if (lotProcessCapabilityValue != "")
				{
					def allRecipe = lot.getAllRecipeObj()
					for (recipe in allRecipe)
					{
						if (recipe.isSubRecipe())
						{
							if (recipe.getMainEquipmentId() == equipmentId)
							{
								def thruputControlValue = recipe.getPropertyContainer().getString("ThruputControl", "")
								if (thruputControlValue != "")
								{
									if (thruputControlValue == lotProcessCapabilityValue)
									{
										def ceqList = request.getChildEquipmentList()
										for (ceq in ceqList)
										{
											if (ceq.CHILD_EQUIPMENT_ID == recipe.getEquipmentId())
											{
												def thurputValue = ceq.PM_LIST.PM_THRUPUT_QTY2
												def equipmentLotCountValue = container.getString(lot.getEquipmentId() + "_BatchEquipmentLotCount","0")
												if (thurputValue != "0" || equipmentLotCountValue != "0")
												{
													throw new Exception("Child eqp '" + ceq.CHILD_EQUIPMENT_ID + "' thruput value ($thurputValue) or eqp lot count value ($equipmentLotCountValue) is not equal 0!")
												}
												else
												{
													logger.info("Child eqp '" + ceq.CHILD_EQUIPMENT_ID + "' thruput value : $thurputValue, eqp lot count value : $equipmentLotCountValue")
												}
											}											
										}										
									}
									else
									{
										logger.info("Recipe '" + recipe.getRecipeName() + "' thruput control $thruputControlValue is not same as lot process capability $lotProcessCapabilityValue!")
									}
								}
								else
								{
									logger.info("Recipe '" + recipe.getRecipeName() + "' has no thruput control setup!")
								}
							}
						}
					}
				}
				else
				{
					logger.info("Lot '" + lot.getId() + "' has no process capability!")
				}
			}
		}
    }
}