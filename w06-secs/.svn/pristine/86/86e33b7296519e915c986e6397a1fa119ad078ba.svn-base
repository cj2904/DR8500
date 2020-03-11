package EapCheckLotStarted_GRD

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EapCheckLotStarted_GRD_1 {


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="CEquipment")
	private CEquipment cEquipment

	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager

	@DeoBinding(id="LoopCount")
	private int loopCount=20
	
	/**
	 *
	 */
	@DeoExecute
	public void execute() {
		
		def eqpId = cEquipment.getSystemId()

		def lotId = cEquipment.getCurrentLotId()
		def cLot = cMaterialManager.getCLot(lotId)

		def portSelection = cLot.getPropertyContainer().getString("PortSelected", "")

		def newValue

		if (!portSelection.isEmpty() && !portSelection.equals("")){
			if (portSelection.contains(",")){
				newValue = portSelection.split(",")
			}
			else if (!portSelection.contains(",")){

				newValue = portSelection
			}

			for (def item in newValue) {

				def i = 0

				def eqpContainer = cEquipment.getPropertyContainer()
				while(i < loopCount){

					Thread.sleep (1000)


					if(eqpContainer.getBoolean("IsLotStarted", false)==true){
		
						cEquipment.getPropertyContainer().getBoolean("stopEvent", true)
						
						break
					}
					i++;
				}

				if (eqpContainer.getBoolean("IsLotStarted", false)==false){		
					
					cEquipment.getPropertyContainer().setBoolean("stopEvent", false)
					
					throw new Exception("The Lot '$lotId' from Port : '$item' in equipment : '$eqpId' is not started !")				
				}
			}
		}
	}
}
