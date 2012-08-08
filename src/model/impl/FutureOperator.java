package model.impl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;

import model.AbstractOperator;
import model.Bucket;
import model.NotCompleteException;
import util.Utils;

public class FutureOperator extends AbstractOperator {
	private static final long serialVersionUID = 1L;

	private Bucket a = new Bucket();
	private int left_space = 75;
	private Font font = new Font("Arial", Font.BOLD, 15);
	
	public FutureOperator() {
		super.add(this.a);
	}
	
	public FutureOperator(AbstractOperator op) {
		this();
		this.a.setOperator(op);
	}
	
	@Override
	public AbstractOperator createNewInstance() {
		return new FutureOperator();
	}
	
	@Override
	public String getLTL() throws NotCompleteException {
		AbstractOperator op = this.a.getOperator();
		if (op==null) throw new NotCompleteException(); 
		
		boolean op_brackets = needBrackets(op);
		
		StringBuffer sb = new StringBuffer();
		sb.append("F ");
		if (op_brackets) sb.append("(");
		sb.append(op.getLTL());
		if (op_brackets) sb.append(")");
		
		return sb.toString();
	}
	
	private static boolean needBrackets(AbstractOperator op) {
		if (op==null) return false;
		return !((op instanceof StateOperator) || (op instanceof NotOperator) || (op instanceof NextOperator) || (op instanceof FutureOperator) || (op instanceof AlwaysOperator));
	}
	
	@Override
	public Color getColor() {
		return Color.magenta;
	}
	
	public void setOperator(AbstractOperator operator) {
		this.a.setOperator(operator);
	}
	
	@Override
	public Dimension getOperatorSize() {
		Insets insets = this.getInsets();
		
		Dimension ps = this.a.getPreferredSize();
		
		return new Dimension(insets.left + left_space + ps.width + insets.right, insets.top + ps.height + insets.bottom);
	}

	@Override
	protected void layoutBuckets() {
		Insets insets = this.getInsets();
		
		Dimension ps = this.a.getPreferredSize();
		this.a.setBounds(insets.left + left_space, insets.top, ps.width, ps.height);
	}

	@Override
	protected int getCenterLineX() {
		return this.getInsets().left + 25;
	}
	
	@Override
	public void paintOperator(Graphics2D g, boolean considerHighlighting) {
		g.setColor(this.getBorderColor(considerHighlighting));
		
		int textCenterPosX = this.getInsets().left + (left_space/2);
	    int textCenterPosY = this.getInsets().top + (this.a.getPreferredSize().height / 2);
		
		g.setFont(this.font);
		Utils.paintTextCentered(g, "FUTURE", textCenterPosX, textCenterPosY-7);
		
		g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
		        BasicStroke.JOIN_MITER, 10.0f, new float[] { 1f, 4f, 1f, 4f, 1f, 4f, 1f, 4f, 15.0f, 4f, 1f, 4f, 1f, 4f}, 0.0f));
		Utils.paintArrow(g, new Point(this.getInsets().left + 5, textCenterPosY + 7), left_space-10);
	
	}

}
