package editor.toolpane;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import model.impl.AlwaysOperator;
import model.impl.AndOperator;
import model.impl.FutureOperator;
import model.impl.IfThenOperator;
import model.impl.NextOperator;
import model.impl.NotOperator;
import model.impl.OrOperator;
import model.impl.StateOperator;
import model.impl.UntilOperator;


import util.layouts.ListLayout;

public class ToolPane extends JComponent {
	private static final long serialVersionUID = 1L;

	public ToolPane() {
		this.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
		this.setLayout(new ListLayout(5, 5, ListLayout.FILL_BEHAVIOR_INDIVIDUAL));
		this.add(OperatorProvider.createOperatorProvider("IF THEN", new IfThenOperator()));
		this.add(OperatorProvider.createOperatorProvider("AND", new AndOperator()));
		this.add(OperatorProvider.createOperatorProvider("OR", new OrOperator()));
		this.add(OperatorProvider.createOperatorProvider("NOT", new NotOperator()));
		this.add(OperatorProvider.createOperatorProvider("NEXT", new NextOperator()));
		this.add(OperatorProvider.createOperatorProvider("FUTURE", new FutureOperator()));
		this.add(OperatorProvider.createOperatorProvider("ALWAYS", new AlwaysOperator()));
		this.add(OperatorProvider.createOperatorProvider("UNTIL", new UntilOperator()));
		this.add(OperatorProvider.createOperatorProvider("STATE", new StateOperator()));
		this.add(new Trash());
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		g.setColor(Color.gray);
		g.drawLine(1, 0, 1, this.getHeight());
		g.setColor(Color.lightGray);
		g.drawLine(0, 0, 0, this.getHeight());
	}
	
}
