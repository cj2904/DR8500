package _UtilMesTrackIn

import static sg.znt.pac.resources.i18n.TscI18n.localize
import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.TrackInWIPMainWafersRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Perform MES track in from Win API gateway
''')
class _UtilMesTrackIn_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="EqId")
    private String eqId

    @DeoBinding(id="LotId")
    private String lotId
    
    @DeoBinding(id="WaferId")
    private String waferId
    
    /**
     *
     */
    @DeoExecute(result="Message")
    public String execute()
    {
        def request = new TrackInWIPMainWafersRequest(lotId, eqId)
        def waferItem = request.getInputData().getWafersDetails().addWafersDetailsItem()
        waferItem.setContainer(lotId)
        waferItem.setWaferScribeNumber(waferId)
        def response = cCamstarService.trackInWafers(request)
        if (response.isSuccessful())
        {
            logger.info(response.getResponseData().getCompletionMsg())
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(response)
        }    
    }    
}