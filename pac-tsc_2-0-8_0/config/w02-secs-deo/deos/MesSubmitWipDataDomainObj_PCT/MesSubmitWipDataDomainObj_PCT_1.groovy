package MesSubmitWipDataDomainObj_PCT

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.domainobject.filter.FilterAllDomainObjects
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.SetWIPDataRequest
import sg.znt.pac.domainobject.WipDataDomainObject
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest

@CompileStatic
@Deo(description='''
submit wip data using domain object for PCT eqp
''')
class MesSubmitWipDataDomainObj_PCT_1
{
    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataDomainObjectManager

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outbound = new W02CompleteOutLotRequest(inputXmlDocument)
        def outboundLot = outbound.getContainerName()
        def serviceName = WipDataDomainObject.SERVICE_TYPE_MOVE_OUT_WIP_DATA

        CLot lot = null
        try
        {
            if(outboundLot != null && outboundLot.length() > 0)
            {
                lot = cMaterialManager.getCLot(outboundLot)
            }
        }
        catch (Exception e)
        {
            e.printStackTrace()
        }

        if(lot == null)
        {
            throw new Exception("Lot: '$outboundLot' is not found in PAC!")
        }
        else
        {
            def trackOutWipData = lot.getEquipmentId() + "-" + lot.getId()
            def wipDataSet = wipDataDomainObjectManager.getWipDataSet(trackOutWipData)
            if(wipDataSet != null)
            {
                def eqpId = lot.getEquipmentId().substring(lot.getEquipmentId().length() - 1)
                def request = new SetWIPDataRequest()
                request.getInputData().setContainer(lot.getId())
                request.getInputData().setEquipment(mainEquipment.getSystemId() + "-" + eqpId)
                request.getInputData().setServiceName(serviceName)
                request.getInputData().setProcessType("NORMAL")

                def wipDataList = wipDataSet.getAll(new FilterAllDomainObjects())
                for(wipData in wipDataList)
                {
                    def detailsItem = request.getInputData().getDetails().addDetailsItem()
                    detailsItem.setWIPDataName(wipData.getId())
                    detailsItem.setWIPDataValue(wipData.getValue())
                }

                def reply = cCamstarService.setWIPData(request)
                if(reply.isSuccessful())
                {
                    logger.info(reply.getResponseData().getCompletionMsg())
                }
                else
                {
                    throw new Exception(reply.getExceptionData().getErrorDescription())
                }
            }
            else
            {
                logger.info("No WIP Data Set found in Domain Object for lot '$outboundLot'!")
            }
        }
    }
}