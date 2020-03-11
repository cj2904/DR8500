package AOIDprSendStartCommand

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.RecipeParameterUtil
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
AOI specific function:<br/>
<b>Dispatch to start equipment model scenario trigger</b>
''')
class AOIDprSendStartCommand_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())
   
    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment
    
    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        W02TrackInLotRequest trackInLot = new W02TrackInLotRequest(inputXmlDocument)
        def cLot = cMaterialManager.getCLot(trackInLot.getContainerName())
        if (cLot == null)
        {
            throw new Exception("Lot " + trackInLot.getContainerName() + " does not exist")
        }
        
        String eqpRecipe = RecipeParameterUtil.getEqpRecipe(trackInLot.getRecipeParamList())
        
        if (eqpRecipe == null || eqpRecipe.length() == 0)
        {
            throw new Exception("Could not find EqpRecipe in Camstar recipe parameter")
        }
        else
        {
			def lotId = cLot.getId()
            def parameters = new HashMap<String, Object>()
            parameters.put("PORTNO", "1")
            parameters.put("CSTNAME", lotId) //TODO: dummy value or cassette can be obtain from Camstar outbound call?
            parameters.put("RCPNAME", eqpRecipe)
            //parameters.put("SLOT", "1,2,3,4,5,6,7,8,9,10") //TODO: This field can be remove when AOI vendor finish modifying their software
            
            cEquipment.getModelScenario().eqpStartEquipment(parameters)
        }
    }
}