package SnrEqpDownloadRecipe

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked
import sg.znt.pac.exception.ValidationFailureException
import sg.znt.pac.machine.TscEquipment
import sg.znt.services.camstar.outbound.TrackInLotRequest

@Deo(description='''
Scenario based download recipe
''')
class SnrEqpDownloadRecipe_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="TscEquipment")
    private TscEquipment equipment
    
	@DeoBinding(id="InputXmlDocument")
	private String inputXmlDocument
    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
		TrackInLotRequest outbound = new TrackInLotRequest(inputXmlDocument)
		def recipe = outbound.getRecipeName() + "_" + outbound.getRecipeRevision()
		
        def rmsService = equipment.getRmsService()
        if (rmsService != null)
        {
            equipment.getModelScenario().eqpDownloadRecipe(recipe, rmsService, false)
        }
        else
        {
            throw new ValidationFailureException("", "RMS is not configured, please report to person-in-charge")
        }
    }
}