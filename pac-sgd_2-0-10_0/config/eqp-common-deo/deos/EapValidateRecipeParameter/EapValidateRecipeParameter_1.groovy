package EapValidateRecipeParameter

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.ZTypes.ZString
import de.znt.pac.deo.annotations.*
import de.znt.pac.parameter.ParameterSet
import de.znt.pac.parameter.ParameterSetContainer
import de.znt.pac.property.PropertyFilterSearchByParameterPrefix
import de.znt.zutil.container.PropertyContainer
import groovy.transform.TypeChecked;
import sg.znt.pac.exception.ValidationFailureException
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll

@Deo(description='''
Validate Camstar recipe parameter
''')
class EapValidateRecipeParameter_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
		def lot = cMaterialManager.getCLotList(new LotFilterAll()).get(0)
		
		Map<String, String> rcpParam = getProperty(lot.getPropertyContainer(), "RcpParam_")
		Map<String, String> maxLimit = getProperty(lot.getPropertyContainer(), "Max_")
		Map<String, String> minLimit = getProperty(lot.getPropertyContainer(), "Min_")
		Map<String, String> fixValue = getProperty(lot.getPropertyContainer(), "FixValue_")
		
		Map<String, String> machineParam = getProperty(lot.getPropertyContainer(), "LotStart_")
		for (lotParam in rcpParam) 
		{
			def foundParam = machineParam.get(lotParam.getKey())
			if(foundParam == null)
			{
				throw new ValidationFailureException(lot.getId(), "Could not find parameter: " + lotParam.getKey())
			}
			else
			{
				if(!foundParam.equals(lotParam.getValue()))
				{
					throw new ValidationFailureException(lot.getId(), "Recipe parameter does not match: " + lotParam.getValue() + " (Camstar) vs " + foundParam + " (Machine)")
				}
			}
		}
		
		for(lotParam in maxLimit)
		{
			def foundParam = machineParam.get(lotParam.getKey())
			if(foundParam == null)
			{
				throw new ValidationFailureException(lot.getId(), "Could not find parameter: " + lotParam.getKey())
			}
			else
			{
                if (lotParam.getValue().length() > 0)
                {
                    def doubleValue = 0.0
                        def foundParamDouble = 0.0
                        try
                    {
                            foundParamDouble = Double.parseDouble(foundParam)
                                doubleValue = Double.parseDouble(lotParam.getValue())
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace()
                        throw new ValidationFailureException(lot.getId(), "Paramter: " + lotParam.getKey() + " not in correct format to check for upper/max limit")
                    }
                    
                    if(foundParamDouble > doubleValue)
                    {
                        throw new ValidationFailureException(lot.getId(), "Parameter: " + lotParam.getKey() + " has exceed upper/max limit: " + doubleValue)
                    }
                }
                else
                {
                    logger.info("Max limit not set in MES, skip upper/max limit check")
                }
			}
		}
		
		for(lotParam in minLimit)
		{
			def foundParam = machineParam.get(lotParam.getKey())
			if(foundParam == null)
			{
				throw new ValidationFailureException(lot.getId(), "Could not find parameter: " + lotParam.getKey())
			}
			else
			{
                if (lotParam.getValue().length() > 0)
                {
                    def doubleValue = 0.0
                        def foundParamDouble = 0.0
                        try
                    {
                            doubleValue = Double.parseDouble(foundParam)
                                foundParamDouble = Double.parseDouble(lotParam.getValue())
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace()
                        throw new ValidationFailureException(lot.getId(), "Paramter: " + lotParam.getKey() + " not in correct format to check for lower/min limit")
                    }
                    
                    if(doubleValue < foundParamDouble)
                    {
                        throw new ValidationFailureException(lot.getId(), "Parameter: " + lotParam.getKey() + " has exceed lower/min limit: " + doubleValue)
                    }
                }
                else
                {
                    logger.info("Min limit not set in MES, skip lower/min limit check")
                }
			}
		}
        
        for(lotParam in fixValue)
        {
            def foundParam = machineParam.get(lotParam.getKey())
            if(foundParam == null)
            {
                throw new ValidationFailureException(lot.getId(), "Could not find parameter: " + lotParam.getKey())
            }
            else
            {
                def doubleValue = 0.0
                def foundParamDouble = 0.0
                try
                {
                    doubleValue = Double.parseDouble(foundParam)
                    foundParamDouble = Double.parseDouble(lotParam.getValue())
                }
                catch(Exception e)
                {
                    e.printStackTrace()
                    throw new ValidationFailureException(lot.getId(), "Paramter: " + lotParam.getKey() + " not in correct format to check for fix value")
                }
                
                if(doubleValue != foundParamDouble)
                {
                    throw new ValidationFailureException(lot.getId(), "Parameter: " + lotParam.getKey() + " does not match with fix value: " + doubleValue)
                }
            }
        }
    }
	
	Map<String, String> getProperty(PropertyContainer propertyContainer, String prefix)
	{
		ParameterSet pvs = new ParameterSet()
		ParameterSetContainer pvsContainer = new ParameterSetContainer(pvs)
		
		PropertyFilterSearchByParameterPrefix filter = new PropertyFilterSearchByParameterPrefix(prefix)
		propertyContainer.copyPropertiesTo(pvsContainer, filter)
		
		Map<String, String> rcpParam = new HashMap<String, String>()
		for (def i = 0; i < pvsContainer.getParameterCount(); i++)
		{
			ZString value = pvsContainer.getParameter(i).getAttributeValue()
			rcpParam.put(remotePrefix(pvsContainer.getParameter(i).getId(), prefix), value.getStringValue())
		}
		
		return rcpParam
	}
	
	String remotePrefix(String key, String prefix)
	{
		return key.replace(prefix, "")
	}
}