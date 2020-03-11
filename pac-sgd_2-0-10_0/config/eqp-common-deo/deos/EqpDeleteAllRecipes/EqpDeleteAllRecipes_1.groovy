package EqpDeleteAllRecipes

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.services.secs.dto.S7F17DeleteProcessProgramSend;
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.TypeChecked;

@Deo(description='''
Delete all existing recipes from equipment
''')
class EqpDeleteAllRecipes_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
        if (secsGemService == null)
        {
            logger.error("No secsGemService, do not execute!")
        }
        else
        {
            S7F17DeleteProcessProgramSend request = new S7F17DeleteProcessProgramSend()
            def reply = secsGemService.sendS7F17DeleteProcessProgramSend(request)
            if(reply.isAccepted())
            {
                //OK
            }
        }
    }
}