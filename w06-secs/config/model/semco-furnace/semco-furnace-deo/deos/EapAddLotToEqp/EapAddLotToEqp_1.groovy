package EapAddLotToEqp

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.W06Constants
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
eap add track in lot to eqp port
''')
class EapAddLotToEqp_1
{

    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="WipDataManager")
    private WipDataDomainObjectManager wipDataManager

    @DeoBinding(id="InputXml")
    private String inputXml

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outbound = new W02TrackInLotRequest(inputXml)
        def lotId = outbound.getContainerName()
        def eqpId = outbound.getResourceName()
        def recipeId = outbound.getRecipeName()

        def portList = cEquipment.getPortList()

        for(port in portList)
        {
            if(port.getPortId().equalsIgnoreCase(outbound.getResourceName()))
            {
                def rcp = port.getPropertyContainer().getString(W06Constants.MES_LOT_RECIPE, "")
                if(rcp.length() > 0)
                {
                    if(!rcp.equalsIgnoreCase(recipeId))
                    {
                        throw new Exception("Existing recipe: '$rcp' is different with track in lot recipe: '$recipeId'!!!")
                    }
                }
                else
                {
                    port.getPropertyContainer().setString(W06Constants.MES_LOT_RECIPE, recipeId)
                    logger.info("Same Recipe Detected!!")
                }
                port.getPropertyContainer().setString(lotId, lotId)
            }
        }
    }
}