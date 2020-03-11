package MczGetEquipmentMaint

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl

@CompileStatic
@Deo(description='''
Test the get equipment maintenance call
''')
class MczGetEquipmentMaint_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="EquipmentId")
    private String equipmentId
    
    @DeoBinding(id="CCamstarServiceImpl")
    private CCamstarServiceImpl cCamstarService
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new GetEquipmentMaintRequest(equipmentId)
        def reply = cCamstarService.getEquipmentMaint(request)
        if (reply.isSuccessful())
        {
			def items = reply.getResponseData().getObjectChanges().getEqpProcessCapability().getEqpProcessCapabilityItems()
			while(items.hasNext())
			{
				def item = items.next()
				logger.info(item.getProcessCapability().getName() + " status:" + item.getActivationStatus())
			}
            logger.info(reply.getResponseData().toXmlString())
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
    }
}