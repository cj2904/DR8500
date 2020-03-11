package EqpSelectRecipe_GRD

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.camstar.semisuite.service.dto.TrackOutResponse
import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.outbound.TrackInLotRequest

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EqpSelectRecipe_GRD_1 {


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="SecsGemService")
	private SecsGemService secsGemService

	@DeoBinding(id="InputXml")
	private String inputXml

	@DeoBinding(id="RecipeManager")
	private RecipeManager recipeManager

	@DeoBinding(id="CEquipment")
	private CEquipment cEquipment
	
	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager
	
	/**
	 *
	 */
	@DeoExecute
	public void execute() {
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

				def eqp = PacConfig.getStringProperty("Equipment1.Name", "")

				logger.info("Current Port used by Equipment is : " + item)

				if (item.length() > 0){
					def trackOutbound = new TrackInLotRequest(inputXml)

					def recipeName = trackOutbound.getRecipeName()
					def lot = trackOutbound.getContainerName()

					if (recipeName == null || recipeName.length() == 0) {
						throw new Exception("Recipe not found!")
					}

					def recipeId = cEquipment.getSystemId() + "-" + recipeName
					def recipe = recipeManager.getDomainObject(recipeId)

					if (recipe == null) {
						throw new Exception("$recipeId not found in recipe domain object!")
					}
					try {
						def recipeParam = recipe.getElement("EqpRecipe")

						if (recipeParam == null) {
							throw new Exception("Equipment recipe cannot be empty, please configure as Recipe Parameter with 'EqpRecipe' in Camstar!")
						}

						def recipeValue = recipeParam.getParameterValue()
						if (recipeValue==null || recipeValue.length()==0) {
							throw new Exception("Equipment recipe value cannot be empty, please configure 'EqpRecipe' value in Camstar!")
						}
						//cEquipment.getModelScenario().eqpSelectRecipe(eqpRecipe, parameters)
						sendRecipe(recipeValue, lot, item)
					}
					catch (Exception e) {
						logger.error(e.getMessage())
					}
					cEquipment.getPropertyContainer().setBoolean("IsLotStarted", false)
				}
			}
		}
	}
	void sendRecipe(String recipeValue, String lot , String item) {

		def parameters = new HashMap<String, Object>()
		parameters.put("PPID_" + item.trim(), recipeValue)
		parameters.put("LOTID_" + item.trim(), lot)
		cEquipment.getModelScenario().eqpSelectRecipe(recipeValue, parameters)
		}
	}