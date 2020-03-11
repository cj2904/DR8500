package SputterEapSendMapCommand

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.pac.PacConfig

@CompileStatic
@Deo(description='''
Sputter specific function:<br/>
<b>pac to send MAP command to equipment</b>
''')
class SputterEapSendMapCommand_1 {


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="CEquipment")
	private CEquipment cEquipment

	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager

	@DeoBinding(id="InputXmlDocument")
	private String inputXmlDocument

	private SecsGemService secsGemService
	/**
	 *
	 */
	@DeoExecute
	public void execute() {
		W02TrackInLotRequest trackInLot = new W02TrackInLotRequest(inputXmlDocument)
		def cLot = cMaterialManager.getCLot(trackInLot.getContainerName())
		if (cLot == null) {
			throw new Exception("Lot " + trackInLot.getContainerName() + " does not exist")
		}

		secsGemService = (SecsGemService) cEquipment.getExternalService()

		def lotId = cEquipment.getCurrentLotId()
		def portSelection = cLot.getPropertyContainer().getString("PortSelected", "")

		def newValue
		def portAvail = false
		
				if (!portSelection.isEmpty() && !portSelection.equals(""))
				{
					def eqp = PacConfig.getStringProperty("Equipment1.Name", "")
					def portAvailable = PacConfig.getStringArrayProperty("PortAvailable." + eqp + ".ID", "", ",")

					for (item in portAvailable)
					{
						if (portSelection.equalsIgnoreCase(item.trim())) {
							portAvail = true
							newValue = portSelection
						}
					}
				}
				if (!portAvail)
				{
					throw new Exception("PortId : '$portSelection' not found in equipment ! The allowable PortIDs are : A, B, C !" )
				}
				
				
		S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("MAP"))
		request.addParameter(new SecsAsciiItem("PortID"), new SecsAsciiItem(newValue.trim())) //TODO: need to get port from track in WIP data

		def reply = secsGemService.sendS2F41HostCommandSend(request)
		if (reply.isCommandAccepted())
		{
			//ok
		}
		else
		{
			throw new Exception("Executing remote command MAP failed, reply message: " + reply.getHCAckMessage())
		}
	}
}