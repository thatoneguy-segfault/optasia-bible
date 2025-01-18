abstract class HTMLWriter extends HTMLContentWriterVisitor {
	protected HTML out;
	protected boolean doXlink;

	public HTMLWriter(HTML out, boolean doXlink) {
		this.out = out;
		this.doXlink = doXlink;
	}
}
