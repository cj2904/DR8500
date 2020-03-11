package _TestReadModbus

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.services.modbus.W02ModBusService
import de.znt.modbus.config.AddressType
import de.znt.modbus.config.DataType
import de.znt.modbus.config.VariableConfiguration
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class _TestReadModbus_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="W02ModBusService")
    private W02ModBusService w02ModBusService

    @DeoBinding(id="Address")
    private String address

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def result = w02ModBusService.readHoldingRegisterIntValue(Integer.parseInt(address))
		logger.info("Result at $address is $result")
		
		/*
		 * 
		def conf = new VariableConfiguration();
		conf.setName("Test");
		conf.setArraySize((short)1);
		conf.setModbusType(DataType.INT_32);
		conf.setAddressType(AddressType.HOLDING_REGISTER);
		conf.setAddress(Integer.parseInt(address));
		conf.setSlaveID((byte)1);

		try
		{
			def result = w02ModBusService.read([conf]);
			//ModbusVariableFCInteger variable = (ModbusVariableFCInteger) values.get(0);
			logger.info(variable.getValues()[0])
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
				 */

    }
}