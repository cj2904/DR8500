package EapValidateRecipeParameter_GRD

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
class EapValidateRecipeParameter_GRD_1 {


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
		cEquipment.getPropertyContainer().setBoolean("stopEvent", true)
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

	public Map getValueFromEqp(String eqpRecipe) {
		def recipeBody
		HashMap<String, String> recipeMap = new HashMap<String, String>()
		def request = new S7F5ProcessProgramRequest()
		def ppId = new SecsAsciiItem(eqpRecipe)
		request.setPPID(ppId)

		def reply = secsGemService.sendS7F5ProcessProgramRequest(request)

		if(reply.getData().getPPBODY() instanceof SecsAsciiItem) {
			def recipeBodyInAscii = (SecsAsciiItem)reply.getData().getPPBODY()
			recipeBody = recipeBodyInAscii.getString().getBytes()
		}
		else {
			SecsBinary recipeBodyInBinary = (SecsBinary)reply.getData().getPPBODY()
			recipeBody = recipeBodyInBinary.getBytes()
		}
		recipeBody = new String(recipeBody)
		logger.info("This is the recipeBody after convert to string format: '$recipeBody'!")
		recipeBody = recipeBody.replace("\$", "")
		recipeBody = recipeBody.replace("Now", "")

		if(recipeBody.length() == 0) {
			throw new Exception("Recipe PPBody is empty! Please ensure it is available on Equipment!")
		}
		logger.info("Print RecipeBody " + recipeBody)

		def recipeBodyv1 = recipeBody.replace("\$", "").trim()
		def recipeBodyv2 = recipeBodyv1.split("\r")
		for (recipe in recipeBodyv2) {
			def recipeBodySplit = recipe.split("=")
			def recipeTitle = recipeBodySplit[0].trim()
			def newRecipeBody = recipeBodySplit[1].trim()
			recipeMap.put(recipeTitle, newRecipeBody);
		}

		logger.info("This is map value " + recipeMap)

		return recipeMap
	}
}