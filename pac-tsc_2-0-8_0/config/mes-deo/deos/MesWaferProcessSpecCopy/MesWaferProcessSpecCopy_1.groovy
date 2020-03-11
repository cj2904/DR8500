
package MesWaferProcessSpecCopy


import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import sg.znt.camstar.semisuite.service.dto.GetWaferProcessSpecMaintListRequest
import sg.znt.camstar.semisuite.service.dto.WaferProcessSpecCopyRequest
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Get MES equipment maintenance
''')
class MesWaferProcessSpecCopy_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="WaferProcessSpec")
    private String waferProcessSpec


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def itemcount = 0
        GetWaferProcessSpecMaintListRequest request = new GetWaferProcessSpecMaintListRequest()
        def reply = cCamstarService.getWaferProcessSpecMaintList(request)
        if(reply.isSuccessful())
        {
            def items = reply.getResponseData().getObjectToChange().getSelectionValuesEx().getRecordSet().getRecordSetItems()
            while(items.hasNext())
            {
                def item = items.next()
                def row = item.getRow()
                itemcount++
                logger.error("["+itemcount + "] Wafer Process Spec name " + row.getName() + " rev ["+row.getRevision()+"]")

                if(itemcount==1)
                {
                    def request2 = new WaferProcessSpecCopyRequest()
                    request2.getInputDataload().getObjectToChange().setName(row.getName())
                    request2.getInputDataload().getObjectToChange().setRevision(row.getRevision())

                    request2.getInputData().getObjectChanges().setName("" + row.getName() + " Copy")
                    request2.getInputData().getObjectChanges().setRevision(row.getRevision())
                    def reply2 = cCamstarService.waferProcessSpecCopy(request2)
                    if(reply2.isSuccessful())
                    {
                        logger.error("Wafer Process Spec Copy Done for ["+row.getName()+"] rev["+row.getRevision()+"]")
                    }
                    else
                    {
                        logger.error(reply2.getExceptionData().getErrorDescription())
                    }
                    break;
                }
            }
        }
        else
        {
            logger.error(reply.getExceptionData().getErrorDescription())
        }
    }
}