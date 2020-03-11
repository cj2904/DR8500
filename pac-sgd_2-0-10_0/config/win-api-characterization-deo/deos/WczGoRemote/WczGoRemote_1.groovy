package WczGoRemote

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.services.zwin.ZWinApiService

@CompileStatic
@Deo(description='''
Send command 'Go remote' to win api gateway
''')
class WczGoRemote_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="WinApiService")
    private ZWinApiService winApiService
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        winApiService.requestOnline()
        logger.info("Equipment state change to online/remote!")
    }
}