package EapVerifyCenterMinViolation

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
class EapVerifyCenterMinViolation_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataDomainObjectManager

    private String grpSeperator = "-"
    private String centerKey = TscConfig.getStringProperty("VerifyCenter.CenterWipData.Name", "");
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        verifyCenterMin()
    }

    public void verifyCenterMin()
    {
        for (WipDataDomainObjectSet objectSet : wipDataDomainObjectManager.getAllWipDataSet())
        {
            // DETERMINE THE GROUP
            List<String> grpList = new ArrayList<String>();
            List<WipDataDomainObject> all = objectSet.getAll(new FilterAllDomainObjects());
            if (all.size() > 0)
            {
                for (int i = 1; i < all.size(); i++)
                {
                    WipDataDomainObject wipdataitemgrp = all.get(i - 1);
                    WipDataDomainObject wipdataitem = all.get(i);
                    String wipdataitemgrpstr = SgdUtil.getGroupNameWithLastSeperator(wipdataitemgrp.getId(),grpSeperator);
                    String wipdataitemstr = SgdUtil.getGroupNameWithLastSeperator(wipdataitem.getId(),grpSeperator);
                    if (wipdataitemgrpstr.length() > 0 && wipdataitemstr.length() > 0 && wipdataitemgrpstr.equalsIgnoreCase(wipdataitemstr) == false)
                    {
                        if(isHaveCenter(objectSet, wipdataitemstr))
                        {
                            grpList.add(wipdataitemstr);
                        }
                    }
                    else
                    {
                        boolean found = false
                        for (grp in grpList) 
                        {
                            if(grp.equalsIgnoreCase(wipdataitemgrpstr))
                            {
                                found = true
                                break
                            }
                        }
                        if (!found)
                        {
                            if(isHaveCenter(objectSet, wipdataitemgrpstr))
                            {
                                grpList.add(wipdataitemgrpstr);
                            }
                        }
                    }
                }
            }
            logger.info("TCC " + grpList.toString())

            // GET WIPDATA BELONG TO SAME GROUP FROM ALL WIPDATA THA MAY NOT IN SEQUENCE
            String invalidGroup ="";
            Map<String, String> mp = new HashMap<String, String>();
            for (String grp : grpList)
            {
                mp.clear();
                List<WipDataDomainObject> allgrp = objectSet.getAll(new FilterAllDomainObjects());
                for (int i = 0; i < allgrp.size(); i++)
                {
                    WipDataDomainObject wipdataitem = allgrp.get(i);
                    if (wipdataitem.getId().contains(grp))
                    {
                        def idName = wipdataitem.getId()
                        if(objectSet.getObjectType().equalsIgnoreCase(WipDataDomainObjectSet.OBJECT_TYPE_WAFER) && TscConfig.useBatchWaferDataCollection())
                        {
                            idName = objectSet.getId() + "@" + wipdataitem.getId();
                        }
                        mp.put(idName, wipdataitem.getValue());
                    }
                }

                // CHECK DATA IN GROUP WHETHER IS CENTER MIN
                String minKey = ""
                if (mp.size() > 0)
                {
                    double minVal = Double.MAX_VALUE;
                    for (Map.Entry<String, String> entry : mp.entrySet())
                    {
                        String key = entry.getKey();
                        def rawValue = entry.getValue();
                        if (rawValue!=null && rawValue.length()>0)
                        {
                            try 
                            {
                                double value = Double.parseDouble(rawValue);
                                logger.info("CenterMinViolation Check key [" + key + "] value [" + value + "]");
                                if (value < minVal)
                                {
                                    minVal = value;
                                    minKey = key;
                                    logger.info("CenterMinViolation Found for Key [" + minKey + "]");
                                }
                            } 
                            catch (Exception e) 
                            {
                                e.printStackTrace()
                            }
                            
                        }                        
                    }

                    if (minKey.matches(centerKey) == false)
                    {
                        if(invalidGroup.length()>0)
                        {
                            invalidGroup =invalidGroup + ",";
                        }
                        invalidGroup =invalidGroup + minKey;
                    }
                }
            }
            objectSet.getPropertyContainer().setString("CenterMinViolation",invalidGroup)
        }
    }

    boolean isHaveCenter(WipDataDomainObjectSet objectSet, String str)
    {
        List<WipDataDomainObject> allgrp = objectSet.getAll(new FilterAllDomainObjects());
        for (int i = 0; i < allgrp.size(); i++)
        {
            WipDataDomainObject wipdataitem = allgrp.get(i);
            if(wipdataitem.getId().contains(str) && wipdataitem.getId().matches(centerKey))
            {
                return true
            }
        }
        return false
    }

}