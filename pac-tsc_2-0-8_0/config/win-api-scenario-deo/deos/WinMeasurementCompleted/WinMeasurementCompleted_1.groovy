package WinMeasurementCompleted

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.WaferClassificationRequest
import sg.znt.camstar.semisuite.service.dto.WaferClassificationResponse
import sg.znt.camstar.semisuite.service.dto.WaferClassificationRequestDto.InputData.TscDetails.TscDetailsItem
import sg.znt.pac.domainobject.WaferClassificationDomainObject
import sg.znt.pac.domainobject.WaferClassificationDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.zwin.ZWinApiService
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import elemental.json.JsonObject
import groovy.transform.CompileStatic

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class WinMeasurementCompleted_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="ParamMap")
    private Map<String, String> paramMap

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="EventId")
    private String eventTid

    @DeoBinding(id="EventName")
    private String eventName

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager materialManager

    @DeoBinding(id="WaferClassificationManager")
    private WaferClassificationDomainObjectManager wcManager

    private CLot clot;
    private String winReplyCode = ""
    private String winReplyMsg = ""

    /**
     *
     */
    @DeoExecute(result="Reply")
    public JsonObject execute()
    {
        def lotId = paramMap.get("LotId")
        if (lotId == null || lotId.length()==0)
        {
            throw new Exception("Missing param 'LotId'!")
        }
        clot = materialManager.getCLot(lotId)

        int detailsQty = 0;
        String brokenQty = paramMap.get("BrokenWaferQty")
        String visualFailQty = paramMap.get("VisualFailWaferQty")
        String waferClassList = paramMap.get("WaferClassList")
		String employeeId = paramMap.get("EmployeeId")

        // Fill Data To Wafer Classification Touch Point
        WaferClassificationRequest request = new WaferClassificationRequest()
        request.getInputData().setContainer(lotId)
        request.getInputData().setTscBrokenWaferQty(brokenQty)
        request.getInputData().setTscVisualFailWaferQty(visualFailQty)
		request.getInputData().setEmployee(employeeId)

        //0.001-0.002;08-8N0A8A7;08-5N1A715;8;false,
        //0.003-0.004;08-F4AK11W;08-5N1A715;8;false,
        //0.010-0.020;08-F4AK16W;08-5N1A715;4;true
        String[] valuelist = ((String) waferClassList).split(";")
        for(String value : valuelist)
        {
            String[] si = value.split("=")
            String resClass = si[0]
            String newProduct = si[1]
            String qty = si[3]
            String crossProduct = si[4]

            TscDetailsItem item = request.getInputData().getTscDetails().addTscDetailsItem()
            item.setTscQty(qty)
            item.setTscResistanceClass(resClass)
            item.setTscIsCrossProduct(crossProduct)
            item.getTscNewProduct().setName(newProduct)
            item.getTscNewProduct().setRev(findWcProductVersion(resClass,newProduct))

            detailsQty = detailsQty + Integer.parseInt(qty);
        }

        //Validate Lot Qty Tally With Total Measurement Quantity
        boolean validInput = true
        if((detailsQty + Integer.parseInt(brokenQty) + Integer.parseInt(visualFailQty)) != clot.getQty())
        {
            logger.info("Lot Qty ["+clot.getQty()+"]")
            logger.info("Broken Qty ["+brokenQty+"]")
            logger.info("VisualFail Qty ["+visualFailQty+"]")
            logger.info("Wafer Class Qty ["+detailsQty+"]")
            
            validInput = false
            winReplyCode = 999
            winReplyMsg = "Error! Total Measured Wafer Qty Not Equal With Lot Qty. PAC Cannot Perform Wafer Classification to MES."
        }

        if(validInput)
        {
            WaferClassificationResponse reply = cCamstarService.waferClassification(request)
            if(reply.isSuccessful())
            {
                winReplyCode = ""
                winReplyMsg = reply.getResponseData().getCompletionMsg()
                materialManager.removeCLot(clot)
                wcManager.removeAllDomainObject()
            }
            else
            {
                winReplyCode = reply.getExceptionData().getErrorCode();
                winReplyMsg = reply.getExceptionData().getErrorDescription()
            }
        }

        ZWinApiService winApiService = ((ZWinApiService)secsGemService)
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("LotId",lotId)
        param.put("EquipmentId", cEquipment.getSystemId())
        param.put("ShowMessage", "true")
        
        return winApiService.buildEventReplyMessage(eventTid, eventName, param, winReplyCode, winReplyMsg);
        
    }

    String findWcProductVersion(String res, String newProdut)
    {
        List<WaferClassificationDomainObject> wcall = wcManager.getAllDomainObject()
        for(int i=0 ; i < wcall.size(); i++)
        {
            WaferClassificationDomainObject item = (WaferClassificationDomainObject) wcall[i]
            if(item.getResistanceClass().equals(res) && item.getNewProduct().equals(newProdut))
            {
                return item.getNewProductRev()
            }
        }
        return ""
    }
}