package EapRemoveRecipeDomainObject

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
remove recipe domain object for a lot when more than one lot in pac
''')
class EapRemoveRecipeDomainObject_1
{

    @DeoBinding(id="RecipeManager")
    RecipeManager recipeManager

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
        def recipeDOs = recipeManager.getAllDomainObject()
        def lotList = cMaterialManager.getCLotList(new LotFilterAll())
        def recipe = outbound.getRecipeName()
        def eqpId = outbound.getResourceName()
        def similarDO = false
        for (lot in lotList)
        {
            if (lot.getRecipe().equalsIgnoreCase(recipe) && lot.getEquipmentId().equalsIgnoreCase(eqpId))
            {
                logger.info("Lot '" + lot.getId() + "' has is using the has the same equipment ID and recipe Name, domain object will not be removed!")
                similarDO = true
                break
            }
        }
        if (!similarDO)
        {
            for (recipeDO in recipeDOs)
            {
                if(recipeDO.getRecipeName().equalsIgnoreCase(outbound.getRecipeName()) && recipeDO.getMainEquipmentId().equalsIgnoreCase(outbound.getResourceName()))
                {
                    recipeManager.removeDomainObject(recipeDO)
                    logger.info("Recipe Domain Object '" + recipeDO.getId() + "' is being removed from Recipe Manager")
                    break
                }
            }
        }
    }
}