package EapSleep

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
DEO for waiting second
''')
class EapSleep_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="DurationInSecond")
	private Integer durationInSecond

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		try 
		{
			Thread.sleep(durationInSecond*1000)
		} 
		catch (Exception e) 
		{
			e.printStackTrace()
		}
    
    }
}