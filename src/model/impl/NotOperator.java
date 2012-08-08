package model.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;

import model.AbstractOperator;
import model.Bucket;
import model.NotCompleteException;
import util.Utils;

public class NotOperator extends AbstractOperator {
	private static final long serialVersionUID = 1L;

	private Bucket a = new Bucket();
	private int vertical_space = 25;
	private Font font = new Font("Arial", Font.BOLD, 15);
	
	public NotOperator() {
		super.add(this.a);
	}
	
	public NotOperator(AbstractOperator op) {
		this();
		this.a.setOperator(op);
	}
	
	@Override
	public AbstractOperator createNewInstance() {
		return new NotOperator();
	}
	
	@Override
	public String getLTL() throws NotCompleteException {
		AbstractOperator op = this.a.getOperator();
		if (op==null) throw new NotCompleteException();
		
		boolean op_brackets = needBrackets(op);
		
		StringBuffer sb = new StringBuffer();
		sb.append("!");
		if (op_brackets) sb.append("(");
		sb.append(op.getLTL());
		if (op_brackets) sb.append(")");
		
		return sb.toString();
	}
	
	private static boolean needBrackets(AbstractOperator op) {
		if (op==null) return false;
//		return !((op instanceof StateOperator) || (op instanceof NotOperator) || (op instanceof NextOperator) || (op instanceof FutureOperator) || (op instanceof AlwaysOperator));
		return !((op instanceof NotOperator) || (op instanceof NextOperator) || (op instanceof FutureOperator) || (op instanceof AlwaysOperator));
	}
	
	@Override
	public Color getColor() {
		return Color.darkGray;
	}
	
	public void setFirstOperator(AbstractOperator operator) {
		this.a.setOperator(operator);
	}

	
	@Override
	public Dimension getOperatorSize() {
		Insets insets = super.getInsets();
		Dimension a_ps = this.a.getPreferredSize();
		
		return new Dimension(insets.left + a_ps.width + insets.right, insets.top + vertical_space + a_ps.height + insets.bottom);
	}

	@Override
	protected void layoutBuckets() {
		Insets insets = super.getInsets();
		Dimension a_ps = this.a.getPreferredSize();
		
		this.a.setBounds(insets.left, insets.top + vertical_space, a_ps.width, a_ps.height);
	}

	@Override
	protected int getCenterLineX() {
		return this.a.getCenterLineX() + this.getInsets().left;
	}
	
	@Override
	public void paintOperator(Graphics2D g, boolean considerHighlighting) {
		g.setColor(this.getBorderColor(considerHighlighting));
	
	    int textCenterPosY = this.getInsets().top + (vertical_space / 2);
		
		g.setFont(this.font);
		Utils.paintTextCentered(g, "NOT", this.getCenterLineX(), textCenterPosY);
	}

}
