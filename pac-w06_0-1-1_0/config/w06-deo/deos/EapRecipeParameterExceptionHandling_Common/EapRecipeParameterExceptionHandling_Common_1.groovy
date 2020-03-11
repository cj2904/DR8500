package EapRecipeParameterExceptionHandling_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.W06Constants
import sg.znt.pac.exception.CamstarException
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
W06 specific handling:<br/>
<b>Reply camstar on recipe parameter validation error</b>
''')
class EapRecipeParameterExceptionHandling_Common_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        W02TrackInLotRequest trackInLot = new W02TrackInLotRequest(inputXmlDocument)
        def cLot = cMaterialManager.getCLot(trackInLot.getContainerName())
        if (cLot != null)
        {
            def exceptionMsg = cLot.getPropertyContainer().getString("ExceptionMessage", "")
            if (exceptionMsg != null && exceptionMsg.length() > 0)
            {
                throw new CamstarException(exceptionMsg)
            }
			else
			{
				def batchId = cLot.getBatchId()
				List<CLot> cLotList = cMaterialManager.getCLotList(new LotFilterAll())
				for (curCLot in cLotList)
				{
					if (curCLot.getBatchId() == batchId)
					{
						curCLot.getPropertyContainer().setBoolean(W06Constants.PAC_LOT_PROPERTY_CONTAINER_RECIPE_VALIDATION_COMPLETED, true)
					}
				}
			}
        }
    }
}