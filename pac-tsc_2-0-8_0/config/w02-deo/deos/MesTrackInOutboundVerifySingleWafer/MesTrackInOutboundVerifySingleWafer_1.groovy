package MesTrackInOutboundVerifySingleWafer

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.material.WaferFilterAll
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Check only one wafer is allowed to track in
''')
class MesTrackInOutboundVerifySingleWafer_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager materialManager
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outboundRequest = new W02TrackInLotRequest(inputXmlDocument)
        def eqpId = outboundRequest.getResourceName()
        def lotId = outboundRequest.getContainerName()
        def lotCount = PacUtils.valueOfInteger(outboundRequest.getEquipmentLotCount(), 0)
        if (lotCount>=1)
        {
            def wafers = getProcessingWafer(eqpId)
            throw new Exception("There is wafer [$wafers] pending for track out! Please track out the wafers first!")
        }
        if (outboundRequest.getLotTrackInWaferList().size()>1)
        {
            throw new Exception("Only one wafer is allowed to track in!")
        }
        
        def wafers = getProcessingWafer(eqpId)
        if (wafers.length()>0)
        {
            throw new Exception("There is wafer [$wafers] pending for track out! Please track out the wafers first!")
        }
    }
    
    private String getProcessingWafer(String eqpId)
    {
        def wafers = ""
        try 
        {
            def lotList = materialManager.getCLotList(new LotFilterAll())
            for (lot in lotList) 
            {
                def wafersList = lot.getWaferList(new WaferFilterAll())
                for (wafer in wafersList) 
                {
                    if (wafer.getEquipmentId().equalsIgnoreCase(eqpId))
                    {
                        if (wafers.length()>0)
                        {
                            wafers = wafers + ","
                        }
                        wafers = wafers + wafer.getWaferScribeID()
                    }                
                }
            }            
        } 
        catch (Exception e) 
        {
            e.printStackTrace()
        }
        return wafers        
    }
}