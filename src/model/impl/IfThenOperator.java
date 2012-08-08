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

public class IfThenOperator extends AbstractOperator {
	private static final long serialVersionUID = 1L;

	private Bucket a = new Bucket(), b = new Bucket();
	private int vertical_space_between_buckets = 25;
	private Font font = new Font("Arial", Font.BOLD, 15);
	
	public IfThenOperator() {
		super.add(this.a);
		super.add(this.b);
	}
	
	public IfThenOperator(AbstractOperator op1, AbstractOperator op2) {
		this();
		this.a.setOperator(op1);
		this.b.setOperator(op2);
	}
	
	@Override
	public AbstractOperator createNewInstance() {
		return new IfThenOperator();
	}
	
	@Override
	public String getLTL() throws NotCompleteException {
		AbstractOperator op1 = this.a.getOperator();
		AbstractOperator op2 = this.b.getOperator();
		if (op1==null || op2==null) throw new NotCompleteException();
		
		boolean op1_brackets = needBrackets(op1);
		boolean op2_brackets = needBrackets(op2);
		
		StringBuffer sb = new StringBuffer();
		if (op1_brackets) sb.append("(");
		sb.append(op1.getLTL());
		if (op1_brackets) sb.append(")");
		sb.append(" -> ");
		if (op2_brackets) sb.append("(");
		sb.append(op2.getLTL());
		if (op2_brackets) sb.append(")");
		
		return sb.toString();
	}
	
	private static boolean needBrackets(AbstractOperator op) {
		if (op==null) return false;
		return !((op instanceof StateOperator) || op instanceof NotOperator);
	}
	
	@Override
	public Color getColor() {
		return Color.green;
	}
	
	public void setFirstOperator(AbstractOperator operator) {
		this.a.setOperator(operator);
	}
	
	public void setSecondOperator(AbstractOperator operator) {
		this.b.setOperator(operator);
	}
	
	@Override
	public Dimension getOperatorSize() {
		Insets insets = super.getInsets();

		int a_cl = this.a.getCenterLineX();
		int b_cl = this.b.getCenterLineX();
		Dimension a_ps = this.a.getPreferredSize();
		Dimension b_ps = this.b.getPreferredSize();
		
		// Abstand linker Rand zu Centerlinie
		int left = Math.max(a_cl, b_cl) + insets.left;
		
		// Abstand rechter Rand zu Centerlinie
		int right = Math.max(a_ps.width-a_cl, b_ps.width-b_cl) + insets.right;
		
		return new Dimension(left+right, insets.top + vertical_space_between_buckets + a_ps.height + vertical_space_between_buckets + b_ps.height + insets.bottom);
	}

	@Override
	protected void layoutBuckets() {
		Insets insets = super.getInsets();

		int a_cl = this.a.getCenterLineX();
		int b_cl = this.b.getCenterLineX();
		Dimension a_ps = this.a.getPreferredSize();
		Dimension b_ps = this.b.getPreferredSize();
		
		// Abstand linker Rand zu Centerlinie
		int centerLineX = Math.max(a_cl, b_cl) + insets.left;
		
		this.a.setBounds(centerLineX - a_cl, insets.top + vertical_space_between_buckets, a_ps.width, a_ps.height);
		this.b.setBounds(centerLineX - b_cl, insets.top + vertical_space_between_buckets + a_ps.height + vertical_space_between_buckets, b_ps.width, b_ps.height);
	}

	@Override
	protected int getCenterLineX() {
		int a_cl = this.a.getCenterLineX();
		int b_cl = this.b.getCenterLineX();
		
		// Abstand linker Rand zu Centerlinie
		return Math.max(a_cl, b_cl) + this.getInsets().left;
	}
	
	@Override
	public void paintOperator(Graphics2D g, boolean considerHighlighting) {
		g.setColor(this.getBorderColor(considerHighlighting));
		
		int textCenterPosY1 = this.getInsets().top + (vertical_space_between_buckets / 2);
		int textCenterPosY2 = this.getInsets().top + vertical_space_between_buckets + this.a.getPreferredSize().height + (vertical_space_between_buckets / 2);
		g.setFont(this.font);
		
		Utils.paintTextCentered(g, "IF", this.getCenterLineX(), textCenterPosY1);
		Utils.paintTextCentered(g, "THEN", this.getCenterLineX(), textCenterPosY2);
	}

}
