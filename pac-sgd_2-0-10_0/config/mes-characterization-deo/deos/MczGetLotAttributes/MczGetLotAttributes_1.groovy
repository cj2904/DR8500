package MczGetLotAttributes

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.GetLotAttributesRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl

@CompileStatic
@Deo(description='''
Get lot's attribute
''')
class MczGetLotAttributes_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CamstarService")
    private CCamstarServiceImpl camstarService
    
    @DeoBinding(id="LotId")
    private String lotId
    
    @DeoBinding(id="Attribute")
    private String attribute
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
         def request = new GetLotAttributesRequest(lotId, attribute, attribute)        
         def reply = camstarService.getLotAttributes(request);
         if(reply.isSuccessful())
         {
             logger.info(reply.getAttributeValue(attribute))
             logger.info(reply.getResponseData().toXmlString())
         }
         else
         {
             CamstarMesUtil.handleNoChangeError(reply)
         }
    }
}