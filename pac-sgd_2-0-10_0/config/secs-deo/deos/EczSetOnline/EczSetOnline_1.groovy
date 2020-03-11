package EczSetOnline

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import de.znt.pac.deo.annotations.*
import sg.znt.pac.machine.CEquipment
import de.znt.services.secs.SecsGemService

@Deo(description='''
 Request online for equipment
''')
class EczSetOnline_1 {

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    /**
     * Request online for equipment
     */
    @DeoExecute
    public void setEqpRequestOnline()
    {
        secsGemService.requestOnline()
    }
}