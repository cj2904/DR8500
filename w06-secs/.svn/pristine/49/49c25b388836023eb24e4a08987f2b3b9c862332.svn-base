package EqpStartEquipment_GRD

import groovy.transform.TypeChecked
import sg.znt.pac.machine.CEquipment

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.SecsServiceImpl
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.zsecs.composite.SecsAsciiItem

@Deo(description='''
Send remote start command to gateway
''')
class EqpStartEquipment_GRD_1 {


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())


	@DeoBinding(id="SecsGemService")
	private SecsGemService secsGemService

	@DeoBinding(id="CEquipment")
	private CEquipment cEquipment
	/**
	 *
	 */
	@DeoExecute
	@TypeChecked
	public void execute() {
		def eqpId = cEquipment.getSystemId()

		if (cEquipment.getPropertyContainer().getBoolean("stopEvent", false)== false){
			throw new Exception("Event received TimeOut, Equipment : '$eqpId' not started !")
		}
		else {
			cEquipment.getPropertyContainer().getBoolean("stopEvent", true)
			
			S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("START"))
			S2F42HostCommandAcknowledge reply = secsGemService.sendS2F41HostCommandSend(request)
			if(reply.isCommandAccepted()) {
				//OK
				cEquipment.getPropertyContainer().setBoolean("IsLotStarted", true)
			}
			else {
				throw new Exception("Error sending start command to equipment: '$eqpId' with reply message: " + reply.getHCAckMessage())
			}
		}
	}
}