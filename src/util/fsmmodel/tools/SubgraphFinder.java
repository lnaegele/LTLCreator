package util.fsmmodel.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.fsmmodel.Fsm;
import util.fsmmodel.State;
import util.fsmmodel.Transition;


public class SubgraphFinder {

	public static void main(String[] args) {
		Fsm fsm = new Fsm();

		State s1 = fsm.addState("State1");
		State s2 = fsm.addState("State2");
		State s3 = fsm.addState("State3");
		State s4 = fsm.addState("State4");
		State s5 = fsm.addState("State5");
		State s6 = fsm.addState("State6");
		State s7 = fsm.addState("State7");
		State s8 = fsm.addState("State8");
		State s9 = fsm.addState("State9");
		State s10 = fsm.addState("State10");
		
		s1.addTransition(s2);
		s2.addTransition(s1);
		s2.addTransition(s3);
		s3.addTransition(s1);
		s3.addTransition(s4);
		s4.addTransition(s2);
		s4.addTransition(s5);
		s5.addTransition(s6);
		s5.addTransition(s9);
		s6.addTransition(s7);
		s6.addTransition(s8);
		s7.addTransition(s8);
		s8.addTransition(s2);
		s9.addTransition(s10);
		s10.addTransition(s2);
		
		for (SubGraph subGraph : computeSubgraphs(fsm)) {
			State source = subGraph.getStart();
			State target = subGraph.getEnd();
			if (source.equals(target)) {
				System.out.println(source.getId() + " is an important and central state.");
			}
			else {
				boolean isSmall = false;
				for (State s : target.getSources()) {
					if (s.equals(source)) {
						isSmall = true;
						break;
					}
				}
				System.out.println(source.getId() + " always leads finally to " + target.getId() + " (before " + source.getId() + " can be visited again).");

				if (!isSmall) {
					List<Transition> subgraphSourcesOfTarget = subGraph.getIncomingTransitions(target);
					
					String targets = subgraphSourcesOfTarget.get(0).getSource().getId();
					for (int i=1; i<subgraphSourcesOfTarget.size(); i++) {
						targets += ", " + subgraphSourcesOfTarget.get(i).getSource().getId();
					}
					System.out.println(source.getId() + " always leads finally to at least one of: " + targets + " (before " + source.getId() + " can be visited again).");
				}
			}
		}
	}
	
	private SubgraphFinder() {}
	
	/**
	 * Ermittelt Subgraphen, wobei der Startzustand und der Endzustand beliebig
	 * viele eingehende Transitionen von außerhalb haben dürfen.
	 * 
	 * @param model
	 * @return
	 */
	public static List<SubGraph> computeSubgraphs(Fsm model) {
		List<SubGraph> result = new ArrayList<SubGraph>();
		for (State state : model.getAllStates()) {
			if (state.getTargets().size()<2) continue;
			try {
				result.add(findEndSubGraph(state, null));
			} catch (InterruptedException e) {}
		}
		return result;
	}

