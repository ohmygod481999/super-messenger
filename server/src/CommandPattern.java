import java.util.regex.Pattern;

public final class CommandPattern {

    public static final Pattern LOGIN   = Pattern.compile("^Login \\[(.*)]$");
    public static final Pattern LOGOUT  = Pattern.compile("^Logout$");
    public static final Pattern UTEXT   = Pattern.compile("^UText \\[(.*)] (.*)$");
    public static final Pattern UFILE   = Pattern.compile("^UFile \\[(.*)] \\[(.*)] (\\d*)$");
    public static final Pattern UGET    = Pattern.compile("^UGet \\[(.*)] \\[(.*)]$");
    public static final Pattern CREATE  = Pattern.compile("^Create \\[(.*)]$");
    public static final Pattern JOIN    = Pattern.compile("^Join \\[(.*)]$");
    public static final Pattern LEAVE   = Pattern.compile("^Leave \\[(.*)]$");
    public static final Pattern GTEXT   = Pattern.compile("^GText \\[(.*)] (.*)$");
    public static final Pattern GFILE   = Pattern.compile("^GFile \\[(.*)] \\[(.*)] (\\d*)$");
    public static final Pattern GGET    = Pattern.compile("^GGet \\[(.*)] \\[(.*)]$");
    public static final Pattern USERS   = Pattern.compile("^Users( \\[(.*)])?$");
    public static final Pattern GROUPS  = Pattern.compile("^Groups$");

}
