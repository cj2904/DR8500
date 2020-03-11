package MczSetEquipmentStatus

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.SetEquipmentStatusRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl

@CompileStatic
@Deo(description='''
Set equipment status
''')
class MczSetEquipmentStatus_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CamstarService")
    private CCamstarServiceImpl camstarService
    
    @DeoBinding(id="EquipmentId")
    private String equipmentId
    
    @DeoBinding(id="StatusCode")
    private String statusCode
    
    @DeoBinding(id="StatusReason")
    private String statusReason
    
    @DeoBinding(id="Comments")
    private String comments
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new SetEquipmentStatusRequest(equipmentId, statusCode, statusReason)
        request.getInputData().setComments(comments)
        def reply = camstarService.setEquipmentStatus(request)
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