package ModbusVerifyRecipeParameter

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.SgdConfig
import sg.znt.pac.exception.ModbusException
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.PacUtils
import sg.znt.services.modbus.W02ModBusService
import de.znt.pac.deo.annotations.*
import de.znt.pac.mapping.MappingManager

@CompileStatic
@Deo(description='''
Verify recipe parameter from defined mapping
''')
class ModbusVerifyRecipeParameter_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="MaterialManager")
    private CMaterialManager materialManager

    @DeoBinding(id="W02ModBusService")
    private W02ModBusService w02ModBusService

    @DeoBinding(id="EquipmentId")
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
                    def eqId = recipe.getEquipmentLogicalId()
                    def mappingName = equipmentId + "-RecipeParam"
                    logger.info("This is the mapping name: '$mappingName'!")
                    if (recipe.getFixRecipe() !=null && recipe.getFixRecipe().trim().length()>0)
                    {
                        mappingName = equipmentId + "-RecipeParamFix"
                        logger.info("Using fix mapping name '" + mappingName + "'")
                    }
                    def mappingset = mappingManager.getMappingSet(mappingName)
                    if (mappingset !=null)
                    {
                        def mappings = mappingset.getMappings()
                        def chamber =  SgdConfig.findChamberIdBySystemId(w02ModBusService.getChambers(), eqId, equipmentId, cEquipment)
                        logger.info("MainEqpId:'$equipmentId' with ChildLogicalId:'$eqId' resolves chamber as '$chamber'")
                        if (eqId.length()>0 && chamber.length()==0 && recipe.isSubRecipe())
                        {
                            throw new Exception("Unknown chamber for equipment '" + eqId + "'!")
                        }

                        for (mapping in mappings)
                        {
                            def source = mapping.getSources().get(0) //C1 ,RecipeParam1
                            def sourceChamber = mappingManager.getSchemaComponent(source).getName()
                            logger.info("The sourceChamber: '$sourceChamber' for source: '$source'!")

                            if (!chamber.equalsIgnoreCase(sourceChamber))
                            {
                                continue
                            }
                            def sourceItem = mappingManager.getSchemaItem(source)
                            def address = Integer.parseInt(sourceItem.getUnit())
                            def target = mapping.getTarget()  //C1-Duration, configure dummy param until 20
                            def targetItem = mappingManager.getSchemaItem(target)
                            def pureKey = targetItem.getName().replaceAll(chamber + "-", "")
                            def value = recipe.getParamFinalValue(pureKey)
                            def maxValue = recipe.getMaxValueByUsedRecipe(pureKey)
                            def minValue = recipe.getMinValueByUsedRecipe(pureKey)
                            logger.info("This is the value: '$value' for puerKey: '$pureKey'!")
                            logger.info("This is the maxValue: '$maxValue' for puerKey: '$pureKey'!")
                            logger.info("This is the minValue: '$minValue' for puerKey: '$pureKey'!")

                            if(value == null)
                            {
                                value = ""
                            }

                            if(maxValue == null)
                            {
                                maxValue = ""
                            }

                            if(minValue == null)
                            {
                                minValue = ""
                            }

                            def modbusValue = w02ModBusService.readHoldingRegisterIntValue(address)
                            logger.info("address is $address for key $pureKey at $chamber, schema chamber is $sourceChamber")

                            if (value != null && value.length() > 0)
                            {
                                if(pureKey.equalsIgnoreCase("AcidType"))
                                {
                                    def shortValue = (int)Double.parseDouble(value)
                                    logger.info("This is the shortValue:'$shortValue', and this is the modbusValue:'$modbusValue'")
                                    if (modbusValue != shortValue)
                                    {
                                        errMsg = errMsg + "Acid Type '$modbusValue' at modbus address '$address' require TestWorkFlow to be completed! Please perform test workflow before proceed with Lot Track In!"
                                    }
                                }
                                else
                                {
                                    def shortValue = (int)Double.parseDouble(value)
                                    logger.info("This is the shortValue:'$shortValue', and this is the modbusValue:'$modbusValue'")
                                    if (modbusValue != shortValue)
                                    {
                                        errMsg = errMsg + "Value $pureKey at $eqId is $modbusValue, different with MES configure value $shortValue. "
                                    }
                                }
                            }

                            if (maxValue != null && maxValue.length() > 0)
                            {
                                if(modbusValue.toFloat() > PacUtils.valueOfFloat(maxValue, 0))
                                {
                                    errMsg = errMsg + "Value $pureKey at $eqId is $modbusValue larger than MES configure value $maxValue."
                                }
                            }

                            if (minValue != null && minValue.length() > 0)
                            {
                                if(modbusValue.toFloat() < PacUtils.valueOfFloat(minValue, 0))
                                {
                                    errMsg = errMsg + "Value $pureKey at $eqId is $modbusValue smaller than MES configure value $minValue."
                                }
                            }
                        }
                    }

                    if (errMsg.length()>0)
                    {
                        throw new ModbusException(errMsg,ModbusException.RECIPE_PARAM_COMAPRE_FAILED)
                    }
                }
            }
        }
    }
}