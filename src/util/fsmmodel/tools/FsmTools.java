package util.fsmmodel.tools;

import java.util.List;
import java.util.Map;

import util.fsmmodel.Fsm;
import util.fsmmodel.State;


public class FsmTools {

	private FsmTools() {}
	
	/**
	 * Computes subgraphs. A subgraph starts with a state having at least two
	 * outgoing transitions and ends with a state in which all possible paths
	 * coming from the start state flow together. Start and end state are the
	 * only ones which may have incoming transitions from outside.
	 * 
	 * @param model
	 * @return
	 */
	public static List<SubGraph> computeSubgraphs(Fsm model) {
		return SubgraphFinder.computeSubgraphs(model);
	}
	
	/**
	 * Computes subgraphs. A subgraph starts with a state having at least two
	 * outgoing transitions and ends with a state in which all possible paths
	 * coming from the start state flow together. Start and end state are the
	 * only ones which may have incoming transitions from outside.
	 * 
	 * @param model
	 * @return
	 */
	public static void computeSubgraphs(Fsm model, ResultListener l) {
		SubgraphFinder.computeSubgraphs(model, l);
	}
	
	/**
	 * Calculates weights for each state. A states weight increases in having
	 * more incoming transitions or in having important predecessors. A state
	 * passes its weight to its successors in equal parts.
	 * 
	 * @param states
	 * @return
	 */
	public static Map<State, Integer> calculateWeights(List<State> states) {
		return WeightsCalculator.calculateWeights(states);
	}
	
}
