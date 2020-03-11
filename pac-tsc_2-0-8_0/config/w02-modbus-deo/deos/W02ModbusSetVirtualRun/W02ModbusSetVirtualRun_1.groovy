package W02ModbusSetVirtualRun

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.mes.outbound.OutboundRequest.InputData.OutboundRequestInfo.RequestParams.RequestParamsItem.ListValue;
import sg.znt.camstar.semisuite.service.dto.TrackOutWIPMainWafersRequest
import sg.znt.pac.TscConfig
import sg.znt.pac.TscConstants
import sg.znt.services.camstar.CCamstarService
import OutboundRequest.CommonOutboundRequest
import de.znt.camstar.semisuite.service.dto.LotModifyAttrsRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Set Virtual Run Attribute and auto track out
''')
class W02ModbusSetVirtualRun_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="InputXml")
    private String inputXml

	@DeoBinding(id="CamstarService")
	private CCamstarService camstarService

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    	def request = new CommonOutboundRequest(inputXml)
		def eqId = request.getResourceName()
		if (TscConfig.getVirtualEquipmentName().equalsIgnoreCase(eqId))
		{
			def req = new LotModifyAttrsRequest(request.getContainerName())
			def value = request.getParamValue(TscConstants.LOT_MES_ATTR_VIRTUAL_RUN_COUNT)
			def runCount = 1
			if (value !=null && value.length()>1)
			{
				runCount = Integer.parseInt(value) + 1
			}
			req.getInputData().setAttribute(TscConstants.LOT_MES_ATTR_VIRTUAL_RUN_COUNT, Integer.toString(runCount))
			def reply = camstarService.lotModifyAttributes(req)
			if (reply.isSuccessful())
			{
				def trackOut = new TrackOutWIPMainWafersRequest(request.getResourceName(), eqId)
				def waferList = request.getTxnWaferList()
				for(wafer in waferList)
				{
					def item = trackOut.getInputData().getWafersDetails().addWafersDetailsItem()
					item.setContainer(request.getContainerName())
					item.setWaferScribeNumber(wafer.getWaferScribeNumber())
				}
				if (waferList.size()>0)
				{
					def trackOutReply = camstarService.trackOutWafers(trackOut)
					if (trackOutReply.isSuccessful())
					{
						logger.info(trackOutReply.getResponseData().getCompletionMsg())
					}
					else
					{
						logger.error(trackOutReply.getExceptionData().getErrorDescription())
					}
				}
			}
			else
			{
				logger.error(reply.getExceptionData().getErrorDescription())
			}
			//Set virtual run attribute 1
		}
    }
}