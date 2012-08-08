package util.components.graphtab;

public interface GraphTabListener {

	public void selectedIndexChanged(int index);
	public void graphTabAdded(GraphTab graphTab);
	public void graphTabRemoved(GraphTab graphTab);
	
}
