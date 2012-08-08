package editor.tabpane;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import model.AbstractOperator;
import util.components.graphtab.DefaultTitleComponent;
import util.components.graphtab.FunctionTab;
import util.components.graphtab.GraphTab;
import util.components.graphtab.GraphTabPane;
import util.components.graphtab.TitleComponent;
import util.layouts.ListLayout;
import editor.constraintcreator.ConstraintCreator;
import editor.constraintcreator.ConstraintCreatorListener;
import editor.validator.ConstraintValidator;

public class MagicTab extends FunctionTab {

	private GraphTabPane graphTabPane;
	private ConstraintValidator validator;
	
	public MagicTab(GraphTabPane graphTabPane, ConstraintValidator validator) {
		this.graphTabPane = graphTabPane;
		this.validator = validator;
	}
	
	@Override
	protected TitleComponent createTitleComponent() {
		TitleComponent tc = new DefaultTitleComponent();
		tc.setLayout(new BorderLayout());
		tc.add(new JLabel(new ImageIcon(AddTab.class.getResource("magic.png"))));
		tc.setToolTipText("Compute constraints automatically");
		tc.revalidate();
		tc.repaint();
		return tc;
	}

	@Override
	protected void execute(final GraphTabPane graphTabPane, int selectedIndex) {
		class Switch {private boolean active=true;}
		final Switch s = new Switch();
		
		Window parent = SwingUtilities.getWindowAncestor(this.graphTabPane);
		
		final JDialog dlg = new JDialog(parent, "Creating constraints", JDialog.DEFAULT_MODALITY_TYPE);
		Container cont = dlg.getContentPane();
		cont.setLayout(new ListLayout(0, 0, ListLayout.FILL_BEHAVIOR_EQUAL));
		dlg.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		final JProgressBar progress = new JProgressBar();
		progress.setValue(0);
		progress.setString("0 %");
		progress.setIndeterminate(true);
		cont.add(new JLabel("Please wait while generating constraints..."));
		final JLabel foundText = new JLabel("Found: 0");
		cont.add(foundText);
		final JLabel newText = new JLabel("New: 0");
		cont.add(newText);
		cont.add(progress);
		cont.add(new JButton(new AbstractAction("Cancel") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				this.setEnabled(false);
				s.active = false;
			}
		}));
		
		dlg.pack();
		dlg.setLocationRelativeTo(parent);
		dlg.setResizable(false);
		
		dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dlg.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						ConstraintCreator.createConstraints(MagicTab.this.validator, new ConstraintCreatorListener() {
							@Override
							public void createConstraint(final AbstractOperator operator) {
								// nur NEUE Constraints hinzufügen
								boolean similar = false;
								for (GraphTab tab : graphTabPane.getGraphTabs()) {
									final AbstractOperator op = ((OperatorTab)tab).getDashboard().getOperator();
									if (op==null) continue;
									if (op.isSimilar(operator)) {
										similar = true;
										break;
									}
								}

								foundText.setText("Found: " + (Integer.valueOf(foundText.getText().substring(7)) + 1));
								if (!similar) {
									newText.setText("New: " + (Integer.valueOf(newText.getText().substring(5)) + 1));
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											MagicTab.this.graphTabPane.addGraphTab(new OperatorTab(operator), true);
										}
									});
								}
							}
							@Override
							public boolean continueGeneration() {
								return s.active;
							}
							@Override
							public void setProgress(final int progr) {
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										progress.setIndeterminate(false);
										progress.setValue(progr);
										progress.setString(progr + " %");
									}
								});
							}
						});
						
						if (Integer.valueOf(newText.getText().substring(5))==0) JOptionPane.showMessageDialog(dlg, "No new constraints found.", "", JOptionPane.INFORMATION_MESSAGE);
						dlg.setVisible(false);
						dlg.dispose();
					}
				}).start();
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				s.active = false;
			}
		});

		dlg.setVisible(true);
	}
	
}
