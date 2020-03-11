package EqpStoreEventParameter

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked
import sg.znt.pac.exception.ValidationFailureException
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll

@Deo(description='''
Keep event file content
''')
class EqpStoreEventParameter_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager
	
	@DeoBinding(id="EventName")
	private String eventName
	
	@DeoBinding(id="Parameter")
	private Map parameter
	
	@DeoBinding(id="PropertyPrefix")
	private String propertyPrefix
    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
		if(parameter.size() == 0)
		{
			throw new ValidationFailureException("", eventName + "::: No parameter found")
		}
		
		List<CLot> cLotList = cMaterialManager.getCLotList(new LotFilterAll())
    	CLot lot = cLotList.get(0)
		for (entry in parameter.entrySet()) 
		{
			String value = entry.getValue()
			lot.getPropertyContainer().setString(propertyPrefix + "_" + entry.getKey(), value)
		}
    }
}