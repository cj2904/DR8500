package MczGetMaintenanceStatuses

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.GetMaintenanceStatusesRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Get maintenance statuses of equipment
''')
class MczGetMaintenanceStatuses_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarServiceImpl")
    private CCamstarServiceImpl cCamstarService
       
    @DeoBinding(id="EquipmentId")
    private String equipmentId
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new GetMaintenanceStatusesRequest()
        request.getInputData().setResource(equipmentId)
        def reply = cCamstarService.getMaintenanceStatuses(request)
        if(reply.isSuccessful())
        {
            logger.info(reply.getResponseData().toXmlString())
        }
        CamstarMesUtil.handleNoChangeError(reply)
    }
}