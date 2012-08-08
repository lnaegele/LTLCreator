package editor.tabpane;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import model.AbstractOperator;
import util.components.graphtab.TitleComponent;
import util.components.resultdisplay.ResultDisplay;

public class OperatorTabTitleComponent extends TitleComponent {
	private static final long serialVersionUID = 1L;

	private int maxWidth = 80;
	private int maxHeight = 80;
	private Image image = null;
	private Dimension imgSize = new Dimension();
	private ResultDisplay disp = new ResultDisplay(false);
	private String monitor = "";
	private ExecutorService executorService;
	
	public OperatorTabTitleComponent() {
		this.setLayout(null);
		this.setPreferredSize(new Dimension(115, 100));

		this.add(disp);
		Dimension ps = disp.getPreferredSize();
		disp.setBounds(115-ps.width, 100-ps.height, ps.width, ps.height);
		
		executorService = Executors.newSingleThreadExecutor();
	}

	@Override
	public void setMouseOver(boolean mouseOver) {}

	@Override
	public void setActive(boolean active) {}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		synchronized (monitor) {
			if (this.image!=null) {
				g.drawImage(this.image, (3+this.maxWidth-this.imgSize.width)/2, (this.getHeight()-imgSize.height)/2, this);
			}
		}
	}
	
	public void setDisplayedOperator(final AbstractOperator operator) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				final int imgWidth;
				final int imgHeight;
				final Image img;
				
				if (operator==null) {
					imgWidth = 0;
					imgHeight = 0;
					img = null;
				}
				else {
					Dimension ps = operator.getPreferredSize();
					BufferedImage myImage = new BufferedImage(ps.width, ps.height, BufferedImage.TYPE_INT_ARGB);
					operator.paintOperator(myImage.createGraphics(), false, true, true);
							
					double factor = Math.min(maxWidth/(double)ps.width, maxHeight/(double)ps.height);
					imgWidth = Math.min((int)(ps.width * factor), ps.width/2);
					imgHeight =  Math.min((int)(ps.height * factor), ps.height/2);
					img = myImage.getScaledInstance(Math.max(1, imgWidth), Math.max(1, imgHeight), Image.SCALE_SMOOTH);
				}
				
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						synchronized (monitor) {
							imgSize.width = imgWidth;
							imgSize.height = imgHeight;
							image = img;
						}
						
						repaint();
					}
				});
			}
		});
	}
	
	public void showProgress() {
		this.disp.showProgress();
	}
	
	public void showSuccess() {
		this.disp.showSuccess();
	}
	
	public void showFailure() {
		this.disp.showFailure();
	}

	public void showIncomplete() {
		this.disp.showIncomplete();
	}
	
	public void showNil() {
		this.disp.showNil();
	}
}
