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
				

				Fsm model = new Fsm();

//				State s1 = model.addState("Menu");
//				State s2 = model.addState("Polling");
//				State s3 = model.addState("Are you..?");
//				State s4 = model.addState("Person valid");
//				State s5 = model.addState("Not taken yet");
//				State s6 = model.addState("No help");
//				State s7 = model.addState("Reason");
//				State s8 = model.addState("Staff notified");
//				State s9 = model.addState("Guided intake");
//				State s10 = model.addState("Well done!");
//				
//				s1.addTransition(s2);
//				s2.addTransition(s1);
//				s2.addTransition(s3);
//				s3.addTransition(s1);
//				s3.addTransition(s4);
//				s4.addTransition(s2);
//				s4.addTransition(s5);
//				s5.addTransition(s6);
//				s5.addTransition(s9);
//				s6.addTransition(s7);
//				s6.addTransition(s8);
//				s7.addTransition(s8);
//				s8.addTransition(s2);
//				s9.addTransition(s10);
//				s10.addTransition(s2);
//				model.setInitialState(s1);
				
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
				
//				new Thread(new Runnable() {
//					
//					@Override
//					public void run() {
//						try {
//							Thread.sleep(6000);
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						
//						Fsm model = new Fsm();
//
//						State s1 = model.addState("Menu");
//						State s2 = model.addState("Polling");
//						State s3 = model.addState("Are you..?");
//						State s4 = model.addState("Person valid");
//						State s5 = model.addState("Not taken yet");
//						State s6 = model.addState("No help");
//						model.setInitialState(s1);
//						
//						editor.setModel(model);
//						
//					}
//				}).start();
			}
		});
	}
	
}
