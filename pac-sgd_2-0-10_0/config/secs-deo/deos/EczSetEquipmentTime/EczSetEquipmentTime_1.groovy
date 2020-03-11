package EczSetEquipmentTime

import java.text.SimpleDateFormat

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService

@Deo(description='''
Set the equipment time
''')
class EczSetEquipmentTime_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        secsGemService.setEquipmentTime()
        Date equipmentDate = secsGemService.requestEquipmentTime()
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        logger.info ("Uploaded equipment time: "+sdf.format(equipmentDate))
    }
}