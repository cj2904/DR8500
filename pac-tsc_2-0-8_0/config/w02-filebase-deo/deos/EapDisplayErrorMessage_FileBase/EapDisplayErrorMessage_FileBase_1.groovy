package EapDisplayErrorMessage_FileBase

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import sg.znt.services.equipment.file.EquipmentFileHandler
import sg.znt.services.equipment.file.EquipmentFileService

@CompileStatic
@Deo(description='''
Display error message at eqp
''')
class EapDisplayErrorMessage_FileBase_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager

    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment
    
    @DeoBinding(id="EquipmentFileService")
    private EquipmentFileService equipmentFileService

    @DeoBinding(id="Exception")
    private Throwable exception
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        EquipmentFileHandler _fileHandler
        try
        {
            def recipeName = ""
            def lotId = ""
            def lotList = cMaterialManager.getCLotList(new LotFilterAll())
            if (lotList.size()>0)
            {
                for (lot in lotList)
                {
                    lotId = lot.getId()
                    def lotEqpId = lot.getEquipmentId()
                    if (lotEqpId.equalsIgnoreCase(mainEquipment.getSystemId()))
                    {
                        recipeName =  lot.getRecipe()
                        logger.info("Get recipe '$recipeName' from lot '$lotId'...")
                        break
                    }
                }
            }
            def errorMessage = exception.getMessage()
            _fileHandler = equipmentFileService.getFileHandler()

            def requestId = _fileHandler.getREQUEST_ID()
            logger.info("Performing display error message: last RequestId = '$requestId'...")
            equipmentFileService.displayErrorMessage(lotId, recipeName, errorMessage)

            logger.info("File base display error message successful...")
        }
        catch(Exception e)
        {
            logger.error(e.getMessage())
            throw new Exception(e.getMessage())
        }
    }
}