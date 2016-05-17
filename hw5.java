/* Name: Derek Nguyen

   UID: 304275956

   Others With Whom I Discussed Things: Sung Hyun Yoon	904303999

   Other Resources I Consulted:
   
*/

import java.io.*;
import java.util.Arrays;
import java.lang.Math;

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
								 	Long roundedValue = Math.round((0.299 * rgb.R) + (0.587 * rgb.G) + (0.114 * rgb.B));	// safe?
								 	int newValue = roundedValue.intValue();
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
		throw new ImplementMe();
    }

	// implement using Java 8 Streams
    public PPMImage mirrorImage2() {
		throw new ImplementMe();
    }

	// implement using Java's Fork/Join library
    public PPMImage gaussianBlur(int radius, double sigma) {
		throw new ImplementMe();
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
		// test

		
	}
}
