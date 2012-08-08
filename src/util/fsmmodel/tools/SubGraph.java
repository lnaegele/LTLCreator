package util.fsmmodel.tools;

import java.util.ArrayList;
import java.util.List;

import util.fsmmodel.State;
import util.fsmmodel.Transition;


public class SubGraph {

	private State start, end;
	private List<Transition> visitableTransitions;
	
	protected SubGraph(State start, State end, List<Transition> visitableTransitions) {
		this.start = start;
		this.end = end;
		this.visitableTransitions = visitableTransitions;
	}
	
	public State getStart() {
		return this.start;
	}
	
	public State getEnd() {
		return this.end;
	}
	
	public boolean contains(State state) {
		for (Transition transition : this.visitableTransitions) {
			if (transition.getSource()==state || transition.getTarget()==state) return true;
		}
		return false;
	}

	public List<Transition> getOutgoingTransitions(State state) {
		List<Transition> outgoingTransitions = new ArrayList<Transition>();
		for (Transition outgoingTransition : state.getOutgoingTransitions()) {
			if (this.visitableTransitions.contains(outgoingTransition)) {
				outgoingTransitions.add(outgoingTransition);
			}
		}
		return outgoingTransitions;
	}
	
	public List<Transition> getIncomingTransitions(State state) {
		List<Transition> incomingTransitions = new ArrayList<Transition>();
		for (Transition incomingTransition : state.getIncomingTransitions()) {
			if (this.visitableTransitions.contains(incomingTransition)) {
				incomingTransitions.add(incomingTransition);
			}
		}
		return incomingTransitions;
	}
	
	public List<State> getTargets(State state) {
		List<State> targets = new ArrayList<State>();
		for (Transition outgoingTransition : state.getOutgoingTransitions()) {
			if (this.visitableTransitions.contains(outgoingTransition)) {
				targets.add(outgoingTransition.getTarget());
			}
		}
		return targets;
	}
	
	public List<State> getSources(State state) {
		List<State> sources = new ArrayList<State>();
		for (Transition incomingTransition : state.getIncomingTransitions()) {
			if (this.visitableTransitions.contains(incomingTransition)) {
				sources.add(incomingTransition.getSource());
			}
		}
		return sources;
	}
}
