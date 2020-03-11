package EapRequestWipData_FileBase

import org.apache.commons.collections4.map.HashedMap
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.ItemNotFoundException
import de.znt.pac.deo.annotations.*
import de.znt.pac.mapping.MappingManager
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.equipment.file.EquipmentFileHandler
import sg.znt.services.equipment.file.EquipmentFileService

@CompileStatic
@Deo(description='''
Verify MES recipe parameters
''')
class EapRequestWipData_FileBase_1
{

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="MappingManager")
    private MappingManager mappingManager

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataDomainObjectManager

    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment

    @DeoBinding(id="EquipmentFileService")
    private EquipmentFileService equipmentFileService
    
    @DeoBinding(id="FileSuffix")
    private String fileSuffix = ""

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def eqpId = mainEquipment.getSystemId()
        def lotId = mainEquipment.getCurrentLotId()

        CLot cLot = null
        try
        {
            cLot = cMaterialManager.getCLot(lotId)
        }
        catch (ItemNotFoundException e)
        {
            logger.info("Lot '$lotId' not found in PAC!" + e.printStackTrace())
            return
        }

        def wipDataList = wipDataDomainObjectManager.getAllDomainObject()
        for(wipDataObjSet in wipDataList)
        {
            if(wipDataObjSet.getId().equalsIgnoreCase(eqpId + "-" + lotId))
            {
                def lotMoveOutWipData = wipDataObjSet.getMoveOutWipDataItems()
                logger.info("Lot Move Out WIP Data:'$lotMoveOutWipData'")
                
                if (lotMoveOutWipData.size()>0)
                {
                    def schemaComponent = mappingManager.getSchemaComponentByName("MES", "WipData")
                    if (schemaComponent==null)
                    {
                        throw new Exception("Schema 'MES' with schema component 'WipData' not found in mapping manager schema!")
                    }
                    def schemaItems = schemaComponent.getSchemaItems()
                    Map<String, String> hMap = new HashedMap<String, String>()
                    String[] parameterList = new String[schemaItems.size()]
                    for (int i=0; i<schemaItems.size(); i++)
                    {
                        parameterList[i] = schemaItems.get(i).getUnit()
                    }
                    
                    if (parameterList.length>0)
                    {
                        LinkedHashMap<String, String> wipDataValues = requestWipDataFromEqp(parameterList)
                        for (schemaItem in schemaItems)
                        {
                            def value = wipDataValues.get(schemaItem.getUnit())
                            if (value==null)
                            {
                                throw new Exception("No value found for parameter '" + schemaItem.getUnit() + "', please ensure mapping manager and output file parameter are matched!")
                            }
                            
                            for (wipData in lotMoveOutWipData)
                            {
                                if (wipData.getId().equalsIgnoreCase(schemaItem.getName()))
                                {
                                    wipData.setValue(value)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private LinkedHashMap<String, String> requestWipDataFromEqp(String[] parameterList)
    {
        try
        {
            EquipmentFileHandler _fileHandler = equipmentFileService.getFileHandler()

            def requestId = _fileHandler.getREQUEST_ID()
            logger.info("Performing request data: last RequestId = '$requestId'...")
            LinkedHashMap<String, String> wipDataMapList = equipmentFileService.requestData(fileSuffix, parameterList)
            logger.info("File base request data completed with values:'$wipDataMapList'")
            return wipDataMapList
        }
        catch(Exception e)
        {
            logger.error(e.getMessage())
            throw new Exception(e.getMessage())
        }
    }
}