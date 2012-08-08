package util.fsmmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Fsm {

	private State initialState;
	private List<State> states = new ArrayList<State>();
	private boolean allowDuplicateStateNames;
	
	public Fsm() {
		this(false);
	}
	
	public Fsm(boolean allowDuplicateStateNames) {
		this.allowDuplicateStateNames = allowDuplicateStateNames;
	}
	
	public boolean containsStateId(String stateId) {
		synchronized (this.states) {
			if (stateId==null) throw new FsmException("State name can not be null.");
			for (State state : this.states) {
				if (stateId.equals(state.getId())) return true;
			}
			return false;
		}
	}
	
	public State addState(String id) {
		synchronized (this.states) {
			if (id==null) throw new FsmException("Id can not be null.");
			State state = new State(id);
			this.addState(state);
			return state;
		}
	}
	
	public void addState(State state) {
		synchronized (this.states) {
			if (state==null) throw new FsmException("State can not be null.");
			if (this.states.contains(state)) throw new FsmException("State can not be added to fsm twice.");
			if (!this.allowDuplicateStateNames && this.containsStateId(state.getId())) throw new FsmException("There exists already a state called '" + state.getId() + "'.");
			if (state.getIncomingTransitions().size()>0 || state.getOutgoingTransitions().size()>0) throw new FsmException("Bad error.");
			
			Fsm parentFsm = state.getParentFsm();
			if (parentFsm!=null) {
				throw new FsmException("Can not add state '" + state.getId() + "' to fsm since it's already part of another fsm.");
			}
			state.setParentFsm(this);
			this.states.add(state);
			
		}
	}
	
// 	public void copyFsm(Fsm fsm) {
// 		Map<State, State> newStates = new HashMap<State, State>();
// 		synchronized (this.states) {
// 			for (State state : fsm.getAllStates()) {
// 				State newState = getImportedStateForExternalState(state, newStates);
// 				for (Transition transition : state.getOutgoingTransitions()) {
// 					// TODO:
// 					throw new RuntimeException("Not implemented yet.");
// 				}
// 			}
// 		}
// 	}
// 	
// 	private State getImportedStateForExternalState(State externalState, Map<State, State> newStates) {
// 		if (!newStates.containsKey(externalState)) {
//			State newState = new State(externalState.getId());
//			newStates.put(externalState, newState);
//			this.addState(newState);
//		}
//		return newStates.get(externalState);
// 	}
	
	public void removeState(State state) {
		synchronized (this.states) {
			if (state==null) throw new FsmException("State can not be null.");
			if (!this.states.contains(state)) throw new FsmException("State '" + state.getId() + "' can not be removed because it's not part of the fsm.");
			if (state.getIncomingTransitions().size()>0 || state.getOutgoingTransitions().size()>0) throw new FsmException("State '" + state.getId() + "' can not be removed because it still has incoming or outgoing transitions.");
			
			state.setParentFsm(null);
			this.states.remove(state);
			if (this.initialState.equals(state)) this.initialState = null;
		}
	}

	public void setInitialState(State initialState) {
		synchronized (this.states) {
			if (initialState==null) throw new FsmException("Initial state can not be null.");
			if (!this.states.contains(initialState)) throw new FsmException("Initial state must be part of state machine.");
			this.initialState = initialState;
		}
	}
	
	public State getInitialState() {
		synchronized (this.states) {
			if (this.initialState==null) throw new FsmException("Initial state not yet set.");
			return this.initialState;
		}
	}

	public List<State> getAllStates() {
		synchronized (this.states) {
			return Collections.unmodifiableList(this.states);
		}
	}
	
}
