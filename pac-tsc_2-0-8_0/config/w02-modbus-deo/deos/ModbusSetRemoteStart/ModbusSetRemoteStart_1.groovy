package ModbusSetRemoteStart

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.services.modbus.SgdModBusService
import sg.znt.services.modbus.W02ModBusService
import sg.znt.services.modbus.SgdModBusServiceImpl.ModBusEvent
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class ModbusSetRemoteStart_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="ModBusEvent")
    private ModBusEvent modbusEvent

	@DeoBinding(id="W02ModBusService")
	private W02ModBusService modBusService

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		modBusService.setRemoteStart(modbusEvent.getChamber())
    }
}