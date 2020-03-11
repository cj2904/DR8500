package MczTrackIn

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.TrackInWIPMainWafersRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import de.znt.camstar.semisuite.service.dto.TrackInRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Track in a lot / wafer
''')
class MczTrackIn_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="LotId")
    private String lotId

    @DeoBinding(id="WaferId")
    private String waferId

    @DeoBinding(id="EquipmentId")
    private String equipmentId

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        if (waferId != null && waferId.length()>0)
        {
            def request = new TrackInWIPMainWafersRequest(lotId, equipmentId)
            def waferItem = request.getInputData().getWafersDetails().addWafersDetailsItem()
            waferItem.setWaferScribeNumber(waferId)
            def response = cCamstarService.trackInWafers(request)
            if (response.isSuccessful())
            {
                logger.info(response.getResponseData().toXmlString())
            }
            else
            {
                CamstarMesUtil.handleNoChangeError(response)
            }            
        }
        else
        {
            def request = new TrackInRequest(equipmentId, CCamstarService.DEFAULT_PROCESS_TYPE, lotId)
            def response = cCamstarService.trackIn(request)
            if (response.isSuccessful())
            {
                logger.info(response.getResponseData().toXmlString())
            }
            else
            {
                CamstarMesUtil.handleNoChangeError(response)
            }
        }
    }
}