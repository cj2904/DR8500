package MczGetWIPDataSetupMatrixMaint

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.GetWIPDataSetupMatrixMaintDetailsRequest
import sg.znt.camstar.semisuite.service.dto.GetWIPDataSetupMatrixMaintRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Get WIP Data Setup Matrix Maintenance
''')
class MczGetWIPDataSetupMatrixMaint_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarServiceImpl")
    private CCamstarServiceImpl camstarService
    
    @DeoBinding(id="SpecName")
    private String specName
    
    @DeoBinding(id="EquipmentId")
    private String equipmentId
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def wipDataSetupRequest = new GetWIPDataSetupMatrixMaintRequest()
        def objectChanges = wipDataSetupRequest.getInputData().getObjectChanges()
        objectChanges.getSsSpec().setName(specName)
        objectChanges.getSsSpec().setUseROR("true")
        objectChanges.setSsEquipment(equipmentId)
        def wipDataSetupResponse = camstarService.getWIPDataSetupMatrixMaint(wipDataSetupRequest)
        if (wipDataSetupResponse.isSuccessful())
        {
            logger.info(wipDataSetupResponse.getResponseData().toXmlString())
            def iterator = wipDataSetupResponse.getAllWIPDataSetupMatrixMaint()
            while (iterator.hasNext())
            {
                def headerName = iterator.next().getName()
                def requestDetails = new GetWIPDataSetupMatrixMaintDetailsRequest()
                requestDetails.getInputData().getObjectToChange().setName(headerName)
                def detailsReply = camstarService.getWIPDataSetupMatrixMaintDetails(requestDetails)
                logger.info(detailsReply.getResponseData().toXmlString())
                if (wipDataSetupResponse.isSuccessful())
                {
                    def replyObjectChanges = detailsReply.getResponseData().getObjectChanges()
                    def wIPDataDetails = replyObjectChanges.getSsWIPDataDetails().getSsWIPDataDetailsItems()
                    def name = replyObjectChanges.getSsWIPDataSetup().getName()
                    logger.info("Wip Data Name = " + name)
                    while (wIPDataDetails.hasNext()) 
                    {
                        def item = wIPDataDetails.next()
                        logger.info("Wip Data Item Name = " + item.getWIPDataName().getName() + "|" + item.getServiceName())
                        //the wip data item is not update if there is any change on the wip data setup, need to trigger the save again in wip data matrix to refresh
                    }
                }
                else
                {
                    CamstarMesUtil.handleNoChangeError(detailsReply)
                }
                break
            }
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(wipDataSetupResponse)
        }
    }
}