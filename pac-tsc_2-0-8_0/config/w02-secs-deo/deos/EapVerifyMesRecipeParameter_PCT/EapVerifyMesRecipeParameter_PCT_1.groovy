package EapVerifyMesRecipeParameter_PCT

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.Deo
import de.znt.pac.deo.annotations.DeoBinding
import de.znt.pac.deo.annotations.DeoExecute
import de.znt.services.secs.SecsGemService
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsComponent
import de.znt.zsecs.composite.SecsComposite
import de.znt.zsecs.composite.SecsF4Item
import de.znt.zsecs.sml.SmlAsciiParser
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.Recipe
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
eap verify recipe parameter of pico eqp and outbound
''')
class EapVerifyMesRecipeParameter_PCT_1
{
    @DeoBinding(id="MaterialManager")
    private CMaterialManager materialManager

    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        Recipe mainRecipe = null
        def outboundLot, recipe
        if(inputXmlDocument.length() > 0)
        {
            def request = new W02TrackInLotRequest(inputXmlDocument)
            outboundLot = request.getContainerName()
        }

        def lotList = materialManager.getCLotList(new LotFilterAll())
        if(lotList.size() > 0)
        {
            for(lot in lotList)
            {
                def lotId = lot.getId()
                if(lotId.equalsIgnoreCase(outboundLot))
                {
                    recipe = lot.getEquipmentId() + "-" + lot.getRecipe()
                    logger.info("Get the recipe '$recipe' from lot '$lotId'")
                    mainRecipe = recipeManager.getDomainObject(recipe)
                }
            }
        }

        if(mainRecipe == null)
        {
            logger.info("No main recipe found, skip verify!")
        }
        else
        {
            // Eqp_Recipe
            def eqpRecipe = mainRecipe.getElement("EqpRecipe")
            logger.info("This is the eqpRecipe selected: '$eqpRecipe'")
            if(eqpRecipe == null) {
                throw new Exception("Equipment recipe cannot be empty, please configure as Recipe Parameter with 'EqpRecipe' in Camstar!")
            }

            // Eqp_Recipe Param Value
            def eqpRecipeValue = eqpRecipe.getParameterValue()
            logger.info("This is the eqpRecipeValue selected: '$eqpRecipeValue'")

            if(eqpRecipeValue == null || eqpRecipeValue.length() == 0) {
                throw new Exception("Equipment recipe value cannot be empty, please configure 'EqpRecipe' value in Camstar!")
            }

            // Recipe used's EqpId
            def recipeEqp = mainRecipe.getEquipmentId()
            def eqpId = recipeEqp.substring(recipeEqp.length() - 1)
            logger.info("The eqpId: '$eqpId' from recipeEqp: '$recipeEqp'")

            //Concat eqpId with Eqp_Recipe for retrieve recipe used from Eqp
            def recipeId = eqpId + eqpRecipeValue
            logger.info("The recipeId: '$recipeId' after concat eqpId and eqpRecipe")

            // Main Recipe all recipe param
            def recipeParams = mainRecipe.getAllParam()

            logger.info("This is recipeParam [" + recipeParams + "]")

            HashMap<String, String> mainEqpMap = new HashMap<String, String>()
            HashMap<String, String> paramMap = new HashMap<String, String>()

            for(recipeParam in recipeParams) {
                // Camstar outbound

                // EqpModuleRecipe
                if(recipeParam.getParameterName().contains("EqpModuleRecipe")) {
                    def moduleRecipe = recipeParam.getParameterName()
                    def moduleValue = recipeParam.getParameterValue()
                    logger.info("This is the recipe value: '$moduleValue' from recipe param: '$recipeParam'")

                    def arrModuleRecipe = moduleRecipe.split("@")
                    def moduleId = arrModuleRecipe[1]
                    logger.info("This is the moduleId : '$moduleId' for the arrModuleRecipe: '$arrModuleRecipe'")

                    //            (HPO, _Prog1.mrc)
                    mainEqpMap.put(moduleId, moduleValue)
                }

                // Module Recipe recipe parameter
                else if(recipeParam.getParameterName().startsWith("_"))
                {
                    // An array with selected EqpModuleRecipe Param Value's Param (Arm Recipe)
                    // Arm Recipe Param Value
                    def moduleName = recipeParam.getParameterName()

                    def arrModuleParams = moduleName.trim().split("@")
                    logger.info("The armRecipe array after split: '$arrModuleParams'")
                    def moduleParamId = arrModuleParams[0]
                    logger.info("The moduleParamId: '$moduleParamId' for arm recipe: '$arrModuleParams'")

                    Set entrySet = paramMap.entrySet()
                    boolean foundInMap = false
                    for (entry in entrySet)
                    {
                        if (moduleParamId.equalsIgnoreCase(entry.getKey()))
                        {
                            def curValue = entry.getValue()
                            def newValue = curValue + ";" + moduleName
                            paramMap.put(moduleParamId, newValue)
                            foundInMap = true
                            break
                        }
                    }
                    if (!foundInMap)
                    {
                        paramMap.put(moduleParamId, moduleName)
                    }
                }
            }

            Set newEntrySet = paramMap.entrySet()
            for(param in newEntrySet)
            {
                logger.info("This is key: " + param.getKey())
                logger.info("This is value: " + param.getValue())
            }

            logger.info("This is the hash map for main recipe: '$mainEqpMap'")
            logger.info("This is the hash map for moduleName and paramValue: '$paramMap'")

            if(mainEqpMap != null && mainEqpMap.size() > 0)
            {
                def mainReq = getEqpRecipeSML(mainEqpMap, recipeId, eqpId)
            }
            if(paramMap != null && paramMap.size() > 0)
            {
                def subReq = getSubRecipeSML(paramMap, eqpId, recipe)
            }
        }
    }

    public String getEqpRecipeSML(HashMap<String, String> mainEqpMap, String recipeId, String eqpId)
    {
        def request = '''S7F25 W <A "''' + recipeId + '''" >.'''
        def parser = new SmlAsciiParser()
        def message = parser.parse(request)
        def sentMessage = secsGemService.requestMessage(message)
        def sml = sentMessage.getSML()

        //            (HPO, _Prog1.mrc)
        def recipeParams = mainEqpMap.entrySet()
        logger.info("This is the mainEqpMap hash map recipe params: '$recipeParams'")
        for(recipeParam in recipeParams)
        {
            def moduleId = recipeParam.getValue()
            def moduleValue = recipeParam.getKey()

            logger.info("The module recipe: '$moduleId' and module value: '$moduleValue' for recipe param: '$recipeParam'!")

            if(moduleId != null || moduleId.length() > 0)
            {
                moduleId = eqpId + moduleId
                def eqpRecipeRequest = getEqpRecipeSML(sml, moduleId, moduleValue, recipeId)
                logger.info("request = '$eqpRecipeRequest'")
            }
            else
            {
                logger.info("There are no module recipe configure in Camstar for comparision!")
            }
        }
    }

    public String getEqpRecipeSML(String sml, String moduleId, String moduleValue, String recipeId)
    {
        def eqp = mainEquipment.getSystemId()
        def parser = new SmlAsciiParser()
        def replyMessage = parser.parse(sml)
        def data = replyMessage.getData()
        def dataList = data.getValueList()
        def param1 = ((SecsAsciiItem)dataList.get(0)).getString()
        def param2 = ((SecsAsciiItem)dataList.get(1)).getString()
        def param3 = ((SecsAsciiItem)dataList.get(2)).getString()
        def moduleList = ((SecsComposite)dataList.get(3)).getValueList()
        logger.info("param1=$param1, param2=$param2, param3=$param3")

        def valueFound = false
        for (var in moduleList)     //  list of each module
        {
            def aList = var.getValueList()
            def recipeName = ((SecsAsciiItem)aList.get(0)).getString()
            logger.info("This is param: '$recipeName' and moduleId: '$moduleId'")
            if(recipeName.endsWith(".mrc"))
            {
                if(recipeName.equalsIgnoreCase(moduleId))
                {
                    def moduleInfoList = ((SecsComposite)aList.get(1)).getValueList()
                    for (moduleInfo in moduleInfoList)
                    {
                        def setKeyVal = moduleInfo.getValueList()
                        def key = ((SecsAsciiItem)setKeyVal.get(0)).getString()
                        def value = ((SecsAsciiItem)setKeyVal.get(1)).getString()

                        if (key.equalsIgnoreCase("Module Type"))
                        {
                            if (value.equalsIgnoreCase(moduleValue))
                            {
                                valueFound = true
                                break
                            }
                        }
                    }
                    if (valueFound)
                    {
                        logger.info("Recipe '$recipeName' with module type '$moduleId' from Camstar and equipment is matched")
                        break
                    }
                }
            }
        }

        if(valueFound == false)
        {
            throw new Exception("Module Recipe '$moduleId' with Module Type '$moduleValue' configured in Camstar not found/matched with Equipment Flow Recipe '$recipeId'!")
        }
    }

    public String getSubRecipeSML(HashMap<String, String> paramMap, String eqpId, String recipe)
    {
        Recipe subRecipe = recipeManager.getDomainObject(recipe)
        def recipeParams = paramMap.entrySet()
        logger.info("This is subRecipe: '$subRecipe', recipe: '$recipe'")
        logger.info("This is the paramMap hash map recipe params: '$recipeParams'")

        for(recipeParam in recipeParams)
        {
            def subRecipeKey = recipeParam.getKey()
            def subValues = recipeParam.getValue()
            def valuesArr = subValues.trim().split(";")

            logger.info("subRecipeKey: '$subRecipeKey' and subValues: '$subValues'")
            logger.info("valuesArr: '$valuesArr'")

            subRecipeKey = eqpId + subRecipeKey
            logger.info("subKey: '$subRecipeKey'")
            def parser = new SmlAsciiParser()
            def request = '''S7F25 W <A "''' + subRecipeKey + '''" >.'''
            def message = parser.parse(request)
            def sentMessage = secsGemService.requestMessage(message)
            def sml = sentMessage.getSML()


            for(values in valuesArr)
            {
                def param = subRecipe.getElement(values)
                def paramId = param.getParameterName()
                def paramValue = param.getParameterValue()
                def minValue = param.getMinValue()
                def maxValue = param.getMaxValue()

                if(paramValue == null && paramValue.length() == 0)
                {
                    paramValue = ""
                }
                if(minValue == null && minValue.length() == 0)
                {
                    minValue = ""
                }
                if(maxValue == null && maxValue.length() == 0)
                {
                    maxValue = ""
                }

                logger.info("This is the paramValue: '$paramValue', minValue: '$minValue', maxValue: '$maxValue' for param: '$paramId'")

                def arrModuleParams = paramId.trim().split("@")
                logger.info("The armRecipe array after split: '$arrModuleParams'")
                def moduleParamId = arrModuleParams[0]
                logger.info("The moduleParamId: '$moduleParamId' for arm recipe: '$arrModuleParams'")
                def arrArmStep = arrModuleParams[1].split("\\.")
                logger.info("The arrArmStep: '$arrArmStep' for arm recipe: '$arrModuleParams'")
                def armId = arrArmStep[0]
                logger.info("The armId: '$armId' for arm recipe: '$arrArmStep'")
                def stepId = arrArmStep[1]
                logger.info("The stepId: '$stepId' for arm recipe; '$arrArmStep'")

                if(moduleParamId != null && armId != null && stepId != null)
                {
                    moduleParamId = eqpId + moduleParamId
                    if(paramValue.startsWith("_"))
                    {
                        paramValue = eqpId + paramValue
                    }
                    logger.info("request param: moduleParam: '$moduleParamId', armId: '$armId', stepId: '$stepId', paramValue: '$paramValue', maxValue: '$maxValue', minValue: '$minValue'")
                    def subRecipeRequest = getSubRecipeSML(sml, moduleParamId, armId, stepId, paramValue, maxValue, minValue)
                    logger.info("request = '$subRecipeRequest'")
                }
            }
        }
    }

    public String getSubRecipeSML(String sml, String moduleParamId, String armId, String stepId, String paramValue, String maxValue, String minValue)
    {
        def eqp = mainEquipment.getSystemId()
        def parser = new SmlAsciiParser()
        def replyMessage = parser.parse(sml)
        def data = replyMessage.getData()
        def dataList = data.getValueList()
        def param1 = ((SecsAsciiItem)dataList.get(0)).getString()
        def param2 = ((SecsAsciiItem)dataList.get(1)).getString()
        def param3 = ((SecsAsciiItem)dataList.get(2)).getString()
        def moduleList = ((SecsComposite)dataList.get(3)).getValueList()
        logger.info("param1=$param1, param2=$param2, param3=$param3")

        def moduleFound = false
        for (module in moduleList)
        {
            def subList = module.getValueList()
            def moduleParam1 = ((SecsAsciiItem)subList.get(0)).getString()
            if(moduleParam1.equalsIgnoreCase(moduleParamId))
            {
                moduleFound = true
            }
            else
            {
                continue
            }

            if(moduleFound == true)
            {
                //List for step
                def stepList = ((SecsComposite)subList.get(1)).getValueList()
                logger.info("subList=$subList")
                logger.info("moduleParam1=$moduleParam1")
                logger.info("stepList:$stepList")

                def stepFound = false
                for(step in stepList)
                {
                    def arrStep = step.getValueList()
                    def size = arrStep.size()
                    logger.info("arrStep=$arrStep")
                    logger.info("size=$size")
                    def stepText = ((SecsAsciiItem)arrStep.get(0)).getString()
                    logger.info("stepText = $stepText")
                    if(stepText.contains("Step")){
                        def stepValue = ((SecsF4Item)arrStep.get(1)).getFloat(0)
                        logger.info("stepValue = $stepValue, stepId = '$stepId'")

                        if(stepValue.toInteger().toString().equalsIgnoreCase(stepId))
                        {
                            stepFound = true
                        }
                        else
                        {
                            continue
                        }

                        if(stepFound == true)
                        {
                            logger.info("stepValue=$stepValue")
                            def armFound = false
                            for(arr in arrStep)
                            {
                                def index = arrStep.indexOf(arr)
                                logger.info("index=$index")
                                def flag = false
                                logger.info("flag out = $flag")

                                if(arr.toString().contains("SecsComposite"))
                                {
                                    flag = true
                                    logger.info("flag in = $flag")
                                }
                                else
                                {
                                    continue
                                }

                                if(flag == true){
                                    def armList = ((SecsComposite)arrStep.get(index)).getValueList()

                                    logger.info("armList=$armList")

                                    def armParam = ((SecsAsciiItem)armList.get(0)).getString()
                                    def armValue = ((SecsComponent)armList.get(1)).getValueList().get(0).toString()

                                    if(armValue == null && armValue.toString().length() == 0)
                                    {
                                        armValue = ""
                                    }
                                    def value = armList.get(1).getType()

                                    logger.info("armParam=$armParam")
                                    logger.info("armValue=$armValue")
                                    logger.info("value=$value")

                                    if(armParam != null && armParam.length() > 0)
                                    {
                                        if(armParam.equalsIgnoreCase(armId))
                                        {
                                            armFound = true
                                            logger.info("The parse in '$armId', '$armParam' so is true")
                                        }
                                        else
                                        {
                                            continue
                                        }
                                    }
                                    else
                                    {
                                        throw new Exception("'$armParam' is null or empty")
                                    }

                                    if(armFound == true)
                                    {
                                        logger.info("This is armParam: '$armParam' and armId: '$armId'")
                                        if (maxValue.length()>0)
                                        {
                                            if (armValue.toFloat() > maxValue.toFloat())
                                            {
                                                throw new Exception("[$moduleParamId, $armId, $stepId] current value $armValue exceed max defined value $maxValue!")
                                            }
                                        }
                                        if (minValue.length()>0)
                                        {
                                            if (armValue.toFloat() < minValue.toFloat())
                                            {
                                                throw new Exception("[$moduleParamId, $armId, $stepId] current value $armValue less than min defined value $minValue!")
                                            }
                                        }
                                        if (paramValue.length()>0)
                                        {
                                            if(armValue.isNumber() || paramValue.isNumber())
                                            {
                                                def armFloat = PacUtils.valueOfFloat(armValue, 0)
                                                def moduleFloat = PacUtils.valueOfFloat(paramValue, 0)
                                                if(armFloat != moduleFloat)
                                                {
                                                    throw new Exception("[$moduleParamId, $armId, $stepId] current value(num) $armFloat does not same as defined value(num) $moduleFloat!")
                                                }
                                            }
                                            else if (!armValue.equalsIgnoreCase(paramValue))
                                            {
                                                throw new Exception("[$moduleParamId, $armId, $stepId] current value(char) $armValue does not same as defined value(char) $paramValue!")
                                            }
                                        }
                                    }
                                    else
                                    {
                                        throw new Exception("'$armParam' from Equipment is different with '$armId' from Camstar")
                                    }
                                }

                            }
                            if(armFound == false)
                            {
                                throw new Exception("'$armId' from Camstar is not found in Eqp '$eqp' Recipe '$moduleParam1'! Please ensure it is available or remove from Camstar")
                            }
                        }
                    }
                }
                if(stepFound == false)
                {
                    throw new Exception("Step: '$stepId' for armId: '$armId', Recipe '$moduleParam1' from Camstar is not found in Eqp '$eqp'! Please ensure it is available or remove from Camstar")
                }
            }
        }
        if(moduleFound == false)
        {
            throw new Exception("moduleParam: '$moduleParamId' from Camstar is not found in Eqp '$eqp'! Please ensure it is available or remove from Camstar")
        }
    }
}