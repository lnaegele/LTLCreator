package editor.validator;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import model.AbstractOperator;
import model.NotCompleteException;
import editor.validator.ValidationResultListener.ValidationResult;


public class ValidatorThread {
	
	private static ValidatorThread instance = null;
	
	public static synchronized ValidatorThread getInstance() {
		if (instance==null) instance = new ValidatorThread();
		return instance;
	}
	
	private List<Job> constraints = new ArrayList<Job>();
	private List<Job> currentlyRunningJobs = new ArrayList<Job>();
	
	private ValidatorThread() {
		new Thread() {
			public void run() {
				while (true) {
					final Job job;
					synchronized(constraints) {
						while (constraints.size()==0) {
							try {
								constraints.wait();
							} catch (InterruptedException e) {}
						}
						job = constraints.remove(0);
						if (job.canceled) continue;
							
						currentlyRunningJobs.add(job);
					}
					
					ValidationResult r;
					if (job.operator==null) r = ValidationResult.EMPTY;
					else {
						try {
							if (job.validator.validate(job.operator)) r = ValidationResult.SUCCESSFUL;
							else r = ValidationResult.FAILURE;
						} catch (NotCompleteException e) {
							r = ValidationResult.INCOMPLETE;
						} catch (ValidationCanceledException e) {
							r = ValidationResult.EMPTY;
						}
					}
					
					final ValidationResult result = r;
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							synchronized(constraints) {
								currentlyRunningJobs.remove(job);
								if (!job.canceled) {
									job.validationResultListener.validationResult(job.identifier, result);
								}
							}
						}
					});
				}
			};
		}.start();
	}
	
	public void addToQueue(final Object identifier, AbstractOperator operator, ConstraintValidator validator, final ValidationResultListener l) {
		synchronized(this.constraints) {
			// Alten Job für diesen Constraint entfernen
			this.cancel(identifier);
			
			Job job = new Job(identifier, operator, validator, l);
			this.constraints.add(job);
			this.constraints.notifyAll();
		}
	}
	
	public void cancel(Object identifier) {
		synchronized(this.constraints) {
			for (Job job : this.constraints) {
				if (job.identifier==identifier) {
					job.canceled = true;
				}
			}
			for (Job job : currentlyRunningJobs) {
				if (job.identifier==identifier) {
					job.canceled = true;
				}
			}
		}
	}
	
	private class Job {
		public Object identifier;
		public AbstractOperator operator;
		public ConstraintValidator validator;
		public ValidationResultListener validationResultListener;
		public boolean canceled = false;
		public Job(Object identifier, AbstractOperator operator, ConstraintValidator validator, ValidationResultListener l) {
			this.identifier = identifier;
			this.operator = operator;
			this.validator = validator;
			this.validationResultListener = l;
		}
	}
}
