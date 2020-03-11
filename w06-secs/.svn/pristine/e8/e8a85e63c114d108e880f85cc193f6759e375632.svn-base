package FurnaceEqpSelectRecipe

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsComposite
import de.znt.zsecs.composite.SecsU1Item
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.W06Util

@CompileStatic
@Deo(description='''
KE-Furnace specific function:<br/>
<b>pac to send "PP_SELECT       " command to equipment</b>
''')
class FurnaceEqpSelectRecipe_1
{

    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    private SecsGemService secsGemService
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        secsGemService = (SecsGemService) cEquipment.getExternalService()

        def port = ""
        def batchId = cEquipment.getPropertyContainer().getLong(cEquipment.getSystemId() + "_BatchID", new Long(-1)).longValue().toString()
        def batchLots = cEquipment.getPropertyContainer().getStringArray("LotSeq", new String[0])
        def batchQty = batchLots.size()
        def rcp = cMaterialManager.getCLotList(new LotFilterAll()).get(0).getRecipe()
        def eqp = cEquipment.getSystemId()
        def recipeId = eqp + "-" + rcp

        def recipe = recipeManager.getDomainObject(recipeId)

        if (recipe == null)
        {
            logger.error("$recipeId not found in recipe domain object!")
        }

        def recipeParam = recipe.getElement("EqpRecipe")

        if (recipeParam == null)
        {
            logger.error("Equipment recipe cannot be empty, please configure as Recipe Parameter with 'EqpRecipe' in Camstar!")
        }

        def recipeValue = recipeParam.getParameterValue()
        if (recipeValue==null || recipeValue.length()==0)
        {
            logger.error("Equipment recipe value cannot be empty, please configure 'EqpRecipe' value in Camstar!")
        }


        S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem(reserveStrLength("PP_SELECT")))
        //        request.addParameter(new SecsAsciiItem("0001"), new SecsAsciiItem(reserveStrLength("")))
        request.addParameter(new SecsAsciiItem("0002"), new SecsAsciiItem((reserveStrLength(batchId))))
        request.addParameter(new SecsAsciiItem("0003"), new SecsAsciiItem(reserveStrLength(recipeValue)))
        request.addParameter(new SecsAsciiItem("0004"), new SecsU1Item((short) batchQty))

        SecsComposite lotList = new SecsComposite()
        for(batchLot in batchLots)
        {
            lotList.add(new SecsAsciiItem(trimLotId(batchLot)))
        }

        while(lotList.size < 7)
        {
            lotList.add(new SecsAsciiItem(trimLotId("")))
        }
        request.addParameter(new SecsAsciiItem("0005"), lotList)

        SecsComposite reserve = new SecsComposite()
        request.addParameter(new SecsAsciiItem("0012"), reserve)

        def reply = secsGemService.sendS2F41HostCommandSend(request)
        if (reply.isCommandAccepted())
        {
            //ok
        }
        else
        {
            throw new Exception("Executing remote command PP_SELECT failed, reply message: " + reply.getHCAckMessage())
        }
    }

    private String reserveStrLength(String str)
    {
        while(str.length() < 16)
        {
            str += " ";
        }

        return str
    }

    private String trimLotId(String containerName)
    {
        String trimContainerName = W06Util.getLotIdWithTrimWorkOrder(containerName)
        return reserveStrLength(trimContainerName)
    }
}