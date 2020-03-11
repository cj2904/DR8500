package EapVerifyTestWorkFlow_AutoProber

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.services.camstar.outbound.TrackInLotRequest

@CompileStatic
@Deo(description='''
Verify if there is any test workflow to be completed upon track in
''')
class EapVerifyTestWorkFlow_AutoProber_1
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
        def lotId = outbound.getContainerName()
        def lotWorkflow = outbound.getParamValue("Workflow")
        def testWorkflow = outbound.getParamValue("tscEqpReserved3")
        if (testWorkflow.length()>0)
        {
            if (!lotWorkflow.equalsIgnoreCase(testWorkflow))
            {
                throw new Exception("Please perform Test Work Flow '$testWorkflow' before proceed with Production Run!")
            }
            else
            {
                logger.info("Lot ID '$lotId' workflow match with TestWorkflow '$testWorkflow'...")
            }
        }
        else
        {
            logger.info("Lot ID '$lotId' testWorkflow field is empty...")
        }
    }
}