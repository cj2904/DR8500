package EczDownloadRecipe

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S7F3ProcessProgramSend
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsBinary

@Deo(description='''
Download a recipe to equipment
''')
class EczDownloadRecipe_1 {
	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="Path")
	private String path

	@DeoBinding(id="PPID")
	private String ppid

	@DeoBinding(id="SecsGemService")
	private SecsGemService secsGemService

	@DeoBinding(id="IsBinary")
	private boolean isBinary
	
	/**
	 *
	 */
	@DeoExecute
	public void execute() {

		def request = new S7F3ProcessProgramSend()
		request.getData().setPPID(new SecsAsciiItem(ppid))
		if (isBinary)
		{
			request.getData().setPPBODY(new SecsBinary(getRecipe()))
		}
		else
		{
			request.getData().setPPBODY(new SecsAsciiItem(new String(getRecipe())))
		}

		def reply = secsGemService.sendS7F3ProcessProgramSend(request)

		if(reply.isAccepted()) {
			logger.info("Download Recipe Successful")
		}
		else {
			throw new Exception("Download Recipe Fail")
		}
	}

	public byte[] getRecipe()
	throws Exception {
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