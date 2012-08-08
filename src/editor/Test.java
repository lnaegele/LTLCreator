package editor;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import util.fsmmodel.Fsm;
import util.fsmmodel.State;


public class Test {
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				final Editor editor = new Editor();
				editor.addNewEmptyDashboard(false);
				
				/* medication reminder example */
				Fsm model = new Fsm();
				
				State menu = model.addState("Menu");
				State additionalservices = model.addState("Additional services");
				State polling = model.addState("Polling");
				State pendingreminder = model.addState("Pending reminder");
				State nottakenalready = model.addState("Not taken already");
				State refuseintake = model.addState("Refuse intake");
				State reason = model.addState("Reason");
				State staffnotified = model.addState("Staff notified");
				State guidedintake = model.addState("Guided intake");
				State nohelp = model.addState("No help");
				State welldone = model.addState("Well done!");
				
				menu.addTransition(additionalservices);
				menu.addTransition(polling);
				additionalservices.addTransition(polling);
				polling.addTransition(menu);
				polling.addTransition(pendingreminder);
				pendingreminder.addTransition(polling);
				pendingreminder.addTransition(nottakenalready);
				nottakenalready.addTransition(refuseintake);
				refuseintake.addTransition(reason);
				refuseintake.addTransition(staffnotified);
				reason.addTransition(staffnotified);
				staffnotified.addTransition(polling);
				nottakenalready.addTransition(guidedintake);
				guidedintake.addTransition(welldone);
				welldone.addTransition(polling);
				nottakenalready.addTransition(nohelp);
				nohelp.addTransition(guidedintake);
				nohelp.addTransition(welldone);
				
				model.setInitialState(menu);
				editor.setModel(model);
				
				final JFrame frame = new JFrame("LTL Creator by Ludwig Nägele");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.add(editor);
				frame.pack();
				frame.setMinimumSize(new Dimension(400,300));
				frame.setLocationByPlatform(true);
				frame.setVisible(true);
			}
		});
	}
	
}