	/**
	 * Ermittelt Subgraphen, wobei der Startzustand und der Endzustand beliebig
	 * viele eingehende Transitionen von außerhalb haben dürfen.
	 * 
	 * @param model
	 * @return
	 */
	public static void computeSubgraphs(Fsm model, ResultListener l) {
		List<State> allStates = model.getAllStates();
		for (int i=0; i<allStates.size(); i++) {
			l.setProgress(((i+1)*100) / allStates.size());
			State state = allStates.get(i);
			if (!l.continueGeneration()) return;
			if (state.getTargets().size()<2) continue;
			
			// TODO: Problem: StackOverflow
			try {
				l.newSubGraphFound(findEndSubGraph(state, l));
			} catch (InterruptedException e) {
				return;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Findet einen Zustand, in dem alle möglichen Pfade ausgehend vom
	 * angegebenen Startzustand wieder zusammenlaufen.
	 * 
	 * @param startState
	 * @return
	 * @throws InterruptedException 
	 */
	public static SubGraph findEndSubGraph(State startState, ResultListener l) throws InterruptedException {
		Map<State, Integer> stateVisits = markChildren(startState, l);
		State result = findFirstCommonState(startState, stateVisits, l);
		if (result==null) result = startState;
		
		return new SubGraph(startState, result, getVisitedTransitionsBetween(startState, result, l));
	}

	/**
	 * Geht jeden möglichen Pfad (alle möglichen Kombinationen) ab und addiert 1
	 * auf jeden besuchten Zustand.
	 * 
	 * @param state
	 * @return
	 * @throws InterruptedException 
	 */
	private static Map<State, Integer> markChildren(State state, ResultListener l) throws InterruptedException {
		Map<State, Integer> stateVisits = new HashMap<State, Integer>();
		markChildrenHelper(state, new ArrayList<Transition>(), stateVisits, l);
		return stateVisits;
	}
	
	private static int markChildrenHelper(State state, List<Transition> history, Map<State, Integer> stateVisits, ResultListener l) throws InterruptedException {
		int pathsBackToStartState = 0;
		State startState = state;
		if (history.size()>0) startState = history.get(0).getSource();
		
		l1: for (Transition outgoingTransition : state.getOutgoingTransitions()) {
			if (l!=null && !l.continueGeneration()) throw new InterruptedException();
			
			// Diesen Zustand in diesem Zweig schon besucht?
			for (Transition t : history) {
				if (outgoingTransition.getTarget().equals(t.getTarget())) continue l1;
			}
			
			// Wieder beim Anfangszustand?
			if (outgoingTransition.getTarget().equals(startState)) {
				pathsBackToStartState += 1;
				continue;
			}
			
			history.add(outgoingTransition);
			pathsBackToStartState += markChildrenHelper(outgoingTransition.getTarget(), history, stateVisits, l);
			history.remove(outgoingTransition);
		}
		
		// Besuchscounter anpassen
		if (stateVisits.containsKey(state)) {
			stateVisits.put(state, stateVisits.get(state) + pathsBackToStartState);
		}
		else {
			stateVisits.put(state, pathsBackToStartState);
		}
		
		return pathsBackToStartState;
	}
	
	/**
	 * Findet den ersten Zustand mit 'markValue' Besuchen. Dies ist der Zustand,
	 * wo alle Pfade ausgehend von einem Startzustand aus enden. Entspricht
	 * dieser Zustand dem Startzustand, so gibt es keinen kleineren Subgraphen
	 * als den kompletten Graph selbst (der zyklisch ist).
	 * 
	 * @param state
	 * @param stateVisits
	 * @return
	 * @throws InterruptedException 
	 */
	private static State findFirstCommonState(State state, Map<State, Integer> stateVisits, ResultListener l) throws InterruptedException {
		return findFirstCommonStateHelper(state, stateVisits.get(state), new ArrayList<Transition>(), stateVisits, l);
	}
	private static State findFirstCommonStateHelper(State state, int markValue, List<Transition> history, Map<State, Integer> stateVisits, ResultListener l) throws InterruptedException {
		State startState = state;
		if (history.size()>0) startState = history.get(0).getSource();
		
		l1: for (Transition outgoingTransition : state.getOutgoingTransitions()) {
			if (l!=null && !l.continueGeneration()) throw new InterruptedException();
			
			for (Transition t : history) {
				if (outgoingTransition.getTarget().equals(t.getTarget())) continue l1;
			}
			
			State found = null;
			if (stateVisits.get(outgoingTransition.getTarget())==markValue) {
				found = outgoingTransition.getTarget();
			}
			
			if (found==null) {		
				history.add(outgoingTransition);
				found = findFirstCommonStateHelper(outgoingTransition.getTarget(), markValue, history, stateVisits, l);
				history.remove(outgoingTransition);
			}
			
			if (found!=null) {
				if (startState.equals(found)) return found;
				if (!transitionsComingFromOutside(startState, found, l)) return found;
			}
		}
		return null;
	}
	
	/**
	 * Überprüft ob zwischen zwei Zuständen andere Transitionen von außerhalb
	 * kommen. Der Startzustand darf dabei beliebig viele Transitionen von
	 * außerhalb besitzen, ebenso wie der Zielzustand.
	 * 
	 * @param start
	 * @param end
	 * @return
	 * @throws InterruptedException 
	 */
	private static boolean transitionsComingFromOutside(State start, State end, ResultListener l) throws InterruptedException {
		List<Transition> visitedTransitions = getVisitedTransitionsBetween(start, end, l);
		for (State end2 : end.getSources()) {
			if (!visitedTransitions.contains(end2)) continue;
			if (transitionsComingFromOutsideHelper(start, end2, new ArrayList<Transition>(), visitedTransitions, l)) return true;
		}
		return false;
	}
	private static boolean transitionsComingFromOutsideHelper(State start, State end, List<Transition> history, List<Transition> globallyVisitedTransitions, ResultListener l) throws InterruptedException {
		if (end.equals(start)) return false;
		
		for (Transition incomingTransition : end.getIncomingTransitions()) {
			if (l!=null && !l.continueGeneration()) throw new InterruptedException();
			
			if (history.contains(incomingTransition)) continue;
			if (!globallyVisitedTransitions.contains(incomingTransition)) {
				return true;
			}
			history.add(incomingTransition);
			if (transitionsComingFromOutsideHelper(start, incomingTransition.getSource(), history, globallyVisitedTransitions, l)) {
				history.remove(incomingTransition);
				return true;
			}
			history.remove(incomingTransition);
		}
		return false;
	}
	
	/**
	 * Liefert die Menge aller Transitionen, die zwischen Start- und Endzustand besucht werden können.
	 * @param start
	 * @param end
	 * @return
	 * @throws InterruptedException 
	 */
	private static List<Transition> getVisitedTransitionsBetween(State start, State end, ResultListener l) throws InterruptedException {
		List<Transition> visitedTransitions = new ArrayList<Transition>();
		getVisitedTransitionsBetweenHelper(start, end, visitedTransitions, l);
		return visitedTransitions;
	}
	private static void getVisitedTransitionsBetweenHelper(State start, State end, List<Transition> visitedTransitions, ResultListener l) throws InterruptedException {
		if (start.equals(end)) {
			// Existieren Transitionen auf sich selbst?
			for (Transition outgoingTransition : start.getOutgoingTransitions()) {
				if (outgoingTransition.getTarget().equals(end)) visitedTransitions.add(outgoingTransition);
			}
			return;
		}
		
		for (Transition outgoingTransition : start.getOutgoingTransitions()) {
			if (l!=null && !l.continueGeneration()) throw new InterruptedException();
			
			if (visitedTransitions.contains(outgoingTransition)) continue;
			visitedTransitions.add(outgoingTransition);
			getVisitedTransitionsBetweenHelper(outgoingTransition.getTarget(), end, visitedTransitions, l);
		}
	}
	
//	/**
//	 * Ermittelt, ob dieser Subgraph Endzustände enthält, also ob es terminierende Pfade gibt, auf denen der Subgraph-Endzustand nie erreicht wird.
//	 * @param start
//	 * @param end
//	 * @return
//	 */
//	public static boolean containsTerminationStates(State start, State end) {
//		return containsTerminationStatesHelper(start, end, new ArrayList<State>());
//	}
//	private static boolean containsTerminationStatesHelper(State start, State end, List<State> history) {
//		if (start.equals(end)) {
//			return false;
//		}
//
//		boolean result = true;
//		history.add(start);
//		for (State target : start.getTargets()) {
//			result = false;
//			if (history.contains(target)) continue;
//			if (containsTerminationStatesHelper(target, end, history)) {
//				result = true;
//				break;
//			}
//		}
//		history.remove(start);
//		return result;
//	}
//
//	/**
//	 * Ermittelt, ob es zwischen Start- und Endzustand endlos lange Abläufe geben könnte.
//	 * @param start
//	 * @param end
//	 * @return
//	 */
//	public static boolean containsCycle(State start, State end) {
//		return containsCycleHelper(start, end, new ArrayList<State>());
//	}
//	private static boolean containsCycleHelper(State start, State end, List<State> history) {
//		if (start.equals(end)) {
//			// Existieren Transitionen auf sich selbst?
//			for (State target : start.getTargets()) {
//				if (target.equals(end)) return true;
//			}
//			return false;
//		}
//
//		boolean result = true;
//		history.add(start);
//		for (State target : start.getTargets()) {
//			result = false;
//			if (history.contains(target) || containsCycleHelper(target, end, history)) {
//				result = true;
//				break;
//			}
//		}
//		history.remove(start);
//		return result;
//	}
	
	/**
	 * Ermittelt, ob alle Pfade ausgehend von start auch in end ankommen (keine Zyklen, keine terminierenden Zustände).
	 * Wenn start gleich end ist, wird nicht automatisch true zurückgeliefert. Es muss mindestens eine Transition gegangen sein.
	 * @param start
	 * @param end
	 * @return
	 */
	public static boolean allPathsFromLeadTo(State start, State end) {
		List<State> history = new ArrayList<State>();
		
		boolean result = false;
		history.add(start);
		for (State target : start.getTargets()) {
			result = true;
			if (!allPathsFromLeadToHelper(target, end, new ArrayList<State>())) {
				result = false;
				break;
			}
		}
		history.remove(start);
		return result;
	}
	
	private static boolean allPathsFromLeadToHelper(State start, State end, List<State> history) {
		if (start.equals(end)) return true;
		if (history.contains(start)) return false;
		
		boolean result = false;
		history.add(start);
		for (State target : start.getTargets()) {
			result = true;
			if (!allPathsFromLeadToHelper(target, end, new ArrayList<State>())) {
				result = false;
				break;
			}
		}
		history.remove(start);
		return result;
	}
}
