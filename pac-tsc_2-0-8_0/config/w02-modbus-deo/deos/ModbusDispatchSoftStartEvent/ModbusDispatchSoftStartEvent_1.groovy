package ModbusDispatchSoftStartEvent

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import sg.znt.services.modbus.SgdModBusServiceImpl.ModBusEvent
import sg.znt.services.modbus.W02ModBusService
import sg.znt.pac.scenario.modbus.w02.W02ModbusScenario
import sg.znt.pac.machine.TscEquipment
import java.lang.String

@CompileStatic
@Deo(description='''
Dispatch modbus event to handler
''')
class ModbusDispatchSoftStartEvent_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="ModBusEvent")
    private ModBusEvent modBusEvent

    @DeoBinding(id="TscEquipment")
    private TscEquipment tscEquipment

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		tscEquipment.getModelScenario().handleEqpSoftStartEvent(modBusEvent)
    }
}