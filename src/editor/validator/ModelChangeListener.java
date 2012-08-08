package editor.validator;

import java.util.List;

public interface ModelChangeListener {

	public void modelChanged(List<String> variableNames);
	
}
