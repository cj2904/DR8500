package EapValidateRecipeParameter_KE

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsComposite
import de.znt.zsecs.composite.SecsU2Item
import de.znt.zsecs.sml.SmlAsciiParser
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.W06RecipeParameterManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.RecipeParameterUtil
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
eap verify track in lot recipe param with eqp
''')
class EapValidateRecipeParameter_KE_1
{
    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="W06RecipeParameterManager")
    private W06RecipeParameterManager w06RecipeParameterManager

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    private String exceptionMsg = ""

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def trackInLot = new W02TrackInLotRequest(inputXmlDocument)
        def cLot = cMaterialManager.getCLot(trackInLot.getContainerName())
        if (cLot == null)
        {
            throw new Exception("Lot " + trackInLot.getContainerName() + " does not exist")
        }

        def eqpRecipe = RecipeParameterUtil.getEqpRecipe(trackInLot.getRecipeParamList())
        if (eqpRecipe == null || eqpRecipe.length() == 0)
        {
            throw new Exception("Could not find EqpRecipe in Camstar recipe parameter")
        }
        else
        {
            def eqpRecipeMap = getEqpRecipeFromEqp(eqpRecipe)
            def eqpId = cEquipment.getSystemId()
            def domainObjectSet = w06RecipeParameterManager.getDomainObject(eqpId + "-" + eqpRecipe)
            def domainObject = domainObjectSet.getElement(eqpRecipe)
            def recipeParameters = domainObject.getAllRecipeParameter(eqpRecipe)
            exceptionMsg = cLot.getPropertyContainer().getString("ExceptionMessage", "")

            for (param in recipeParameters)
            {
                def shortParamKey = param.getParameterName().replace(eqpRecipe + "@", "")
                def eqpValue = eqpRecipeMap.get(shortParamKey)
                if (eqpValue == null)
                {
                    def msg = "Equipment Recipe '$eqpRecipe' parameter in CAMSTAR '$shortParamKey' not found in formatted recipe."
                    setExceptionMsg(msg)
                }
                else
                {
                    boolean pass = RecipeParameterUtil.validateRecipeParameter(param, eqpValue)
                    if (!pass)
                    {
                        def fixValue = param.getParameterFixValue()
                        def minValue = param.getParameterMinValue()
                        def maxValue = param.getParameterMaxValue()
                        def paraName = param.getParameterName()
                        def msg = "$paraName failed parameter checking, eqp value = $eqpValue Camstar parameter value (fix = $fixValue, minValue = $minValue, maxValue = $maxValue"
                        setExceptionMsg(msg)
                    }
                }
            }
            cLot.getPropertyContainer().setString("ExceptionMessage", exceptionMsg)
        }
    }

    public void setExceptionMsg(String msg)
    {
        if (exceptionMsg.length() == 0)
        {
            exceptionMsg = msg
        }
        else
        {
            exceptionMsg += "\t" + msg
        }
    }

    public Map<String, String> getEqpRecipeFromEqp(String eqpRecipe)
    {
        def rcp = reserveStrLength(eqpRecipe)
        logger.info("Jimmy modified recipe length: '$rcp'")
        def requestSml = "S7F25 W <A \"$rcp\" >."
        logger.info("Request SML :: $requestSml")
        def parser = new SmlAsciiParser()
        def requestMsg = parser.parse(requestSml, false)


        def responseMsg = secsGemService.requestMessage(requestMsg)
        def responseSml = responseMsg.getSML()
        def data = responseMsg.getData()
        logger.info("Response SML :: $responseSml")

        def eqpRecipeMap = new HashMap<String,String>()
        def dataList = data.getValueList()
        def param1 = ((SecsAsciiItem)dataList.get(0)).getString()
        def param2 = ((SecsAsciiItem)dataList.get(1)).getString()
        def param3 = ((SecsAsciiItem)dataList.get(2)).getString()
        logger.info("PPID=$param1, MDLN=$param2, SOFTREV=$param3")

        def recipeParamList = ((SecsComposite)dataList.get(3)).getValueList()
        logger.info("RecipeParamList: '$recipeParamList'")

        for(recipeParam in recipeParamList)
        {
            def paramList = recipeParam.getValueList()
            def rcpParamName = ((SecsU2Item)paramList.get(0)).getInteger(0).toString()

            def rcpParamList = ((SecsComposite)paramList.get(1)).getValueList()
            def rcpParamValue

            def paramValueA = ((SecsAsciiItem)rcpParamList.get(1)).getString().trim()
            def paramValueB = ((SecsAsciiItem)rcpParamList.get(2)).getString().trim()

            if(paramValueA.length() > 0)
            {
                rcpParamValue = paramValueA
            }
            if(paramValueB.length() > 0)
            {
                rcpParamValue = rcpParamValue + "," + paramValueB
            }

            eqpRecipeMap.put(rcpParamName, rcpParamValue)
        }
        logger.info("HashMap Value :: $eqpRecipeMap")

        return eqpRecipeMap
    }

    private String reserveStrLength(String str)
    {
        while(str.length() < 16)
        {
            str += " "
        }

        return str
    }
}