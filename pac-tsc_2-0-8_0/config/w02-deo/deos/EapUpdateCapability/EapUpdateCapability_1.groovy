package EapUpdateCapability

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintRequest
import sg.znt.camstar.semisuite.service.dto.SetEqpProcessCapabilityRequest
import sg.znt.camstar.semisuite.service.dto.SetEquipmentMaintRequest
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.mes.ProcessCapability
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''

''')
class EapUpdateCapability_1
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

    private int thruput2 =0
    private CLot lot

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def req = new W02CompleteOutLotRequest(inputXml)
        def lotId = req.getContainerName()
        lot = cMaterialManager.getCLot(lotId)

        logger.info("lot isCancelTrackIn '" + req.isCancelTrackIn() + "'")
        if (req.isCancelTrackIn())
        {
            logger.info("lot '" + lotId + "' is Cancel TrackIn. Do Not Perform Update Capability")
            return
        }

        int trackOutQty = Integer.parseInt(req.getTrackOutQty())
        try
        {
            thruput2 = Integer.parseInt(req.getThruputQty2())
        }
        catch (Exception e)
        {
            e.printStackTrace()
        }

        if (lot != null)
        {
            def capability = new ProcessCapability(lot.getProcessCapability())
            updateEquipmentCapability(capability)
        }

        /**
         def lotList = cMaterialManager.getCLotList(new LotFilterAll())
         def lotSize = lotList.size()
         if (lotSize == 1)
         {
         if (lotId !=null)
         {
         def capability = new ProcessCapability(lot.getProcessCapability())
         updateEquipmentCapability(capability)
         }
         }
         else
         {
         def lotsId = ""
         for (lt in lotList)
         {
         if (lotsId.length()>0)
         {
         lotsId = lotsId + ","
         }
         lotsId = lt.getId()
         }
         if (lotsId.length()>0)
         {
         logger.info("Ignore capability check since contain at least one lot '" + lotsId + "'")
         }
         }
         **/
    }

    private void updateEquipmentCapability(ProcessCapability requireCapability)
    {
        def request = new GetEquipmentMaintRequest(lot.getEquipmentId())
        def reply = camstarService.getEquipmentMaint(request)
        ArrayList<ProcessCapability> capabilityList = new ArrayList()
        if (reply.isSuccessful())
        {
            def items = reply.getResponseData().getObjectChanges().getEqpProcessCapability().getEqpProcessCapabilityItems()
            while(items.hasNext())
            {
                def item = items.next()
                ProcessCapability capability = new ProcessCapability(item.getProcessCapability().getName())
                capabilityList.add(capability)
            }
        }
        requireCapability.updateCapabilityList(capabilityList)

        //get thruput from outbout and add current outbound
        //TODO: need to verify thruput capability
        //MZ //SZ
        def accumalatedThruput = equipment.getThruput2AtTrackIn()+thruput2

        def request2 = new SetEqpProcessCapabilityRequest()
        request2.getInputData().setResource(lot.getEquipmentId())
        for (var in capabilityList)
        {
            def item = request2.getInputData().getDetails().addDetailsItem()
            item.setAvailability("TRUE")
            if (var.isActivated())
            {
                if (var.getMaxThruput()==-1 && var.getMinThruput()==-1)
                {
                    item.setActivationStatus("TRUE")
                }
                else
                {
                    if (var.getMaxThruput()!=-1)
                    {
                        if ((accumalatedThruput+equipment.getThruput2WarningQty())>var.getMaxThruput())
                        {
                            item.setActivationStatus("FALSE")
                        }
                        else
                        {
                            item.setActivationStatus("TRUE")
                        }
                    }
                    else if (accumalatedThruput>=var.getMinThruput())
                    {
                        if (accumalatedThruput>=var.getMinThruput())
                        {
                            item.setActivationStatus("TRUE")
                        }
                        else
                        {
                            item.setActivationStatus("FALSE")
                        }
                    }
                }
            }
            else
            {
                item.setActivationStatus("FALSE")
            }
            item.getProcessCapability().setName(var.getName())
        }

        def reply2=camstarService.setEqpProcessCapability(request2)

        if(requireCapability.getName().length()>0)
        {
            def request3 = new SetEquipmentMaintRequest(lot.getEquipmentId())
            //TODO: need change to new attribute for lastProcessCapability field
            //request3.getInputData().getObjectChanges().setVendorSerialNumber(requireCapability.getName())
            request3.getInputData().getObjectChanges().initChildParameter("tscLastRunCapability")
            request3.getInputData().getObjectChanges().getChildParameter("tscLastRunCapability").setValue(requireCapability.getName())
            def reply3 = camstarService.setEquipmentMaint(request3)
        }

    }
}