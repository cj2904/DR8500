package EapVerifyTestWorkFlow_PCT

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.services.camstar.outbound.TrackInLotRequest

@CompileStatic
@Deo(description='''
verify PCT eqp test workflow
''')
class EapVerifyTestWorkFlow_PCT_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outbound = new TrackInLotRequest(inputXmlDocument)
        def lotWorkflow = outbound.getParamValue("Workflow")
        def testWorkflow = outbound.getParamValue("tscEqpReserved4")
        if (testWorkflow.length()>0)
        {
            if (!lotWorkflow.equalsIgnoreCase(testWorkflow))
            {
                throw new Exception("Please perform Test Work Flow '$testWorkflow' before proceed with Production Run!")
            }
        }
    }
}