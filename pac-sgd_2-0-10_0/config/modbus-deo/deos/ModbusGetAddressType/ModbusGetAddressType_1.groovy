package ModbusGetAddressType

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import de.znt.pac.deo.annotations.*
import de.znt.modbus.config.AddressType

@Deo(description='''
Address Type
''')
class ModbusGetAddressType_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="AddressType")
    private AddressType addressType

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    	logger.info(addressType.value())
    }
}