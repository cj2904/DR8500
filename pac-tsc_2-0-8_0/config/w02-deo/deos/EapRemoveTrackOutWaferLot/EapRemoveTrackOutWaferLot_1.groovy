package EapRemoveTrackOutWaferLot

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.domainobject.PM
import sg.znt.pac.domainobject.PmManager
import sg.znt.pac.domainobject.Recipe
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.WaferFilterAll
import sg.znt.services.camstar.outbound.TrackOutLotRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Check if previous wafer has successfully track out
''')
class EapRemoveTrackOutWaferLot_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="TscEquipment")
    private CEquipment equipment
    
    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager materialManager
    
    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager
    
    @DeoBinding(id="PMManager")
    private PmManager pmManager
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        TrackOutLotRequest outboundRequest = new TrackOutLotRequest(inputXmlDocument)
        
        def trackOutWaferList = outboundRequest.getTrackOutWaferList()
       
        def lotId = outboundRequest.getContainerName()
        def lot = materialManager.getCLot(lotId)
        def eqpId = outboundRequest.getResourceName()
        
        if (trackOutWaferList.size()==0)
        {
            def waferList = lot.getWaferList(new WaferFilterAll())
            for (wafer in waferList) 
            {
                if (wafer.getEquipmentId().equalsIgnoreCase(equipment.getSystemId()))
                {
                    logger.info("Remove wafer '" + wafer.getId() + "' from lot '" + lotId + "' since it's cancel track in.")
                    lot.removeWafer(wafer)
                }
            }            
        }
        else
        {
            for (toWafer in trackOutWaferList) 
            {
                def wafer = lot.getWafer(toWafer.getWaferNumber())
                if (wafer == null)
                {
                    logger.error("Wafer '" + toWafer.getWaferNumber() + "' is missing from lot '" + lotId + "'")
                }
                else
                {
                    logger.info("Remove wafer '" + wafer.getId() + "' from lot '" + lotId + "'")
                    lot.removeWafer(wafer)                    
                }
            }
        }
        
        cleanupRecipe(eqpId)
        cleanupPM(eqpId)
        
        if (lot.getWaferCount()==0)
        {
            logger.info("Remove lot '" + lot.getId() + "' from equipment '" + eqpId + "''")
            materialManager.removeCLot(lot)
        }
        equipment.setLastTrackOutLotId(lotId)
    }
    
    private void cleanupRecipe(String eqpId)
    {
        logger.info("Clean up recipe for '" + eqpId + "'")
        def allRecipe = recipeManager.getAllDomainObject()
        ArrayList<Recipe> arrCleanUp = new ArrayList()
        for (recipe in allRecipe)
        {
            if (recipe.getEquipmentId().equalsIgnoreCase(eqpId))
            {
                arrCleanUp.add(recipe)
            }
        }
        for (recipe in arrCleanUp) 
        {
            try 
            {
                logger.info("Removing recipe '" + recipe.getId() + " from $eqpId")
                recipeManager.removeDomainObject(recipeManager.getDomainObject(recipe.getId()))
            } 
            catch (Exception e) 
            {
                e.printStackTrace()
            }
        }
    }
    
    private void cleanupPM(String eqpId)
    {
        logger.info("Clean up PM for '" + eqpId + "'")
        def allPm = pmManager.getAllDomainObject()
        ArrayList<PM> arrCleanUp = new ArrayList()
        for (pm in allPm)
        {
            if (pm.getEquipmentId().equalsIgnoreCase(eqpId))
            {
                arrCleanUp.add(pm)
            }
        }
        for (pm in arrCleanUp)
        {
            try
            {
                logger.info("Removing PM '" + pm.getId() + " from $eqpId")
                pmManager.removeDomainObject(pmManager.getDomainObject(pm.getId()))
            }
            catch (Exception e)
            {
                e.printStackTrace()
            }
        }
    }
}