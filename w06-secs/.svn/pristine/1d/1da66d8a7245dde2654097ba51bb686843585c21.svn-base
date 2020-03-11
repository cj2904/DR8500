package EqpSelectDummyRecipe_GRD

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EqpSelectDummyRecipe_GRD_1 {


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="CEquipment")
	private CEquipment cEquipment

	@DeoBinding(id="DummyRecipeId")
	private String dummyRecipeId = "IDLE"
	
	@DeoBinding(id="DummyRecipeExt")
	private String dummyRecipeExt
	
	@DeoBinding(id="DummyLotId")
	private String dummyLotId = "IDLE"

	/**
	 *
	 */
	@DeoExecute
	public void execute() {

		def dummyRecipe = dummyRecipeId + dummyRecipeExt
		def portList = ["A", "B"]
		def parameters = new HashMap<String, Object>()

		for (def item in portList) {
			parameters.put("PPID_" + item, dummyRecipe)
			parameters.put("LOTID_" + item, dummyLotId)
		}
		cEquipment.getModelScenario().eqpSelectRecipe(dummyRecipe, parameters)
	}
}