package imp

import de.znt.zsecs.SecsMessage
import de.znt.zsecs.composite.SecsComposite
import de.znt.zsecs.composite.SecsDataItem
import de.znt.zsecs.sml.SecsItemType

public class ImpUtil
{
    public static SecsMessage sendMessage(Integer stream, Integer function, String recipe, String lot, String qty, String port)
    {
        if(stream.equals(4))
        {
            if(function.equals(81))
            {
                //send s4f81 select recipe
                return sendS4F81(recipe, lot, qty, port)
            }
            else if(function.equals(93))
            {
                //send s4f93 cancel / abort message to eqp
                return sendS4F93(recipe, lot, qty, port)
            }
            else if(function.equals(87))
            {
                // send s4f87 load cassette
                return sendS4F87(recipe, lot, qty, port)
            }
            else if(function.equals(95))
            {
                // send s4f95 unload cassette
                return sendS4F95(recipe, lot, qty, port)
            }
            else if(function.equals(85))
            {
                // send close door
                return sendS4F85(recipe, lot, qty, port)
            }
        }
    }

    public static SecsMessage sendS4F81(String recipe, String lot, String qty, String port)
    {
        def list = new SecsComposite()
        list.add(SecsDataItem.createDataItem(SecsItemType.A, recipe))
        list.add(SecsDataItem.createDataItem(SecsItemType.A, lot))
        list.add(SecsDataItem.createDataItem(SecsItemType.U1, qty))
        list.add(SecsDataItem.createDataItem(SecsItemType.U1, port))
        def secsMessage = new SecsMessage((byte) 4, (byte) 81, false, list)
        println("Generated SecsMessage for s4F81: '$secsMessage'")
        return secsMessage
    }

    public static SecsMessage sendS4F93(String recipe, String lot, String qty, String port)
    {
        def list = new SecsComposite()
        list.add(SecsDataItem.createDataItem(SecsItemType.A, recipe))
        list.add(SecsDataItem.createDataItem(SecsItemType.A, lot))
        list.add(SecsDataItem.createDataItem(SecsItemType.U1, qty))
        list.add(SecsDataItem.createDataItem(SecsItemType.U1, port))
        def secsMessage = new SecsMessage((byte) 4, (byte) 93, false, list)
        println("Generated SecsMessage for s4F83: '$secsMessage'")
        return secsMessage
    }

    public static SecsMessage sendS4F87(String recipe, String lot, String qty, String port)
    {
        def list = new SecsComposite()
        list.add(SecsDataItem.createDataItem(SecsItemType.A, recipe))
        list.add(SecsDataItem.createDataItem(SecsItemType.A, lot))
        list.add(SecsDataItem.createDataItem(SecsItemType.U1, qty))
        list.add(SecsDataItem.createDataItem(SecsItemType.U1, port))
        def secsMessage = new SecsMessage((byte) 4, (byte) 87, false, list)
        println("Generated SecsMessage for s4F87: '$secsMessage'")
        return secsMessage
    }

    public static SecsMessage sendS4F95(String recipe, String lot, String qty, String port)
    {
        def list = new SecsComposite()
        list.add(SecsDataItem.createDataItem(SecsItemType.A, recipe))
        list.add(SecsDataItem.createDataItem(SecsItemType.A, lot))
        list.add(SecsDataItem.createDataItem(SecsItemType.U1, qty))
        list.add(SecsDataItem.createDataItem(SecsItemType.U1, port))
        def secsMessage = new SecsMessage((byte) 4, (byte) 95, false, list)
        println("Generated SecsMessage for s4F95: '$secsMessage'")
        return secsMessage
    }

    public static SecsMessage sendS4F85(String recipe, String lot, String qty, String port)
    {
        def list = new SecsComposite()
        list.add(SecsDataItem.createDataItem(SecsItemType.A, recipe))
        list.add(SecsDataItem.createDataItem(SecsItemType.A, lot))
        list.add(SecsDataItem.createDataItem(SecsItemType.U1, qty))
        list.add(SecsDataItem.createDataItem(SecsItemType.U1, port))
        def secsMessage = new SecsMessage((byte) 4, (byte) 85, false, list)
        println("Generated SecsMessage for s4F85: '$secsMessage'")
        return secsMessage
    }

    public static String getErrorMessage(String errorCode)
    {
        if(errorCode.equalsIgnoreCase("0"))
        {
            return "No Error"
        }
        if(errorCode.equalsIgnoreCase("1"))
        {
            return "secs2_s4_incorrect_process"
        }
        else if(errorCode.equalsIgnoreCase("2"))
        {
            return "secs2_s4_cassette_on_port"
        }
        else if(errorCode.equalsIgnoreCase("3"))
        {
            return "secs2_s4_failure_table"
        }
        else if(errorCode.equalsIgnoreCase("4"))
        {
            return "ecs2_s4_failure_door"
        }
        else if(errorCode.equalsIgnoreCase("5"))
        {
            return "secs2_s4_wh_man_mode"
        }
        else if(errorCode.equalsIgnoreCase("6"))
        {
            return "secs2_s4_bad_msg"
        }
        else if(errorCode.equalsIgnoreCase("7"))
        {
            return "secs2_s4_bad_mid"
        }
        else if(errorCode.equalsIgnoreCase("8"))
        {
            return "secs2_s4_bad_ppid"
        }
        else if(errorCode.equalsIgnoreCase("9"))
        {
            return "secs2_s4_bad_qty"
        }
        else if(errorCode.equalsIgnoreCase("10"))
        {
            return "secs2_s4_bad_ptn"
        }
        else if(errorCode.equalsIgnoreCase("11"))
        {
            return "secs2_s4_cmd_ignore"
        }
        else if(errorCode.equalsIgnoreCase("12"))
        {
            return "secs2_s4_cassette_not_on_port"
        }
        else if(errorCode.equalsIgnoreCase("13"))
        {
            return "secs2_s4_cassette_stuck"
        }
        else if(errorCode.equalsIgnoreCase("14"))
        {
            return "secs2_s4_load_in_progress"
        }
        else if(errorCode.equalsIgnoreCase("15"))
        {
            return "secs2_s4_unload_in_progress"
        }
        else if(errorCode.equalsIgnoreCase("16"))
        {
            return "secs2_s4_ptn_not_match"
        }
        else if(errorCode.equalsIgnoreCase("17"))
        {
            return "secs2_s4_mid_not_match"
        }
        else if(errorCode.equalsIgnoreCase("18"))
        {
            return "secs2_s4_ppid_not_match"
        }
        else if(errorCode.equalsIgnoreCase("19"))
        {
            return "secs2_s4_qty_not_match"
        }
        else if(errorCode.equalsIgnoreCase("33"))
        {
            return "secs2_s4_wh_lost_comm"
        }
        else if(errorCode.equalsIgnoreCase("34"))
        {
            return "secs2_s4_wh_rob_hold"
        }
        else if(errorCode.equalsIgnoreCase("35"))
        {
            return "secs2_s4_wh_invac_hold"
        }
    }
}