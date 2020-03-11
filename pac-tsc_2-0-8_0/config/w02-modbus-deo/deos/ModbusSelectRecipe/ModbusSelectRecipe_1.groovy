package ModbusSelectRecipe

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.SgdConfig
import sg.znt.pac.domainobject.Recipe
import sg.znt.pac.exception.ModbusException
import sg.znt.pac.machine.CEquipment;
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.modbus.W02ModBusService
import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Select recipe id at modbus equipment
''')
class ModbusSelectRecipe_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="W02ModBusService")
    private W02ModBusService w02ModBusService

    @DeoBinding(id="EquipmentId")
    private String equipmentId

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager materialManager
	
	@DeoBinding(id="CEquipment")
	private CEquipment cEquipment


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def allLot = materialManager.getCLotList(new LotFilterAll())
        for (lot in allLot)
        {
            if (lot.getEquipmentId().equalsIgnoreCase(equipmentId))
            {
                def allRecipe = lot.getAllRecipeObj()
                for (recipe in allRecipe)
                {
                    def logicalId = recipe.getEquipmentLogicalId()
					def chamber = SgdConfig.findChamberIdBySystemId(w02ModBusService.getChambers(),logicalId, equipmentId, cEquipment)
                    if (logicalId.length()>0 && chamber.length()==0 && recipe.isSubRecipe())
                    {
                        throw new Exception("Unknown chamber for equipment '" + logicalId + "'!")
                    }

                    logger.info("YY logicalId " + logicalId)
                    logger.info("YY chamber " + chamber)
                    logger.info("YY recipeName " + recipe.getRecipeName())
                    logger.info("YY getUsedRecipeName " + recipe.getUsedRecipeName())

                    if (!recipe.getRecipeName().equals(recipe.getUsedRecipeName()))
                    {
                        if (chamber.length()>0)
                        {
                            def selectRecipeValue = 0
                            if (recipe.getFixRecipe() !=null && recipe.getFixRecipe().trim().length()>0)
                            {
                                selectRecipeValue= Integer.parseInt(recipe.getFixRecipe())
                            }
                            else
                            {
                                selectRecipeValue =Integer.parseInt(recipe.getUsedRecipeName())
                            }
                            logger.info("YY1 SELECTRECIPE recipeName " + selectRecipeValue + " chamber " + chamber)
                            if(selectRecipeValue > 0)
                            {
                                w02ModBusService.setSelectRecipe(chamber, selectRecipeValue)
                            }
                            else
                            {
                                logger.error("YY1 SELECTRECIPE recipeName " + selectRecipeValue + " chamber " + chamber + " is NOT valid. Not Select Recipe at Eqp.")
                            }
                        }
                    }
                    else
                    {
                        if (!recipe.containSubRecipe() && chamber.length()>0)
                        {
                            def selectRecipeValue = 0
                            if (recipe.getFixRecipe() !=null && recipe.getFixRecipe().trim().length()>0)
                            {
                                selectRecipeValue= Integer.parseInt(recipe.getFixRecipe())
                            }
                            else
                            {
                                selectRecipeValue =Integer.parseInt(recipe.getUsedRecipeName())
                            }
                            logger.info("YY2 SELECTRECIPE recipeName " + selectRecipeValue + " chamber " + chamber)
                            if(selectRecipeValue > 0)
                            {
                                w02ModBusService.setSelectRecipe(chamber, selectRecipeValue)
                            }
                            else
                            {
                                logger.error("YY1 SELECTRECIPE recipeName " + selectRecipeValue + " chamber " + chamber + " is NOT valid. Not Select Recipe at Eqp.")
                            }
                        }
                    }
                }

                Thread.sleep(PacConfig.getIntProperty("ModbusVerifyRecipeDelay",1000))
                for (recipe in allRecipe)
                {
					def chamber = SgdConfig.findChamberIdBySystemId(w02ModBusService.getChambers(), recipe.getEquipmentLogicalId(), lot.getEquipmentId(), cEquipment)
                    if (chamber.length()>0 && !recipe.getRecipeName().equals(recipe.getUsedRecipeName()))
                    {
                        def selectRecipeValue = 0
                        if (recipe.getFixRecipe() !=null && recipe.getFixRecipe().trim().length()>0)
                        {
                            selectRecipeValue= Integer.parseInt(recipe.getFixRecipe())
                        }
                        else
                        {
                            selectRecipeValue =Integer.parseInt(recipe.getUsedRecipeName())
                        }
                        def currentRecipeValue = w02ModBusService.getCurrentRecipe(chamber)
                        logger.info("YY1 VERIFYRECIPE: $currentRecipeValue and $selectRecipeValue, equipment id-" + chamber)
                        if (selectRecipeValue != currentRecipeValue)
                        {
                            throw new ModbusException("Recipe '$selectRecipeValue' select failed at chamber " + chamber + ", equipment recipe is still '$currentRecipeValue'!",ModbusException.RECIPE_SELECT_FAILED)
                        }
                    }
                    else
                    {
                        if (!recipe.containSubRecipe() && chamber.length()>0)
                        {
                            def selectRecipeValue = 0
                            if (recipe.getFixRecipe() !=null && recipe.getFixRecipe().trim().length()>0)
                            {
                                selectRecipeValue= Integer.parseInt(recipe.getFixRecipe())
                            }
                            else
                            {
                                selectRecipeValue =Integer.parseInt(recipe.getUsedRecipeName())
                            }
                            def currentRecipeValue = w02ModBusService.getCurrentRecipe(chamber)
                            logger.info("YY2 VERIFYRECIPE: $currentRecipeValue and $selectRecipeValue, equipment id-" + chamber)
                            if (selectRecipeValue != currentRecipeValue)
                            {
                                throw new ModbusException("Recipe '$selectRecipeValue' select failed at chamber " + chamber +", equipment recipe is still '$currentRecipeValue'",ModbusException.RECIPE_SELECT_FAILED)
                            }
                        }
                    }
                }
                break
            }
        }
    }

    private void oldMethod()
    {
        /*
         * 
         def request = new W02TrackInLotRequest(inputXml)
         def lot = materialManager.getCLot(request.getContainerName())
         if (lot!=null)
         {
         def allRecipe = lot.getAllRecipeObj()
         for (recipe in allRecipe)
         {
         if (!recipe.getRecipeName().equals(recipe.getUsedRecipeName()))
         {
         w02ModBusService.setSelectRecipe(recipe.getEquipmentLogicalId(), Integer.parseInt(recipe.getUsedRecipeName()))
         }
         }
         }
         */
    }
}