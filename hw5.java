/* Name: Derek Nguyen

   UID: 304275956

   Others With Whom I Discussed Things: Sung Hyun Yoon	904303999

   Other Resources I Consulted:
   http://www.deadcoderising.com/2015-05-19-java-8-replace-traditional-for-loops-with-intstreams/
   
*/

import java.io.*;
import java.util.Arrays;
import java.lang.Math;
import java.util.concurrent.*;
import java.util.stream.IntStream;

// a marker for code that you need to implement
class ImplementMe extends RuntimeException {}

// an RGB triple
class RGB {
    public int R, G, B;

    RGB(int r, int g, int b) {
    	R = r;
		G = g;
		B = b;
    }

    public String toString() { return "(" + R + "," + G + "," + B + ")"; }

}


// an object representing a single PPM image
class PPMImage {
    protected int width, height, maxColorVal;
    protected RGB[] pixels;

    public PPMImage(int w, int h, int m, RGB[] p) {
		width = w;
		height = h;
		maxColorVal = m;
		pixels = p;
    }

    // parse a PPM image file named fname and produce a new PPMImage object
    public PPMImage(String fname) 
    	throws FileNotFoundException, IOException {
		FileInputStream is = new FileInputStream(fname);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		br.readLine(); // read the P6
		String[] dims = br.readLine().split(" "); // read width and height
		int width = Integer.parseInt(dims[0]);
		int height = Integer.parseInt(dims[1]);
		int max = Integer.parseInt(br.readLine()); // read max color value
		br.close();

		is = new FileInputStream(fname);
	    // skip the first three lines
		int newlines = 0;
		while (newlines < 3) {
	    	int b = is.read();
	    	if (b == 10)
				newlines++;
		}

		int MASK = 0xff;
		int numpixels = width * height;
		byte[] bytes = new byte[numpixels * 3];
        is.read(bytes);
		RGB[] pixels = new RGB[numpixels];
		for (int i = 0; i < numpixels; i++) {
	    	int offset = i * 3;
	    	pixels[i] = new RGB(bytes[offset] & MASK, 
	    						bytes[offset+1] & MASK, 
	    						bytes[offset+2] & MASK);
		}
		is.close();

		this.width = width;
		this.height = height;
		this.maxColorVal = max;
		this.pixels = pixels;
    }

	// write a PPMImage object to a file named fname
    public void toFile(String fname) throws IOException {
		FileOutputStream os = new FileOutputStream(fname);

		String header = "P6\n" + width + " " + height + "\n" 
						+ maxColorVal + "\n";
		os.write(header.getBytes());

		int numpixels = width * height;
		byte[] bytes = new byte[numpixels * 3];
		int i = 0;
		for (RGB rgb : pixels) {
	    	bytes[i] = (byte) rgb.R;
	    	bytes[i+1] = (byte) rgb.G;
	    	bytes[i+2] = (byte) rgb.B;
	    	i += 3;
		}
		os.write(bytes);
		os.close();
    }

	// implement using Java 8 Streams
    public PPMImage negate() {
		RGB[] negated = Arrays.stream(pixels)
		 					  .parallel()		
							  .map(rgb -> {		// change each rgb value to negate image
								  return new RGB(this.maxColorVal - rgb.R, this.maxColorVal - rgb.G, this.maxColorVal - rgb.B);
							  })
							  .toArray(RGB[]::new);
		return new PPMImage(this.width, this.height, this.maxColorVal, negated);	// construct a new PPMImage
    }

	// implement using Java 8 Streams
    public PPMImage greyscale() {
		RGB[] greyscaled = Arrays.stream(pixels)
								 .parallel()
								 .map(rgb -> {	// compute new pixel value and change each RGB
								 	int newValue = Math.round((0.299f * rgb.R) + (0.587f * rgb.G) + (0.114f * rgb.B));
								 	return new RGB(newValue, newValue, newValue);
								 })
								 .toArray(RGB[]::new);
		return new PPMImage(this.width, this.height, this.maxColorVal, greyscaled);	// construct a new PPMImage
    }    
    
	// implement using Java's Fork/Join library
    public PPMImage mirrorImage() {
    	// create new RGB array to represent the mirrored image by using compute() of MirrorImage
    	RGB[] mirrored = new RGB[this.height * this.width];
		MirrorImage img = new MirrorImage(mirrored, pixels, 0, this.height, this.width);
		img.compute();
		return new PPMImage(this.width, this.height, this.maxColorVal, mirrored);
    }

