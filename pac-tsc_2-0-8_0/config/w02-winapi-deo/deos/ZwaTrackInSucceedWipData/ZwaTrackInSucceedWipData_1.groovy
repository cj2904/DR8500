package ZwaTrackInSucceedWipData

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.domainobject.WipDataDomainObject
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.domainobject.WipDataDomainObjectSet
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.zwin.ZWinApiService
import de.znt.pac.deo.annotations.*
import de.znt.pac.domainobject.filter.FilterAllDomainObjects
import elemental.json.JsonObject

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class ZwaTrackInSucceedWipData_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="ZWinApiService")
    private ZWinApiService zWinApiService

    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataDomainObjectManager

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager materialManager

    @DeoBinding(id="MainEquipment")
    private CEquipment cEquipment

    private CLot clot = null;

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        String wipdatalist = ""
        String wipservicetype = ""

        List<WipDataDomainObjectSet> wddb = wipDataDomainObjectManager.getAllDomainObject()
        for(WipDataDomainObjectSet wds : wddb)
        {
            List<WipDataDomainObject> wdi = wds.getAll(new FilterAllDomainObjects())
            for(WipDataDomainObject wd : wdi)
            {
                wipservicetype = wd.getServiceType()
                if (wd.isHidden())
                {
                    if(wipdatalist.length()>0)
                    {
                        wipdatalist = wipdatalist +  ";"
                    }
                    wipdatalist = wipdatalist  + wd.getId() + "="
                }
            }
        }
        clot = materialManager.getCLotList(new LotFilterAll()).get(0)

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("LotId", clot.getId())
        param.put("WipDataList", wipdatalist)
        param.put("ServiceType", wipservicetype)
        param.put("EquipmentId", cEquipment.getSystemId())
        param.put("Message", "")

        JsonObject jo = zWinApiService.buildCommandMessage("MesTrackInSucceed",param);
        zWinApiService.sendTcpMessage(jo)
    }
}