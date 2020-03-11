package EapVerifyMesRecipeParameter_FileBase

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.domainobject.filter.FilterAllDomainObjects
import de.znt.pac.mapping.MappingManager
import groovy.transform.CompileStatic
import sg.znt.pac.TscConfig
import sg.znt.pac.domainobject.Recipe
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import sg.znt.services.equipment.file.EquipmentFileHandler
import sg.znt.services.equipment.file.EquipmentFileService
import sg.znt.services.equipment.file.EquipmentFileServiceImpl

@CompileStatic
@Deo(description='''
Verify MES recipe parameters
''')
class EapVerifyMesRecipeParameter_FileBase_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="MappingManager")
    private MappingManager mappingManager

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager

    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataDomainObjectManager

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment

    @DeoBinding(id="isUniDirectional")
    private boolean isUniDirectional
    //
        @DeoBinding(id="EquipmentFileService")
        private EquipmentFileService equipmentFileService

    Recipe mainRecipe = null
    String[] paramterList = []

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outbounds = new W02TrackInLotRequest(inputXmlDocument)
        def outboundLot = outbounds.getContainerName()

        def lotLists = cMaterialManager.getCLotList(new LotFilterAll())
        def correct = false
        def eqpId

        for(lot in lotLists)
        {
            if(lot.getId().equalsIgnoreCase(outboundLot))
            {
                eqpId = lot.getEquipmentId()

                if(eqpId.equalsIgnoreCase(mainEquipment.getSystemId()))
                {
                    correct = true
                }
            }
        }

//        def prober = TscConfig.getTrackInProberEqp(eqpId)
        //        def inputPath = PacConfig.getStringProperty(prober + ".Path", "") + "\\Input"
        //        def outputPath = PacConfig.getStringProperty(prober + ".Path", "") + "\\Output"
        //        def alarmPath = PacConfig.getStringProperty(prober + ".Path", "") + "\\Alarm"
        //        def eventPath  = PacConfig.getStringProperty(prober + ".Path", "") + "\\Event"

//        def inputPath = ""
//        def outputPath = ""
//        def alarmPath = ""
//        def eventPath = ""

