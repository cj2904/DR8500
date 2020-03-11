package EapVerifyPmThruput

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.TscConfig
import sg.znt.pac.TscConstants
import sg.znt.pac.domainobject.PmManager
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EapVerifyPmThruput_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="PmManager")
    private PmManager pmManager

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        if(TscConfig.getBooleanProperty("ThruputQty", true) == false)
        {
            return
        }
        def allobj = pmManager.getAllDomainObject()
        for(pm in allobj)
        {
            def pmname = pm.getPmName()
            def pmeqp = pm.getEquipmentId()
            logger.info(">PM Name " + pm.getPmName())
            logger.info(" PM Equipment " + pm.getEquipmentId())

            def childEqpTotalQty = 0

            def existingLotList = cMaterialManager.getCLotList(new LotFilterAll())
            for (existingLot in existingLotList)
            {
                def batchTotalQty = existingLot.getPropertyContainer().getInteger("BatchTotalQty",0)
                def firstLotInBatch = existingLot.getPropertyContainer().getBoolean(TscConstants.MATERIAL_ATTR_FIRST_LOT_IN_BATCH, true)

                if (firstLotInBatch)
                {
                    def childeqp = existingLot.getPropertyContainer().getStringArray(existingLot.getEquipmentId() + "_ChildEquipments", new String[0])
                    for(int i=0 ; i < childeqp.length ; i ++)
                    {
                        if(pmeqp.equalsIgnoreCase(childeqp[i]))
                        {
                            def recipeThruputFactor = 1
                            def allRecipe = existingLot.getAllRecipeObj()
                            for (recipe in allRecipe)
                            {
                                def recipechildEqp = recipe.getEquipmentLogicalId()
                                if (recipechildEqp.equalsIgnoreCase(pmeqp))
                                {
                                    if(recipe.getThruputFactor()!=null)
                                    {
                                        recipeThruputFactor = recipe.getThruputFactor()
                                    }
                                }
                            }

                            logger.info(" lot ["+existingLot.getId()+"] with child eq ["+childeqp[i]+"] found for pm eqp ["+pmeqp+"], add on batch qty ["+batchTotalQty+"] with thruputfactor ["+recipeThruputFactor+"].")
                            childEqpTotalQty = childEqpTotalQty + (batchTotalQty * recipeThruputFactor)
                        }
                    }
                }
            }

            logger.info(" childEqpTotalQty " + childEqpTotalQty)
            logger.info(" PMThruputQty2 " + pm.getThruputQty2())
            logger.info(" PMQty2 " + pm.getPmQty2())
            logger.info(" PMToleranceQty2 " +  pm.getToleranceQty2())

            if(pm.getPmQty2()>0)
            {
                def accumTrackIn = (pm.getThruputQty2() + childEqpTotalQty)
                def pmLimit = pm.getPmQty2()
                if (pm.getToleranceQty2() >0)
                {
                    pmLimit = pmLimit + pm.getToleranceQty2()
                }
                logger.info(" accumTrackIn " + accumTrackIn)
                logger.info("<pmLimit " +  pmLimit)

                if(accumTrackIn > pmLimit)
                {
                    throw new Exception("TrackIn Failed. Equipment ["+pmeqp+"] PM ["+pmname+"] is Over Thruput Limit ["+pmLimit+"] with Accumulate TrackIn Qty ["+accumTrackIn+"] From TotalEqpTrackInQty ["+childEqpTotalQty+"] PMThruputQty ["+pm.getThruputQty2()+"]!")
                }
            }
        }
    }
}