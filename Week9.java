
//IAT455 - Workshop week 9

//**********************************************************/
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.lang.String;
import java.util.Arrays;

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

	double si,ei,sj,ej;
	
	BufferedImage carImage; // reference to an Image object

	BufferedImage texture;
	BufferedImage target;


	BufferedImage quilt;
	BufferedImage quilt1, quilt2, quilt3;
	BufferedImage target_gray,texture_gray;
	//Original was 2
	int percent = 3;
	//Original was 4
	int overlapPercent = 4;
	
	double alpha = 0.6;

	BufferedImage myOptImage, myOptImage1;

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
			
			/*
			carImage = ImageIO.read(new File("images/JesseS5.jpg"));

			texture = ImageIO.read(new File("images/synth.jpg"));
			target = ImageIO.read(new File("images/JesseS5.jpg"));
			
			*/
			carImage = ImageIO.read(new File("images/JesseS5.jpg"));

			texture = ImageIO.read(new File("images/t13.png"));
			target = ImageIO.read(new File("images/Cerone_White_44.png"));


		} catch (Exception e) {
			System.out.println("Cannot load the provided image");
		}
		this.setTitle("IAT 455 Final Project");
		this.setVisible(true);

		// constructor
		// Get an image from the specified file in the current directory on the
		// local hard disk.
		
		
		width = carImage.getWidth();//
		height = carImage.getHeight();//

		target_gray = rgb2gray(target);
		texture_gray = rgb2gray(texture);

		quilt = new BufferedImage(texture.getWidth() * 2, texture.getWidth() * 2, texture.getType());
		//quilt1 = quilt1(texture, quilt, false);
		//quilt2 = quilt1(texture, quilt, true);
		quilt2 = quilt3(texture, target, texture_gray, target_gray , true);
		//quilt3 = quilt(texture, quilt);
		
		myOptImage = quilt2(texture, target, texture_gray, target_gray);
		//myOptImage1 = quilt2(texture, jesse, rgb2gray(jesse));
		//myOptImage = quilt(texture, target);

		// Anonymous inner-class listener to terminate program
		this.addWindowListener(new WindowAdapter() {// anonymous class definition
			public void windowClosing(WindowEvent e) {
				System.exit(0);// terminate the program
			}// end windowClosing()
		}// end WindowAdapter
		);// end addWindowListener
	}// end constructor

	

	//=======================================================//
	
	//Randomly finds small patch
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
	
	
	public BufferedImage smallPatch2(BufferedImage src) {
		int w = src.getWidth() / percent;
		int h = src.getHeight() / percent;
		//int w = 30;
		//int h = 30;
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

	public BufferedImage quilt(BufferedImage input, BufferedImage output) {
		int outW = output.getWidth();
		int outH = output.getHeight();

		int inpW = input.getWidth();
		int inpH = input.getHeight();

		int smlW = inpW / percent;
		int smlH = inpH / percent;

		int overlapW = smlW / 4;
		int overlapH = smlH / 4;

		BufferedImage smlBlockA = smallPatch2(output);
		BufferedImage result = new BufferedImage(outW, outH, output.getType());
		//BufferedImage result = new BufferedImage(outW, outH, output.getType());
		for (int j = 0; j < result.getHeight(); j += (smlH - overlapH)) {
			for (int i = 0; i < result.getWidth(); i += (smlW - overlapW)) {

				if (i == 0 && j == 0) {
					smlBlockA = smallPatch(input);
				} else {
					smlBlockA = minimumErrorBoundary(result, returnB(result, input, i, j), i, j);
				}

				for (int a = 0; a < smlW && i + a < result.getWidth(); a++) {
					for (int b = 0; b < smlH && j + b < result.getHeight(); b++) {
						result.setRGB(i + a, j + b, smlBlockA.getRGB(a, b));
					}
				}

			}
		}
		return result;
	}
	
	public BufferedImage quilt2(BufferedImage input, BufferedImage output, BufferedImage greyMapTex,BufferedImage greyMapTar ) {
		int outW = output.getWidth();
		int outH = output.getHeight();

		int inpW = input.getWidth();
		int inpH = input.getHeight();

		int smlW = inpW / percent;
		int smlH = inpH / percent;
		
		//System.out.println(Integer.toString(smlW)+" , "+Integer.toString(smlH) );
		//int smlW = 30;
		//int smlH = 30;

		int overlapW = smlW / 4;
		int overlapH = smlH / 4;

		BufferedImage smlBlockA = smallPatch2(output);
		BufferedImage result = new BufferedImage(outW, outH, input.getType());
		//BufferedImage result = new BufferedImage(outW, outH, output.getType());
		for (int j = 0; j < result.getHeight(); j += (smlH - overlapH)) {
			for (int i = 0; i < result.getWidth(); i += (smlW - overlapW)) {

				if (i == 0 && j == 0) {
					smlBlockA = smallPatch2(input);
				} else {
					smlBlockA = minimumErrorBoundary1(result, returnB1(result, input, greyMapTex, greyMapTar , i, j), greyMapTex, greyMapTar , i, j);
				}

				for (int a = 0; a < smlW && i + a < result.getWidth(); a++) {
					for (int b = 0; b < smlH && j + b < result.getHeight(); b++) {
						result.setRGB(i + a, j + b, smlBlockA.getRGB(a, b));
					}
				}

			}
		}
		return result;
	}
	
	public double findBlockB1(BufferedImage output, BufferedImage blockB, BufferedImage greyMapTex, BufferedImage greyMapTar, int x, int y) {

		//TODO have greymap actually be used
		int widthA = blockB.getWidth();
		int heightA = blockB.getHeight();

		int overlapW = widthA / 4;

		int startY = heightA - overlapW;
		//Color cost between texture image
		double mycost = 0;
		//correspondence between source image cost
		double corCost = 0;
		
		//double[] twoCosts = new double [2];

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
					
					//double colorDifference = Math.pow((hsbA[2] - hsbB[2]), 2);
					//double correspondenceDifference = Math.pow((hsbC[2] - hsbD[2]), 2);
					
					double colorDifference = Math.pow((redA - redB), 2) + Math.pow((blueA - blueB), 2) + Math.pow((greenA - greenB), 2);
					double corDifference = Math.pow((redC - redD), 2) + Math.pow((blueC - blueD), 2) + Math.pow((greenC - greenD), 2);
					//colorArray[j][i] = colorDifference;
					//System.out.println("color value differenc					
					

					//double colorDifference = Math.pow((redA - redB), 2) + Math.pow((blueA - blueB), 2)+ Math.pow((greenA - greenB), 2);
					mycost += colorDifference;
					corCost += corDifference;
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
					int pixelC = greyMapTex.getRGB(i, j);
					int redC = getRed(pixelC);
					int blueC = getBlue(pixelC);
					int greenC = getGreen(pixelC);
					
					int pixelD;
					int redD = 0;
					int blueD = 0;
					int greenD = 0;

					try {
						pixelD = greyMapTar.getRGB(x + i, y + j);
						redD = getRed(pixelD);
						blueD = getBlue(pixelD);
						greenD = getGreen(pixelD);
					} catch (Exception e) {
						System.out.println(Integer.toString(x+i)+","+Integer.toString(y+j));
						System.out.println("Height is "+ Integer.toString(greyMapTar.getHeight())+", Width is "+Integer.toString(greyMapTar.getWidth()));
					}

					
					float[] hsbA = new float[3];
					float[] hsbB = new float[3];
					float[] hsbC = new float[3];
					float[] hsbD = new float[3];
					Color.RGBtoHSB(redA, blueA, greenA, hsbA);
					Color.RGBtoHSB(redB, blueB, greenB, hsbB);				
					Color.RGBtoHSB(redC, blueC, greenC, hsbC);
					Color.RGBtoHSB(redD, blueD, greenD, hsbD);
					
					
					//double colorDifference = Math.pow((hsbA[2] - hsbB[2]), 2);
					//double correspondenceDifference = Math.pow((hsbC[2] - hsbD[2]), 2);
					double colorDifference = Math.pow((redA - redB), 2) + Math.pow((blueA - blueB), 2) + Math.pow((greenA - greenB), 2);
					double corDifference = Math.pow((redC - redD), 2) + Math.pow((blueC - blueD), 2) + Math.pow((greenC - greenD), 2);

					//double colorDifference = Math.pow((redA - redB), 2) + Math.pow((blueA - blueB), 2)+ Math.pow((greenA - greenB), 2);
					mycost += colorDifference;
					corCost += corDifference;
				}
			}
		}
		//twoCosts[0] = mycost;
		//twoCosts[1] = corCost;
		double trueCost = ((mycost*alpha)+(corCost* (1-alpha)));
		return trueCost;
	}	
	
	public BufferedImage returnB1(BufferedImage output, BufferedImage input, BufferedImage greyMapTex, BufferedImage greyMapTar, int x, int y) {
		
		//TODO have greymap actually be used
		BufferedImage blockB = smallPatch2(input);
		BufferedImage finalBlockB = blockB;
		//double[] originCosts = findBlockB1(output, rgb2gray(blockB), greymap,x, y);
		double originCost = findBlockB1(output, rgb2gray(blockB), greyMapTex, greyMapTar ,x, y);
		//double originColCost = originCosts[0];
		//double originCorCost = 50.0;
		//double originCorCost = originCosts[1];
		for (int t = 0; t < 200; t++) {
			blockB = smallPatch(input);
			//double[] iterativeCosts = findBlockB1(output, rgb2gray(blockB), greymap, x, y);		
			double iterativeCost = findBlockB1(output, rgb2gray(blockB), greyMapTex, greyMapTar, x, y);	
			//double iterativeColCost = iterativeCosts[0];
			//double iterativeCorCost = iterativeCosts[1];
			if (iterativeCost < originCost) {
				//System.out.println("originColCost: "+ Double.toString(originColCost) + " iterativeColCost: "+ Double.toString(iterativeColCost)+ " originCorCost: "+ Double.toString(originCorCost) + " iterativeCorCost: "+ Double.toString(iterativeCorCost));
				originCost = iterativeCost;
				//originCorCost = iterativeCorCost;
				finalBlockB = blockB;
			}
		}
		return finalBlockB;
	}

	public BufferedImage minimumErrorBoundary1(BufferedImage output, BufferedImage blockB, BufferedImage greyMapTex, BufferedImage greyMapTar , int x, int y) {

		BufferedImage cutB = new BufferedImage(blockB.getWidth(), blockB.getHeight(), blockB.getType());

		int widthA = blockB.getWidth();
		int heightA = blockB.getHeight();

		int overlapW = widthA / 4;

		int startingA = (widthA * 3) / 4;

		int a = 0;
		int locationX = 0;

		double[][] colorArray = new double[heightA][widthA];
		//Correspondence array
		double[][] corArray = new double[heightA][widthA];
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
					double corDifference = Math.pow((redC - redD), 2) + Math.pow((blueC - blueD), 2) + Math.pow((greenC - greenD), 2);
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
					double corDifference = Math.pow((redC - redD), 2) + Math.pow((blueC - blueD), 2) + Math.pow((greenC - greenD), 2);
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
	
	
	
	public BufferedImage quilt3(BufferedImage input, BufferedImage output, BufferedImage greyMapTex, BufferedImage greyMapTar , Boolean overlap) {
		int outW = output.getWidth();
		int outH = output.getHeight();

		int inpW = input.getWidth();
		int inpH = input.getHeight();

		int smlW = inpW / percent;
		int smlH = inpH / percent;

		int overlapW = 0;
		int overlapH = 0;
		if (overlap) {
			overlapW = smlW / overlapPercent;
			overlapH = smlH / overlapPercent;
		}

		BufferedImage smlBlockA = smallPatch(input);
		BufferedImage result = new BufferedImage(outW, outH, output.getType());
		for (int j = 0; j < result.getHeight(); j += (smlH - overlapH)) {
			for (int i = 0; i < result.getWidth(); i += (smlW - overlapW)) {

				if (overlap) {
					if (i == 0 && j == 0) {
						smlBlockA = smallPatch(input);
					} else {
						smlBlockA = returnB1(result, input, greyMapTex, greyMapTar , i, j);
					}
				} else {
					smlBlockA = smallPatch(input);
				}

				for (int a = 0; a < smlW && i + a < result.getWidth(); a++) {
					for (int b = 0; b < smlH && j + b < result.getHeight(); b++) {
						result.setRGB(i + a, j + b, smlBlockA.getRGB(a, b));
					}
				}

			}
		}
		return result;
	}
	
	
	
	public double findBlockB(BufferedImage output, BufferedImage blockB, int x, int y) {

		int widthA = blockB.getWidth();
		int heightA = blockB.getHeight();

		int overlapW = widthA / 4;

		int startY = heightA - overlapW;

		double mycost = 0;

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

					double colorDifference = Math.pow((redA - redB), 2) + Math.pow((blueA - blueB), 2)
							+ Math.pow((greenA - greenB), 2);
					mycost += colorDifference;
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

					double colorDifference = Math.pow((redA - redB), 2) + Math.pow((blueA - blueB), 2)
							+ Math.pow((greenA - greenB), 2);
					mycost += colorDifference;
				}
			}
		}

		return mycost;
	}

	public BufferedImage returnB(BufferedImage output, BufferedImage input, int x, int y) {
		BufferedImage blockB = smallPatch(input);
		BufferedImage finalBlockB = blockB;
		double originCost = findBlockB(output, blockB, x, y);
		for (int t = 0; t < 200; t++) {
			blockB = smallPatch(input);
			double iterativeCost = findBlockB(output, blockB, x, y);
			if (iterativeCost < originCost) {
				originCost = iterativeCost;
				finalBlockB = blockB;
			}
		}
		return finalBlockB;
	}


	
	public BufferedImage minimumErrorBoundary(BufferedImage output, BufferedImage blockB, int x, int y) {

		BufferedImage cutB = new BufferedImage(blockB.getWidth(), blockB.getHeight(), blockB.getType());

		int widthA = blockB.getWidth();
		int heightA = blockB.getHeight();

		int overlapW = widthA / 4;

		int startingA = (widthA * 3) / 4;

		int a = 0;
		int locationX = 0;

		int[][] colorArray = new int[heightA][widthA];

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

					double colorDifference = Math.pow((redA - redB), 2) + Math.pow((blueA - blueB), 2)
							+ Math.pow((greenA - greenB), 2);

					colorArray[j][i] = (int) colorDifference;
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

					double colorDifference = Math.pow((redA - redB), 2) + Math.pow((blueA - blueB), 2)
							+ Math.pow((greenA - greenB), 2);

					colorArray[j][i] = (int) colorDifference;
				}
			}
		}
