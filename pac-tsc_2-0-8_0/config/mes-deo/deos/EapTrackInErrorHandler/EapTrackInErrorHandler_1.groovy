package EapTrackInErrorHandler

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.camstar.semisuite.service.inbound.dto.RecipeDownloadRequest
import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked
import sg.znt.pac.TscConfig
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.material.WaferFilterAll
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@Deo(description='''
pac error handler
''')
class EapTrackInErrorHandler_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="RecipeManager")
    RecipeManager recipeManager

    @DeoBinding(id="Exception")
    private Throwable exception

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
        def outboundRequest = new W02TrackInLotRequest(inputXmlDocument)
        def lotId = outboundRequest.getContainerName()

        try
        {
            def cLot = cMaterialManager.getCLot(lotId)

            if(cLot != null)
            {
                def curLotEqp = cLot.getEquipmentId()
                def curLotRecipe = cLot.getRecipe()

                if (!TscConfig.getBooleanProperty("SingleWaferProcessing", false))
                {
                    cMaterialManager.removeCLot(cLot)
                }
                else
                {
                    def trackInWaferList = outboundRequest.getLotTrackInWaferList()
                    if(trackInWaferList != null && trackInWaferList.size() > 0)
                    {
                        for (trackInWafer in trackInWaferList)
                        {
                            def waferId = trackInWafer.getWaferNumber()
                            logger.info("Remove wafer '" + waferId + "' from lot '" + lotId + "' since it's cancel track in.")
                            cLot.removeWafer(cLot.getWafer(waferId))
                        }
                    }
                    if (cLot.getWaferCount() == 0)
                    {
                        cMaterialManager.removeCLot(cLot)
                        logger.info("Remove lot since it's the only wafer!")
                    }
                }

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