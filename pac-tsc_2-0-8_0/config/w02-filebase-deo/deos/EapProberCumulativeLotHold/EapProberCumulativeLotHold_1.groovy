package EapProberCumulativeLotHold

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.TscConfig
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import de.znt.camstar.semisuite.service.dto.LotHoldRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Send Hold Lot to Camstar for file base prober.
''')
class EapProberCumulativeLotHold_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
    
    @DeoBinding(id="Reason")
    private String reason
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        boolean proceedHold = false
        try
        {
            def holdReason = TscConfig.getProberCumulativeFailureHoldReason()
            def lotList = cMaterialManager.getCLotList(new LotFilterAll())
            def lotHold

            if(lotList.size()>0)
            {
                lotHold = lotList.get(0).getId()
                logger.info("Lot ID to hold: '$lotHold'")
                proceedHold = true
            }
            else
            {
                logger.error("Lot hold is triggered but no Lot ID is found.")
                proceedHold = false
            }

            if(proceedHold==true)
            {
                def request = new LotHoldRequest(lotHold, holdReason)
                request.getInputData().setComments("Auto hold for lot: '$lotHold' due to reason: '$reason'.")
                def reply = cCamstarService.lotHold(request)

                if(reply.isSuccessful())
                {
                    logger.info(reply.getResponseData().getCompletionMsg())
                }
                else
                {
                    CamstarMesUtil.handleNoChangeError(reply)
                }
            }
        }catch(Exception ex)
        {
            ex.printStackTrace()
        }
    }
}