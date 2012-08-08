package editor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import model.AbstractOperator;
import model.Bucket;
import model.NotCompleteException;
import model.OperatorChangeListener;
import model.impl.StateOperator;
import editor.tabpane.OperatorTab;
import editor.toolpane.StatePainter;
import editor.validator.ConstraintValidator;
import editor.validator.ModelChangeListener;
import editor.validator.ValidationResultListener;
import editor.validator.ValidatorThread;

public class OperatorAndModelChangeListener implements OperatorChangeListener, ValidationResultListener, ModelChangeListener {
	private OperatorTab operatorTab;
	private ConstraintValidator validator;
	private String monitor = "";
	
	public OperatorAndModelChangeListener(OperatorTab operatorTab, ConstraintValidator validator) {
		this.operatorTab = operatorTab;
		this.validator = validator;
	}
	
	@Override
	public void modelChanged(final List<String> variableNames) {			
		synchronized (this.monitor) {
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						modelChanged(variableNames);
					}
				});
				return;
			}
			
			AbstractOperator op = this.operatorTab.getDashboard().getOperator();
			if (op==null) return;
			
			boolean change = false;
			for (StateOperator stateOperator : getStateOperators(op)) {
				String oldLTL = null;
				try {
					oldLTL = stateOperator.getLTL();
				} catch (NotCompleteException e) {}
				String newLTL = this.validator.getLTLForVariable(stateOperator.getId());
				if (oldLTL==null && newLTL==null) continue;
				if (oldLTL!=null && oldLTL.equals(newLTL)) continue;
				stateOperator.setLTL(newLTL);
				change = true;
			}
			if (change) op.notifyChangeListeners_operatorChanged();
			else revalidateOperator(op);
		}
	}
		
	@Override
	public void operatorChanged(final AbstractOperator operator) {
		synchronized (this.monitor) {
				
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						operatorChanged(operator);
					}
				});
				return;
			}
		
			revalidateOperator(operator);
		}
	}
	
	private void revalidateOperator(AbstractOperator operator) {
		ValidatorThread.getInstance().cancel(this.operatorTab);
		
		AbstractOperator op = null;
		if (operator!=null) {
			// Variablennamen setzen
			for (final StateOperator stateOperator : getStateOperators(operator)) {
				if (stateOperator.getId()==null) {
					showChangeState(stateOperator);
					return;
				}
				stateOperator.setLTL(this.validator.getLTLForVariable(stateOperator.getId()));
			}
			
			// operator copieren
			op = copyOperator(operator);
		}
		
		this.operatorTab.showProgress();
		ValidatorThread.getInstance().addToQueue(this.operatorTab, op, this.validator, this);
	}
	
	@Override
	public void validationResult(Object identifier, ValidationResult result) {
		if (result==ValidationResult.SUCCESSFUL) this.operatorTab.showSuccess();
		else if (result==ValidationResult.FAILURE) this.operatorTab.showFailure();
		else if (result==ValidationResult.INCOMPLETE) this.operatorTab.showIncomplete();
		else this.operatorTab.showNil();
	}
	
	private List<StateOperator> getStateOperators(AbstractOperator operator) {
		List<StateOperator> result = new ArrayList<StateOperator>();
		getStateOperators(operator, result);
		return result;
	}
	private void getStateOperators(AbstractOperator operator, List<StateOperator> result) {
		if (operator==null) return;
		if (operator instanceof StateOperator) {
			result.add((StateOperator)operator);
			return;
		}
		for (Component c : operator.getComponents()) {
			getStateOperators(((Bucket)c).getOperator(), result);
		}
	}
	
	private void showChangeState(final StateOperator stateOperator) {
		stateOperator.getParent().doLayout();
		
		List<String> variables;
		synchronized (this.monitor) {
			variables = this.validator.getVariableNames();
		}
		
		if (variables.size()==0) {
			JOptionPane.showMessageDialog(stateOperator, "There are no states to select.", "No states", JOptionPane.INFORMATION_MESSAGE);
			stateOperator.getCurrentParent().undock(stateOperator);
			return;
		}
		
		final JDialog diag = new JDialog(SwingUtilities.getWindowAncestor(stateOperator), "Choose state", JDialog.DEFAULT_MODALITY_TYPE);
		final JPanel panel = new JPanel(new GridLayout(0, 3, 5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		MouseListener ml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				synchronized (OperatorAndModelChangeListener.this.monitor) {
					String id = ((StatePainter)e.getSource()).getDescription();
					stateOperator.setLTL(OperatorAndModelChangeListener.this.validator.getLTLForVariable(id));
					stateOperator.setId(id);
					diag.setVisible(false);
				}
			}
		};
		
		for (String stateName : variables) {
			JComponent c2 = new StatePainter(stateName, stateOperator.getBackgroundColor1(false), stateOperator.getBackgroundColor2(false), stateOperator.getBorderColor(false));
			c2.setToolTipText(stateName);
			c2.addMouseListener(ml);
			panel.add(c2);
		}

		JScrollPane scroll = new JScrollPane(panel);
		scroll.setBorder(null);
		diag.add(scroll);
		
		int maxHeight = 350;
		Dimension ps = scroll.getPreferredSize();
		if (ps.height<350) {
			scroll.setPreferredSize(new Dimension(ps.width, ps.height));
		}
		else {
			scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			ps = scroll.getPreferredSize();
			scroll.setPreferredSize(new Dimension(ps.width, maxHeight));
			scroll.getVerticalScrollBar().setUnitIncrement(8);
		}
		diag.pack();
		diag.setResizable(false);
		diag.setLocationRelativeTo(stateOperator);
		
		diag.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		diag.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				synchronized (OperatorAndModelChangeListener.this.monitor) {
					stateOperator.getCurrentParent().undock(stateOperator);
					diag.setVisible(false);
				}
			}
		});
		diag.setVisible(true);
	}
	
	private AbstractOperator copyOperator(AbstractOperator operator) {
		if (operator==null) return null;
		AbstractOperator op = operator.createNewInstance();
		if (op instanceof StateOperator) {
			((StateOperator)op).setId(((StateOperator)operator).getId());
			try {
				((StateOperator)op).setLTL(((StateOperator)operator).getLTL());
			} catch (NotCompleteException e) {}
		}
		else {
			for (int i=0; i<operator.getComponentCount(); i++) {
				((Bucket)op.getComponent(i)).setOperator(copyOperator(((Bucket)operator.getComponent(i)).getOperator()));
			}
		}
		return op;
	}
}
