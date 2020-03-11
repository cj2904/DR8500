package EapAddLot

import groovy.transform.TypeChecked

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.EquipmentIdentifyService
import sg.znt.pac.TscConstants
import sg.znt.pac.domainobject.EquipmentMaterialDomainObjectManager
import sg.znt.pac.domainobject.PmManager
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.domainobject.RecipeParameter
import sg.znt.pac.domainobject.WipDataDomainObject
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.domainobject.WipDataDomainObjectSet
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.machine.TscEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.CWafer
import sg.znt.pac.material.CWaferImpl
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.material.WaferFilterAll
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import sg.znt.services.camstar.outbound.W02TrackInLotRequest.PM
import de.znt.pac.ItemNotFoundException
import de.znt.pac.deo.annotations.*
import de.znt.pac.domainobject.filter.FilterAllDomainObjects

@Deo(description='''
Create lot when receive track in outbound
''')
class EapAddLot_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    private TscEquipment mainEquipment

    @DeoBinding(id="MainEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="EquipmentMaterialDomainObjectManager")
    private EquipmentMaterialDomainObjectManager equipmentMaterialDomainObjectManager

    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager

    @DeoBinding(id="PMManager")
    private PmManager pmManager

    @DeoBinding(id="WipDataManager")
    private WipDataDomainObjectManager wipDataManager

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    CLot cLot;

    @DeoBinding(id="EquipmentIdentifyService")
    private EquipmentIdentifyService equipmentIdentifyService
    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
        mainEquipment = (TscEquipment) cEquipment;
        W02TrackInLotRequest outboundRequest = new W02TrackInLotRequest(inputXmlDocument)
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
        if (waferSize == 1)
        {
            logger.info("CAMSTAR: TrackIn: " + lotId + "|" + waferQty + "|" + outboundRequest.getTrackInWaferList().get(0).getWaferScribeNumber() + "|" + recipe + "|" + eqpId)
        }
        else
        {
            logger.info("CAMSTAR: TrackIn: " + lotId + "|" + waferQty + "|" + recipe + "|" + eqpId)
        }

        try
        {
            cLot = cMaterialManager.getCLot(lotId)
        }
        catch (ItemNotFoundException e)
        {
            cLot = cMaterialManager.createCLot(lotId)
            cMaterialManager.addCLot(cLot)
        }

        def lotContainer = cLot.getPropertyContainer()
        if(productLine != null && productLine.length() > 0)
        {
            cLot.setProductLine(productLine)
        }
        else
        {
            cLot.setProductLine("")
        }
        if(modelNumber != null && modelNumber.length() > 0)
        {
            lotContainer.setString(TscConstants.LOT_ATTR_MODEL_NUMBER, modelNumber)
        }
        else
        {
            lotContainer.setString(TscConstants.LOT_ATTR_MODEL_NUMBER, "")
        }

        if(measType1 != null && measType1.length() > 0)
        {
            lotContainer.setString(TscConstants.LOT_ATTR_MEAS_TYPE_1, measType1)
        }
        else
        {
            lotContainer.setString(TscConstants.LOT_ATTR_MEAS_TYPE_1, "")
        }

        if(measType2 != null && measType2.length() > 0)
        {
            lotContainer.setString(TscConstants.LOT_ATTR_MEAS_TYPE_2, measType2)
        }
        else
        {
            lotContainer.setString(TscConstants.LOT_ATTR_MEAS_TYPE_2, "")
        }

        if(measType3 != null && measType3.length() > 0)
        {
            lotContainer.setString(TscConstants.LOT_ATTR_MEAS_TYPE_3, measType3)
        }
        else
        {
            lotContainer.setString(TscConstants.LOT_ATTR_MEAS_TYPE_3, "")
        }

        if(measType4 != null && measType4.length() > 0)
        {
            lotContainer.setString(TscConstants.LOT_ATTR_MEAS_TYPE_4, measType4)
        }
        else
        {
            lotContainer.setString(TscConstants.LOT_ATTR_MEAS_TYPE_4, "")
        }

        if (capability !=null && capability.length()>0)
        {
            cLot.setProcessCapability(capability)
        }
        else
        {
            cLot.setProcessCapability("")
        }

        if(recipe != null && recipe.length() > 0)
        {
            cLot.setRecipe(recipe)
        }
        else
        {
            cLot.setRecipe("")
        }


        if(recipeRev != null && recipeRev.length() > 0)
        {
            cLot.setRecipeVersion(recipeRev)
        }
        else
        {
            cLot.setRecipeVersion("")
        }

        if(qty != null && qty.length() > 0)
        {
            cLot.setQty(Integer.parseInt(qty))
        }
        else
        {
            cLot.setQty(0)
        }

        if(gdpw != null && gdpw.length()>0)
        {
            cLot.setGDPW(PacUtils.valueOfInteger(gdpw, 0))
        }

        if(qty2 != null && qty2.length() > 0)
        {
            cLot.setQty2(Integer.parseInt(qty2))
        }
        else
        {
            cLot.setQty2(0)
        }

        if(operator != null && qty2.length() > 0)
        {
            cLot.setOperatorId(operator)
        }
        else
        {
            cLot.setOperatorId("")
        }

        if(shift != null && shift.length() > 0)
        {
            cLot.setShiftName(shift)
        }
        else
        {
            cLot.setShiftName("")
        }

        if(product != null && product.length() > 0)
        {
            cLot.setProduct(product)
        }
        else
        {
            cLot.setProduct("")
        }

        if(productFamily != null && productFamily.length() > 0)
        {
            cLot.setProductFamily(productFamily)
        }
        else
        {
            cLot.setProductFamily("")
        }

        if(trackInQty != null && qty.length() > 0)
        {
            //add track in qty
            cLot.setTrackInQty(Integer.parseInt(trackInQty))
        }
        else
        {
            cLot.setTrackInQty(0)
        }

        if(workflow != null && workflow.length() > 0)
        {
            cLot.setWorkflow(workflow)
        }
        else
        {
            cLot.setWorkflow("")
        }

        if(owner != null && owner.length() > 0)
        {
            cLot.setOwner(owner)
        }
        else
        {
            cLot.setOwner("")
        }

        if(workflowNotes != null && workflowNotes.length() > 0)
        {
            cLot.setWorkflowNotes(workflowNotes)
        }
        else
        {
            cLot.setWorkflowNotes("")
        }

        if(workflowSteps != null && workflowSteps.length > 0)
        {
            cLot.setWorkflowSteps(workflowSteps)
        }
        else
        {
            cLot.setWorkflowSteps("")
        }

        if(lotStep != null && lotStep.length() > 0)
        {
            cLot.setStep(lotStep)
        }
        else
        {
            cLot.setStep("")
        }

        if(lotStepNotes != null && lotStepNotes.length() > 0)
        {
            cLot.setStepNotes(lotStepNotes)
        }
        else
        {
            cLot.setStepNotes("")
        }

        if(tscDriveInRecipe != null && tscDriveInRecipe.length() > 0)
        {
            cLot.getPropertyContainer().setString(TscConstants.LOT_MES_ATTR_DRIVE_IN_RECIPE,String.valueOf(tscDriveInRecipe))
        }
        else
        {
            cLot.getPropertyContainer().setString(TscConstants.LOT_MES_ATTR_DRIVE_IN_RECIPE,"")
        }

        if(tscPreDRecipe != null && tscPreDRecipe.length() > 0)
        {
            cLot.getPropertyContainer().setString(TscConstants.LOT_MES_ATTR_PRE_D_RECIPE,String.valueOf(tscPreDRecipe))
        }
        else
        {
            cLot.getPropertyContainer().setString(TscConstants.LOT_MES_ATTR_PRE_D_RECIPE,"")
        }

        if(tscResistance != null && tscResistance.length() > 0)
        {
            cLot.getPropertyContainer().setString(TscConstants.LOT_MES_ATTR_TSC_RESISTANCE,String.valueOf(tscResistance))
            logger.info("setTscResistance=$tscResistance")
        }
        else
        {
            cLot.getPropertyContainer().setString(TscConstants.LOT_MES_ATTR_TSC_RESISTANCE,"")
            logger.info("setTscResistance=")
        }

        if(yieldFailureLimit != null && yieldFailureLimit.length() > 0)
        {
            cLot.getPropertyContainer().setDouble(TscConstants.LOT_YIELD_FAILURE_LIMIT, PacUtils.valueOfDouble(yieldFailureLimit))
            logger.info("yieldFailureLimit=$yieldFailureLimit")
        }

        if(tscIsRequiredRangeCheck != null && tscIsRequiredRangeCheck.length() > 0)
        {
            logger.info("tscIsRequiredRangeCheck=$tscIsRequiredRangeCheck")

            //0=false
            //-1=true
            if (tscIsRequiredRangeCheck.equalsIgnoreCase("0"))
            {
                cLot.getPropertyContainer().setBoolean(TscConstants.LOT_MES_ATTR_TSC_IS_REQUIRED_RANGE_CHECK, false)
            }
            else
            {
                cLot.getPropertyContainer().setBoolean(TscConstants.LOT_MES_ATTR_TSC_IS_REQUIRED_RANGE_CHECK, true)
            }
        }

        if(tscEpiThickness != null && tscEpiThickness.length() > 0)
        {
            cLot.getPropertyContainer().setString(TscConstants.PN_MES_ATTR_TSC_EPI_THICKNESS, tscEpiThickness)
        }
        else
        {
            cLot.getPropertyContainer().setString(TscConstants.PN_MES_ATTR_TSC_EPI_THICKNESS, "")
        }

        if(tscLastProcessEqp != null && tscLastProcessEqp.length() > 0)
        {
            cLot.getPropertyContainer().setString(TscConstants.LOT_MES_ATTR_LAST_PROCESS_EQP, tscLastProcessEqp)
        }
        else
        {
            cLot.getPropertyContainer().setString(TscConstants.LOT_MES_ATTR_LAST_PROCESS_EQP, "")
        }

        /*
         * 
         def attributes = TscConfig.getAttributeID()
         for (attr in attributes) 
         {
         def paramValue = outboundRequest.getParameter(attr);
         cLot.getPropertyContainer().setString(attr, paramValue);
         }
         */

        cLot.setRejectQty(0)
        cLot.setEquipmentId(eqpId)

        addTrackInWaferInformation(cLot,outboundRequest)
        addPmInformation(outboundRequest)
        addLotWipData(outboundRequest)
        addWaferWipData(cLot,outboundRequest)
        addChildEquipmentInfo(outboundRequest)

        addRecipeInformation(outboundRequest)
        //TODO:Verify recipe condition check is valid

        cEquipment.setCurrentLotId(lotId)
        cEquipment.setEquipmentDummyLot(outboundRequest.getResourceName(), outboundRequest.getDummyLotId())
        //addLotCarriersInformation(cLot)

        setBatchInfo(outboundRequest)

        setLotChildEqp(outboundRequest)
    }

    public void setLotChildEqp(W02TrackInLotRequest outboundRequest)
    {
        def childeqp = mainEquipment.getPropertyContainer().getStringArray(outboundRequest.getResourceName() + "_ChildEquipments", new String[0])
        cLot.getPropertyContainer().setStringArray(outboundRequest.getResourceName() + "_ChildEquipments", childeqp)
    }

    public void setBatchInfo(W02TrackInLotRequest outboundRequest)
    {
        def resourceName = cEquipment.getSystemId()
        def container = cEquipment.getPropertyContainer()
        def batchTrackInLots = container.getStringArray(outboundRequest.getResourceName() + "_BatchTrackInLots", new String[0]);
        def batchTotalQty = container.getInteger(outboundRequest.getResourceName() + "_BatchTotalQty", -1);
        def batchId = container.getLong(outboundRequest.getResourceName() + "_BatchID", new Long(-1)).longValue();

        if (batchTrackInLots.length >0)
        {
            def lotContainer = cLot.getPropertyContainer()
            lotContainer.setStringArray("BatchTrackInLots", batchTrackInLots);
            lotContainer.setInteger("BatchTotalQty", batchTotalQty);
            lotContainer.setLong("BatchID", batchId);
            if(batchTrackInLots[0].equalsIgnoreCase(cLot.getId()))
            {
                logger.info("Lot '" + cLot.getId() + "' is in first batch in '" + batchTrackInLots + "'");
                cLot.getPropertyContainer().setBoolean(TscConstants.MATERIAL_ATTR_FIRST_LOT_IN_BATCH,true)
                container.setStringArray(resourceName + "_BatchTrackInLots2", batchTrackInLots)
            }
            else
            {
                logger.info("Lot '" + cLot.getId() + "' is not in first batch in '" + batchTrackInLots + "'");
                cLot.getPropertyContainer().setBoolean(TscConstants.MATERIAL_ATTR_FIRST_LOT_IN_BATCH,false)
            }
        }
        else
        {
            if(cMaterialManager.getCLotList(new LotFilterAll()).size()==1)
            {
                cLot.getPropertyContainer().setBoolean(TscConstants.MATERIAL_ATTR_FIRST_LOT_IN_BATCH,true)
                container.setStringArray(resourceName + "_BatchTrackInLots2", batchTrackInLots)
            }
            else
            {
                cLot.getPropertyContainer().setBoolean(TscConstants.MATERIAL_ATTR_FIRST_LOT_IN_BATCH,false)
            }
        }
    }

    public void addChildEquipmentInfo(W02TrackInLotRequest request)
    {
        def ceqList = request.getChildEquipmentList()
        def childEqList = new ArrayList<String>();
        def childEqpsList = new ArrayList<String>();
        for (ceq in ceqList)
        {
            childEqList.add(ceq.CHILD_EQUIPMENT_LOGICAL_ID)
            childEqpsList.add(ceq.CHILD_EQUIPMENT_ID)
        }
        mainEquipment.getPropertyContainer().setStringArray(request.getResourceName() + "_ChildEquipments", childEqList.toArray(new String[0]))
        mainEquipment.getPropertyContainer().setStringArray(request.getResourceName() + "_ChildEquipmentsId", childEqpsList.toArray(new String[0]))

        if (ceqList==null || ceqList.size()==0)
        {
            return
        }

        childEqList.clear()
        def childEqIdList = new ArrayList<String>();
        for (ceq in ceqList)
        {
            childEqList.add(ceq.CHILD_EQUIPMENT_ID)
            childEqIdList.add(ceq.CHILD_EQUIPMENT_LOGICAL_ID)
            //child equipment thruput requirement need to
            def thruputReq = ceq.CEQ_THRUPUT_REQUIREMENT
            def pmList = ceq.PM_LIST
            if (pmList == null || pmList.size()==0)
            {
                continue
            }
            for (item in pmList)
            {
                def pm = pmManager.createDomainObject(ceq.CHILD_EQUIPMENT_ID + "-" + item.PM_NAME)
                pm.setPmName(item.PM_NAME)
                pm.setEquipmentId(ceq.CHILD_EQUIPMENT_ID)
                pm.setDummyLoyId(ceq.CEQ_DUMMY_LOT_ID)
                pm.setPmNextDateDue(item.PM_NEXT_DATE_DUE)
                pm.setTolerancePeriod(item.PM_TOLERANCE_PERIOD)
                pm.setWarningPeriod(item.PM_WARNING_PERIOD)
                pm.setPmQty(PacUtils.valueOfInteger(item.PM_QTY,-1))
                pm.setPmQty2(PacUtils.valueOfInteger(item.PM_QTY2,-1))
                pm.setPmWarningQty(PacUtils.valueOfInteger(item.PM_WARNING_QTY,-1))
                pm.setPmWarningQty2(PacUtils.valueOfInteger(item.PM_WARNING_QTY2,-1))
                pm.setThruputQty(PacUtils.valueOfInteger(item.PM_THRUPUT_QTY,-1))
                pm.setThruputQty2(PacUtils.valueOfInteger(item.PM_THRUPUT_QTY2,-1))
                pm.setToleranceQty(PacUtils.valueOfInteger(item.PM_TOLERANCE_QTY,-1))
                pm.setToleranceQty2(PacUtils.valueOfInteger(item.PM_TOLERANCE_QTY2,-1))
                if (item.PM_NAME.equalsIgnoreCase(ceq.CEQ_THRUPUT_REQUIREMENT))
                {
                    pm.setThruputControl(true)
                }
                pm.setCalculatedEndDate(item.PM_CENDDATE)
                pm.setMRFirstDate(item.PM_MRFIRSTDATE)
                pm.setFirstDate(item.PM_FIRSTDATE)
                pm.setThruputQtyAdj2(PacUtils.valueOfInteger(item.PM_TPQTYADJ2,-1))
                pmManager.addDomainObject(pm)
            }
            def equipment = equipmentIdentifyService.getEquipmentBySystemId(request.getResourceName())
            if (equipment ==null)
            {
                childEqIdList.add(request.getResourceName())
                childEqList.add(request.getResourceName())
            }

            if (equipment != null)
            {
                equipment.setChildEquipmentIds(childEqIdList)
                equipment.setChildEquipments(childEqList)
            }
            else
            {
                //Handle virtual main equipment
                def childEqIds = cEquipment.getChildEquipmentIds()
                for (childEqId in childEqIds)
                {
                    if (childEqIdList.indexOf(childEqId)==-1)
                    {
                        childEqIdList.add(childEqId)
                    }
                }
                cEquipment.setChildEquipmentIds(childEqIdList)

                def childEqs = cEquipment.getChildEquipments()
                for (childEq in childEqs)
                {
                    if (childEqList.indexOf(childEq)==-1)
                    {
                        childEqList.add(childEq)
                    }
                }
                cEquipment.setChildEquipments(childEqList)
            }
        }
    }

    public void addPmInformation(W02TrackInLotRequest request)
    {
        def equipment = equipmentIdentifyService.getEquipmentBySystemId(request.getResourceName())
        if (equipment !=null)
        {
            equipment.setThruputRequirement(request.getThruputRequirement())
        }

        def pmList = request.getPMList()
        for (item in pmList)
        {
            def pm = pmManager.createDomainObject(request.getResourceName() + "-" + item.PM_NAME)
            pm.setPmName(item.PM_NAME)
            pm.setEquipmentId(request.getResourceName())
            pm.setPmNextDateDue(item.PM_NEXT_DATE_DUE)
            pm.setTolerancePeriod(item.PM_TOLERANCE_PERIOD)
            pm.setWarningPeriod(item.PM_WARNING_PERIOD)
            pm.setPmQty(PacUtils.valueOfInteger(item.PM_QTY,-1))
            pm.setPmQty2(PacUtils.valueOfInteger(item.PM_QTY2,-1))
            pm.setPmWarningQty(PacUtils.valueOfInteger(item.PM_WARNING_QTY,-1))
            pm.setPmWarningQty2(PacUtils.valueOfInteger(item.PM_WARNING_QTY2,-1))
            pm.setThruputQty(PacUtils.valueOfInteger(item.PM_THRUPUT_QTY,-1))
            pm.setThruputQty2(PacUtils.valueOfInteger(item.PM_THRUPUT_QTY2,-1))
            pm.setToleranceQty(PacUtils.valueOfInteger(item.PM_TOLERANCE_QTY,-1))
            pm.setToleranceQty2(PacUtils.valueOfInteger(item.PM_TOLERANCE_QTY2,-1))
            if (item.PM_NAME.equalsIgnoreCase(request.getThruputRequirement()))
            {
                pm.setThruputControl(true)
            }

            pm.setCalculatedEndDate(item.PM_CENDDATE)
            pm.setMRFirstDate(item.PM_MRFIRSTDATE)
            pm.setFirstDate(item.PM_FIRSTDATE)
            pm.setThruputQtyAdj2(PacUtils.valueOfInteger(item.PM_TPQTYADJ2,-1))

            pmManager.addDomainObject(pm)
        }
    }


    public void addWaferWipData(CLot lot, W02TrackInLotRequest request)
    {
        def waferList = lot.getWaferList(new WaferFilterAll())
        for (wafer in waferList)
        {
            def wipDataList = request.getWipDataItemList()
            if (wipDataList.size()>0)
            {
                def waferWipData = wipDataManager.createDomainObject(request.getContainerName() + "-" + wafer.getWaferScribeID())
                waferWipData.setObjectType(WipDataDomainObjectSet.OBJECT_TYPE_WAFER)
                waferWipData.setEquipmentId(request.getResourceName())
                waferWipData.setWipDataName(request.getWipDataSetupName())
                for (var in wipDataList)
                {
                    if (var.WIP_DATA_IS_WAFER_DATA.equalsIgnoreCase("TRUE") || !var.WIP_DATA_IS_WAFER_DATA.equalsIgnoreCase("0"))
                    {
                        def item = new WipDataDomainObject(var.WIP_DATA_NAME)
                        item.setServiceName(var.WIP_DATA_SERVICE_NAME)
                        item.setIsHidden(var.WIP_DATA_IS_HIDDEN.equalsIgnoreCase("TRUE") || !var.WIP_DATA_IS_HIDDEN.equalsIgnoreCase("0"))
                        item.setIsWaferData(var.WIP_DATA_IS_WAFER_DATA.equalsIgnoreCase("TRUE") || !var.WIP_DATA_IS_HIDDEN.equalsIgnoreCase("0"))
                        item.setIsRequired(var.WIP_DATA_IS_REQUIRED.equalsIgnoreCase("TRUE") || !var.WIP_DATA_IS_REQUIRED.equalsIgnoreCase("0"))
                        item.setEquipmentId(request.getResourceName())
                        item.setLowerLimit(var.WIP_DATA_LOWER_LIMIT)
                        item.setMaxDataValue(var.WIP_DATA_MAX_VALUE)
                        item.setMinDataValue(var.WIP_DATA_MIN_VALUE)
                        item.setServiceType(var.WIP_DATA_SERVICE_NAME)
                        item.setUpperLimit(var.WIP_DATA_UPPER_LIMIT)
                        item.setValidValues(var.WIP_DATA_VALID_VALUES)
                        item.setDefaultValue(var.WIP_DATA_DEFAULT_VALUE)
                        item.setUom(var.WIP_DATA_UOM)
                        item.setUomNotes(var.WIP_DATA_UOM_NOTES)
                        waferWipData.addElement(item)
                    }
                }

                if (waferWipData.getAll(new FilterAllDomainObjects()).size()>0)
                {
                    wipDataManager.addDomainObject(waferWipData)
                }

            }
        }
    }


    public void addLotWipData(W02TrackInLotRequest request)
    {
        def wipDataList = request.getWipDataItemList()
        if (wipDataList.size()>0)
        {
            def setFound = null;
            setFound = wipDataManager.getDomainObject(request.getResourceName() + "-" +request.getContainerName())

            if(setFound==null)
            {
                def lotWipData = wipDataManager.createDomainObject(request.getResourceName() + "-" +request.getContainerName())
                lotWipData.setObjectType(WipDataDomainObjectSet.OBJECT_TYPE_LOT)
                lotWipData.setLotId(request.getContainerName())
                lotWipData.setEquipmentId(request.getResourceName())
                lotWipData.setWipDataName(request.getWipDataSetupName())
                for (var in wipDataList)
                {
                    if (var.WIP_DATA_IS_WAFER_DATA.equalsIgnoreCase("FALSE") || var.WIP_DATA_IS_WAFER_DATA.equalsIgnoreCase("0"))
                    {
                        def item = new WipDataDomainObject(var.WIP_DATA_NAME)
                        item.setServiceName(var.WIP_DATA_SERVICE_NAME)
                        item.setIsHidden(var.WIP_DATA_IS_HIDDEN.equalsIgnoreCase("TRUE") || !var.WIP_DATA_IS_HIDDEN.equalsIgnoreCase("0"))
                        item.setIsWaferData(var.WIP_DATA_IS_WAFER_DATA.equalsIgnoreCase("TRUE") || !var.WIP_DATA_IS_HIDDEN.equalsIgnoreCase("0"))
                        item.setIsRequired(var.WIP_DATA_IS_REQUIRED.equalsIgnoreCase("TRUE") || !var.WIP_DATA_IS_REQUIRED.equalsIgnoreCase("0"))
                        item.setEquipmentId(request.getResourceName())
                        item.setLowerLimit(var.WIP_DATA_LOWER_LIMIT)
                        item.setMaxDataValue(var.WIP_DATA_MAX_VALUE)
                        item.setMinDataValue(var.WIP_DATA_MIN_VALUE)
                        item.setServiceType(var.WIP_DATA_SERVICE_NAME)
                        item.setUpperLimit(var.WIP_DATA_UPPER_LIMIT)
                        item.setValidValues(var.WIP_DATA_VALID_VALUES)
                        item.setDefaultValue(var.WIP_DATA_DEFAULT_VALUE)
                        item.setValue(var.WIP_DATA_VALUE)
                        item.setUom(var.WIP_DATA_UOM)
                        item.setUomNotes(var.WIP_DATA_UOM_NOTES)
                        lotWipData.addElement(item)
                    }
                }

                if (lotWipData.getAll(new FilterAllDomainObjects()).size()>0)
                {
                    wipDataManager.addDomainObject(lotWipData)
                }
            }
        }
    }

    public void addRecipeInformation(W02TrackInLotRequest request)
    {
        if (request.getRecipeName().length()==0)
        {
            return
        }

        def mainRecipe = recipeManager.createDomainObject(request.getResourceName() + "-" + request.getRecipeName())
        def paramList = request.getRecipeParamList();
        mainRecipe.setRecipeName(request.getRecipeName())
        mainRecipe.setMainRecipeName(request.getRecipeName())
        mainRecipe.setRecipeRevision(request.getRecipeRevision())
        mainRecipe.setEquipmentId(request.getResourceName())
        mainRecipe.setMainEquipmentId(request.getResourceName())
        mainRecipe.setIsSubRecipe("FALSE")

        def lastProcessCapability = request.getLastProcessCapability()
        if (lastProcessCapability.contains(";"))
        {
            String[] arrLastCapability = lastProcessCapability.split(";")
            mainRecipe.setLastCapability(lastProcessCapability)
            mainRecipe.setCapabilityCondition(arrLastCapability[1])
        }
        else
        {
            mainRecipe.setLastCapability(lastProcessCapability)
            mainRecipe.setCapabilityCondition("")
        }

        mainRecipe.setRequireCapability(cLot.getProcessCapability())

        def PMList = request.getPMList()
        for (pm in PMList)
        {
            if (request.getThruputRequirement().equalsIgnoreCase(pm.PM_NAME))
            {
                mainRecipe.setThruput(pm.PM_THRUPUT_QTY)
                mainRecipe.setThruput2(pm.PM_THRUPUT_QTY2)
            }
        }

        for (recipeParam in paramList)
        {
            def param = new RecipeParameter(recipeParam.getParamName())
            param.setMaxValue(recipeParam.getMaxDataValue())
            param.setMinValue(recipeParam.getMinDataValue())
            param.setDataType(recipeParam.getFieldType())
            param.setParameterValue(recipeParam.getParamValue())
            param.setValidValues(recipeParam.getValidValues())
            mainRecipe.addElement(param)
        }
        recipeManager.addDomainObject(mainRecipe)

        def subRecipeList = request.getSubRecipeList()

        if (subRecipeList.size()>0)
        {
            mainRecipe.setContainSubRecipe(true)
        }

        //sum track in qty here
        def totalqty = 0
        def getCLotList = cMaterialManager.getCLotList(new LotFilterAll())
        for(lot in getCLotList)
        {
            if(lot.getId().equalsIgnoreCase(request.getContainerName()))
            {
                logger.info("lot.getTrackInQty()" + lot.getTrackInQty())
                logger.info("request.getTrackInQty() " + request.getTrackInQty())
                //lot.setTrackInQty(lot.getTrackInQty()+Integer.parseInt(request.getTrackInQty()))
                totalqty = totalqty + lot.getTrackInQty();
                logger.info("lot.getTrackInQty() after " + lot.getTrackInQty())
            }
        }

        for(lot in getCLotList)
        {
            if(lot.getEquipmentId().equalsIgnoreCase(request.getResourceName()))
            {
                if(lot.getId().equalsIgnoreCase(request.getContainerName())==false)
                {
                    logger.info("lot.getTrackInQty()" + lot.getTrackInQty())
                    totalqty = totalqty + lot.getTrackInQty()
                    logger.info("lot.totalqty()" + totalqty)
                }
            }
        }


        for (subRecipe in subRecipeList)
        {
            def ceqId = getEquipmentId(request, subRecipe.getEquipmentLogicalId())
            def subRecipeObj = recipeManager.createNewDomainObject(ceqId + "-" + subRecipe.getName())
            subRecipeObj.setRecipeRevision(subRecipe.getRevision())
            subRecipeObj.setRecipeName(subRecipe.getName())
            subRecipeObj.setEquipmentId(ceqId)
            subRecipeObj.setEquipmentLogicalId(subRecipe.getEquipmentLogicalId())
            subRecipeObj.setMainEquipmentId(request.getResourceName())
            subRecipeObj.setIsSubRecipe("TRUE")
            subRecipeObj.setSequence(subRecipe.getSequence())
            subRecipeObj.setLastCapability(request.getLastProcessCapability())
            subRecipeObj.setRequireCapability(cLot.getProcessCapability())
            subRecipeObj.setMainRecipeName(request.getRecipeName())

            def childLogicalIdArray = mainEquipment.getPropertyContainer().getStringArray(request.getResourceName() + "_ChildEquipments", new String[0])
            boolean logicalIdFound = false
            for (var in childLogicalIdArray)
            {
                if (var.equalsIgnoreCase(subRecipe.getEquipmentLogicalId()))
                {
                    logicalIdFound = true
                    break
                }
            }
            if(!logicalIdFound)
            {
                def childLogicalList = new ArrayList<String>();
                for (var in childLogicalIdArray)
                {
                    childLogicalList.add(var)
                }
                childLogicalList.add(subRecipe.getEquipmentLogicalId())
                def newLogicalRecords = (String[])childLogicalList.toArray(new String[childLogicalList.size()])
                mainEquipment.getPropertyContainer().setStringArray(request.getResourceName() + "_ChildEquipments", newLogicalRecords)
            }
            if (ceqId.length() > 0)
            {
                def childSystemIdArray = mainEquipment.getPropertyContainer().getStringArray(request.getResourceName() + "_ChildEquipmentsId", new String[0])
                boolean systemIdFound = false
                for (var in childSystemIdArray)
                {
                    if (var.equalsIgnoreCase(ceqId))
                    {
                        systemIdFound = true
                        break
                    }
                }
                if(!systemIdFound)
                {
                    def childSystemList = new ArrayList<String>();
                    for (var in childSystemIdArray)
                    {
                        childSystemList.add(var)
                    }
                    childSystemList.add(ceqId)
                    def newLogicalRecords = (String[])childSystemList.toArray(new String[childSystemList.size()])
                    mainEquipment.getPropertyContainer().setStringArray(request.getResourceName() + "_ChildEquipmentsId", newLogicalRecords)
                }
            }

            if (lastProcessCapability.contains(";"))
            {
                String[] arrLastCapability = lastProcessCapability.split(";")
                subRecipeObj.setLastCapability(lastProcessCapability)
                subRecipeObj.setCapabilityCondition(arrLastCapability[1])
            }
            else
            {
                subRecipeObj.setLastCapability(lastProcessCapability)
                subRecipeObj.setCapabilityCondition("")
            }
            subRecipeObj.setRequireCapability(cLot.getProcessCapability())


            def pm = getChildEquipmentThruput(request, ceqId)
            if (pm !=null)
            {
                subRecipeObj.setThruput(pm.PM_THRUPUT_QTY)
                subRecipeObj.setThruput2(pm.PM_THRUPUT_QTY2)
            }

            def params = subRecipe.getRecipeParams()

            for (recipeParam in params)
            {
                def param = new RecipeParameter(recipeParam.getParamName())
                param.setMaxValue(recipeParam.getMaxValue())
                param.setMinValue(recipeParam.getMinValue())
                param.setDataType(recipeParam.getFieldType())
                param.setParameterValue(recipeParam.getParamValue())
                param.setValidValues(recipeParam.getValidValues())
                subRecipeObj.addElement(param)
            }




            subRecipeObj.setTrackInQty(totalqty)
            //subRecipeObj.setTrackInQty(cLot.getTrackInQty())
            recipeManager.addDomainObject(subRecipeObj)
        }
    }

    public PM getChildEquipmentThruput(W02TrackInLotRequest request,String childEqId)
    {
        def ceqList = request.getChildEquipmentList()
        for (ceq in ceqList)
        {
            if (ceq.CHILD_EQUIPMENT_ID.equalsIgnoreCase(childEqId))
            {
                def requirement = ceq.CEQ_THRUPUT_REQUIREMENT
                def pmList = ceq.PM_LIST
                for(pm in pmList)
                {
                    if (pm.PM_NAME.equalsIgnoreCase(requirement))
                    {
                        return pm
                    }
                }
            }
        }
        return null
    }

    public String getEquipmentId(W02TrackInLotRequest request,String logicalId)
    {
        def ceqList = request.getChildEquipmentList()
        for (ceq in ceqList)
        {
            if (ceq.CHILD_EQUIPMENT_LOGICAL_ID.equalsIgnoreCase(logicalId))
            {
                return ceq.CHILD_EQUIPMENT_ID
            }
        }
        return ""
    }

    public void addTrackInWaferInformation(CLot lot, W02TrackInLotRequest request)
    {
        def waferQty = request.getWaferQty()
        def trackInWaferList = request.getLotTrackInWaferList()
        def trackInWaferSize = trackInWaferList.size()
        if(trackInWaferList != null && trackInWaferSize > 0)
        {
            for (trackInWafer in trackInWaferList)
            {
                CWafer wafer = new CWaferImpl(trackInWafer.getWaferNumber())
                wafer.setWaferScribeID(trackInWafer.getWaferScribeNumber())
                wafer.setNPDW(Integer.parseInt(trackInWafer.getWaferNDPW()))
                wafer.setGoodQty(Integer.parseInt(trackInWafer.getWaferGoodQty()))
                wafer.setSequence(trackInWafer.getWaferSequence())
                wafer.setEquipmentId(request.getResourceName())
                lot.addWafer(wafer)
            }
        }
    }

    //detect dv change, check duration
}