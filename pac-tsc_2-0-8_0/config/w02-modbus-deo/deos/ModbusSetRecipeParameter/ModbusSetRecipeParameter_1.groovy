package ModbusSetRecipeParameter

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.SgdConfig
import sg.znt.pac.machine.CEquipment;
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.modbus.W02ModBusService
import de.znt.pac.deo.annotations.*
import de.znt.pac.mapping.MappingManager

@CompileStatic
@Deo(description='''
Set recipe parameter base on mapping set
''')
class ModbusSetRecipeParameter_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="W02ModBusService")
    private W02ModBusService w02ModBusService

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager materialManager

    @DeoBinding(id="EqId")
    private String equipmentId

	@DeoBinding(id="MappingManager")
	private MappingManager mappingManager
	
	@DeoBinding(id="CEquipment")
	private CEquipment cEquipment

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def allLot = materialManager.getCLotList(new LotFilterAll())
		String errMsg = ""
		for (lot in allLot)
		{
			if (lot.getEquipmentId().equalsIgnoreCase(equipmentId))
			{
				
				def allRecipe = lot.getAllRecipeObj()
				for (recipe in allRecipe)
				{
                    if (!recipe.getMainEquipmentId().equalsIgnoreCase(equipmentId))
                    {
                        continue
                    }
                    if (recipe.getFixRecipe() ==null || recipe.getFixRecipe().trim().length()==0)
                    {
                        logger.info("Skip writing since no fix recipe!")
                        continue
                    }
					def eqId = recipe.getEquipmentLogicalId()
					def mainEqp = lot.getEquipmentId()
					def mappingName = mainEqp + "-WriteRecipeParam"
					def mappingset = mappingManager.getMappingSet(mappingName)
					if (mappingset !=null)
					{
						def mappings = mappingset.getMappings()
						def chamber = SgdConfig.findChamberIdBySystemId(w02ModBusService.getChambers(), eqId, mainEqp, cEquipment)
						
						for (mapping in mappings)
						{
							def source = mapping.getSources().get(0) //C1 ,RecipeParam1
							def sourceItem = mappingManager.getSchemaItem(source)
							def sourceChamber = mappingManager.getSchemaComponent(source).getName()
							
							if (!chamber.equalsIgnoreCase(sourceChamber))
							{
								continue
							}

							def address = Integer.parseInt(sourceItem.getUnit())
							def target = mapping.getTarget()  //C1-Duration, configure dummy param until 20
							def targetItem = mappingManager.getSchemaItem(target)
							def pureKey = targetItem.getName().replaceAll(chamber + "-", "")
							def value = recipe.getParamFinalValue(pureKey)
							if (value!=null && value.length()>0)
							{
								def shortValue = Double.parseDouble(value)
								w02ModBusService.writeHoldingRegisterShortValue(address, (short)shortValue)
							}
							else
							{
								w02ModBusService.writeHoldingRegisterShortValue(address, (short)0)
							}
						}
					}
				}
				
				break
			}
		}

    }
}