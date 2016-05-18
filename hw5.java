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
								  rgb.R = this.maxColorVal - rgb.R;
								  rgb.G = this.maxColorVal - rgb.G;	
								  rgb.B = this.maxColorVal - rgb.B;
								  return rgb;
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
								 	rgb.R = newValue;
								 	rgb.G = newValue;
								 	rgb.B = newValue;
								 	return rgb;
								 })
								 .toArray(RGB[]::new);
		return new PPMImage(this.width, this.height, this.maxColorVal, greyscaled);	// construct a new PPMImage
    }    
    
	// implement using Java's Fork/Join library
    public PPMImage mirrorImage() {
    	RGB[] mirrored = new RGB[this.height * this.width];
		MirrorImage img = new MirrorImage(mirrored, pixels, 0, this.height, this.width);
		img.compute();
		return new PPMImage(this.width, this.height, this.maxColorVal, mirrored);
    }

	// implement using Java 8 Streams
    public PPMImage mirrorImage2() {
		RGB[] mirrored = IntStream.range(0, width*height)
								  .mapToObj(i -> {		// simulate swapping by finding the corresponding RGB object in pixels
								      return pixels[((i/width) + 1) * width - (i % width) - 1];
								  })
								  .toArray(RGB[]::new);
		return new PPMImage(this.width, this.height, this.maxColorVal, mirrored);
    }

	// implement using Java's Fork/Join library
    public PPMImage gaussianBlur(int radius, double sigma) {
		//RGB[] blurred = (new GaussianBlur(pixels, 0, this.height, this.width, radius, sigma)).compute();
		//return new PPMImage(this.width, this.height, this.maxColorVal, blurred);
    	throw new ImplementMe();
    }

}

