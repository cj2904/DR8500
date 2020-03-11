package MczGetRecipeMatrixMaint

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.GetRecipeMatrixMaintDetailsRequest
import sg.znt.camstar.semisuite.service.dto.GetRecipeMatrixMaintRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Query Recipe Matrix Maintenance
''')
class MczGetRecipeMatrixMaint_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="SpecName")
    private String specName
    
    @DeoBinding(id="EquipmentId")
    private String equipmentId
    
    @DeoBinding(id="CCamstarServiceImpl")
    private CCamstarServiceImpl camstarService
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new GetRecipeMatrixMaintRequest()
        def objectChanges = request.getInputData().getObjectChanges()
        objectChanges.getSpec().setName(specName)
        objectChanges.getSpec().setUseROR("true")
        objectChanges.getEquipment().setName(equipmentId)
        def reply = camstarService.getRecipeMatrixMaint(request)
        if (reply.isSuccessful())
        {
            logger.info(reply.getResponseData().toXmlString())            
            def iterator = reply.getAllRecipeMatrixMaintRecord()
            while (iterator.hasNext())
            {
                def row = iterator.next().getRow()
                def requestDetails = new GetRecipeMatrixMaintDetailsRequest()
                requestDetails.getInputData().getObjectToChange().setName(row.getName())
                def detailsReply = camstarService.getRecipeMatrixMaintDetails(requestDetails)
                def name = detailsReply.getResponseData().getObjectChanges().getRecipe().getName()
                logger.info("Recipe name = " + name)
            }
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
    }
}