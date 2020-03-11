package MczGetEquipmentSetup

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.GetEquipmentSetupRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl

@CompileStatic
@Deo(description='''
Get equipment setup for equipment
''')
class MczGetEquipmentSetup_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CamstarService")
    private CCamstarServiceImpl camstarService
    
    @DeoBinding(id="EquipmentId")
    private String equipmentId
    
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new GetEquipmentSetupRequest()
        request.getInputData().setResource(equipmentId)
        
        def reply = camstarService.getEquipmentSetup(request)
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