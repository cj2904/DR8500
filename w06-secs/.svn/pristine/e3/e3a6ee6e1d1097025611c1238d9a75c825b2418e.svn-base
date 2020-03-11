package EapVerifyDoseLimitReset

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
eap verify eqp dose limit is reset
''')
class EapVerifyDoseLimitReset_1
{


    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def eqp = cEquipment.getSystemId()
        def doseLimit = cEquipment.getPropertyContainer().getBoolean("DoseLimitReset", true)
        if(!doseLimit)
        {
            throw new Exception("Equipment: 'eqp' dummy dose limit is not yet reset!!! Please reset is before continue!!!")
        }
    }
}