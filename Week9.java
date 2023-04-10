
//IAT455 - Final Project Code

//Based on kikikkkkz image-quilting-and-texture-transfer
//https://github.com/kikikkkkz/image-quilting-and-texture-transfer

//Also based on Alice Wang and Jordan Mecom's code for texture transfer
//http://jmecom.github.io/projects/computational-photography/texture-synthesis/


//**********************************************************/
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.lang.String;

import javax.imageio.ImageIO;

class Week9 extends Frame{  //controlling class
	
	
	BufferedImage texture;
	BufferedImage target;

	BufferedImage quilt;
	
	BufferedImage target_gray,texture_gray;
	//Original was 2
	//Liked how big the blocks were with 7
	int percent = 15;
	//Original was 4
	int overlapPercent = 4;
	//Alpha is tolerance between correlation and color, basically which one is favored more than the other
	//Max for Alpha is 1 and min is 0
	double alpha = 0.6;
	

	BufferedImage myOptImage, myOptImage1;

	public Week9() {
		// constructor
		// Get an image from the specified file in the current directory on the
		// local hard disk.
		try {
			
			//texture = ImageIO.read(new File("images/t12.png"));
			texture = ImageIO.read(new File("images/comp-8.jpg"));
			//target = ImageIO.read(new File("images/Cerone_White_44.png"));
			//target = ImageIO.read(new File("images/Cerone_White_100pix.png"));
			target = ImageIO.read(new File("images/Mattpog_Smol.png"));

		} catch (Exception e) {
			System.out.println("Cannot load the provided image");
		}
		this.setTitle("IAT 455 Final Project");
		this.setVisible(true);
		

		target_gray = rgb2gray(target);
		texture_gray = rgb2gray(texture);


		quilt = new BufferedImage(target.getWidth() , target.getWidth(), texture.getType());
		
		myOptImage = quilt2(texture, target, texture_gray, target_gray);
		

		// Anonymous inner-class listener to terminate program
		this.addWindowListener(new WindowAdapter() {// anonymous class definition
			public void windowClosing(WindowEvent e) {
				System.exit(0);// terminate the program
			}// end windowClosing()
		}// end WindowAdapter
		);// end addWindowListener
	}// end constructor

	

	//=======================================================//
	
	//Randomly finds small patch, from old code but doesn't need to be changed
	public BufferedImage smallPatch(BufferedImage src) {
		int w = src.getWidth() / percent;
		int h = src.getHeight() / percent;
		BufferedImage result = new BufferedImage(w, h, src.getType());

		int randomX = (int) (Math.random() * (src.getWidth() - w));
		int randomY = (int) (Math.random() * (src.getHeight() - h));
		for (int i = 0; i < result.getWidth(); i++) {
			for (int j = 0; j < result.getHeight(); j++) {
				result.setRGB(i, j, src.getRGB(randomX + i, randomY + j));
			}
		}
		return result;
	}
	
	
	//Modified quilt code to use new methods for quilt
	public BufferedImage quilt2(BufferedImage input, BufferedImage output, BufferedImage greyMapTex,BufferedImage greyMapTar ) {
		int outW = output.getWidth();
		int outH = output.getHeight();

		int inpW = input.getWidth();
		int inpH = input.getHeight();
		
		//Used for determining the block size width
		int smlW = inpW / percent;
		int smlH = inpH / percent;
		
		//Used for determining overlap size in the block size
		int overlapW = smlW / 4;
		int overlapH = smlH / 4;

		BufferedImage smlBlockA = smallPatch(output);
		BufferedImage result = new BufferedImage(outW, outH, input.getType());

		for (int j = 0; j < result.getHeight(); j += (smlH - overlapH)) {
			for (int i = 0; i < result.getWidth(); i += (smlW - overlapW)) {
				//First Patch selected is always random
				if (i == 0 && j == 0) {
					smlBlockA = smallPatch(input);
				}
				//Any other patch performs minimum error cut
				else {
					//minimumErrorBoundary1(BufferedImage output, BufferedImage blockB, BufferedImage greyMapTex, BufferedImage greyMapTar , int x, int y)
					//In this case blockB is the returnB1
					smlBlockA = minimumErrorBoundary1(result, returnB1(result, input, greyMapTex, greyMapTar , i, j), greyMapTex, greyMapTar , i, j);
				}
				//Overwriting RGB values
				for (int a = 0; a < smlW && i + a < result.getWidth(); a++) {
					for (int b = 0; b < smlH && j + b < result.getHeight(); b++) {
						result.setRGB(i + a, j + b, smlBlockA.getRGB(a, b));
					}
				}

			}
		}
		return result;
	}
	
