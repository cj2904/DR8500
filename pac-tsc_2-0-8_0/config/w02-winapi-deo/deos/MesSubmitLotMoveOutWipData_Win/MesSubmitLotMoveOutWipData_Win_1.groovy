package MesSubmitLotMoveOutWipData_Win

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.SetWIPDataRequest
import sg.znt.pac.EquipmentIdentifyService
import sg.znt.pac.TscConfig
import sg.znt.pac.domainobject.WipDataDomainObject
import sg.znt.pac.machine.TscEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.TrackOutLotRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Submit wip date during tracked out
''')
class MesSubmitLotMoveOutWipData_Win_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager materialManager
    
    @DeoBinding(id="EquipmentIdentifyService")
    private EquipmentIdentifyService equipmentIdentifyService
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outboundRequest = new TrackOutLotRequest(inputXmlDocument)
        def lotId = outboundRequest.getContainerName()
        def lot = materialManager.getCLot(lotId)
        TscEquipment equipment = (TscEquipment) equipmentIdentifyService.getEquipmentBySystemId(outboundRequest.getResourceName())
        
        def serviceType = WipDataDomainObject.SERVICE_TYPE_MOVE_OUT_WIP_DATA
        def wipDataItems = lot.getWipDataByEquipment(equipment.getSystemId()).getWipDataItems(serviceType)
        def nonAutoItems = TscConfig.getStringProperty("DataCollection.NonAutoItem", "")
        if (wipDataItems != null && wipDataItems.size()>0)
        {
            def wdIt = wipDataItems.iterator()
            while (wdIt.hasNext()) 
            {
                def entry = wdIt.next()
                if (entry.getId().matches(nonAutoItems))
                {
                    logger.info("Removing non auto wip data item '" + entry.getId() + "'" + nonAutoItems + "...")
                    wdIt.remove()
                }
            }
            
            if (wipDataItems.size()>0)
            {
                requestWipData(wipDataItems, equipment, lot)
                def request = new SetWIPDataRequest()
                request.getInputData().setContainer(lot.getId())
                request.getInputData().setEquipment(equipment.getSystemId())
                request.getInputData().setServiceName(serviceType.toString())
                request.getInputData().setProcessType("NORMAL")
                
                boolean gotRecord = false
                for (entry in wipDataItems)
                {
                    if (!entry.getValue().equalsIgnoreCase("NotSupport"))
                    {
                        def detailItem = request.getInputData().getDetails().addDetailsItem()
                        detailItem.setWIPDataName(entry.getId())
                        detailItem.setWIPDataValue(entry.getValue())
                        gotRecord = true
                    }
                    else
                    {
                        logger.info("Skip wip data submission for '" + entry.getId() + "' since it's not configured in api")
                    }
                }
                
                if (gotRecord)
                {
                    def reply = cCamstarService.setWIPData(request)
                    if(reply.isSuccessful())
                    {
                        String message = reply.getResponseData().getCompletionMsg()
                        logger.info(message)
                    }
                    else
                    {
                        throw new Exception(reply.getExceptionData().getErrorDescription())
                    }                    
                }
            }
        }    
    }
    
    void requestWipData(List<WipDataDomainObject> wipDataItems, TscEquipment equipment, CLot cLot)
    {
        List<String> variableList = new ArrayList<String>()
        for (entry in wipDataItems)
        {
            variableList.add(entry.getId())
        }
        List<String> valueList = equipment.getModelScenario().eqpRequestVariableByList(variableList)
        
        if(valueList != null)
        {
            for (int i=0; i<valueList.size();i++)
            {
                def t_wipName = variableList.get(i);
                def t_wipValue = valueList.get(i);
                
                if (t_wipValue.contains(":"))
                {
                    String[] split = t_wipValue.split(":");
                    if (split.length > 1)
                    {
                        t_wipValue = split[1];
                    }
                    else
                    {
                        t_wipValue = "";
                    }
                }
                
                if(t_wipValue.endsWith("NotSupport"))
                {
                    def notSupportWipData = cLot.getPropertyContainer().getString("NotSupport_WIP_Data", "")
                    cLot.getPropertyContainer().setString("NotSupport_WIP_Data", notSupportWipData + "\n" + t_wipValue)
                    continue
                }
                
                for(lotWd in wipDataItems)
                {
                    if(lotWd.getId().equalsIgnoreCase(t_wipName))
                    {
                        lotWd.setValue(t_wipValue)
                    }
                }
            }
        }
    }
}