package MesGetLotWIPFlag_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.GetLotWIPMainRequest
import sg.znt.services.camstar.CCamstarService

@CompileStatic
@Deo(description='''
W06 common function:<br/>
<b>Get Lot WIP flag</b></b>
''')
class MesGetLotWIPFlag_Common_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="CCamstarService")
	private CCamstarService cCamstarService

	@DeoBinding(id="LotId")
	private String lotId

	/**
	 *
	 */
	@DeoExecute(result="WIPFlag")
	public String execute()
	{
		def wipFlag = ""
		
		GetLotWIPMainRequest request = new GetLotWIPMainRequest(lotId);
		def response = cCamstarService.getLotWIPMain(request)
		if (response.isSuccessful())
		{
			wipFlag = response.getResponseData().getWIPFlagSelection()
		}
		
		return wipFlag
	}
}