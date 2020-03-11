package EczUploadStatusVariables

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

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
class EczUploadStatusVariables_1
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

            try
            {
                bw.write("VID;DataType;Name;Value;\n")

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
                            String dataType = EczUtil.getSecsDataType(sv)
                            if(valueToOutput.length()>0)
                            {
                                valueToOutput = valueToOutput + ","
                            }
                            valueToOutput = valueToOutput + EczUtil.getVariableData(sv)

                            def value = vid + ";" + EczUtil.removeXmlKeywork(v.getSVName()) + ";" + dataType + ";" + valueToOutput
                            logger.info(value)
                            bw.write(value + "\n");
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
            }
        }
    }
}