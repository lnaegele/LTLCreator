package util.fsmmodel.tools;

public interface ResultListener {

	public void newSubGraphFound(SubGraph subGraph);
	public boolean continueGeneration();
	public void setProgress(int progress);
	
}
