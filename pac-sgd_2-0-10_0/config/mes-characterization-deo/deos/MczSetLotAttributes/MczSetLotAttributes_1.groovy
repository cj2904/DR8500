package MczSetLotAttributes

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.ModifyLotAttributesRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl

@CompileStatic
@Deo(description='''
Set lot attribute value
''')
class MczSetLotAttributes_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CamstarService")
    private CCamstarServiceImpl camstarService
    
    @DeoBinding(id="LotId")
    private String lotId
    
    @DeoBinding(id="AttributeName")
    private String attributeName
    
    @DeoBinding(id="AttributeValue")
    private String attributeValue
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def attributePairValues = new HashMap<String, String>()
        attributePairValues.put(attributeName, attributeValue)
        
        def attributeRequest = new ModifyLotAttributesRequest(false, lotId, attributePairValues)        
        def reply = camstarService.setLotAttributes(attributeRequest)
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