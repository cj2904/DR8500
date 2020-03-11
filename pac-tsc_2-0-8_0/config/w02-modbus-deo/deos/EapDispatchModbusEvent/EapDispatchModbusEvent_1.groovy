package EapDispatchModbusEvent

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.scenario.EqModelScenario
import sg.znt.pac.scenario.modbus.w02.W02ModbusScenario
import sg.znt.services.modbus.W02ModBusService
import sg.znt.services.modbus.SgdModBusServiceImpl.ModBusEvent
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.trigger.DeoEventDispatcher

@CompileStatic
@Deo(description='''

''')
class EapDispatchModbusEvent_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="DeoEventDispatcher")
    private DeoEventDispatcher deoEventDispatcher

	@DeoBinding(id="ModbusService")
	private W02ModBusService modbusService

	@DeoBinding(id="ModbusEvent")
	private ModBusEvent modbusEvent

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		Map<String, Object> params = new HashMap<String, Object>()
		params.put(W02ModBusService.MODBUS_SERVICE, modbusService)
		params.put(W02ModBusService.MODBUS_EVENT, modbusEvent)

		deoEventDispatcher.notifyEvent(EqModelScenario.PROVIDER, new W02ModbusScenario().getModelType(), modbusEvent.getName(), params)
    }
}