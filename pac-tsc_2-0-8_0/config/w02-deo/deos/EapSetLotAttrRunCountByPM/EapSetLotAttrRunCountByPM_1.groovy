package EapSetLotAttrRunCountByPM

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.mes.outbound.OutboundRequest.InputData.OutboundRequestInfo.RequestParams.RequestParamsItem.ListParam
import sg.mes.outbound.OutboundRequest.InputData.OutboundRequestInfo.RequestParams.RequestParamsItem.ListParam.ListParamItem
import sg.mes.outbound.OutboundRequest.InputData.OutboundRequestInfo.RequestParams.RequestParamsItem.ListParam.ListParamItem.InnerListParam.InnerListParamItem
import sg.znt.camstar.semisuite.service.dto.ModifyLotAttributesRequest
import sg.znt.pac.TscConfig;
import sg.znt.pac.TscConstants;
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import OutboundRequest.CommonOutboundRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EapSetLotAttrRunCountByPM_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="inputXml")
    private String inputXml

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def maintenancestatus = ""
        def maintPMName = ""
        def lastUsageCount = ""
        def totalUsageCount = ""
        def request = new CommonOutboundRequest(inputXml)
        def eqpPmReqName = request.getItemValue("tscPMUsedName")

        def eqId = request.getResourceName()
        def lotId = request.getContainerName()

        ListParam itemValue = (ListParam) request.getItemValue("PMItems")
        Iterator<ListParamItem> listParamItems = itemValue.getListParamItems();
        while (listParamItems.hasNext())
        {
            ListParamItem item = listParamItems.next();
            maintenancestatus = item.getValue()

            Iterator<InnerListParamItem> innerListParamItems = item.getInnerListParam().getInnerListParamItems();
            while(innerListParamItems.hasNext())
            {
                InnerListParamItem next2 = innerListParamItems.next();
                if(next2.getName().equals("PM_Name"))
                {
                    maintPMName = next2.getValue();
                }

                if(next2.getName().equals("PM_LastUsageCount"))
                {
                    lastUsageCount = next2.getValue();
                }

                if(next2.getName().equals("PM_TotalUsageCount"))
                {
                    totalUsageCount = next2.getValue();
                }
            }

            if(maintPMName.equals(eqpPmReqName))
            {
                def attributePairValues = new HashMap<String, String>()
                attributePairValues.put(TscConstants.LOT_MES_ATTR_RUN_COUNT, lastUsageCount)

                def attributeRequest = new ModifyLotAttributesRequest(false, lotId, attributePairValues)
                def reply = cCamstarService.setLotAttributes(attributeRequest)
                if(reply.isSuccessful())
                {
                    logger.info(reply.getResponseData().toXmlString())
                }
                else
                {
                    CamstarMesUtil.handleNoChangeError(reply)
                }
            }
        }
    }
}