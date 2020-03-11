package MczTrackOut

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.TrackOutWIPMainRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl

@CompileStatic
@Deo(description='''
Perform MES track out
''')
class MczTrackOut_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="EquipmentId")
    private String equipmentId
    
    @DeoBinding(id="LotId")
    private String lotId
    
    @DeoBinding(id="TrackOutQty")
    private String trackOutQty
    
    @DeoBinding(id="RemainInEquipment")
    private String remainInEquipment
  
    @DeoBinding(id="RemainInEquipmentIfPossible")
    private String remainInEquipmentIfPossible
    
    @DeoBinding(id="CCamstarServiceImpl")
    private CCamstarServiceImpl cCamstarService
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {       
        def request = new TrackOutWIPMainRequest()
        request.getInputData().setEquipment(equipmentId)
        def  con = request.getInputData().getContainers().addContainersItem()
        con.setName(lotId)
        request.getInputData().setProcessType("NORMAL")
        request.getInputData().setWIPFlag("2")
        request.getInputData().setTrackOutQty(trackOutQty)
        request.getInputData().setRemainInEquipment(remainInEquipment)
        request.getInputData().setRemainInEquipmentIfPossible(remainInEquipmentIfPossible)
        def reply = cCamstarService.trackOut(request)
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