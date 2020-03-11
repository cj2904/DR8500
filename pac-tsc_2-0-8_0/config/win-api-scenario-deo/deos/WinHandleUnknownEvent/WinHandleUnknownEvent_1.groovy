package WinHandleUnknownEvent


import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.machine.CEquipment
import sg.znt.services.zwin.ZWinApiService
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import elemental.json.JsonObject

@CompileStatic
@Deo(description='''
Handle unknow event
''')
class WinHandleUnknownEvent_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

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

    @DeoExecute(result="Reply")
    public JsonObject execute()
    {
        ZWinApiService winApiService = ((ZWinApiService)secsGemService)
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("ShowMessage", "true");
        if(eventName.length()>0)
        {
            JsonObject ob = winApiService.buildEventReplyMessage(eventTid, eventName, param, "-1", "Unknown Event '" + eventName + "'");
            winApiService.sendTcpMessage(ob)
        }
        else
        {
            logger.info("No Event Name Found. Do Nothing.")    
        }
    }
}