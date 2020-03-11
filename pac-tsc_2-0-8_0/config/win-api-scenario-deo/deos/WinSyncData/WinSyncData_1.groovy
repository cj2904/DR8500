package WinSyncData

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.domainobject.WipDataDomainObject
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.material.WaferFilterAll
import sg.znt.pac.util.TscWinApiEqpUtil
import sg.znt.services.zwin.ZWinApiService
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import elemental.json.JsonObject

@CompileStatic
@Deo(description='''
Synchronize the data to win api gateway during startup
''')
class WinSyncData_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="ParamMap")
    private Map<String, String> paramMap

    @DeoBinding(id="Equipment")
    private CEquipment equipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

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
        def currentWaferId = ""
        def currentLotId = ""
        def currentWaferNo = ""
        def currentWipDataList = ""
        def serviceType =""

        def lots = cMaterialManager.getCLotList(new LotFilterAll())
        for (lot in lots)
        {
            if (lot.getEquipmentId().equalsIgnoreCase(equipment.getSystemId()))
            {
                currentLotId = lot.getId()
                def waferList = lot.getWaferList(new WaferFilterAll())
                List<WipDataDomainObject>  wipDataItems = null
                if (waferList.size() > 0)
                {
                    for (wafer in waferList)
                    {
                        if (wafer.getEquipmentId().equalsIgnoreCase(equipment.getSystemId()))
                        {
                            currentWaferId = wafer.getWaferScribeID()
                            currentWaferNo = wafer.getId()
                            def waferWipData = wafer.getWipData()
                            if (waferWipData != null)
                            {
                                wipDataItems = waferWipData.getTrackOutWipDataItems()
                                serviceType = wipDataItems.get(0).getServiceType()
                            }
                            else
                            {
                                wipDataItems = lot.getWipDataByEquipment(equipment.getSystemId()).getTrackOutWipDataItems()
                                serviceType = wipDataItems.get(0).getServiceType()
                            }

                            break
                        }
                    }
                }
                else
                {
                    wipDataItems = lot.getWipDataByEquipment(equipment.getSystemId()).getTrackOutWipDataItems()
                    if (wipDataItems != null && wipDataItems.size()>0)
                    {
                        serviceType = wipDataItems.get(0).getServiceType()
                    }
                }
                currentWipDataList = getWipDataWinDto(wipDataItems)
                break
            }
        }

        ZWinApiService winApiService = ((ZWinApiService)secsGemService)
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("LotId",currentLotId)
        param.put("WaferId", currentWaferId)
        param.put("WaferNo", currentWaferNo)
        param.put("ServiceType", serviceType)
        param.put("WipDataList", currentWipDataList)
        param.put("EquipmentId", equipment.getSystemId())

        return winApiService.buildEventReplyMessage(eventTid, eventName, param, "", "");
    }

    String getWipDataWinDto(List<WipDataDomainObject> wipDataItems)
    {
        def wipDataList = ""
        TscWinApiEqpUtil.removeNonAutoItem(wipDataItems)
        for (WipDataDomainObject wipDataDomainObject : wipDataItems)
        {
            if (wipDataDomainObject.isHidden())
            {
                if(wipDataList.length()>0)
                {
                    wipDataList = wipDataList + ";";
                }
                wipDataList = wipDataList + wipDataDomainObject.getId() + "=" + wipDataDomainObject.getValue();
            }
        }
        return wipDataList
    }
}