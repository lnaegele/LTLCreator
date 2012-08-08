package util.dnd;

import java.awt.Point;

public interface DropTarget {

	public boolean accepts(Draggable draggable);
	public void drop(Draggable draggable, Point point, DropTarget previousParent);
	public void undock(Draggable draggable);
	public void setDraggingMouseOver(boolean hoverActive);
	
}
