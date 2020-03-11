package EczImportEquipmentConstants

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.util.SecsCharacterizationUtil
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F13EquipmentConstantRequest
import de.znt.services.secs.dto.S2F14EquipmentConstantData
import de.znt.services.secs.dto.S2F29EquipmentConstantNamelistRequest
import de.znt.zsecs.composite.SecsDataItem
import de.znt.zsecs.composite.SecsNumberItem
import de.znt.zsecs.composite.SecsDataItem.ItemName
import eqp.EczUtil

@Deo(description='''
Upload and import equipment constants
''')
class EczImportEquipmentConstants_1 
{
    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="OutputFile")
    private String outputFile

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        SecsCharacterizationUtil util = new SecsCharacterizationUtil()
        def request = new S2F29EquipmentConstantNamelistRequest()
        def response = secsGemService.sendS2F29EquipmentConstantNamelistRequest(request)
        if (outputFile.length()>0)
        {
            File file = new File(outputFile);
            file.getParentFile().mkdirs()
            logger.info("Saving data to file '" + file.getAbsolutePath() + "'")
            FileOutputStream fos = new FileOutputStream(file);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            
            def outputFileImport = outputFile + "_Import"
            File fileImport = new File(outputFileImport);
            FileOutputStream fosImport = new FileOutputStream(fileImport);
            BufferedWriter bwImport = new BufferedWriter(new OutputStreamWriter(fosImport));
            
            try
            {
                bw.write("VID;Name;DataType;Value;\n")
                def list = response.getEquipmentConstantList()

                for (int i = 0; i< list.getSize(); i ++)
                {
                    def v = list.getEquipmentConstant(i)
                    try
                    {
                        Number vid = ((SecsNumberItem)v.getECID()).getNumber(0)

                        def ecid = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(vid)))
                        def s2F13Request = new S2F13EquipmentConstantRequest(ecid)
                        S2F14EquipmentConstantData result = secsGemService.sendS2F13EquipmentConstantRequest(s2F13Request)
                        int totalSize = result.getData().getSize()
                        for(int j=0;j<totalSize;j++)
                        {
                            def ec = result.getData().getECV(j)
                            def valueToOutput = ""
                            String dataType = util.getSecsDataType(ec)
                            if(valueToOutput.length()>0)
                            {
                                valueToOutput = valueToOutput + ","
                            }
                            valueToOutput = valueToOutput + util.getVariableData(ec)

                            def value = vid + ";" + EczUtil.removeXmlKeywork(v.getECName()) + ";" + dataType + ";" + valueToOutput
                            def valueImport = vid + ";" + EczUtil.removeXmlKeywork(v.getECName()) + ";" + dataType
                            logger.info(value)
                            bw.write(value + "\n");
                            bwImport.write(valueImport + "\n");
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace()
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace()
            }
            finally
            {
                bw.close()
                fos.close();
                bwImport.close()
                fosImport.close();
            }
            
            util.importEquipmentConstant(secsGemService, outputFileImport, (char)';')
        }        
    }   
}