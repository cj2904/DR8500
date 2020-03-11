package DprRequestVariable

import java.util.Map.Entry

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked
import sg.znt.pac.domainobject.MachineDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.services.zwin.ZWinApiServiceImpl

@Deo(description='''
Dispatch to equipment
''')
class DprRequestVariable_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CEquipment")
    private CEquipment mainEquipment

    @DeoBinding(id="MachineDomainObjectManager")
    private MachineDomainObjectManager manager
	
	@DeoBinding(id="VariableList")
	private List<String> variableList

    /**
     *
     */
    @DeoExecute(result="List")
    @TypeChecked
    public List execute()
    {
        def machineSet = manager.getMachineSet(mainEquipment.getName())
        def list = machineSet.getAllMachineDomainObject()
		List<String> valueList = new ArrayList<String>()
        Hashtable<String, List> table = new Hashtable<String, List>();        
        for (mo in list) {
            def cEquipment = mo.getCEquipment()
            logger.info("Calling scenario: " + cEquipment.getModelScenario().getModelType())
            List<String> value = cEquipment.getModelScenario().eqpRequestVariableByList(variableList)
			
            if (value == null)
            {
                logger.info(cEquipment.getName() + " has no data collection")
            }
            else
            {
                table.put(cEquipment.getName(), value)
            }
        }
        logAllEquipmentDC(table, variableList)
        for (int i = 0; i < variableList.size(); i++)
        {
            String name = variableList.get(i)
            String value = name + ":" + ZWinApiServiceImpl.CONST_NOT_SUPPORT
            String notSupportValue = value
            Set<Entry<String,List>> entrySet = table.entrySet()
            for (Entry<String, List> entry : entrySet)
            {
                List equipmentDataCollection = entry.getValue()
                String dcValue = equipmentDataCollection.get(i)
                if (value.equalsIgnoreCase(notSupportValue))
                {
                    value = dcValue
                    logger.info("Assign value '" + dcValue + "' to '" + name + "'")
                }
            }
//            if (value.equalsIgnoreCase(notSupportValue))
//            {
//                throw new Exception("No value returned for wip Data '" + name + "'")
//            }
            valueList.add(value)
        }
		
		return valueList
    }
    
    private void logAllEquipmentDC(Hashtable<String, List> table, List<String> variableList)
    {
        logger.info("Consolidate data Collection: Key = '" +  variableList.toString() + "'")
        Set<Entry<String,List>> entrySet = table.entrySet()
        for (Entry<String, List> entry : entrySet)
        {
            List equipmentDataCollection = entry.getValue()
            logger.info("Consolidate data Collection: [" + entry.getKey() + "] " + equipmentDataCollection.toString()) 
        }
    }
}