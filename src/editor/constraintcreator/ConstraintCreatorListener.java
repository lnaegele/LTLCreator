package editor.constraintcreator;

import model.AbstractOperator;

public interface ConstraintCreatorListener {

	public void createConstraint(AbstractOperator operator);
	public boolean continueGeneration();
	public void setProgress(int progress);
	
}
