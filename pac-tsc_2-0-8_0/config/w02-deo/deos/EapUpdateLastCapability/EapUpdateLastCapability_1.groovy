package EapUpdateLastCapability

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.SetEquipmentMaintRequest
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.mes.ProcessCapability
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02TrackInLotCompleteRequest
import de.znt.pac.ItemNotFoundException
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''

''')
class EapUpdateLastCapability_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService camstarService

    @DeoBinding(id="CEquipment")
    private CEquipment equipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="InputXml")
    private String inputXml

    private int thruput2=0
    private CLot lot

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def req = new W02TrackInLotCompleteRequest(inputXml)
        def lotId = req.getContainerName()

        try
        {
            lot = cMaterialManager.getCLot(lotId)
        }
        catch (ItemNotFoundException ex)
        {
            logger.info("ItemNotFoundException exxxx.")
        }

        try
        {
            thruput2 = Integer.parseInt(req.getWaferQty())
        }
        catch (Exception e)
        {
            e.printStackTrace()
        }

        if (lot != null)
        {
            def batchTrackInLots = equipment.getPropertyContainer().getStringArray(req.getResourceName() + "_BatchTrackInLots", new String[0]);
            def batchLotSize = batchTrackInLots.length
            if (batchLotSize > 0)
            {
                def lastLotIdInBatch = batchTrackInLots[batchLotSize-1]
                if (lotId.equalsIgnoreCase(lastLotIdInBatch))
                {
                    Thread.sleep(5000)

                    def allRecipe = lot.getAllRecipeObj()
                    boolean capabilityChangeFlag = false
                    for (recipe in allRecipe)
                    {
                        if (req.getResourceName().equalsIgnoreCase(recipe.getMainEquipmentId()) && recipe.isSubRecipe())
                        {
//                            if (recipe.getLastCapability().length() == 0)
//                            {
//                                def lotProcessCapability = lot.getProcessCapability()
//                                recipe.setLastCapability(lotProcessCapability)
//                                logger.info("Recipe '" + recipe.getId() + "' last capability is '$lotProcessCapability' for lot '$lotId' with process capability '$lotProcessCapability'")
//                            }
                            def capability = new ProcessCapability(lot.getProcessCapability())
                            if(capability.getName().length()>0)
                            {
                                def capabilityName = capability.getName()
                                def capabilityCondition = recipe.getCapabilityCondition()

                                logger.info("Recipe '" + recipe.getId() + "' Lot ID:'$lotId' Capability Name:'$capabilityName' Capability Condition:'$capabilityCondition'")
                                def value = capabilityName
                                if (capabilityCondition.length() > 0)
                                {
                                    value = value + ";" + capabilityCondition
                                    capabilityChangeFlag = true
                                }
                                def request3 = new SetEquipmentMaintRequest(lot.getEquipmentId())
                                request3.getInputData().getObjectChanges().initChildParameter("tscLastRunCapability")
                                request3.getInputData().getObjectChanges().getChildParameter("tscLastRunCapability").setValue(value)

                                def reply3 = camstarService.setEquipmentMaint(request3)
                                if (reply3.isSuccessful())
                                {
                                    logger.info(reply3.getResponseData().getCompletionMsg())
                                    if (capabilityChangeFlag)
                                    {
                                        break
                                    }
                                }
                                else
                                {
                                    throw new Exception(reply3.getExceptionData().getErrorDescription())
                                }
                            }
                        }
                    }
                }
                else
                {
                    logger.info("Current Lot ID '$lotId' is not match with last lot in batch '$lastLotIdInBatch'!")
                }
            }
        }
    }
}