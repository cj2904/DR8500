package W02ModbusVerifyAlternateChamber

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.SgdConfig
import sg.znt.pac.TscConstants
import sg.znt.pac.exception.ModbusException
import sg.znt.pac.machine.CEquipment
import sg.znt.services.modbus.SgdModBusServiceImpl.ModBusEvent
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Verify start for alternate chamber
''')
class W02ModbusVerifyAlternateChamber_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment

    @DeoBinding(id="ModBusEvent")
    private ModBusEvent modBusEvent

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def chamberId = modBusEvent.getChamber()
		def alternateChamberId = SgdConfig.findAlternateCheckChamberId(chamberId)
		
		if (alternateChamberId !=null && alternateChamberId.length()>0)
		{
			def lastAlternateChamberId = mainEquipment.getPropertyContainer().getString(TscConstants.EQP_ATTR_LAST_ALTERNATE_CHAMBER, "")
			if (chamberId.equals(lastAlternateChamberId))
			{
				throw new ModbusException("Must start at $alternateChamberId!", ModbusException.WRONG_CHAMBER)
			}
			else
			{
				mainEquipment.getPropertyContainer().setString(TscConstants.EQP_ATTR_LAST_ALTERNATE_CHAMBER, chamberId)
			}
		}
    }
}