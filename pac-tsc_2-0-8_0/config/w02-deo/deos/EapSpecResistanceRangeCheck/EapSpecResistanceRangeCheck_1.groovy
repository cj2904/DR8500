package EapSpecResistanceRangeCheck

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.TscConfig
import sg.znt.pac.TscConstants
import sg.znt.pac.domainobject.WipDataDomainObject
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.domainobject.WipDataDomainObjectSet
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import de.znt.camstar.semisuite.service.dto.LotHoldRequest
import de.znt.camstar.semisuite.service.dto.ViewContainerStatusRequest
import de.znt.camstar.semisuite.service.dto.ViewContainerStatusResponse
import de.znt.pac.deo.annotations.*
import de.znt.pac.domainobject.filter.FilterAllDomainObjects


@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EapSpecResistanceRangeCheck_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataDomainObjectManager

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    /**
     *
     */
    @DeoExecute
    public void execute()
    {

        def lotList = cMaterialManager.getCLotList(new LotFilterAll())
        for(int i=0 ; i<lotList.size() ; i++)
        {
            Boolean holdFlag = false;
            CLot lot = lotList.get(i)

            if(lot.getPropertyContainer().getBoolean(TscConstants.LOT_MES_ATTR_TSC_IS_REQUIRED_RANGE_CHECK, false))
            {
                String resistivityRange = lot.getPropertyContainer().getString(TscConstants.LOT_MES_ATTR_TSC_RESISTANCE, "")
                String[] minmax = resistivityRange.split("-")
                if(minmax.length > 1)
                {
                    Double minRange  = Double.parseDouble(String.valueOf(minmax[0]))
                    Double maxRange  = Double.parseDouble(String.valueOf(minmax[1]))

                    if(minRange.isNaN() == false && maxRange.isNaN() == false)
                    {
                        for (WipDataDomainObjectSet objectSet : wipDataDomainObjectManager.getAllWipDataSet())
                        {
                            List<WipDataDomainObject> all = objectSet.getAll(new FilterAllDomainObjects());
                            if (all.size() > 0)
                            {
                                for (int k = 0; k < all.size(); k++)
                                {
                                    WipDataDomainObject wipdataitem = all.get(k);

                                    def rawValue = String.valueOf(wipdataitem.getValue())
                                    if (rawValue != null && rawValue.length()>0)
                                    {
                                        try 
                                        {
                                            Double wipvalue = Double.parseDouble(rawValue)
                                        
                                            if(wipvalue.isNaN()==false)
                                            {
                                                if(wipvalue < minRange || wipvalue > maxRange)
                                                {
                                                    holdFlag = true;
                                                    logger.info(wipdataitem.getId() + "'s value " + wipvalue + "' is not in range " + resistivityRange)
                                                    break;
                                                }
                                            }
                                        } 
                                        catch (Exception e) 
                                        {
                                            e.printStackTrace()
                                        }                                        
                                    }                                    
                                }
                            }
                        }
                    }
                }

                if(holdFlag)
                {
                    String holdReason = TscConfig.getSpecResistanceRangeCheckHoldReason()
                    LotHoldRequest req = new LotHoldRequest(lot.getId(),holdReason )
                    def reply  = cCamstarService.lotHold(req)
                    if (reply.isSuccessful())
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
    }

    String getLotAttribute(String lotId, String attrName)
    {
        String result = ""
        ViewContainerStatusRequest request = new ViewContainerStatusRequest(lotId)
        request.getRequestData().getLotAttributes().initChildParameter(attrName, false)

        ViewContainerStatusResponse reply = new ViewContainerStatusResponse()
        reply.getResponseData().getLotAttributes().initChildParameter(attrName,false)
        reply =  cCamstarService.viewContainerStatus(request)
        if(reply.isSuccessful())
        {
            result = reply.getResponseData().getLotAttributes().getChildParameter(attrName).getValue()
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
        return result
    }
}