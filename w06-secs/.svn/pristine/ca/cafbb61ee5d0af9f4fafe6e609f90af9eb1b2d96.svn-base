package EapValidateRecipeParameter_AOI

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsComposite
import de.znt.zsecs.sml.SmlAsciiParser
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.domainobject.W06RecipeParameterManager
import sg.znt.pac.domainobject.W06RecipeParameterSet
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.RecipeParameterUtil
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EapValidateRecipeParameter_AOI_1 {


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="InputXmlDocument")
	private String inputXmlDocument

	@DeoBinding(id="MaterialManager")
	private CMaterialManager cMaterialManager

	@DeoBinding(id="RecipeManager")
	private RecipeManager recipeManager

	@DeoBinding(id="SecsGemService")
	private SecsGemService secsGemService

	@DeoBinding(id="CEquipment")
	private CEquipment cEquipment

	@DeoBinding(id="W06RecipeParameterManager")
	private W06RecipeParameterManager w06RecipeParameterManager

	/**
	 *
	 */
	@DeoExecute
	public void execute() {
		
		W02TrackInLotRequest trackInLot = new W02TrackInLotRequest(inputXmlDocument)
		def eqpId = cEquipment.getSystemId()
		def cLot = cMaterialManager.getCLot(trackInLot.getContainerName())
		if (cLot == null) {
			throw new Exception("Lot " + trackInLot.getContainerName() + " does not exist")
		}
		String eqpRecipe = RecipeParameterUtil.getEqpRecipe(trackInLot.getRecipeParamList())

		if (eqpRecipe == null || eqpRecipe.length() == 0) {
			throw new Exception("Could not find EqpRecipe : '$eqpRecipe' in Camstar recipe parameter")
		}

		else {

			Map<String, String> eqpValMap = getValueFromEqp(eqpRecipe)
			logger.info("Print recipeMap " + eqpValMap)
			logger.info("Print eqpRecipe " + eqpRecipe)

			W06RecipeParameterSet domainObjectSet = w06RecipeParameterManager.getDomainObject(eqpId + "-" + eqpRecipe)
			def domainObject = domainObjectSet.getElement(eqpRecipe)
			def recipeParameters = domainObject.getAllRecipeParameter(eqpRecipe)
			for (param in recipeParameters) {

				def eqpValue = eqpValMap.get(param.getParameterName().replace(eqpRecipe + "@", ""))

				if (eqpValue == null){

					def msg = "Equipment Recipe '$eqpValue' parameter in CAMSTAR '$param' not found in recipe body." 
					setLotExceptionMsg(cLot, msg)
				}
				else {
					boolean pass = RecipeParameterUtil.validateRecipeParameter(param, eqpValue)
					logger.info("Check " + param.getParameterName() + " Pass $pass")
					if (!pass) {
						String fixValue = param.getParameterFixValue()
						String minValue = param.getParameterMinValue()
						String maxValue = param.getParameterMaxValue()
						def exceptionMsg = param.getParameterName() + " failed parameter checking, eqp value = " + eqpValue + " Camstar parameter value (fix = " + fixValue + ", minValue = " + minValue + ", maxValue = " + maxValue + ")"
						setLotExceptionMsg(cLot, exceptionMsg)
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

	public Map getValueFromEqp(String eqpRecipe)
    {		
		def requestSml = "S7F25 W <A \"$eqpRecipe\" >."
		logger.info("Request SML :: $requestSml")	
		def parser = new SmlAsciiParser()
		def message = parser.parse(requestSml)
		
		def sentMessage = secsGemService.requestMessage(message)
		def responseSml = sentMessage.getSML()
		def data = sentMessage.getData()				
		logger.info("Response SML :: $responseSml")
		
        HashMap<String, String> recipeMap = new HashMap<String, String>()
        def dataList = data.getValueList()
        def param1 = ((SecsAsciiItem)dataList.get(0)).getString()
        def param2 = ((SecsAsciiItem)dataList.get(1)).getString()
        def param3 = ((SecsAsciiItem)dataList.get(2)).getString()
        logger.info("PPID=$param1, MDLN=$param2, SOFTREV=$param3")
		
        def recipeListLevel1 = ((SecsComposite)dataList.get(3)).getValueList()
		
        for (recipe in recipeListLevel1)     //  list of each module
        {
            def aList = recipe.getValueList()
			def valueList = ((SecsComposite)aList.get(1)).getValueList()
			def key = ((SecsAsciiItem)valueList.get(0)).getString()
			def value = ((SecsAsciiItem)valueList.get(4)).getString()
			
			recipeMap.put(key, value)
        }		
		logger.info("This is map value " + recipeMap)
		
		return recipeMap
    }
}