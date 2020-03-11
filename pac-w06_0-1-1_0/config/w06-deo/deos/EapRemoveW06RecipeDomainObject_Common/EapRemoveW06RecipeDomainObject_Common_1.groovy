package EapRemoveW06RecipeDomainObject_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.ItemNotFoundException
import de.znt.pac.deo.annotations.*
import de.znt.pac.domainobject.DomainObject
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.W06RecipeParameterManager
import sg.znt.pac.domainobject.W06RecipeParameterSet
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest

@CompileStatic
@Deo(description='''
W06 common function:<br/>
<b>Remove recipe domain object</b>
''')
class EapRemoveW06RecipeDomainObject_Common_1
{


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())


	@DeoBinding(id="InputXml")
	private String inputXml

	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager

	@DeoBinding(id="W06RecipeParameterManager")
	private W06RecipeParameterManager w06RecipeParameterManager

	/**
	 *	remove domain object with "EqpSystemId-CamstarRecipeObjName" after removing all outbound lot domain object
	 */

	@DeoExecute
	public void execute()
	{
		def completeLotOutbound = new W02CompleteOutLotRequest(inputXml)
		def resourceName = completeLotOutbound.getResourceName()
		def outboundLotList = completeLotOutbound.getLotList()
		def cLotList = cMaterialManager.getCLotList(new LotFilterAll())

		List<String> outboundLotRecipeList = new ArrayList<String>()
		for (lot in outboundLotList)
		{
			CLot cLot = null
			try
			{
				cLot = cMaterialManager.getCLot(lot)
			}
			catch (ItemNotFoundException e)
			{
				logger.info("Lot ID '$lot' not found in pac Material Manager, skip remove W06 Recipe Domain Object...")
				continue
			}
			def cLotRecipe = cLot.getRecipe()
			def recipeExist = false
			for (outboundLotRecipe in outboundLotRecipeList)
			{
				if (outboundLotRecipe.equalsIgnoreCase(cLotRecipe))
				{
					recipeExist = true
					break
				}
			}
			if (!recipeExist)
			{
				outboundLotRecipeList.add(cLotRecipe)				
			}
		}
		logger.info("Lot recipe list '$outboundLotRecipeList'")

		List<String> recipeIdsInUsed = new ArrayList<String>()
		for (outboundLotRecipe in outboundLotRecipeList)
		{
			def recipeInUsed = false
			for (cLot in cLotList)
			{
				if (!isLotMatch(cLot.getId(), outboundLotList))
				{
					if (cLot.getRecipe().equalsIgnoreCase(outboundLotRecipe) && cLot.getEquipmentId().equalsIgnoreCase(resourceName))
					{
						recipeIdsInUsed.add(resourceName + "-" + outboundLotRecipe)
						recipeInUsed = true
						break
					}
				}
			}
			if (recipeInUsed)
			{
				continue
			}
		}
		
		/*
		 * remove recipe domain object since there is no other cLot using
		 */
		List<W06RecipeParameterSet> w06RecipeParamSetList = w06RecipeParameterManager.getAllDomainObject()
		for (w06RecipeParamSet in w06RecipeParamSetList)
		{
			boolean recipeFound = false
			for (recipeIdInUsed in recipeIdsInUsed)
			{
				if (recipeIdInUsed.equalsIgnoreCase(w06RecipeParamSet.getId()))
				{
					recipeFound = true
					break
				}
			}
			if (!recipeFound)
			{
				logger.info("Removing W06RecipeParameterDomainObject '" + w06RecipeParamSet.getId() + "'...")
				w06RecipeParameterManager.removeDomainObject(w06RecipeParamSet)
			}
		}
	}
	
	private boolean isLotMatch(String curLotId, List<String> lotIdList)
	{
		for (lotId in lotIdList)
		{
			if (lotId.equalsIgnoreCase(curLotId))
			{
				return true
			}
		}
		return false
	}
}