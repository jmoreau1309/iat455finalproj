import java.awt.image.BufferedImage;

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
	String casee;
	int bsize;
	int ovsize;	
//function [ errors ] = calc_errors_2( tex, tex_corr, tar_corr, p, q, alpha, ...
//out_slice, casee, bsize, ovsize, do_constraint_2)
//CALC_ERRORS

	public Calc_Errors_2(BufferedImage tex, BufferedImage tex_corr, BufferedImage tar_corr, int p, int q, double alpha, int [][] out_slice, String casee, int bsize, int ovsize) {
		

		tex = this.tex;
		tex_corr = this.tex_corr;
		tar_corr = this.tar_corr;
		

		tex_h = tex.getHeight();
		tex_w = tex.getWidth();
		
		int out_slice_h = this.out_slice[0].length;
		int out_slice_w = this.out_slice.length;
		
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
			  }
		}
		
	}



}
