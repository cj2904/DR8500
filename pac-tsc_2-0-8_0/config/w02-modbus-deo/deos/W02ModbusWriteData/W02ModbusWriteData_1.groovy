package W02ModbusWriteData
 
import org.apache.commons.logging.*

import de.znt.modbus.config.*
import de.znt.modbus.fccom.ModbusVariableFCShort
import de.znt.pac.deo.annotations.*
import de.znt.services.modbus.ModBusService
 
@Deo(description='''
Example DEO to write data via modbus ESA
''')
class W02ModbusWriteData_1
{
    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())
 
    @DeoBinding(id="ModBusService")
    private ModBusService modBusService
 
    @DeoExecute(input=["ParameterName", "Addresse", "Value","SlaveId"])
    public void writeParameters(String parameterName, Integer addresse, Integer value,Integer slaveId)
    {
        def parameter = new VariableConfiguration()
        parameter.setName("TEST")
        parameter.setModbusType(DataType.INT_16)
        parameter.setArraySize((short)1)
        parameter.setAddressType(AddressType.HOLDING_REGISTER)
        parameter.setAddress(addresse)
		parameter.setSlaveID((short)slaveId)
		Short[] a=new Short[1]
		a[0]=(short)value	//TODO: Kelly & Patrick to change			
		def valueToWrite = new ModbusVariableFCShort(parameter, a)
		
        modBusService.write([valueToWrite])
		logger.info("Address $addresse write with value $value")
		
    }
}