package SputterDprSelectRecipe

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.util.RecipeParameterUtil
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Sputter specific function:<br/>
<b>Dispatch to select recipe model scenario trigger</b>
''')
class SputterDprSelectRecipe_1 {


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
            def parameters = new HashMap<String, Object>()
            parameters.put("PPID", eqpRecipe)
            
            cEquipment.getModelScenario().eqpSelectRecipe(eqpRecipe, parameters)
        }
    }
}