package EczGetCurrentMachineRecipe

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import sg.znt.pac.machine.CEquipment

@Deo(description='''
Get current recipe id on the SECS equipment
''')
class EczGetCurrentMachineRecipe_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        cEquipment.uploadMachineLoadedRecipe()
        logger.info("Current recipe on the machine is " + cEquipment.getMachineLoadedRecipe())
    }
}