package EapStoreSpcDataDomainObject

import groovy.transform.CompileStatic

import java.text.SimpleDateFormat

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.TscConstants
import sg.znt.pac.domainobject.SequenceParameter
import sg.znt.pac.domainobject.SequenceParameterManager
import sg.znt.pac.domainobject.SequenceParameterSet
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import OutboundRequest.CommonOutboundRequest
import de.znt.pac.deo.annotations.*
import de.znt.pac.domainobject.filter.FilterAllDomainObjects

@CompileStatic
@Deo(description='''
Store SPC header and data based on the number
''')
class EapStoreSpcDataDomainObject_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="SequenceParameterManager")
    private SequenceParameterManager sequenceParameterManager

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="InputXml")
    private String inputXml

    private CLot clot

    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new CommonOutboundRequest(inputXml)

        String eqId = request.getResourceName()
        String lotId = request.getContainerName()
        clot = cMaterialManager.getCLot(lotId)

        if(clot.getPropertyContainer().getBoolean(TscConstants.MATERIAL_ATTR_FIRST_LOT_IN_BATCH,false))
        {
            createSpcHeader(request)
            createSpcData(request)
        }
        else
        {
            logger.info("Lot '" + lotId + "' is not the first batch, added to the sub lot list")
                        
            def lotList = cMaterialManager.getCLotList(new LotFilterAll())
            CLot spcLot = null
            for (lot in lotList)
            {
                if(lot.getPropertyContainer().getBoolean(TscConstants.MATERIAL_ATTR_FIRST_LOT_IN_BATCH,false))
                {
                    spcLot = lot
                    break                                        
                }                
            }
            
            if(spcLot == null)
            {
                logger.error("No SPC lot is marked!")
                return
            }
            String seqSetIdData = request.getResourceName() + "-" +spcLot.getId() + "-" + TscConstants.SPC_DATA_NAME
            
            SequenceParameterSet seqSetHeader = sequenceParameterManager.getDomainObject(seqSetIdData)
            if(seqSetHeader==null)
            {
                logger.error("No SPC data is found!")
                return
            }
            def seqParamList = seqSetHeader.getAll(new FilterAllDomainObjects())
            
            SequenceParameter nextLotSeq = null
            def found = false
            for (seq in seqParamList)
            {
                if (seq.getParamName().indexOf("Lot") > -1)
                {
                    if(seq.getParamValue().equalsIgnoreCase(""))
                    {
                        if (nextLotSeq == null)
                        {
                            nextLotSeq = seq
                            logger.info("Identify next available lot sequence parameter '" + nextLotSeq.getParamName() + "|" + nextLotSeq.getSequenceName())
                        }
                    }
                    else if(seq.getParamValue().equalsIgnoreCase(lotId))
                    {
                        found = true
                        break
                    }
                }
            }
            
            if (!found) 
            {
                if(nextLotSeq == null) 
                {
                    logger.error("Unable to assign lot '" + lotId + "', no more available sequence lot slot")
                }
                else 
                {
                    logger.info("Assigning lot '" + lotId + "' to sequence '" + nextLotSeq.getParamName() + "|" + nextLotSeq.getSequenceName())
                    nextLotSeq.setParamValue(lotId)
                }
            }
        
        }
        
    }
    
    private String mapSpcValue(CLot lot, String headerId, String equipmentId)
    {
        def mappingValue = ""
        if (headerId.equalsIgnoreCase("Date"))
        {
            mappingValue = new SimpleDateFormat("yyyy/M/dd").format(System.currentTimeMillis())            
        }
        else if (headerId.equalsIgnoreCase("Time"))
        {
            mappingValue = new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis())
        }
        else if (headerId.equalsIgnoreCase("Lot01"))
        {
            mappingValue = lot.getId()
        }
        else
        {
            //mappingValue = headerId
            logger.info("Unable to resolving '" + headerId + "'..")
        }
        logger.info("Resolving header '" + headerId + "' to value '" + mappingValue + "'")
        return mappingValue
    }
    
    
    private void createSpcHeader(CommonOutboundRequest request)
    {
        String seqSetIdHeader = request.getResourceName() + "-" +request.getContainerName() + "-" + TscConstants.SPC_HEADER_NAME
        
        if(sequenceParameterManager.getDomainObject(seqSetIdHeader)==null)
        {
            SequenceParameterSet seqSetHeader = sequenceParameterManager.createDomainObject(seqSetIdHeader)
            def counter = 1
            String headerId = ""
            def headerResolveValue = ""
            String headerName = ""
            while (true)
            {
                if (counter < 10)
                {
                    headerId = TscConstants.SPC_HEADER_NAME + "_0" + counter
                }
                else
                {
                    headerId = TscConstants.SPC_HEADER_NAME + "_" + counter
                }
                logger.info("Searching header id = " + headerId)
                def headerValue = request.getItemValue(headerId)
                if (headerValue == null)
                {
                    logger.info("Last header counter = " + counter)
                    break
                }
                if (headerValue.toString().indexOf("{") > -1)
                {
                    headerName = headerValue.toString().replaceAll("{","").replaceAll("}", "")
                    headerResolveValue = mapSpcValue(clot, headerName, request.getResourceName())
                }
                else
                {
                    headerName = ""
                    headerResolveValue = headerValue.toString()
                }
                
                seqSetHeader.addSequenceParameter(new SequenceParameter(counter+"",counter+"",headerId,headerName,headerResolveValue.toString()))
                counter = counter +1
            }
            sequenceParameterManager.addDomainObject(seqSetHeader)
        }
    }
    
    private void createSpcData(CommonOutboundRequest request)
    {
        String seqSetIdData = request.getResourceName() + "-" +request.getContainerName() + "-" + TscConstants.SPC_DATA_NAME
        
        SequenceParameterSet seqSetHeader = sequenceParameterManager.getDomainObject(seqSetIdData)
        if(seqSetHeader==null)
        {
            seqSetHeader = sequenceParameterManager.createDomainObject(seqSetIdData)
            def counter = 1
            String headerId = ""
            def headerResolveValue = ""
            String headerName = ""
            while (true)
            {
                if (counter < 10)
                {
                    headerId = TscConstants.SPC_DATA_NAME + "_0" + counter
                }
                else
                {
                    headerId = TscConstants.SPC_DATA_NAME + "_" + counter
                }
                logger.info("Searching spc data header id = " + headerId)
                def headerValue = request.getItemValue(headerId)
                if (headerValue == null)
                {
                    logger.info("Last spc data  counter = " + counter)
                    break
                }
                if (headerValue.toString().indexOf("{") > -1)
                {
                    headerName = headerValue.toString().replaceAll("\\{","").replaceAll("}", "")
                    headerResolveValue = mapSpcValue(clot, headerName, request.getResourceName())
                }
                else
                {
                    headerName = ""
                    headerResolveValue = headerValue.toString()
                }
                
                seqSetHeader.addSequenceParameter(new SequenceParameter(counter+"",counter+"",headerId,headerName,headerResolveValue.toString()))
                counter = counter +1
            }
            sequenceParameterManager.addDomainObject(seqSetHeader)
        }
        else
        {}
    }
}