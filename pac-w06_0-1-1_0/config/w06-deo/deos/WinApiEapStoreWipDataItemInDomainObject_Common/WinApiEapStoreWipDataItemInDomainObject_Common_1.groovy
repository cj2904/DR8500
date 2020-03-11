package WinApiEapStoreWipDataItemInDomainObject_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.DuplicateItemException
import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.WinApiWipData
import sg.znt.pac.domainobject.WinApiWipDataManager
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
W06 specific handling:<br/>
<b>Store Measurement WIP Data in Win API Domain Object</b>
''')
class WinApiEapStoreWipDataItemInDomainObject_Common_1
{


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="WinApiWipDataManager")
	private WinApiWipDataManager winApiWipDataManager

	@DeoBinding(id="InputXmlDocument")
	private String inputXmlDocument

	/**
	 *
	 */
	@DeoExecute
	public void execute()
	{
		W02TrackInLotRequest request = new W02TrackInLotRequest(inputXmlDocument)
		def wipDataItemList = request.getWipDataItemList()
		for (wipDataItem in wipDataItemList)
		{
			/*
			 * WIP_DATA_IS_HIDDEN
			 * 0	:	FALSE
			 * -1	:	TRUE
			 */
			if (!wipDataItem.WIP_DATA_IS_HIDDEN.equalsIgnoreCase("0"))
			{
				try
				{
					def newDObj = winApiWipDataManager.createNewDomainObject(wipDataItem.WIP_DATA_SERVICE_NAME + "-" + wipDataItem.WIP_DATA_NAME)
					newDObj.setWipDataServiceName(wipDataItem.WIP_DATA_SERVICE_NAME)
					newDObj.setWipDataItemName(wipDataItem.WIP_DATA_NAME)
					
					newDObj.notifyListeners()
				}
				catch (DuplicateItemException e)
				{
					logger.info("Domain object type='" + WinApiWipData.TYPE + "', id='" + wipDataItem.WIP_DATA_NAME + "' already exists in repository!")
				}
			}
		}
	}
}