package BenchEapRecipeParameterValidation_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S7F25FormattedProcessProgramRequest
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsComposite
import de.znt.zsecs.composite.SecsU2Item
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.W06ChamberRecipeParameter
import sg.znt.pac.domainobject.W06RecipeParameterManager
import sg.znt.pac.domainobject.W06RecipeParameterSet
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.RecipeParameterUtil
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import sg.znt.services.camstar.outbound.W02TrackInLotRequest.RecipeParam

@CompileStatic
@Deo(description='''
W06 Bench specific logic:<br/>
<b>Validate chamber recipe parameter</b>
''')
class BenchEapRecipeParameterValidation_Common_1
{
	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())
	

	@DeoBinding(id="InputXmlDocument")
	private String inputXmlDocument

	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager

	@DeoBinding(id="W06RecipeParameterManager")
	private W06RecipeParameterManager w06RecipeParameterManager

	@DeoBinding(id="CEquipment")
	private CEquipment cEquipment

	SecsGemService secsGemService
	/**
	 *
	 */
	@DeoExecute
	public void execute()
	{
		secsGemService = (SecsGemService) cEquipment.getExternalService()

		W02TrackInLotRequest request = new W02TrackInLotRequest(inputXmlDocument)
		def lotId = request.getContainerName()
		def eqpId = request.getResourceName()
		def cLot = cMaterialManager.getCLot(request.getContainerName())
		if (cLot == null)
		{
			throw new Exception("Lot " + request.getContainerName() + " does not exist")
		}

		/*
		 * get formatted recipe
		 */
		
		def eqpRecipe = RecipeParameterUtil.getEqpRecipe(request.getRecipeParamList())
		LinkedHashMap<String, String> recipeParamMap = getFormattedRecipe(eqpRecipe)
		List<RecipeParam> recipeParamList = request.getRecipeParamList()
		for (recipeParam in recipeParamList)
		{
			if (recipeParam.getParamName().equalsIgnoreCase("SubRecipe"))
			{
				def eqpSubRecipes = recipeParamMap.get("SubRecipe")
				String[] eqpSubRecipeList = eqpSubRecipes.split(",")
				def mesSubRecipes = recipeParam.getParamValue()
				String[] mesSubRecipeList = mesSubRecipes.split(",")
				for (mesSubRecipe in mesSubRecipeList)
				{
					def subRecipeFound = false
					for (eqpSubRecipe in eqpSubRecipeList)
					{
						if(mesSubRecipe.trim().equalsIgnoreCase(eqpSubRecipe))
						{
							subRecipeFound = true
							break
						}
					}
					if (!subRecipeFound)
					{
						throw new Exception("MES modelled SubRecipe '$mesSubRecipe' not found in equipment sub recipes '$eqpSubRecipes'!")
					}
				}
			}
		}
		W06RecipeParameterSet chamberRecipe = w06RecipeParameterManager.getDomainObject("ChamberRecipe")
		for (recipeName in chamberRecipe.getRecipeNameList())
		{
			W06ChamberRecipeParameter recipeParam = (W06ChamberRecipeParameter) chamberRecipe.getElement(recipeName)
			def recipeParameters = recipeParam.getAllChamberRecipeParameter(recipeName)
			
			for (param in recipeParameters)
			{
				def recipeParameterId = recipeName + "@" + param.getTank() + "#" + param.getParameterName() + "%" + param.getStep()
				def eqpValue = recipeParamMap.get(recipeParameterId)
				if (eqpValue == null)
				{
					throw new Exception("Recipe parameter '" + recipeParameterId + "' not found in recipe parameter map '$recipeParamMap'!")
				}
				boolean pass = RecipeParameterUtil.validateRecipeParameter(param, eqpValue)
				if (!pass)
				{
					String fixValue = param.getParameterFixValue()
					String minValue = param.getParameterMinValue()
					String maxValue = param.getParameterMaxValue()
					def exceptionMsg = cLot.getPropertyContainer().getString("ExceptionMessage", "")
					if (exceptionMsg.length() == 0)
					{
						exceptionMsg = recipeParameterId + " failed parameter checking, eqp value = " + eqpValue + " Camstar parameter value (fix = " + fixValue + ", minValue = " + minValue + ", maxValue = " + maxValue
					}
					else
					{
						exceptionMsg += "\t" + recipeParameterId + " failed parameter checking, eqp value = " + eqpValue + " Camstar parameter value (fix = " + fixValue + ", minValue = " + minValue + ", maxValue = " + maxValue
					}
					cLot.getPropertyContainer().setString("ExceptionMessage", exceptionMsg)
				}
			}
		}
	}
	
	private LinkedHashMap<String,String> getFormattedRecipe(String eqpRecipe)
	{
		def request = new S7F25FormattedProcessProgramRequest(new SecsAsciiItem(eqpRecipe))
		def reply = secsGemService.sendS7F25FormattedProcessProgramRequest(request)
		def formattedRecipeData = reply.getData()
		def processCommands = formattedRecipeData.getProcessCommands()
		def chamberId = ""
		def subRecipeName = ""
		def recipeParamMap = new LinkedHashMap<String, String>()
		for (int i=0; i<processCommands.getSize(); i++)
		{
			def processCommand = processCommands.getProcessCommand(i)
			if (processCommand.getCCode().getValueList().get(0).toString().endsWith("1300"))
			{
				def params = processCommand.getParameters()
				def chamberName = params.getPParm(0).getValueList().get(0).toString()
				
				def charPos = chamberName.lastIndexOf("/")
				
				chamberId = PacConfig.getStringProperty(chamberName.substring(charPos+1) + ".ChamberId", "")
				if (chamberId.length()==0)
				{
					throw new Exception("Chamber ID could not be resolved for property '" + chamberName.substring(charPos+1) + ".ChamberId'!")
				}
				subRecipeName = params.getPParm(1).getValueList().get(0).toString()
				def subRecipe = recipeParamMap.get("SubRecipe")
				if (subRecipe == null)
				{
					recipeParamMap.put("SubRecipe", subRecipeName)
				}
				else
				{
					recipeParamMap.put("SubRecipe", subRecipe + "," + subRecipeName)
				}
				continue
			}
			else if (processCommand.getCCode().getValueList().get(0).toString().endsWith("1100"))
			{
				def params = processCommand.getParameters()
				def paramSize = params.getSize()
				if (paramSize == 5)
				{
					def step = params.getPParm(0).getValueList().get(0)
					def innerParam = ((SecsComposite)params.getPParm(1)).getValueList()
					if (innerParam.size()==1)
					{
						def speed = ((SecsU2Item)innerParam.get(0)).getInteger(0).toString()
						recipeParamMap.put(subRecipeName + "@" + chamberId + "#Speed%" + step, speed)
					}
					else if (innerParam.size()==5)
					{
						def supply = ((SecsU2Item)innerParam.get(0)).getInteger(0).toString()
						def shower = ((SecsU2Item)innerParam.get(1)).getInteger(0).toString()
						def n2Bubbling = ((SecsU2Item)innerParam.get(2)).getInteger(0).toString()
						def drain = ((SecsU2Item)innerParam.get(3)).getInteger(0).toString()
						def drainLine = ((SecsU2Item)innerParam.get(4)).getInteger(0).toString()
						
						recipeParamMap.put(subRecipeName + "@" + chamberId + "#Supply%" + step, supply)
						recipeParamMap.put(subRecipeName + "@" + chamberId + "#Shower%" + step, shower)
						recipeParamMap.put(subRecipeName + "@" + chamberId + "#N2Bubbling%" + step, n2Bubbling)
						recipeParamMap.put(subRecipeName + "@" + chamberId + "#Drain%" + step, drain)
						recipeParamMap.put(subRecipeName + "@" + chamberId + "#DrainLine%" + step, drainLine)
					}
					def jump = params.getPParm(2).getValueList().get(0).toString()
					def count = params.getPParm(3).getValueList().get(0).toString()
					def processTime = params.getPParm(4).getValueList().get(0).toString()
					
					recipeParamMap.put(subRecipeName + "@" + chamberId + "#Jump%" + step, jump)
					recipeParamMap.put(subRecipeName + "@" + chamberId + "#Count%" + step, count)
					recipeParamMap.put(subRecipeName + "@" + chamberId + "#ProcessTime%" + step, processTime)
				}
			}
		}
		logger.info("RecipeParameterMap:'$recipeParamMap'")
		return recipeParamMap
	}
}