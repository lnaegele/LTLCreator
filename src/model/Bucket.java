package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import util.Utils;
import util.dnd.Draggable;
import util.dnd.DropTarget;


public class Bucket extends JComponent implements LayoutManager, DropTarget, OperatorChangeListener {
	private static final long serialVersionUID = 1L;
	
	private AbstractOperator operator = null;
	private Dimension standardSize =  new Dimension(50, 50);
	private boolean draggingActive = false;
	
	public Bucket() {
		this.setLayout(this);
	}
	
	public void clear() {
		this.setOperator(null);
	}
	
	public synchronized void setOperator(AbstractOperator operator) {
		if (this.operator==operator) return;
		if (this.operator!=null) {
			this.operator.removeChangeListener(this);
			this.remove(this.operator);
		}
		this.operator = operator;
		if (operator!=null) {
			operator.addChangeListener(this);
			this.add(operator);
		}
		this.revalidate();
		this.repaint();
		notifyChangeListeners_operatorChanged(operator);
	}
	
	public AbstractOperator getOperator() {
		return this.operator;
	}
	
	@Override
	public void setDraggingMouseOver(boolean draggingActive) {
		if (this.draggingActive==draggingActive) return;
		this.draggingActive = draggingActive;
		this.repaint();
	}
	
	protected void setHoveredOperator(AbstractOperator operator) {
		if (getOperator()!=null) getOperator().setHoveredOperator(operator);
	}
	
	@Override
	public void undock(Draggable draggable) {
		if (this.getOperator()!=draggable) return;
		
		this.setOperator(null);
		Container parent = this.getParent();
		if (!(parent instanceof AbstractOperator)) return;
		((AbstractOperator)parent).setMouseOver(false);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintSelf(g, true, false, false);
	}
	
	protected void paintSelf(Graphics g, boolean considerHighlighting, boolean recursive, boolean doLayout) {
		if (doLayout) doLayout();
		
		Graphics2D g2 = (Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		if (this.operator==null) {
			g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
			        BasicStroke.JOIN_MITER, 10.0f, new float[] { 5.0f }, 0.0f));
			
			int distance = 5;
			if (!this.draggingActive || !considerHighlighting) g2.setColor(Color.white);
			else g2.setColor(Color.yellow);
				
			int availWidth = this.getWidth()-1;
			int availHeight = this.getHeight()-1;
				
			g2.drawRoundRect(distance, distance, availWidth-(1+2*distance), availHeight-(1+2*distance), 10, 10);
			Utils.paintTextCentered(g2, "drop", availHeight/2, availHeight/2);
		}
		else if (recursive) {
			this.operator.paintOperator(g2, considerHighlighting, recursive, doLayout);
		}
	}
	
	public int getCenterLineX() {
		if (this.operator==null) return (this.standardSize.width / 2);
		else return this.operator.getCenterLineX();
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {}

	@Override
	public void removeLayoutComponent(Component comp) {}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		if (this.operator==null) return this.standardSize;
		else return this.operator.getPreferredSize();
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return this.getPreferredSize();
	}

	@Override
	public void layoutContainer(Container parent) {
		if (this.operator==null) return;
		Dimension ps = this.operator.getPreferredSize();
		this.operator.setBounds(0, 0, ps.width, ps.height);
	}

	@Override
	public boolean accepts(Draggable object) {
		if (this.getOperator()!=null) return false;
		if (!(object instanceof AbstractOperator)) return false;
		AbstractOperator draggedOperator = (AbstractOperator) object;
		
		if (isSubBucket(this, draggedOperator)) return false;
		return true;
	}
	
	private boolean isSubBucket(Bucket mayBeChild, AbstractOperator mayBeParent) {
		Container parent = mayBeChild.getParent();
		while (parent!=null) {
			if (parent==mayBeParent) return true;
			parent = parent.getParent();
		}
		return false;
	}

	@Override
	public void drop(Draggable object, Point point, DropTarget previousParent) {
		setOperator((AbstractOperator)object);
	}
	
	@Override
	public void operatorChanged(AbstractOperator operator) {
		notifyChangeListeners_operatorChanged(operator);
	}
	
	private List<OperatorChangeListener> operatorChangeListeners = new ArrayList<OperatorChangeListener>();
	public void addChangeListener(OperatorChangeListener l) {
		synchronized(this.operatorChangeListeners) {
			this.operatorChangeListeners.add(l);
		}
	}
	public void removeChangeListener(OperatorChangeListener l) {
		synchronized(this.operatorChangeListeners) {
			this.operatorChangeListeners.remove(l);
		}
	}
	private void notifyChangeListeners_operatorChanged(AbstractOperator operator) {
		synchronized(this.operatorChangeListeners) {
			for (OperatorChangeListener l : this.operatorChangeListeners) {
				l.operatorChanged(operator);
			}
		}
	}

}
