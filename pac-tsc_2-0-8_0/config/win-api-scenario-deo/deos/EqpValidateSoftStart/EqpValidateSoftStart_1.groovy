package EqpValidateSoftStart

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.machine.CEquipment
import sg.znt.pac.machine.TscEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Validate soft start of the state
''')
class EqpValidateSoftStart_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="Equipment")
    private CEquipment cEquipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        TscEquipment equipment = (TscEquipment) cEquipment
        def lotList = cMaterialManager.getCLotList(new LotFilterAll())
        for (lot in lotList)
        {
            if (lot.getEquipmentId().equalsIgnoreCase(cEquipment.getSystemId()))
            {
                validateValues(lot, equipment)
                break
            }
        }
    }
    
    void validateValues(CLot lot, TscEquipment cEquipment)
    {
        def recipeObj = cEquipment.getRecipeObject()
        if (recipeObj == null)
        {
            throw new Exception("No recipe defined in '" + cEquipment.getSystemId() + "'!")
        }
        def recipe = recipeObj.getUsedRecipeName()
        def params = new HashMap<String, Object>()
        params.put("LotId", lot.getId())
        params.put("Recipe", recipe)
        cEquipment.getModelScenario().eqpValidateValue(params)
    }
}