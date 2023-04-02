import java.awt.image.BufferedImage;

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
	// N = 3;


	
	public Transfer() {
	 sizeh = (Math.floor(out_h/bsize));
	 sizew = (Math.floor(out_h/bsize));
	 
	 out = new int [(int) tar_h] [(int) tar_w];
	 outslice = new int [bsize] [bsize];
	
	for (int i = 1; i < sizeh; i++) {    // loops over rows
	  for (int j = 1; j < sizew ; j++) {   // loops over cols
	    
			    // If i == 1 then we're at the top row, so check error above
			    //    j == 1           ""     left col, so check error left
			    //
			    // If we're in the case where i, j >= 2
			    //   then si:ei will get the the strip above the block we're considering
			    //   and  sj:ej will get the strip to the left of the block
		  	
		  		//Start Row
			    si = (i-1)*bsize - (i-1)*ovsize + 1;
			    //End Row
			    ei = si + bsize - 1;
			    //Start Column
			    sj = (j-1)*bsize - (j-1)*ovsize + 1;
			    //End Column
			    ej = sj + bsize - 1;
			    
			    //     for k = 1:N
			    // Set alpha
			    //       alpha = 0.8*((i-1)/(N-1)) + 0.1;
			    
			    // Error for each potential match
			    errors = new double[(int) (tex_h - bsize)][(int) (tex_w - bsize)];
			    
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
}

