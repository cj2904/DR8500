package EapSetLotAttrPreMeasValue

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.ModifyLotAttributesRequest
import sg.znt.pac.TscConfig
import sg.znt.pac.TscConstants
import sg.znt.pac.domainobject.WipDataDomainObject
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.domainobject.WipDataDomainObjectSet
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import OutboundRequest.CommonOutboundRequest
import de.znt.pac.deo.annotations.*
import de.znt.pac.domainobject.filter.FilterAllDomainObjects

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EapSetLotAttrPreMeasValue_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataDomainObjectManager

    @DeoBinding(id="inputXml")
    private String inputXml

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new CommonOutboundRequest(inputXml)
        def lotId = request.getContainerName()
        def lotstep = request.getItemValue("Step")

        def matchStep = false;
        String[] stepsarr = TscConfig.getCalcPrePostMeasSteps()
        for(int i=0 ; i < stepsarr.length ; i ++)
        {
            if(stepsarr[i].equals(lotstep))
            {
                matchStep = true;
            }
        }

        if(matchStep)
        {
            def wipdatalist = ""
            List<WipDataDomainObjectSet> wddb = wipDataDomainObjectManager.getAllDomainObject()
            for(WipDataDomainObjectSet wds : wddb)
            {
                List<WipDataDomainObject> wdi = wds.getAll(new FilterAllDomainObjects())
                for(int i=0 ; i <wdi.size()-1 ; i++)
                {
                    WipDataDomainObject wd = wdi.get(i)
                    if(wipdatalist.length()>0)
                    {
                        wipdatalist = wipdatalist +  ";"
                    }
                    wipdatalist = wipdatalist  + wd.getValue()
                }
            }

            def attributePairValues = new HashMap<String, String>()
            attributePairValues.put(TscConstants.LOT_MES_ATTR_PRE_MEASURE_VALUE, wipdatalist)

            def attributeRequest = new ModifyLotAttributesRequest(false, lotId, attributePairValues)
            def reply = cCamstarService.setLotAttributes(attributeRequest)
            if(reply.isSuccessful())
            {
                logger.info(reply.getResponseData().toXmlString())
            }
            else
            {
                CamstarMesUtil.handleNoChangeError(reply)
            }
        }
    }
}