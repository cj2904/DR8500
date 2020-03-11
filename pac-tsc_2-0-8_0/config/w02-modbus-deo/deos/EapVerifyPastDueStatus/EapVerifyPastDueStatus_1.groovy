package EapVerifyPastDueStatus

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintRequest
import sg.znt.camstar.semisuite.service.dto.GetMaintenanceStatusesRequest
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
verify eqp past due status
''')
class EapVerifyPastDueStatus_1
{
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outbound = new W02TrackInLotRequest(inputXmlDocument)
        def outboundLot = outbound.getContainerName()

        def lotList = cMaterialManager.getCLotList(new LotFilterAll())



        if(lotList.size() > 0)
        {
            def lot = cMaterialManager.getCLot(outboundLot)
            def eqpId = lot.getEquipmentId() //CS06-SPM-CLEAN
            logger.info("Jimmy's track in lot: '$lot', and eqp: '$eqpId'")

            def maineqpchild = new ArrayList<String>()
            def eqpreq = new GetEquipmentMaintRequest(eqpId)
            def eqpreply = cCamstarService.getEquipmentMaint(eqpreq)
            def cqitems = eqpreply.getResponseData().getObjectChanges().getChildEquipments().getChildEquipmentsItems()
            while(cqitems.hasNext())
            {
                def item = cqitems.next()
                def eqName = item.getName()
                maineqpchild.add(eqName)
            }

            def targeteqplogicalid =""
            def allRecipe = lot.getAllRecipeObj()
            for (recipe in allRecipe) {
                if(eqpId.equalsIgnoreCase(recipe.getMainEquipmentId())) {

                    def found = false
                    def childEqp = recipe.getEquipmentLogicalId()

                    for(int i =0 ; i<maineqpchild.size() ; i++)
                    {
                        logger.info("maineqpchild.get(i) " + maineqpchild.get(i) + " childEqp " + childEqp)
                        if(maineqpchild.get(i).equalsIgnoreCase(childEqp))
                        {
                            found = true
                            break
                        }
                    }

                    if(!found)
                    {
                        targeteqplogicalid = childEqp
                        logger.info("childEqp found = " + targeteqplogicalid)

                    }
                }
            }

            def childEqpRequest = new GetEquipmentMaintRequest()
            childEqpRequest.getInputData().getObjectToChange().setName(targeteqplogicalid)
            def childEqpReply = cCamstarService.getEquipmentMaint(childEqpRequest)
            def parentEqp = childEqpReply.getResponseData().getObjectChanges().getParentResource().getName()

            def mainEqpRequest = new GetEquipmentMaintRequest()
            mainEqpRequest.getInputData().getObjectToChange().setName(parentEqp)
            def mainEqpReply = cCamstarService.getEquipmentMaint(mainEqpRequest)
            def childEqps = mainEqpReply.getResponseData().getObjectChanges().getChildEquipments().getChildEquipmentsItems()

            def mainRequest = new GetMaintenanceStatusesRequest()
            mainRequest.getInputData().setResource(parentEqp)
            def mainReply = cCamstarService.getMaintenanceStatuses(mainRequest)
            if(mainReply.isSuccessful())
            {
                def records = mainReply.getAllMaintenanceRecord()
                while(records.hasNext())
                {
                    def record = records.next()
                    def item = record.getRow()
                    def resource = item.getResourceName()
                    def status = item.getMaintenanceState()
                    logger.info("Jimmy's main Eqp resource name and status: '$resource' = '$status'")

                    if(status.length() > 0 && status != null && status.equalsIgnoreCase("Past Due"))
                    {
                        throw new Exception("Parent Equipment : '$parentEqp' cannot track in. Please perform required maintenance before continue!")
                    }
                }
            }

            while(childEqps.hasNext())
            {
                def eqp = childEqps.next().getId()
                def childRequest = new GetMaintenanceStatusesRequest()
                childRequest.getInputData().setResource(eqp)
                def childReply = cCamstarService.getMaintenanceStatuses(childRequest)

                if(childReply.isSuccessful())
                {
                    def records = childReply.getAllMaintenanceRecord()
                    while(records.hasNext())
                    {
                        def record = records.next()
                        def item = record.getRow()
                        def resource = item.getResourceName()
                        def status = item.getMaintenanceState()
                        logger.info("Jimmy's child Eqp resource name and status: '$resource' = '$status'")

                        if(status.length() > 0 && status != null && status.equalsIgnoreCase("Past Due"))
                        {
                            throw new Exception("Child Equipment : '$eqp' cannot track in. Please perform required maintenance before continue!")
                        }
                    }
                }

            }
        }
    }
}