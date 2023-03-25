
public class Calc_Errors_2 {

function [ errors ] = calc_errors_2( tex, tex_corr, tar_corr, p, q, alpha, ...
out_slice, casee, bsize, ovsize, do_constraint_2)
//CALC_ERRORS

[tex_h, tex_w, ~] = size(tex);

errors = zeros(tex_h - bsize, tex_w - bsize);

	for (int i = 1; i < (tex_h - bsize); i++)// loops over rows
	  for (int j = 1; j < (tex_w - bsize); j++)   // loops over cols
	    // We need to find a block whose above (or left) slice
	    // is similar to the stuff that was already at the block we're
	    // considering, from quilting.m's main loop
	    si = i; sj = j;
	    if strcmp(casee, 'above')
	      ei = si + ovsize - 1;
	      ej = sj + bsize - 1;
	      
	    elseif strcmp(casee, 'left')
	      ei = si + bsize - 1;
	      ej = sj + ovsize - 1;
	      
	    elseif strcmp(casee, 'corner')
	      // Used to remove the tiny overlapping region
	      ei = si + ovsize - 1;
	      ej = sj + ovsize - 1;
	    end
	    
	    // Retrieve the block slice
	    block_slice = tex(si:ei, sj:ej, :);
	    
	    // Constraint 1: block overlap matching error
	    constraint_1 = sum((out_slice(:) - block_slice(:)).^2);
	    
	    // Constraint 2: squared error between correspondence map pixels
	    // within source texture block and those at the current target
	    // image position
	    constraint_2 = 0;
	    
	    if do_constraint_2
	      tar_block = tar_corr((p-1)*bsize + 1:p*bsize, (q-1)*bsize + 1:q*bsize);
	      tex_block = tex_corr(si:si+bsize - 1, sj:sj+bsize - 1, :);
	      constraint_2 = sum(tex_block(:) - tar_block(:)).^2;
	    end    
	    
	    // Calculate the error for the block
	    errors(i, j) =  constraint_1 * alpha + constraint_2 * (1 - alpha);
	  end
	end

end
}
