package _TestMesSubmitAdHocWipData

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import de.znt.camstar.semisuite.service.dto.AdhocWIPDataRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Test mes submit adhoc wip data
''')
class _TestMesSubmitAdHocWipData_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new AdhocWIPDataRequest("Tool_Spc_Adhoc", "Equipment", "S31575")
        //request.getInputData().initNameChildParameter("Resource").setValue(equipment.getSystemId())
        
        def detailItem = request.getInputData().getDetails().addDetailsItem()
        detailItem.setWIPDataName("Exhaust")
        detailItem.setWIPDataValue("20")
    
        detailItem = request.getInputData().getDetails().addDetailsItem()
        detailItem.setWIPDataName("Blade Life")
        detailItem.setWIPDataValue("88")
        
        detailItem = request.getInputData().getDetails().addDetailsItem()
        detailItem.setWIPDataName("5S Vision Yield")
        detailItem.setWIPDataValue("99")
        
        def reply = cCamstarService.sendAdhocWipData(request)
        if (reply.isSuccessful())
        {
            logger.info(reply.getResponseData().getCompletionMsg())
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
    }
}