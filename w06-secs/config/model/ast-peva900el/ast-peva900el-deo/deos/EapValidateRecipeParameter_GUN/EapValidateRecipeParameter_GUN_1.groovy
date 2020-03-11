package EapValidateRecipeParameter_GUN

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.Deo
import de.znt.pac.deo.annotations.DeoBinding
import de.znt.pac.deo.annotations.DeoExecute
import de.znt.services.secs.SecsGemService
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsComposite
import de.znt.zsecs.sml.SmlAsciiParser
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.W06RecipeParameterManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.RecipeParameterUtil
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EapValidateRecipeParameter_GUN_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

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
	
	public Map<String,String> getEqpRecipeFromEqp(String eqpRecipe)
    {		
		def requestSml = "S7F25 W <A \"$eqpRecipe\" >."
		logger.info("Request SML :: $requestSml")	
		def parser = new SmlAsciiParser()
		def requestMsg = parser.parse(requestSml)
		
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
		
        def recipeListLevel1 = ((SecsComposite)dataList.get(3)).getValueList()
		def recipeListLevel2 = ((SecsComposite)recipeListLevel1.get(0)).getValueList()		
		def recipeListLevel3 = ((SecsComposite)recipeListLevel2.get(1)).getValueList()
		def recipeListLevel4 = ((SecsComposite)recipeListLevel3.get(0)).getValueList()		
		
        for (recipe in recipeListLevel4)     //  list of each module
        {
            def aList = recipe.getValueList()
            def key = ((SecsAsciiItem)aList.get(0)).getString()
			def value = ""
			if (aList.get(1).getClass().equals(SecsComposite))
			{
				def recipeListLevel5 = ((SecsComposite)aList.get(1)).getValueList()
				for (recipeLevel2 in recipeListLevel5)
				{
					if (value.isEmpty())
					{
						value = ((SecsAsciiItem)recipeLevel2).getString()
					}
					else
					{
						value += ";" + ((SecsAsciiItem)recipeLevel2).getString()
					}
				}
			}
			else
			{
				value = ((SecsAsciiItem)aList.get(1)).getString()
			}
			logger.info("This is key: '$key' and value: '$value'" )
			eqpRecipeMap.put(key, value)
        }		
		logger.info("HashMap Value :: $eqpRecipeMap")
		
		return eqpRecipeMap
    }
}