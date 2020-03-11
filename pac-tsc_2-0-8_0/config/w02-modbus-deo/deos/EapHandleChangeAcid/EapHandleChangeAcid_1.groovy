package EapHandleChangeAcid

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import sg.znt.pac.material.CMaterialManager;
import sg.znt.services.camstar.CCamstarService;
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EapHandleChangeAcid_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="CCamstarService")
	private CCamstarService cCamstarService

	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    	//1. check if lot exisit
		//2. cancel track in existing lot
		//3. close pm for equipment
    }
}