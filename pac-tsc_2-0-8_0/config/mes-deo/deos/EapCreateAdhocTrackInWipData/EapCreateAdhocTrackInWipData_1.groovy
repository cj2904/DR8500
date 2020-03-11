package EapCreateAdhocTrackInWipData

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.outbound.RequestWipDataRequest

@CompileStatic
@Deo(description='''
Handle adhoc track in request
''')
class EapCreateAdhocTrackInWipData_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
    
    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        RequestWipDataRequest outbound = new RequestWipDataRequest(inputXmlDocument)
        def serviceName = outbound.getWipDataServiceName()
        def cLot = null
        try 
        {
            cLot = cMaterialManager.getCLot(outbound.getContainerName())
        } 
        catch (Exception e) 
        {
            logger.error(e.getMessage())
            if (serviceName.equalsIgnoreCase("TrackInLot"))
            {
                cLot = cMaterialManager.createCLot(outbound.getContainerName())
                cLot.setPropertyKey("IsTempCreate", "True")
                cMaterialManager.addCLot(cLot)                
            }
            else
            {
                throw e
            }
        }           
    }
}