package WinApiEapRemoveWipDataDO_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import OutboundRequest.CommonOutboundRequest
import de.znt.pac.ItemNotFoundException
import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.WinApiWipDataManager
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll

@CompileStatic
@Deo(description='''
W06 common function:<br/>
<b>Remove WIP Data Domain Object at after Lot sucessfully Track Out</b>
''')
class WinApiEapRemoveWipDataDO_Common_1
{


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())


	@DeoBinding(id="WinApiWipDataManager")
	private WinApiWipDataManager winApiWipDataManager

	@DeoBinding(id="InputXml")
	private String inputXml

	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager

	/**
	 *
	 */
	@DeoExecute
	public void execute()
	{
		winApiWipDataManager.removeAllDomainObject()
	}
}