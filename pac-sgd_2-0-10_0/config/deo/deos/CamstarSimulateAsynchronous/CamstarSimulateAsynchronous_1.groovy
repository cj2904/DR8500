package CamstarSimulateAsynchronous

import org.apache.activemq.command.ActiveMQTextMessage
import org.apache.commons.io.FileUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.subject.SgdCamstarTopicSubject

@CompileStatic
@Deo(description='''
Simluate Camstar Asynchronous from file
by given subject name
''')
class CamstarSimulateAsynchronous_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="Filepath")
    private String filePath

    @DeoBinding(id="SubjectName")
    private String subjectName
	
	
	@DeoBinding(id="CamstarService")
	private CCamstarService cCamstarService


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def allSubject = cCamstarService.getSubjectList()
		for (subject in allSubject)
		{
			if (subject instanceof SgdCamstarTopicSubject)
			{
				if (subject.getName().equals(subjectName))
				{
					SgdCamstarTopicSubject syncSubject = (SgdCamstarTopicSubject) subject
					ActiveMQTextMessage msg = new ActiveMQTextMessage()
					msg.setText(FileUtils.readFileToString(new File(filePath), "UTF-8"))
					syncSubject.onMessage(msg)
					break
				}
			}
		}
    }
}