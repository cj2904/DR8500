package ProberEapSendCassetteDataCommand

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsComposite
import de.znt.zsecs.composite.SecsU4Item
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>pac to send CST_DATA to equipment</b>
''')
class ProberEapSendCassetteDataCommand_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    
    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment
    
    private SecsGemService secsGemService
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        secsGemService = (SecsGemService) cEquipment.getExternalService()
        
        W02TrackInLotRequest trackInLot = new W02TrackInLotRequest(inputXmlDocument)
        def lotId = trackInLot.getContainerName()
        def waferList = trackInLot.getTrackInWaferList()
        def testerRecipe = "tester" //TODO: fill in tester recipe
        def proberRecipe = "prober" //TODO: fill in prober recipe
        
        S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("CST_DATA"))

        SecsComposite patDataList = new SecsComposite()
        SecsComposite lotDataList = new SecsComposite()
        int count = 1
        for (wafer in waferList) 
        {
            SecsComposite innerList = new SecsComposite()
            innerList.add(new SecsU4Item(count))
            innerList.add(new SecsAsciiItem(lotId))
            innerList.add(new SecsAsciiItem(wafer.getWaferContainer()))
            innerList.add(new SecsAsciiItem(testerRecipe))
            innerList.add(new SecsAsciiItem(proberRecipe))
            
            patDataList.add(innerList)
            lotDataList.add(innerList)
            
            count++
        }
        
        request.addParameter(new SecsAsciiItem("PAT_DATA"), patDataList)
        request.addParameter(new SecsAsciiItem("LOT_DATA"), lotDataList)
        
        def reply = secsGemService.sendS2F41HostCommandSend(request)
        if (reply.isCommandAccepted())
        {
            //ok
        }
        else
        {
            throw new Exception("Executing remote command CST_DATA failed, reply message: " + reply.getHCAckMessage())
        }
    }
}