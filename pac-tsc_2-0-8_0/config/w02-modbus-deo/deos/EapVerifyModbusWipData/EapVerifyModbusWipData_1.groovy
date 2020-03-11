package EapVerifyModbusWipData

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.TscConfig
import sg.znt.pac.TscConstants
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import OutboundRequest.CommonOutboundRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EapVerifyModbusWipData_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    
    @DeoBinding(id="MainEquipment")
    private CEquipment equipment
    
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new CommonOutboundRequest(inputXmlDocument)
        def lotId = request.getContainerName()
        def cLot = null
        
        try 
        {
            cLot = cMaterialManager.getCLot(lotId)
        } 
        catch (Exception e) 
        {
            e.printStackTrace()
        }
        if (cLot != null)
        {
            def eqId = cLot.getEquipmentId()
            
            def wipData = cLot.getWipDataByEquipment(eqId)
            if(wipData==null)
            {
                logger.info("Lot get wip data return null.")
                return
            }
            
            def moveOutWipData = wipData.getMoveOutWipDataItems()
            
            for (wipDataItem in moveOutWipData)
            {
                
                def tempPattern = TscConfig.getStringProperty("WipDataMultipleTemperatureRegExp","T_.*[0-9]")
                def waferPattern = TscConfig.getStringProperty("WipDataMultipleWaterRegExp","W_.*[0-9]")
                
                if(wipDataItem.getId().matches(tempPattern) && wipDataItem.getId().endsWith("1"))
                {
                    wipDataItem.setIsHidden(true)
                    if(wipDataItem.getValue()==null || wipDataItem.getValue().length()==0)
                    {
                        throw new Exception("Wip data '" + wipDataItem.getId() + "' is empty!")
                    }
                }
                else if(wipDataItem.getId().matches(waferPattern) && wipDataItem.getId().endsWith("1"))
                {
                    wipDataItem.setIsHidden(true)
                    if (cLot.getPropertyContainer().getBoolean(TscConstants.MATERIAL_ATTR_FIRST_LOT_IN_BATCH,true))
                    {
                        if(wipDataItem.getValue()==null || wipDataItem.getValue().length()==0)
                        {
                            throw new Exception("Wip data '" + wipDataItem.getId() + "' is empty!")
                        }
                    }
                }
            
            }
        } 
    }
}