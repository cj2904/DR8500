package EapCalcPrePostMeasValue

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.GetLotAttributesRequest
import sg.znt.camstar.semisuite.service.dto.SetWIPDataRequest
import sg.znt.pac.TscConstants
import sg.znt.pac.domainobject.WipDataDomainObject
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.domainobject.WipDataDomainObjectSet
import sg.znt.pac.machine.CEquipment;
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.deo.annotations.*
import de.znt.pac.domainobject.filter.FilterAllDomainObjects

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EapCalcPrePostMeasValue_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService camstarService

    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataDomainObjectManager

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager materialManager

    @DeoBinding(id="MainEquipment")
    private CEquipment cEquipment

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def clot = materialManager.getCLotList(new LotFilterAll()).get(0)
        String lotId = clot.getId()
        String prevalstr = ""
        String[] prevalarr
        String[] postvalarr

        String attribute = TscConstants.LOT_MES_ATTR_PRE_MEASURE_VALUE
        def request = new GetLotAttributesRequest(lotId, attribute, attribute)
        def reply = camstarService.getLotAttributes(request);
        if(reply.isSuccessful())
        {
            prevalstr = reply.getAttributeValue(attribute)
            logger.info(prevalstr)
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }

        prevalarr = prevalstr.split(",")
        postvalarr = new String[prevalarr.length]

        List<WipDataDomainObjectSet> wddb = wipDataDomainObjectManager.getAllDomainObject()
        for(WipDataDomainObjectSet wds : wddb)
        {
            List<WipDataDomainObject> wdi = wds.getAll(new FilterAllDomainObjects())
            for(int i=0 ; i<wdi.size()-1 ; i++)
            {
                WipDataDomainObject wd = wdi.get(i)
                postvalarr[i] = wd.getValue()
            }
        }

        double totalDiff = 0
        String servicename = ""
        String wipdataname = ""
        for(int i=0 ; i<prevalarr.length ; i++)
        {
            double diff = Double.parseDouble(String.valueOf(prevalarr[i])) - Double.parseDouble(String.valueOf(postvalarr[i]))
            totalDiff = totalDiff + diff
        }
        double result = totalDiff / 5 / 3

        for(WipDataDomainObjectSet wds : wddb)
        {
            List<WipDataDomainObject> wdi = wds.getAll(new FilterAllDomainObjects())
            wipdataname = wdi.get(wds.getElementCount()-1).getId()
            servicename = wdi.get(wds.getElementCount()-1).getServiceType()
            wdi.get(wds.getElementCount()-1).setValue(String.valueOf(result))
        }

        SetWIPDataRequest wiprequest = new SetWIPDataRequest()
        wiprequest.getInputData().setContainer(lotId)
        wiprequest.getInputData().setEquipment(cEquipment.getSystemId())
        wiprequest.getInputData().setServiceName(servicename)
        wiprequest.getInputData().setProcessType("NORMAL")

        def detailItem = wiprequest.getInputData().getDetails().addDetailsItem()
        detailItem.setWIPDataName(wipdataname)
        detailItem.setWIPDataValue(String.valueOf(result))

        def wipreply = camstarService.setWIPData(wiprequest)
        if(wipreply.isSuccessful())
        {
            logger.info("Calculated PrePost Measurement Result WipData Submitted.")
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
    }
}