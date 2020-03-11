package EapSendSpcDataCsvFile

import groovy.transform.CompileStatic

import java.text.SimpleDateFormat

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.TscConfig
import sg.znt.pac.TscConstants
import sg.znt.pac.domainobject.SequenceParameter
import sg.znt.pac.domainobject.SequenceParameterManager
import sg.znt.pac.domainobject.WipDataDomainObject
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.domainobject.WipDataDomainObjectSet
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest
import de.znt.pac.deo.annotations.*
import de.znt.pac.domainobject.filter.FilterAllDomainObjects

@CompileStatic
@Deo(description='''
Export SPC data to file 
''')
class EapSendSpcDataCsvFile_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataDomainObjectManager

    @DeoBinding(id="SequenceParameterManager")
    private SequenceParameterManager sequenceParameterManager

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="InputXml")
    private String inputXml
    
    private CLot sendlot

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def spcCvsFilePath = TscConfig.getStringProperty("SPC.CSV.File.Path", "SPC/")
        def spcCvsSeperator = TscConfig.getStringProperty("SPC.CSV.File.Seperator", "\t")
        
        def outbound = new W02CompleteOutLotRequest(inputXml)
       
		if (outbound.isCancelTrackIn())
		{
			logger.info("Is cancel track in do not perform Export SPC data to file.")
			return
		}
		
        def lotList = outbound.getLotList()
        if (lotList.size() == 0)
        {
            def lotId = outbound.getContainerName()
            sendSpcData(spcCvsFilePath, spcCvsSeperator, lotId)
        }
        else
        {
            for (lotId in lotList) 
            {
                sendSpcData(spcCvsFilePath, spcCvsSeperator, lotId)
            }
        }
    }
    
    private void sendSpcData(String spcCvsFilePath, String spcCvsSeperator, String lotId)
    {
        
        def lot = cMaterialManager.getCLot(lotId)
        
        if (!lot.getPropertyContainer().getBoolean(TscConstants.MATERIAL_ATTR_FIRST_LOT_IN_BATCH,true))
        {
            logger.info("Skip spc file send since the lot '" + lot.getId() + "' is not first in the batch")
            return
        }
        
        sendlot = lot

        if(sendlot!=null)
        {
            String spcHeader = ""
            String spcHeader2 = TscConfig.getStringProperty("SPC.Column.List", "")
            String spcHeader3 = ""
            String spcHeader4 = TscConfig.getStringProperty("SPC.Header.List", "")
            String spcData = ""
            String wipDataString = ""
            
            // RETRIVE THE SEND LOT WIP DATA
            String wipservicetype = WipDataDomainObject.SERVICE_TYPE_TRACK_OUT_WIP_DATA
            Map<String, Object> wipParamSet = new HashMap<String, Object>();
            List<WipDataDomainObjectSet> wddb = wipDataDomainObjectManager.getAllDomainObject()
            for(WipDataDomainObjectSet wds : wddb)
            {
                if(wds.getLotId().equals(sendlot.getId()))
                {
                    List<WipDataDomainObject> wdi = wds.getAll(new FilterAllDomainObjects())
                    for(WipDataDomainObject wd : wdi)
                    {
                        if (wd.isHidden() && wd.getServiceType().equalsIgnoreCase(wipservicetype))
                        {
                            if(wd.getValue().length()==0)
                            {
                                if(TscConfig.getBooleanProperty("Spc.FullData.Send", false))
                                {
                                    logger.error("Cancel Send Csv File As Wip Data ["+wd.getId()+"] Not Complete.")
                                    return
                                }
                            }
                            
                            
                            if(spcHeader4.length()>0)
                            {
                                spcHeader4 = spcHeader4 + spcCvsSeperator
                            }
                            spcHeader4 = spcHeader4 + wd.getId()
                            
                            if(wipDataString.length()>0)
                            {
                                wipDataString = wipDataString + spcCvsSeperator
                            }
                            wipDataString = wipDataString + wd.getValue()
                            wipParamSet.put(wd.getId(), wd.getValue())
                        }
                        else
                        {
                            logger.info("Wip data '" + wd.getId() + "' is " + wd.isHidden() + " and " + wd.getServiceType() + ", skip spc send")
                        }
                    }
                }
            }
            
            // STORE WIPDATA IN LOT SEQUENCE SET
            String seqSetId = sendlot.getEquipmentId() + "-" +sendlot.getId() + "-" + TscConstants.SPC_HEADER_NAME
            String seqSetDataId = sendlot.getEquipmentId() + "-" +sendlot.getId() + "-" + TscConstants.SPC_DATA_NAME
            def seqSet = sequenceParameterManager.getDomainObject(seqSetId)
            def seqDataSet = sequenceParameterManager.getDomainObject(seqSetDataId)
            
            if(seqSet!=null)
            {
                // FILL SEQUENCE SET HEADER TO FILE BUFFER
                def seqParamList = seqSet.getAll(new FilterAllDomainObjects())
                for (seq in seqParamList)
                {
                    if(spcHeader.length()>0)
                    {
                        spcHeader = spcHeader + spcCvsSeperator
                    }
                    spcHeader = spcHeader + seq.getParamValue()
                }
            }

            // FILL SEQUENCE SET HEADER TO FILE BUFFER

            // FILL SEQUENCE SET DATA TO FILE BUFFER
            
            int headerCount = 0
            if(seqDataSet!=null)
            {
                // FILL SEQUENCE SET HEADER TO FILE BUFFER
                def seqParamList = seqDataSet.getAll(new FilterAllDomainObjects())
                for (seq in seqParamList)
                {
                    if(spcData.length()>0)
                    {
                        spcData = spcData + spcCvsSeperator
                    }
                    spcData = spcData + seq.getParamValue()
                    def paramName = seq.getParamName()
                    if (!paramName.equalsIgnoreCase("Date") && !paramName.equalsIgnoreCase("Time"))
                    {
                        headerCount = headerCount + 1
                    }
                }
            }
            spcHeader3 = headerCount + spcCvsSeperator+wipParamSet.size()
            
            if(wipParamSet.size() == 0)
            {
                logger.error("Cancel Send Csv File As no wip data is found!")
                return
            }
            if(spcData.length()>0)
            {
                spcData = spcData + spcCvsSeperator
            }
            spcData = spcData + wipDataString
            logger.info("spcHeader ["+spcHeader+"]")
            logger.info("spcHeader2 ["+spcHeader2+"]")
            logger.info("spcHeader3 ["+spcHeader3+"]")
            logger.info("spcHeader4 ["+spcHeader4+"]")
            logger.info("spcData ["+spcData+"]")

            String fileName = sendlot.getEquipmentId() + "_" + sendlot.getId() + "_" + new SimpleDateFormat("yyyyMMddhhmmssss").format(System.currentTimeMillis()) + ".csv"
            OutputStreamWriter osw = null;
            try
            {
                String fullPath = spcCvsFilePath + fileName
                File file = new File(fullPath)
                file.getParentFile().mkdirs()
                logger.info("File Full Path " + file.getAbsolutePath())
                //osw = new OutputStreamWriter(new FileOutputStream(file), "UTF8")
                osw = new OutputStreamWriter(new FileOutputStream(file), TscConfig.getStringProperty("SPC.File.Encoding", "Big5"))
                osw.write(spcHeader + "\r\n")
                osw.write(spcHeader2 + "\r\n")
                osw.write(spcHeader3 + "\r\n")
                osw.write(spcHeader4 + "\r\n")
                osw.write(spcData + "\r\n")
            }
            catch (Exception e)
            {
                logger.info("FileWriter Write File Exception!");
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    osw.flush();
                    osw.close();
                }
                catch (IOException e)
                {
                    logger.info("FileWrite Flushing/Closing Exception!");
                    e.printStackTrace();
                }
            }
            
        }
    }
}