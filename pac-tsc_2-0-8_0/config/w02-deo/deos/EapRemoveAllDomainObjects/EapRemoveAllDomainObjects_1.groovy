package EapRemoveAllDomainObjects

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.domainobject.EquipmentMaterialDomainObjectManager
import sg.znt.pac.domainobject.EquipmentPMDomainObjectManager
import sg.znt.pac.domainobject.PmManager
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.domainobject.SequenceParameterManager
import sg.znt.pac.domainobject.WaferClassificationDomainObjectManager
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.domainobject.WipDataStatisticDomainObjectManager
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EapRemoveAllDomainObjects_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="EquipmentMaterialDomainObjectManager")
    EquipmentMaterialDomainObjectManager equipmentMaterialDomainObjectManager

    @DeoBinding(id="WipDataStatisticDomainObjectManager")
    WipDataStatisticDomainObjectManager wipDataStatisticDomainObjectManager

    @DeoBinding(id="EquipmentPMDomainObjectManager")
    EquipmentPMDomainObjectManager equipmentPMDomainObjectManager

    @DeoBinding(id="WipDataDomainObjectManager")
    WipDataDomainObjectManager wipDataDomainObjectManager

    @DeoBinding(id="RecipeManager")
    RecipeManager recipeManager

    @DeoBinding(id="PmManager")
    PmManager pmManager

    @DeoBinding(id="SequenceParameterManager")
    private SequenceParameterManager sequenceParameterManager

    @DeoBinding(id="WaferClassificationDomainObjectManager")
    private WaferClassificationDomainObjectManager waferClassificationDomainObjectManager

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    /**
     *
     */
    @DeoExecute
    public void execute() 
    {
        if(cMaterialManager.getCLotList(new LotFilterAll()).size()==0) {
            if(equipmentMaterialDomainObjectManager.getAllDomainObject().size()>0) {
                equipmentMaterialDomainObjectManager.removeAllDomainObject()
                logger.info("Remove equipmentMaterialDomainObjectManager Done.")
            }
            if(wipDataStatisticDomainObjectManager.getAllDomainObject().size()>0) {
                wipDataStatisticDomainObjectManager.removeAllDomainObject()
                logger.info("Remove wipDataStatisticDomainObjectManager Done.")
            }
            if(equipmentPMDomainObjectManager.getAllDomainObject().size()>0) {
                equipmentPMDomainObjectManager.removeAllDomainObject()
                logger.info("Remove equipmentPMDomainObjectManager Done.")
            }
            if(wipDataDomainObjectManager.getAllDomainObject().size()>0) {
                wipDataDomainObjectManager.removeAllDomainObject()
                logger.info("Remove wipDataDomainObjectManager Done.")
            }
            if(recipeManager.getAllDomainObject().size()>0) {
                recipeManager.removeAllDomainObject()
                logger.info("Remove recipeManager Done.")
            }
            if(pmManager.getAllDomainObject().size()>0) {
                pmManager.removeAllDomainObject()
                logger.info("Remove pmManager Done.")
            }
            if(sequenceParameterManager.getAllDomainObject().size()>0) {
                sequenceParameterManager.removeAllDomainObject()
                logger.info("Remove sequenceParameterManager Done.")
            }
            if(waferClassificationDomainObjectManager.getAllDomainObject().size()>0) {
                try {
                    waferClassificationDomainObjectManager.removeAllDomainObject()
                    logger.info("Remove waferClassificationDomainObjectManager Done.")
                }
                catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }
    }
}