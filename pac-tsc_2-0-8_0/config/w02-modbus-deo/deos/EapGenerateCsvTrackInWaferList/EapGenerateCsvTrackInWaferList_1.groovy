package EapGenerateCsvTrackInWaferList

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.scheduling.concurrent.ForkJoinPoolFactoryBean

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.TscConfig
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
Generate cvs based on track in wafer list
''')
class EapGenerateCsvTrackInWaferList_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        W02TrackInLotRequest outbound = new W02TrackInLotRequest(inputXmlDocument)
        def lotId = outbound.getContainerName()
        def waferList = outbound.getLotTrackInWaferList()
        def filePath = TscConfig.getStringProperty("W02-Modbus.CSV.Output.Path", "")
        
        if(filePath == "")
        {
            logger.info("FilePath is not set, skip generate csv...")
            return
        }
        
        if(!filePath.startsWith("\\\\"))
        {
            filePath = "\\\\" + filePath
        }

        try 
        {
            File file = new File(filePath + "\\CASSETTEA_DATA.csv")
            logger.info("File path for CASSETTEA_DATA: $filePath ...")
            PrintWriter fileWriter = new PrintWriter(file)
            
            //---Fixed template format---
            fileWriter.println("-- Pat Data --,,")
            fileWriter.println("Index,Lot ID,Wafer ID")
            for(int i = 1; i <= 5; i++)
            {
                fileWriter.println("$i,,")
            }
            fileWriter.println("-- Lot Data --,,")
            fileWriter.println("Index,Lot ID,Wafer ID")
            //---End of Fixed template format---
            
            def waferNo = ""
            def waferId = ""
            def dataValue = ""
            def index = 1
            for (wafer in waferList)
            {
                 waferNo = wafer.getWaferNumber()
                 waferId = lotId + "-" + waferNo
                 dataValue = index + "," + lotId + "," + waferId
                 fileWriter.println(dataValue)
                 index++
            }
            fileWriter.flush()
            fileWriter.close()
            logger.info("CSV generate successful...")
        }
        catch (IOException e) 
        {
           e.printStackTrace()
           throw new Exception(e)
        }
    }
}