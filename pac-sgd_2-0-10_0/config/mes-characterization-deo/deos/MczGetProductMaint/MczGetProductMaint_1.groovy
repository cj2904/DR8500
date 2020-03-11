package MczGetProductMaint

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.GetProductMaintRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl


@CompileStatic
@Deo(description='''
Get PN information
''')
class MczGetProductMaint_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CamstarService")
    private CCamstarServiceImpl camstarService
    
    @DeoBinding(id="Product")
    private String product
    
    @DeoBinding(id="ProductRevision")
    private String productRevision
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new GetProductMaintRequest()
        request.getInputData().getObjectToChange().setName(product)
        request.getInputData().getObjectToChange().setRev(productRevision)
        def reply = camstarService.getProductMaint(request)
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