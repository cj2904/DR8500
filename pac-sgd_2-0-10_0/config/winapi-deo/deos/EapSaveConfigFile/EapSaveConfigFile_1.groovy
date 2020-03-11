package EapSaveConfigFile

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import groovy.transform.TypeChecked;
import sg.znt.pac.SgdConfig

import java.io.File;
import java.io.FileOutputStream;
import java.lang.String
import java.nio.charset.Charset

@Deo(description='''
Save gateway configuration file in pac folder
''')
class EapSaveConfigFile_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="FileName")
    private String fileName

    @DeoBinding(id="EquipmentId")
    private String equipmentId
	
	@DeoBinding(id="FileContent")
	private StringBuffer fileContent
	
	@DeoBinding(id="Encoding")
	private String encoding

    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
		String gatewayConfigPath = System.getProperty("user.dir") + SgdConfig.getGatewayConfigPath(equipmentId)
		File file = new File(gatewayConfigPath + "\\" + fileName)
		
		FileOutputStream fos = new FileOutputStream(file)
		fos.write(fileContent.toString().getBytes(Charset.forName(encoding)))
		
		fos.close()
    }
}