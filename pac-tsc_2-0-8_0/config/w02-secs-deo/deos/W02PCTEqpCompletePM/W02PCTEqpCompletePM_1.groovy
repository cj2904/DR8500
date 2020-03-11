package W02PCTEqpCompletePM

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
import sg.znt.camstar.semisuite.service.dto.CompleteMaintenanceRequest
import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintRequest
import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintResponse
import sg.znt.camstar.semisuite.service.dto.GetMaintenanceStatusesRequest
import sg.znt.camstar.semisuite.service.dto.SetEquipmentStatusRequest
import sg.znt.camstar.semisuite.service.dto.SetResourceCommentRequest
import sg.znt.pac.date.CDateFormat
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.CCamstarService

@CompileStatic
@Deo(description='''
complete pm for PCT equipment upon track out
''')
class W02PCTEqpCompletePM_1
{

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="SecsControl")
    private SecsControl secsControl

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def vidNameT1 = "TotalWaferCount[Track1]"
        def svidT1 = secsControl.translateSvVid(vidNameT1)
        SecsComponent< ? > svidItemT1 = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(svidT1)))
        def s1f3T1 = new S1F3SelectedEquipmentStatusRequest(svidItemT1)
        def replyS1F3T1 = secsGemService.sendS1F3SelectedEquipmentStatusRequest(s1f3T1)
        def dataT1 = replyS1F3T1.getData().getSV(0).getValueList().get(0)
        def eqpValueT1 = dataT1.toString()
        if (eqpValueT1.length() > 0)
        {
            int counter = PacUtils.valueOfInteger(eqpValueT1)
            if (counter == 0)
            {
                def eqpId = PacConfig.getStringProperty("Track1EqpName", "")
                if (eqpId.length() > 0)
                {
                    eapCompletePM(eqpId)
                }
            }
        }

        def vidNameT2 = "TotalWaferCount[Track2]"
        def svidT2 = secsControl.translateSvVid(vidNameT2)
        SecsComponent< ? > svidItemT2 = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(svidT2)))
        def s1f3T2 = new S1F3SelectedEquipmentStatusRequest(svidItemT2)
        def replyS1F3T2 = secsGemService.sendS1F3SelectedEquipmentStatusRequest(s1f3T2)
        def dataT2 = replyS1F3T2.getData().getSV(0).getValueList().get(0)
        def eqpValueT2 = dataT2.toString()
        if (eqpValueT2.length() > 0)
        {
            int counter = PacUtils.valueOfInteger(eqpValueT2)
            if (counter == 0)
            {
                def eqpId = PacConfig.getStringProperty("Track2EqpName", "")
                if (eqpId.length() > 0)
                {
                    eapCompletePM(eqpId)
                }
            }
        }
    }

    public void eapCompletePM(String systemId)
    {
        GetEquipmentMaintRequest mRequest = new GetEquipmentMaintRequest(systemId)
        mRequest.getRequestData().getObjectChanges().initChildParameter("tscPMReqNameRef")

        GetEquipmentMaintResponse mReply = new GetEquipmentMaintResponse()
        mReply.getResponseData().getObjectChanges().initChildParameter("tscPMReqNameRef")
        mReply = cCamstarService.getEquipmentMaint(mRequest)

        def pmName = ""
        if(mReply.isSuccessful())
        {
            pmName = mReply.getResponseData().getObjectChanges().getChildParameter("tscPMReqNameRef").getValue()
            logger.info("GetEquipmentMaintRequest for tscPMReqNameRef : '$pmName'")
        }
        else
        {
            logger.error("Failed to Get Equipment Maint with error:" + mReply.getExceptionData().getErrorDescription())
        }

        logger.info("EqpId:'$systemId' with PM Name:'$pmName'")

        if(pmName.length() > 0)
        {
            def request = new CompleteMaintenanceRequest()
            request.getInputData().getResource().setName(systemId)
            request.getInputData().setForceMaintenance("TRUE")
            def maintenanceRequest = request.getInputData().getMaintenanceReq()
            maintenanceRequest.setName(pmName)
            maintenanceRequest.setUseROR("true")
            def serviceDetailsItem = request.getInputData().getServiceDetails().addServiceDetailsItem()
            serviceDetailsItem.getMaintenanceStatus().setId(getStatusId(pmName, systemId))

            def reply = cCamstarService.completePMMaint(request)
            if(reply.isSuccessful())
            {
                logger.info(reply.getResponseData().getCompletionMsg())
                addResourceComment(systemId)
                setEqpStatus(systemId, pmName)
            }
            else
            {
                CamstarMesUtil.handleNoChangeError(reply)
            }
        }
    }

    public void setEqpStatus(String eqpId, String pmName)
    {
        def eqpStatus = PacConfig.getStringProperty("EquipmentStatus.WaitProduction", "")
        def request = new SetEquipmentStatusRequest(eqpId, eqpStatus)
        def comment = "Equipment '$eqpId' PM '$pmName' completed, set equipment status to '$eqpStatus'"
        request.getInputData().setComments(comment)

        def reply = cCamstarService.setEquipmentStatus(request)
        if(reply.isSuccessful())
        {
            logger.info(reply.getResponseData().getCompletionMsg())
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
    }

    public String getStatusId(String pmName, String eqpId)
    {
        def maintenanceStatusId = ""
        def request = new GetMaintenanceStatusesRequest()
        request.getInputData().setResource(eqpId)
        def reply = cCamstarService.getMaintenanceStatuses(request)
        if(reply.isSuccessful())
        {
            def items = reply.getAllMaintenanceRecord()
            while(items.hasNext())
            {
                def item = items.next()
                def itemRow = item.getRow()
                def dueTimeStamp = itemRow.getNextDateDue()
                def pastDueTimeStamp = itemRow.getNextDateLimit()
                def completed = itemRow.getCompleted()
                def due = itemRow.getDue()
                def pastDue = itemRow.getPastDue()
                def pmState = itemRow.getMaintenanceState()
                def key = itemRow.getMaintenanceReqName()
                if (key.equalsIgnoreCase(pmName))
                {
                    return itemRow.getMaintenanceStatus()
                }
            }
        }
        return ""
    }

    void addResourceComment(String eqpId)
    {
        def eventTime = CDateFormat.getFormatedDate(new Date())
        def request = new SetResourceCommentRequest(eqpId, "Event ResetWaferCount at equipment $eqpId @ $eventTime")
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