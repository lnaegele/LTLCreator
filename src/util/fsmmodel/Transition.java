package util.fsmmodel;


public class Transition {

	private State source, target;
	
	protected Transition(State source, State target) {
		if (source==null) throw new FsmException("Source can not be null.");
		if (target==null) throw new FsmException("Target can not be null.");
		this.source = source;
		this.target = target;
	}
	
	public State getSource() {
		return this.source;
	}
	
	public State getTarget() {
		return this.target;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(this.getClass().isInstance(obj))) return false;
		return this.source==this.getClass().cast(obj).source &&
				this.target==this.getClass().cast(obj).target;
	}
	
}
