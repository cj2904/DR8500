package EapGenerateCsvTrackInWaferList_FileBase

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.TscConfig
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import sg.znt.services.equipment.file.EquipmentFileHandler

@CompileStatic
@Deo(description='''
Generate cvs based on track in wafer list
''')
class EapGenerateCsvTrackInWaferList_FileBase_1
{
    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        W02TrackInLotRequest outbound = new W02TrackInLotRequest(inputXmlDocument)
        def product = outbound.getProduct()
        def lotId = outbound.getContainerName()
        def probingYieldLimit = outbound.getYieldFailureLimit()
        def waferList = outbound.getLotTrackInWaferList()

        def wipDataList = outbound.getWipDataItemList()
        def isPAT = -1
        def realRunQty = -1

        if (wipDataList.size()>0)
        {
            for (var in wipDataList)
            {
                if (var.WIP_DATA_NAME.equalsIgnoreCase("PAT"))
                {
                    isPAT = PacUtils.valueOfInteger(var.WIP_DATA_VALUE, -1)
                }
            }
            for (var in wipDataList)
            {
                if (var.WIP_DATA_NAME.equalsIgnoreCase("Real Run Qty"))
                {
                    realRunQty = PacUtils.valueOfInteger(var.WIP_DATA_VALUE, -1)
                    break
                }
            }
        }

        if (isPAT == -1)
        {
            throw new Exception("WIP Data 'PAT' is not found!")
        }

        def recipeParamList = outbound.getRecipeParamList()
        def testerRecipe = ""
        def proberRecipe = ""
        for (recipeParam in recipeParamList)//check R_Sub only
        {
            if (recipeParam.getParamName().equalsIgnoreCase("R_Sub"))
            {
                proberRecipe = recipeParam.getParamValue()
                break
            }
        }

        if(proberRecipe == "")
        {
            throw new Exception("RecipeParam_ParamName : 'R_Sub' could not be found!")
        }

        def inputPath = EquipmentFileHandler.getInputPath()

        PrintWriter fileWriter
        try
        {
            def patDataCount = PacUtils.valueOfInteger(TscConfig.getStringProperty("FileBase.CSV.PatDataCount", "4"), 0)
            File file = new File(inputPath + "\\CASSETTE_DATA.csv")
            logger.info("File path for CASSETTE_DATA: $inputPath ...")
            fileWriter = new PrintWriter(file)

            //---Fixed template format---
            // De Ce  = not uniDirectional
            // Chang Luo = uniDirectional
//            if(isUniDirectional == true)
//            {
//                fileWriter.println("ProbingYieldLimit=$probingYieldLimit,,,,")
//            }

            fileWriter.println("-- Pat Data --,,,,")
            fileWriter.println("Index,Lot ID,Wafer ID,Tester Recipe,Prober Recipe")
            if (product.endsWith("-H"))
            {
                if (isPAT == 0)
                {
                    def waferNo = ""
                    def waferId = ""
                    def dataValue = ""
                    def index = 1
                    for (wafer in waferList)
                    {
                        waferNo = wafer.getWaferNumber()
                        waferId = lotId + "-" + waferNo
                        dataValue = index + "," + lotId + "," + waferId + "," + testerRecipe + "," + proberRecipe
                        fileWriter.println(dataValue)
                        index++
                        if (index > patDataCount)
                        {
                            break
                        }
                    }
                }
                else if (isPAT == 1)
                {
                    def patLotFilePath = ""
                    def patLotFile = new File(patLotFilePath)
                    if (patLotFile.exists())
                    {
                        //OK
                        for(int i = 1; i <= patDataCount; i++)
                        {
                            fileWriter.println("$i,,,,")
                        }
                    }
                    else
                    {
                        throw new Exception("WIP Data 'PAT' value = '$isPAT' but 'PAT_LotID' file is not exist!")
                    }
                }
                else
                {
                    throw new Exception("WIP Data 'PAT' is '$isPAT' which is not defined!")
                }
            }
            else
            {
                for(int i = 1; i <= patDataCount; i++)
                {
                    fileWriter.println("$i,,,,")
                }
            }
            fileWriter.println("-- Lot Data --,,,,")
            fileWriter.println("Index,Lot ID,Wafer ID,Tester Recipe,Prober Recipe")
            //---End of Fixed template format---

            def waferNo = ""
            def waferId = ""
            def dataValue = ""
            def index = 1
            for (wafer in waferList)
            {
                waferNo = wafer.getWaferNumber()
                waferId = lotId + "-" + waferNo
                dataValue = index + "," + lotId + "," + waferId + "," + testerRecipe + "," + proberRecipe
                fileWriter.println(dataValue)
                index++
                if (index > realRunQty)
                {
                    logger.info("Wafer count hits real run qty : '$realRunQty'...")
                    break
                }
            }
            logger.info("CSV file is generated successfully...")
        }
        catch (IOException e)
        {
            e.printStackTrace()
            throw new Exception(e)
        }
        finally
        {
            logger.info("Flushing file writer...")
            fileWriter.flush()
            fileWriter.close()
            logger.info("Flushing file writer completed...")
        }
    }
}