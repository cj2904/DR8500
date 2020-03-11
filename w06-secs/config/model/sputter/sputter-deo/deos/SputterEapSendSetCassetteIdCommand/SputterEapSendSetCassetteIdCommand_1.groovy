package SputterEapSendSetCassetteIdCommand

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.CompileStatic
import sg.znt.pac.W06Constants
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.W06Util
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
Sputter specific function:<br/>
<b>pac to send SET_CASS_ID command to equipment</b>
''')
class SputterEapSendSetCassetteIdCommand_1 {


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())


	@DeoBinding(id="CEquipment")
	private CEquipment cEquipment

	@DeoBinding(id="InputXmlDocument")
	private String inputXmlDocument

	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager

	@DeoBinding(id="WipDataDomainObjectManager")
	private WipDataDomainObjectManager wipDataDomainObjectManager

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

		def eqpId = cEquipment.getSystemId()
		def lotId = cEquipment.getCurrentLotId()
		def Port = W06Constants.MES_WIP_DATA_PORT_NAME
		def wipDataDomainObject = wipDataDomainObjectManager.getDomainObject(eqpId + "-" + lotId)
		def trackInWipDataItems = wipDataDomainObject.getElement(Port)
		def wipPortValue = trackInWipDataItems.getValue().trim()

		cLot.getPropertyContainer().setString("PortSelected", wipPortValue.trim().toUpperCase())

		def newValue
		def portAvail = false

		if (!wipPortValue.isEmpty() && !wipPortValue.equals(""))
		{
			def eqp = PacConfig.getStringProperty("Equipment1.Name", "")
			def portAvailable = PacConfig.getStringArrayProperty("PortAvailable." + eqp + ".ID", "", ",")

			for (item in portAvailable)
			{
				if (wipPortValue.equalsIgnoreCase(item.trim())) {
					portAvail = true
					newValue = wipPortValue
				}
			}
		}
		if (!portAvail)
		{
			throw new Exception("PortId : '$wipPortValue' not found in equipment ! The allowable PortIDs are : A, B, C !" )
		}
		
		def initialLotId = trackInLot.getContainerName()
		def trimLotId = W06Util.getLotIdWithTrimWorkOrder(initialLotId)
			
		S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("SET_CASS_ID"))
		request.addParameter(new SecsAsciiItem("PortID"), new SecsAsciiItem(newValue.trim())) //TODO: need to get port from track in WIP data
		request.addParameter(new SecsAsciiItem("CassetteID"), new SecsAsciiItem(trimLotId.trim()))

		def reply = secsGemService.sendS2F41HostCommandSend(request)
		if (reply.isCommandAccepted())
		{
			//ok
		}
		else
		{
			throw new Exception("Executing remote command SET_CASS_ID failed, reply message: " + reply.getHCAckMessage())
		}
	}
}