package EapWipDataCalcRRV

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.SgdUtil
import sg.znt.pac.TscConfig
import sg.znt.pac.domainobject.WipDataDomainObject
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.domainobject.WipDataDomainObjectSet
import de.znt.pac.deo.annotations.*
import de.znt.pac.domainobject.filter.FilterAllDomainObjects

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EapWipDataCalcRRV_1 {



    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataDomainObjectManager

    private String grpSeperator = "-"
    private String rrvWipDataName = TscConfig.getStringProperty("RRV.WipData.Name", "");


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        if (rrvWipDataName.length()==0)
        {
            logger.info("Skip executing since no rrv is configured!")
            return
        }
        def wds = wipDataDomainObjectManager.getAllWipDataSet()
        for (WipDataDomainObjectSet objectSet : wds)
        {
            // DETERMINE THE GROUP
            List<String> grpList = new ArrayList<String>();
            List<WipDataDomainObject> all = objectSet.getAll(new FilterAllDomainObjects());
            
            WipDataDomainObject rrvWipDataItem = null
            for (wdo in all) 
            {
                if (wdo.getId().matches(rrvWipDataName))
                {
                    rrvWipDataItem = wdo
                    break
                }
            }
            if (rrvWipDataItem != null)
            {
                String wipdataitemgrpstr = SgdUtil.getGroupNameWithLastSeperator(rrvWipDataItem.getId(),grpSeperator);
                List<Double> numList = new ArrayList<Double>()
                
                for (wdo in all)
                {
                    if (!wdo.getId().equalsIgnoreCase(rrvWipDataItem.getId()) && wdo.getId().indexOf(wipdataitemgrpstr)!=-1)
                    {                        
                        def rawValue = wdo.getValue()
                        if (rawValue != null && rawValue.length()>0)
                        {
                            try
                            {
                                numList.add(Double.parseDouble(String.valueOf(rawValue)))
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace()
                            }
                        }
                    
                    }
                }
                def result = SgdUtil.getInEquility(numList)
                logger.info("RRV inequility = " + result)
                rrvWipDataItem.setValue(result+"")                
            }
        }
    
    }
}