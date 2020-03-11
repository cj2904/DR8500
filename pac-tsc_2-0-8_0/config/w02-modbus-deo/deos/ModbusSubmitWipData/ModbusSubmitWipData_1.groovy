package ModbusSubmitWipData

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.SetWIPDataRequest
import sg.znt.pac.TscConfig
import sg.znt.pac.domainobject.RecipeParameter
import sg.znt.pac.domainobject.WipDataDomainObject
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest
import sg.znt.services.modbus.W02ModBusService
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Submit wip data after track out complete.
''')
class ModbusSubmitWipData_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="W02ModBusService")
    private W02ModBusService w02ModBusService

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="MaterialManager")
    private CMaterialManager materialManager

    @DeoBinding(id="InputXml")
    private String inputXml

    @DeoBinding(id="CEquipment")
    private CEquipment equipment
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new W02CompleteOutLotRequest(inputXml)
        def eqId = request.getResourceName()
        def lotList = request.getLotList()
        if (lotList.size() == 0)
        {
            def lotId = request.getContainerName()
            handleWipData(eqId, lotId)
        }
        else
        {
            for (lotId in lotList) {
                handleWipData(eqId, lotId)
            }
        }
    }
    
    private void handleWipData(String eqId, String lotId)
    {
        logger.info("SubmitWipData for '$lotId' for '$eqId'")
        def lot = materialManager.getCLot(lotId)
        if (lot !=null)
        {
            def wipData = lot.getWipDataByEquipment(eqId)
            def moveOutWipData = wipData.getMoveOutWipDataItems()
            def mappingName = lot.getEquipmentId() + "-WipData"
            def wipDataMapping = w02ModBusService.readMappingValue(mappingName).getContainerMap()
            if (wipDataMapping.size()>0)
            {
                def wipDataRequest = new SetWIPDataRequest()
                wipDataRequest.getInputData().setEquipment(eqId)
                wipDataRequest.getInputData().setContainer(lotId)
                wipDataRequest.getInputData().setServiceName(WipDataDomainObject.SERVICE_TYPE_MOVE_OUT_WIP_DATA)
                wipDataRequest.getInputData().setProcessType("NORMAL")

                def keys = wipDataMapping.keySet()
                def setOnlyOnceForWater = false
                def setOnlyOnceForTemperature = false
                def specialHandling = false
                for (wipDataItem in moveOutWipData)
                {
                    def value = ""
                    if (wipDataItem.getId().equalsIgnoreCase(TscConfig.getStringProperty("WipData.WidthDepthSec.Name","Width depth sec")))
                    {
                        logger.info("Wip data '" + wipDataItem.getId() + "' is driveIn")
                        def allRecipe = lot.getAllRecipeObj()
                        for (recipe in allRecipe)
                        {
                            def fixRecipe = recipe.getFixRecipe()
                            if (fixRecipe != null && fixRecipe.length()>0)
                            {
                                def usedRecipe = recipe.getUsedRecipeName()
                                logger.info("Recipe " + usedRecipe + " contain R_FIX")
                                int total = 0;
                                String duration1Name = TscConfig.getStringProperty("Recipe.Param.Duration1.Name","Duration1") + "." + usedRecipe
                                String duration2Name = TscConfig.getStringProperty("Recipe.Param.Duration2.Name","Duration2") + "." + usedRecipe
                                for (RecipeParameter recipeParameter : recipe.getAllParam())
                                {
                                    if (recipeParameter.getParameterName().matches(duration1Name + "|" + duration2Name))
                                    {
                                        String paramValue = recipeParameter.getParameterValue();
                                        logger.info("Parameter name '" + recipeParameter.getParameterName() + "' is duration, value=$paramValue")
                                        total = total + PacUtils.valueOfInteger(paramValue)
                                        value = total.toString()
                                    }
                                }
                                logger.info("Wip data '" + wipDataItem.getId() + "' value=$value")                                
                            }            
                        }
                    }
                    else
                    {
                        specialHandling = false
                        def waferPattern = TscConfig.getStringProperty("WipDataMultipleWaterRegExp","W_.*[0-9]")
                        def temperaturePattern = TscConfig.getStringProperty("WipDataMultipleTemperatureRegExp","T_.*[0-9]")
                        def pattern = waferPattern + "|" + temperaturePattern
                        logger.info("Check WipData ["+ wipDataItem.getId()+"] Whether Match Pattern ["+ pattern +"]")
                        if(wipDataItem.getId().matches(waferPattern))
                        {
                            specialHandling = true
                            value = wipDataItem.getValue()
                            if (value == null || value.length() == 0)
                            {
                                try
                                {
                                    if (!setOnlyOnceForWater)
                                    {
                                        value = equipment.getPropertyContainer().getString(lot.getEquipmentId() + "_LastWaterResistence", "")
                                        wipDataItem.setValue(value)
                                        logger.info("[" + lot.getId() + "] Clone last water resistenace value '" + value + "'")
                                    }
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace()
                                }
                            }
                            setOnlyOnceForWater = true
                        }
                        else if(wipDataItem.getId().matches(temperaturePattern))
                        {
                            specialHandling = true
                            value = wipDataItem.getValue()
                            if (value == null || value.length() == 0)
                            {
                                try
                                {
                                    if (!setOnlyOnceForTemperature)
                                    {
                                        value = equipment.getPropertyContainer().getString(lot.getEquipmentId() + "_LastTemperature", "")
                                        wipDataItem.setValue(value)
                                        logger.info("[" + lot.getId() + "] Clone last temperature value '" + value + "'")
                                    }
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace()
                                }
                                
                            }
                            setOnlyOnceForTemperature = true
                        }
                        
                        if(!specialHandling && (value == null || value.length()==0))
                        {
                            value = wipDataItem.getValue()
                            if (value == null || value.length() == 0)
                            {
                                value = wipDataMapping.getOrDefault(wipDataItem.getId(), "")
                            }
                            value = value.length()==0?wipDataItem.getDefaultValue():value
                            if(wipDataItem.getUomNotes().length()>0)
                            {
                                try
                                {
                                    double multiplyValue = Double.parseDouble(wipDataItem.getUomNotes())
                                    double conversionValue = Double.parseDouble(value)
                                    conversionValue = BigDecimal.valueOf(conversionValue).multiply(BigDecimal.valueOf(multiplyValue)).doubleValue()
                                    logger.info("Conversion applied, value="+ value + ",multiplyValue=" + multiplyValue + ",conversionValue=" + conversionValue)
                                    value = conversionValue + ""
                                }
                                catch (NumberFormatException ex)
                                {
                                    ex.printStackTrace()
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                    
                    if(value.length()>0)
                    {
                        def newItem = wipDataRequest.getInputData().getDetails().addDetailsItem()
                        newItem.setWIPDataName(wipDataItem.getId())
                        newItem.setWIPDataValue(value)
                    }
                }

                if (wipDataRequest.getInputData().getDetails().getDetailsItems().size()>0)
                {
                    def reply = cCamstarService.setWIPData(wipDataRequest)

                    if (reply.isSuccessful())
                    {
                        logger.info(reply.getResponseData().getCompletionMsg())
                    }
                    else
                    {
                        logger.info(reply.getExceptionData().getErrorDescription())
                    }
                }
            }
        }
    }
}