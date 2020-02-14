% Demo to extract frames and get frame means from a movie and save individual frames to separate image files.
% Then rebuilds a new movie by recalling the saved images from disk.
% Also computes the mean gray value of the color channels
% And detects the difference between a frame and the previous frame.
% Illustrates the use of the VideoReader and VideoWriter classes.
% A Mathworks demo (different than mine) is located here http://www.mathworks.com/help/matlab/examples/convert-between-image-sequences-and-video.html

clc;    % Clear the command window.
close all;  % Close all figures (except those of imtool.)
imtool close all;  % Close all imtool figures.
clear;  % Erase all existing variables.
workspace;  % Make sure the workspace panel is showing.
fontSize = 16;

% Open the rhino.avi demo movie that ships with MATLAB.
% First get the folder that it lives in.
folder = fileparts(which('IMG-1675.MOV')); % Determine where demo folder is (works with all versions).
% Pick one of the two demo movies shipped with the Image Processing Toolbox.
% Comment out the other one.
movieFullFileName = fullfile(folder, 'IMG-1675.MOV');
% movieFullFileName = fullfile(folder, 'traffic.avi');
% Check to see that it exists.
if ~exist(movieFullFileName, 'file')
	strErrorMessage = sprintf('File not found:\n%s\nYou can choose a new one, or cancel', movieFullFileName);
	response = questdlg(strErrorMessage, 'File not found', 'OK - choose a new movie.', 'Cancel', 'OK - choose a new movie.');
	if strcmpi(response, 'OK - choose a new movie.')
		[baseFileName, folderName, FilterIndex] = uigetfile('*.avi');
		if ~isequal(baseFileName, 0)
			movieFullFileName = fullfile(folderName, baseFileName);
		else
			return;
		end
	else
		return;
	end
end


try
    tic;
    tstart = tic;
	videoObject = VideoReader(movieFullFileName)
	% Determine how many frames there are.
	numberOfFrames = videoObject.NumberOfFrames;
	vidHeight = videoObject.Height;
	vidWidth = videoObject.Width;
	
	numberOfFramesWritten = 0;
	% Prepare a figure to show the images in the upper half of the screen.
	figure;
	screenSize = get(0, 'ScreenSize');
	%Enlarge figure to full screen.
	set(gcf, 'units','normalized','outerposition',[0 0 1 1]);
	
	% Loop through the movie, writing all frames out.
	% Each frame will be in a separate file with unique name.
	meanGrayLevels = zeros(numberOfFrames, 1);
    
    horizontal_centroid = zeros(1,numberOfFrames);
    temp_movement = zeros(1,numberOfFrames);
    movement = zeros(1,numberOfFrames);
    sum = zeros(1,numberOfFrames);
    step = 1;
    
    BackgroundRef = read(videoObject,1);
    
	for frame = 1 : step : numberOfFrames
		% Extract the frame from the movie structure.
		thisFrame = read(videoObject, frame);

		% Display it
		hImage = subplot(2, 3, 1);
		image(thisFrame); 
        title('Original video', 'FontSize', fontSize);
		drawnow; % Force it to refresh the window.	
	
		% Now let's do the differencing
		alpha = 0.7;
        
		if frame == 1
            
			Background = thisFrame;
%             Background = rgb2gray(Background);
%             Background = imgaussfilt(Background);
       
		else
			% Change background slightly at each frame
			% 			Background(t+1)=(1-alpha)*I+alpha*Background
			Background = (1-alpha)* thisFrame + alpha * Background;
%             Background = rgb2gray(Background);
%             Background = imgaussfilt(Background);
		end
		% Display the changing/adapting background.
		subplot(2, 3, 2);
		imshow(Background);
		title('Adaptive Background', 'FontSize', fontSize);
		% Calculate a difference between this frame and the background.
        thisFrame = rgb2gray(thisFrame);
%         thisFrame = imgaussfilt(thisFrame);
		differenceImage = thisFrame - uint8(Background);
        
