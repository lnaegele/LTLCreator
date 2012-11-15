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

public class UntilOperator extends AbstractOperator {
	private static final long serialVersionUID = 1L;

	private Bucket a = new Bucket();
	private Bucket b = new Bucket();
	private int space_between_buckets = 50;
	private Font font = new Font("Arial", Font.BOLD, 15);
	
	public UntilOperator() {
		super.add(this.a);
		super.add(this.b);
	}
	
	public UntilOperator(AbstractOperator op1, AbstractOperator op2) {
		this();
		this.a.setOperator(op1);
		this.b.setOperator(op2);
	}
	
	@Override
	public AbstractOperator createNewInstance() {
		return new UntilOperator();
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
		sb.append(" U ");
		if (op2_brackets) sb.append("(");
		sb.append(op2.getLTL());
		if (op2_brackets) sb.append(")");
		
		return sb.toString();
	}
	
	private static boolean needBrackets(AbstractOperator op) {
		if (op==null) return false;
		return !(op instanceof StateOperator);
	}
	
	@Override
	public Color getColor() {
		return Color.cyan;
	}
	
	public void setOperator(AbstractOperator operator) {
		this.a.setOperator(operator);
	}
	
	@Override
	public Dimension getOperatorSize() {
		Insets insets = this.getInsets();
		
		Dimension ps1 = this.a.getPreferredSize();
		Dimension ps2 = this.b.getPreferredSize();
		
		return new Dimension(insets.left + ps1.width + space_between_buckets + ps2.width + insets.right, insets.top + Math.max(ps1.height, ps2.height) + insets.bottom);
	}

	@Override
	protected void layoutBuckets() {
		Insets insets = this.getInsets();
		
		Dimension ps1 = this.a.getPreferredSize();
		Dimension ps2 = this.b.getPreferredSize();
		
		int max_height = Math.max(ps1.height, ps2.height);
		
		int top1 = (max_height - ps1.height) / 2;
		int top2 = (max_height - ps2.height) / 2;
		
		this.a.setBounds(insets.left, insets.top + top1, ps1.width, ps1.height);
		this.b.setBounds(insets.left + ps1.width + space_between_buckets, insets.top + top2, ps2.width, ps2.height);
	}

	@Override
	protected int getCenterLineX() {
		return this.getInsets().left + this.a.getCenterLineX();
	}
	
	@Override
	public void paintOperator(Graphics2D g, boolean considerHighlighting) {
		g.setColor(this.getBorderColor(considerHighlighting));
		
		Dimension ps = this.a.getPreferredSize();
		
		int textCenterPosX = this.getInsets().left + ps.width + (space_between_buckets/2);
	    int textCenterPosY = this.getHeight() / 2;
		
		g.setFont(this.font);
		Utils.paintTextCentered(g, "UNTIL", textCenterPosX, textCenterPosY - 7);
		
		g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
		        BasicStroke.JOIN_MITER, 10.0f, new float[] { 15.0f, 4f }, 0.0f));
		Utils.paintArrow(g, new Point(this.getInsets().left + ps.width + 5, textCenterPosY + 7), space_between_buckets-10);
	
	}

}
