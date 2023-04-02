
//IAT455 - Workshop week 9

//**********************************************************/
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.image.Kernel;
import java.awt.image.ConvolveOp;
import java.io.File;
import java.lang.String; 

import javax.imageio.ImageIO;

class Week9 extends Frame{  //controlling class
	BufferedImage src1;  
	BufferedImage src1_bright;
	BufferedImage src1_brightGama; 
	
	BufferedImage statueImg;
	BufferedImage backgroundImg; 
	BufferedImage statueMatte; 
	BufferedImage edge_mask;
	BufferedImage jesse; 
	BufferedImage synth;
	
	BufferedImage blurred; 
	BufferedImage colorCorrected; 
	BufferedImage coloredEdges;
	BufferedImage shadedStatue;
	BufferedImage finalResult;
	BufferedImage tex_corr;  
	BufferedImage tar_corr;

	int width, width1; 
	int height, height1; 
	//Transfer variables
	int bsize = 20;
	double ovsize = Math.floor(bsize/6);
	double tolerance = 0.1;
	double alpha = 0.6;
	double si,ei,sj,ej;

	public Week9() {
		// constructor
		// Get an image from the specified file in the current directory on the
		// local hard disk.
		try {
			src1 = ImageIO.read(new File("images/backdoor.jpg")); 
			statueImg = ImageIO.read(new File("images/statue.jpg"));
			backgroundImg = ImageIO.read(new File("images/background.jpg")); 
			statueMatte = ImageIO.read(new File("images/statue_mat0.jpg")); 
			edge_mask = ImageIO.read(new File("images/edge_mask.jpg")); 
			jesse = ImageIO.read(new File("images/JesseS5.jpg")); 
			synth = ImageIO.read(new File("images/synth.jpg")); 

		} catch (Exception e) {
			System.out.println("Cannot load the provided image");
		}
		this.setTitle("IAT 455 Final Project");
		this.setVisible(true);

		width = src1.getWidth();
		height = src1.getHeight(); 
		
		width1 = statueImg.getWidth();
		height1 = statueImg.getHeight();
		
		src1_bright = increaseBrightness(src1, 5); 
		src1_brightGama = gammaIncreaseBrightness(src1, 0.65);
		
		BufferedImage background_copy = copyImg(backgroundImg); 
		//produce copy of image to work around Java exception - see: 
		//http://background-subtractor.googlecode.com/svn-history/r68/trunk/src/imageProcessing/ImageBlurrer.java
		//http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4957775		
		blurred = blur(background_copy);
		
		colorCorrected = colorCorrect(statueImg, blurred);
		
		coloredEdges = combineImages(colorCorrected,edge_mask,Operations.multiply); //TODO: replace the statueImg with proper method call
		
		BufferedImage edgelessStatue =  combineImages(invert(edge_mask),statueImg,Operations.multiply); //TODO: replace the statueImg with proper method call
		shadedStatue = combineImages(edgelessStatue, coloredEdges, Operations.add); //TODO: replace the statueImg with proper method call
		
		tar_corr = suppressToBlack(jesse);
		tex_corr = suppressToBlack(synth);
		finalResult = over(shadedStatue,statueMatte,backgroundImg); //TODO: replace the statueImg with proper method call

		//Anonymous inner-class listener to terminate program
		this.addWindowListener(
				new WindowAdapter(){//anonymous class definition
					public void windowClosing(WindowEvent e){
						System.exit(0);//terminate the program
					}//end windowClosing()
				}//end WindowAdapter
				);//end addWindowListener
	}// end constructor
	
	
	public BufferedImage increaseBrightness(BufferedImage src, int factor) {
		BufferedImage result = new BufferedImage(src.getWidth(),
				src.getHeight(), src.getType());


		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = src.getRGB(i, j);
				int r,g,b,newR,newG,newB;
				r = getRed(rgb);
				g = getGreen(rgb);
				b = getBlue(rgb);
				newR = clip(r*factor);
				newG = clip(g*factor);
				newB = clip(b*factor);
				int newRGB = new Color(newR,newG,newB).getRGB();
				result.setRGB(i, j, newRGB);
			}
		}
		
		
		return result;
	}
	
	public BufferedImage gammaIncreaseBrightness(BufferedImage src, double gamma) {
		BufferedImage result = new BufferedImage(src.getWidth(),
				src.getHeight(), src.getType());

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = src.getRGB(i, j);
				int r,g,b,newR,newG,newB;
				r = getRed(rgb);
				g = getGreen(rgb);
				b = getBlue(rgb);
				double power = 1.00/gamma;
				newR = clip((int) Math.pow(r, power));
				newG = clip((int) Math.pow(g, power));
				newB = clip((int) Math.pow(b, power));
				int newRGB = new Color(newR,newG,newB).getRGB();
				result.setRGB(i, j, newRGB);
			}
		}
		
		return result;
	}
	
	public BufferedImage blur(BufferedImage image) {
		
		float data[] = { 0.0625f, 0.125f, 0.0625f, 0.125f, 0.125f, 0.125f, 0.0625f, 0.125f, 0.0625f };
		int width = image.getWidth();
		int height = image.getHeight();
		Kernel kernel = new Kernel(3, 3, data);
		ConvolveOp convolve = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
		return convolve.filter(image, null);

	}
	
	public static BufferedImage copyImg(BufferedImage input) {
		BufferedImage tmp = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < input.getWidth(); x++) {
			for (int y = 0; y < input.getHeight(); y++) {
				tmp.setRGB(x, y, input.getRGB(x, y));
			}
		}
		return tmp;
	}
	
	public BufferedImage colorCorrect(BufferedImage src, BufferedImage bg) {
		BufferedImage result = new BufferedImage(src.getWidth(),
				src.getHeight(), src.getType());
			int width = src.getWidth();
			int height = src.getHeight();

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb1 = src.getRGB(i, j);
				int rgb2 = bg.getRGB(i, j);
				int r1,r2,g1,g2,b1,b2;
				float[] hsb1 = new float[3];
				float[] hsb2 = new float[3];				
				r1 = getRed(rgb1);
				r2 = getRed(rgb2);
				g1 = getGreen(rgb1);
				g2 = getGreen(rgb2);
				b1 = getBlue(rgb1);
				b2 = getBlue(rgb2);
				Color.RGBtoHSB(r1, g1, b1, hsb1);
				Color.RGBtoHSB(r2, g2, b2, hsb2);	
				
				int newRGB = Color.HSBtoRGB(hsb2[0], hsb2[2], hsb1[2]);
				
				result.setRGB(i, j, newRGB);
			}
		}
		

		return result;
	}
	

	

	
