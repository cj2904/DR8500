package EapAddLotToPort

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.TscConstants
import sg.znt.pac.W06Constants
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
eap add lot to track in eqp port
''')
class EapAddLotToPort_1
{

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="InputXml")
    private String xml

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outboundRequest = new W02TrackInLotRequest(xml)
        def lotId = outboundRequest.getContainerName()
        def recipe = outboundRequest.getRecipeName()
        def recipeRev = outboundRequest.getRecipeRevision()
        def trackInQty = outboundRequest.getTrackInQty()
        def gdpw = outboundRequest.getGDPW()
        def qty = outboundRequest.getQty()
        def qty2 = outboundRequest.getQty2()
        def operator = outboundRequest.getOperatorId()
        def shift = outboundRequest.getShiftName()
        def product = outboundRequest.getProduct()
        def productFamily = outboundRequest.getProductFamily()
        def productLine = outboundRequest.getProductLine()
        def modelNumber = outboundRequest.getModelNumber()
        def measType1 = outboundRequest.getMeasType1()
        def measType2 = outboundRequest.getMeasType2()
        def measType3 = outboundRequest.getMeasType3()
        def measType4 = outboundRequest.getMeasType4()
        def capability = outboundRequest.getProcessCapability()
        def workflow = outboundRequest.getWorkflow()
        def owner = outboundRequest.getOwner()
        def workflowSteps = outboundRequest.getWorkflowSteps()
        def workflowNotes = outboundRequest.getWorkflowNotes()
        def lotStep = outboundRequest.getStep()
        def lotStepNotes = outboundRequest.getStepNotes()
        def tscDriveInRecipe = outboundRequest.getTscDriveInRecipe()
        def tscPreDRecipe = outboundRequest.getTscPreDRecipe()
        def tscResistance = outboundRequest.getTscResistance()
        def yieldFailureLimit = outboundRequest.getYieldFailureLimit()
        def tscIsRequiredRangeCheck = outboundRequest.getTscIsRequiredRangeCheck()
        def tscEpiThickness = outboundRequest.getTscEpiThickness()
        def waferQty = outboundRequest.getWaferQty()
        def eqpId = outboundRequest.getResourceName()
        def tscLastProcessEqp = outboundRequest.getTscLastProcessEqp()
        def waferSize = outboundRequest.getTrackInWaferList().size()

        //        def wipDataDO = wipDataManager.getDomainObject(eqpId + "-" + lotId)
        //        def lotLoc = wipDataDO.getElement("Lot Loc").getValue()

        def portList = cEquipment.getPortList()
        for(port in portList)
        {
            if(port.getPortId().equalsIgnoreCase(eqpId))
            {
                port.setLotId(lotId)

                //ProductLine
                if(productLine != null && productLine.length() > 0) {
                    port.getPropertyContainer().setString(W06Constants.MES_LOT_PRODUCTLINE, productLine)
                }
                else {
                    port.getPropertyContainer().setString(W06Constants.MES_LOT_PRODUCTLINE, "")
                }

                //ProductFamily
                if(productFamily != null && productFamily.length() > 0) {
                    port.getPropertyContainer().setString(W06Constants.MES_LOT_PRODUCT_FAMILY, productFamily)
                }
                else {
                    port.getPropertyContainer().setString(W06Constants.MES_LOT_PRODUCT_FAMILY, "")
                }

                //Product
                if(product != null && product.length() > 0) {
                    port.getPropertyContainer().setString(W06Constants.MES_LOT_PRODUCT, product)
                }
                else {
                    port.getPropertyContainer().setString(W06Constants.MES_LOT_PRODUCT, "")
                }

                //ModelNumber
                if(modelNumber != null && modelNumber.length() > 0) {
                    port.getPropertyContainer().setString(TscConstants.LOT_ATTR_MODEL_NUMBER, modelNumber)
                }
                else {
                    port.getPropertyContainer().setString(TscConstants.LOT_ATTR_MODEL_NUMBER, "")
                }

                //Recipe
                if(recipe != null && recipe.length() > 0) {
                    port.getPropertyContainer().setString(W06Constants.MES_LOT_RECIPE, recipe)
                }
                else {
                    port.getPropertyContainer().setString(W06Constants.MES_LOT_RECIPE, "")
                }

                //RecipeRevision
                if(recipeRev != null && recipeRev.length() > 0) {
                    port.getPropertyContainer().setString(W06Constants.MES_LOT_RECIPE_VERSION, recipeRev)
                }
                else {
                    port.getPropertyContainer().setString(W06Constants.MES_LOT_RECIPE_VERSION, "")
                }

                //Qty
                if(qty != null && qty.length() > 0) {
                    port.getPropertyContainer().setInteger(W06Constants.MES_LOT_QUANTITY, Integer.parseInt(qty))
                }
                else {
                    port.getPropertyContainer().setInteger(W06Constants.MES_LOT_QUANTITY, 0)
                }

                //Qty2
                if(qty2 != null && qty2.length() > 0) {
                    port.getPropertyContainer().setInteger(W06Constants.MES_LOT_QUANTITY2, Integer.parseInt(qty2))
                }
                else {
                    port.getPropertyContainer().setInteger(W06Constants.MES_LOT_QUANTITY2, 0)
                }

                //RejectQty
                port.getPropertyContainer().setInteger("RejectQty", 0)

                //GDPW
                if(gdpw != null && gdpw.length()>0) {
                    port.getPropertyContainer().setInteger(W06Constants.MES_LOT_GDPW, PacUtils.valueOfInteger(gdpw, 0))
                }

                //TrackInQty
                if(trackInQty != null && qty.length() > 0) {
                    //add track in qty
                    port.getPropertyContainer().setInteger(W06Constants.MES_LOT_TRACK_IN_QTY, Integer.parseInt(trackInQty))
                }
                else {
                    port.getPropertyContainer().setInteger(W06Constants.MES_LOT_TRACK_IN_QTY, 0)
                }

                //WorkFlow
                if(workflow != null && workflow.length() > 0) {
                    port.getPropertyContainer().setString(W06Constants.MES_LOT_WORKFLOW, workflow)
                }
                else {
                    port.getPropertyContainer().setString(W06Constants.MES_LOT_WORKFLOW, "")
                }

                if(tscDriveInRecipe != null && tscDriveInRecipe.length() > 0) {
                    port.getPropertyContainer().setString(TscConstants.LOT_MES_ATTR_DRIVE_IN_RECIPE,String.valueOf(tscDriveInRecipe))
                }
                else {
                    port.getPropertyContainer().setString(TscConstants.LOT_MES_ATTR_DRIVE_IN_RECIPE,"")
                }

                if(tscPreDRecipe != null && tscPreDRecipe.length() > 0) {
                    port.getPropertyContainer().setString(TscConstants.LOT_MES_ATTR_PRE_D_RECIPE,String.valueOf(tscPreDRecipe))
                }
                else {
                    port.getPropertyContainer().setString(TscConstants.LOT_MES_ATTR_PRE_D_RECIPE,"")
                }

                if(tscResistance != null && tscResistance.length() > 0) {
                    port.getPropertyContainer().setString(TscConstants.LOT_MES_ATTR_TSC_RESISTANCE,String.valueOf(tscResistance))
                    logger.info("setTscResistance=$tscResistance")
                }
                else {
                    port.getPropertyContainer().setString(TscConstants.LOT_MES_ATTR_TSC_RESISTANCE,"")
                    logger.info("setTscResistance=")
                }

                if(yieldFailureLimit != null && yieldFailureLimit.length() > 0) {
                    port.getPropertyContainer().setDouble(TscConstants.LOT_YIELD_FAILURE_LIMIT, PacUtils.valueOfDouble(yieldFailureLimit))
                    logger.info("yieldFailureLimit=$yieldFailureLimit")
                }

                if(tscIsRequiredRangeCheck != null && tscIsRequiredRangeCheck.length() > 0) {
                    logger.info("tscIsRequiredRangeCheck=$tscIsRequiredRangeCheck")

                    //0=false
                    //-1=true
                    if (tscIsRequiredRangeCheck.equalsIgnoreCase("0"))
                    {
                        port.getPropertyContainer().setBoolean(TscConstants.LOT_MES_ATTR_TSC_IS_REQUIRED_RANGE_CHECK, false)
                    }
                    else
                    {
                        port.getPropertyContainer().setBoolean(TscConstants.LOT_MES_ATTR_TSC_IS_REQUIRED_RANGE_CHECK, true)
                    }
                }

                if(tscEpiThickness != null && tscEpiThickness.length() > 0)
                {
                    port.getPropertyContainer().setString(TscConstants.PN_MES_ATTR_TSC_EPI_THICKNESS, tscEpiThickness)
                }
                else
                {
                    port.getPropertyContainer().setString(TscConstants.PN_MES_ATTR_TSC_EPI_THICKNESS, "")
                }

                if(tscLastProcessEqp != null && tscLastProcessEqp.length() > 0)
                {
                    port.getPropertyContainer().setString(TscConstants.LOT_MES_ATTR_LAST_PROCESS_EQP, tscLastProcessEqp)
                }
                else
                {
                    port.getPropertyContainer().setString(TscConstants.LOT_MES_ATTR_LAST_PROCESS_EQP, "")
                }
            }
        }
    }
}