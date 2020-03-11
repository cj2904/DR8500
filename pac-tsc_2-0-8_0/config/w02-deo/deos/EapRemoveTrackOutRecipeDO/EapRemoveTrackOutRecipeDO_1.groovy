package EapRemoveTrackOutRecipeDO

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.material.WaferFilterAll
import sg.znt.services.camstar.outbound.TrackOutLotRequest

@CompileStatic
@Deo(description='''
remove track out lot recipeDO
''')
class EapRemoveTrackOutRecipeDO_1
{

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="RecipeManager")
    RecipeManager recipeManager

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
        def outbound = new TrackOutLotRequest(inputXmlDocument)
        def outboundLot = outbound.getContainerName()

        try
        {
            def cLot = cMaterialManager.getCLot(outboundLot)

            if(cLot != null)
            {
                def curLotEqp = cLot.getEquipmentId()
                def curLotRecipe = cLot.getRecipe()

                def singleWaferProcessing = PacConfig.getBooleanProperty("SingleWaferProcessing", false)
                def lotLvlFound = false
                def lotList = cMaterialManager.getCLotList(new LotFilterAll())
                for(lot in lotList)
                {
                    if (singleWaferProcessing)
                    {
                        def waferLvlFound = false
                        def waferList = lot.getWaferList(new WaferFilterAll())
                        for (wafer in waferList)
                        {
                            if (wafer.getEquipmentId().equalsIgnoreCase(curLotEqp) && lot.getRecipe().equalsIgnoreCase(curLotRecipe))
                            {
                                logger.info("Lot '" + lot.getId() + "' with Wafer '" + wafer.getId() + "' in Equipment '$curLotEqp' using recipe '$curLotRecipe', skip remove from Recipe Domain Object" )
                                waferLvlFound = true
                                break
                            }
                        }
                        if (waferLvlFound)
                        {
                            lotLvlFound = true
                            break
                        }
                    }
                    else
                    {
                        if (lot.getEquipmentId().equalsIgnoreCase(curLotEqp) && lot.getRecipe().equalsIgnoreCase(curLotRecipe))
                        {
                            lotLvlFound = true
                            break
                        }
                    }
                }
                if(lotLvlFound == false)
                {
                    def recipeDOs = recipeManager.getAllDomainObject()
                    for (recipeDO in recipeDOs)
                    {
                        if (recipeDO.getMainEquipmentId().equalsIgnoreCase(curLotEqp) && recipeDO.getMainRecipeName().equalsIgnoreCase(curLotRecipe))
                        {
                            logger.info("Removing Recipe Domain Object '" + recipeDO.getId() + "' ...")
                            recipeManager.removeDomainObject(recipeDO)
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage())
        }
    }
}