%         differenceImageRef = thisFrame - uint8(BackgroundRef);
        
		% Threshold with Otsu method.
		grayImage = rgb2gray(differenceImage); % Convert to gray level
		thresholdLevel = graythresh(grayImage); % Get threshold.
		binaryImage = im2bw( grayImage, thresholdLevel); % Do the binarization
        
        binaryImage = medfilt2(binaryImage);
        
% 		grayImageRef = rgb2gray(differenceImageRef); % Convert to gray level
% 		thresholdLevelRef = graythresh(grayImageRef); % Get threshold.
% %         thresholdLevelRef = 0.07;
% 		binaryImageRef = im2bw( grayImage, thresholdLevelRef); % Do the binarization
%         
%         binaryImageRef = medfilt2(binaryImageRef);        
        
        disk = strel('disk',3);
%         ball = strel('ball',5,5);
		% Plot the binary image.
		subplot(2, 3, 5);
		imshow(binaryImage);
%       binraryImage = imdilate(imerode(binaryImage,disk),disk);
        binaryImage = imdilate(imdilate(imdilate(imerode(binaryImage,disk),disk),disk),disk);
        
%         binaryImageRef = imdilate(imdilate(imdilate(imerode(binaryImageRef,disk),disk),disk),disk);
        
%         binaryImage =  imdilate(imdilate(imdilate(imdilate(binaryImage,disk),disk),disk),disk);
        
%       binaryImage = imdilate(imdilate(imdilate(imerode(binaryImage,ball),ball),ball),ball);
%         imshow(binaryImage);
% 		title('Binarized Difference Image', 'FontSize', fontSize);    
        
        [y,x] = ndgrid(1:size(binaryImage,1),1:size(binaryImage,2));
        centroid = mean([x(logical(binaryImage)),y(logical(binaryImage))]);
        
        % plot the coordination of x 
        hPlot = subplot(2, 3, 3);
		hold on;
        horizontal_centroid(frame) = centroid(1);
%         centroid(1)
%         
%         a = linspace(0,numberOfFrames);
        plot(horizontal_centroid,'b-', 'LineWidth',3);
        grid on;
        
        
        if frame == 1 || frame == (1+step) || frame == (1+2*step)
            previousValue = horizontal_centroid(1);
            prev2Value = horizontal_centroid(1);
            prev3Value = horizontal_centroid(1);   
        else
            previousValue = horizontal_centroid(frame - step);  
            prev2Value = horizontal_centroid(frame - 2*step);
            prev3Value = horizontal_centroid(frame - 3*step);             
        end
        
        currentValue = horizontal_centroid(frame);
        delta = currentValue - previousValue;
        delta2 = currentValue - prev2Value;
        delta3 = currentValue - prev3Value;
        
        if ((abs(delta) < 5) || (abs(delta2) < 5) || (abs(delta3) < 5))|| ((abs(delta) > 25*step) || (abs(delta2) > 50*step) || (abs(delta3) > 75*step)) 
            temp_movement(frame) = 0;
        else 
            temp_movement(frame) = delta;
        end
        
%         hPlot = subplot(2, 3, 4);
%         hold on;
%         a = linspace(1,numberOfFrames);
%         plot(temp_movement,'k-', 'LineWidth',2);        
%         imshow(binaryImageRef);
        
        
        if frame == 1 || frame == (1+step) || frame == (1+2*step)
            movement(frame) = temp_movement(frame);
        else
            if temp_movement(frame-step) == 0 || temp_movement(frame-2*step) == 0
                movement(frame) = 0;
            else
                movement(frame) = temp_movement(frame);
            end
        end
%         movement(frame)
        % plot the movement of object
        hPlot = subplot(2, 3, 6);
        hold on;
%         a = linspace(1,abs(numberOfFrames/2));
        plot(movement,'k-', 'LineWidth',2);
        
    end
    
    tprocess = toc(tstart)
	
catch ME
	% Some error happened if you get here.
	strErrorMessage = sprintf('Error extracting movie frames from:\n\n%s\n\nError: %s\n\n)', movieFullFileName, ME.message);
	uiwait(msgbox(strErrorMessage));
end

