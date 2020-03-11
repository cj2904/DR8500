package MczGetLotRejectsInProcess

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.GetLotRejectsInProcessRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl

@CompileStatic
@Deo(description='''
Get lot rejected data list from MES
''')
class MczGetLotRejectsInProcess_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="EquipmentId")
    private String equipmentId
    
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
        def request = new GetLotRejectsInProcessRequest()
        request.getInputData().setContainer(lotId)
        request.getInputData().setEquipment(equipmentId)
        request.getInputData().setProcessType("NORMAL")

        def reply = camstarService.getLotRejectsInProcess(request)
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