package MczViewContainerStatus

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.camstar.semisuite.service.dto.ViewContainerStatusRequest
import de.znt.pac.deo.annotations.*
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl

@CompileStatic
@Deo(description='''
View container lot status
''')
class MczViewContainerStatus_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="LotId")
    private String lotId
    
    @DeoBinding(id="CamstarService")
    private CCamstarServiceImpl camstarService
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new ViewContainerStatusRequest(lotId)
        def reply = camstarService.viewContainerStatus(request)
        if(reply.isSuccessful())
        {
            logger.info(reply.getResponseData().toXmlString())
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
    }
}