	//Modified FindblockB code to account for cost of correspondence
	public double findBlockB1(BufferedImage output, BufferedImage blockB, BufferedImage greyMapTex, BufferedImage greyMapTar, int x, int y) {

		int widthA = blockB.getWidth();
		int heightA = blockB.getHeight();
		
		//How many pixels overlap with the block next to it
		int overlapW = widthA / 4;

		int startY = heightA - overlapW;
		
		//Color cost between texture image
		double colorCost = 0;
		//correspondence between source image cost
		double corCost = 0;
		double corTotalC= 0;
		double corTotalD = 0;
		BufferedImage monoBlockB = rgb2gray(blockB);

		if (y != 0) {
			for (int j = 0; j < overlapW && y + j < output.getHeight(); j++) {
				for (int i = 0; i < widthA && x + i < output.getWidth(); i++) {
					int pixelA = output.getRGB(x + i, y + j);
					int redA = getRed(pixelA);
					int blueA = getBlue(pixelA);
					int greenA = getGreen(pixelA);

					int pixelB = blockB.getRGB(i, j);
					int redB = getRed(pixelB);
					int blueB = getBlue(pixelB);
					int greenB = getGreen(pixelB);
					
					//int pixelC = greyMapTex.getRGB(i, j);
					int pixelC = monoBlockB.getRGB(i, j);
					int redC = getRed(pixelC);
					int blueC = getBlue(pixelC);
					int greenC = getGreen(pixelC);
					
					int pixelD = greyMapTar.getRGB(x + i,y + j);
					int redD = getRed(pixelD);
					int blueD = getBlue(pixelD);
					int greenD = getGreen(pixelD);
					
					float[] hsbA = new float[3];
					float[] hsbB = new float[3];
					float[] hsbC = new float[3];
					float[] hsbD = new float[3];
					Color.RGBtoHSB(redA, blueA, greenA, hsbA);
					Color.RGBtoHSB(redB, blueB, greenB, hsbB);
					Color.RGBtoHSB(redC, blueC, greenC, hsbC);
					Color.RGBtoHSB(redD, blueD, greenD, hsbD);
					
					
					
					//Was using HSB before but wasn't sure if it was working
					//double colorDifference = Math.pow((hsbA[2] - hsbB[2]), 2);
					//double corDifference = Math.pow((hsbC[2] - hsbD[2]), 2);
					
					//Color difference is how well it matches with adjacent pixels
					double colorDifference = Math.pow((redA - redB), 2) + Math.pow((blueA - blueB), 2) + Math.pow((greenA - greenB), 2);
					//double colorDifference = 0;
					//corDifference is how well it matches with the luminance of the target greymap with the texture greymap
					//double corDifference = Math.pow((redC - redD), 2) + Math.pow((blueC - blueD), 2) + Math.pow((greenC - greenD), 2);
					
					colorCost += colorDifference;
					//corCost += corDifference;
					corTotalC = corTotalC + redC;
					corTotalD = corTotalD + redD;
				}
			}
		}

		if (x != 0) {
			for (int j = 0; j < heightA && y + j < output.getHeight(); j++) {
				for (int i = 0; i < overlapW && x + i < output.getWidth(); i++) {
					int pixelA = output.getRGB(x + i, y + j);
					int redA = getRed(pixelA);
					int blueA = getBlue(pixelA);
					int greenA = getGreen(pixelA);

					int pixelB = blockB.getRGB(i, j);
					int redB = getRed(pixelB);
					int blueB = getBlue(pixelB);
					int greenB = getGreen(pixelB);
					
					//Texture Greymap
					//int pixelC = greyMapTex.getRGB(i, j);
					int pixelC = monoBlockB.getRGB(i, j);
					int redC = getRed(pixelC);
					int blueC = getBlue(pixelC);
					int greenC = getGreen(pixelC);
					
					//Target Greymap
					int pixelD = greyMapTar.getRGB(x + i, y + j);
					int redD = getRed(pixelD);
					int blueD = getBlue(pixelD);
					int greenD = getGreen(pixelD);


					
					float[] hsbA = new float[3];
					float[] hsbB = new float[3];
					float[] hsbC = new float[3];
					float[] hsbD = new float[3];
					Color.RGBtoHSB(redA, blueA, greenA, hsbA);
					Color.RGBtoHSB(redB, blueB, greenB, hsbB);				
					Color.RGBtoHSB(redC, blueC, greenC, hsbC);
					Color.RGBtoHSB(redD, blueD, greenD, hsbD);
					
					//Was using HSB before but wasn't sure if it was working
					//double colorDifference = Math.pow((hsbA[2] - hsbB[2]), 2);
					//double corDifference = Math.pow((hsbC[2] - hsbD[2]), 2);
					
					//Color difference is how well it matches with adjacent pixels
					double colorDifference = Math.pow((redA - redB), 2) + Math.pow((blueA - blueB), 2) + Math.pow((greenA - greenB), 2);
					//double colorDifference = 0;
					//corDifference is how well it matches with the luminance of the target greymap with the texture greymap
					//double corDifference = Math.pow((redC - redD), 2) + Math.pow((blueC - blueD), 2) + Math.pow((greenC - greenD), 2);

					corTotalC = corTotalC + redC;
					corTotalD = corTotalD + redD;

					colorCost += colorDifference;
					//corCost += corDifference;
				}
			}
		}
		corCost = corTotalC - corTotalD;
		corCost = Math.pow(corCost, 2);
		//System.out.println(corCost);
		//True cost is ripped from the Matlab code, combines the values of the corCost and colorcost
		double alphacon = 1-alpha;
		double trueCost = ((colorCost*alpha)+(corCost*alphacon));
		//System.out.println(trueCost);
		return trueCost;
	}	
	