	// implement using Java 8 Streams
    public PPMImage mirrorImage2() {
		RGB[] mirrored = IntStream.range(0, width*height)
								  .parallel()
								  .mapToObj(i -> {				// simulate swapping by finding the corresponding RGB object in pixels
								      return pixels[((i/width) + 1) * width - (i % width) - 1];
								  })
								  .toArray(RGB[]::new);
		return new PPMImage(this.width, this.height, this.maxColorVal, mirrored);
    }

	// implement using Java's Fork/Join library
    public PPMImage gaussianBlur(int radius, double sigma) {
    	// create new RGB array to represent the blurred image by using compute() of GaussianBlur
		RGB[] blurred = new RGB[this.height * this.width];
		Gaussian gaussian = new Gaussian();
		double[][] filter = gaussian.gaussianFilter(radius, sigma);
		for (int i = 0; i < filter.length; i++)
			for (int j = 0; j < filter.length; j++)
				System.out.println(filter[i][j]);
		GaussianBlur img = new GaussianBlur(blurred, pixels, 0, this.height, this.width, radius, filter);
		img.compute();
		return new PPMImage(this.width, this.height, this.maxColorVal, blurred);
    }
    /*
    public boolean equals(PPMImage test) {
    	return Arrays.equals(this.pixels, test.pixels) && this.width == test.width && this.height == test.height && this.maxColorVal == test.maxColorVal;
    }
	*/
}

class MirrorImage extends RecursiveAction {
	private final int SEQUENTIAL_CUTOFF = 5;
	private RGB[] mirrored, pixels;
	private int top, width, height;

	public MirrorImage(RGB[] mirrored, RGB[] pixels, int top, int height, int width) {
		this.mirrored = mirrored;
		this.pixels = pixels;
		this.top = top;
		this.height = height;
		this.width = width;
	}
	
	public void compute() {
		int mid = height / 2;
		if (mid > SEQUENTIAL_CUTOFF) {		// recursively divide image in half to parallelize swapping
			MirrorImage upper = new MirrorImage(mirrored, pixels, top, mid, width);
			MirrorImage lower;
			if (height % 2 == 0)			// even number of pixels
				lower = new MirrorImage(mirrored, pixels, top + mid, mid, width);
			else							// odd number of pixels so add one to height
				lower = new MirrorImage(mirrored, pixels, top + mid, mid + 1, width);
			upper.fork();					// swap upper section in parallel
			lower.compute();
			upper.join();
		} else {
			// swap left half with right half of pixels for each row
			for (int i = top; i < top + height; i++)
				for (int j = 0; j < width; j++)
					mirrored[(i * width) + j] = pixels[(i * width) + width - j - 1];
		}
	}
}

class GaussianBlur extends RecursiveAction {
	private final int SEQUENTIAL_CUTOFF = 5;
	private RGB[] blurred, pixels;
	private int top, width, height, radius;
	private double[][] filter;

	public GaussianBlur(RGB[] blurred, RGB[] pixels, int top, int height, int width, int radius, double[][] filter) {
		this.blurred = blurred;
		this.pixels = pixels;
		this.top = top;
		this.height = height;
		this.width = width;
		this.radius = radius;
		this.filter = filter;
	}

	public void compute() {
		int mid = height / 2;
		if (mid > SEQUENTIAL_CUTOFF) {		// recursively divide image in half to parallelize blurring
			GaussianBlur upper = new GaussianBlur(blurred, pixels, top, mid, width, radius, filter);
			GaussianBlur lower;
			if (height % 2 == 0)			// even number of pixels
				lower = new GaussianBlur(blurred, pixels, top + mid, mid, width, radius, filter);
			else							// odd number of pixels so add one to height
				lower = new GaussianBlur(blurred, pixels, top + mid, mid + 1, width, radius, filter);
			upper.fork();					// swap upper section in parallel
			lower.compute();
			upper.join();
		} else {				
			// blur each pixel using gaussianFilter
			int fullHeight = pixels.length / width;

			// use the Gaussian blur algorithm to change the RGB value of each pixel in the image
			for (int i = top; i < top + height; i++) {
				for (int j = 0; j < width; j++) {
					RGB current = pixels[(i * width) + j];
					double currentR = 0.0;
					double currentG = 0.0;
					double currentB = 0.0;

					// use "clamping semantics" to find the right pixel to multiply with the gaussianFilter
					for (int k = 0; k < filter.length; k++) {
						for (int l = 0; l < filter.length; l++) {
							int row = i - radius + k;
							int column = j - radius + l;
							/*
							if ((i - radius + k) < 0 && (j - radius + l) < 0)
								pixelIndex = 0;
							else if ((i - radius + k) < 0 && (j - radius + l) >= width)
								pixelIndex = width - 1;
							else if ((i - radius + k) >= fullHeight && (j - radius + l) >= width)
								pixelIndex = pixels.length - 1;
							else if ((j - radius + l) < 0 && (i - radius + k) >= fullHeight)
								pixelIndex = (fullHeight - 1) * width;
							else if ((i - radius + k) < 0)
								pixelIndex = j - radius + l;
							else if ((j - radius + l) < 0)
								pixelIndex = (i - radius + k) * width;
							else if ((i - radius + k) >= fullHeight)
								pixelIndex = ((fullHeight - 1) * width) + j - radius + l;
							else if ((j - radius + l) >= width)
								pixelIndex = ((i - radius + k) * width) + (width - 1);
							else
								pixelIndex = ((i - radius + k) * width) + j - radius + l;
							*/
							// check if row is out of bounds
							if (row < 0)
								row = 0;
							else if (row >= fullHeight)
								row = fullHeight - 1;
							// check if column is out of bounds
							if (column < 0)
								column = 0;
							else if (column >= width)
								column = width - 1;

							int pixelIndex = (width * row) + column;

							currentR += filter[k][l] * pixels[pixelIndex].R;
							currentG += filter[k][l] * pixels[pixelIndex].G;
							currentB += filter[k][l] * pixels[pixelIndex].B;
						}
					}
					current.R = Math.round((float)currentR);
					current.G = Math.round((float)currentG);
					current.B = Math.round((float)currentB);
					blurred[(i*width) + j] = current;
				}
			}
		}
	}
}

