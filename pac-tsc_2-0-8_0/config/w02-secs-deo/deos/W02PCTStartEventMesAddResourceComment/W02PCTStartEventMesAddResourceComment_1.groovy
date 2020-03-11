package W02PCTStartEventMesAddResourceComment

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsControl
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S1F3SelectedEquipmentStatusRequest
import de.znt.zsecs.composite.SecsComponent
import de.znt.zsecs.composite.SecsDataItem
import de.znt.zsecs.composite.SecsDataItem.ItemName
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.SetResourceCommentRequest
import sg.znt.pac.date.CDateFormat
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService

@CompileStatic
@Deo(description='''
add comment for start event
''')
class W02PCTStartEventMesAddResourceComment_1 {

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="SecsControl")
    private SecsControl secsControl
    
    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService
    
    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {        
        def vidName = "LotId"
        def svidLotId = secsControl.translateSvVid(vidName)
        SecsComponent< ? > svidItemLotId = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(svidLotId)))
        def s1f3LotId = new S1F3SelectedEquipmentStatusRequest(svidItemLotId)
        def replyS1F3LotId = secsGemService.sendS1F3SelectedEquipmentStatusRequest(s1f3LotId)
        def data = replyS1F3LotId.getData().getSV(0).getValueList().get(0)
        def lotId = data.toString()
        
        vidName = "Track"
        def svidTrack = secsControl.translateSvVid(vidName)
        SecsComponent< ? > svidItemTrack = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(svidTrack)))
        def s1f3Track = new S1F3SelectedEquipmentStatusRequest(svidItemTrack)
        def replyS1F3Track = secsGemService.sendS1F3SelectedEquipmentStatusRequest(s1f3Track)
        data = replyS1F3Track.getData().getSV(0).getValueList().get(0)
        def track = data.toString()
        
//        vidName = "PPID"
//        def svidPpid = secsControl.translateSvVid(vidName)
//        SecsComponent< ? > svidItemPpid = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(svidPpid)))
//        def s1f3Ppid = new S1F3SelectedEquipmentStatusRequest(svidItemPpid)
//        def replyS1F3Ppid = secsGemService.sendS1F3SelectedEquipmentStatusRequest(s1f3Ppid)
//        data = replyS1F3Ppid.getData().getSV(0).getValueList().get(0)
//        def ppid = data.toString()
        
        def systemId = PacConfig.getStringProperty("Track" + track + "EqpName", "")
        
        def eventTime = CDateFormat.getFormatedDate(new Date())
        def request = new SetResourceCommentRequest(systemId, "Event LotStart at equipment $systemId for Lot ID '$lotId' @ $eventTime")
        def reply = cCamstarService.setResourceComment(request)
        if(reply.isSuccessful()) {
            logger.info(reply.getResponseData().getCompletionMsg())
        }
        else {
            CamstarMesUtil.handleNoChangeError(reply)
        }
        
    }
}