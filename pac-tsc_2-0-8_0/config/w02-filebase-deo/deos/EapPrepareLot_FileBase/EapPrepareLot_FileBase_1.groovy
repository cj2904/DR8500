package EapPrepareLot_FileBase

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.TscConfig
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import sg.znt.services.equipment.file.EquipmentFileHandler
import sg.znt.services.equipment.file.EquipmentFileServiceImpl


@CompileStatic
@Deo(description='''
Eap prepare lot
''')
class EapPrepareLot_FileBase_1
{

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="EquipmentFileService")
    private EquipmentFileService equipmentFileService

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outbounds = new W02TrackInLotRequest(inputXmlDocument)
        def outboundLot = outbounds.getContainerName()

        def lotList = cMaterialManager.getCLotList(new LotFilterAll())
        def correct = false
        def eqpId

        for(lot in lotList)
        {
            if(lot.getId().equalsIgnoreCase(outboundLot))
            {
                eqpId = lot.getEquipmentId()

                if(eqpId.equalsIgnoreCase(cEquipment.getSystemId()))
                {
                    correct = true
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

        if(correct == true)
        {
            EquipmentFileHandler _fileHandler
            try
            {
                W02TrackInLotRequest outbound = new W02TrackInLotRequest(inputXmlDocument)
                def lotId = outbound.getContainerName()
                def probingYieldLimit = outbound.getYieldFailureLimit()
                def recipeName = ""
                def recipeParamList = outbound.getRecipeParamList()
                for (recipeParam in recipeParamList)
                {
                    if (recipeParam.getParamName().equalsIgnoreCase("R_Sub"))
                    {
                        recipeName = recipeParam.getParamValue()
                        break
                    }
                }

                if (recipeName == "")
                {
                    logger.info("RecipeParam_ParamName : 'R_Sub' could not be found, use Recipe_Name instead : $recipeName ")
                    recipeName = outbound.getRecipeName()
                }

                def hashMap = new LinkedHashMap<String, String>()
                hashMap.put("ProbingYieldLimit", probingYieldLimit)

                _fileHandler = equipmentFileService.getFileHandler()

                def requestId = _fileHandler.getREQUEST_ID()
                logger.info("Performing prepare lot: last RequestId = '$requestId'")

                equipmentFileService.prepareLot(lotId, recipeName, hashMap)

                logger.info("File base prepare lot successful...")
            }
            catch(Exception e)
            {
                logger.error(e.getMessage())
                throw new Exception(e.getMessage())
            }
        }
    }
}