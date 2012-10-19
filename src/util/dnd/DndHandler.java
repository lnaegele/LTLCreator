package util.dnd;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class DndHandler implements AWTEventListener {

	private static DndHandler instance = null;
	
	public static synchronized DndHandler getInstance() {
		if (instance==null) instance = new DndHandler();
		return instance;
	}
	
	private boolean dragActive = false;
	private Draggable mouseOverOperator = null;
	private Draggable draggedOperator = null;
	private DropTarget activeTargetBucket = null;
	private Point mousePositionRelativeToDraggable = null;
	
	private final Cursor defaultCursor = Cursor.getDefaultCursor();
	private final Cursor dragCursorRestricted = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(getClass().getResource("cursor_drag_hand.png")).getImage(), new Point(6, 4), "");
	private final Cursor dragCursorAccepted = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(getClass().getResource("cursor_drag_hand2.png")).getImage(), new Point(6, 4), "");
	
	private Container parent = null;
	
	private DndHandler() {
	}
	
	public synchronized void registerTopLevelComponent(JComponent parent) {
		if (this.parent!=null) throw new RuntimeException("More than one top level component is not yet supported.");
		this.parent = parent;
		Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_MOTION_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK);
	}
	
	private boolean startDragging(Point point) {
		Draggable draggable = getDraggableAt(point, this.parent);
		if (draggable==null) draggable = getDraggableProvidedAt(point, this.parent);
		if (draggable==null) return false;
		
		startDragging(draggable, SwingUtilities.convertPoint(this.parent, point, (Component)draggable));
		return true;		
	}
	
	private void startDragging(Draggable draggable, Point mousePositionRelativeToDraggable) {
		if (draggable==null) throw new RuntimeException("Parameter can not be null.");
		draggedOperator = draggable;
		this.mousePositionRelativeToDraggable = mousePositionRelativeToDraggable;
		dragActive = true;
		parent.setCursor(this.dragCursorRestricted);
	}
	
	private boolean dragging(Point point) {
		if (!dragActive) return false;
		
		DropTarget bucket = getDropTargetAt(point, draggedOperator, this.parent);
		
		if (activeTargetBucket!=null) activeTargetBucket.setDraggingMouseOver(false);
	
		// Innerhalb des gleichen Elternelements
		if (bucket!=null && bucket==draggedOperator.getCurrentParent()) {
			parent.setCursor(this.dragCursorRestricted);
		}
		else if (bucket!=null) {
			activeTargetBucket = bucket;
			activeTargetBucket.setDraggingMouseOver(true);
			parent.setCursor(this.dragCursorAccepted);
		}
		else {
			activeTargetBucket = null;
			parent.setCursor(this.dragCursorRestricted);
		}
		return true;
	}
	
	private boolean stopDragging(final Point point) {
		if (!dragActive) return false;
		
		dragging(point);
		parent.setCursor(this.defaultCursor);
		
		if (activeTargetBucket!=null) {
			activeTargetBucket.setDraggingMouseOver(false);
			DropTarget parentDropTarget = draggedOperator.getCurrentParent();
			if (parentDropTarget!=null) parentDropTarget.undock(draggedOperator);
			Point pointScreen = SwingUtilities.convertPoint(this.parent, point, (Component)activeTargetBucket);
			pointScreen.translate(-mousePositionRelativeToDraggable.x, -mousePositionRelativeToDraggable.y);
			activeTargetBucket.drop(draggedOperator, pointScreen, parentDropTarget);
		}
		
		dragActive = false;
		draggedOperator = null;
		activeTargetBucket = null;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setMouseOver(point);
			}
		});
		return true;
	}
	
	private void setMouseOver(Point point) {
		if (point==null) setMouseOverOperator(null);
		else setMouseOverOperator(getDraggableAt(point, this.parent));
	}
	
	private void setMouseOverOperator(Draggable operator) {
		if (this.dragActive) return;

		if (this.mouseOverOperator!=null) this.mouseOverOperator.setMouseOver(false);
		this.mouseOverOperator = operator;
		if (this.mouseOverOperator!=null) this.mouseOverOperator.setMouseOver(true);
		
	}	
	
	private static Draggable getDraggableAt(Point point, Container parent) {
		Component comp = getComponentAt(point, parent);
		if (comp instanceof Container) {
			Rectangle b = comp.getBounds();
			Draggable result = getDraggableAt(new Point(point.x-b.x, point.y-b.y), (Container)comp);
			if (result!=null) return result;
		}
		if (comp instanceof Draggable) return (Draggable)comp;
		return null;
	}
	
	private static Draggable getDraggableProvidedAt(Point point, Container parent) {
		Component comp = getComponentAt(point, parent);
		if (comp instanceof Container) {
			Rectangle b = comp.getBounds();
			Draggable result = getDraggableProvidedAt(new Point(point.x-b.x, point.y-b.y), (Container)comp);
			if (result!=null) return result;
		}
		if (comp instanceof DraggableProvider) return ((DraggableProvider)comp).getDraggable();
		return null;
	}
	
	private static DropTarget getDropTargetAt(Point point, Draggable draggedOperator, Container parent) {
		Component comp = getComponentAt(point, parent);
		if (comp instanceof Container) {
			Rectangle b = comp.getBounds();
			DropTarget result = getDropTargetAt(new Point(point.x-b.x, point.y-b.y), draggedOperator, (Container)comp);
			if (result!=null) return result;
		}
		if (comp instanceof DropTarget && ((DropTarget)comp).accepts(draggedOperator)) return (DropTarget)comp;
		return null;
	}
	
	private static Component getComponentAt(Point point, Container parent) {
		// TODO: Wenn mehrere Componenten übereinanderliegen, dann das mit höhrerem z order!
		for (Component c : parent.getComponents()) {
			Rectangle b = c.getBounds();
			if (c.isShowing() && c.contains(new Point(point.x-b.x, point.y-b.y))) {
				return c;
			}
		}
		return null;
	}
	
	@Override
	public void eventDispatched(AWTEvent event) {
		if (!(event instanceof MouseEvent)) return;
		
		MouseEvent me = (MouseEvent) event;
		if (!SwingUtilities.isDescendingFrom(me.getComponent(), this.parent)) return;
		
		Point pointRelToParent = SwingUtilities.convertPoint(me.getComponent(), me.getPoint(), this.parent);
		Draggable draggable;
		switch (me.getID()) {
		case MouseEvent.MOUSE_CLICKED:
			draggable = getDraggableAt(pointRelToParent, this.parent);
			if (draggable!=null && ((Component)draggable).isShowing()) {
				draggable.mouseClick(SwingUtilities.convertPoint(this.parent, pointRelToParent, (Component)draggable));
				me.consume();
			}
			break;
		case MouseEvent.MOUSE_ENTERED:
			setMouseOver(pointRelToParent);
			break;
		case MouseEvent.MOUSE_EXITED:
			setMouseOver(null);
			break;
		case MouseEvent.MOUSE_MOVED:
			setMouseOver(pointRelToParent);
			break;
		case MouseEvent.MOUSE_PRESSED:
			if (me.getButton()==MouseEvent.BUTTON1) {
				if (startDragging(pointRelToParent)) me.consume();
			}
			break;
		case MouseEvent.MOUSE_RELEASED:
			if (me.getButton()==MouseEvent.BUTTON1) {
				if (stopDragging(pointRelToParent)) me.consume();
			}
			break;
		case MouseEvent.MOUSE_DRAGGED:
			if (dragging(pointRelToParent)) me.consume();
			break;
		}
	}

}