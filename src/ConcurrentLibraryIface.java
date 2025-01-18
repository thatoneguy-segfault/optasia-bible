import java.util.Set;
import java.util.ArrayList;

/*
 * An interface that should only allow thread-safe operations.
 */
interface ConcurrentLibraryIface {
	//public String getOutputDirectory();
	public Path getInputDirectory();
	public Set<String> getCollectionNames();
	public ArrayList<String> getCollectionTranslationNames(String collectionName);
	public BibleFactory getBibleFactory(String shortname);

	public LibraryIndexView getNonProductionIndex();
	public LibraryIndexView getProductionIndex(String collectionName);
}
