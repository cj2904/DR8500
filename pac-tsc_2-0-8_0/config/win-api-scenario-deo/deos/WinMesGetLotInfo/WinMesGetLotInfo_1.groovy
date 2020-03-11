package WinMesGetLotInfo

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.GetLotWIPMainRequest
import sg.znt.pac.machine.CEquipment
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.zwin.ZWinApiService
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import elemental.json.JsonObject

@CompileStatic
@Deo(description='''
Get lot info for win api
''')
class WinMesGetLotInfo_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="ParamMap")
    private Map<String, String> paramMap

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment
    
    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService
    
    @DeoBinding(id="EventId")
    private String eventTid
    
    @DeoBinding(id="EventName")
    private String eventName
    
    /**
     *
     */
    @DeoExecute(result="Reply")
    public JsonObject execute()
    {
        def lotId = paramMap.get("LotId")
        if (lotId == null || lotId.length()==0)
        {
            throw new Exception("Missing param 'LotId'!")
        }
        
        def waferList = ""
        def equipmentList = cEquipment.getSystemId()
        def request = new GetLotWIPMainRequest()
        def inputData = request.getInputData()
        def requestData = request.getRequestData()
        
        inputData.setContainer(lotId)
        inputData.removeParameter("Equipment")
        inputData.removeParameter("SelectionId")
        inputData.setProcessType(CCamstarService.DEFAULT_PROCESS_TYPE);
        inputData.setWIPFlag(CCamstarService.WIPFlag.TRACKIN.getValue())
        def item = inputData.getContainers().addContainersItem()
        item.setName(lotId)
        request.removeChildNode("__perform")
        request.removeChildNode("__execute")
        requestData.removeParameter("RequiredActivities")
        
        def wipMainReply = cCamstarService.getLotWIPMain(request)

        //if (wipMainReply.isSuccessful())
        if (true)
        {
            def eqpIterator = wipMainReply.getResponseData().getEquipmentSelection().getEquipmentSelectionItems()
            def validEqp = false
            while (eqpIterator.hasNext()) 
            {
                if (eqpIterator.next().getName().equalsIgnoreCase(cEquipment.getSystemId()))
                {
                    validEqp = true
                    break
                }
            }
            if (!validEqp)
            {
                throw new Exception("Lot '" + lotId + "' is not allowed to run at equipment '" + cEquipment.getSystemId() + "'")
            }
            def iterator = wipMainReply.getResponseData().getWafersDetailsSelectionAll().getWafersDetailsSelectionAllItems()
            while (iterator.hasNext()) 
            {
                def wafer = iterator.next()
                if (waferList.length()>0)
                {
                    waferList = waferList + ","
                }
                waferList = waferList + wafer.getWaferScribeNumber()
            }
        }
        else
        {
            //CamstarMesUtil.handleNoChangeError(wipMainReply)
        }
        
        ZWinApiService winApiService = ((ZWinApiService)secsGemService)
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("LotId",lotId)
        param.put("WaferList", waferList)
        param.put("EquipmentList", cEquipment.getSystemId())
        
        return winApiService.buildEventReplyMessage(eventTid, eventName, param, "", "");
    }
}