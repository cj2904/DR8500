package EapStoreEqpWipDataValue

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.TscConfig
import sg.znt.pac.TscConstants
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.CWafer
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.material.WaferFilterByScribeIds
import sg.znt.pac.util.WinApiEqpUtil
import de.znt.pac.deo.annotations.*
import de.znt.util.HexString


@CompileStatic
@Deo(description='''
Save the wip data locally
''')
class EapStoreEqpWipDataValue_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="Equipment")
    private CEquipment equipment
    
    @DeoBinding(id="ParamMap")
    private Map<String, String> paramMap
    
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager materialManager

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def serviceType = paramMap.get("ServiceType")
        if (serviceType == null || serviceType.length()==0)
        {
            throw new Exception("Missing param 'ServiceType'!")
        }
        
        def lotId = paramMap.get("LotId")
        if (lotId == null || lotId.length()==0)
        {
            throw new Exception("Missing param 'LotId'!")
        }
        
        def wipData = paramMap.get("WipDataValue")
        if (wipData == null || wipData.length()==0)
        {
            throw new Exception("Missing param 'WipDataValue'!")
        }

        def waferId = paramMap.get("WaferId")
        
        def lot = materialManager.getCLot(lotId)
        
        def wipDataSet = WinApiEqpUtil.convertSubString2Map(wipData)
        
        if(waferId != null && waferId.length()>0)
        {
            updateWaferWipData(lot, waferId, serviceType, wipDataSet)
        }
        else
        {
            boolean batchWaferPatternFound = false
            if (TscConfig.useBatchWaferDataCollection())
            {
                for (wd in wipDataSet) 
                {
                    if (wd.getKey().indexOf("@")>-1)
                    {
                        batchWaferPatternFound = true
                        def waferItem = wd.getKey().split("@")
                        logger.info("Batch wafer pattern found '" + Arrays.toString(waferItem) + "' with value '" + wd.getValue() + "'")
                        def waferScribeId = waferItem[0]
                        def waferWipItemName = waferItem[1] 
                        Map<String, String> waferWipDataSet = new HashMap()
                        waferWipDataSet.put(waferItem[1], wd.getValue()) 
                        updateWaferWipData(lot, waferScribeId, serviceType, waferWipDataSet)
                    }
                }
            }
    
            if (!batchWaferPatternFound)
            {
                updateLotWipData(lot, serviceType, wipDataSet)
                def lotList = materialManager.getCLotList(new LotFilterAll())
                for (ll in lotList)
                {
                    if(!ll.getPropertyContainer().getBoolean(TscConstants.MATERIAL_ATTR_FIRST_LOT_IN_BATCH,false))
                    {
                        logger.info("Cloning wip data to lot '" + ll.getId() + "'...")
                        updateLotWipData(ll, serviceType, wipDataSet)
                    }
                }
            }            
        }
    }
    
    void updateLotWipData(CLot lot, String serviceType, Map<String, String> wipDataSet)
    {
        def wipDataItems = lot.getWipDataByEquipment(equipment.getSystemId()).getWipDataItems(serviceType)
        
        def es = wipDataSet.entrySet()
        for(Map.Entry wd in wipDataSet)
        {
            boolean found = false
            for(lotWd in wipDataItems)
            {
                String WinApiHexWipDataName = HexString.bufferToHex(String.valueOf(wd.getKey()).getBytes());
                String WinApiBufWipDataName = new String(HexString.hexToBuffer(WinApiHexWipDataName))
                
                String WDDOHexWipDataName = HexString.bufferToHex(String.valueOf(lotWd.getId()).getBytes("utf-8"));
                String WDDOBufWipDataName = new String(HexString.hexToBuffer(WDDOHexWipDataName),"utf-8")

                logger.info("WinApi RawWipDataName ["+ wd.getKey() + "] WinApiHexWipDataName ["+WinApiHexWipDataName+"] WinApiBufWipDataName ["+WinApiBufWipDataName+"]")
                logger.info("WDDO   RawWipDataName ["+ lotWd.getId() + "] WDDOHexWipDataName ["+WDDOHexWipDataName+"] WDDOBufWipDataName ["+WDDOBufWipDataName+"]")
            
                if(WinApiHexWipDataName.equalsIgnoreCase(WDDOHexWipDataName))
                {
                    logger.info("WipDataName Matched ["+ wd.getKey() + "]")
                    String value = String.valueOf(wd.getValue())
                    if(lotWd.getUomNotes().length()>0)
                    {
                        try 
                        {
                            double multiplyValue = Double.parseDouble(lotWd.getUomNotes())
                            double conversionValue = Double.parseDouble(value)
                            conversionValue = BigDecimal.valueOf(conversionValue).multiply(BigDecimal.valueOf(multiplyValue)).doubleValue()
                            logger.info("Conversion applied, value="+ value + ",multiplyValue=" + multiplyValue + ",conversionValue=" + conversionValue)
                            value = conversionValue + ""
                        } 
                        catch (Exception e) 
                        {
                            e.printStackTrace()
                        }
                        wd.setValue(value)                        
                    }
                    lotWd.setValue(value)
                    found = true
                    break
                }
            }
            if (!found)
            {
                logger.error("Wip item '" + wd.getKey() + "' not found in lot '" + lot.getId() + "!")
            }
        }
    }
    
    void updateWaferWipData(CLot lot, String waferScribeId, String serviceType, Map<String, String> wipDataSet)
    {
        CWafer wafer = null
        def waferList = lot.getWaferList(new WaferFilterByScribeIds(waferScribeId))
        if (waferList.size() == 0)
        {
            throw new Exception("Cannot find wafer '" + waferScribeId + "'!")
        }
        else
        {
            wafer = waferList.get(0)
        }
        def wipDataItems = wafer.getWipData().getWipDataItems(serviceType)
        
        def es = wipDataSet.entrySet()
        for (Map.Entry wd in wipDataSet)
        {
            boolean found = false
            for (waferWd in wipDataItems) 
            {
                if (waferWd.getId().equalsIgnoreCase(String.valueOf(wd.getKey())))
                {
                    String value = String.valueOf(wd.getValue())
                    if(waferWd.getUomNotes().length()>0)
                    {
                        try
                        {
                            double multiplyValue = Double.parseDouble(waferWd.getUomNotes())
                            double conversionValue = Double.parseDouble(value)
                            conversionValue = BigDecimal.valueOf(conversionValue).multiply(BigDecimal.valueOf(multiplyValue)).doubleValue()
                            logger.info("Conversion applied, value="+ value + ",multiplyValue=" + multiplyValue + ",conversionValue=" + conversionValue)
                            value = conversionValue + ""
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace()
                        }
                    }
                    waferWd.setValue(value)
                    found = true
                    break
                }
            }
            if (!found)
            {
                logger.error("Wip item '" + wd.getKey() + "' not found in wafer '" + wafer.getWaferScribeID() + "'!")
            }
        }
    }
}