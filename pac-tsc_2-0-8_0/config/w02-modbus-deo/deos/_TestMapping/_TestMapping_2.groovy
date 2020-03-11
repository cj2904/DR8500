package _TestMapping

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.mapping.MappingConfigurationWrapper
import de.znt.pac.mapping.MappingManager

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class _TestMapping_2 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="MappingManager")
    private MappingManager mappingManager

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def configuration = mappingManager.getMappingConfigurationCopy()
		def schemas = configuration.getSchemas();
		
		for (schema in schemas) 
		{
			def components = schema.getSchemaComponents()
			for (component in components) 
			{
				def items = component.getSchemaItems()
				for (item in items) 
				{
					logger.info(component.getName() + "-(" + item.getName() + "):" + item.getUnit())
				}
			}
		}
    }
}