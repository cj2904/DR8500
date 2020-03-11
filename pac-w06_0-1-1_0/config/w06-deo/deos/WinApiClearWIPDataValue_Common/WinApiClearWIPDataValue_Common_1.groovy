package WinApiClearWIPDataValue_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.WinApiWipDataManager
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
W06 common function:<br/>
<b>Clear all WIP data in persistent</b>
''')
class WinApiClearWIPDataValue_Common_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="WinApiWipDataManager")
	private WinApiWipDataManager winApiWipDataManager
	

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def allWipData = winApiWipDataManager.getAllDomainObject()
		for (wipData in allWipData)
		{
			wipData.setWipDataValue("")
			wipData.notifyListeners()
		}
    }
}