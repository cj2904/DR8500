package EapCsvSetEqpCapability

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.SetEqpProcessCapabilityRequest
import sg.znt.services.camstar.CCamstarService;
import de.znt.pac.deo.annotations.*



@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EapCsvSetEqpCapability_1
{

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarService")
    private CCamstarService camstarService

    @DeoBinding(id="FilePath")
    private String filePath

    private String eqpId
    private String[] capability

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        String line = ""
        String cvsSplitBy = ","

        try
        {
           BufferedReader br = new BufferedReader(new FileReader(filePath))
            int count = 0
            while ((line = br.readLine()) != null)
            {
                logger.info("file " + filePath)
                String[] item = line.split(cvsSplitBy)
               
                if(count==0)
                {
                    eqpId = item[0]
                    logger.info("eqpId " + eqpId)
                }
                if(count==1)
                {
                    capability = item
                    logger.info("capability " + capability)
                }
                count++
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        def request = new SetEqpProcessCapabilityRequest()
        request.getInputData().setResource(eqpId)
        for(int i=0 ; i<capability.length ; i++)
        {
            def item = request.getInputData().getDetails().addDetailsItem()
            item.getProcessCapability().setName(capability[i])
            item.setActivationStatus("TRUE")
            item.setAvailability("TRUE")
        }

        def reply = camstarService.setEqpProcessCapability(request)
        logger.info(reply.getResponseData().toXmlString())
    }
}