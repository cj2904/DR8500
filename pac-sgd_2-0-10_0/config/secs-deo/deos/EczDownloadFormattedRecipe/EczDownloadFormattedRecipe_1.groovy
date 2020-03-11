package EczDownloadFormattedRecipe

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.error.DeoExecutionException
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S7F1ProcessProgramLoadInquire
import de.znt.services.secs.dto.S7F23FormattedProcessProgramSend
import de.znt.services.secs.dto.S7F23FormattedProcessProgramSendDto.Data
import de.znt.util.error.ErrorManager
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsComponent
import de.znt.zsecs.composite.SecsDataItem
import de.znt.zsecs.composite.SecsDataItem.ItemName
import de.znt.zsecs.sml.SecsItemType

@Deo(description='''
Download formatted recipe parameter to equipment
''')
class EczDownloadFormattedRecipe_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="RecipeId")
    private String recipeId

    @DeoBinding(id="Path")
    private String path

    /**
     *
     */
    @DeoExecute
    public void execute() {
        /**
        S7F1ProcessProgramLoadInquire loadRequest = new S7F1ProcessProgramLoadInquire()
        SecsAsciiItem secsPpId = new SecsAsciiItem(recipeId)
        loadRequest.getData().setPPID(secsPpId)
        def dataType = SecsDataItem.getDataType("S7F1", SecsDataItem.getDataType(ItemName.VID, SecsItemType.U4))
        loadRequest.getData().setLENGTH(SecsDataItem.createDataItem(dataType, getRecipe().length + ""))

        def loadReply = secsGemService.sendS7F1ProcessProgramLoadInquire(loadRequest)

        if (loadReply.getPPGNT() != 0) {
            throw new DeoExecutionException("Failed to send S7F1")
        }
        **/

        Data data = new Data()
        data.setPpId(new SecsAsciiItem(recipeId))
        //		data.setMdln("")
        //		data.setSoftRev("")

        def prop = loadPropertiesFile(path + '\\' + recipeId)
        def formattedEs = prop.entrySet()
        for (entry in formattedEs)
        {
            def param = data.getProcessCommands().addProcessCommand()
            param.setCCode(new SecsAsciiItem(entry.getKey()))
            param.getParameters().addPParm(new SecsAsciiItem(entry.getValue()))
        }

        S7F23FormattedProcessProgramSend request = new S7F23FormattedProcessProgramSend(data)
        def response = secsGemService.sendS7F23FormattedProcessProgramSend(request)

        SecsComponent<?> ackc7Component = response.getAckC7Component()
        if (ackc7Component == null || ackc7Component.getSize() == 0)
        {
            throw new DeoExecutionException("Download Formatted Recipe Fail")
        }
        else
        {
            logger.info("Download Formatted Recipe Successful")
        }
    }
    
    public byte[] getRecipe()
    throws Exception {
        String fileName = path + "\\" + recipeId;
        File file = new File(fileName);
        if (file.exists()) {
            logger.info("Request recipe '" + recipeId + "' from '" + file.getAbsolutePath() + "'");
            byte[] b = new byte[(int) file.length()];
            FileInputStream is = new FileInputStream(file);
            is.read(b);
            is.close();

            return b;
        }
        else {
            logger.error("Recipe file '" + fileName + " not found!");
            throw new Exception("Recipe '$fileName' not found!");
        }
    }

    Properties loadPropertiesFile(String fileName)
    {
        // try to open file
        Properties props = new Properties()
        InputStream is = null
        try
        {
            logger.info(">>>Loading " + fileName);
            is = getPropertiesInputStream(fileName);
            // load properties
            if (is != null)
            {
                props.load(is)
                is.close();
            }
            else
            {
                logger.error("ERROR: loading URL " + fileName + " failed");
            }
        }
        catch (IOException i)
        {
            ErrorManager.handleError(i, this);
            logger.error("ERROR: loading property URL " + fileName + " failed");
        }
        return props;
    }

    InputStream getPropertiesInputStream(String fileName)
    {
        try
        {
            InputStream is = new FileInputStream(fileName);
            logger.info("Loaded as file  " + fileName);
            return is;
        }
        catch (FileNotFoundException e)
        {
            logger.info("Failed to load '" + fileName + "' as File (" + e.getClass().getName() + ")");
        }

        return null;
    }

}