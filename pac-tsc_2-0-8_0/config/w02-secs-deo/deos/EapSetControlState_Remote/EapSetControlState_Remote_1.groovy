package EapSetControlState_Remote

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
set the eqp control state to remote
''')
class EapSetControlState_Remote_1
{

    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def controlState = mainEquipment.getControlState()
        def control = controlState.getStateName()

        if(controlState.isOnline() && controlState.isLocal())
        {
            controlState.setRemote()
        }
        else
        {
            throw new Exception("Equipment: '$mainEquipment' current is in '$control' state!")
        }
    }
}