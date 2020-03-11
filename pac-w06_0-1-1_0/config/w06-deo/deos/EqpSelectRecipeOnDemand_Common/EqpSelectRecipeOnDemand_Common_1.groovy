package EqpSelectRecipeOnDemand_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import java.lang.String
import sg.znt.pac.machine.CEquipment
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest

@CompileStatic
@Deo(description='''
W06 common function:<br/>
<b>Select recipe during track out or cancel track in</b>
''')
class EqpSelectRecipeOnDemand_Common_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="DummyRecipeIdForTrackOut")
    private String dummyRecipeIdForTrackOut = "IDLE"
    
    @DeoBinding(id="DummyRecipeIdForCancelTrackIn")
    private String dummyRecipeIdForCancelTrackIn

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        W02CompleteOutLotRequest completeLot = new W02CompleteOutLotRequest(inputXmlDocument)
        if (completeLot.isWaferProcessing())
        {
            //Case A: Use track out wafer list to determine if it was track out or cancel track in
            def trackOutWaferList = completeLot.getTrackOutWaferList()
            if (trackOutWaferList != null && trackOutWaferList.size() > 0)
            {
                cEquipment.getModelScenario().eqpSelectRecipe(dummyRecipeIdForTrackOut, new HashMap<String, Object>())
            }
            else if (trackOutWaferList != null && trackOutWaferList.size() == 0)
            {
                cEquipment.getModelScenario().eqpSelectRecipe(dummyRecipeIdForCancelTrackIn, new HashMap<String, Object>())
            }
            else if (trackOutWaferList == null)
            {
                logger.error("Error parsing wafer quantity as trackOutWaferList is null")
            }
            
            //Case B: Use wafer qty to determine if it was track out or cancel track in
//            def waferQty = completeLot.getWaferQty()
//            if (waferQty != null && waferQty.length() > 0)
//            {
//                if (Integer.parseInt(waferQty) > 0) //Track out
//                {
//                    cEquipment.getModelScenario().eqpSelectRecipe(dummyRecipeIdForTrackOut, new HashMap<String, Object>())
//                }
//                else //Cancel track in
//                {
//                    cEquipment.getModelScenario().eqpSelectRecipe(dummyRecipeIdForCancelTrackIn, new HashMap<String, Object>())
//                }
//            }
//            else
//            {
//                logger.error("Error parsing wafer quantity as waferQty is null or zero length")
//            }
        }
        else
        {
            def trackOutQty = completeLot.getTrackOutQty()
            if (trackOutQty != null && trackOutQty.length() > 0)
            {
                if (Integer.parseInt(trackOutQty) > 0) //Track out
                {
                    cEquipment.getModelScenario().eqpSelectRecipe(dummyRecipeIdForTrackOut, new HashMap<String, Object>())
                }
                else //Cancel track in
                {
                    cEquipment.getModelScenario().eqpSelectRecipe(dummyRecipeIdForCancelTrackIn, new HashMap<String, Object>())
                }
            }
            else
            {
                logger.error("Error parsing wafer quantity as trackOutQty is null or zero length")
            }
        }
        
    }
}