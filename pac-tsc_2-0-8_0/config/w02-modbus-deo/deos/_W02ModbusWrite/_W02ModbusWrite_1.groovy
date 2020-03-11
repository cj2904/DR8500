package _W02ModbusWrite

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import sg.znt.services.modbus.W02ModBusService

@CompileStatic
@Deo(description='''
write modbus data
''')
class _W02ModbusWrite_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="W02ModBusService")
    private W02ModBusService w02ModBusService

    /**
     *
     */
    @DeoExecute
    public void execute()	
    {
		short[] s = [11,22,33,44,55]
		w02ModBusService.writeHoldingRegisterShortValue(11,s)
		def values = w02ModBusService.readHoldingRegisterShortValue(11, 5)
		for (var in values) 
		{
			logger.info("value is $var")
		}
    }
}