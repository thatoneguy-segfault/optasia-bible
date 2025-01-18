import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Allow pass-by-reference of a matcher.
 * Useful for if/else statements.
 */
public class REHelper
{
	public Matcher m;
	public REHelper()
	{
	}

	public boolean matches()
	{
		return m.matches();
	}

	public String group(int i)
	{
		return m.group(i);
	}

	public static boolean strMatches(String s, REHelper reHelper, Pattern pattern)
	{
		reHelper.m = pattern.matcher(s);
		return reHelper.m.matches();
	}
	
	public static boolean strMatches(String s, REHelper reHelper, String pattern)
	{
		reHelper.m = Pattern.compile(pattern).matcher(s);
		return reHelper.m.matches();
	}

	public boolean strMatches(String s, Pattern pattern)
	{
		m = pattern.matcher(s);
		boolean r = m.matches();
		return r;
	}

	public boolean strMatches(String s, String pattern)
	{
		m = Pattern.compile(pattern).matcher(s);
		boolean r = m.matches();
		return r;
	}
}


