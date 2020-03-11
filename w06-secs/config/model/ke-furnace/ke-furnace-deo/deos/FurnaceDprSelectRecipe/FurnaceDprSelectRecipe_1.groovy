package FurnaceDprSelectRecipe

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsBinary
import de.znt.zsecs.composite.SecsComposite
import de.znt.zsecs.composite.SecsU1Item
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.util.RecipeParameterUtil
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
KE-Furnace specific function:<br/>
<b>Dispatch to select recipe model scenario trigger</b>
''')
class FurnaceDprSelectRecipe_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())
    
    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    
    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        W02TrackInLotRequest trackInLot = new W02TrackInLotRequest(inputXmlDocument)
        String eqpRecipe = RecipeParameterUtil.getEqpRecipe(trackInLot.getRecipeParamList())
        
        if (eqpRecipe == null || eqpRecipe.length() == 0)
        {
            throw new Exception("Could not find EqpRecipe in Camstar recipe parameter")
        }
        else
        {
            cEquipment.getModelScenario().eqpSelectRecipe(eqpRecipe, new HashMap<String, Object>())
        }
    }
}