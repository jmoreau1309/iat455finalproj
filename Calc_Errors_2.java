import java.awt.image.BufferedImage;
import java.awt.image.*;
public class Calc_Errors_2 {

	BufferedImage tex;
	BufferedImage tex_corr;
	BufferedImage tar_corr;
	int tex_h,tex_w;
	int p;
	int q;
	double alpha;
	double si,ei,sj,ej;
	int [][] out_slice;
	int [][] block_slice;
	int [] test;
	String casee;
	int bsize;
	int ovsize;	
	int constraint;
//function [ errors ] = calc_errors_2( tex, tex_corr, tar_corr, p, q, alpha, ...
//out_slice, casee, bsize, ovsize, do_constraint_2)
//CALC_ERRORS

	public Calc_Errors_2(BufferedImage tex, BufferedImage tex_corr, BufferedImage tar_corr, int p, int q, double alpha, int [][] out_slice, String casee, int bsize, int ovsize) {
		
		int [][] block_slice = new int[(int) (ei - si)][(int) (ej - sj)];

		tex = this.tex;
		tex_corr = this.tex_corr;
		tar_corr = this.tar_corr;


		tex_h = tex.getHeight();
		tex_w = tex.getWidth();
		
		int [] test = new int[tex_w];
		
		int out_slice_h = this.out_slice[0].length;
		int out_slice_w = this.out_slice.length;
		
		int r1,g1,b1,r2,g2,b2,rgb1,rgb2;
		rgb1 = 0;
		rgb2 = 0;
		for (int i = 1; i < out_slice_h; i++) {    // loops over rows
			  for (int j = 1; j < out_slice_w; j++) {   // loops over cols
				  out_slice[i][j] = this.out_slice[i][j];
			  }
			}
		casee = this.casee;
		bsize = this.bsize;
		ovsize = this.ovsize;
		
		for (int i = 1; i < tex_h-bsize; i++) {    // loops over rows
			  for (int j = 1; j < tex_w-bsize; j++) {   // loops over cols
				  si = i;
				  sj = j;
				   
				  if(this.casee == "above") {
				      ei = si + ovsize - 1;
				      ej = sj + bsize - 1;
				  }
				  else if(this.casee == "left") {
				      ei = si + bsize - 1;
				      ej = sj + ovsize - 1;
				  }
				  else if(this.casee == "corner") {
				      //Used to remove the tiny overlapping region
				      ei = si + ovsize - 1;
				      ej = sj + ovsize - 1;
				  }
			        int ci = (int) si;
			        int cj = (int) sj;	
			        
			        //tex.getRGB((int) si,(int) sj,tex_w,tex_h,test ,0,ci);
			        
			        for (ci = (int) si; ci < ei ; ci++) {
			        	for (cj = (int) sj; cj < ej ; cj++) {
			        			block_slice[ci][cj] = tex.getRGB(ci, cj);

			        		}

			        }
			        
			        for (ci = (int) si; ci < ei ; ci++) {
			        	for (cj = (int) sj; cj < ej ; cj++) {

			        		r1 = getRed((block_slice[ci][cj]));
			        		g1 = getGreen(block_slice[ci][cj]);
			        		b1 = getBlue(block_slice[ci][cj]);
			        		
			        		rgb1 = r1+g1+b1+rgb1;
			        		
			        		r2 = getRed((out_slice[ci][cj]));
			        		g2 = getGreen(out_slice[ci][cj]);
			        		b2 = getBlue(out_slice[ci][cj]);
			        		
			        		rgb2 = r2+g2+b2+rgb2;
			        	}
			        }
			        constraint = (int) (rgb2 - Math.pow((double) rgb1, (double)2));
			        
			        //Will calculate the error for the block
			        //errors[i][j] =  constraint_1 * alpha
			        
			  }
		}
		
		
		
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

}
