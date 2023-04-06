import java.awt.Graphics;
import java.awt.image.BufferedImage;

//Based on Image Quilting for Texture Synthesis and Transfer
//by Alice Wang and Jordan Mecom
//http://jmecom.github.io/projects/computational-photography/texture-synthesis/
public class Transfer {
	
	// Parameters

	int bsize = 20;
	double ovsize = Math.floor(bsize/6);
	double tolerance = 0.1;
	double alpha = 0.6;
	double out_h = 0.32;
	
	double si,ei,sj,ej;
	
	double sizeh,sizew, tex_h, tex_w, tar_h, tar_w;
	double row_idx,col_idx;
	BufferedImage tex;  
	BufferedImage tar;
	BufferedImage tex_corr;  
	BufferedImage tar_corr;
	double[][] errors;
	int [][] out;
	int [][] outslice;
	int [][] dp;
	int errorsnum;
	// N = 3;


	
	public Transfer() {
	 sizeh = (Math.floor(out_h/bsize));
	 sizew = (Math.floor(out_h/bsize));
	 
	 out = new int [(int) tar_h] [(int) tar_w];
	 outslice = new int [bsize] [bsize];
	
	for (int i = 1; i < sizeh; i++) {    // loops over rows
	  for (int j = 1; j < sizew ; j++) {   // loops over cols

		  	
		  		//Start Row
			    si = (i-1)*bsize - (i-1)*ovsize + 1;
			    //End Row
			    ei = si + bsize - 1;
			    //Start Column
			    sj = (j-1)*bsize - (j-1)*ovsize + 1;
			    //End Column
			    ej = sj + bsize - 1;
			    
			    
			    // Error for each potential match
			    errors = new double[(int) (tex_h - bsize)][(int) (tex_w - bsize)];
			    
			    
			    //First block is random
			    if (i == 1 && j == 1) {
			        int min = 0; // Minimum value of range
			        int max = (int) tex_h-bsize; // Maximum value of range
			        row_idx = (int)Math.floor(Math.random() * (max - min + 1) + min);
			        
			        max = (int) tex_w - bsize;
			        col_idx = (int)Math.floor(Math.random() * (max - min + 1) + min);
			        
			        int ci = (int) si;
			        int cj = (int) sj;
			        //Cycles through the block size
			        for (ci = (int) si; ci < ei ; ci++) {
			        	
			        	for (cj = (int) sj; cj < ej ; cj++) {
			        			out[ci][cj] = tex.getRGB((int)row_idx, (int)col_idx);
					        	col_idx++;
			        		}
			        	
			        	row_idx++;
			        }
			    }
			      
			    else if (i == 1) {
			    	//Above Case
			        int ci = (int) si;
			        int cj = (int) sj;
			        //Cycles through the block size
			        for (ci = (int) si; ci < ei ; ci++) {
			        	
			        	for (cj = (int) sj; cj < (sj+ovsize-1) ; cj++) {
			        			outslice[ci][cj] = out[ci][cj];      	
			        		}
			        	
			        }
			    	

			    }
			    else if (j == 1) {

			    }
			    else {

			    }
			    

			    
			    // Find the min cut
			    if (i == 1) {
			      // Above overlap

			    }
			    
			    
			    if (j == 1) {

			    }
			  

	  		  }

	  		}		


	}
	
	
	public void dpCut(int [][] a, int [][] b, String casee) {
		
		int cj;
		int ci;
		int r1,g1,b1,r2,g2,b2,rgb1,rgb2;
		rgb1 = 0;
		rgb2 = 0;
		//This is done incorrectly and need to actually figure out what sum of arrays is doing in matlab
        for (ci = (int) si; ci < ei ; ci++) {
        	for (cj = (int) sj; cj < ej ; cj++) {

        		r1 = getRed((a[ci][cj]));
        		g1 = getGreen(a[ci][cj]);
        		b1 = getBlue(a[ci][cj]);
        		
        		rgb1 = r1+g1+b1+rgb1;
        		
        		r2 = getRed((b[ci][cj]));
        		g2 = getGreen(b[ci][cj]);
        		b2 = getBlue(b[ci][cj]);
        		
        		rgb2 = r2+g2+b2+rgb2;
        	}
        }
        errorsnum = (int) Math.pow( (double) (rgb2 -  rgb1), (double)2);
        
        if (casee.equals("hori")) {
        	//Confused by this line, adding it to be consistent with code from matlab but will review later
        	errorsnum = errorsnum;
        }
        int error_h = errorsnum;
        int error_w = errorsnum;
        
        dp = new int [(int) error_h] [(int) error_w];
        
        //end of incorrect part

	      //Cycles through the block size
	      for (ci = (int) si; ci < ei ; ci++) {
	        	
	        for (cj = (int) sj; cj < (sj+ovsize-1) ; cj++) {
	        			//outslice[ci][cj] = out[ci][cj];      	
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

