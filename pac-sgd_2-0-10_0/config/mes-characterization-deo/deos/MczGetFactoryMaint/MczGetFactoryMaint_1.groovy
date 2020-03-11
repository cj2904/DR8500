package MczGetFactoryMaint

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import sg.znt.camstar.semisuite.service.dto.GetFactoryMaintRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl

@CompileStatic
@Deo(description='''
Get factory setting from Camstar
''')
class MczGetFactoryMaint_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="Factory")
    private String factory
    
    @DeoBinding(id="CCamstarServiceImpl")
    private CCamstarServiceImpl cCamstarService
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        GetFactoryMaintRequest request = new GetFactoryMaintRequest(factory)
        def reply = cCamstarService.getFactoryMaint(request)
        if (reply.isSuccessful())
        {
            logger.info(reply.getResponseData().toXmlString())
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
    }
}