package EqpLoadAndSetRecipe

import org.apache.commons.lang3.StringUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.CompileStatic
import sg.znt.pac.W06Constants
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
eqp load and set recipe
''')
class EqpLoadAndSetRecipe_1
{

	@DeoBinding(id="SecsGemService")
	private SecsGemService secsGemService

	@DeoBinding(id="CEquipment")
	private CEquipment cEquipment

	@DeoBinding(id="RecipeManager")
	private RecipeManager recipeManager

	@DeoBinding(id="WipDataDomainObjectManager")
	private WipDataDomainObjectManager wipDataDomainObjectManager

	@DeoBinding(id="InputXml")
	private String inputXml

	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())


	/**
	 *
	 */
	@DeoExecute
	public void execute()
	{
		def outbound = new W02TrackInLotRequest(inputXml)
		def lotId = outbound.getContainerName()
		def recipeName = outbound.getRecipeName()

		if (recipeName == null || recipeName.length() == 0)
		{
			throw new Exception("Recipe not found!")
		}

		def recipeId = cEquipment.getSystemId() + "-" + recipeName
		def recipe = recipeManager.getDomainObject(recipeId)

		if (recipe == null)
		{
			throw new Exception("$recipeId not found in recipe domain object!")
		}

		def recipeParam = recipe.getElement("EqpRecipe")

		if (recipeParam == null)
		{
			throw new Exception("Equipment recipe cannot be empty, please configure as Recipe Parameter with 'EqpRecipe' in Camstar!")
		}

		def recipeValue = recipeParam.getParameterValue()
		if (recipeValue==null || recipeValue.length()==0)
		{
			throw new Exception("Equipment recipe value cannot be empty, please configure 'EqpRecipe' value in Camstar!")
		}
		else
		{
			sendRecipeLoad(recipeValue)
		}

		def wipData = outbound.getResourceName() + "-" + outbound.getContainerName()
		def wipDataDo = wipDataDomainObjectManager.getWipDataSet(wipData)

		def markingCodePrefix = ""
		def markingSequenceRange = ""
		if(wipDataDo != null)
		{
			def trackInWipData = wipDataDo.getTrackInWipDataItems()
			if(trackInWipData != null)
			{
				for(wip in trackInWipData)
				{
					if (wip.getId().equalsIgnoreCase(W06Constants.MES_WIP_DATA_MARKING_CODE_PREFIX))
					{
						markingCodePrefix = wip.getValue()
						logger.info(W06Constants.MES_WIP_DATA_MARKING_CODE_PREFIX + ":'$markingCodePrefix'")
						continue
					}
					else if (wip.getId().equalsIgnoreCase(W06Constants.MES_WIP_DATA_MARKING_SEQUENCE_RANGE))
					{
						markingSequenceRange = wip.getValue()
						logger.info(W06Constants.MES_WIP_DATA_MARKING_SEQUENCE_RANGE + ":'$markingSequenceRange'")
						continue
					}
				}
			}
		}

		if (markingSequenceRange.length()>0)
		{
			def startAt = -1
			def endAt = -1
			def seqRangeArray = markingSequenceRange.split("-")
			if (seqRangeArray.length < 3)
			{
				def startSeq = seqRangeArray[0].trim()
				try
				{
					startAt = new Integer(startSeq)
				}
				catch (NumberFormatException e)
				{
					throw new Exception("Cannot cast '$startSeq' to Integer data type!")
				}

				if (seqRangeArray.length == 2)
				{
					def endSeq = seqRangeArray[1].trim()
					try
					{
						endAt = new Integer(endSeq)
					}
					catch (NumberFormatException e)
					{
						throw new Exception("Cannot cast '$endSeq' to Integer data type!")
					}
				}
			}
			else
			{
				throw new Exception("WIP data '" + W06Constants.MES_WIP_DATA_MARKING_SEQUENCE_RANGE + "' value '$markingSequenceRange' is not in the correct format '<Start Sequence Number>-<End Sequence Number>'!")
			}

			if (markingCodePrefix.length()>0)
			{
				markingCodePrefix += "-"
			}

			if (startAt > -1)
			{
				def extMarkingText = ""
				if (endAt > -1)
				{
					def range = endAt - startAt
					if (range > 24 || range < 0)
					{
						throw new Exception("Marking Sequence Range is not acceptable with value '$markingSequenceRange'!")
					}
					for (int i=startAt; i<=endAt; i++)
					{
						extMarkingText += markingCodePrefix + StringUtils.leftPad(i.toString(), 3, "0") + ","
					}
					def counter = extMarkingText.count(",")
					if (counter == 25 && extMarkingText.endsWith(","))
					{
						extMarkingText = extMarkingText.substring(0, extMarkingText.length()-1)
					}
					else if (counter > 24)
					{
						throw new Exception("The format of ExternalMarkingText is invalid:'$extMarkingText'!")
					}
					for (int j=counter+1; j<25; j++)
					{
						extMarkingText += ","
					}
				}
				else
				{
					extMarkingText = markingCodePrefix + StringUtils.leftPad(startAt.toString(), 3, "0") + StringUtils.repeat(",", 24)
				}

				sendRecipeSet("UseExternalMarkingText", "True")
				sendRecipeSet("ExternalMarkingText", extMarkingText)
			}
			else
			{
				throw new Exception("Start Marking Sequence cannot be a negative value '$startAt'!")
			}
		}
		else
		{
			sendRecipeSet("MainText", lotId)
		}
	}

	void sendRecipeLoad(String recipeValue)
	{
		def request =  new S2F41HostCommandSend(new SecsAsciiItem("Recipe_Load"))

		request.addParameter(new SecsAsciiItem("RecipeName") , new SecsAsciiItem(recipeValue))

		S2F42HostCommandAcknowledge reply = secsGemService.sendS2F41HostCommandSend(request)
		logger.info "PPSelect command : " + reply.getHCAckMessage()
		if (!reply.isCommandAccepted())
		{
			throw new Exception("Fail to select recipe with error: " + reply.getHCAckMessage())
		}
	}

	void sendRecipeSet(String variable, String value)
	{
		def request =  new S2F41HostCommandSend(new SecsAsciiItem("Recipe_Set"))

		request.addParameter(new SecsAsciiItem("Variable") , new SecsAsciiItem(variable))
		request.addParameter(new SecsAsciiItem("Value") , new SecsAsciiItem(value))
		
		S2F42HostCommandAcknowledge reply = secsGemService.sendS2F41HostCommandSend(request)
		logger.info "PPSelect command : " + reply.getHCAckMessage()
		if (!reply.isCommandAccepted())
		{
			throw new Exception("Fail to perform 'Recipe_Set' with error: " + reply.getHCAckMessage())
		}
	}
}