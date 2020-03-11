package EapSubmitWDRunCountByPM

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.SetWIPDataRequest
import sg.znt.pac.TscConfig
import sg.znt.pac.domainobject.WipDataDomainObject
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest.PM
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Submit PM last usage to wip data
''')
class EapSubmitWDRunCountByPM_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="inputXml")
    private String inputXml

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new W02CompleteOutLotRequest(inputXml)
        def pm = request.getUsedPM()
        if (pm == null)
        {
            throw new Exception("Usage PM is not defined!")
        }        
        
        request.getContainerName()
        def lotList = request.getLotList()
        if (lotList.size() == 0)
        {
            def lotId = request.getContainerName()
            submitRunCount(pm, request, lotId)
        }
        else
        {
            for (lotId in lotList) {
                submitRunCount(pm, request, lotId)
            }
        }
    }
    
    private void submitRunCount(PM pm, W02CompleteOutLotRequest request, String lotId)
    {        
        String pmUsageCount = pm.PM_TOTAL_USAGE_COUNT
		String pmLastUsageCount = pm.PM_LAST_USAGE_COUNT
		
		def finalUsageCount = PacUtils.valueOfFloat(pmUsageCount, 0) - PacUtils.valueOfFloat(pmLastUsageCount, 0)
		logger.info("Final Usage Count is '$finalUsageCount'")
		
		
        def cLot = cMaterialManager.getCLot(lotId)
        def moveOutWipData = cLot.getWipDataByEquipment(request.getResourceName()).getMoveOutWipDataItems()
        def runWipDataName = TscConfig.getStringProperty("UsagePM.Run.WipDataName", "Run")
        for (wd in moveOutWipData)
        {
            if (wd.getId().equalsIgnoreCase(runWipDataName))
            {
                int runCount = (int)finalUsageCount
                logger.info("Found run wip data, filling with run usage count " + finalUsageCount + "|" + runCount)
                
                def wipDataRequest = new SetWIPDataRequest()
                wipDataRequest.getInputData().setContainer(cLot.getId())
                wipDataRequest.getInputData().setEquipment(request.getResourceName())
                wipDataRequest.getInputData().setServiceName(WipDataDomainObject.SERVICE_TYPE_MOVE_OUT_WIP_DATA)
                wipDataRequest.getInputData().setProcessType("NORMAL")
                
                def detailItem = wipDataRequest.getInputData().getDetails().addDetailsItem()
                detailItem.setWIPDataName(runWipDataName)
                detailItem.setWIPDataValue(runCount+"")
                
                def reply = cCamstarService.setWIPData(wipDataRequest)
                if(reply.isSuccessful())
                {
                    def message = reply.getResponseData().getCompletionMsg()
                    logger.info(message)
                }
                else
                {
                    CamstarMesUtil.handleNoChangeError(reply)
                }
                break
            }
        }
    }
}