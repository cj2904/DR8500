package EapCleanUpAdhocTrackInWipData

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.outbound.RequestWipDataRequest

@CompileStatic
@Deo(description='''
Clean up adhoc track in request
''')
class EapCleanUpAdhocTrackInWipData_1 
{
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
        if (serviceName.equalsIgnoreCase("TrackInLot"))
        {
            try 
            {
                def cLot = cMaterialManager.getCLot(outbound.getContainerName())
                if(cLot != null)
                {
                    if (cLot.getPropertyContainer().getString("IsTempCreate", "False").equalsIgnoreCase("true"))
                    {
                        cMaterialManager.removeCLot(cLot)
                    }                    
                }
            } 
            catch (Exception e) 
            {
                logger.debug(e.getMessage())
            }
        }
    }
}