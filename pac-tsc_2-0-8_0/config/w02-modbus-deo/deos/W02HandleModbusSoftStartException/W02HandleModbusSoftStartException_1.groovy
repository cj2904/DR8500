package W02HandleModbusSoftStartException

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.exception.ModbusException
import sg.znt.services.modbus.W02ModBusService
import sg.znt.services.modbus.SgdModBusServiceImpl.ModBusEvent
import de.znt.pac.deo.annotations.*
import de.znt.util.error.ErrorManager

@CompileStatic
@Deo(description='''
Handle modbus select recipe exception
''')
class W02HandleModbusSoftStartException_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="W02ModBusService")
    private W02ModBusService w02ModBusService

    @DeoBinding(id="ModBusEvent")
    private ModBusEvent modBusEvent

	@DeoBinding(id="Exception")
	private Throwable exception

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def errorCode =10
		if (exception instanceof ModbusException)
		{
			ModbusException modbusException = (ModbusException)exception 
			errorCode = modbusException.getErrorCode()
		}
        ErrorManager.handleError(exception, this)
		logger.error(exception.getMessage())
		w02ModBusService.setSoftStartValidationFailWarning(modBusEvent.getChamber(), errorCode)
    }
}