package WinMeasurementClassRequest

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import elemental.json.JsonObject
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.WaferClassificationDomainObject
import sg.znt.pac.domainobject.WaferClassificationDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.zwin.ZWinApiService

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class WinMeasurementClassRequest_1
{
    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager materialManager

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

    @DeoBinding(id="WaferClassificationManager")
    private WaferClassificationDomainObjectManager wcManager

    private CLot clot
    private String winReplyCode = ""
    private String winReplyMsg = ""

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

        clot = materialManager.getCLot(lotId)

        List<WaferClassificationDomainObject> wcallresetqty = wcManager.getAllDomainObject()
        for(int i =0 ; i < wcallresetqty.size(); i++)
        {
            WaferClassificationDomainObject item = (WaferClassificationDomainObject) wcallresetqty[i]
            item.setQuantity(0)
        }

        def ungroupedData = ""
        String[] data = ((String) paramMap.get("MeasurementData")).split(";")
        for(int i = 0 ; i < data.length ; i++)
        {
            WaferClassificationDomainObject wc = findWcClass(data[i])
            if(wc!=null)
            {
                wc.setQuantity(wc.getQuantity() + 1)
            }
            else
            {
                int j=i+1
                ungroupedData = ungroupedData + "[$j]" + data[i] + ","
                logger.error("Data '" + data[i] + "' does not belong to any of the wafer classification group!")
            }
        }
        def msg = ""
        if (ungroupedData.length()>0)
        {
            ungroupedData.substring(0, ungroupedData.length()-1)
            msg =  "List of measurement data [" + ungroupedData + "] that does not belongs to any of the wafer class!"
        }

        String itemclasslist = "";
        List<WaferClassificationDomainObject> wcall = wcManager.getAllDomainObject()
        for(int i =0 ; i < wcall.size(); i++)
        {
            WaferClassificationDomainObject item = (WaferClassificationDomainObject) wcall[i]
            if(itemclasslist.length()!=0)
            {
                itemclasslist = itemclasslist + ","
            }
            itemclasslist = itemclasslist + item.toString()
        }

        ZWinApiService winApiService = ((ZWinApiService)secsGemService)
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("LotId",lotId)
        param.put("ItemClassList",itemclasslist)
        param.put("EquipmentId", cEquipment.getSystemId())
        param.put("Message", msg)
        return winApiService.buildEventReplyMessage(eventTid, eventName, param, winReplyCode, winReplyMsg);
    }

    WaferClassificationDomainObject findWcClass(String datum)
    {
        List<WaferClassificationDomainObject> wcall = wcManager.getAllDomainObject()
        for(int i =0 ; i < wcall.size(); i++)
        {
            WaferClassificationDomainObject item = (WaferClassificationDomainObject) wcall[i]
            double val = Double.parseDouble(datum.trim())
            logger.info("Measurement val ["+val+"]")
            logger.info("Class Min ["+item.getMin()+"]")
            logger.info("Class Max ["+item.getMax()+"]")
            if((val > item.getMin()) && (val <= item.getMax()))
            {
                logger.info("$val belong to class ["+item.getId()+"]")
                return item
            }
        }
        return null
    }
}