package _TestAddNewDO

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import sg.znt.pac.domainobject.PmManager
import java.lang.String

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class _TestAddNewDO_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="PmManager")
    private PmManager pmManager

    @DeoBinding(id="ObjectId")
    private String objId

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def obj = pmManager.createDomainObject(objId)
		pmManager.addDomainObject(obj)
    }
}