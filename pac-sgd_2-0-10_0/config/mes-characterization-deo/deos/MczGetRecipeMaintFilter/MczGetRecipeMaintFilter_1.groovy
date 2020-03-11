package MczGetRecipeMaintFilter

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.GetRecipeMaintFilterRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl

@CompileStatic
@Deo(description='''
Get MES Recipe filter list based on filter string
''')
class MczGetRecipeMaintFilter_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="Recipe")
    private String recipe
    
    @DeoBinding(id="CCamstarServiceImpl")
    private CCamstarServiceImpl cCamstarService
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        GetRecipeMaintFilterRequest request = new GetRecipeMaintFilterRequest(recipe)
        def reply = cCamstarService.getRecipeMaintFilter(request)
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