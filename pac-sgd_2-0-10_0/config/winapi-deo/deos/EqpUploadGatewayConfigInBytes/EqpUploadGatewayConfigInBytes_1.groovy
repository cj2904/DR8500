package EqpUploadGatewayConfigInBytes

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked
import sg.znt.pac.SgdConfig
import sg.znt.pac.machine.CEquipment

@Deo(description='''
Save upload config file in pac local drive
''')
class EqpUploadGatewayConfigInBytes_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="Parameter")
	private Map parameter
	
	@DeoBinding(id="CEquipment")
	private CEquipment equipment
    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
		if(parameter.size() > 0)
		{
			String configPath = SgdConfig.getGatewayConfigPath(equipment.getName())
			File file = new File(System.getProperty("user.dir", "") + configPath + "\\" + parameter.get("FileName", ""))
			
			logger.info("Saving config file to: " + file.getAbsolutePath() + " : " + configPath)
			String fileContent = parameter.get("Content", "")
			byte[] byteContent = DatatypeConverter.parseHexBinary(fileContent);
			FileOutputStream fos = new FileOutputStream(file)
			fos.write(byteContent)
			fos.close()
		}
    }
}