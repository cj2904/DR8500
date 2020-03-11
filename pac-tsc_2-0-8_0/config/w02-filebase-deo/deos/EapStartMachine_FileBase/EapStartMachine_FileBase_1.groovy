package EapStartMachine_FileBase

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.TscConfig
import sg.znt.pac.domainobject.Recipe
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.equipment.file.EquipmentFileHandler
import sg.znt.services.equipment.file.EquipmentFileServiceImpl


@CompileStatic
@Deo(description='''
Eap start machine
''')
class EapStartMachine_FileBase_1
{

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

        @DeoBinding(id="EquipmentFileService")
        private EquipmentFileService equipmentFileService

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment

    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager

    @DeoBinding(id="isUniDirectional")
    private boolean isUniDirectional

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        Recipe mainRecipe = null
        def lotList = cMaterialManager.getCLotList(new LotFilterAll())
        def lotId = ""
        def doRecipeName = ""
        def eqpId

        if (lotList.size()>0)
        {
            for (lot in lotList)
            {
                lotId = lot.getId()
                def lotEqpId = lot.getEquipmentId()
                if (lotEqpId.equalsIgnoreCase(mainEquipment.getSystemId()))
                {
                    eqpId = lot.getEquipmentId()
                    def recipe = mainEquipment.getSystemId() + "-" + lot.getRecipe()
                    logger.info("Get recipe '$recipe' from lot '$lotId'...")
                    mainRecipe = recipeManager.getDomainObject(recipe)
                    if(mainRecipe == null)
                    {
                        throw new Exception("No recipe selected for lot: '$lot'")
                    }
                    doRecipeName = mainRecipe.getRSubRecipeName()
                    break
                }
            }
        }

//        def prober = TscConfig.getTrackInProberEqp(eqpId)
//        def inputPath = PacConfig.getStringProperty(prober + ".Path", "") + "\\Input"
//        def outputPath = PacConfig.getStringProperty(prober + ".Path", "") + "\\Output"
//        def alarmPath = PacConfig.getStringProperty(prober + ".Path", "") + "\\Alarm"
//        def eventPath  = PacConfig.getStringProperty(prober + ".Path", "") + "\\Event"

        //        def inputPath = ""
        //        def outputPath = ""
        //        def alarmPath = ""
        //        def eventPath = ""

//        def equipmentFileService = new EquipmentFileServiceImpl(prober, 1000, 20000, inputPath, outputPath, alarmPath, eventPath)

        if (!(doRecipeName.length() > 0))
        {
            throw new Exception("Recipe in recipe domain is empty!")
        }

        EquipmentFileHandler _fileHandler
        try
        {
            _fileHandler = equipmentFileService.getFileHandler()

            def requestId = _fileHandler.getREQUEST_ID()
            logger.info("Performing prepare lot: last RequestId = '$requestId'")

            // De Ce  = not uniDirectional
            // Chang Luo = uniDirectional
            //            if(isUniDirectional == false)

            equipmentFileService.startMachine(lotId, doRecipeName)
            logger.info("File base start machine successful...")

        }
        catch(Exception e)
        {
            logger.error(e.getMessage())
            throw new Exception(e.getMessage())
        }
    }
}