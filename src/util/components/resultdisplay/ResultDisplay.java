package util.components.resultdisplay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

public class ResultDisplay extends JComponent {
	private static final long serialVersionUID = 1L;
	
	private static ImageIcon progress_anim = new ImageIcon(ResultDisplay.class.getResource("progress.png"));
	private static ImageIcon success = new ImageIcon(ResultDisplay.class.getResource("success.png"));
	private static ImageIcon failure = new ImageIcon(ResultDisplay.class.getResource("fail.png"));
	private static ImageIcon incomplete = new ImageIcon(ResultDisplay.class.getResource("incomplete.png"));
	private static Font font = new Font("Arial", Font.PLAIN, 15);
	private static final Color col_failure = new Color(199, 14, 14);
	private static final Color col_success = new Color(22, 155, 26);
	private static final Color col_progress = new Color(180, 180, 180);
	private static final Color col_incomplete = new Color(241, 178, 34);
	
	private BufferedImage progress = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
	private String monitor = "";
	
	private int mode = 0;
	private boolean showText;
	
	public ResultDisplay() {
		this(true);
	}
	
	public ResultDisplay(boolean showText) {
		this.showText = showText;
		this.setBackground(Color.white);
		this.setPreferredSize(new Dimension(this.showText ? 100 : 32, 32));
		
		new Thread() {
			@Override
			public void run() {
				int img = 0;
				while (true) {
					synchronized (monitor) {
						progress = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
						progress.getGraphics().drawImage(progress_anim.getImage(), 0, 0, 32, 32, 32 * img, 0, 32 * (img + 1), 32, null);
					}
					ResultDisplay.this.repaint();
					img = (img + 1) % 4;
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {}
					
					synchronized (monitor) {
						if (mode!=1) {
							try {
								monitor.wait();
							} catch (InterruptedException e) {}
						}
					}
				}
			}
		}.start();
	}
	
	public void showProgress() {
		synchronized (monitor) {
			this.mode = 1;
			this.monitor.notifyAll();
		}
		this.repaint();
	}
	
	public void showSuccess() {
		synchronized (monitor) {
			this.mode = 2;
		}
		this.repaint();
	}
	
	public void showFailure() {
		synchronized (monitor) {
			this.mode = 3;
		}
		this.repaint();
	}

	public void showIncomplete() {
		synchronized (monitor) {
			this.mode = 4;
		}
		this.repaint();
	}
	
	public void showNil() {
		synchronized (monitor) {
			this.mode = 0;
		}
		this.repaint();
	}
	
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D gb = (Graphics2D)g;
		gb.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gb.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		Image img;
		String text = "";
		synchronized (monitor) {
			if (this.mode==0) return;
			if (this.mode==1) {
				img = this.progress;
				text = "validating...";
				g.setColor(col_progress);
			}
			else if (this.mode==2) {
				img = success.getImage();
				text = "valid";
				g.setColor(col_success);
			}
			else if (this.mode==3) {
				img = failure.getImage();
				text = "not valid";
				g.setColor(col_failure);
			}
			else {
				img = incomplete.getImage();
				text = "incomplete";
				g.setColor(col_incomplete);
			}
		}

		g.drawImage(img, 0, 0, this);
		if (this.showText) {
			g.setFont(font);
			g.drawString(text, 40, 20);
		}
	}
}