	public BufferedImage returnB1(BufferedImage output, BufferedImage input, BufferedImage greyMapTex, BufferedImage greyMapTar, int x, int y) {

		BufferedImage blockB = smallPatch(input);
		BufferedImage finalBlockB = blockB;
		//int replaceCounter = 0;
		double originCost = findBlockB1(output, rgb2gray(blockB), greyMapTex, greyMapTar ,x, y);
		//Determines best block to use based on truecost from findBlockB1
		for (int t = 0; t < 200; t++) {
			blockB = smallPatch(input);
	
			double iterativeCost = findBlockB1(output, rgb2gray(blockB), greyMapTex, greyMapTar, x, y);	
			//System.out.println("Iterative cost is: "+ Double.toString(iterativeCost)+ " Origin cost is: "+ Double.toString(originCost));
			if (iterativeCost < originCost) {
				originCost = iterativeCost;
				finalBlockB = blockB;
				//replaceCounter++;
			}
		}
		//System.out.println("Replace counter went to: "+ Integer.toString(replaceCounter));
		
		return finalBlockB;
	}
	//Modified minimumErrorBoundary1 Code to perform boundary cuts, for sake of texture transfer should account for color relation to adjacent placed blocks
	//and should account for how well it corresponds to the luminance of the greyMapTar 
	public BufferedImage minimumErrorBoundary1(BufferedImage output, BufferedImage blockB, BufferedImage greyMapTex, BufferedImage greyMapTar , int x, int y) {

		BufferedImage cutB = new BufferedImage(blockB.getWidth(), blockB.getHeight(), blockB.getType());

		int widthA = blockB.getWidth();
		int heightA = blockB.getHeight();

		int overlapW = widthA / 4;

		int startingA = (widthA * 3) / 4;

		int a = 0;
		int locationX = 0;

		double[][] colorArray = new double[heightA][widthA];
		//System.out.println("HSB Code is running");
		if (y != 0) {
			for (int j = 0; j < overlapW && y + j < output.getHeight(); j++) {
				for (int i = 0; i < widthA && x + i < output.getWidth(); i++) {

											
					int pixelA = output.getRGB(x + i, y + j);
					int redA = getRed(pixelA);
					int blueA = getBlue(pixelA);
					int greenA = getGreen(pixelA);

					int pixelB = blockB.getRGB(i, j);
					int redB = getRed(pixelB);
					int blueB = getBlue(pixelB);
					int greenB = getGreen(pixelB);
					
					int pixelC = greyMapTex.getRGB(i, j);
					int redC = getRed(pixelC);
					int blueC = getBlue(pixelC);
					int greenC = getGreen(pixelC);	
					
					int pixelD = greyMapTar.getRGB(x+i, y+j);
					int redD = getRed(pixelD);
					int blueD = getBlue(pixelD);
					int greenD = getGreen(pixelD);	
					
					float[] hsbA = new float[3];
					float[] hsbB = new float[3];
					float[] hsbC = new float[3];
					float[] hsbD = new float[3];	
					Color.RGBtoHSB(redA, blueA, greenA, hsbA);
					Color.RGBtoHSB(redB, blueB, greenB, hsbB);
					Color.RGBtoHSB(redC, blueC, greenC, hsbC);
					Color.RGBtoHSB(redD, blueD, greenD, hsbD);
					double colorDifference = Math.pow((redA - redB), 2) + Math.pow((blueA - blueB), 2) + Math.pow((greenA - greenB), 2);
					//double colorDifference = 0;
					double corDifference = Math.pow((redC - redD), 2);
					//double corDifference = Math.pow((redC - redD), 2) + Math.pow((blueC - blueD), 2) + Math.pow((greenC - greenD), 2);

					//double colorDifference = Math.pow((hsbA[2] - hsbB[2]), 2);
					//double corDifference = Math.pow((hsbC[2] - hsbD[2]), 2);
					
					//colorArray[j][i] = colorDifference;
					//corArray[j][i] = corDifference;
					
					double trueDif = ((colorDifference*alpha)+(corDifference* (1-alpha)));
					colorArray[i][j] = trueDif;
					//System.out.println("color value difference is "+ colorDifference);
				}
			}
		}

		if (x != 0) {
			for (int j = 0; j < heightA && y + j < output.getHeight(); j++) {
				for (int i = 0; i < overlapW && x + i < output.getWidth(); i++) {

					int pixelA = output.getRGB(x + i, y + j);
					int redA = getRed(pixelA);
					int blueA = getBlue(pixelA);
					int greenA = getGreen(pixelA);

					int pixelB = blockB.getRGB(i, j);
					int redB = getRed(pixelB);
					int blueB = getBlue(pixelB);
					int greenB = getGreen(pixelB);
					
					int pixelC = greyMapTex.getRGB(i, j);
					int redC = getRed(pixelC);
					int blueC = getBlue(pixelC);
					int greenC = getGreen(pixelC);	
					
					int pixelD = greyMapTar.getRGB(x + i, y + j);
					int redD = getRed(pixelD);
					int blueD = getBlue(pixelD);
					int greenD = getGreen(pixelD);	
					
					float[] hsbA = new float[3];
					float[] hsbB = new float[3];
					float[] hsbC = new float[3];
					float[] hsbD = new float[3];	
					Color.RGBtoHSB(redA, blueA, greenA, hsbA);
					Color.RGBtoHSB(redB, blueB, greenB, hsbB);
					Color.RGBtoHSB(redC, blueC, greenC, hsbC);
					Color.RGBtoHSB(redD, blueD, greenD, hsbD);
					
					double colorDifference = Math.pow((redA - redB), 2) + Math.pow((blueA - blueB), 2) + Math.pow((greenA - greenB), 2);
					//double colorDifference = 0;
					//double corDifference = Math.pow((redC - redD), 2) + Math.pow((blueC - blueD), 2) + Math.pow((greenC - greenD), 2);
					double corDifference = Math.pow((redC - redD), 2);
					//double colorDifference = Math.pow((hsbA[2] - hsbB[2]), 2);
					//double corDifference = Math.pow((hsbC[2] - hsbD[2]), 2);
					
					//colorArray[j][i] = colorDifference;
					//corArray[j][i] = corDifference;
					double trueDif = ((colorDifference*alpha)+(corDifference* (1-alpha)));
					colorArray[i][j] = trueDif;
				}
			}
		}
//				System.out.print(colorArray.length+" , ");
//				System.out.println(colorArray[0].length);
//				System.out.println(Arrays.deepToString(colorArray));
		
		if (y != 0) {
			for (int s = 1; s < widthA; s++) {
				for (int t = 0; t < overlapW; t++) {
					if (t == 0) {
						colorArray[t][s] += Math.min(colorArray[t][s - 1], colorArray[t + 1][s - 1]);
						//corArray[t][s] += Math.min(corArray[t][s - 1], corArray[t + 1][s - 1]);
					} else if (t == overlapW - 1) {
						colorArray[t][s] += Math.min(colorArray[t][s - 1], colorArray[t - 1][s - 1]);
						//corArray[t][s] += Math.min(corArray[t][s - 1], corArray[t - 1][s - 1]);
					} else {
						colorArray[t][s] += Math.min(Math.min(colorArray[t][s - 1], colorArray[t + 1][s - 1]),colorArray[t - 1][s - 1]);
						//corArray[t][s] += Math.min(Math.min(corArray[t][s - 1], corArray[t + 1][s - 1]),corArray[t - 1][s - 1]);
					}

				}
			}
		}
//				System.out.println(Arrays.deepToString(colorArray));

		if (x != 0) {
			for (int t = 1; t < heightA; t++) {
				for (int s = 0; s < overlapW; s++) {

					if (s == 0) {
						colorArray[t][s] += Math.min(colorArray[t - 1][s], colorArray[t - 1][s + 1]);
						//corArray[t][s] += Math.min(corArray[t - 1][s], corArray[t - 1][s + 1]);
					} else if (s == overlapW - 1) {
						colorArray[t][s] += Math.min(colorArray[t - 1][s - 1], colorArray[t - 1][s]);
						//corArray[t][s] += Math.min(corArray[t - 1][s - 1], corArray[t - 1][s]);
					} else {
						colorArray[t][s] += Math.min(Math.min(colorArray[t - 1][s - 1], colorArray[t - 1][s]),colorArray[t - 1][s + 1]);
						//corArray[t][s] += Math.min(Math.min(corArray[t - 1][s - 1], corArray[t - 1][s]),corArray[t - 1][s + 1]);
					}

				}
			}
//					System.out.println(Arrays.deepToString(colorArray));

		}
		
		int[] locatY = new int[widthA];
		if (y != 0) {
			for (int k = widthA - 1; k > 0; k--) {
				double minimumCol = 0;
				//double minimumCor = 0;
				if (k == widthA - 1) {
					for (int u = 0; u < overlapW; u++) {
						if (u == 0 || ((colorArray[k][u] < minimumCol) ) ) {
							minimumCol = colorArray[k][u];
							//minimumCor = corArray[k][u];
							locatY[k] = u;
						}
					}
				}
				if (locatY[k] == 0) {
					double smalleastCol = 0;
					//double smalleastCor = 0;					
					for (int q = 0; q < 2; q++) {
						if ((smalleastCol == 0 ) || (colorArray[k - 1][q] < smalleastCol) ) {
							smalleastCol = colorArray[k - 1][q];
							//smalleastCor = corArray[k - 1][q];
							locatY[k - 1] = q;
						}
					}
				} else if (locatY[k] == overlapW - 1) {
					double minumCol = 0;
					//double minumCor = 0;
					for (int r = overlapW - 2; r < overlapW; r++) {
						if ((minumCol == 0 ) || (colorArray[k - 1][r] < minumCol )) {
							minumCol = colorArray[k - 1][r];
							//minumCor = corArray[k - 1][r];
							locatY[k - 1] = r;
						}
					}
				} else {
					double smallCol = 0;
					//double smallCor = 0;
					for (int d = locatY[k] - 1; d < locatY[k] + 2; d++) {
						if ((smallCol == 0 ) || (colorArray[k - 1][d] < smallCol )) {
							smallCol = colorArray[k - 1][d];
							//smallCor = corArray[k - 1][d];
							locatY[k - 1] = d;
						}
					}
				}
			}
		}
		
		int[] locatX = new int[heightA];
		if (x != 0) {
			for (int k = heightA - 1; k > 0; k--) {
				double minimumCol = 0;
				//double minimumCor = 0;
				if (k == heightA - 1) {
					for (int u = 0; u < overlapW; u++) {
						if (u == 0 || (colorArray[k][u] < minimumCol )) {
							minimumCol = colorArray[k][u];
							//minimumCor = corArray[k][u];
							locatX[k] = u;
						}
					}
				}

				if (locatX[k] == 0) {
					double smalleastCol = 0;
					//double smalleastCor = 0;
					for (int q = 0; q < 2; q++) {
						if ((smalleastCol == 0 ) || (colorArray[k - 1][q] < smalleastCol)) {
							smalleastCol = colorArray[k - 1][q];
							//smalleastCor = corArray[k - 1][q];
							locatX[k - 1] = q;
						}
					}
				} else if (locatX[k] == overlapW - 1) {
					double minumCol = 0;
					//double minumCor = 0;
					for (int r = overlapW - 2; r < overlapW; r++) {
						if ((minumCol == 0 ) || (colorArray[k - 1][r] < minumCol )) {
							minumCol = colorArray[k - 1][r];
							//minumCor = corArray[k - 1][r];
							locatX[k - 1] = r;
						}
					}
				} else {
					double smallCol = 0;
					//double smallCor = 0;
					for (int d = locatX[k] - 1; d < locatX[k] + 2; d++) {
						if ((smallCol == 0 ) || (colorArray[k - 1][d] < smallCol )) {
							smallCol = colorArray[k - 1][d];
							//smallCor = corArray[k - 1][d];
							locatX[k - 1] = d;
						}
					}
				}

			}
		}
		
		for (int o = 0; o < heightA; o++) {
			for (int b = 0; b < widthA; b++) {

				if (o < locatY[b] && y + o < output.getHeight() && x + b < output.getWidth()) {
					if (y != 0) {
						cutB.setRGB(b, o, output.getRGB(x + b, y + o));
					} else {
						cutB.setRGB(b, o, blockB.getRGB(b, o));
					}
				} else {
					if (x != 0) {
						if (b < locatX[o] && x + b < output.getWidth() && y + o < output.getHeight()) {
							cutB.setRGB(b, o, output.getRGB(x + b, y + o));
						} else {
							cutB.setRGB(b, o, blockB.getRGB(b, o));
						}
					} else {
						cutB.setRGB(b, o, blockB.getRGB(b, o));
					}
				}
			}
		}
		
		
		
		
		
		/*
		for (int o = 0; o < colorArray.length; o++) {	
			for (int b = 0; b < colorArray[o].length; b++) {
				System.out.println("Height is "+ Integer.toString(o) + " and width is "+ Integer.toString(b)+ " color value difference is "+ Double.toString(colorArray[o][b]));
			}
		}
		*/
		return cutB;
	}	
	

