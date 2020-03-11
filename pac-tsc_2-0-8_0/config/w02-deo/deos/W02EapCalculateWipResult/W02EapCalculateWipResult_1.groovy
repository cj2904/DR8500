package W02EapCalculateWipResult

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.SetWIPDataRequest
import sg.znt.pac.SgdUtil
import sg.znt.pac.domainobject.WipDataDomainObject
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.CCamstarService
import OutboundRequest.CommonOutboundRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class W02EapCalculateWipResult_1 {


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())


	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager

	@DeoBinding(id="inputXml")
	private String inputXml
    
    @DeoBinding(id="WipParamNameP11")
    private String p11Name = "P11"
    
      @DeoBinding(id="WipParamNameP22")
    private String p22Name = "P22"

	@DeoBinding(id="CCamstarService")
	private CCamstarService cCamstarService

	/**
	 *
	 */
	@DeoExecute
	public void execute() {
		def outbound =  new CommonOutboundRequest(inputXml)
		def eqId = outbound.getResourceName()
		def lotId = outbound.getContainerName()
		def lot = cMaterialManager.getCLot(lotId)

		if (lot != null) {
			def wipData = lot.getWipDataByEquipment(eqId)
			def wipDataItems = wipData.getTrackInWipDataItems()
			def list = new ArrayList<Double>()
			for (value in wipDataItems) {
				list.add(Double.parseDouble(value.getValue()))
			}
			//from lot get requirement name,
			def result1 = SgdUtil.getAverage(list)
			def result2 = SgdUtil.getUniform(list)
			
			
			def request = new SetWIPDataRequest()
            request.getInputData().setContainer(lotId)
            request.getInputData().setEquipment(eqId)
            request.getInputData().setServiceName(WipDataDomainObject.SERVICE_TYPE_TRACK_OUT_WIP_DATA)
            request.getInputData().setProcessType("NORMAL")

			def detailItem1 = request.getInputData().getDetails().addDetailsItem()
			detailItem1.setWIPDataName(String.valueOf(p11Name))
			detailItem1.setWIPDataValue(String.valueOf(result1))


			def detailItem2 = request.getInputData().getDetails().addDetailsItem()
			detailItem2.setWIPDataName(String.valueOf(p22Name))
			detailItem2.setWIPDataValue(String.valueOf(result2))

            
            def reply = cCamstarService.setWIPData(request)
            if(reply.isSuccessful())
            {
                def message = reply.getResponseData().getCompletionMsg()
            }
            else
            {
                throw new Exception(reply.getExceptionData().getErrorDescription())
            }
			
		}
	}
}