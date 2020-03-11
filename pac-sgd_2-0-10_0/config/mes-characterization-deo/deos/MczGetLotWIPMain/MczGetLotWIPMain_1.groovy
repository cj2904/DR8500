package MczGetLotWIPMain

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.GetLotWIPMainRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl

@CompileStatic
@Deo(description='''
Issue command with GetLotWipMain for specific lot
''')
class MczGetLotWIPMain_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarServiceImpl")
    private CCamstarServiceImpl cCamstarService

    @DeoBinding(id="LotId")
    private String lotId
  
    @DeoBinding(id="EquipmentId")
    private String equipmentId
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def wipMainRequest = new GetLotWIPMainRequest(lotId, equipmentId)

        def wipMainReply = cCamstarService.getLotWIPMain(wipMainRequest)

        if (wipMainReply.isSuccessful())
        {
            logger.info(wipMainReply.getResponseData().toXmlString())
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(wipMainReply)
        }
    }
}