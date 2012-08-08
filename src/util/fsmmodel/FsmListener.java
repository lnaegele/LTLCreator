package util.fsmmodel;

public interface FsmListener {

	public void stateAdded(State state);
	public void stateRemoved(State state);
	public void transitionAdded(Transition transition);
	public void transitionRemoved(Transition transition);
	
}
