package _UtilMesCancelTrackInLot

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import de.znt.camstar.semisuite.service.dto.TrackOutRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''

''')
class _UtilMesCancelTrackInLot_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="LotId")
    private String lotId

    @DeoBinding(id="EqId")
    private String eqId

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def request = new TrackOutRequest(eqId, "NORMAL", lotId)
		request.getInputData().setTrackOutQty("0")
		request.setServiceType("WaferWIPMain")
		def reply = cCamstarService.trackOut(request)
        if (reply.isSuccessful())
        {
            logger.info(reply.getResponseData().toXmlString())
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
    }
}