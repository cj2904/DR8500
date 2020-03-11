package MczGetEquipmentMaterialSetup

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaterialsSetupRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl

@CompileStatic
@Deo(description='''
Get equipment material setup
''')
class MczGetEquipmentMaterialSetup_1 {


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
        def request = new GetEquipmentMaterialsSetupRequest();
        request.getInputData().setResource(equipmentId);

        def reply = camstarService.getEquipmentMaterialSetup(request);
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