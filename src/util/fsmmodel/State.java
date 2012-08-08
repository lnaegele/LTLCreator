package util.fsmmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class State {
	
	private List<Transition> incomingTransitions = new ArrayList<Transition>();
	private List<Transition> outgoingTransitions = new ArrayList<Transition>();
	private String id;
	private Fsm parentFsm = null;
	
	protected State(String id) {
		this.id = id;
	}
	
	public final String getId() {
		return this.id;
	}
	
	public boolean containsTarget(State target) {
		if (target==null) throw new FsmException("Target can not be null.");
		return this.containsTransition(new Transition(this, target));
	}
	
	public boolean containsTransition(Transition transition) {
		if (transition==null) throw new FsmException("Transition can not be null.");
		return this.outgoingTransitions.contains(transition);
	}
	
	public final Transition addTransition(State target) {
		if (target==null) throw new FsmException("Target can not be null.");
		if (this.parentFsm==null) throw new FsmException("Can not add transitions to state '" + this.getId() + "' since it's not part of a fsm.");
		Transition transition = new Transition(this, target);
		if (this.containsTransition(transition)) throw new FsmException("Can not add transition to state '" + transition.getSource().getId() + "' because an equal transition already exists.");
		this.outgoingTransitions.add(transition);
		transition.getTarget().incomingTransitions.add(transition);
		return transition;
	}

	public final void removeTransition(Transition transition) {
		if (transition==null) throw new FsmException("Transition can not be null.");
		if (this.parentFsm==null) throw new FsmException("Can not remove transitions from state '" + this.getId() + "' since it's not part of a fsm.");
		if (transition.getSource()!=this) throw new FsmException("The given transition does not match to state '" + this.getId() + "'.");
		if (!this.containsTransition(transition)) throw new FsmException("Can not remove transition from state '" + this.getId() + "' because it doesn't exist.");
		this.outgoingTransitions.remove(transition);
		transition.getTarget().incomingTransitions.remove(transition);
	}
	
	public final List<State> getSources() {
		List<State> sources = new ArrayList<State>();
		for (Transition incomingTransition : this.incomingTransitions) {
			sources.add(incomingTransition.getSource());
		}
		return sources;
	}
	
	public final List<State> getTargets() {
		List<State> targets = new ArrayList<State>();
		for (Transition outgoingTransition : this.outgoingTransitions) {
			targets.add(outgoingTransition.getTarget());
		}
		return targets;
	}
	
	protected final void setParentFsm(Fsm parentFsm) {
		if ((this.parentFsm==null)==(parentFsm==null)) throw new FsmException("Bad error.");
		this.parentFsm = parentFsm;
	}
	
	public final Fsm getParentFsm() {
		return this.parentFsm;
	}

	public final List<Transition> getIncomingTransitions() {
		return Collections.unmodifiableList(this.incomingTransitions);
	}
	
	public final List<Transition> getOutgoingTransitions() {
		return Collections.unmodifiableList(this.outgoingTransitions);
	}
	
}
