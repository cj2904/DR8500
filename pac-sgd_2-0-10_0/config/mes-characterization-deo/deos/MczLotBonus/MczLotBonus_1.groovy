package MczLotBonus

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.LotBonusRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl


@CompileStatic
@Deo(description='''
Submit Lot bonus to MES
''')
class MczLotBonus_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="LotId")
    private String lotId
    
    @DeoBinding(id="EquipmentId")
    private String equipmentId
   
    @DeoBinding(id="BonusReason")
    private String bonusReason
    
    @DeoBinding(id="BonusQty")
    private String bonusQty
    
    @DeoBinding(id="CamstarService")
    private CCamstarServiceImpl camstarService
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new LotBonusRequest(lotId)
        request.getInputData().getEquipment().setName(equipmentId)
        request.getInputData().getProcessType().setName("NORMAL")
        def detailItem = request.getInputData().getDetails().addDetailsItem()
        detailItem.setInProcessQty(bonusQty)
        detailItem.getBonusReason().setName(bonusReason)
        def reply = camstarService.lotBonus(request)
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