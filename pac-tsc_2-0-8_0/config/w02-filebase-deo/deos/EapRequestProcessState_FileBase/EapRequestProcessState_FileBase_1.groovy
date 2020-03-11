package EapRequestProcessState_FileBase

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
Eap request process state
''')
class EapRequestProcessState_FileBase_1
{
    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

        @DeoBinding(id="EquipmentFileService")
        private EquipmentFileService equipmentFileService

    @DeoBinding(id="InputXml")
    private String inputXml

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outbound = new W02TrackInLotRequest(inputXml)
        def outboundLot = outbound.getContainerName()

        def lotList = cMaterialManager.getCLotList(new LotFilterAll())
        def correct = false
        def eqpId

        for(lot in lotList)
        {
            if(lot.getId().equalsIgnoreCase(outboundLot))
            {
                logger.info("Jimmy systemid = " + cEquipment.getSystemId())
                eqpId = lot.getEquipmentId()

                if(eqpId.equalsIgnoreCase(cEquipment.getSystemId()))
                {
                    logger.info("Jimmy eqpId = $eqpId, systemid = " + cEquipment.getSystemId())
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
                _fileHandler = equipmentFileService.getFileHandler()

                def requestId = _fileHandler.getREQUEST_ID()
                logger.info("Performing request process state: last RequestId = '$requestId'")
                String requestProcessState = equipmentFileService.requestProcessState()

                def isReady2Start = _fileHandler.isReady2Start(requestProcessState)
                if(!isReady2Start)
                {
                    throw new Exception("Equipment is not ready to start, current process state: $requestProcessState !")
                }
                logger.info("File base request process state successful...")
            }
            catch(Exception e)
            {
                logger.error(e.getMessage())
                throw new Exception(e.getMessage())
            }
        }
    }
}