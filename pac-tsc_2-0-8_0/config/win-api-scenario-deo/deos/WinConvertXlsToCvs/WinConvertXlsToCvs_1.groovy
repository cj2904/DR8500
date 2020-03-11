package WinConvertXlsToCvs

import groovy.transform.CompileStatic
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile
import jcifs.smb.SmbFileInputStream
import jcifs.smb.SmbFileOutputStream

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.io.excel.ExcelParser
import sg.znt.pac.machine.CEquipment
import sg.znt.services.jcifs.ArchiveFileService
import sg.znt.services.jcifs.ArchiveUserAccount
import sg.znt.services.zwin.ZWinApiServiceImpl
import de.znt.pac.crypto.PacCryptor
import de.znt.pac.deo.annotations.*
import elemental.json.JsonObject

@CompileStatic
@Deo(description='''
Convert the xls to csv file for gateway parsing
''')
class WinConvertXlsToCvs_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="ParamMap")
    private Map<String, String> paramMap

    @DeoBinding(id="Equipment")
    private CEquipment equipment
    
    @DeoBinding(id="EventId")
    private String eventTid
    
    @DeoBinding(id="EventName")
    private String eventName

    /**
     *
     */
    @DeoExecute(result="Reply")
    public JsonObject execute()
    {
        ZWinApiServiceImpl winApiService = (ZWinApiServiceImpl) equipment.getExternalService()
        
        def sourceFilePath = paramMap.get("SourceFilePath")
        if (sourceFilePath == null || sourceFilePath.length()==0)
        {
            throw new Exception("Missing param 'SourceFilePath'!")
        }
        
        def destFilePath = paramMap.get("DestFilePath")
        if (destFilePath == null || destFilePath.length()==0)
        {
            throw new Exception("Missing param 'DestFilePath'!")
        }
        
        def winApiGateWayDir = winApiService.getWinApiGatewayDirectory()
        sourceFilePath = winApiGateWayDir + sourceFilePath
        destFilePath = winApiGateWayDir + destFilePath
        logger.info("sourceFilePath1: " + sourceFilePath)
        if(winApiGateWayDir.startsWith("smb"))
        {
            sourceFilePath = sourceFilePath.replaceAll("\\\\", "/")
            destFilePath = destFilePath.replaceAll("\\\\", "/")
        }
        //sourceFilePath = "smb://10.0.8.67/WinApiGateway/FileStorage/MeasurementData.xlsx"
        //destFilePath = "smb://10.0.8.67/WinApiGateway/FileStorage/data.xlsx"
        InputStream fromStream = null;
        OutputStream toStream = null;
        
        try 
        {
            def fileService = winApiService.getEquipmentFileService()
            def machineUserAccount = getUserAccount(fileService, "Local")
            
            def userName = machineUserAccount.getUserId()
            def password = PacCryptor.decodeBase64(machineUserAccount.getPassword())
            logger.info("UserName: " + userName + "|" + password)
            logger.info("sourceFilePath: " + sourceFilePath + "|" + destFilePath)
            def authentication = new NtlmPasswordAuthentication(userName + ":" + password);
            SmbFile from = new SmbFile(sourceFilePath, authentication);
            fromStream = new SmbFileInputStream(from);
            
            def to = new SmbFile(destFilePath, authentication);
            
            def excelParser = new ExcelParser(fromStream, sourceFilePath.indexOf(".xlsx")!=-1)
            def rowDataList = excelParser.readAllRowData(0)
            def data = ""
            for (row in rowDataList) 
            {
                def rowData = row.getRowData()
                if (data.length()>0)
                {
                    data = data + "\n"
                }
                for (info in rowData) 
                {
                    data = data + info.toString() + ","
                }
            }
            toStream = new SmbFileOutputStream(to);
            toStream.write(data.getBytes())
        }
        finally
        {
            if (fromStream != null)
            {
                fromStream.close()
            }
            if (toStream != null)
            {
                toStream.close()
            }
        } 
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("SourceFilePath",paramMap.get("SourceFilePath"))
        param.put("DestFilePath", paramMap.get("DestFilePath"))
        
        return winApiService.buildEventReplyMessage(eventTid, eventName, param, "", "");
    }
    
    private ArchiveUserAccount getUserAccount(ArchiveFileService fileService, String accountType)
    {
        def userAccounts = fileService.getUserAccount()
        for (acc in userAccounts)
        {
            if(acc.getAccountType().equals(accountType))
            {
                return acc
            }
        }
        
        return null
    }
}