package _TestSetData

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class _TestSetData_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

	@DeoBinding(id="Key")
	private String key

	@DeoBinding(id="Value")
	private String value

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		cEquipment.getPropertyContainer().setString(key, value)
    }
}