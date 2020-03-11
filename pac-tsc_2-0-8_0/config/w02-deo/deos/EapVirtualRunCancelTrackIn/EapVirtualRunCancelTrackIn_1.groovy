package EapVirtualRunCancelTrackIn

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.TrackOutWIPMainWafersRequest
import sg.znt.camstar.semisuite.service.dto.TrackOutWIPMainWafersRequestDto.InputData.Containers.ContainersItem
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.CCamstarService.WIPFlag
import sg.znt.services.camstar.outbound.W02TrackInLotCompleteRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''

''')
class EapVirtualRunCancelTrackIn_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="InputXml")
    private String inputXml

    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new W02TrackInLotCompleteRequest(inputXml)
        def lotId = request.getContainerName()
        def eqpId = request.getResourceName()
        //int trackInWaferQty = request.getLotTrackInWaferList().size()
        int trackInWaferQty = Integer.parseInt(request.getWaferQty())

        int realRunQty = -1
        def wipDataList = request.getWipDataItemList()
        if (wipDataList.size()>0)
        {
            for (var in wipDataList)
            {
                if (var.WIP_DATA_NAME.contains("Real Run Qty"))
                {
                    realRunQty = PacUtils.valueOfInteger(var.WIP_DATA_VALUE, -1)
                }
            }
        }

        logger.info("trackInWaferQty " + trackInWaferQty)
        logger.info("realRunQty " + realRunQty)

        if (realRunQty > -1 && trackInWaferQty>realRunQty)
        {
            def request2 = new TrackOutWIPMainWafersRequest()
            request2.getInputData().setEquipment(eqpId)
            ContainersItem  con = request2.getInputData().getContainers().addContainersItem()
            con.setName(lotId)
            request2.getInputData().setProcessType("NORMAL")
            WIPFlag f = WIPFlag.TRACKOUT
            request2.getInputData().setWIPFlag(f.getValue())
            request2.getInputData().setTrackOutQty(String.valueOf(0))
            request2.getInputData().setRemainInEquipment("false")
            request2.getInputData().setRemainInEquipmentIfPossible("false")
            def reply = cCamstarService.trackOutWafers(request2)
            if(reply.isSuccessful())
            {
                logger.info(reply.getResponseData().getCompletionMsg())
                //track in remain qty to virtual equipment, to
                //update lot attribute to true
                //track in wip data qty with random wafer to real equipment
            }
            else
            {
                CamstarMesUtil.handleNoChangeError(reply)
            }
        }
    }
}