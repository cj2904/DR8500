package EapVerifyPmStatus

import groovy.transform.CompileStatic

import java.text.ParseException
import java.text.SimpleDateFormat

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.domainobject.PmManager
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EapVerifyPmStatus_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="PmManager")
    private PmManager pmManager

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        W02TrackInLotRequest outboundRequest = new W02TrackInLotRequest(inputXmlDocument)
        //def trackInQty = outboundRequest.getTrackInQty()
        int qty2 = Integer.parseInt(outboundRequest.getQty2())
        def lotId = outboundRequest.getContainerName()
        def lot = cMaterialManager.getCLot(lotId)

        List<String> eqplist = new ArrayList<String>()
        def allobj = pmManager.getAllDomainObject()
        for(pm in allobj)
        {
            def pmname = pm.getPmName()
            def pmeqp = pm.getEquipmentId()
            logger.info("PM Name " + pm.getPmName())
            logger.info("PM Equipment " + pm.getEquipmentId())

            if(pm.getPmNextDateDue().length()>0)
            {
                def loc = Locale.US
                def formatdatestr = "MM/dd/yyyy hh:mm:ss a"

                def nextduedate = pm.getPmNextDateDue()
                if(pm.getFirstDate()!=null && pm.getMRFirstDate()!=null && pm.getFirstDate().length()>0 && pm.getMRFirstDate().length()>0 && gconvertDateString(pm.getFirstDate(),formatdatestr,loc)!=gconvertDateString(pm.getMRFirstDate(),formatdatestr,loc))
                {
                    nextduedate = pm.getMRFirstDate()
                }
                else
                {
                    nextduedate = pm.getPmNextDateDue()
                }

                def currtime = System.currentTimeMillis()
                def nextdue = (gconvertDateString(nextduedate,formatdatestr,loc))
                def tole= (long) (pm.getTolerancePeriod()*24*60*60*1000)
                if (tole <0)
                {
                    tole = 0
                }
                def difftz = 0//(12*60*60*1000)
                def totaldue = nextdue+tole+difftz

                logger.info(pmname + " currtimelong " + currtime)
                logger.info(pmname + " nextduestr " + nextduedate)
                logger.info(pmname + " nextduelong " + nextdue)
                logger.info(pmname + " toledaylong  " + tole )
                logger.info(pmname + " difftzlong " + difftz)
                logger.info(pmname + " totalnextduelong " + totaldue)
                logger.info(pmname + " CurrTime " + new Date(currtime).toString())
                logger.info(pmname + " Next Due " + new Date(nextdue).toString())
                logger.info(pmname + " Total Next Due " + new Date(totaldue).toString())

                if(currtime>=(totaldue))
                {
                    throw new Exception("TrackIn Failed. Equipment $pmeqp PM $pmname is PastDue for Due Date at ["+new Date(totaldue).toString()+"]!")
                }
            }

            if(pm.getPmQty2()>0)
            {
                int factor = 1
                def childEqp = ""
                def allRecipe = lot.getAllRecipeObj()
                for (recipe in allRecipe)
                {
                    childEqp = recipe.getEquipmentLogicalId()
                    def recipeThruputFactor = recipe.getThruputFactor()
                    logger.info("recipeThruputFactor childEqp " + childEqp)
                    logger.info("recipeThruputFactor " + recipeThruputFactor)
                    if (recipeThruputFactor>1 && childEqp.equalsIgnoreCase(pmeqp))
                    {
                        factor = (int) Math.round(recipeThruputFactor)
                        logger.info("recipeThruputFactor rounded " + factor)
                    }
                }

                def qtyadj2 = 0
                if(pm.getThruputQtyAdj2()>0)
                {
                    qtyadj2 = pm.getThruputQtyAdj2()
                }
                def current = pm.getThruputQty2() + qtyadj2 + (qty2*factor)
                def tolerence = pm.getPmQty2() + pm.getToleranceQty2()

                logger.info("PM current qty2 " + qty2)
                logger.info("PM current factor " + factor)
                logger.info("PM current qtyadj2 " +qtyadj2 )
                logger.info("PM current thruputQty2 " +pm.getThruputQty2() )
                logger.info("PM PmQty2 " + pm.getPmQty2())
                logger.info("PM PmToleranceQty2 " + pm.getToleranceQty2())
                logger.info("PM current " + current)
                logger.info("PM tolerence " + tolerence)
                if(current > tolerence)
                {
                    throw new Exception("TrackIn Failed. Equipment ["+pmeqp+"] PM ["+pmname+"] is Past Due for Thruput Limit ["+tolerence+"] with Current TrackIn Qty ["+current+"]!")
                }
            }

            /*
             if(eqplist.contains(pmeqp))
             {
             logger.info("PM Equipment " + pmeqp + " found in eqplist, skip getmaintenancestatus")
             continue
             }
             else
             {
             logger.info("PM Equipment " +pmeqp + " not found in eqplist, add getmaintenancestatus")
             eqplist.add(pmeqp)
             }
             logger.info("PM Equipment " +pmeqp + " perform getmaintenancestatus")
             def requestmaineqreq = new GetMaintenanceStatusesRequest()
             requestmaineqreq.getInputData().setResource(pm.getEquipmentId())
             requestmaineqreq.getInputData().setPastDue("true");
             def reply = cCamstarService.getMaintenanceStatuses(requestmaineqreq)
             if(reply.isSuccessful())
             {
             def items = reply.getAllMaintenanceRecord()
             while (items.hasNext())
             {
             throw new Exception("TrackIn Failed. Equipment $pmeqp PM $pmname is PastDue!")
             def item = items.next()
             def itemRow = item.getRow()
             def dueTimeStamp = itemRow.getNextDateDue()
             def pastDueTimeStamp = itemRow.getNextDateLimit()
             def completed = itemRow.getCompleted()
             def due = itemRow.getDue();
             def pastDue = itemRow.getPastDue()
             def pmState = itemRow.getMaintenanceState()
             def pmUsed = itemRow.getMaintenanceReqName()
             def maintenancestatus = itemRow.getMaintenanceStatus()
             if(pastDue.equalsIgnoreCase("true"))
             {
             throw new Exception("TrackIn Failed. Equipment $pmeqp PM $pmname is PastDue!")
             }
             }
             }
             */
        }
    }

    public long gconvertDateString(String dateVal, String dateFormatIn, Locale loc)
    {
        if (dateVal == null || dateVal.length() == 0)
        {
            return 0;
        }

        Date parse = null;
        SimpleDateFormat CAMSTAR_PM_DATE_FORMAT = new SimpleDateFormat(dateFormatIn, loc);
        try
        {
            parse = CAMSTAR_PM_DATE_FORMAT.parse(dateVal);
        }
        catch (ParseException pe)
        {
            pe.printStackTrace();
        }


        long newTime = 0
        try
        {
            newTime = (long) (parse.getTime());
            logger.info("convertDateString new Date " + CAMSTAR_PM_DATE_FORMAT.format(new Date(newTime)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return newTime;
    }
}