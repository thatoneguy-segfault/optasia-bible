import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.LinkedHashMap;

class CliBuilder {
	private String usage;
	private ArrayList<String> otherArgs = new ArrayList<String>();

	private LinkedList<CliArg> usageList = new LinkedList<CliArg>();
	private LinkedHashMap<String, CliArg> argMap = new LinkedHashMap<String, CliArg>();

	private boolean debug = false;

	class CliArg {
		public String shortname;
		public String longname;
		public String description;
		public String value = null;
		public boolean hasValue = false;
		public String defaultArg = null;
		public boolean isSet = false;

		public CliArg() {}

		public CliArg(String shortname, String longname, String description, String value, String defaultArg) {
			this.shortname = shortname;
			this.longname = longname;
			this.description = description;
			this.value = defaultArg;
			this.defaultArg = defaultArg;
			hasValue = (this.value != null);
		}
	}

	public CliBuilder(String usage) {
		this.usage = usage;
	}


	// e.g. grep 'hello' other1 other2 other3
	private boolean allowOtherArgs = true;
	public void allowOtherArgs(boolean tf)
	{
		allowOtherArgs = tf;
	}

	public void parseArgs(String[] args) {
		if (debug) {
			System.out.print("Parsing: ");
			for (String a : args) {
				System.out.print(" " + a);
			}
			System.out.println("");
		}

		String arg, value;
		for (int i = 0; i < args.length; i++) {
			arg = null;
			value = null;
			REHelper re = new REHelper();

			if (debug) System.out.println("Parsing " + args[i]);

			if (re.strMatches(args[i], "^-([A-Za-z])$")) {
				if (debug) System.out.println("Parsing -arg [value]");
				arg = re.group(1);
				if ((argMap.containsKey(arg)) && (argMap.get(arg).hasValue) && (i < args.length-1)) {
					if (!args[i+1].startsWith("-")) {
						value = args[i+1];
						i++;
					}
				}
			} else if (re.strMatches(args[i], "^--([A-Za-z]+)=(.+)$")) {
				if (debug) System.out.println("Parsing --longarg=value");
				arg = re.group(1);
				value = re.group(2);
			} else if (re.strMatches(args[i], "^--([A-Za-z]+)$")) {
				if (debug) System.out.println("Parsing --longarg [value]");
				arg = re.group(1);
				if ((argMap.containsKey(arg)) && (argMap.get(arg).hasValue) && (i < args.length-1)) {
					if (!args[i+1].startsWith("-")) {
						value = args[i+1];
						i++;
					}
				}
			} else if (allowOtherArgs) {
				if (debug) System.out.println("Parsing other arg ");
				if (args[i].startsWith("-")) {
					System.out.println("Error: Unrecognized argument " + args[i]);
					printUsage();
					System.exit(1);
				}
				otherArgs.add(args[i]);
			} else {
				System.out.println("Error: Unrecognized argument " + re.group(0));
				printUsage();
				System.exit(1);
			}

			if (arg != null) {
				if (debug) System.out.println("Processing arg " + arg);
				if (!argMap.containsKey(arg)) {
					System.out.println("Error: Unrecognized argument " + re.group(0));
					printUsage();
					System.exit(1);
				}
				CliArg cli = argMap.get(arg);
				cli.isSet = true;

				if (cli.hasValue && value == null) {
					System.out.println("Error: Expected value after argument " + re.group(0));
					printUsage();
					System.exit(1);
				}
				if (!cli.hasValue && value != null) {
					System.out.println("Error: No value after argument " + re.group(0));
					printUsage();
					System.exit(1);
				}

				if (value != null) {
					cli.value = value;
				}
			}
		}
	}

	public void add(String shortname, String longname, String description) {
		CliArg a = new CliArg(shortname, longname, description, null, null);
		usageList.add(a);
		if (shortname != null)
			argMap.put(shortname, a);
		if (longname != null)
			argMap.put(longname, a);
	}

	// e.g. foo --parse <true|false>
	public void addStringArgAny(String shortname, String longname, String description, String defaultArg) {
		CliArg a = new CliArg(shortname, longname, description, null, defaultArg);
		usageList.add(a);
		if (shortname != null)
			argMap.put(shortname, a);
		if (longname != null)
			argMap.put(longname, a);
	}

	public boolean isSet(String arg) {
		if (!argMap.containsKey(arg))
			throw new AssertionError("Arg '" + arg + "' was not defined");
		return argMap.get(arg).isSet;
	}

	public String value(String arg) {
		if (!argMap.containsKey(arg))
			throw new AssertionError("Arg '" + arg + "' was not defined");
		return argMap.get(arg).value;
	}

	public void printUsage() {
		System.out.println(usage);
		for (CliArg cli : usageList) {

			String usageArg = "";
			if (cli.shortname != null)
				usageArg += "-" + cli.shortname;
			if (cli.shortname != null && cli.longname != null)
				usageArg += "|";
			if (cli.longname != null)
				usageArg += "--" + cli.longname;

			String usageDefault = "";
			if (cli.defaultArg != null)
				usageDefault = "  default='"+cli.defaultArg+"'";
			System.out.println("\t" + usageArg + "\t: " + cli.description + usageDefault);
		}
	}

	public ArrayList<String> getOtherArgs() {
		return otherArgs;
	}
}
