
public class Transfer {
	
	// Parameters

	int bsize = 20;
	double ovsize = Math.floor(bsize/6);
	double tolerance = 0.1;
	double alpha = 0.6;
	double out_h = 0.32;
	BufferedImage tex;  
	BufferedImage tar;
	BufferedImage tex_corr;  
	BufferedImage tar_corr;

	// N = 3;

	// Open texture and target image
	//tex = double(imread('images/starry-night.jpg'));
	// tex = imresize(tex, 0.5);

	//[tex_h, tex_w, ~] = size(tex);

	//tar = double(imread('images/JesseS5_Small.jpg'));
	// tar = imresize(tar, 0.5);
	
	//[tar_h, tar_w, ~] = size(tar);

	//out = double(zeros(tar_h, tar_w, 3));
	//[out_h, out_w, ~] = size(out);

	// Make the correspondence images
	// G = fspecial('gaussian', [5 5], 2);

	//tex_corr = double(rgb2gray(uint8(tex)));
	//tar_corr = double(rgb2gray(uint8(tar)));

	// tex_corr = imfilter(tex_corr, G, 'same');
	// tar_corr = imfilter(tar_corr, G, 'same');
	double sizeh = (Math.floor(out_h/bsize));
	double sizew = (Math.floor(out_h/bsize));
	
	for (int i = 1; i < sizeh; i++) {    // loops over rows
	  for (int j = 1; j < sizew ; j++) {   // loops over cols
	    
			    // If i == 1 then we're at the top row, so check error above
			    //    j == 1           ""     left col, so check error left
			    //
			    // If we're in the case where i, j >= 2
			    //   then si:ei will get the the strip above the block we're considering
			    //   and  sj:ej will get the strip to the left of the block
			    si = (i-1)*bsize - (i-1)*ovsize + 1;
			    ei = si + bsize - 1;
			    sj = (j-1)*bsize - (j-1)*ovsize + 1;
			    ej = sj + bsize - 1;
			    
			    //     for k = 1:N
			    // Set alpha
			    //       alpha = 0.8*((i-1)/(N-1)) + 0.1;
			    
			    // Error for each potential match
			    errors = zeros(tex_h - bsize, tex_w - bsize);
			    
			    if (i == 1 && j == 1) {
			      // Very first case, so pick one at random
			      row_idx = randi(tex_h - bsize);
			      col_idx = randi(tex_w - bsize);
			      
			      out(si:ei, sj:ej, :) = tex(row_idx :row_idx  + bsize - 1, ...
			        col_idx:col_idx + bsize - 1, :);
			    }
			      
			    else if (i == 1) {
			      // Top row, so only check left edges
			      out_slice = out(si:ei, sj:sj + ovsize - 1, :);
			      errors = calc_errors_2(tex, tex_corr, tar_corr, i, j, ...
			        alpha, out_slice, 'left', bsize, ovsize, true);
			    }
			    else if (j == 1) {
			      // Left col, so only check above
			      out_slice = out(si:si + ovsize - 1, sj:ej, :);
			      errors = calc_errors_2(tex, tex_corr, tar_corr, i, j, ...
			        alpha, out_slice, 'above', bsize, ovsize, true);
			    }
			    else {
			      // Typical case, check above and left
			      out_slice = out(si:ei, sj:sj + ovsize - 1, :);
			      errors = calc_errors_2(tex, tex_corr, tar_corr, i, j, ...
			        alpha, out_slice, 'left', bsize, ovsize, true);
			      
			      out_slice = out(si:si + ovsize - 1, sj:ej, :);
			      errors = errors + calc_errors_2(tex, tex_corr, tar_corr, i, j, ...
			        alpha, out_slice, 'above', bsize, ovsize, false);
			      
			      // Remove the overlapping region in above and left cases
			      out_slice = out(si:si + ovsize - 1, sj:sj + ovsize - 1, :);
			      errors = errors - calc_errors_2(tex, tex_corr, tar_corr, i, j, ...
			        alpha, out_slice, 'corner', bsize, ovsize, false);
			    }
			    
			//     // Consider the additional constraint, modifying errors accordingly
			//     errors = errors .* alpha; // Original constraint is weighted by alpha
			//     tar_block = tar_corr((i-1)*bsize + 1:i*bsize, (j-1)*bsize + 1:j*bsize);
			//     //       imshow(uint8(tar_block));
			//     
			//     // Look at all possible block offsets and update the error
			//     // to compare each offset with the target block in correspondence
			//     // image
			//     for m = 1:tex_h - bsize
			//       for n = 1:tex_w - bsize
			//         // Add on the next constraint: ssd between correspondence images
			//         tex_block = tex_corr(m:m+bsize-1, n:n+bsize-1);
			//         errors(m, n) = errors(m, n) + (1 - alpha) .* sum(tex_block(:) - tar_block(:)).^2;
			//       end
			//     end
			//     //     end
			    
			    // Find the match and its indicies
			    matches = find(errors(:) <= (1 + tolerance)*min(errors(:)));
			    match_index = matches(randi(length(matches)));
			    [tex_r, tex_c] = ind2sub(size(errors), match_index);
			    
			    // Boundary divides the sides of the min cut with 1s and 0s
			    boundary = ones(bsize, bsize);
			    
			    // Find the min cut
			    if (i == 1) {
			      // Above overlap
			      im_overlap = tex(tex_r:tex_r+ovsize - 1, tex_c:tex_c+bsize - 1, :);
			      out_overlap = out(si:si+ovsize - 1, sj:ej, :);
			      cut = dpcut(im_overlap, out_overlap, 'hori');
			      boundary(1:ovsize, 1:bsize) = double(cut >= 0);
			    }
			    
			    
			    if (j == 1) {
			      // Left overlap
			      im_overlap = tex(tex_r:tex_r+bsize-1, tex_c:tex_c+ovsize-1, :);
			      out_overlap = out(si:ei, sj:sj+ovsize-1, :);
			      cut = dpcut(im_overlap, out_overlap, 'vert');
			      boundary(1:bsize, 1:ovsize) = boundary(1:bsize, 1:ovsize) .* double(cut >= 0);
			    }
			  
			    
			    // Overlay the block onto the output image, cutting along the boundary
			    boundary = repmat(boundary, 1, 1, 3);
			    out(si:ei, sj:ej, :) = out(si:ei, sj:ej, :) .* (boundary == 0) + ...
			      tex(tex_r:tex_r+bsize-1, tex_c:tex_c+bsize-1, :) .* (boundary == 1);
			  //end
	  		  }
			  //imshow(uint8(out));
			  //imsave(uint8(out));
			  drawnow();
			//end
	  		}		
			//imshow(uint8(out));


}

