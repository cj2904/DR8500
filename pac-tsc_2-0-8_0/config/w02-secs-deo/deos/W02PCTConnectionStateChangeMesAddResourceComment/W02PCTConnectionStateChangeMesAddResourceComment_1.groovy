package W02PCTConnectionStateChangeMesAddResourceComment

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
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class W02PCTConnectionStateChangeMesAddResourceComment_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="SecsControl")
    private SecsControl secsControl

    @DeoBinding(id="MainEquipment")
    private CEquipment cEquipment

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def vidNameT1 = "Track1ConnectionStatus"
        def svidT1 = secsControl.translateSvVid(vidNameT1)
        SecsComponent< ? > svidItemT1 = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(svidT1)))
        def s1f3T1 = new S1F3SelectedEquipmentStatusRequest(svidItemT1)
        def replyS1F3T1 = secsGemService.sendS1F3SelectedEquipmentStatusRequest(s1f3T1)
        def dataT1 = replyS1F3T1.getData().getSV(0).getValueList().get(0)
        def t1ConnectionStatus = dataT1.toString()

        def vidNameT2 = "Track2ConnectionStatus"
        def svidT2 = secsControl.translateSvVid(vidNameT2)
        SecsComponent< ? > svidItemT2 = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(svidT2)))
        def s1f3T2 = new S1F3SelectedEquipmentStatusRequest(svidItemT2)
        def replyS1F3T2 = secsGemService.sendS1F3SelectedEquipmentStatusRequest(s1f3T2)
        def dataT2 = replyS1F3T2.getData().getSV(0).getValueList().get(0)
        def t2ConnectionStatus = dataT2.toString()

        def t1LastConnectionStatus = cEquipment.getPropertyContainer().getString("Track1ConnectionStatus", "")
        if (!t1ConnectionStatus.equalsIgnoreCase(t1LastConnectionStatus))
        {
            mesAddResourceComment(PacConfig.getStringProperty("Track1EqpName", ""), t1ConnectionStatus)
            cEquipment.getPropertyContainer().setString("Track1ConnectionStatus", t1ConnectionStatus)
        }

        def t2LastConnectionStatus = cEquipment.getPropertyContainer().getString("Track2ConnectionStatus", "")
        if (!t2ConnectionStatus.equalsIgnoreCase(t2LastConnectionStatus))
        {
            mesAddResourceComment(PacConfig.getStringProperty("Track2EqpName", ""), t2ConnectionStatus)
            cEquipment.getPropertyContainer().setString("Track2ConnectionStatus", t2ConnectionStatus)
        }
    }

    public void mesAddResourceComment(String systemId, String connectionStatus)
    {
        def status = ""
        if (connectionStatus.equalsIgnoreCase("1"))
        {
            status = "ONLINE"
        }
        else if (connectionStatus.equalsIgnoreCase("2"))
        {
            status = "OFFLINE"
        }

        if (status.length() > 0)
        {
            def eventTime = CDateFormat.getFormatedDate(new Date())
            def request = new SetResourceCommentRequest(systemId, "Connection status is changed to '$status' @ $eventTime")
            def reply = cCamstarService.setResourceComment(request)
            if(reply.isSuccessful())
            {
                logger.info(reply.getResponseData().getCompletionMsg())
            }
            else
            {
                CamstarMesUtil.handleNoChangeError(reply)
            }
        }
    }
}