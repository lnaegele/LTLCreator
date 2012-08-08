package editor.validator;

public interface ValidationResultListener {

	public void validationResult(Object identifier, ValidationResult result);
	
	public static enum ValidationResult {
		SUCCESSFUL, FAILURE, INCOMPLETE, EMPTY
	}
	
}
