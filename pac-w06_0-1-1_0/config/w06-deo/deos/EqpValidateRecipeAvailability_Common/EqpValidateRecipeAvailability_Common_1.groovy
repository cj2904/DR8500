package EqpValidateRecipeAvailability_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.domainobject.W06RecipeParameterManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.RecipeParameterUtil
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S7F19CurrentEPPDRequest
import de.znt.services.secs.dto.S7F20CurrentEPPDDataDto.PpidList
import de.znt.zsecs.composite.SecsAsciiItem

@CompileStatic
@Deo(description='''
W06 common function:<br/>
<b>Verify if recipe exist in equipment</b>   

''')
class EqpValidateRecipeAvailability_Common_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment
    
    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
    
    @DeoBinding(id="W06RecipeParameterManager")
    private W06RecipeParameterManager w06RecipeParameterManager
    
    private SecsGemService secsGemService
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
            SecsGemService secsGemService = (SecsGemService) cEquipment.getExternalService()
            S7F19CurrentEPPDRequest request = new S7F19CurrentEPPDRequest()
            def reply = secsGemService.sendS7F19CurrentEPPDRequest(request)
            
            List<String> files = new ArrayList<String>();
            PpidList ppidList = (PpidList) reply.getPpidList()
            for(int i = 0; i < ppidList.getSize(); i++)
            {
                SecsAsciiItem recipe = (SecsAsciiItem) ppidList.getPPID(i)
                def singleRecipeName = recipe.getString()
                if(!files.contains(singleRecipeName))
                {
                    files.add(singleRecipeName);
                }
            }
                
            def exist = false
            for (recipe in files)
            {
                if(recipe.startsWith(eqpRecipe))
                {
                    exist = true
                    break
                }
            }
            
            if(!exist)
            {
                throw new Exception("Recipe '" + eqpRecipe + "' not found in '" + cEquipment.getSystemId() + "' equipment")
            }
        }
    }
}