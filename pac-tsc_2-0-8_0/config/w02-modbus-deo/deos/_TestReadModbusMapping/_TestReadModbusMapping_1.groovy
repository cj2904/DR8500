package _TestReadModbusMapping

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import sg.znt.services.modbus.W02ModBusService
import java.lang.String

@CompileStatic
@Deo(description='''
Read mapping value
''')
class _TestReadModbusMapping_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="W02ModBusService")
    private W02ModBusService w02ModBusService

    @DeoBinding(id="MappingSetName")
    private String mappingSetName

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def container = w02ModBusService.readMappingValue(mappingSetName)
		def map = container.getContainerMap()
		def keys =map.keySet()
		for (key in keys) 
		{
			def value = map.get(key)
			logger.info("Value for $key is $value.")
		}
    }
}