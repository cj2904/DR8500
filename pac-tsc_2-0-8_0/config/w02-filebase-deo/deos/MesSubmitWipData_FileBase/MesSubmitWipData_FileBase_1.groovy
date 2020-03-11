package MesSubmitWipData_FileBase

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
Submit wafer WIP data to MES
''')
class MesSubmitWipData_FileBase_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="LotId")
    private String lotId
    
    @DeoBinding(id="WaferId")
    private String waferId
    
    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService
    
    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataDomainObjectManager

    @DeoBinding(id="Equipment")
    private CEquipment cEquipment
    
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager materialManager
    
    private String rrvWipDataName = TscConfig.getStringProperty("RRV.WipData.Name", "");
    
    private String serviceType= ""
    /**
     *
     */
    @DeoExecute(result="reply")
    public String execute()
    {
       if (lotId == null || lotId.length()==0)
       {
            throw new Exception("Missing param 'LotId'!")
       }
        
        def lot = materialManager.getCLot(lotId)
        def message = ""
        message = submitWaferWipData(lot, waferId, serviceType)
        
        if (message.length()>0)
        {
            message = message + " "
        }
        message = message// + getCalcMsg()
        
        return message
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
                if (entry.isHidden() && entry.getId().equalsIgnoreCase("WftGoodDieCount"))
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
   
}