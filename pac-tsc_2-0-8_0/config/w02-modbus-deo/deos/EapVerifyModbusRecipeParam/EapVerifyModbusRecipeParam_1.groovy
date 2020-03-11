package EapVerifyModbusRecipeParam

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import sg.znt.pac.machine.CEquipment
import sg.znt.services.modbus.W02ModBusService

@CompileStatic
@Deo(description='''
Verify recipe param value is correct at
modbus equipment.
''')
class EapVerifyModbusRecipeParam_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="W02ModBusService")
    private W02ModBusService w02ModBusService

	@DeoBinding(id="RecipeParam")
	private String recipeParam

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    
    }
}