package EczReqDownloadRecipePermission

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S7F1ProcessProgramLoadInquire
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsDataItem
import de.znt.zsecs.composite.SecsDataItem.ItemName
import de.znt.zsecs.sml.SecsItemType

@Deo(description='''
Request permission before download
''')
class EczReqDownloadRecipePermission_1 {


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="SecsGemService")
	private SecsGemService secsGemService

	@DeoBinding(id="Path")
	private String path

	@DeoBinding(id="PPID")
	private String ppid

	/**
	 *
	 */
	@DeoExecute
	public void execute() {
		def loadRequest = new S7F1ProcessProgramLoadInquire()
		loadRequest.getData().setPPID(new SecsAsciiItem(ppid))
		def dataType = SecsDataItem.getDataType("S7F1", SecsDataItem.getDataType(ItemName.VID, SecsItemType.U4))
		loadRequest.getData().setLENGTH(SecsDataItem.createDataItem(dataType, getRecipe().length + ""))

		def loadReply = secsGemService.sendS7F1ProcessProgramLoadInquire(loadRequest)

		if (loadReply.getPPGNT() != 0) {
			throw new Exception("S7F1: Grant permission failed.")
		}
		else
		{
			logger.info("Permission to download recipe $ppid to equipment is granted!")
		}
	}

	public byte[] getRecipe() throws Exception 
	{
		String fileName = path + "\\" + ppid;
		File file = new File(fileName);
		if (file.exists()) {
			logger.info("Request recipe '" + ppid + "' from '" + file.getAbsolutePath() + "'");
			byte[] b = new byte[(int) file.length()];
			FileInputStream is = new FileInputStream(file);
			is.read(b);
			is.close();

			return b;
		}
		else {
			logger.error("Recipe file '" + fileName + " not found!");
			throw new Exception("Recipe '$fileName' not found!");
		}
	}
}