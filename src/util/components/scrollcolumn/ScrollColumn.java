package util.components.scrollcolumn;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class ScrollColumn extends JComponent implements LayoutManager, AWTEventListener {
	private static final long serialVersionUID = 1L;

	private ImageIcon arrow_up = new ImageIcon(getClass().getResource("up.png"));
	private ImageIcon arrow_down = new ImageIcon(getClass().getResource("down.png"));
	
	private Component component;
	private double scrollPosition = 0;	
	private Ticker ticker = new Ticker();
	private int maxScrollHeight = 50;
	private int actualScrollHeight;
	private boolean mouseOver = false;
	
	public ScrollColumn(Component component) {
		this.component = component;
		this.setLayout(this);
		this.add(component);
		this.addMouseListener(new MouseAdapter() {});
		this.addMouseMotionListener(new MouseMotionAdapter() {});
		Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_MOTION_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK + AWTEvent.MOUSE_WHEEL_EVENT_MASK);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		if (!this.mouseOver) return;
		if (this.scrollPosition<0) {
			Rectangle up = getUpArea();
			g.drawImage(this.arrow_up.getImage(), (up.width-this.arrow_up.getIconWidth())/2, (up.height-this.arrow_up.getIconHeight())/2, this);
		}
		if (this.scrollPosition>this.getHeight()-this.component.getPreferredSize().height) {
			Rectangle down = getDownArea();
			g.drawImage(this.arrow_down.getImage(), (down.width-this.arrow_down.getIconWidth())/2, down.y+(down.height-this.arrow_down.getIconHeight())/2, this);
		}
	}
	
	
	@Override
	public void addLayoutComponent(String name, Component comp) {}

	@Override
	public void removeLayoutComponent(Component comp) {}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return this.component.getPreferredSize();
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return this.preferredLayoutSize(parent);
	}

	@Override
	public void layoutContainer(Container parent) {
		Dimension ps = this.component.getPreferredSize();
		
		int height = parent.getHeight();
		if (height>=ps.height) {
			this.scrollPosition = 0;
		}
		
		this.scrollPosition = Math.min(0, Math.max(this.scrollPosition, this.getHeight()-this.component.getPreferredSize().height));
		this.component.setBounds(0, (int)this.scrollPosition, parent.getWidth(), Math.max(ps.height, this.getHeight()));
		
		this.actualScrollHeight = Math.min(this.maxScrollHeight, this.getHeight()/2);
	}
	
	private Rectangle getUpArea() {
		return new Rectangle(0, 0, this.getWidth(), this.actualScrollHeight);
	}
	
	private Rectangle getDownArea() {
		return new Rectangle(0, this.getHeight()-this.actualScrollHeight, this.getWidth(), this.actualScrollHeight);
	}
	
	private void changeScrollPosition(double offset) {
		this.scrollPosition = Math.min(0, Math.max(this.scrollPosition+offset, this.getHeight()-this.component.getPreferredSize().height));
		this.revalidate();
		this.repaint();
	}
	
	private class Ticker {
		
		private Thread t;
		private int speed = 0;
		private int ticksPerSecond = 35;
		
		public Ticker() {
			t = new Thread(new Runnable() {
				@Override
				public void run() {
					while(true) {
						synchronized(t) {
							if (speed==0) {
								try {
									t.wait();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							else {
								changeScrollPosition(speed/(double)ticksPerSecond);
							}
						}
						try {
							Thread.sleep(1000/ticksPerSecond);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
			this.t.start();
		}
		
		/**
		 * Pixel per second
		 * @param speed
		 */
		private void setSpeed(int speed) {
			synchronized (this.t) {
				this.speed = speed;
				this.t.notify();
			}
		}
	}

	@Override
	public void eventDispatched(AWTEvent event) {
		if (!(event instanceof MouseEvent)) return;
		
		MouseEvent me = (MouseEvent) event;
		if (!SwingUtilities.isDescendingFrom(me.getComponent(), this)) return;

		Point p = SwingUtilities.convertPoint(me.getComponent(), me.getPoint(), this); 
		
		switch (me.getID()) {
		case MouseEvent.MOUSE_WHEEL:
			changeScrollPosition(-10*((MouseWheelEvent)me).getUnitsToScroll());
			me.consume();
			break;
		case MouseEvent.MOUSE_CLICKED:
			break;
		case MouseEvent.MOUSE_ENTERED:
			this.mouseOver = true;
			this.repaint();
			break;
		case MouseEvent.MOUSE_EXITED:
			this.ticker.setSpeed(0);
			this.mouseOver = false;
			this.repaint();
			break;
		case MouseEvent.MOUSE_MOVED:
			Rectangle up = getUpArea();
			Rectangle down = getDownArea();
			if (up.contains(p)) this.ticker.setSpeed((up.height - (p.y - up.y))*8);
			else if (down.contains(p)) this.ticker.setSpeed(-(p.y - down.y)*8);
			else this.ticker.setSpeed(0);
			break;
		case MouseEvent.MOUSE_PRESSED:
			break;
		case MouseEvent.MOUSE_RELEASED:
			break;
		case MouseEvent.MOUSE_DRAGGED:
			break;
		}
	}
	
}
