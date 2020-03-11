package MesTioSelectRecipe

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import sg.znt.services.zwin.TscZWinApiServiceImpl
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem

@CompileStatic
@Deo(description='''
Select recipe for the machine
''')
class MesTioSelectRecipe_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    
    @DeoBinding(id="MainEquipment")
    private CEquipment equipment
    
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager materialManager
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outboundRequest = new W02TrackInLotRequest(inputXmlDocument)
        def lotId = outboundRequest.getContainerName()
        def lot = materialManager.getCLot(lotId)
        
        def recipeName = ""
        if(equipment.getRecipeObject()!=null)
        {
            recipeName = equipment.getRecipeObject().getRecipeName()
        }
        else
        {
            throw new Exception("No recipe to select!")
        } 
        
        TscZWinApiServiceImpl zwinApi = (TscZWinApiServiceImpl) equipment.getExternalService()
        
        def request = new S2F41HostCommandSend(new SecsAsciiItem("SelectRecipe"))
        request.addParameter(new SecsAsciiItem("LotId"), new SecsAsciiItem(lotId))
        request.addParameter(new SecsAsciiItem("Recipe"), new SecsAsciiItem(lot.getRecipe()))
        
        def reply = zwinApi.sendS2F41HostCommandSend(request)
    }
}