public BufferedImage combineImages(BufferedImage src1, BufferedImage src2, Operations op) {
	BufferedImage comp = new BufferedImage(src1.getWidth(), src1.getHeight(), src1.getType());

	int width = src1.getWidth();
	int height = src1.getHeight();

	WritableRaster wRaster = src1.copyData(null);

	int rgb1, rgb2, rgb3, r, g, b, a;
	// apply the operation to each pixel

	switch (op) {
	case add:
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				// grab all RGB values from the matte and two images
				rgb1 = src1.getRGB(i, j);
				rgb2 = src2.getRGB(i, j);

				// Add RGB values together
				r = getRed(rgb1) + getRed(rgb2);
				g = getGreen(rgb1) + getGreen(rgb2);
				b = getBlue(rgb1) + getBlue(rgb2);

				r = clip(r);
				g = clip(g);
				b = clip(b);
				int rgb = new Color(r, g, b).getRGB();
				comp.setRGB(i, j, rgb);
			}
		}
		return comp;

	case multiply:
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				rgb1 = src1.getRGB(i, j);
				rgb2 = src2.getRGB(i, j);
				// Multiply the all the RGBA values between the two images together and divide
				// by 255
				r = getRed(rgb1) * getRed(rgb2) / 255;
				g = getGreen(rgb1) * getGreen(rgb2) / 255;
				b = getBlue(rgb1) * getBlue(rgb2) / 255;
				r = clip(r);
				g = clip(g);
				b = clip(b);
				int rgb = new Color(r, g, b).getRGB();
				comp.setRGB(i, j, rgb);
			}
		}
		return comp;

	case subtract:
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				rgb1 = src1.getRGB(i, j);
				rgb2 = src2.getRGB(i, j);
				r = Math.abs(getRed(rgb1) - getRed(rgb2));
				g = Math.abs(getGreen(rgb1) - getGreen(rgb2));
				b = Math.abs(getBlue(rgb1) - getBlue(rgb2));
				r = clip(r);
				g = clip(g);
				b = clip(b);
				int rgb = new Color(r, g, b).getRGB();
				comp.setRGB(i, j, rgb);
			}
		}
		return comp;

	default:
		return comp;
	}

}

public BufferedImage suppressToBlack(BufferedImage src) {
	BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());

	int rgb, r, g, b, a, avg;
	for (int i = 0; i < src.getWidth(); i++) {
		for (int j = 0; j < src.getHeight(); j++) {
			
			rgb = src.getRGB(i,j);

	        a = (rgb>>24)&0xff;
	        r = (rgb>>16)&0xff;
	        g = (rgb>>8)&0xff;
	        b = rgb&0xff;

	        //calculate average
	        avg = (r+g+b)/3;

	        //replace RGB value with avg
	        rgb = (a<<24) | (avg<<16) | (avg<<8) | avg;

	        result.setRGB(i, j, rgb);
		}
	}

	return result;
}

public BufferedImage createInvertedMatte(BufferedImage src) {
	BufferedImage invertedMatte = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());

	// Write your code here
	int rgb, r, g, b;
	for (int i = 0; i < width; i++) {
		for (int j = 0; j < height; j++) {
			rgb = src.getRGB(i, j);
			r = getRed(rgb);
			g = getGreen(rgb);
			b = getBlue(rgb);

			int matte = b - Math.max(r, g);

			matte = clip(matte);

			rgb = new Color(matte, matte, matte).getRGB();
			invertedMatte.setRGB(i, j, rgb);
		}
	}
	return invertedMatte;
}

