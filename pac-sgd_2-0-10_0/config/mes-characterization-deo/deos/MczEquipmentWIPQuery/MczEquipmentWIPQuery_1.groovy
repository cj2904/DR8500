package MczEquipmentWIPQuery

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import sg.znt.camstar.semisuite.service.dto.EquipmentWIPQueryRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl

@CompileStatic
@Deo(description='''
Query the equipment wip
''')
class MczEquipmentWIPQuery_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="EquipmentId")
    private String equipmentId
    
    @DeoBinding(id="CamstarService")
    private CCamstarServiceImpl camstarService
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new EquipmentWIPQueryRequest(equipmentId)
        def reply = camstarService.equipmentWIPQuery(request)
        if(reply.isSuccessful())
        {
            logger.info(Arrays.toString(reply.getLotIdList().toArray()))
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
    }
}