package EczGetStatusVariable

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S1F3SelectedEquipmentStatusRequest
import de.znt.services.secs.dto.S1F4SelectedEquipmentStatusData
import de.znt.zsecs.composite.SecsComponent
import de.znt.zsecs.composite.SecsDataItem
import de.znt.zsecs.composite.SecsDataItem.ItemName
import eqp.EczUtil

@Deo(description='''
Request status variable
''')
class EczGetStatusVariable_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="SecsService")
    private SecsGemService secsService

    @DeoBinding(id="StatusVariables")
    private String vid

    /**
     *
     */
    @DeoExecute(result="statusData")
    public String execute()
    {
        S1F3SelectedEquipmentStatusRequest request = new S1F3SelectedEquipmentStatusRequest()
        SecsComponent< ? > svid = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(vid)))
        request.addSVID(svid)

        S1F4SelectedEquipmentStatusData s1F4Reply = secsService.sendS1F3SelectedEquipmentStatusRequest(request)

        String status = ""
        def dataSize = s1F4Reply.getData().getSize()
        for(int i=0;i<dataSize;i++)
        {
            if(status.length()>0)
            {
                status = status + ","
            }
            status = status + EczUtil.getVariableData(s1F4Reply.getData().getSV(i))
        }
        logger.info("Get status variable : '" + vid + "', value is : '" + status + "'")

        return status
    }
}