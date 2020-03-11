package EqpSelectDummyRecipe_SPT

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
import sg.znt.pac.machine.CEquipment
import sg.znt.services.camstar.outbound.TrackInLotRequest

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EqpSelectDummyRecipe_SPT_1 {


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="SecsGemService")
	private SecsGemService secsGemService

	@DeoBinding(id="RecipeManager")
	private RecipeManager recipeManager

	@DeoBinding(id="CEquipment")
	private CEquipment cEquipment
	
    @DeoBinding(id="DummyRecipeId")
    private String dummyRecipeId = "IDLE"
    
    @DeoBinding(id="DummyRecipeExt")
    private String dummyRecipeExt


	/**
	 *
	 */
	@DeoExecute
	public void execute() {

		def eqp = PacConfig.getStringProperty("Equipment1.Name", "")
		def lot = ""
		
		def dummyRecipe = dummyRecipeId + dummyRecipeExt
		def parameters = new HashMap<String, Object>()
            parameters.put("PPID", dummyRecipe)
		cEquipment.getModelScenario().eqpSelectRecipe(dummyRecipe, parameters)
		}
	}