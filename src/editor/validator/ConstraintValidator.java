package editor.validator;

import java.util.ArrayList;
import java.util.List;

import util.fsmmodel.Fsm;
import util.fsmmodel.State;

import model.AbstractOperator;
import model.NotCompleteException;

public abstract class ConstraintValidator {

	private Fsm model = null;
	private String monitor = "";
	private List<ModelChangeListener> listeners = new ArrayList<ModelChangeListener>();
	
	public abstract boolean validate(String ltl) throws ValidationCanceledException;
	public abstract String getLTLForVariable(String variable);
	
	
	public boolean validate(AbstractOperator operator) throws NotCompleteException, ValidationCanceledException {
		String ltl;
		synchronized (this.monitor) {
			ltl = operator.getLTL();
		}
		return validate(ltl);
	}
	
	public List<String> getVariableNames() {
		synchronized (this.monitor) {			
			List<String> variableNames = new ArrayList<String>();
			if (this.model!=null) {
				for (State state : this.model.getAllStates()) {
					variableNames.add(state.getId());
				}
			}
			return variableNames;
		}
	}
	
	public void setModel(Fsm model) {
		synchronized (this.monitor) {
			this.model = model;
			notifyModelChangeListeners_modelChanged(getVariableNames());
		}
	}
	
	public Fsm getModel() {
		synchronized (this.monitor) {
			return this.model;
		}
	}
	
	private void notifyModelChangeListeners_modelChanged(final List<String> variableNames) {
		synchronized (monitor) {
			synchronized (listeners) {
				for (final ModelChangeListener l : listeners) {
					l.modelChanged(variableNames);
				}
			}
		}
	}
	
	public void addModelChangeListener(ModelChangeListener l) {
		synchronized (this.listeners) {
			this.listeners.add(l);
		}
	}
	
	public void removeModelChangeListener(ModelChangeListener l) {
		synchronized (this.listeners) {
			this.listeners.remove(l);
		}
	}
	
}
