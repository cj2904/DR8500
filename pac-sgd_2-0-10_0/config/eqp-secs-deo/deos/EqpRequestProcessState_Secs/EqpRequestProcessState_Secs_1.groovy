package EqpRequestProcessState_Secs

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import groovy.transform.TypeChecked
import sg.znt.pac.machine.CEquipment

@Deo(description='''
Request variable from equipment
''')
class EqpRequestProcessState_Secs_1
{
    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService
    
    @DeoBinding(id="CEquipment")
    private CEquipment equipment
    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
        equipment.updateControlState()
        equipment.updateProcessState()
    }
}
