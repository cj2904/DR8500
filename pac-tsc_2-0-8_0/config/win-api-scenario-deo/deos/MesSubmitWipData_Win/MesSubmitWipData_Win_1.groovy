package MesSubmitWipData_Win

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.SetWIPDataRequest
import sg.znt.camstar.semisuite.service.dto.SetWaferWIPDataRequest
import sg.znt.pac.TscConfig
import sg.znt.pac.TscConstants
import sg.znt.pac.domainobject.WipDataDomainObject
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.CWafer
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.material.WaferFilterAll
import sg.znt.pac.material.WaferFilterByScribeIds
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Submit track out WIP data to MES
''')
class MesSubmitWipData_Win_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="ParamMap")
    private Map<String, String> paramMap

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService
    
    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataDomainObjectManager

    @DeoBinding(id="Equipment")
    private CEquipment cEquipment
    
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager materialManager
    
    private String rrvWipDataName = TscConfig.getStringProperty("RRV.WipData.Name", "");
    
    /**
     *
     */
    @DeoExecute(result="reply")
    public String execute()
    {
        def serviceType = paramMap.get("ServiceType")
        if (serviceType == null || serviceType.length()==0)
        {
            throw new Exception("Missing param 'ServiceType'!")
        }
        
        def lotId = paramMap.get("LotId")
        if (lotId == null || lotId.length()==0)
        {
            throw new Exception("Missing param 'LotId'!")
        }
        
        def waferId = paramMap.get("WaferId")
        
        def lot = materialManager.getCLot(lotId)
        def message = ""
        if(waferId != null && waferId.length()>0)
        {
            message = submitWaferWipData(lot, waferId, serviceType)
        }
        else
        {
            message = submitBatchWaferWipData(lot, serviceType)
            if (message == null)
            {
                message = submitLotWipData(lot, serviceType, false)
                def lotList = materialManager.getCLotList(new LotFilterAll())
                for (ll in lotList)
                {
                    if(!ll.getPropertyContainer().getBoolean(TscConstants.MATERIAL_ATTR_FIRST_LOT_IN_BATCH,false))
                    {
                        submitLotWipData(ll, serviceType, true)
                    }
                }
            }            
        }
        
        if (message.length()>0)
        {
            message = message + " "
        }
        message = message + getCalcMsg()
        
        return message
    }
    
    String submitBatchWaferWipData(CLot lot, String serviceType)
    {
        if (TscConfig.useBatchWaferDataCollection())
        {
            def request = new SetWaferWIPDataRequest()
            request.getInputData().setContainer(lot.getId())
            request.getInputData().setEquipment(cEquipment.getSystemId())
            request.getInputData().setServiceName(serviceType.toString())
            request.getInputData().setProcessType("NORMAL")
            
            boolean validData = false
            def waferList = lot.getWaferList(new WaferFilterAll())
            for (wafer in waferList) 
            {
                def wipDataList = wafer.getWipData()
                if(wipDataList!=null)
                {
                    def wipDataItems = wipDataList.getTrackOutWipDataItems()
                    for (entry in wipDataItems)
                    {
                        if (entry.isHidden())
                        {
                            def detailItem = request.getInputData().getDetails().addDetailsItem()
                            detailItem.setWaferScribeNumber(wafer.getWaferScribeID())
                            detailItem.setWIPDataName(entry.getId())
                            detailItem.setWIPDataValue(entry.getValue())
                            validData = true
                        }
                        else
                        {
                            logger.info("Skip wip data submission for '" + entry.getId() + "' since it's not hidden")
                        }
                    }
                }
            }
            if(validData)
            {
                def reply = cCamstarService.setWaferWIPData(request)
                if(reply.isSuccessful())
                {
                    def message = reply.getResponseData().getCompletionMsg()
                    logger.info(message)
                    return message
                }
                else
                {
                    throw new Exception(reply.getExceptionData().getErrorDescription())
                }
            }
        }
        return null
    }
    
    String submitWaferWipData(CLot lot, String waferScribeId, String serviceType)
    {
        CWafer wafer = null
        def waferList = lot.getWaferList(new WaferFilterByScribeIds(waferScribeId))
        if (waferList.size() == 0)
        {
            throw new Exception("Cannot find wafer '" + waferScribeId + "'!")
        }
        else
        {
            wafer = waferList.get(0)
        }
        def wipDataItems = wafer.getWipData().getWipDataItems(serviceType)
        def nonAutoItems = TscConfig.getStringProperty("DataCollection.NonAutoItem", "")
        
        def wdIt = wipDataItems.iterator()
        while (wdIt.hasNext())
        {
            def entry = wdIt.next()
            if (!entry.getId().matches(rrvWipDataName) && entry.getId().matches(nonAutoItems))
            {
                logger.info("Removing non auto wip data item '" + entry.getId() + "|" + nonAutoItems + "'...")
                wdIt.remove()
            }
        }
        
        def request = new SetWaferWIPDataRequest()
        request.getInputData().setContainer(lot.getId())
        request.getInputData().setEquipment(cEquipment.getSystemId())
        request.getInputData().setServiceName(serviceType.toString())
        request.getInputData().setProcessType("NORMAL")
        
        if (wipDataItems.size()>0)
        {
            for (entry in wipDataItems)
            {
                if (entry.isHidden())
                {
                    def detailItem = request.getInputData().getDetails().addDetailsItem()
                    detailItem.setWaferScribeNumber(wafer.getWaferScribeID())
                    detailItem.setWIPDataName(entry.getId())
                    detailItem.setWIPDataValue(entry.getValue())
                }
                else
                {
                    logger.info("Skip wip data submission for '" + entry.getId() + "' since it's not hidden")
                }
            }
            
            def reply = cCamstarService.setWaferWIPData(request)
            if(reply.isSuccessful())
            {
                def message = reply.getResponseData().getCompletionMsg()
                logger.info(message)
                return message
            }
            else
            {
                throw new Exception(reply.getExceptionData().getErrorDescription())
            }
        }
        return ""
    }
    
    String submitLotWipData(CLot lot, String serviceType, boolean isClone)
    {
        List<WipDataDomainObject> wipDataItems = null
        if(lot.getWipDataByEquipment(cEquipment.getSystemId())!=null)
        {
            wipDataItems = lot.getWipDataByEquipment(cEquipment.getSystemId()).getWipDataItems(serviceType)
        }
        else
        {
            logger.error("No WIpDataItems Found. Return.")
            return
        }
        
        def request = new SetWIPDataRequest()
        request.getInputData().setContainer(lot.getId())
        request.getInputData().setEquipment(cEquipment.getSystemId())
        request.getInputData().setServiceName(serviceType.toString())
        request.getInputData().setProcessType("NORMAL")
        
        if (isClone)
        {
            logger.info("Lot '" + lot.getId() + "''s wip data is cloned..")
            request.getInputData().setTscIsSkipSPCCheck("true")
        }
        
        boolean submit = false
        for (entry in wipDataItems)
        {
            def send = true
            def required = entry.isRequired()
            if((entry.getValue() == null || entry.getValue().length()==0))
            {
                if (!required)
                {
                    send = false
                }
            }
            if (send)
            {
                if (entry.isHidden())
                {
                    def detailItem = request.getInputData().getDetails().addDetailsItem()
                    detailItem.setWIPDataName(entry.getId())
                    detailItem.setWIPDataValue(entry.getValue())
                    submit = true
                }
                else
                {
                    logger.info("Skip wip data submission for '" + entry.getId() + "' since it's not hidden")
                }
            }  
        }
        
        if (submit)
        {
            def reply = cCamstarService.setWIPData(request)
            if(reply.isSuccessful())
            {
                String message = reply.getResponseData().getCompletionMsg()
                    logger.info(message)
                    return message
            }
            else
            {
                throw new Exception(reply.getExceptionData().getErrorDescription())
            }            
        }
        return ""
    }
    
    String getCalcMsg()
    {
        def msg = ""
        def wds = wipDataDomainObjectManager.getAllWipDataSet()
        for(objectSet in wds)
        {
            def value = objectSet.getPropertyContainer().getString("CenterMinViolation", "")
            if (value.length()>0)
            {
                if (msg.length()>0)
                {
                    msg = msg + ", "
                }
                msg = msg + value
            }
        }
        if (msg.length()>0)
        {
            return "CenterMinViolation: [" + msg + "]"
        }
        return ""
    }
}