	public BufferedImage rgb2gray(BufferedImage bi) {
		int heightLimit = bi.getHeight();
		int widthLimit = bi.getWidth();
		BufferedImage converted = new BufferedImage(widthLimit, heightLimit, BufferedImage.TYPE_BYTE_GRAY);

		for (int height = 0; height < heightLimit; height++) {
			for (int width = 0; width < widthLimit; width++) {
				// Remove the alpha component
				Color c = new Color(bi.getRGB(width, height) & 0x00ffffff);
				// Normalize

				int newRed = (int) (0.2989 * c.getRed());
				int newGreen = (int) (0.5870 * c.getGreen());
				int newBlue = (int) (0.1140 * c.getBlue());
				int roOffset = newRed + newGreen + newBlue;
				converted.setRGB(width, height, new Color(roOffset, roOffset, roOffset).getRGB());
			}
		}
		return converted;
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
	
	public void paint(Graphics g) {


		this.setSize(1920, 1080);

		g.setColor(Color.BLACK);
		Font f1 = new Font("Helvetica", Font.PLAIN, 13);
		//Font f2 = new Font("Helvetica", Font.PLAIN, 6);
		g.setFont(f1);

		g.drawString("1.Texture image", 25, 45);
		

		g.drawString("2. Target image", 25, 50 * 2 + quilt.getHeight()+30 );
		g.drawString("2.a. Grayscale image", 25 * 2 + target.getWidth(), 50 * 2 + quilt.getHeight()+30);
		g.drawString("2.b. Texture transfer image", 25 * 3 + target.getWidth() * 2, 50 * 2 + quilt.getHeight()+30);

		
		g.drawImage(texture, 25, 50, texture.getWidth(), texture.getHeight(), this);

		g.drawImage(target, 25, 50 * 2 + quilt.getHeight()+40, target.getWidth(), target.getHeight(), this);


		//Reason for try and catch is beacause java tries to draw these before they are even finished being made, this prevents massive amounts of error messages
		try {

			g.drawImage(myOptImage, 25 * 2 + target.getWidth() * 2, 50 * 2 + quilt.getHeight()+40, myOptImage.getWidth(), myOptImage.getHeight(), this);

		} catch (Exception e) {
			System.out.println("Cannot draw myOptImage");
		}
		

	}
  public static void main(String[] args){
	
    Week9 img = new Week9();//instantiate this object
    img.repaint();//render the image
	
  }//end main
}
//=======================================================//