package MczGetLotCarriers

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.GetLotCarriersRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl

@CompileStatic
@Deo(description='''
Get lot's carriers
''')
class MczGetLotCarriers_1 {


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
        def request = new GetLotCarriersRequest(lotId)
        def reply = camstarService.getLotCarriers(request)
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