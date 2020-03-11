package EapVerifyModbusRecipe

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import sg.znt.pac.machine.CEquipment
import sg.znt.services.modbus.W02ModBusService

@CompileStatic
@Deo(description='''
Verify load recipe is valie at modbus
equipment.
''')
class EapVerifyModbusRecipe_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="W02ModBusService")
    private W02ModBusService w02ModBusService

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    
    }
}