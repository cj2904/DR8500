package Test

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import java.lang.String

@CompileStatic
@Deo(description='''
test
''')
class Test_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="ImpXml")
    private String impXml

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    
    }
}