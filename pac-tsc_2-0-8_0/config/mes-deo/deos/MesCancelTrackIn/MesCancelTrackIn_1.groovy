package MesCancelTrackIn

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.TrackOutWIPMainRequest
import sg.znt.camstar.semisuite.service.dto.TrackOutWIPMainRequestDto.InputData.Containers.ContainersItem
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.CCamstarService.WIPFlag
import sg.znt.services.camstar.outbound.TrackInLotRequest
import de.znt.pac.deo.annotations.*

@Deo(description='''
Cancel MES track in
''')
class MesCancelTrackIn_1
{

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
     
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
    
    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        TrackInLotRequest outboundRequest = new TrackInLotRequest(inputXmlDocument)
        def lot = cMaterialManager.getCLot(outboundRequest.getContainerName())
        
        def request = new TrackOutWIPMainRequest()
        request.getInputData().setEquipment(mainEquipment.getSystemId())
        ContainersItem  con = request.getInputData().getContainers().addContainersItem()
        con.setName(lot.getId())
        request.getInputData().setProcessType("NORMAL")
        WIPFlag f = WIPFlag.TRACKOUT
        request.getInputData().setWIPFlag(f.getValue())
        request.getInputData().setTrackOutQty(String.valueOf(0))
        request.getInputData().setRemainInEquipment("false")
        request.getInputData().setRemainInEquipmentIfPossible("false")
        def reply = cCamstarService.trackOut(request)
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