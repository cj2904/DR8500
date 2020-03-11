package MczSetEquipmentMaterialSetup

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.SetEquipmentMaterialsSetupRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl

@CompileStatic
@Deo(description='''
Set single material setup
''')
class MczSetEquipmentMaterialSetup_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CamstarService")
    private CCamstarServiceImpl camstarService
    
    @DeoBinding(id="EquipmentId")
    private String equipmentId
    
    @DeoBinding(id="MaterialLotName")
    private String materialLotName
    
    @DeoBinding(id="MaterialPartName")
    private String materialPartName
    
    @DeoBinding(id="MaterialPartNameRev")
    private String materialPartNameRev
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new SetEquipmentMaterialsSetupRequest(equipmentId)
        request.getInputData().setComments("");
        
        def details = request.getInputData().getDetails()
        def detailItem = details.addDetailsItem()
        detailItem.setMaterialLotName(materialLotName)
        detailItem.getMaterialPart().setName(materialPartName);
        detailItem.getMaterialPart().setRev(materialPartNameRev);
                
        def reply = camstarService.setEquipmentMaterialSetup(request)
        if(reply.isSuccessful())
        {
            logger.info(reply.getResponseData().toXmlString())
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
    }
}