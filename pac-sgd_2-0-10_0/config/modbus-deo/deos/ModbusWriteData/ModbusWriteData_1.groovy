package ModbusWriteData
 
import org.apache.commons.logging.*

import de.znt.modbus.config.*
import de.znt.modbus.fccom.ModbusVariableFCShort
import de.znt.pac.deo.annotations.*
import de.znt.services.modbus.ModBusService
 
@Deo(description='''
Example DEO to write data via modbus ESA
''')
class ModbusWriteData_1
{
    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())
 
    @DeoBinding(id="ModBusService")
    private ModBusService modBusService
 
    @DeoExecute(input=["ParameterName", "Addresse", "Value","SlaveId"])
    public void writeParameters(String parameterName, Integer addresse, Integer value,Integer slaveId)
    {
        def parameter = new VariableConfiguration()
        parameter.setName(parameterName)
        parameter.setModbusType(DataType.INT_16)
        parameter.setArraySize((short)slaveId)
        parameter.setAddressType(AddressType.HOLDING_REGISTER)
        parameter.setAddress(addresse)
		parameter.setSlaveID((short)1)
		Short[] a=new Short[1]
		a[0]=(short)value				
		def valueToWrite = new ModbusVariableFCShort(parameter, a)
		
        modBusService.write([valueToWrite])
		logger.info("Address $addresse write with value $value")
		
    }
}