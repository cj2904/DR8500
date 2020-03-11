package EapHandleStateChange

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked;
import sg.znt.pac.machine.CEquipment

@Deo(description='''
Handle MES state change
''')
class EapHandleStateChange_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    @DeoBinding(id="CEquipment")
    private CEquipment equipment

    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
    	logger.info("LY CHECK >>> " + inputXmlDocument)
    }
}