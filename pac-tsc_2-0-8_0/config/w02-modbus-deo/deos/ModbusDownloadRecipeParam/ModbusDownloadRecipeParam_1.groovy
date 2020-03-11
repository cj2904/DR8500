package ModbusDownloadRecipeParam

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import sg.znt.services.modbus.W02ModBusService
import java.lang.String

@CompileStatic
@Deo(description='''
Download recipe param to modbus equipment
''')
class ModbusDownloadRecipeParam_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="W02ModBusService")
    private W02ModBusService w02ModBusService

    @DeoBinding(id="RecipeParam")
    private String recipeParam

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    
    }
}