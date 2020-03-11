package EapValidateProcessState

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked;
import sg.znt.pac.machine.CEquipment
import sg.znt.services.zwin.ZWinApiException

@Deo(description='''
Validate machine process state is ready to start
''')
class EapValidateProcessState_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="CEquipment")
	private CEquipment equipment
    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
		if(!equipment.getProcessState().isReady2Start())
		{
            def exception = new ZWinApiException("-2", "Machine '" + equipment.getName() + "' is not idle, current process state = " + equipment.getProcessState().getStateName())
            exception.setName(equipment.getName())
			throw exception
		}
    }
}