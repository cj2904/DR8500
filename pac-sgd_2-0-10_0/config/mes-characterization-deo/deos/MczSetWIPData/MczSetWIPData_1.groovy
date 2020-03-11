package MczSetWIPData

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import sg.znt.camstar.semisuite.service.dto.SetWIPDataRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl

@CompileStatic
@Deo(description='''
Set wip data for a lot
''')
class MczSetWIPData_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="EquipmentId")
    private String equipmentId
    
    @DeoBinding(id="LotId")
    private String lotId

    @DeoBinding(id="ServiceName")
    private String serviceName

    @DeoBinding(id="WipDataName")
    private String wipDataName
    
    @DeoBinding(id="WipDataValue")
    private String wipDataValue
    
    @DeoBinding(id="CCamstarServiceImpl")
    private CCamstarServiceImpl cCamstarService

    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new SetWIPDataRequest()
        request.getInputData().setContainer(lotId)
        request.getInputData().setEquipment(equipmentId)
        request.getInputData().setServiceName(serviceName)
        request.getInputData().setProcessType("NORMAL")
        
        def detailItem = request.getInputData().getDetails().addDetailsItem()
        detailItem.setWIPDataName(wipDataName)
        detailItem.setWIPDataValue(wipDataValue)
        
        def reply = cCamstarService.setWIPData(request)
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