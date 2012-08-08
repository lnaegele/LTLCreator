package model.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import model.AbstractOperator;
import model.NotCompleteException;
import util.Utils;

public class StateOperator extends AbstractOperator {
	private static final long serialVersionUID = 1L;
	
	private String id = null;
	private String label = null;
	private String ltl = null;
	private Font font = new Font("Arial", Font.PLAIN, 10);
	
	public StateOperator() {}
	
	public StateOperator(String id) {
		this.id = id;
	}
	
	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		if (id.equals(this.id)) return;
		this.id = id;
		this.label = null;
		
		this.repaint();
		super.notifyChangeListeners_operatorChanged();
	}
	
	public void setLTL(String ltl) {
		this.ltl = ltl;
		this.repaint();
	}
	
	@Override
	public String getLTL() throws NotCompleteException {
		if (this.ltl==null) throw new NotCompleteException();
		return this.ltl;
	}

	@Override
	public Color getColor() {
		return Color.white;
	}

	@Override
	public AbstractOperator createNewInstance() {
		return new StateOperator();
	}

	@Override
	public Dimension getOperatorSize() {
		return new Dimension(50, 50);
	}

	@Override
	protected void layoutBuckets() {}

	@Override
	protected int getCenterLineX() {
		return 25;
	}
	
	@Override
	public void paintOperator(Graphics2D g, boolean considerHighlighting) {
		if (this.ltl!=null) g.setColor(this.getBorderColor(considerHighlighting));
		else g.setColor(Color.red);
		
		if (this.id!=null) {
			g.setFont(this.font);
			Dimension ps = this.getPreferredSize();
			
			if (this.label==null) {
				this.setToolTipText(null);
				this.label = this.id;
				g.setFont(this.font);
				FontMetrics fm = g.getFontMetrics();
				int maxWidth = ps.width - 10;
				if (fm.stringWidth(this.id)>maxWidth) {
					while (fm.stringWidth(this.label+"...")>maxWidth) {
						this.label = this.label.substring(0, this.label.length()-1);
					}
					this.setToolTipText(this.id);
					this.label = label + "...";
				}
			}
			
			Utils.paintTextCentered(g, this.label, ps.width/2, ps.height/2);
		}
	}
	
	@Override
	public boolean isSimilar(AbstractOperator op) {
		if (!(op instanceof StateOperator)) return false;
		StateOperator stop = (StateOperator) op;
		return ((this.id==null & stop.id==null) || this.id!=null && this.id.equals(stop.id));
	}
}
