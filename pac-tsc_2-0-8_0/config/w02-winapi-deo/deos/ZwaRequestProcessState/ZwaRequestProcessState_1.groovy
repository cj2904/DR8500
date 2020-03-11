package ZwaRequestProcessState

import groovy.transform.CompileStatic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import sg.znt.pac.machine.CEquipment
import sg.znt.services.zwin.ZWinApiService
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class ZwaRequestProcessState_1 {

  @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="ZWinApiService")
    private ZWinApiService zWinApiService

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        cEquipment.getProcessState().setState(zWinApiService.requestProcessState(cEquipment.getSystemId()))
    }
}