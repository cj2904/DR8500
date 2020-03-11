package EczImportStatusVariables

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.util.SecsCharacterizationUtil
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S1F11StatusVariableNamelistRequest
import de.znt.services.secs.dto.S1F3SelectedEquipmentStatusRequest
import de.znt.zsecs.composite.SecsDataItem
import de.znt.zsecs.composite.SecsNumberItem
import de.znt.zsecs.composite.SecsDataItem.ItemName
import eqp.EczUtil

@Deo(description='''
Upload status variables from machine
''')
class EczImportStatusVariables_1 
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
        
        def request = new S1F11StatusVariableNamelistRequest()
        def reply =  secsGemService.sendS1F11StatusVariableNamelistRequest(request)
        if (outputFile.length()>0)
        {
            def statusVariableList = reply.getStatusVariableList()
            File file = new File(outputFile);
            file.getParentFile().mkdirs()
            logger.info("Saving data to file '" + file.getAbsolutePath() + "'")
            
            FileOutputStream fos = new FileOutputStream(file);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            def importFileName = outputFile + "_Import"
            File fileImport = new File(importFileName);
            FileOutputStream fosImport = new FileOutputStream(fileImport);
            BufferedWriter bwImport = new BufferedWriter(new OutputStreamWriter(fosImport));
            
            String eventEnabledList = ""
            try
            {
                bw.write("VID;Name;DataType;Value;\n")
                
                for (int i = 0; i< statusVariableList.getSize(); i ++)
                {
                    def v = statusVariableList.getStatusVariable(i)
                    try
                    {
                        Number vid = ((SecsNumberItem)v.getSVID()).getNumber(0)
                        def svid = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(vid)))
                        def s1f3Request = new S1F3SelectedEquipmentStatusRequest(svid)
                        def result = secsGemService.sendS1F3SelectedEquipmentStatusRequest(s1f3Request)
                        int totalSize = result.getData().getSize()
                        for(int j=0;j<totalSize;j++)
                        {
                            def sv = result.getData().getSV(j)
                            def valueToOutput = ""
                            String dataType = util.getSecsDataType(sv)
                            if(valueToOutput.length()>0)
                            {
                                valueToOutput = valueToOutput + ","
                            }
                            valueToOutput = valueToOutput + util.getVariableData(sv)

                            def value = vid + ";" + EczUtil.removeXmlKeywork(v.getSVName()) + ";" + dataType + ";" + valueToOutput
                            def valueImport = vid + ";" + EczUtil.removeXmlKeywork(v.getSVName()) + ";" + dataType
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
            }
            catch (Exception e)
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
            
            try
            {
                util.importStatusVariable(secsGemService, importFileName, (char)';')
            }
            catch (Exception e)
            {
                e.printStackTrace()
            }
        }
    }   
}