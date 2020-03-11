package EczImportAllEventFromVid

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.util.SecsCharacterizationUtil
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.SecsService
import de.znt.services.secs.dto.S1F3SelectedEquipmentStatusRequest
import de.znt.services.secs.dto.S1F4SelectedEquipmentStatusData
import de.znt.zsecs.composite.SecsComponent
import de.znt.zsecs.composite.SecsComposite
import de.znt.zsecs.composite.SecsDataItem
import de.znt.zsecs.composite.SecsNumberItem
import de.znt.zsecs.composite.SecsDataItem.ItemName
import eqp.EczUtil


@CompileStatic
@Deo(description='''
Import event from VID to pac
''')
class EczImportAllEventFromVid_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="SecsService")
    private SecsGemService secsService

    @DeoBinding(id="StatusVariables")
    private String vid

    @DeoBinding(id="OutputFile")
    private String outputFile

    /**
     *
     */
    @DeoExecute
    public void execute() {
        String status = ""

        if (outputFile.length()>0) {
            S1F3SelectedEquipmentStatusRequest request = new S1F3SelectedEquipmentStatusRequest()
            SecsComponent< ? > svid = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(vid)))
            request.addSVID(svid)

            S1F4SelectedEquipmentStatusData s1F4Reply = secsService.sendS1F3SelectedEquipmentStatusRequest(request)

            File file = new File(outputFile);
            file.getParentFile().mkdirs()
            logger.info("Saving data to file '" + file.getAbsolutePath() + "'")
            FileOutputStream fos = new FileOutputStream(file);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            def runCount = 1;
            try 
            {
                def dataSize = s1F4Reply.getData().getSize()
                for(int i=0;i<dataSize;i++) {
                    if(status.length()>0) {
                        status = status + ","
                    }
                    SecsComponent<?> data = s1F4Reply.getData().getSV(i)
                    if(data instanceof SecsComposite) {
                        List<SecsComponent> valueList = ((SecsComposite)data).getValueList()
                        for (ceid in valueList) {
                            if (ceid instanceof SecsNumberItem) {
                                def ceidLIst = ((SecsNumberItem) ceid).getValueList()
                                for (ceidSub in ceidLIst) {
                                    logger.info("Event ID: " + ceidSub)
                                    String dataValue = ceidSub.toString() + ";Event_$ceidSub;;"
                                    logger.info(dataValue)
                                    bw.write(dataValue + "\n");
                                    runCount= runCount +1;
                                }
                            }
                        }
                    }

                    def value = EczUtil.getVariableData(data)
                    status = status + value
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
            SecsCharacterizationUtil util = new SecsCharacterizationUtil()    
            util.importEvent((SecsService)secsService, outputFile, ";".charAt(0))
            
        }

        logger.info("Get status variable : '" + vid + "', value is : '" + status + "'")
    }
}