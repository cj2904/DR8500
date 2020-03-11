package EczSendHostCommand

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.SecsService
import de.znt.zsecs.SecsMessage
import de.znt.zsecs.sml.SmlAsciiParser

@Deo(description='''
Send host command in SML format
''')
class EczSendHostCommand_1
{


    @DeoBinding(id="Logger")
    private Log uiLogger = LogFactory.getLog(getClass())


    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="DynamicInput")
    private String dynamicInput

    private static final String KEY_HOST_CMD_PPSELECT = "hostcommand"

    /**
     dynamicInput = "hostcommand=PPSELECT;PPID=ABC;PROMPT=YES;module=1"
     */

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        SmlAsciiParser parser = new SmlAsciiParser()
        def primarySml = generateSmlByDynamicInput()

        SecsMessage primaryMessage = parser.parse(primarySml)

        def secsService = (SecsService) secsGemService
        def replyMessage = secsService.requestMessage(primaryMessage);
        uiLogger.info replyMessage.getSML()
    }

    String generateSmlByDynamicInput()
    {
        Properties prop = parseProperty(dynamicInput)

        String primarySml =
                '''
         S2F41 W
          <L [2]
           <A "''' + prop.getProperty(KEY_HOST_CMD_PPSELECT) + '''" >''' + generateParamSml(prop) + '''
          >.
        '''
        return primarySml
    }

    String generateParamSml(Properties prop)
    {
        String paramValue = "";
        int paramCount = 0;
        def e = prop.propertyNames()
        while (e.hasMoreElements())
        {
            String key = (String) e.nextElement()
            def value = prop.getProperty(key)
            if (!key.equalsIgnoreCase(KEY_HOST_CMD_PPSELECT))
            {
                def itemValue = '''<L [2]
                     <A "''' + key + '''" >
                     <A "''' + value + '''">
                >'''
                paramValue = paramValue + itemValue
                paramCount = paramCount + 1
            }
        }
        def rootParamValue = "<L [" + paramCount + "]" + paramValue + ">"
        return rootParamValue
    }

    Properties parseProperty(String prop)
    {
        Properties property = new Properties();

        StringTokenizer tokenizer = new StringTokenizer(prop.trim(), ";");

        while (tokenizer.hasMoreElements())
        {
            String value = (String) tokenizer.nextElement();
            int index = value.indexOf("=");

            if (index < 0)
            {
                throw new IllegalArgumentException("CustomProperty syntax error: '=' not found!");
            }
            property.setProperty(value.substring(0, index).trim(), value.substring(index + 1, value.length()).trim());
        }
        return property;
    }
}