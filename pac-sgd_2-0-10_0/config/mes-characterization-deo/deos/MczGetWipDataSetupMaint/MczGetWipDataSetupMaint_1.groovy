package MczGetWipDataSetupMaint

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.GetWipDataSetupMaintDetailsRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Get Wip Data Setup Maintenance
''')
class MczGetWipDataSetupMaint_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="WipDataSetupName")
    private String wipDataSetupName
    
    @DeoBinding(id="CCamstarServiceImpl")
    private CCamstarServiceImpl camstarService
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def wipDataSetupMaintDetailsRequest = new GetWipDataSetupMaintDetailsRequest()
        wipDataSetupMaintDetailsRequest.getInputData().setObjectToChange(wipDataSetupName)
        def wipDataSetupMaintDetailsResponse = camstarService.getWipDataSetupMaintDetails(wipDataSetupMaintDetailsRequest)
        if (wipDataSetupMaintDetailsResponse.isSuccessful())
        {
            logger.info(wipDataSetupMaintDetailsResponse.getResponseData().toXmlString())
            def items = wipDataSetupMaintDetailsResponse.getResponseData().getObjectChanges().getDetails().getDetailsItems()
            while (items.hasNext())
            {
                def name = items.next().getWIPDataName().getName()
                logger.info("Wip data name = " + name)
            }
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(wipDataSetupMaintDetailsResponse)
        }
    }
}