public BufferedImage invert(BufferedImage src) {
	BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
	
	int width = src.getWidth();
	int height = src.getHeight();
	
	for (int i = 0; i < width; i++) {
		for (int j = 0; j < height; j++) {
			int rgb = src.getRGB(i, j);
			int r,g,b,newR,newG,newB;
			r = getRed(rgb);
			g = getGreen(rgb);
			b = getBlue(rgb);
			newR = 255 - r;
			newG = 255 - g;
			newB = 255 - b;
			int newRGB = new Color(newR,newG,newB).getRGB();
			result.setRGB(i, j, newRGB);
			
		}
	}
	return result;

}

public BufferedImage over(BufferedImage foreground, BufferedImage matte, BufferedImage background) {
	BufferedImage AM, BM, neg, result;

	// Write your code here
	AM = combineImages(foreground, matte, Operations.multiply);
	BM = combineImages(background, matte, Operations.multiply);
	neg = combineImages(background, BM, Operations.subtract);
	result = combineImages(AM, neg, Operations.add);
	// NOTE: You should change the return statement below to the actual result
	return result;
}

public BufferedImage improveMatte(BufferedImage src) {
	BufferedImage matte = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
	int w = src.getWidth();
	int h = src.getHeight();

	int rgb, r, g, b;
	for (int i = 0; i < w; i++) {
		for (int j = 0; j < h; j++) {

			rgb = src.getRGB(i, j);
			r = getRed(rgb);
			g = getGreen(rgb);
			b = getBlue(rgb);

			float hsb[] = Color.RGBtoHSB(r, g, b, null);

			float newBrightness = hsb[2] > 0.05 ? 1 : hsb[2];

			int newRGB = Color.HSBtoRGB(hsb[0], 0f, newBrightness);

			matte.setRGB(i, j, newRGB);
		}
	}

	return matte;
}
	
	private int clip(int v) {
		v = v > 255 ? 255 : v;
		v = v < 0 ? 0 : v;
		return v;
	}

	protected int getRed(int pixel) {
		return (pixel >>> 16) & 0xFF;
	}

	protected int getGreen(int pixel) {
		return (pixel >>> 8) & 0xFF;
	}

	protected int getBlue(int pixel) {
		return pixel & 0xFF;
	}
	
	public void paint(Graphics g){
		int w = (int) (width/2.5f); //door image
		int h = (int) (height/2.5f);
		
		int w1 = width1/2; //statue
		int h1 = height1/2;
				
		this.setSize(w*4 +500,h*3+50);
		
		g.setColor(Color.BLACK);
	    Font f1 = new Font("Verdana", Font.PLAIN, 13);  
	    g.setFont(f1); 
		
		g.drawImage(src1,20,50,w, h,this);
	    g.drawImage(src1_bright, 50+w, 50, w, h,this);
	    g.drawImage(src1_brightGama, 80+w*2, 50, w, h,this);

	    g.drawImage(statueImg, 150+w*3, 50, w1, h1,this);
	    g.drawImage(backgroundImg, 150+w*3+w1+40, 50, w1, h1,this);
	    
	    g.drawImage(statueMatte,150+w*3, 50+h1+70,w1, h1,this);
	    g.drawImage(edge_mask, 150+w*3+w1+40, 50+h1+70, w1, h1,this);

	    g.drawImage(jesse,30,50+h+180,w1, h1,this);
	    g.drawString("Blurred background", 30, 50+h+170); 
	    
	    g.drawImage(colorCorrected,30+w1+30,50+h+180,w1, h1,this);
	    g.drawString("Color corrected", 30+w1+30, 50+h+170); 
	    
	    g.drawImage(coloredEdges,30+w1*2+60,50+h+180,w1, h1,this);
	    g.drawString("Colored Edges", 30+w1*2+60, 50+h+170);
	    
	    g.drawImage(shadedStatue,30+w1*3+90,50+h+180,w1, h1,this);
	    g.drawString("Shaded Statue", 30+w1*3+90, 50+h+170);
	    
	    g.drawImage(finalResult,30+w1*4+120,50+h+180,w1, h1,this);
	    g.drawString("Final Result", 30+w1*4+120, 50+h+170);
	    
	    g.drawString("Dark image", 20, 40); 
	    g.drawString("Increased brightness", 50+w, 40);
	    g.drawString("Increased brightness-Gamma", 80+w*2, 40);
	    
	    g.drawString("Statue Image", 150+w*3, 40); 
	    g.drawString("Background Image", 150+w*3+w1+40, 40);
	    
	    g.drawString("Statue - Matte", 150+w*3, 50+h1+60); 
	    g.drawString("Edge Matte", 150+w*3+w1+40, 50+h1+60);  
	    
	}
	//=======================================================//

  public static void main(String[] args){
	
    Week9 img = new Week9();//instantiate this object
    img.repaint();//render the image
	
  }//end main
}
//=======================================================//