//				System.out.println(colorArray.length);
//				System.out.println(colorArray[0].length);
//				System.out.println(Arrays.deepToString(colorArray));

		if (y != 0) {
			for (int s = 1; s < widthA; s++) {
				for (int t = 0; t < overlapW; t++) {
					if (t == 0) {
						colorArray[t][s] += Math.min(colorArray[t][s - 1], colorArray[t + 1][s - 1]);
					} else if (t == overlapW - 1) {
						colorArray[t][s] += Math.min(colorArray[t][s - 1], colorArray[t - 1][s - 1]);
					} else {
						colorArray[t][s] += Math.min(Math.min(colorArray[t][s - 1], colorArray[t + 1][s - 1]),
								colorArray[t - 1][s - 1]);
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
					} else if (s == overlapW - 1) {
						colorArray[t][s] += Math.min(colorArray[t - 1][s - 1], colorArray[t - 1][s]);
					} else {
						colorArray[t][s] += Math.min(Math.min(colorArray[t - 1][s - 1], colorArray[t - 1][s]),
								colorArray[t - 1][s + 1]);
					}

				}
			}
//					System.out.println(Arrays.deepToString(colorArray));

		}

		int[] locatY = new int[widthA];
		if (y != 0) {
			for (int k = widthA - 1; k > 0; k--) {
				int minimum = 0;
				if (k == widthA - 1) {
					for (int u = 0; u < overlapW; u++) {
						if (u == 0 || colorArray[k][u] < minimum) {
							minimum = colorArray[k][u];
							locatY[k] = u;
						}
					}
				}
				if (locatY[k] == 0) {
					int smalleast = 0;
					for (int q = 0; q < 2; q++) {
						if (smalleast == 0 || colorArray[k - 1][q] < smalleast) {
							smalleast = colorArray[k - 1][q];
							locatY[k - 1] = q;
						}
					}
				} else if (locatY[k] == overlapW - 1) {
					int minum = 0;
					for (int r = overlapW - 2; r < overlapW; r++) {
						if (minum == 0 || colorArray[k - 1][r] < minum) {
							minum = colorArray[k - 1][r];
							locatY[k - 1] = r;
						}
					}
				} else {
					int small = 0;
					for (int d = locatY[k] - 1; d < locatY[k] + 2; d++) {
						if (small == 0 || colorArray[k - 1][d] < small) {
							small = colorArray[k - 1][d];
							locatY[k - 1] = d;
						}
					}
				}
			}
		}

		int[] locatX = new int[heightA];
		if (x != 0) {
			for (int k = heightA - 1; k > 0; k--) {
				int minimum = 0;
				if (k == heightA - 1) {
					for (int u = 0; u < overlapW; u++) {
						if (u == 0 || colorArray[k][u] < minimum) {
							minimum = colorArray[k][u];
							locatX[k] = u;
						}
					}
				}

				if (locatX[k] == 0) {
					int smalleast = 0;
					for (int q = 0; q < 2; q++) {
						if (smalleast == 0 || colorArray[k - 1][q] < smalleast) {
							smalleast = colorArray[k - 1][q];
							locatX[k - 1] = q;
						}
					}
				} else if (locatX[k] == overlapW - 1) {
					int minum = 0;
					for (int r = overlapW - 2; r < overlapW; r++) {
						if (minum == 0 || colorArray[k - 1][r] < minum) {
							minum = colorArray[k - 1][r];
							locatX[k - 1] = r;
						}
					}
				} else {
					int small = 0;
					for (int d = locatX[k] - 1; d < locatX[k] + 2; d++) {
						if (small == 0 || colorArray[k - 1][d] < small) {
							small = colorArray[k - 1][d];
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
		
		return cutB;
	}

	
	
	public BufferedImage quilt1(BufferedImage input, BufferedImage output, Boolean overlap) {
		int outW = output.getWidth();
		int outH = output.getHeight();

		int inpW = input.getWidth();
		int inpH = input.getHeight();

		int smlW = inpW / percent;
		int smlH = inpH / percent;

		int overlapW = 0;
		int overlapH = 0;
		if (overlap) {
			overlapW = smlW / overlapPercent;
			overlapH = smlH / overlapPercent;
		}

		BufferedImage smlBlockA = smallPatch(input);
		BufferedImage result = new BufferedImage(outW, outH, output.getType());
		for (int j = 0; j < result.getHeight(); j += (smlH - overlapH)) {
			for (int i = 0; i < result.getWidth(); i += (smlW - overlapW)) {

				if (overlap) {
					if (i == 0 && j == 0) {
						smlBlockA = smallPatch(input);
					} else {
						smlBlockA = returnB(result, input, i, j);
					}
				} else {
					smlBlockA = smallPatch(input);
				}

				for (int a = 0; a < smlW && i + a < result.getWidth(); a++) {
					for (int b = 0; b < smlH && j + b < result.getHeight(); b++) {
						result.setRGB(i + a, j + b, smlBlockA.getRGB(a, b));
					}
				}

			}
		}
		return result;
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
				int newRed = (int) (0.309 * c.getRed());
				int newGreen = (int) (0.609 * c.getGreen());
				int newBlue = (int) (0.082 * c.getBlue());
				int roOffset = newRed + newGreen + newBlue;
				converted.setRGB(width, height, new Color(roOffset, roOffset, roOffset).getRGB());
			}
		}
		return converted;
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
	
	public void paint(Graphics g) {
		int w = width / 5;
		int h = height / 5;

		this.setSize(1280, 800);

		g.setColor(Color.BLACK);
		Font f1 = new Font("Helvetica", Font.PLAIN, 13);
		g.setFont(f1);

		g.drawString("1.Texture image", 25, 45);
		
		//Quilt1's drawing
		//g.drawString("1.a. Simple quilt", 25 * 2 + texture.getWidth(), 40);
		
		//Quilt 2's drawing
		//g.drawString("1.b. Overlap quilt", 25 * 3 + texture.getWidth() + quilt.getWidth(), 40);
		
		//Quilt 3's Drawing
		//g.drawString("1.c. Quilt with minimum error boundary cut", 25 * 4 + texture.getWidth() + quilt.getWidth() * 2, 40);

		g.drawString("2. Target image", 25, 50 * 2 + quilt.getHeight() - 10);
		g.drawString("2.a. Grayscale image", 25 * 2 + target.getWidth(), 50 * 2 + quilt.getHeight() - 10);
		g.drawString("2.b. Texture transfer image", 25 * 3 + target.getWidth() * 2, 50 * 2 + quilt.getHeight() - 10);

		
		g.drawImage(texture, 25, 50, texture.getWidth(), texture.getHeight(), this);

		g.drawImage(target, 25, 50 * 2 + quilt.getHeight(), target.getWidth(), target.getHeight(), this);

		//g.drawImage(quilt1, 25 * 2 + texture.getWidth(), 50, quilt1.getWidth(), quilt1.getHeight(), this);
		
		try {

			g.drawImage(quilt2, 25 * 3 + target.getWidth()*2, 50, quilt2.getWidth(), quilt2.getHeight(), this);
		} catch (Exception e) {
			System.out.println("Cannot draw quilt 2");
		}
		/*
		try {

			g.drawImage(quilt3, 25 * 4 + texture.getWidth() + quilt.getWidth() * 2, 50, quilt.getWidth(), quilt3.getHeight(), this);
		} catch (Exception e) {
			System.out.println("Cannot draw quilt 3");
		}

		*/
		g.drawImage(target_gray, 25 * 2 + target.getWidth(), 50 * 2 + quilt.getHeight(), target.getWidth(), target.getHeight(), this);

		try {

			g.drawImage(myOptImage, 25 * 3 + target.getWidth() * 2, 50 * 2 + quilt.getHeight(), myOptImage.getWidth(), myOptImage.getHeight(), this);
			//g.drawImage(myOptImage1, 25 * 4 + jesse.getWidth() * 2, 50 * 2 + quilt.getHeight(), myOptImage1.getWidth(), myOptImage1.getHeight(), this);
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