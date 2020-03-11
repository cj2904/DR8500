package MczSetLotRejectsInProcess

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.LotRejectsInProcessRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl

@CompileStatic
@Deo(description='''
Set Camstar Lot Reject
''')
class MczSetLotRejectsInProcess_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="EquipmentId")
    private String equipmentId
    
    @DeoBinding(id="LotId")
    private String lotId
    
    @DeoBinding(id="RejectCode")
    private String rejectCode
    
    @DeoBinding(id="Comment")
    private String comment
    
    @DeoBinding(id="RejectQty")
    private String rejectQty
    
    /**
    @DeoBinding(id="WaferId")
    private String waferId
    **/
    
    @DeoBinding(id="CamstarService")
    private CCamstarServiceImpl camstarService
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new LotRejectsInProcessRequest()
        request.getInputData().setContainer(lotId)
        request.getInputData().setEquipment(equipmentId)
        request.getInputData().setProcessType("NORMAL")
        
        def di = request.getInputData().getDetails().addDetailsItem()
        di.setLossReason(rejectCode)
        di.setRejectComment(comment)
        /**
        if(waferId != null && waferId.length()>0)
        {
            di.setWaferRejectsQty(rejectQty)
            di.setWaferScribeNumber(waferId)
            di.setWaferRejectsType("WHOLE")
        }
        else
        {
            di.setRejectQty(rejectQty)
        }
        **/
        di.setRejectQty(rejectQty)
        def reply = camstarService.lotRejectsInProcess(request)
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