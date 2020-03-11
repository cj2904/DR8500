package EqpSetEquipmentTime

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService;
import groovy.transform.TypeChecked

@Deo(description='''
Set equipment time
''')
class EqpSetEquipmentTime_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


	@DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
    	secsGemService.setEquipmentTime();
    }
}