class MirrorImage extends RecursiveAction {
	private final int SEQUENTIAL_CUTOFF = 100;
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
			if (height % 2 == 0)	// even number of pixels
				lower = new MirrorImage(mirrored, pixels, top + mid, mid, width);
			else					// odd number of pixels so add one to height
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
/*
class MirrorImage extends RecursiveTask<RGB[]> {
	private final int SEQUENTIAL_CUTOFF = 200;
	private RGB[] pixels;
	private int top, width, height;

	public MirrorImage(RGB[] pixels, int top, int height, int width) {
		this.pixels = pixels;
		this.top = top;
		this.height = height;
		this.width = width;
	}
	
	public RGB[] compute() {
		int mid = height / 2;
		if (mid > SEQUENTIAL_CUTOFF) {		// recursively divide image in half to parallelize swapping
			MirrorImage upper = new MirrorImage(pixels, top, mid, width);
			MirrorImage lower;
			if (height % 2 == 0)	// even number of pixels
				lower = new MirrorImage(pixels, top + mid, mid, width);
			else					// odd number of pixels so add one to height
				lower = new MirrorImage(pixels, top + mid, mid + 1, width);
			upper.fork();					// swap upper section in parallel
			RGB[] lowerSwapped = lower.compute();
			RGB[] upperSwapped = upper.join();

			// reassemble swapped RGB array
			RGB[] swapped = new RGB[height * width];
			int upperLen = upperSwapped.length;
			for (int i = 0; i < upperSwapped.length; i++)
				swapped[i] = upperSwapped[i];
			for (int j = upperLen; j < lowerSwapped.length + upperLen; j++)
				swapped[j] = lowerSwapped[j - upperLen];
			return swapped;
		} else { 						// swap left half with right half
			RGB[] swapped = new RGB[width * height];
			// swap each row of pixels
			for (int i = 0; i < height; i++)
				for (int j = 0; j < width; j++)
					swapped[(i*width) + j] = pixels[((i + top) * width) + width - j - 1];
			return swapped;
		}
	}
} */
/*
class GaussianBlur extends RecursiveTask<RGB[]> {
	private final int SEQUENTIAL_CUTOFF = 300;
	private RGB[] pixels;
	private int top, width, height, radius;
	private double sigma;

	public GaussianBlur(RGB[] pixels, int top, int height, int width, int radius, double sigma) {
		this.pixels = pixels;
		this.top = top;
		this.height = height;
		this.width = width;
		this.radius = radius;
		this.sigma = sigma;
	}

	public RGB[] compute() {
		int mid = height / 2;
		if (mid > SEQUENTIAL_CUTOFF) {
			GaussianBlur upper = new GaussianBlur(pixels, top, mid, width, radius, sigma);
			GaussianBlur lower;
			if (height % 2 == 0)	// even number of pixels
				lower = new GaussianBlur(pixels, top + mid, mid, width, radius, sigma);
			else					// odd number of pixels so add one to height
				lower = new GaussianBlur(pixels, top + mid, mid + 1, width, radius, sigma);
			upper.fork();					// swap upper section in parallel
			RGB[] lowerBlurred = lower.compute();
			RGB[] upperBlurred = upper.join();

			// reassemble blurred RGB array
			RGB[] blurred = new RGB[height * width];
			int upperLen = upperBlurred.length;
			for (int i = 0; i < upperBlurred.length; i++)
				blurred[i] = upperBlurred[i];
			for (int j = upperLen; j < lowerBlurred.length + upperLen; j++)
				blurred[j] = lowerBlurred[j - upperLen];
			return blurred;
		} else {				// blur each pixel using gaussianFilter
			RGB[] blurred = new RGB[width * height];
			Gaussian gaussian = new Gaussian();
			double[][] filter = gaussian.gaussianFilter(radius, sigma);
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					RGB current = pixels[(i*width) + j];
					Long currentR, currentG, currentB;
					for (int k = 0; k < filter.length; k++) {
						for (int l = 0; l < filter.length; l++) {
							int pixelIndex = ((i - radius + k) * width) + j - radius + l;
							currentR += filter[k][l] * pixels[pixelIndex].R;
							currentG += filter[k][l] * pixels[pixelIndex].G;
							currentB += filter[k][l] * pixels[pixelIndex].B;
						}
					}
					/*
					Long currentR = Math.round((filter[0][0] * pixels[((i-1)*width) + j - 1].R) + (filter[0][1] * pixels[((i-1)*width) + j].R)
											+ (filter[0][2] * pixels[((i-1)*width) + j + 1].R) + (filter[1][0] * pixels[(i*width) + j -1].R)
											+ (filter[1][1] * pixels[(i*width) + j].R) + (filter[1][2] * pixels[(i*width) + j + 1].R)
											+ (filter[2][0] * pixels[((i+1)*width) + j - 1].R) + (filter[2][1] * pixels[((i+1)*width) + j].R)
											+ (filter[2][2] * pixels[((i+1)*width) + j + 1].R));
					Long currentG = Math.round((filter[0][0] * pixels[((i-1)*width) + j - 1].G) + (filter[0][1] * pixels[((i-1)*width) + j].G)
											+ (filter[0][2] * pixels[((i-1)*width) + j + 1].G) + (filter[1][0] * pixels[(i*width) + j - 1].G)
											+ (filter[1][1] * pixels[(i*width) + j].G) + (filter[1][2] * pixels[(i*width) + j + 1].G)
											+ (filter[2][0] * pixels[((i+1)*width) + j - 1].G) + (filter[2][1] * pixels[((i+1)*width) + j].G)
											+ (filter[2][2] * pixels[((i+1)*width) + j + 1].G));
					Long currentB = Math.round((filter[0][0] * pixels[((i-1)*width) + j - 1].B) + (filter[0][1] * pixels[((i-1)*width) + j].B)
											+ (filter[0][2] * pixels[((i-1)*width) + j + 1].B) + (filter[1][0] * pixels[(i*width) + j -1].B)
											+ (filter[1][1] * pixels[(i*width) + j].B) + (filter[1][2] * pixels[(i*width) + j + 1].B)
											+ (filter[2][0] * pixels[((i+1)*width) + j - 1].B) + (filter[2][1] * pixels[((i+1)*width) + j].B)
											+ (filter[2][2] * pixels[((i+1)*width) + j + 1].B));
					
					current.R = Math.round(currentR).intValue();
					current.G = Math.round(currentG).intValue();
					current.B = Math.round(currentB).intValue();
					blurred[(i*width) + j] = current;
				}
			}
			return blurred;
		}
	}
}
*/
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
		PPMImage orig = new PPMImage("florence.ppm");

		// test negated image
		/*
		PPMImage negated = orig.negate();
		negated.toFile("negated.ppm");
		*/
		// test greyscaled image
		/*
		PPMImage greyscaled = orig.greyscale();
		greyscaled.toFile("greyscaled.ppm");
		*/
		// test mirrorImage
		
		PPMImage mirrored = orig.mirrorImage();
		mirrored.toFile("mirrored.ppm");
		
		// test mirrorImage2
		/*
		PPMImage mirrored = orig.mirrorImage2();
		mirrored.toFile("mirrored.ppm");
		*/
		// test gaussianBlur
		/*
		PPMImage blurred = orig.gaussianBlur(1, 2.0);
		blurred.toFile("blurred.ppm");
		*/
	}
}
