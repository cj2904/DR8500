package Test1

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.material.CMaterialManager

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class Test1_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager
	
	@DeoBinding(id="WipDataManager")
	private WipDataDomainObjectManager wipDataManager

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def lot=cMaterialManager.getCLot("F32200A-JAH003-M0")
		cMaterialManager.removeCLot(lot)
		wipDataManager.removeAllDomainObject()
    }
}