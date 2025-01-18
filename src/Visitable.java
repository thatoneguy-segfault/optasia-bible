import java.util.Collection;

interface Visitable {
	public Collection<Visitable> getChildren();

	//public void accept(DefaultVisitor v);
}
