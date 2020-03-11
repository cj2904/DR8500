package EczUploadFormattedRecipe

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S7F25FormattedProcessProgramRequest
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsComponent
import de.znt.zsecs.composite.SecsComposite

@Deo(description='''
Fire S7F25 request formatted recipe from equipment
''')
class EczUploadFormattedRecipe_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="Path")
    private String path

    @DeoBinding(id="RecipeId")
    private String recipeId

    /**
     *
     */
    @DeoExecute()
    public void execute() {
        def request = new S7F25FormattedProcessProgramRequest(new SecsAsciiItem(recipeId))
        def response = secsGemService.sendS7F25FormattedProcessProgramRequest(request)

        Properties formattedPP = new Properties()
        def commands = response.getData().getProcessCommands()
        for (int i = 0; i < commands.getSize(); i++) {
            def command = commands.getProcessCommand(i)
            def ccode = getVariableData(command.getCCode())

            def parameters = command.getParameters()
            String value = null
            for (int j = 0; j < parameters.getSize(); j++) {
                if(value == null) {
                    value = getVariableData(parameters.getPParm(j))
                    continue
                }
                value = value + "," + getVariableData(parameters.getPParm(j))
            }

            formattedPP.setProperty(ccode, value)
        }
        def fileName = path + "\\" + recipeId
        savePropertiesFile(fileName, formattedPP)
        logger.info("Formatted recipe $recipeId is uploaded to $fileName successfully")
    }

    public void savePropertiesFile(String propertiesFileName, Properties prop)
    throws IOException {
        FileOutputStream fos = null
        try {
            File file = new File(propertiesFileName);
            file.getParentFile().mkdirs();
            fos = new FileOutputStream(file);

            try {
                prop.store(fos, "Formatted recipe");
            }
            finally {
                fos.close();
            }
        }
        catch (IOException e) {
            throw e;
        }
    }

    public String getVariableData(SecsComponent<?> data) {
        String statusReply = ""
        List<SecsComponent> valueList = data.getValueList()
        for (var in valueList) {
            if(statusReply.length()>0) {
                statusReply = statusReply + ","
            }
            if (var instanceof SecsComposite) {
                statusReply = statusReply + getVariableData(var);
            }
            else if (var instanceof SecsComponent<?>) {
                def vl = var.getValueList()
                for (nv in vl) {
                    statusReply = statusReply + nv;
                }
            }
            else {
                statusReply = statusReply + var;
            }
        }
        return statusReply;
    }
}