package util.fsmmodel.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.fsmmodel.Fsm;
import util.fsmmodel.State;


public class WeightsCalculator {

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
		
		List<State> states = fsm.getAllStates();
		
		Map<State, Integer> stateWeights = calculateWeights(states);
		for (State state : states) {
			System.out.println(state.getId() + " has weight: " + stateWeights.get(state));
		}
	}
	
	private WeightsCalculator() {}
	
	public static Map<State, Integer> calculateWeights(List<State> states) {
		Map<State, Double> stateWeights = new HashMap<State, Double>();
		
		List<State> statesToRecalculate = new ArrayList<State>();
		for (State s : states) {
			statesToRecalculate.add(s);
		}
		
		while(statesToRecalculate.size()>0) {
			recalculateWeight(statesToRecalculate.remove(0), statesToRecalculate, stateWeights);
		}
		
		// Prioritäten normieren
		double priority_min = getWeight(states.get(0), stateWeights);
		for (State s : states) {
			double weight = getWeight(s, stateWeights);
			if (weight>0 && weight<priority_min) priority_min = weight;
		}
		double factor = 1.0 / priority_min;
		Map<State, Integer> normizedStateWeights = new HashMap<State, Integer>();
		for (State s : states) {
			normizedStateWeights.put(s, (int)Math.round(getWeight(s, stateWeights) * factor));
		}
		
		return normizedStateWeights;		
	}
	
	private static void setWeight(State state, double weight, Map<State, Double> stateWeights) {
		stateWeights.put(state, weight);
	}
	
	private static double getWeight(State state, Map<State, Double> stateWeights) {
		if (!stateWeights.containsKey(state)) return 0;
		return stateWeights.get(state);
	}
	
	private static void recalculateWeight(State state, List<State> statesToRecalculate, Map<State, Double> stateWeights) {
		float weight = 0;
		for (State s : state.getSources()) {
			weight += getWeight(s, stateWeights) / s.getTargets().size();
		}
		if (state.getSources().size()>0) weight = Math.max(1, weight);
		if (weight>getWeight(state, stateWeights)) {
			setWeight(state, weight, stateWeights);
			for (State s : state.getTargets()) {
				statesToRecalculate.add(s);
			}
		}
	}
	
}
