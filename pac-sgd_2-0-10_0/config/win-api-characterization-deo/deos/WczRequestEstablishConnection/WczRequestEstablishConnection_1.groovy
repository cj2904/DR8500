package WczRequestEstablishConnection

import java.util.Map.Entry

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.dto.S1F13EstablishCommunicationsRequestByHost
import groovy.transform.CompileStatic
import sg.znt.services.secs.dto.SgdS1F14EstablishCommunicationsRequestAcknowledgeByEquipment
import sg.znt.services.secs.dto.SgdS1F14EstablishCommunicationsRequestAcknowledgeByEquipment.SgdEquipmentInfo
import sg.znt.services.zwin.ZWinApiService

@CompileStatic
@Deo(description='''
Request establish connection
''')
class WczRequestEstablishConnection_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="WinApiService")
    private ZWinApiService winApiService
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new S1F13EstablishCommunicationsRequestByHost()
        def reply = (SgdS1F14EstablishCommunicationsRequestAcknowledgeByEquipment) winApiService.sendS1F13EstablishCommunicationsRequest(request)
        
        if(reply.getEquipmentInfo() != null)
        {
            SgdEquipmentInfo info = (SgdEquipmentInfo) reply.getEquipmentInfo()
            List<Map<String, Object>> infoList = info.getEquipmentInfos()
            for (Map<String, Object> item : infoList)
            {
                for (Entry<String, Object> entry : item.entrySet())
                {
                    logger.info("RequestEstablishConnection: " + entry.getKey() + ":" + entry.getValue())                    
                }
            }
        }
    }
}