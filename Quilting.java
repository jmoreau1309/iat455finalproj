
public class Quilting {
	// Parameters

	% btile
	% bsize = 16;
	% num_blocks_out = 10;
	% ovsize = floor(bsize/6);
	% tolerance = 0.1;

	% caustics
	% bsize = 25;
	% num_blocks_out = 12;
	% ovsize = floor(bsize/6);
	% tolerance = 0.1;

	% farm-aerial
	% bsize = 20;
	% num_blocks_out = 14;
	% ovsize = floor(bsize/6);
	% tolerance = 0.03;

	% % leaf-crop
	% bsize = 20;
	% num_blocks_out = 14;
	% ovsize = floor(bsize/6);
	% tolerance = 0.03;

	% % text3
	% bsize = 20;
	% num_blocks_out = 14;
	% ovsize = floor(bsize/6);
	% tolerance = 0.1;

	% % starfield-crop
	% bsize = 20;
	% num_blocks_out = 14;
	% ovsize = floor(bsize/6);
	% tolerance = 0.1;

	% % japanese-wallpaper
	% bsize = 20;
	% num_blocks_out = 14;
	% ovsize = floor(bsize/6);
	% tolerance = 0.1;

	% floor
	bsize = 35;
	num_blocks_out = 10;
	ovsize = floor(bsize/6);
	tolerance = 0.3;

	% Open image
	im = double(imread('images/floor.jpg'));
	[im_h, im_w, ~] = size(im);

	out_size = num_blocks_out * (bsize - ovsize - 1);
	out = double(zeros(out_size, out_size, 3));
	[out_h, out_w, ~] = size(out);

	for i = 1:num_blocks_out - 1    % loops over rows
	  for j = 1:num_blocks_out - 1  % loops over cols
	    
	    % If i == 1 then we're at the top row, so check error above
	    %    j == 1           ""     left col, so check error left
	    %
	    % If we're in the case where i, j >= 2
	    %   then si:ei will get the the strip above the block we're considering
	    %   and  sj:ej will get the strip to the left of the block
	    si = (i-1)*bsize - (i-1)*ovsize + 1;
	    ei = si + bsize - 1;
	    sj = (j-1)*bsize - (j-1)*ovsize + 1;
	    ej = sj + bsize - 1;
	    
	    % Error for each potential match
	    errors = zeros(im_h - bsize, im_w - bsize);
	    
	    if i == 1 && j == 1
	      % Very first case, so pick one at random
	      row_idx = randi(im_h - bsize);
	      col_idx = randi(im_w - bsize);
	      
	      out(si:ei, sj:ej, :) = im(row_idx :row_idx  + bsize - 1, ...
	        col_idx:col_idx + bsize - 1, :);
	      continue;
	    elseif i == 1
	      % Top row, so only check left edges
	      out_slice = out(si:ei, sj:sj + ovsize - 1, :);
	      errors = calc_errors(im, out_slice, 'left', bsize, ovsize);
	      
	    elseif j == 1
	      % Left col, so only check above
	      out_slice = out(si:si + ovsize - 1, sj:ej, :);
	      errors = calc_errors(im, out_slice, 'above', bsize, ovsize);
	    else
	      % Typical case, check above and left
	      out_slice = out(si:ei, sj:sj + ovsize - 1, :);
	      errors = calc_errors(im, out_slice, 'left', bsize, ovsize);
	      
	      out_slice = out(si:si + ovsize - 1, sj:ej, :);
	      errors = errors + calc_errors(im, out_slice, 'above', bsize, ovsize);
	      
	      % Remove the overlapping region in above and left cases
	      out_slice = out(si:si + ovsize - 1, sj:sj + ovsize - 1, :);
	      errors = errors - calc_errors(im, out_slice, 'corner', bsize, ovsize);
	    end
	    
	    % Find the match and its indicies
	    matches = find(errors(:) <= (1 + tolerance)*min(errors(:)));
	    match_index = matches(randi(length(matches)));
	    [im_r, im_c] = ind2sub(size(errors), match_index);
	    
	    % No cut version:
	    out(si:ei, sj:ej, :) = im(im_r:im_r + bsize - 1, im_c:im_c + bsize - 1, :);
	    
	     Boundary divides the sides of the min cut with 1s and 0s
	     boundary = ones(bsize, bsize);
	     /*
	     % Find the min cut
	     if i ~= 1
	      % Above overlap
	       im_overlap = im(im_r:im_r+ovsize - 1, im_c:im_c+bsize - 1, :);
	       out_overlap = out(si:si+ovsize - 1, sj:ej, :);
	       cut = dpcut(im_overlap, out_overlap, 'hori');
	       boundary(1:ovsize, 1:bsize) = double(cut >= 0);
	     end
	     
	     if j ~= 1
	       % Left overlap
	       im_overlap = im(im_r:im_r+bsize-1, im_c:im_c+ovsize-1, :);
	       out_overlap = out(si:ei, sj:sj+ovsize-1, :);
	       cut = dpcut(im_overlap, out_overlap, 'vert');
	       boundary(1:bsize, 1:ovsize) = boundary(1:bsize, 1:ovsize) .* double(cut >= 0);
	       end
	     
	     % Overlay the block onto the output image, cutting along the boundary
	     boundary = repmat(boundary, 1, 1, 3);
	     out(si:ei, sj:ej, :) = out(si:ei, sj:ej, :) .* (boundary == 0) + ...
	        im(im_r:im_r+bsize-1, im_c:im_c+bsize-1, :) .* (boundary == 1);
	     */
	  end
	  
	end

	imshow(uint8(out));
}
