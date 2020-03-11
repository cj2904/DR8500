package MczSetConsumeMaterials

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.SetConsumeMaterialsRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl

@CompileStatic
@Deo(description='''
Consume material
''')
class MczSetConsumeMaterials_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarServiceImpl")
    private CCamstarServiceImpl cCamstarService
    
    @DeoBinding(id="LotId")
    private String lotId
    
    @DeoBinding(id="MaterialLot")
    private String materialLot
    
    @DeoBinding(id="MaterialPartName")
    private String materialPartName
    
    @DeoBinding(id="QtyToConsume")
    private String qtyToConsume
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new SetConsumeMaterialsRequest()
        request.getInputData().setContainer(lotId)
        def detailItem = request.getInputData().getServiceDetails().addServiceDetailsItem()
        detailItem.setMaterialLotName(materialLot)
        detailItem.getMaterialPart().setName(materialPartName)
        detailItem.getMaterialPart().setUseROR("true")
        detailItem.setQtyToConsume(qtyToConsume + "")
        def reply = cCamstarService.setConsumeMaterials(request)
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