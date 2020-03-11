package MczSetEquipmentSetup

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.SetEquipmentSetupRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.CCamstarServiceImpl

@CompileStatic
@Deo(description='''
Set equipment setup
''')
class MczSetEquipmentSetup_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CamstarService")
    private CCamstarServiceImpl camstarService
    
    @DeoBinding(id="EquipmentId")
    private String equipmentId
    
    @DeoBinding(id="ToolPlan")
    private String toolPlan
    
    @DeoBinding(id="Recipe")
    private String recipe
    
    @DeoBinding(id="RecipeRev")
    private String recipeRev
    
    @DeoBinding(id="ToolItemName")
    private String toolItemName
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new SetEquipmentSetupRequest()
        request.getInputData().setEquipment(equipmentId)
        request.getInputData().setResource(equipmentId)
        request.getInputData().setToolPlan(toolPlan)
        request.getInputData().setProcessType(CCamstarService.DEFAULT_PROCESS_TYPE)

        request.getInputData().getRecipe().setName(recipe)
        request.getInputData().getRecipe().setRev(recipeRev)
        
        if (toolItemName!=null && toolItemName.length()>0)
        {
            def details = request.getInputData().getTools()
            def detailItem = details.addToolsItem()        
            detailItem.setName(toolItemName)            
        }
        
        def reply = camstarService.setEquipmentSetup(request)
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