package EqpRequestVariable_SecsWithMapping

import groovy.transform.TypeChecked

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.SgdConfig
import sg.znt.services.zwin.ZWinApiServiceImpl
import de.znt.pac.deo.annotations.*
import de.znt.pac.mapping.MappingManager
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S1F3SelectedEquipmentStatusRequest
import de.znt.zsecs.composite.SecsComponent
import de.znt.zsecs.composite.SecsDataItem
import de.znt.zsecs.composite.SecsDataItem.ItemName

@Deo(description='''
Request variable from equipment
''')
class EqpRequestVariable_SecsWithMapping_1 {


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())


	@DeoBinding(id="SecsGemService")
	private SecsGemService secsGemService

	@DeoBinding(id="VariableList")
	private List<String> variableList
    
    @DeoBinding(id="MappingManager")
    private MappingManager mappingManager
    
	/**
	 *
	 */
	@DeoExecute(result="List")
	@TypeChecked
	public List execute()
	{
        if (secsGemService != null)
        {
            List<String> value = new ArrayList<String>()
            for (var in variableList)
            {
                def vid = getSecsMappingName(var)
                if (vid.length()>0)
                {
                    def vidValue = ""
                    SecsComponent< ? > svidItem = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(vid)))
                    
                    S1F3SelectedEquipmentStatusRequest request = new S1F3SelectedEquipmentStatusRequest(svidItem)
                    def reply = secsGemService.sendS1F3SelectedEquipmentStatusRequest(request)
                    if(reply.getData().getSize() > 0)
                    {
                        vidValue = reply.getData().getSV(0).getValueList().get(0).toString() + ""
                    }
                    logger.info("Resolving '" + var + "' to '" + vid + "' with value '" + vidValue + "'")
                    value.add(var + ":" + vidValue)
                }
                else
                {
                    value.add(var + ":" + ZWinApiServiceImpl.CONST_NOT_SUPPORT)
                }
            }
            return value
        }		
		
		return null
	}
    
    private String getSecsMappingName(String camstarDataCollectionName)
    {
        def mappingName = SgdConfig.getStringProperty("Mapping_SecsDataCollection","Mapping_SecsDataCollection")
        if (mappingName.length()==0)
        {
            return ""
        }
        def mappingSet = mappingManager.getMappingSet(mappingName)
        if (mappingSet == null)
        {
            return ""
        }
        def mappingList = mappingSet.getMappings()
        for (mapping in mappingList)
        {
            def sourceList = mapping.getSources()
            for (src in sourceList)
            {
                def srcSchemaItem = mappingManager.getSchemaItem(src.getSchemaId(), src.getSchemaComponentId(), src.getSchemaItemId())
                def target = mapping.getTarget()
                def tgtSchemaItem = mappingManager.getSchemaItem(target.getSchemaId(), target.getSchemaComponentId(), target.getSchemaItemId())
                
                if (camstarDataCollectionName.equalsIgnoreCase(srcSchemaItem.getName()))
                {
                    def targetId = tgtSchemaItem.getId()
                    return targetId
                }
            }
        }
        return ""
    }
}