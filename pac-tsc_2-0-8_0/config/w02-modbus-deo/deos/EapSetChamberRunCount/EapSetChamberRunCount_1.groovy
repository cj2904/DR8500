package EapSetChamberRunCount

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.TscConstants
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.modbus.SgdModBusServiceImpl.ModBusEvent
import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Set Chamber run count at soft start
''')
class EapSetChamberRunCount_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="ModBusEvent")
    private ModBusEvent modBusEvent

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

	@DeoBinding(id="RecipeManager")
	private RecipeManager recipeManager

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def chamber = modBusEvent.getChamber()
        def eqLogicalId = PacConfig.getStringProperty(modBusEvent.getChamber() + ".SystemId", "")
		def allLot = cMaterialManager.getCLotList(new LotFilterAll())
        long batch = -1L
		for (lot in allLot) 
		{
            def allLotRecipe = lot.getAllRecipeObj()
            for(recipe in allLotRecipe)
            {
                if (recipe.getEquipmentLogicalId().equalsIgnoreCase(eqLogicalId))
                {
                    def lotBatch = lot.getPropertyContainer().getLong("BatchID", new Long(-1)).longValue()
                    if (batch ==-1)
                    {
                        batch = lotBatch
                    }
                    if (batch == lotBatch)
                    {
                        def runCount = lot.getPropertyContainer().getInteger(eqLogicalId+TscConstants.LOT_ATTR_CHAMBER_RUN_COUNT_POSTFIX, 0)
                        def newRunCount = runCount + 1
                        logger.error("Run Count: [" + lot.getId() + "," + lotBatch + "] increased to  " + newRunCount)
                        lot.getPropertyContainer().setInteger(eqLogicalId+TscConstants.LOT_ATTR_CHAMBER_RUN_COUNT_POSTFIX, newRunCount)
                    }
                    else
                    {
                        logger.error("Run Count: [" + lot.getId() + "," + lotBatch + "] is not in the same batch with " + batch)
                    }
                }
            }
		}
    }
}