// code for creating a Gaussian filter
class Gaussian {

    protected static double gaussian(int x, int mu, double sigma) {
		return Math.exp( -(Math.pow((x-mu)/sigma,2.0))/2.0 );
    }

    public static double[][] gaussianFilter(int radius, double sigma) {
		int length = 2 * radius + 1;
		double[] hkernel = new double[length];
		for(int i=0; i < length; i++)
	    	hkernel[i] = gaussian(i, radius, sigma);
		double[][] kernel2d = new double[length][length];
		double kernelsum = 0.0;
		for(int i=0; i < length; i++) {
	    	for(int j=0; j < length; j++) {
				double elem = hkernel[i] * hkernel[j];
				kernelsum += elem;
				kernel2d[i][j] = elem;
	    	}
		}
		for(int i=0; i < length; i++) {
	    	for(int j=0; j < length; j++)
				kernel2d[i][j] /= kernelsum;
		}
		return kernel2d;
    }
}

// testing purposes
class Test {
	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		
		PPMImage orig = new PPMImage("florence.ppm");
		
		// test negated image
		//RGB[] testNegated = {new RGB(228, 160, 149), new RGB(252, 184, 173), new RGB(41, 55, 55),
		//			 		 new RGB(124, 56, 45), new RGB(154, 86, 75), new RGB(192, 206, 206),
		//			 		 new RGB(92, 217, 255), new RGB(18, 143, 225), new RGB(0, 255, 49)};
		//PPMImage test = new PPMImage(3, 3, 255, testNegated);
		PPMImage negated = orig.negate();
		//assert(negated.equals(test));
		negated.toFile("negated.ppm");

		// test greyscaled image
		//RGB[] testGreyscale = {new RGB(76, 76, 76), new RGB(52, 52, 52), new RGB(204, 204, 204),
		//			 		   new RGB(180, 180, 180), new RGB(150, 150, 150), new RGB(53, 53, 53),
		//			 		   new RGB(71, 71, 71), new RGB(140, 140, 140), new RGB(100, 100, 100)};
		//est = new PPMImage(3, 3, 255, testGreyscale);
		PPMImage greyscaled = orig.greyscale();
		//assert(greyscaled.equals(test));
		greyscaled.toFile("greyscaled.ppm");

		//assert(orig.equals(greyscaled));
		
		// test mirrorImage
		//RGB[] testMirrored = {new RGB(214, 200, 200), new RGB(3, 71, 82), new RGB(27, 95, 106),
		//			 		  new RGB(63, 49, 49), new RGB(101, 169, 180), new RGB(131, 199, 210),
		//			 		  new RGB(255, 0, 206), new RGB(237, 112, 30), new RGB(163, 38, 0)};
		//test = new PPMImage(3, 3, 255, testMirrored);
		PPMImage mirrored = orig.mirrorImage();
		//assert(mirrored.equals(test));
		mirrored.toFile("mirrored.ppm");
		
		// test mirrorImage2
		mirrored = orig.mirrorImage2();
		//assert(mirrored.equals(test));
		mirrored.toFile("mirrored2.ppm");	
		
		// test gaussianBlur		
		orig.gaussianBlur(1, 2.0).toFile("blurred.ppm");
		
		long stopTime = System.currentTimeMillis();
		System.out.println(stopTime - startTime);
	}
}
