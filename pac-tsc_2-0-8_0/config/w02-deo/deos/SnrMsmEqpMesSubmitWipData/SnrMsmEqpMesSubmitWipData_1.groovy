package SnrMsmEqpMesSubmitWipData

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.trigger.DeoEventDispatcher
import de.znt.services.secs.SecsGemService

@CompileStatic
@Deo(description='''
Equipment Mes submit wip data
''')
class SnrMsmEqpMesSubmitWipData_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="Equipment")
    private CEquipment equipment
    
    @DeoBinding(id="ParamMap")
    private Map<String, String> paramMap
    
    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataDomainObjectManager
    
    @DeoBinding(id="DeoEventDispatcher")
    private DeoEventDispatcher deoEventDispatcher
    
    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService
    
    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService
    
    /**
     *
     */
    @DeoExecute(result="ReplyMessage")
    public String execute()
    {
        def wipDataSetupName = paramMap.get("ServiceType")
        if (wipDataSetupName == null || wipDataSetupName.length()==0)
        {
            throw new Exception("Missing param 'ServiceType'!")
        }
        
        def wipDataSetupDo = wipDataDomainObjectManager.getWipDataSet(wipDataSetupName)
        if (wipDataSetupDo == null)
        {
            throw new Exception("Wip data setup '" + wipDataSetupName + "' is not found!")
        }
        
        /**
        def message = ""
        def itemList = wipDataSetupDo.getWipDataItemList(WipDataDomainObject.SERVICE_TYPE_ADHOC_WIP_DATA);
        if (true) //TODO: here hand;e adhoc wipdata
        {
            def result = new PMMeasurementScenario().mesSubmitAdhocWipData(deoEventDispatcher, equipment, cCamstarService, wipDataSetupDo)
            message = result.getMessage()            
            def request = new S2F41HostCommandSend(new SecsAsciiItem("CompleteLot"))
            def param = request.getData().getParameterList().addParameter()
            param.setCPName(new SecsAsciiItem("LotId"))
            param.setCPValue(new SecsAsciiItem(wipDataSetupDo.getLotId()))
            
            def reply = secsGemService.sendS2F41HostCommandSend(request)
            if(reply.isCommandAccepted())
            {
                //OK
            }
        }
        else
        {
            def request = new SetWIPDataRequest()
            request.getInputData().setContainer(wipDataSetupDo.getLotId())
            request.getInputData().setEquipment(equipment.getSystemId())
            request.getInputData().setServiceName(WipDataDomainObject.SERVICE_TYPE_TRACK_OUT_WIP_DATA)
            request.getInputData().setProcessType("NORMAL")
            
            def wipEntrySet = wipDataSetupDo.getDataCollection().entrySet()
            for (entry in wipEntrySet)
            {
                def detailItem = request.getInputData().getDetails().addDetailsItem()
                detailItem.setWIPDataName(entry.getKey())
                detailItem.setWIPDataValue(entry.getValue())
            }
            
            def reply = cCamstarService.setWIPData(request)
            if(reply.isSuccessful())
            {
                message = reply.getResponseData().getCompletionMsg()
                wipDataSetupDo.setCompletionMsg(message)
            }
            else
            {
                throw new Exception(reply.getExceptionData().getErrorDescription())
            }
        }
        return message
         **/
        return ""
    }
}