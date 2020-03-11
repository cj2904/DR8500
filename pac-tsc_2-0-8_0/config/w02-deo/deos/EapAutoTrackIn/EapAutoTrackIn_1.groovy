package EapAutoTrackIn

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.GetLotWIPMainRequest
import sg.znt.camstar.semisuite.service.dto.TrackInWIPMainWafersRequest
import sg.znt.pac.TscConfig
import sg.znt.services.camstar.CCamstarService
import OutboundRequest.CommonOutboundRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Auto track in to next equipment
''')
class EapAutoTrackIn_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="InputXml")
    private String inputXml

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    	def eqIds = TscConfig.getCamstarEquipmentIds()
		def request = new CommonOutboundRequest(inputXml)
		if (eqIds!= null && eqIds.length>1)
		{
			def lotId = request.getContainerName()
			def wipMain = new GetLotWIPMainRequest(lotId)
			def wipMainReply = cCamstarService.getLotWIPMain(wipMain)
			
			if (wipMainReply.isSuccessful())
			{
				def items = wipMainReply.getResponseData().getEquipmentSelection().getEquipmentSelectionItems()
				while(items.hasNext())
				{
					def item = items.next()
					def eqName = item.getName()
					def waferList = request.getTxnWaferList()
					if (TscConfig.isAutoTrackInEquipment(eqName) && waferList.size()>0)
					{
						def trackInRequest = new TrackInWIPMainWafersRequest(lotId, eqName)
						for (wafer in waferList) 
						{
							def waferItem =trackInRequest.getInputData().getWafersDetails().addWafersDetailsItem()
							waferItem.setWaferScribeNumber(wafer.getWaferScribeNumber())
							waferItem.setContainer(lotId)
						}
						def trackInReply = cCamstarService.trackInWafers(trackInRequest)
						if (!trackInReply.isSuccessful())
						{
							logger.error(trackInReply.getExceptionData().getErrorDescription())
						}
					}
				}
			}
			else
			{
				logger.error(wipMainReply.getExceptionData().getErrorDescription())
			}
		}
    }
	
}