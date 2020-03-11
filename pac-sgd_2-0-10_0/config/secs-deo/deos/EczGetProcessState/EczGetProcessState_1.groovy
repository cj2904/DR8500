package EczGetProcessState

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import sg.znt.pac.machine.CEquipment

@Deo(description='''
Get and update process state from equipment
''')
class EczGetProcessState_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="Equipment")
    private CEquipment equipment

    /**
     *
     */
    @DeoExecute
    public void execute() {
        if(equipment.isSecs())
        {
            equipment.updateProcessState()
        }
        
    }
}