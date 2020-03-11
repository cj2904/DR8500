package EapVerifyRecipeParameter_GSD

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S7F5ProcessProgramRequest
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsBinary
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.domainobject.W06RecipeParameterManager
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.RecipeParameterUtil
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
verify track in recipe param for eqp
''')
class EapVerifyRecipeParameter_GSD_1
{

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager

    @DeoBinding(id="W06RecipeParameterManager")
    private W06RecipeParameterManager w06RecipeParameterManager

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outbound = new W02TrackInLotRequest(inputXmlDocument)
        def eqp = outbound.getResourceName()

        def lotList = cMaterialManager.getCLotList(new LotFilterAll())

        for(lot in lotList)
        {
            if(lot.getId().equalsIgnoreCase(outbound.getContainerName()))
            {

                def eqpRecipeValue = RecipeParameterUtil.getEqpRecipe(outbound.getRecipeParamList())
                if(eqpRecipeValue == null || eqpRecipeValue.length() == 0)
                {
                    throw new Exception("Equipment recipe cannot be empty, please configure as Recipe Parameter with 'EqpRecipe' in Camstar!")
                }

                def w06RecipeDO = w06RecipeParameterManager.getDomainObject(eqp + "-" + eqpRecipeValue)
                def recipe = w06RecipeDO.getElement(eqpRecipeValue)
                def hashMap = new HashMap<String, String>()

                def recipeBody = getValueFromEqp(eqpRecipeValue)
                def formatRecipes = recipeBody.replaceAll("\\s{2,}", "=")
                def formatRecipe = formatRecipes.split("\n")
                logger.info("Reply formatRecipe: '$formatRecipe'")

                for(rcp in formatRecipe)
                {
                    def rcpSplit = rcp.split("=")
                    logger.info("Each recipe Param split: '$rcpSplit'")
                    if(rcpSplit.size() > 1)
                    {
                        def recpName = rcpSplit[0]
                        def rcpVal = rcpSplit[1]
                        hashMap.put(recpName, rcpVal)
                    }
                }

                logger.info("Reply HashMap: '$hashMap'")

                def recipeParams = recipe.getAllRecipeParameter(eqpRecipeValue)
                logger.info("This is recipeParam [" + recipeParams + "]")
                for(recipeParam in recipeParams)
                {
                    def eqpValue = hashMap.get(recipeParam.getParameterName().replace(eqpRecipeValue + "@", ""))

                    if(eqpValue == null )
                    {
                        def msg = "Recipe Param Value: '" + recipeParam.getParameterName() + "' configure in Camstar not found in Equipment Recipe: '$recipe''s PPBody! Please verify again!!"
                        setLotExceptionMsg(lot, msg)
                    }
                    else
                    {
                        def pass = RecipeParameterUtil.validateRecipeParameter(recipeParam, eqpValue)

                        if (!pass)
                        {
                            String fixValue = recipeParam.getParameterFixValue()
                            String minValue = recipeParam.getParameterMinValue()
                            String maxValue = recipeParam.getParameterMaxValue()

                            def exceptionMsg = recipeParam.getParameterName() + " failed parameter checking, eqp value = " + eqpValue + " Camstar parameter value (fix = " + fixValue + ", minValue = " + minValue + ", maxValue = " + maxValue + ")"
                            setLotExceptionMsg(lot, exceptionMsg)
                        }
                    }
                }
            }
        }
    }

    public void setLotExceptionMsg(CLot cLot, String curExceptionMsg)
    {
        def exceptionMsg = cLot.getPropertyContainer().getString("ExceptionMessage", "")
        if (exceptionMsg.length() == 0) {
            exceptionMsg = curExceptionMsg
        }
        else {
            exceptionMsg += "\t" + curExceptionMsg
        }
        cLot.getPropertyContainer().setString("ExceptionMessage", exceptionMsg)
    }

    public String getValueFromEqp(String eqpRecipe)
    {
        def recipeBody
        def request = new S7F5ProcessProgramRequest()
        def ppId = new SecsAsciiItem(eqpRecipe)
        request.setPPID(ppId)

        def reply = secsGemService.sendS7F5ProcessProgramRequest(request)

        if(reply.getData().getPPBODY() instanceof SecsAsciiItem)
        {
            logger.info("Jimmy's secsAsciiItem")
            def recipeBodyInAscii = (SecsAsciiItem)reply.getData().getPPBODY()
            recipeBody = recipeBodyInAscii.getString().getBytes()
        }
        else
        {
            logger.info("Jimmy's secsBinary")
            SecsBinary recipeBodyInBinary = (SecsBinary)reply.getData().getPPBODY()
            recipeBody = recipeBodyInBinary.getBytes()
        }
        recipeBody = new String(recipeBody)
        logger.info("This is the recipeBody after convert to string format: '$recipeBody'!")

        if(recipeBody.length() == 0)
        {
            throw new Exception("[$eqpRecipe] recipe PPBody is empty! Please ensure it is available on Equipment!")
        }
        return recipeBody.trim()
    }
}