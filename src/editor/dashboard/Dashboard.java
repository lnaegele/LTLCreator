package editor.dashboard;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import model.AbstractOperator;
import model.OperatorChangeListener;
import util.Utils;
import util.dnd.Draggable;
import util.dnd.DropTarget;
import util.layouts.CenterLayout;

public class Dashboard extends JComponent implements DropTarget, OperatorChangeListener {
	private static final long serialVersionUID = 1L;

	private AbstractOperator operator = null;
	private Dimension emptySize = new Dimension(200, 200);
	
	public Dashboard() {
		this.setLayout(new CenterLayout());
		this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
	}
	
	@Override
	public boolean accepts(Draggable object) {
		return (this.operator==null && object instanceof AbstractOperator);
	}
	
	@Override
	public void drop(Draggable object, Point point, DropTarget previousParent) {
		AbstractOperator operator = (AbstractOperator) object;
		this.operator = operator;
		this.add(operator);
		operator.addChangeListener(this);
		this.revalidate();
		this.repaint();
		this.notifyChangeListeners_operatorChanged();
	}
	
	@Override
	public void undock(Draggable draggable) {
		AbstractOperator operator = (AbstractOperator)draggable;
		operator.removeChangeListener(this);
		this.remove(operator);
		this.operator = null;
		this.revalidate();
		this.repaint();
		this.notifyChangeListeners_operatorChanged();
	}
	
	@Override
	public void setDraggingMouseOver(boolean hoverActive) {}
	
	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		if (this.operator==null) {
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			float dash[] = { 5.0f };
			g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
			        BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
			g.setColor(Color.lightGray);
			
			int distance = 20;
			int availWidth = this.getWidth()-1;
			int availHeight = this.getHeight()-1;
			
			int left = ((availWidth - this.emptySize.width) / 2) + distance;
			int top = ((availHeight - this.emptySize.height) / 2) + distance;
			
			g.drawRoundRect(left, top, this.emptySize.width - (2*distance), this.emptySize.height - (2*distance), 10, 10);
			Utils.paintTextCentered(g2, "drop here", availWidth/2, availHeight/2);
		}
	}
	
	@Override
	public Dimension getPreferredSize() {
		if (this.operator==null) return this.emptySize;
		return super.getPreferredSize();
	}
	
	public AbstractOperator getOperator() {
		return this.operator;
	}
	
	@Override
	public void operatorChanged(AbstractOperator operator) {
		notifyChangeListeners_operatorChanged();
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
	
	public void notifyChangeListeners_operatorChanged() {
		synchronized(operatorChangeListeners) {
			for (OperatorChangeListener l : operatorChangeListeners) {
				l.operatorChanged(this.operator);
			}
		}
	}

}
