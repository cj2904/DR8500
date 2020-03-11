package W02ModbusReadData

import java.lang.reflect.Array;

import org.apache.commons.logging.*
import de.znt.modbus.config.*
import de.znt.pac.deo.annotations.*
import de.znt.services.modbus.ModBusService
import de.znt.services.modbus.ModBusServiceImpl;

@Deo(description='''
Example DEO to read data via Modbus service adapter
''')
class W02ModbusReadData_1
{
	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="ModBusService")
	private ModBusService modBusService

	@DeoExecute(input=["ParameterName", "Addresse","DataType","AddressType","ArraySize","SlaveId"])
	public void readParameter(String parameterName, Integer addresse,DataType dataType, AddressType addressType,Short arraySize,Integer slaveId)
	{
		def parameter = new VariableConfiguration()
		parameter.setName("TEST")
		parameter.setArraySize((short)1)
		parameter.setModbusType(DataType.INT_16)
		parameter.setAddressType(AddressType.HOLDING_REGISTER)
		parameter.setAddress(addresse)
		
		parameter.setSlaveID((short)slaveId)
		def result = modBusService.read([parameter])
		//Test message
		result.each
		{ value ->
			if(value.getValueValid())
			{
				logger.info("Valid result for '${value.getName()}', value='${value.getStringValues()}'.")
			}
			else
			{
				logger.error("Invalid result for '${value.getName()}', value='${value.getStringValues()}'.")
			}
		}

	}
}