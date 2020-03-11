package SnrRequestProcessData

import groovy.transform.TypeChecked

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.machine.TscEquipment
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.deo.annotations.*

@Deo(description='''
Dispatch to equipment scenario to request process data
''')
class SnrRequestProcessData_1 {


   @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    
    @DeoBinding(id="MainEquipment")
    private TscEquipment mainEquipment
    
    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
        mainEquipment.getModelScenario().eqpMesRequestProcessData(cCamstarService, inputXmlDocument)
    }
}