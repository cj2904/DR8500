package EczUploadRecipe

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S7F5ProcessProgramRequest
import de.znt.services.secs.dto.S7F6ProcessProgramData
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsBinary

@Deo(description='''
Upload a specific recipe from equipment
''')
class EczUploadRecipe_1 {


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
    public void execute()
    {
		saveRecipe(ppid, requestRecipeBodyFromEqp(ppid))
        logger.info("Recipe $ppid uploaded to local path $path\\$ppid sucessfully")
    }
	
	public void saveRecipe(String recipeName, byte[] recipeContent)
	throws IOException
	{
		File folder = new File(path);
		if (!folder.exists())
		{
			folder.mkdirs();
		}
		String fileName = path + "\\" + recipeName;
		File file = new File(fileName);
		if (file.exists())
		{
			throw new IOException("Recipe '$recipeName' already exists in '$path'")
		}
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(recipeContent);
		fos.close();
	}

	public byte[] requestRecipeBodyFromEqp(String recipeId)
	{
		byte[] recipeBody;


		S7F5ProcessProgramRequest request = new S7F5ProcessProgramRequest()
		SecsAsciiItem ppid = new SecsAsciiItem(recipeId)
		request.setPPID(ppid)

		S7F6ProcessProgramData reply = secsGemService.sendS7F5ProcessProgramRequest(request)

		if(reply.getData().getPPBODY() instanceof SecsAsciiItem)
		{
			SecsAsciiItem recipeBodyInAscii = (SecsAsciiItem)reply.getData().getPPBODY()
			recipeBody = recipeBodyInAscii.getString().getBytes()
		}
		else
		{
			SecsBinary recipeBodyInBinary = (SecsBinary)reply.getData().getPPBODY()
			recipeBody = recipeBodyInBinary.getBytes()
		}
		return recipeBody;
	}
}