//        def equipmentFileService = new EquipmentFileServiceImpl(prober, 1000, 20000, inputPath, outputPath, alarmPath, eventPath)

        if(correct == true)
        {

            def gDPW = 0
            def realRunQty = 0
            def trackInQty = 0
            def trackInProbeQty = 0

            if (inputXmlDocument.length()>0)
            {
                def outbound = new W02TrackInLotRequest(inputXmlDocument)
                def recipe = outbound.getResourceName() + "-" + outbound.getRecipeName()
                gDPW = PacUtils.valueOfInteger(outbound.getGDPW(), 0)
                trackInQty = PacUtils.valueOfInteger(outbound.getTrackInQty(), 0)
                def wipDataList = outbound.getWipDataItemList()

                if (wipDataList.size()>0)
                {
                    for (var in wipDataList)
                    {
                        if (var.WIP_DATA_NAME.equalsIgnoreCase("Real Run Qty"))
                        {
                            realRunQty = PacUtils.valueOfInteger(var.WIP_DATA_VALUE, -1)
                            break
                        }
                    }
                }
                logger.info("Get recipe '$recipe' from outbound...")
                mainRecipe = recipeManager.getDomainObject(recipe)
            }
            else
            {
                def lotList = cMaterialManager.getCLotList(new LotFilterAll())
                if (lotList.size()>0)
                {
                    for (lot in lotList)
                    {
                        def lotId = lot.getId()
                        def lotEqpId = lot.getEquipmentId()
                        if (lotEqpId.equalsIgnoreCase(mainEquipment.getSystemId()))
                        {
                            def recipe = mainEquipment.getSystemId() + "-" + lot.getRecipe()
                            logger.info("Get recipe '$recipe' from lot '$lotId'...")
                            mainRecipe = recipeManager.getDomainObject(recipe)
                            gDPW = lot.getGDPW()
                            trackInQty = lot.getTrackInQty()
                            def wipDataSet = wipDataDomainObjectManager.getWipDataSet(lotId)
                            if (wipDataSet != null)
                            {
                                def wipDataDOList = wipDataSet.getAll(new FilterAllDomainObjects())
                                for (wipDataDO in wipDataDOList)
                                {
                                    if (wipDataDO.getId().equalsIgnoreCase("Real Run Qty"))
                                    {
                                        realRunQty = PacUtils.valueOfInteger(wipDataDO.getValue(), -1)
                                        break
                                    }
                                }
                            }
                            break
                        }
                    }
                }
            }

            if (realRunQty > trackInQty)
            {
                trackInProbeQty = trackInQty.multiply(gDPW).toInteger()
            }
            else
            {
                trackInProbeQty = realRunQty.multiply(gDPW).toInteger()
            }

            def isRequestRecipeParameter = true

            logger.info("Preparing recipe parameter data from domain object and mapping manager...")
            verifyRecipeParameter(isRequestRecipeParameter, null, trackInProbeQty)

            LinkedHashMap<String, String> recipeParameterMapList = requestRecipeParameterFromEqp(equipmentFileService)

            isRequestRecipeParameter = false
            logger.info("Verifying recipe parameter...")
            verifyRecipeParameter(isRequestRecipeParameter, recipeParameterMapList, trackInProbeQty)
        }
    }

    void verifyRecipeParameter(boolean isRequestRecipeParameter, LinkedHashMap<String, String> recipeParameterMapList, Integer trackInProbeQty)
    {
        def eqpId = mainEquipment.getSystemId()
        if (mainRecipe == null)
        {
            throw new Exception("No recipe is found!")
        }
        else
        {
            def schemaComponent = mappingManager.getSchemaComponentByName("MES", "RecipeParameter")
            if (schemaComponent != null)
            {
                def itemList = schemaComponent.getSchemaItems()
                if (itemList.size() > 0)
                {
                    for (item in itemList)
                    {
                        def definedInMES = false
                        def recipeParamters = mainRecipe.getAllParam()
                        for (param in recipeParamters)
                        {
                            def parameterName = param.getParameterName()
                            if (item.getName().trim().equals(parameterName))
                            {
                                definedInMES = true
                                def parameterMaxValue = param.getMaxValue()
                                def parameterMinValue = param.getMinValue()
                                def parameterValue = param.getParameterValue()
                                if (!item.getUnit()?.trim())
                                {
                                    throw new Exception("VID is not defined for " + item.getName())
                                }
                                def vidName = item.getUnit().trim()
                                if (isRequestRecipeParameter)
                                {
                                    //add vidName into parameterlist
                                    paramterList = paramterList + vidName
                                }
                                else
                                {
                                    def eqpValue = ""
                                    def recipeParamName = ""
                                    for (recipeParam in recipeParameterMapList)
                                    {
                                        recipeParamName = recipeParam.getKey()
                                        if (recipeParamName.equalsIgnoreCase(parameterName))
                                        {
                                            eqpValue = recipeParam.getValue()
                                            break
                                        }
                                    }
                                    if(isUniDirectional)
                                    {
                                        if (parameterName.equalsIgnoreCase("Probe"))
                                        {
                                            eqpValue = (PacUtils.valueOfFloat(eqpValue, 0) + trackInProbeQty).toString()
                                        }
                                    }
                                    else
                                    {
                                        if (parameterName.equalsIgnoreCase("Needle Used Times"))
                                        {
                                            eqpValue = (PacUtils.valueOfFloat(eqpValue, 0) + trackInProbeQty).toString()
                                        }
                                    }
                                    if (parameterMaxValue.length()>0)
                                    {
                                        if (PacUtils.valueOfFloat(eqpValue, 0) > PacUtils.valueOfFloat(parameterMaxValue, 0))
                                        {
                                            throw new Exception("Value [$parameterName] at $eqpId is $eqpValue larger than MES configure value $parameterMaxValue.")
                                        }
                                    }
                                    if (parameterMinValue.length()>0)
                                    {
                                        if (PacUtils.valueOfFloat(eqpValue, 0) < PacUtils.valueOfFloat(parameterMinValue, 0))
                                        {
                                            throw new Exception("Value [$parameterName] at $eqpId is $eqpValue smaller than MES configure value $parameterMinValue.")
                                        }
                                    }
                                    if (parameterValue.length()>0)
                                    {
                                        parameterValue = PacUtils.valueOfFloat(parameterValue, 0)
                                        eqpValue = PacUtils.valueOfFloat(eqpValue, 0)
                                        if (parameterValue != eqpValue)
                                        {
                                            throw new Exception("Value [$parameterName] at $eqpId is $eqpValue, different with MES configure value $parameterValue.")
                                        }
                                    }
                                }
                                break
                            }
                        }
                        if (!definedInMES)
                        {
                            logger.info("Parameter 'parameterName' not defined in Camstar, skip validation...")
                            //                            throw new Exception(item.getName() + " is not defined in Camstar!")
                        }
                    }
                }
                else
                {
                    throw new Exception("Schema Item of Component 'RecipeParameter' is empty!")
                }
            }
            else
            {
                throw new Exception("Schema 'MES' with Component 'RecipeParameter' has no item configured!")
            }
        }
    }

    LinkedHashMap<String, String> requestRecipeParameterFromEqp(EquipmentFileService equipmentFileService)
    {
        try
        {

            LinkedHashMap<String, String> recipeParameterMapList

            EquipmentFileHandler _fileHandler = equipmentFileService.getFileHandler()

            def requestId = _fileHandler.getREQUEST_ID()
            logger.info("Performing request recipe parameter: last RequestId = '$requestId'...")
            //            _fileHandler.initialize(EquipmentFileHandler.Folder_Name_Output)

            if(isUniDirectional)
            {
                recipeParameterMapList = equipmentFileService.requestData(paramterList)
            }
            else
            {
                recipeParameterMapList = equipmentFileService.requestRecipeParameter(paramterList)
            }

            logger.info("File base request recipe parameter successful...")
            logger.info("[$recipeParameterMapList]")
            return recipeParameterMapList
        }
        catch(Exception e)
        {
            logger.error(e.getMessage())
            throw new Exception(e.getMessage())
        }
    }
}