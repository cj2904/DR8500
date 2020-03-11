package RmsDownloadRecipeToEqp

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S7F1ProcessProgramLoadInquire
import de.znt.services.secs.dto.S7F3ProcessProgramSend
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsBinary
import de.znt.zsecs.composite.SecsDataItem
import de.znt.zsecs.composite.SecsNumberItem
import de.znt.zsecs.composite.SecsDataItem.ItemName
import de.znt.zsecs.sml.SecsItemType
import groovy.transform.TypeChecked
import sg.znt.pac.SgdConfig
import sg.znt.services.rms.RmsService

@Deo(description='''
Download recipe to equipment shared folder
''')
class RmsDownloadRecipeToEqp_1 {


    @DeoBinding(id="GuiLogger")
    private Log guiLogger = LogFactory.getLog(getClass())

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="RecipeId")
    private String recipeId

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="RmsService")
    private RmsService rmsService

    @DeoBinding(id="RequirePermission")
    private Boolean requirePermission

    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute() {
        if (rmsService == null) {
            logger.error("RMS is null, do not execute")
        }
        else {
            def rmsRecipe = recipeId
            def recipeFolderSeperator = SgdConfig.getRecipeFolderSeperator()
            def equipmentFolderPath = SgdConfig.getEquipmentFolderSeperator()
            if (rmsRecipe.indexOf(recipeFolderSeperator) > -1) {
                rmsRecipe = rmsRecipe.replace(recipeFolderSeperator, equipmentFolderPath)
            }
            
            def ppBody = rmsService.getRecipe(recipeId)
            if(ppBody != null) {
                if (requirePermission) {
                    S7F1ProcessProgramLoadInquire loadRequest = new S7F1ProcessProgramLoadInquire()
                    SecsAsciiItem secsPpId = new SecsAsciiItem(rmsRecipe)
                    loadRequest.getData().setPPID(secsPpId)
                    def defaultDataType = SecsDataItem.getDataType(ItemName.VID, SecsItemType.U4)
                    def dataType = SecsDataItem.getDataType("S7F1", defaultDataType)
                    SecsNumberItem dataItem = (SecsNumberItem) SecsDataItem.createDataItem(dataType, ppBody.length + "")
                    loadRequest.getData().setLENGTH(dataItem)

                    def loadReply = secsGemService.sendS7F1ProcessProgramLoadInquire(loadRequest)

                    if (loadReply.getPPGNT() != 0) {
                        throw new Exception("Failed to send S7F1 for '" + rmsRecipe + "'")
                    }
                }

                S7F3ProcessProgramSend request = new S7F3ProcessProgramSend()
                request.getData().setPPID(new SecsAsciiItem(rmsRecipe))

                //request.getData().setPPBODY(new SecsAsciiItem(new String(ppBody)))
                request.getData().setPPBODY(new SecsBinary(ppBody))

                def reply = secsGemService.sendS7F3ProcessProgramSend(request)

                if(reply.isAccepted())
                {
                    guiLogger.info("Download Recipe '" + rmsRecipe + "' Successful")
                }
                else
                {
                    throw new Exception("Download Recipe '" + rmsRecipe + "' Fail")
                }
            }
        }
    }
}