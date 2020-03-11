package CamstarWipDataImport

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.GetWIPDataSetupMaintListRequest
import sg.znt.camstar.semisuite.service.dto.GetWIPDataSetupMaintListResponseDto.ResponseData.ObjectToChange.SelectionValuesEx.RecordSet.RecordSetItem
import sg.znt.services.camstar.CCamstarService
import de.znt.camstar.semisuite.service.dto.WIPDataSetupMaintRequest
import de.znt.camstar.semisuite.service.dto.WIPDataSetupMaintResponseDto.ResponseData.ObjectChanges.Details.DetailsItem
import de.znt.pac.deo.annotations.*
import de.znt.pac.mapping.MappingManager
import de.znt.pac.mapping.data.MappingConfiguration
import de.znt.pac.mapping.data.Schema
import de.znt.pac.mapping.data.SchemaComponent
import de.znt.pac.mapping.data.SchemaItem
import de.znt.pac.mapping.data.SchemaItemType

//@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class CamstarWipDataImport_1
{

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="MappingManager")
    private MappingManager mappingManager

    @DeoBinding(id="WipDataName")
    private String wipDataName

    /**
     *
     */
    @DeoExecute
    public void execute()
    {

        String wipstr =""
        Collection<String> ResultList1 = new ArrayList()
        def request = new GetWIPDataSetupMaintListRequest()
        request.getRequestData().getObjectToChange().getRequestSelectionValuesEx().setRowSetSize("400")
        def reply = cCamstarService.getWIPDataSetupMaintList(request)
        if(reply.isSuccessful())
        {
            Iterator<RecordSetItem> items = reply.getResponseData().getObjectToChange().getSelectionValuesEx().getRecordSet().getRecordSetItems()
            while(items.hasNext())
            {
                RecordSetItem i = (RecordSetItem) items.next()
                ResultList1.add(i.getName())
                wipstr = wipstr + "," + i.getName()
            }
        }
        logger.info(wipstr)

        int count = 0
        String[] wipstrarr = wipstr.split(",")
        for(String wip : wipstrarr)
        {
            wipDataName= wip;

            List<DetailsItem> ResultList2 = new ArrayList<DetailsItem>();
            List<DetailsItem> ResultList2Reply = new ArrayList<DetailsItem>();
            def request2 = new WIPDataSetupMaintRequest()
            request2.getInputData().setObjectToChange(wipDataName)
            def reply2 = cCamstarService.sendWipDataSetupMaint(request2)
            if(reply2.isSuccessful())
            {
                ResultList2Reply = reply2.getDetailsItemList()
                for(DetailsItem item : ResultList2Reply)
                {
                    if(Boolean.parseBoolean(item.isHidden))
                    {
                        ResultList2.add(item)
                    }
                }
            }

            if(ResultList2.size()>0)
            {
                String defaultSchema = "MES"
                Schema targetSchema = null
                SchemaComponent targetSchemaComp = null
                MappingConfiguration mappingConfig =  mappingManager.getMappingConfigurationCopy()

                List<Schema> schemas = mappingConfig.getSchemas()
                for(Schema s: schemas)
                {
                    if(s.getName().equals(defaultSchema))
                    {
                        targetSchema = s
                        break
                    }
                }
                if (targetSchema==null)
                {
                    targetSchema = new Schema(null, String.valueOf(de.znt.pac.mapping.IdGenerator.getNewTimeBasedId()), defaultSchema);
                    mappingConfig.getSchemas().add(targetSchema)
                }

                if (targetSchema!=null)
                {
                    for(SchemaComponent sc : targetSchema.getSchemaComponents())
                    {
                        if(sc.getName().equals(wipDataName))
                        {
                            targetSchema.getSchemaComponents().remove(sc)
                            break
                        }
                    }

                    targetSchemaComp = new SchemaComponent(null, String.valueOf(de.znt.pac.mapping.IdGenerator.getNewTimeBasedId()), wipDataName);
                    targetSchema.getSchemaComponents().add(targetSchemaComp)
                }

                if(targetSchema!=null && targetSchemaComp!=null)
                {
                    for(DetailsItem item : ResultList2)
                    {
                        SchemaItem newSchemaItem = new SchemaItem(String.valueOf(de.znt.pac.mapping.IdGenerator.getNewTimeBasedId()), item.getWIPDataName().getName(), SchemaItemType.STRING,item.getServiceName());
                        targetSchemaComp.getSchemaItems().add(newSchemaItem)
                        logger.info("WipDataSetup Impoted " + wipDataName + " Data Name " + item.getWIPDataName().getName())
                    }
                }

                count = count + 1
                logger.info("WipDataSetup Total " + count )
                mappingManager.saveMappingConfiguration(mappingConfig)
                Thread.sleep(1000)
            }
        }
    }
}