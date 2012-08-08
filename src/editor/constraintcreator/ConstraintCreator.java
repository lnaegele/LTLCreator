package editor.constraintcreator;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import util.fsmmodel.Fsm;
import util.fsmmodel.State;
import util.fsmmodel.tools.FsmTools;
import util.fsmmodel.tools.ResultListener;
import util.fsmmodel.tools.SubGraph;

import model.AbstractOperator;
import model.Bucket;
import model.NotCompleteException;
import model.impl.AlwaysOperator;
import model.impl.AndOperator;
import model.impl.FutureOperator;
import model.impl.IfThenOperator;
import model.impl.NextOperator;
import model.impl.NotOperator;
import model.impl.OrOperator;
import model.impl.StateOperator;
import model.impl.UntilOperator;
import editor.validator.ConstraintValidator;
import editor.validator.ValidationCanceledException;

public final class ConstraintCreator {

	private ConstraintCreator() {}
	
	public static void createConstraints(final ConstraintValidator validator, final ConstraintCreatorListener l) {
		if (validator==null) return;
		final Fsm model = validator.getModel();
		if (model==null) return;
		
		FsmTools.computeSubgraphs(model, new ResultListener() {	
			@Override
			public void newSubGraphFound(SubGraph subGraph) {
				for (AbstractOperator op : generateConstraintsBySubGraph(model, subGraph)) {
					
					// LTL setzen
					for (StateOperator stateOperator : getStateOperators(op)) {
						stateOperator.setLTL(validator.getLTLForVariable(stateOperator.getId()));
					}
					
					// Auf Wahrheit überprüfen
					try {
						if (validator.validate(op)) {
							l.createConstraint(op);
							continue;
						}
					} catch (ValidationCanceledException e) {
					} catch (NotCompleteException e) {}
				}
			}
			@Override
			public boolean continueGeneration() {
				return l.continueGeneration();
			}
			@Override
			public void setProgress(int progress) {
				l.setProgress(progress);
			}
		});
		
	}
	
	private static List<AbstractOperator> generateConstraintsBySubGraph(Fsm model, SubGraph subGraph) {
		List<AbstractOperator> operators = new ArrayList<AbstractOperator>();
		
		if (subGraph.getStart()==subGraph.getEnd()) {
				
			// Alle Pfade laufen wieder im gleichen Zustand zusammen
			AlwaysOperator op = new AlwaysOperator(new FutureOperator(new StateOperator(subGraph.getStart().getId())));
			operators.add(op);
			
			if (!subGraph.getSources(subGraph.getEnd()).contains(subGraph.getStart())) {
				
				// Wenn man im Anfangszustand startet, müssen die Vorgängerzustände des Endzustands vor dem Endzustand selbst besucht werden.
				AlwaysOperator op2 = new AlwaysOperator(
					new IfThenOperator(
						new StateOperator(subGraph.getStart().getId()), 
						new NextOperator(
							new UntilOperator(
								new NotOperator(
									new StateOperator(subGraph.getEnd().getId())
								), 
								getOrCombination(subGraph.getEnd().getSources())
							)
						)
					)
				);
				
				operators.add(op2);
			}
		}
		else if (subGraph.getSources(subGraph.getEnd()).contains(subGraph.getStart())) {
			// Sehr kleiner Subgraph, der mindestens einen Pad mit nur einer Transition hat.
		}
		else {
			// Größerer Subgraph, und der Startzustand ist ungleich dem Endzustand
			
			// Wenn man im Anfangszustand startet, wird man irgendwann im Endzustand landen.
			AlwaysOperator op = new AlwaysOperator(
				new IfThenOperator(
					new StateOperator(subGraph.getStart().getId()), 
					new FutureOperator(
						new StateOperator(subGraph.getEnd().getId())
					)
				));
				
			operators.add(op);
				
			
			
			// Wenn man im Anfangszustand startet, wird mind. einer der Vorgängerzustände des Endzustands irgendwann besucht.
			AlwaysOperator op1 = new AlwaysOperator(
				new IfThenOperator(
					new StateOperator(subGraph.getStart().getId()), 
					new FutureOperator(
						getOrCombination(subGraph.getSources(subGraph.getEnd()))
					)
				)
			);
			
			operators.add(op1);
			
			

			// Wenn man im Anfangszustand startet, müssen die Vorgängerzustände des Endzustands vor dem Endzustand selbst besucht werden.
			AlwaysOperator op2 = new AlwaysOperator(
				new IfThenOperator(
					new StateOperator(subGraph.getStart().getId()), 
					new UntilOperator(
						new NotOperator(
							new StateOperator(subGraph.getEnd().getId())
						), 
						getOrCombination(subGraph.getSources(subGraph.getEnd()))
					)
				)
			);
			
			operators.add(op2);
			
			

			// Wenn man im Anfangszustand startet, kann man keinen Zustand außerhalb des Subgraphs besuchen bevor man den Endzustand erreicht.
			List<AbstractOperator> and = new ArrayList<AbstractOperator>();
			for (State state : model.getAllStates()) {
				if (subGraph.contains(state)) continue;
				and.add(new NotOperator(new StateOperator(state.getId())));
			}
			if (and.size()>0) {
				AlwaysOperator op3 = new AlwaysOperator(
					new IfThenOperator(
						new StateOperator(subGraph.getStart().getId()), 
						new UntilOperator(
							getAndCombination(and), 
							new StateOperator(subGraph.getEnd().getId())
						)
					)
				);
					
				operators.add(op3);
			}
		}
		
		return operators;
	}
	
	private static List<StateOperator> getStateOperators(AbstractOperator operator) {
		List<StateOperator> result = new ArrayList<StateOperator>();
		getStateOperators(operator, result);
		return result;
	}
	private static void getStateOperators(AbstractOperator operator, List<StateOperator> result) {
		if (operator==null) return;
		if (operator instanceof StateOperator) {
			result.add((StateOperator)operator);
			return;
		}
		for (Component c : operator.getComponents()) {
			getStateOperators(((Bucket)c).getOperator(), result);
		}
	}
	
	private static AbstractOperator getOrCombination(List<State> states) {
		List<AbstractOperator> operators = new ArrayList<AbstractOperator>();
		for (State state : states) {
			operators.add(new StateOperator(state.getId()));
		}
		return getOrCombination(operators, 0, operators.size()-1);
	}
	private static AbstractOperator getOrCombination(List<AbstractOperator> operators, int start, int end) {
		if (start==end) return operators.get(start);
		int count = (end+1) - start;
		int n = count / 2;
		return new OrOperator(getOrCombination(operators, start, start + n - 1), getOrCombination(operators, start + n, end));
	}
	
	private static AbstractOperator getAndCombination(List<AbstractOperator> operators) {
		return getAndCombination(operators, 0, operators.size()-1);
	}
	private static AbstractOperator getAndCombination(List<AbstractOperator> operators, int start, int end) {
		if (start==end) return operators.get(start);
		int count = (end+1) - start;
		int n = count / 2;
		return new AndOperator(getAndCombination(operators, start, start + n - 1), getAndCombination(operators, start + n, end));
	}
	
}
