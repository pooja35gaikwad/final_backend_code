package com.TWRSPROJ2_BE;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class CSVController {
	public Socket t;
	public double k;
	public BufferedWriter out;
	public BufferedReader in;
	String server = "192.168.2.10";
	int port = 21;
	String user = "anonymous";
	String pass = "123";
	public FTPClient ftpClient = new FTPClient();
	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 3027751179842530581L;
	/**
	 * The singular values array. Normal values of (S).
	 */
	protected double[] singularValues;
	/**
	 * The left singular vectors (U).
	 */
	protected double[][] leftSingularVectors;
	/**
	 * The right singular vectors (V).
	 */
	protected double[][] rightSingularVectors;
	/**
	 * The width of the matrix to decompose.
	 */
	protected int width;
	/**
	 * The height of the matrix to decompose.
	 */
	protected int height;
	// private static final DecimalFormat df = new
	// DecimalFormat(Double.parseDouble("00.0000"));

	@GetMapping(path = "/hello-world")
	public String helloWorld() {
		return "Hello world";
	}

	
	@GetMapping(path = "/vnaConnect")
	public void  connect()
	{
		
		String instrumentName = "192.168.2.10"; // Put instrument hostname here
		try
		 {
		  t= new Socket(instrumentName,5025);
		 
		  out =new BufferedWriter(new OutputStreamWriter(t.getOutputStream()));
		  in =new BufferedReader(
		 new InputStreamReader(t.getInputStream()));
		  out.write("CALC:FORMat POLar\n");
		  out.flush(); 
		  System.out.println("Waiting for source to settle...");
		  
		  out.write("*opc?\n"); // Waits for completion
		  out.flush();
		  String opcResponse = in.readLine();
		  if (!opcResponse.equals("1"))
		 {
		  System.err.println("Invalid response to '*OPC?'!");
		  System.exit(1);
		  
		 }
		  System.out.println("Retrieving instrument ID...");
		  out.write("*idn?\n"); 
		  out.flush();
		  out.write("*idn?\n");
		
		
		  
		  String idnResponse = in.readLine(); // Reads the id string
		  // Prints the id string
		  System.out.println("Instrument ID: " + idnResponse);
		 }
		 catch (IOException e)
		{
		 System.out.println("Error" + e);
		 JOptionPane.showMessageDialog(null, 
                    "Set IP 192.168.2.11 and Subnet 255.255.255.0", 
                    "VNA connection error", 
                    JOptionPane.WARNING_MESSAGE);
		 }
	    
		
		
	}
	
	@GetMapping(path = "/getCsv")
	public CSVDataBean getCsvData() throws IOException, IOException {
		CSVDataBean csvDataBean = new CSVDataBean();

		List<List<String>> records = new ArrayList<>();
		// Function to read CSV file from local
		try (BufferedReader br = new BufferedReader(
				// F:\DHANASHRI\Tracking\test1 21 Dec\HUMA WALKING straigh -8m
				// new FileReader("F:/DHANASHRI/Tracking/test1 21 Dec/HUMA WALKING straigh
				// -8m/File001.csv"))) {
				new FileReader(
						"C:\\Users\\Amitabh 1\\Desktop\\Try1"))) {
			String line = new String();

			while ((line = br.readLine()) != null) {
				// To remove unwanted data from CSV file
				if (!line.startsWith("!") && !line.contains("END") && !line.contains("BEGIN")) {
					String[] values = line.split(",");
					records.add(Arrays.asList(values));
				}
			}

			// created list according to the data types of Freq., Real and imaginary
			List<Long> freq9 = new ArrayList();
			List<Float> real9 = new ArrayList();
			List<Float> img9 = new ArrayList();
			List<Double> mag9 = new ArrayList();
			List<Double> fftMag9 = new ArrayList();

			for (List<String> list : records) {

				// Converted list of freq into Integer
				String freq1 = list.get(0);
				Long i = Long.parseLong(freq1);
				freq9.add(i);

				// Converted list of real into Integer
				String real1 = list.get(1);
				Float j = Float.valueOf(real1);
				real9.add(j);

				// Converted list of imaginary into Integer
				String img1 = list.get(2);
				Float k = Float.valueOf(img1);
				img9.add(k);

				Double magnitude = Math
						.sqrt(Float.valueOf(real1) * Float.valueOf(real1) + Float.valueOf(img1) * Float.valueOf(img1));
				mag9.add(magnitude);

			}

			Complex[] y = new Complex[freq9.size()];
			for (int z = 0; z < freq9.size(); z++) {

				Complex wk = new Complex(real9.get(z), img9.get(z));
				y[z] = wk;
			}
			int n = y.length;
			Complex[] result = FFT.dft(y);
			for (int p = 0; p < 201; p++) {
				fftMag9.add(result[p].abs());
			}
			csvDataBean.setFreq(freq9);
			csvDataBean.setReal(real9);
			csvDataBean.setImg(img9);
			csvDataBean.setMagnitude(mag9);
			csvDataBean.setFftMagnitude(fftMag9);

		}
		return csvDataBean;
	}

	@GetMapping(path = "/getMultipleCsv")
	public List<CSVDataBean> getMultipleCsvData() throws IOException, IOException {

		List<CSVDataBean> csvDataBeanList = new ArrayList();

		// String for source directory
		String srcDir = "F:\\DHANASHRI\\Tracking\\DATASETS\\Detection & Localization\\correlation dataset\\exp_R_2_A_0_R";
		File folder = new File(srcDir);
		double[][] BScan = new double[32][201];
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {

			if (file.isFile()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				{
					List<List<String>> records = new ArrayList<>();
					String line = new String();
					while ((line = br.readLine()) != null) {
						// To remove unwanted data from CSV file
						if (!line.startsWith("!") && !line.contains("END") && !line.contains("BEGIN")) {
							String[] values = line.split(",");
							records.add(Arrays.asList(values));
						}
					}

					List<Long> freq9 = new ArrayList();
					List<Float> real9 = new ArrayList();
					List<Float> img9 = new ArrayList();
					List<Double> mag9 = new ArrayList();
					List<Double> fftMag9 = new ArrayList();

					for (List<String> list : records) {

						// Converted list of freq into Integer
						String freq1 = list.get(0);
						Long i = Long.parseLong(freq1);
						freq9.add(i);

						// Converted list of real into Integer
						String real1 = list.get(1);
						Float j = Float.valueOf(real1);
						real9.add(j);

						// Converted list of imaginary into Integer
						String img1 = list.get(2);
						Float k = Float.valueOf(img1);
						img9.add(k);

						Double magnitude = Math.sqrt(Float.valueOf(real1) * Float.valueOf(real1)
								+ Float.valueOf(img1) * Float.valueOf(img1));
						mag9.add(magnitude);

					}

					Complex[] y = new Complex[freq9.size()];
					for (int z = 0; z < freq9.size(); z++) {

						Complex wk = new Complex(real9.get(z), img9.get(z));
						y[z] = wk;
					}
					int n = y.length;

					Complex[] result = FFT.dft(y);
					for (int p = 0; p < freq9.size(); p++) {
						fftMag9.add(result[p].abs());
					}
					CSVDataBean csvDataBean = new CSVDataBean();
					csvDataBean.setFreq(freq9);
					csvDataBean.setReal(real9);
					csvDataBean.setImg(img9);
					csvDataBean.setMagnitude(mag9);
					csvDataBean.setFftMagnitude(fftMag9);
					csvDataBeanList.add(csvDataBean);

				}

			}
		}

		return csvDataBeanList;

	}

	@GetMapping(path = "/getBScan")
	public double[][] getBScan() throws IOException, IOException {
		// Matrix of A-Sca stacking
		double[][] BScan = new double[32][201];

//		// Matrix to Image on UI panel i.e. Transpose of BScan Matrix
		double[][] transpose = new double[201][32];

		BScan = readMatrixFromPathAndGiveBscan();
		// Matrix of Mean
		double[][] MeanSum = new double[1][201];
		// Matrix of Ones
		double[][] OnesMatrix = new double[32][1];
		// Matrix of multiplication of Mean and Ones for reshaping(To obey matrix
		// multiplication law)
		double[][] MeanOnesMult = new double[32][201];
		// Mean subtracted BScan
		double[][] MeanSubtractedBScan = new double[32][201];

		// For loop to create Ones matrix
		for (double m = 0; m < 32; m++) {
			OnesMatrix[(int) m][0] = 1;
		}

		double rows = BScan.length;
		double cols = BScan[0].length;
		double average;

		// For loop to calculate mean columnwise
		for (double i = 0; i < cols; i++) {
			double sumCol = 0;
			for (double j = 0; j < rows; j++) {
				sumCol = sumCol + BScan[(int) j][(int) i];
			}
			average = sumCol / 32;
			MeanSum[0][(int) i] = average;
		}

		for (double m = 0; m < 32; m++) {
			for (double n = 0; n < 201; n++) {
				transpose[(int) n][(int) m] = BScan[(int) m][(int) n];
			}
		}

		return transpose;
	}

	@GetMapping(path = "/getMeanSubtractedBScan")
	public double[][] getMeanSubtractedBScan() throws IOException, IOException {

		// Matrix of A-Scan stacking
		double[][] BScan = new double[32][201];

		// Matrix to Image on UI panel i.e. Transpose of BScan Matrix
		double[][] transpose = new double[201][32];
		BScan = readMatrixFromPathAndGiveBscan();

		// Matrix of Mean
		double[][] MeanSum = new double[1][201];
		// Matrix of Ones
		double[][] OnesMatrix = new double[32][1];
		// Matrix of multiplication of Mean and Ones for reshaping(To obey matrix
		// multiplication law)
		double[][] MeanOnesMult = new double[32][201];
		// Mean subtracted BScan
		double[][] MeanSubtractedBScan = new double[32][201];

		// For loop to create Ones matrix
		for (double m = 0; m < 32; m++) {
			OnesMatrix[(int) m][0] = 1;
		}

		double rows = BScan.length;
		double cols = BScan[0].length;
		double average;

		// For loop to calculate mean columnwise
		for (double i = 0; i < cols; i++) {
			double sumCol = 0;
			for (double j = 0; j < rows; j++) {
				sumCol = sumCol + BScan[(int) j][(int) i];
			}
			average = sumCol / 32;
			MeanSum[0][(int) i] = average;
		}

		// For loop for multiplication of Mean and Ones for reshaping(To obey matrix
		// multiplication law)
		for (double i = 0; i < rows; i++) {
			for (double j = 0; j < cols; j++) {
				MeanOnesMult[(int) i][(int) j] = OnesMatrix[(int) i][0] * MeanSum[0][(int) j];
			}
		}

		// For loop to subtract Actual actual values from mean(Mean-Subtraction)
		for (double i = 0; i < rows; i++) {
			for (double j = 0; j < cols; j++) {
				MeanSubtractedBScan[(int) i][(int) j] = BScan[(int) i][(int) j] - MeanOnesMult[(int) i][(int) j];
			}
		}

		for (double m = 0; m < 32; m++) {
			for (double n = 0; n < 201; n++) {
				transpose[(int) n][(int) m] = MeanSubtractedBScan[(int) m][(int) n];
			}
		}

		return transpose;
	}

	@GetMapping(path = "/getRangeDopplerImage")
	public double[][] getRangeDopplerImage() throws IOException, IOException {

		// Matrix of A-Sca stacking
		double[][] BScan = new double[32][201];

		// Matrix to Image on UI panel i.e. Transpose of BScan Matrix
		double[][] transpose = new double[201][32];
		double[][] BScan_fft = new double[201][32];
		double[][] rangeDopplerMatrix = new double[201][32];

		BScan = readMatrixFromPathAndGiveBscan();

		// Matrix of Mean
		double[][] MeanSum = new double[1][201];
		// Matrix of Ones
		double[][] OnesMatrix = new double[32][1];
		// Matrix of multiplication of Mean and Ones for reshaping(To obey matrix
		// multiplication law)
		double[][] MeanOnesMult = new double[32][201];
		// Mean subtracted BScan
		double[][] MeanSubtractedBScan = new double[32][201];

		double[][] tt = new double[32][201];
		double[][] tt1 = new double[201][32];
		double[][] tt2 = new double[201][16];
		// For loop to create Ones matrix
		for (double m = 0; m < 32; m++) {
			OnesMatrix[(int) m][0] = 1;
		}

		double rows = BScan.length;
		double cols = BScan[0].length;
		double average;

		// For loop to calculate mean columnwise
		for (double i = 0; i < cols; i++) {
			double sumCol = 0;
			for (double j = 0; j < rows; j++) {
				sumCol = sumCol + BScan[(int) j][(int) i];
			}
			average = sumCol / 32;
			MeanSum[0][(int) i] = average;
		}

		// For loop for multiplication of Mean and Ones for reshaping(To obey matrix
		// multiplication law)
		for (double i = 0; i < rows; i++) {
			for (double j = 0; j < cols; j++) {
				MeanOnesMult[(int) i][(int) j] = OnesMatrix[(int) i][0] * MeanSum[0][(int) j];
			}
		}

		// For loop to subtract Actual actual values from mean(Mean-Subtraction)
		for (double i = 0; i < rows; i++) {
			for (double j = 0; j < cols; j++) {
				MeanSubtractedBScan[(int) i][(int) j] = BScan[(int) i][(int) j] - MeanOnesMult[(int) i][(int) j];
			}
		}

		for (double m = 0; m < 32; m++) {
			for (double n = 0; n < 201; n++) {
				transpose[(int) n][(int) m] = MeanSubtractedBScan[(int) m][(int) n];
			}
		}

		double rows1 = transpose.length;
		double cols1 = transpose[0].length;
		for (double i = 0; i < rows1; i++) {
			List<Double> aScan_list = new ArrayList();
			for (double j = 0; j < cols1; j++) {
				// MeanOnesMult[(int) i][(int) j] = OnesMatrix[(int) i][0] * MeanSum[0][(int)
				// j];

				aScan_list.add(transpose[(int) i][(int) j]);
			}
			Complex[] y1 = new Complex[aScan_list.size()];
			// For loop to generate Complex Numbers
			for (int z = 0; z < aScan_list.size(); z++) {

				Complex wk = new Complex(aScan_list.get(z), 0);
				y1[z] = wk;
			}
			int n = y1.length;

			Complex[] result = FFT.dft(y1);
			for (double j = 0; j < result.length; j++) {
				BScan_fft[(int) i][(int) j] = result[(int) j].abs();
			}
		}

		for (double m = 0; m < 32; m++) {
			for (double n = 0; n < 201; n++) {
				tt[(int) m][(int) n] = BScan_fft[(int) n][(int) m];
			}
		}

		for (double m = 0; m < 32; m++) {
			for (double n = 0; n < 201; n++) {
				tt1[(int) n][(int) m] = tt[(int) m][(int) n];
			}
		}

		for (double m = 0; m < 16; m++) {
			for (double n = 0; n < 201; n++) {
				tt2[(int) n][(int) m] = tt1[(int) n][(int) m];
			}
		}
		return tt2;

	}

	@PostMapping(path = "/setParameters")
	public FreqParameters setParameters(@RequestBody FreqParameters freqParams ) throws IOException, IOException {
		
		FreqParameters freqParams1 = new FreqParameters();
		freqParams1.setStartFreq(freqParams.getStartFreq());
		freqParams1.setStopFreq(freqParams.getStopFreq());
		freqParams1.setNumberOfpoints(freqParams.getNumberOfpoints());
			
		
		 double start=freqParams1.getStartFreq();

		
		 double stop=freqParams1.getStopFreq();
		 
		 double points=freqParams1.getNumberOfpoints();
       
		 String instrumentName = "192.168.2.10"; // Put instrument hostname here
		try
		 {
			
		 try (Socket t = new Socket(instrumentName,5025)) {
		     out =new BufferedWriter(new OutputStreamWriter(t.getOutputStream()));
			 in =new BufferedReader(
			 new InputStreamReader(t.getInputStream()));
			 //start freq
			 String s1= "FREQ:STAR ";
			 double Startfreq=start*Math.pow(10, 9);
			 String s2=String.valueOf(Startfreq);       //.................................get start freq from user        
			 String s3=s1.concat(s2);
			 String s4=" HZ\n";
			 String s5=s3.concat(s4);
			 System.out.println(s5); 
				//out.write("FREQ:STAR 1e9 HZ\n");
			 out.write(s5);
			//*******Stop frequency
			String s6= "FREQ:STOP ";
				
			double Stopfreq=stop*Math.pow(10, 9);
				
			String s7=String.valueOf(Stopfreq);         //.................................get stop freq from user   
			String s8=s6.concat(s7);
			String s9=" HZ\n";
			String s10=s8.concat(s9);
				 //out.write("FREQ:STOP 9e9 HZ\n");
			out.write(s10);
				 
				//*******Freq  points
			String p="SWE:POIN ";
			String p3="\n";
			String p4= String.valueOf(points); 
			String p1=p4.concat(p3);
				 //String p1="401\n";    //.................................get freq point from user
			String p2=p.concat(p1);
			System.out.println(p2); 	
 //out.write("SWE:POIN 101\n");
			out.write(p2);
			
			//out.write("SWE:POIN 501\n");
			//out.write("CALC:FORMat POLar\n");
			out.write("CALC:PAR1:DEF S21\n");
			
			
			
			out.flush(); 
			  System.out.println("Waiting for source to settle...");
			  
			  out.write("*opc?\n"); // Waits for completion
			  out.flush();
			  String opcResponse = in.readLine();
			  if (!opcResponse.equals("1"))
			 {
			  System.err.println("Invalid response to '*OPC?'!");
			  System.exit(1);
			 }
			  System.out.println("Retrieving instrument ID...");
			  out.write("*idn?\n"); 
			  out.flush();
			  out.write("*idn?\n");
			  out.write("*idn?\n");
			  out.write("*idn?\n");
			  out.write("*idn?\n");
			  out.write("*idn?\n");
			  
			  
			  
			  String idnResponse = in.readLine(); // Reads the id string
			  // Prints the id string
			  System.out.println("Instrument ID: " + idnResponse);
		}
		 
		 
		 }
		 catch (IOException e)
			{
			 System.out.println("Error" + e);
			 }
		return freqParams1;
	}

	private double[][] readMatrixFromPathAndGiveBscan() throws IOException {

		List<CSVDataBean> csvDataBeanList = new ArrayList();

		// String for source directory
		String srcDir = "C:\\Users\\Amitabh 1\\Desktop\\Try1";
		File folder = new File(srcDir);

		// Matrix of A-Sca stacking
		double[][] BScan = new double[32][201];

		// Matrix to Image on UI panel i.e. Transpose of BScan Matrix
		double[][] transpose = new double[201][32];
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {

			if (file.isFile()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				{
					List<List<String>> records = new ArrayList<>();
					String line = new String();
					while ((line = br.readLine()) != null) {
						// To remove unwanted data from CSV file
						if (!line.startsWith("!") && !line.contains("END") && !line.contains("BEGIN")) {
							String[] values = line.split(",");
							records.add(Arrays.asList(values));
						}
					}

					List<Long> freq9 = new ArrayList();
					List<Float> real9 = new ArrayList();
					List<Float> img9 = new ArrayList();
					List<Double> mag9 = new ArrayList();
					List<Double> fftMag9 = new ArrayList();
					List<Double> meanArray = new ArrayList();
					Double dummy;
					for (List<String> list : records) {

						// Converted list of freq into Integer
						String freq1 = list.get(0);
						Long i = Long.parseLong(freq1);
						freq9.add(i);

						// Converted list of real into Integer
						String real1 = list.get(1);
						Float j = Float.valueOf(real1);
						real9.add(j);

						// Converted list of imaginary into Integer
						String img1 = list.get(2);
						Float k = Float.valueOf(img1);
						img9.add(k);

						Double magnitude = Math.sqrt(Float.valueOf(real1) * Float.valueOf(real1)
								+ Float.valueOf(img1) * Float.valueOf(img1));
						mag9.add(magnitude);

					}

					Complex[] y = new Complex[freq9.size()];
					// For loop to generate Complex Numbers
					for (int z = 0; z < freq9.size(); z++) {

						Complex wk = new Complex(real9.get(z), img9.get(z));
						y[z] = wk;
					}
					int n = y.length;

					Complex[] result = FFT.dft(y);

					// For loop to save absolute of FFT
					for (int p = 0; p < freq9.size(); p++) {
						fftMag9.add(result[p].abs());
					}
					CSVDataBean csvDataBean = new CSVDataBean();
					csvDataBean.setFreq(freq9);
					csvDataBean.setReal(real9);
					csvDataBean.setImg(img9);
					csvDataBean.setMagnitude(mag9);
					csvDataBean.setFftMagnitude(fftMag9);
					csvDataBeanList.add(csvDataBean);

				}

			}
		}

		// For loop to store A-Scan in matrix i.e. B-Scan
		for (double j = 0; j < 32; j++) {
			for (double i = 0; i < 201; i++) {

				CSVDataBean cSVDataBean = csvDataBeanList.get((int) j);
				double d = cSVDataBean.getFftMagnitude().get((int) i);
				double roundOff = Math.round(d * 10000.0) / 10000.0;
				BScan[(int) j][(int) i] = roundOff;

			}
		}
		return BScan;
	}

	@GetMapping(path = "/getTargetFromSVD")
	public double[][] getTargetFromSVD() throws IOException {
		// Matrix of A-Scan stacking
		double[][] BScan = new double[32][201];
		double[][] SVD = new double[32][201];
		double[][] transpose = new double[201][32];
		BScan = readMatrixFromPathAndGiveBscan();
		for (double m = 0; m < 32; m++) {
			for (double n = 0; n < 201; n++) {
				transpose[(int) n][(int) m] = BScan[(int) m][(int) n];
			}
		}

		height = transpose.length;
		width = transpose[0].length;
		double[][] diagonalMatrix = new double[width][width];
		int nu = Math.min(height, width);
		singularValues = new double[Math.min(height + 1, width)];
		leftSingularVectors = new double[height][nu];
		rightSingularVectors = new double[width][width];
		double[] e = new double[width];
		double[] work = new double[height];
		boolean wantu = true;
		boolean wantv = true;

		int nct = Math.min(height - 1, width);
		int nrt = Math.max(0, Math.min(width - 2, height));
		for (int k = 0; k < Math.max(nct, nrt); k++) {
			if (k < nct) {
				// Compute the transformation for the k-th column and
				// place the k-th diagonal in s[k].
				// Compute 2-norm of k-th column without under/overflow.
				singularValues[k] = 0;
				for (int i = k; i < height; i++)
					singularValues[k] = MathUtils.hypot(singularValues[k], transpose[i][k]);
				if (singularValues[k] != 0.0) {
					if (transpose[k][k] < 0.0)
						singularValues[k] = -singularValues[k];
					for (int i = k; i < height; i++)
						transpose[i][k] /= singularValues[k];
					transpose[k][k] += 1.0;
				}
				singularValues[k] = -singularValues[k];
			}
			for (int j = k + 1; j < width; j++) {
				if (k < nct & singularValues[k] != 0.0) {
					// Apply the transformation.
					double t = 0;
					for (int i = k; i < height; i++)
						t += transpose[i][k] * transpose[i][j];
					t = -t / transpose[k][k];
					for (int i = k; i < height; i++)
						transpose[i][j] += t * transpose[i][k];
				}
				// Place the k-th row of A into e for the
				// subsequent calculation of the row transformation.
				e[j] = transpose[k][j];
			}
			if (wantu & k < nct)
				// Place the transformation in U for subsequent back
				// multiplication.
				for (int i = k; i < height; i++)
					leftSingularVectors[i][k] = transpose[i][k];
			if (k < nrt) {
				// Compute the k-th row transformation and place the
				// k-th super-diagonal in e[k].
				// Compute 2-norm without under/overflow.
				e[k] = 0;
				for (int i = k + 1; i < width; i++)
					e[k] = MathUtils.hypot(e[k], e[i]);
				if (e[k] != 0.0) {
					if (e[k + 1] < 0.0)
						e[k] = -e[k];
					for (int i = k + 1; i < width; i++)
						e[i] /= e[k];
					e[k + 1] += 1.0;
				}
				e[k] = -e[k];
				if (k + 1 < height & e[k] != 0.0) {
					// Apply the transformation.
					for (int i = k + 1; i < height; i++)
						work[i] = 0.0;
					for (int j = k + 1; j < width; j++)
						for (int i = k + 1; i < height; i++)
							work[i] += e[j] * transpose[i][j];
					for (int j = k + 1; j < width; j++) {
						double t = -e[j] / e[k + 1];
						for (int i = k + 1; i < height; i++)
							transpose[i][j] += t * work[i];
					}
				}
				if (wantv)
					// Place the transformation in V for subsequent
					// back multiplication.
					for (int i = k + 1; i < width; i++)
						rightSingularVectors[i][k] = e[i];
			}
		}
		// Set up the final bidiagonal matrix or order p.
		int p = Math.min(width, height + 1);
		if (nct < width)
			singularValues[nct] = transpose[nct][nct];
		if (height < p)
			singularValues[p - 1] = 0.0;
		if (nrt + 1 < p)
			e[nrt] = transpose[nrt][p - 1];
		e[p - 1] = 0.0;
		// If required, generate U.
		if (wantu) {
			for (int j = nct; j < nu; j++) {
				for (int i = 0; i < height; i++)
					leftSingularVectors[i][j] = 0.0;
				leftSingularVectors[j][j] = 1.0;
			}
			for (int k = nct - 1; k >= 0; k--)
				if (singularValues[k] != 0.0) {
					for (int j = k + 1; j < nu; j++) {
						double t = 0;
						for (int i = k; i < height; i++)
							t += leftSingularVectors[i][k] * leftSingularVectors[i][j];
						t = -t / leftSingularVectors[k][k];
						for (int i = k; i < height; i++)
							leftSingularVectors[i][j] += t * leftSingularVectors[i][k];
					}
					for (int i = k; i < height; i++)
						leftSingularVectors[i][k] = -leftSingularVectors[i][k];
					leftSingularVectors[k][k] = 1.0 + leftSingularVectors[k][k];
					for (int i = 0; i < k - 1; i++)
						leftSingularVectors[i][k] = 0.0;
				} else {
					for (int i = 0; i < height; i++)
						leftSingularVectors[i][k] = 0.0;
					leftSingularVectors[k][k] = 1.0;
				}
		}
		// If required, generate V.
		if (wantv)
			for (int k = width - 1; k >= 0; k--) {
				if (k < nrt & e[k] != 0.0)
					for (int j = k + 1; j < nu; j++) {
						double t = 0;
						for (int i = k + 1; i < width; i++)
							t += rightSingularVectors[i][k] * rightSingularVectors[i][j];
						t = -t / rightSingularVectors[k + 1][k];
						for (int i = k + 1; i < width; i++)
							rightSingularVectors[i][j] += t * rightSingularVectors[i][k];
					}
				for (int i = 0; i < width; i++)
					rightSingularVectors[i][k] = 0.0;
				rightSingularVectors[k][k] = 1.0;
			}

		// Main iteration loop for the singular values.
		int pp = p - 1;
		int iter = 0;
		double eps = Math.pow(2.0, -52.0);
		double tiny = Math.pow(2.0, -966.0);
		while (p > 0) {
			int k, kase;
			// Here is where a test for too many iterations would go.
			// This section of the program inspects for
			// negligible elements in the s and e arrays. On
			// completion the variables kase and k are set as follows.
			// kase = 1 if s(p) and e[k-1] are negligible and k<p
			// kase = 2 if s(k) is negligible and k<p
			// kase = 3 if e[k-1] is negligible, k<p, and
			// s(k), ..., s(p) are not negligible (qr step).
			// kase = 4 if e(p-1) is negligible (convergence).
			for (k = p - 2; k >= -1; k--) {
				if (k == -1)
					break;
				if (Math.abs(e[k]) <= tiny + eps * (Math.abs(singularValues[k]) + Math.abs(singularValues[k + 1]))) {
					e[k] = 0.0;
					break;
				}
			}
			if (k == p - 2)
				kase = 4;
			else {
				int ks;
				for (ks = p - 1; ks >= k; ks--) {
					if (ks == k)
						break;
					double t = (ks != p ? Math.abs(e[ks]) : 0.) + (ks != k + 1 ? Math.abs(e[ks - 1]) : 0.);
					if (Math.abs(singularValues[ks]) <= tiny + eps * t) {
						singularValues[ks] = 0.0;
						break;
					}
				}
				if (ks == k)
					kase = 3;
				else if (ks == p - 1)
					kase = 1;
				else {
					kase = 2;
					k = ks;
				}
			}
			k++;
			// Perform the task indicated by kase.
			switch (kase) {
			// Deflate negligible s(p).
			case 1: {
				double f = e[p - 2];
				e[p - 2] = 0.0;
				for (int j = p - 2; j >= k; j--) {
					double t = MathUtils.hypot(singularValues[j], f);
					double cs = singularValues[j] / t;
					double sn = f / t;
					singularValues[j] = t;
					if (j != k) {
						f = -sn * e[j - 1];
						e[j - 1] = cs * e[j - 1];
					}
					if (wantv)
						for (int i = 0; i < width; i++) {
							t = cs * rightSingularVectors[i][j] + sn * rightSingularVectors[i][p - 1];
							rightSingularVectors[i][p - 1] = -sn * rightSingularVectors[i][j]
									+ cs * rightSingularVectors[i][p - 1];
							rightSingularVectors[i][j] = t;
						}
				}
			}
				break;
			// Split at negligible s(k).
			case 2: {
				double f = e[k - 1];
				e[k - 1] = 0.0;
				for (int j = k; j < p; j++) {
					double t = MathUtils.hypot(singularValues[j], f);
					double cs = singularValues[j] / t;
					double sn = f / t;
					singularValues[j] = t;
					f = -sn * e[j];
					e[j] = cs * e[j];
					if (wantu)
						for (int i = 0; i < height; i++) {
							t = cs * leftSingularVectors[i][j] + sn * leftSingularVectors[i][k - 1];
							leftSingularVectors[i][k - 1] = -sn * leftSingularVectors[i][j]
									+ cs * leftSingularVectors[i][k - 1];
							leftSingularVectors[i][j] = t;
						}
				}
			}
				break;
			// Perform one qr step.
			case 3: {
				// Calculate the shift.
				double scale = Math.max(
						Math.max(Math.max(Math.max(Math.abs(singularValues[p - 1]), Math.abs(singularValues[p - 2])),
								Math.abs(e[p - 2])), Math.abs(singularValues[k])),
						Math.abs(e[k]));
				double sp = singularValues[p - 1] / scale;
				double spm1 = singularValues[p - 2] / scale;
				double epm1 = e[p - 2] / scale;
				double sk = singularValues[k] / scale;
				double ek = e[k] / scale;
				double b = ((spm1 + sp) * (spm1 - sp) + epm1 * epm1) / 2.0;
				double c = sp * epm1 * (sp * epm1);
				double shift = 0.0;
				if (b != 0.0 | c != 0.0) {
					shift = Math.sqrt(b * b + c);
					if (b < 0.0)
						shift = -shift;
					shift = c / (b + shift);
				}
				double f = (sk + sp) * (sk - sp) + shift;
				double g = sk * ek;
				// Chase zeros.
				for (int j = k; j < p - 1; j++) {
					double t = MathUtils.hypot(f, g);
					double cs = f / t;
					double sn = g / t;
					if (j != k)
						e[j - 1] = t;
					f = cs * singularValues[j] + sn * e[j];
					e[j] = cs * e[j] - sn * singularValues[j];
					g = sn * singularValues[j + 1];
					singularValues[j + 1] = cs * singularValues[j + 1];
					if (wantv)
						for (int i = 0; i < width; i++) {
							t = cs * rightSingularVectors[i][j] + sn * rightSingularVectors[i][j + 1];
							rightSingularVectors[i][j + 1] = -sn * rightSingularVectors[i][j]
									+ cs * rightSingularVectors[i][j + 1];
							rightSingularVectors[i][j] = t;
						}
					t = MathUtils.hypot(f, g);
					cs = f / t;
					sn = g / t;
					singularValues[j] = t;
					f = cs * e[j] + sn * singularValues[j + 1];
					singularValues[j + 1] = -sn * e[j] + cs * singularValues[j + 1];
					g = sn * e[j + 1];
					e[j + 1] = cs * e[j + 1];
					if (wantu && j < height - 1)
						for (int i = 0; i < height; i++) {
							t = cs * leftSingularVectors[i][j] + sn * leftSingularVectors[i][j + 1];
							leftSingularVectors[i][j + 1] = -sn * leftSingularVectors[i][j]
									+ cs * leftSingularVectors[i][j + 1];
							leftSingularVectors[i][j] = t;
						}
				}
				e[p - 2] = f;
				iter = iter + 1;
			}
				break;
			// Convergence.
			case 4: {
				// Make the singular values positive.
				if (singularValues[k] <= 0.0) {
					singularValues[k] = singularValues[k] < 0.0 ? -singularValues[k] : 0.0;
					if (wantv)
						for (int i = 0; i <= pp; i++)
							rightSingularVectors[i][k] = -rightSingularVectors[i][k];
				}
				// Order the singular values.
				while (k < pp) {
					if (singularValues[k] >= singularValues[k + 1])
						break;
					double t = singularValues[k];
					singularValues[k] = singularValues[k + 1];
					singularValues[k + 1] = t;
					if (wantv && k < width - 1)
						for (int i = 0; i < width; i++) {
							t = rightSingularVectors[i][k + 1];
							rightSingularVectors[i][k + 1] = rightSingularVectors[i][k];
							rightSingularVectors[i][k] = t;
						}
					if (wantu && k < height - 1)
						for (int i = 0; i < height; i++) {
							t = leftSingularVectors[i][k + 1];
							leftSingularVectors[i][k + 1] = leftSingularVectors[i][k];
							leftSingularVectors[i][k] = t;
						}
					k++;
				}
				iter = 0;
				p--;
			}
				break;
			}

			for (int i = 0; i < width; i++) {
				for (int j = 0; j < width; j++)
					diagonalMatrix[i][j] = 0.0;
				diagonalMatrix[i][i] = this.singularValues[i];
			}
		}
		Object[] u2 = new Object[leftSingularVectors.length];
		for (int r = 0; r < u2.length; r++) {
			u2[r] = leftSingularVectors[r][1];
		}
		Object[] v2 = new Object[rightSingularVectors[0].length];
		for (int r = 0; r < v2.length; r++) {
			v2[r] = rightSingularVectors[1][r];
		}

		double[][] uComponent = new double[201][1];
		double[][] vComponent = new double[1][32];
		for (double j = 0; j < 200; j++) {
			uComponent[(int) j][0] = (double) u2[(int) j];
		}
		for (double j = 0; j < 31; j++) {
			vComponent[0][(int) j] = (double) v2[(int) j];
		}

		double[][] svdTargetMultiplication = new double[201][32];
		double[][] targetMatrix = new double[201][32];
		for (int i = 0; i < 201; i++) {
			for (int j = 0; j < 32; j++) {
				for (int k = 0; k < 1; k++) {
					svdTargetMultiplication[i][j] += uComponent[i][k] * vComponent[k][j];
				}
			}
		}

		for (int i = 0; i < svdTargetMultiplication.length; i++) {
			for (int j = 0; j < svdTargetMultiplication[0].length; j++) {
				targetMatrix[i][j] = singularValues[1] * svdTargetMultiplication[i][j];
			}

		}

		return targetMatrix;
	}

	@GetMapping(path = "/getClutterFromSVD")
	public double[][] getClutterFromSVD() throws IOException {
		// Matrix of A-Scan stacking
		double[][] BScan = new double[32][201];
		double[][] SVD = new double[32][201];
		double[][] transpose = new double[201][32];
		BScan = readMatrixFromPathAndGiveBscan();
		for (double m = 0; m < 32; m++) {
			for (double n = 0; n < 201; n++) {
				transpose[(int) n][(int) m] = BScan[(int) m][(int) n];
			}
		}

		height = transpose.length;
		width = transpose[0].length;
		double[][] diagonalMatrix = new double[width][width];
		int nu = Math.min(height, width);
		singularValues = new double[Math.min(height + 1, width)];
		leftSingularVectors = new double[height][nu];
		rightSingularVectors = new double[width][width];
		double[] e = new double[width];
		double[] work = new double[height];
		boolean wantu = true;
		boolean wantv = true;

		int nct = Math.min(height - 1, width);
		int nrt = Math.max(0, Math.min(width - 2, height));
		for (int k = 0; k < Math.max(nct, nrt); k++) {
			if (k < nct) {
				// Compute the transformation for the k-th column and
				// place the k-th diagonal in s[k].
				// Compute 2-norm of k-th column without under/overflow.
				singularValues[k] = 0;
				for (int i = k; i < height; i++)
					singularValues[k] = MathUtils.hypot(singularValues[k], transpose[i][k]);
				if (singularValues[k] != 0.0) {
					if (transpose[k][k] < 0.0)
						singularValues[k] = -singularValues[k];
					for (int i = k; i < height; i++)
						transpose[i][k] /= singularValues[k];
					transpose[k][k] += 1.0;
				}
				singularValues[k] = -singularValues[k];
			}
			for (int j = k + 1; j < width; j++) {
				if (k < nct & singularValues[k] != 0.0) {
					// Apply the transformation.
					double t = 0;
					for (int i = k; i < height; i++)
						t += transpose[i][k] * transpose[i][j];
					t = -t / transpose[k][k];
					for (int i = k; i < height; i++)
						transpose[i][j] += t * transpose[i][k];
				}
				// Place the k-th row of A into e for the
				// subsequent calculation of the row transformation.
				e[j] = transpose[k][j];
			}
			if (wantu & k < nct)
				// Place the transformation in U for subsequent back
				// multiplication.
				for (int i = k; i < height; i++)
					leftSingularVectors[i][k] = transpose[i][k];
			if (k < nrt) {
				// Compute the k-th row transformation and place the
				// k-th super-diagonal in e[k].
				// Compute 2-norm without under/overflow.
				e[k] = 0;
				for (int i = k + 1; i < width; i++)
					e[k] = MathUtils.hypot(e[k], e[i]);
				if (e[k] != 0.0) {
					if (e[k + 1] < 0.0)
						e[k] = -e[k];
					for (int i = k + 1; i < width; i++)
						e[i] /= e[k];
					e[k + 1] += 1.0;
				}
				e[k] = -e[k];
				if (k + 1 < height & e[k] != 0.0) {
					// Apply the transformation.
					for (int i = k + 1; i < height; i++)
						work[i] = 0.0;
					for (int j = k + 1; j < width; j++)
						for (int i = k + 1; i < height; i++)
							work[i] += e[j] * transpose[i][j];
					for (int j = k + 1; j < width; j++) {
						double t = -e[j] / e[k + 1];
						for (int i = k + 1; i < height; i++)
							transpose[i][j] += t * work[i];
					}
				}
				if (wantv)
					// Place the transformation in V for subsequent
					// back multiplication.
					for (int i = k + 1; i < width; i++)
						rightSingularVectors[i][k] = e[i];
			}
		}
		// Set up the final bidiagonal matrix or order p.
		int p = Math.min(width, height + 1);
		if (nct < width)
			singularValues[nct] = transpose[nct][nct];
		if (height < p)
			singularValues[p - 1] = 0.0;
		if (nrt + 1 < p)
			e[nrt] = transpose[nrt][p - 1];
		e[p - 1] = 0.0;
		// If required, generate U.
		if (wantu) {
			for (int j = nct; j < nu; j++) {
				for (int i = 0; i < height; i++)
					leftSingularVectors[i][j] = 0.0;
				leftSingularVectors[j][j] = 1.0;
			}
			for (int k = nct - 1; k >= 0; k--)
				if (singularValues[k] != 0.0) {
					for (int j = k + 1; j < nu; j++) {
						double t = 0;
						for (int i = k; i < height; i++)
							t += leftSingularVectors[i][k] * leftSingularVectors[i][j];
						t = -t / leftSingularVectors[k][k];
						for (int i = k; i < height; i++)
							leftSingularVectors[i][j] += t * leftSingularVectors[i][k];
					}
					for (int i = k; i < height; i++)
						leftSingularVectors[i][k] = -leftSingularVectors[i][k];
					leftSingularVectors[k][k] = 1.0 + leftSingularVectors[k][k];
					for (int i = 0; i < k - 1; i++)
						leftSingularVectors[i][k] = 0.0;
				} else {
					for (int i = 0; i < height; i++)
						leftSingularVectors[i][k] = 0.0;
					leftSingularVectors[k][k] = 1.0;
				}
		}
		// If required, generate V.
		if (wantv)
			for (int k = width - 1; k >= 0; k--) {
				if (k < nrt & e[k] != 0.0)
					for (int j = k + 1; j < nu; j++) {
						double t = 0;
						for (int i = k + 1; i < width; i++)
							t += rightSingularVectors[i][k] * rightSingularVectors[i][j];
						t = -t / rightSingularVectors[k + 1][k];
						for (int i = k + 1; i < width; i++)
							rightSingularVectors[i][j] += t * rightSingularVectors[i][k];
					}
				for (int i = 0; i < width; i++)
					rightSingularVectors[i][k] = 0.0;
				rightSingularVectors[k][k] = 1.0;
			}

		// Main iteration loop for the singular values.
		int pp = p - 1;
		int iter = 0;
		double eps = Math.pow(2.0, -52.0);
		double tiny = Math.pow(2.0, -966.0);
		while (p > 0) {
			int k, kase;
			// Here is where a test for too many iterations would go.
			// This section of the program inspects for
			// negligible elements in the s and e arrays. On
			// completion the variables kase and k are set as follows.
			// kase = 1 if s(p) and e[k-1] are negligible and k<p
			// kase = 2 if s(k) is negligible and k<p
			// kase = 3 if e[k-1] is negligible, k<p, and
			// s(k), ..., s(p) are not negligible (qr step).
			// kase = 4 if e(p-1) is negligible (convergence).
			for (k = p - 2; k >= -1; k--) {
				if (k == -1)
					break;
				if (Math.abs(e[k]) <= tiny + eps * (Math.abs(singularValues[k]) + Math.abs(singularValues[k + 1]))) {
					e[k] = 0.0;
					break;
				}
			}
			if (k == p - 2)
				kase = 4;
			else {
				int ks;
				for (ks = p - 1; ks >= k; ks--) {
					if (ks == k)
						break;
					double t = (ks != p ? Math.abs(e[ks]) : 0.) + (ks != k + 1 ? Math.abs(e[ks - 1]) : 0.);
					if (Math.abs(singularValues[ks]) <= tiny + eps * t) {
						singularValues[ks] = 0.0;
						break;
					}
				}
				if (ks == k)
					kase = 3;
				else if (ks == p - 1)
					kase = 1;
				else {
					kase = 2;
					k = ks;
				}
			}
			k++;
			// Perform the task indicated by kase.
			switch (kase) {
			// Deflate negligible s(p).
			case 1: {
				double f = e[p - 2];
				e[p - 2] = 0.0;
				for (int j = p - 2; j >= k; j--) {
					double t = MathUtils.hypot(singularValues[j], f);
					double cs = singularValues[j] / t;
					double sn = f / t;
					singularValues[j] = t;
					if (j != k) {
						f = -sn * e[j - 1];
						e[j - 1] = cs * e[j - 1];
					}
					if (wantv)
						for (int i = 0; i < width; i++) {
							t = cs * rightSingularVectors[i][j] + sn * rightSingularVectors[i][p - 1];
							rightSingularVectors[i][p - 1] = -sn * rightSingularVectors[i][j]
									+ cs * rightSingularVectors[i][p - 1];
							rightSingularVectors[i][j] = t;
						}
				}
			}
				break;
			// Split at negligible s(k).
			case 2: {
				double f = e[k - 1];
				e[k - 1] = 0.0;
				for (int j = k; j < p; j++) {
					double t = MathUtils.hypot(singularValues[j], f);
					double cs = singularValues[j] / t;
					double sn = f / t;
					singularValues[j] = t;
					f = -sn * e[j];
					e[j] = cs * e[j];
					if (wantu)
						for (int i = 0; i < height; i++) {
							t = cs * leftSingularVectors[i][j] + sn * leftSingularVectors[i][k - 1];
							leftSingularVectors[i][k - 1] = -sn * leftSingularVectors[i][j]
									+ cs * leftSingularVectors[i][k - 1];
							leftSingularVectors[i][j] = t;
						}
				}
			}
				break;
			// Perform one qr step.
			case 3: {
				// Calculate the shift.
				double scale = Math.max(
						Math.max(Math.max(Math.max(Math.abs(singularValues[p - 1]), Math.abs(singularValues[p - 2])),
								Math.abs(e[p - 2])), Math.abs(singularValues[k])),
						Math.abs(e[k]));
				double sp = singularValues[p - 1] / scale;
				double spm1 = singularValues[p - 2] / scale;
				double epm1 = e[p - 2] / scale;
				double sk = singularValues[k] / scale;
				double ek = e[k] / scale;
				double b = ((spm1 + sp) * (spm1 - sp) + epm1 * epm1) / 2.0;
				double c = sp * epm1 * (sp * epm1);
				double shift = 0.0;
				if (b != 0.0 | c != 0.0) {
					shift = Math.sqrt(b * b + c);
					if (b < 0.0)
						shift = -shift;
					shift = c / (b + shift);
				}
				double f = (sk + sp) * (sk - sp) + shift;
				double g = sk * ek;
				// Chase zeros.
				for (int j = k; j < p - 1; j++) {
					double t = MathUtils.hypot(f, g);
					double cs = f / t;
					double sn = g / t;
					if (j != k)
						e[j - 1] = t;
					f = cs * singularValues[j] + sn * e[j];
					e[j] = cs * e[j] - sn * singularValues[j];
					g = sn * singularValues[j + 1];
					singularValues[j + 1] = cs * singularValues[j + 1];
					if (wantv)
						for (int i = 0; i < width; i++) {
							t = cs * rightSingularVectors[i][j] + sn * rightSingularVectors[i][j + 1];
							rightSingularVectors[i][j + 1] = -sn * rightSingularVectors[i][j]
									+ cs * rightSingularVectors[i][j + 1];
							rightSingularVectors[i][j] = t;
						}
					t = MathUtils.hypot(f, g);
					cs = f / t;
					sn = g / t;
					singularValues[j] = t;
					f = cs * e[j] + sn * singularValues[j + 1];
					singularValues[j + 1] = -sn * e[j] + cs * singularValues[j + 1];
					g = sn * e[j + 1];
					e[j + 1] = cs * e[j + 1];
					if (wantu && j < height - 1)
						for (int i = 0; i < height; i++) {
							t = cs * leftSingularVectors[i][j] + sn * leftSingularVectors[i][j + 1];
							leftSingularVectors[i][j + 1] = -sn * leftSingularVectors[i][j]
									+ cs * leftSingularVectors[i][j + 1];
							leftSingularVectors[i][j] = t;
						}
				}
				e[p - 2] = f;
				iter = iter + 1;
			}
				break;
			// Convergence.
			case 4: {
				// Make the singular values positive.
				if (singularValues[k] <= 0.0) {
					singularValues[k] = singularValues[k] < 0.0 ? -singularValues[k] : 0.0;
					if (wantv)
						for (int i = 0; i <= pp; i++)
							rightSingularVectors[i][k] = -rightSingularVectors[i][k];
				}
				// Order the singular values.
				while (k < pp) {
					if (singularValues[k] >= singularValues[k + 1])
						break;
					double t = singularValues[k];
					singularValues[k] = singularValues[k + 1];
					singularValues[k + 1] = t;
					if (wantv && k < width - 1)
						for (int i = 0; i < width; i++) {
							t = rightSingularVectors[i][k + 1];
							rightSingularVectors[i][k + 1] = rightSingularVectors[i][k];
							rightSingularVectors[i][k] = t;
						}
					if (wantu && k < height - 1)
						for (int i = 0; i < height; i++) {
							t = leftSingularVectors[i][k + 1];
							leftSingularVectors[i][k + 1] = leftSingularVectors[i][k];
							leftSingularVectors[i][k] = t;
						}
					k++;
				}
				iter = 0;
				p--;
			}
				break;
			}

			for (int i = 0; i < width; i++) {
				for (int j = 0; j < width; j++)
					diagonalMatrix[i][j] = 0.0;
				diagonalMatrix[i][i] = this.singularValues[i];
			}
		}
		Object[] u2 = new Object[leftSingularVectors.length];
		for (int r = 0; r < u2.length; r++) {
			u2[r] = leftSingularVectors[r][0];
		}
		Object[] v2 = new Object[rightSingularVectors[0].length];
		for (int r = 0; r < v2.length; r++) {
			v2[r] = rightSingularVectors[0][r];
		}

		double[][] uComponent = new double[201][1];
		double[][] vComponent = new double[1][32];
		for (double j = 0; j < 200; j++) {
			uComponent[(int) j][0] = (double) u2[(int) j];
		}
		for (double j = 0; j < 31; j++) {
			vComponent[0][(int) j] = (double) v2[(int) j];
		}

		double[][] svdTargetMultiplication = new double[201][32];
		double[][] targetMatrix = new double[201][32];
		for (int i = 0; i < 201; i++) {
			for (int j = 0; j < 32; j++) {
				for (int k = 0; k < 1; k++) {
					svdTargetMultiplication[i][j] += uComponent[i][k] * vComponent[k][j];
				}
			}
		}

		for (int i = 0; i < svdTargetMultiplication.length; i++) {
			for (int j = 0; j < svdTargetMultiplication[0].length; j++) {
				targetMatrix[i][j] = singularValues[0] * svdTargetMultiplication[i][j];
			}

		}

		return targetMatrix;
	}

	@GetMapping(path = "/getStDeviation")
	public List<Double> getStDeviation() throws IOException, IOException {

		// Matrix of A-Scan stacking
		double[][] BScan = new double[32][201];

		// Matrix to Image on UI panel i.e. Transpose of BScan Matrix
		double[][] transpose = new double[201][32];
		BScan = readMatrixFromPathAndGiveBscan();

		// Matrix of Mean
		double[][] MeanSum = new double[1][201];
		// Matrix of Ones
		double[][] OnesMatrix = new double[32][1];
		// Matrix of multiplication of Mean and Ones for reshaping(To obey matrix
		// multiplication law)
		double[][] MeanOnesMult = new double[32][201];
		// Mean subtracted BScan
		double[][] MeanSubtractedBScan = new double[32][201];

		// For loop to create Ones matrix
		for (double m = 0; m < 32; m++) {
			OnesMatrix[(int) m][0] = 1;
		}

		double rows = BScan.length;
		double cols = BScan[0].length;
		double average;

		// For loop to calculate mean columnwise
		for (double i = 0; i < cols; i++) {
			double sumCol = 0;
			for (double j = 0; j < rows; j++) {
				sumCol = sumCol + BScan[(int) j][(int) i];
			}
			average = sumCol / 32;
			MeanSum[0][(int) i] = average;
		}

		// For loop for multiplication of Mean and Ones for reshaping(To obey matrix
		// multiplication law)
		for (double i = 0; i < rows; i++) {
			for (double j = 0; j < cols; j++) {
				MeanOnesMult[(int) i][(int) j] = OnesMatrix[(int) i][0] * MeanSum[0][(int) j];
			}
		}

		// For loop to subtract Actual actual values from mean(Mean-Subtraction)
		for (double i = 0; i < rows; i++) {
			for (double j = 0; j < cols; j++) {
				MeanSubtractedBScan[(int) i][(int) j] = BScan[(int) i][(int) j] - MeanOnesMult[(int) i][(int) j];
			}
		}

		// for (int r = 0; r < 32; r++) {

		List<Double> stDeviations = new ArrayList<Double>();
		// List<Long> list = new ArrayList<Long>();
		for (int k = 0; k < 201; k++) {
			List<Double> eachColumn = new ArrayList<Double>();
			for (int r = 0; r < 32; r++) {
				eachColumn.add(MeanSubtractedBScan[r][k]);
			}
			// double[] numArray = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
			// Double[] numArray = eachColumn.toArray();
			Double[] numArray = (Double[]) eachColumn.toArray(new Double[eachColumn.size()]);
			double sum = 0.0, standardDeviation = 0.0;
			int length = numArray.length;

			for (double num : numArray) {
				sum += num;
			}

			double mean = sum / length;

			for (double num : numArray) {
				standardDeviation += Math.pow(num - mean, 2);
			}

			double std = Math.sqrt(standardDeviation / length);
			stDeviations.add(std);
			// System.out.print(svd);
		}

		return stDeviations;
	}

	@PostMapping(path = "/upload")
	public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("files") MultipartFile files) {
		String message = "";
		try {
			List<String> fileNames = new ArrayList<>();
			Arrays.asList(files).stream().forEach(file -> {
				//storageService.save(file);
				fileNames.add(file.getOriginalFilename());
			});
			//String fileName = StringUtils.cleanPath(file.getOriginalFilename());
			  message = "Uploaded the files successfully: " + fileNames;
		      return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
		    } catch (Exception e) {
		      message = "Fail to upload files!";
		      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
		    }
	}
	
	@PostMapping(path = "/saveData")
    public Getnumber setfileno(@RequestBody Getnumber filenumber ) throws IOException, IOException {
	
		//connect();
		Getnumber getno = new Getnumber();
		getno.setfileno(filenumber.getfileno());
		int fileno=getno.getfileno();
		System.out.println(fileno);
       int i=fileno;
    
      
		//**************************************VNA FTP COMMANDS
	//	String server = "192.168.2.10";
      // int port = 21;
       //String user = "anonymous";
       //String pass = "123";
    
       String s2;       
		String s4;
		String s5;
		String s6;
		String s8;
		String s7 = null;
		if(i ==1) {
			ftpClient.connect(server, port);
   		ftpClient.login(user, pass);
          ftpClient.enterLocalPassiveMode();
          ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        
	       	 
       	}
       try {

       	  Socket t = new Socket("192.168.2.10",5025);
             BufferedWriter out =new BufferedWriter(new OutputStreamWriter(t.getOutputStream()));
  	       	 BufferedReader in =new BufferedReader(
  	      	new InputStreamReader(t.getInputStream()));
       	  
       	
        
	       
    
           if(i<10)
           {
           s2="File00";       
			 s4=Integer.toString(i);
			 s5=s2.concat(s4);
			 s6=".csv";
			 s7=s5.concat(s6);
			//System.out.print(s7);
           }
           else if(i>=10 || i<=99)
           {
           	
            s2="File0";       
   			 s4=Integer.toString(i);
   			 s5=s2.concat(s4);
   			 s6=".csv";
   			 s7=s5.concat(s6);
           	
           }
           else if(i>=100)
           {

           	 s2="File";       
   			 s4=Integer.toString(i);
   			 s5=s2.concat(s4);
   			 s6=".csv";
   			 s7=s5.concat(s6);
   			System.out.print(s7);
           }
           
           //filenames[i]=s7;
          String s22="MMEM:STOR:FDAT ";       
          String s44="\"";
          String s55=s22.concat(s44);
          String s66=s55.concat(s7);
          String s88="\"\n";
           String rootpath=s66.concat(s88);
       

          out.write(rootpath);
        
    
          Thread.sleep(150);
    
	      out.flush(); 
       System.out.println("Waiting for source to settle...");
       
       out.write("*opc?\n"); // Waits for completion
       out.flush();
       String opcResponse = in.readLine();
       if (!opcResponse.equals("1"))
      {
       System.err.println("Invalid response to '*OPC?'!");
       System.exit(1);
      }
       System.out.println("Retrieving instrument ID...");
       out.write("*idn?\n"); 
       out.flush();
       out.write("*idn?\n");
       out.write("*idn?\n");   
       out.write("*idn?\n");
       out.write("*idn?\n");
       out.write("*idn?\n");
      
       String idnResponse = in.readLine(); // Reads the id string
       // Prints the id string
       System.out.println("Instrument ID: " + idnResponse);
	      
      
      //file transfer
         
       String rootpath1="/USERDATA/";
       String remoteFile1=rootpath1.concat(s7);  
       String folder="TryMe";  //**************************************CREATE NEW FOLDER
       String folderpath = "C:/Software/"+folder;
       getno.setFolderPath(folderpath);
       File f1 = new File(folderpath); 
       System.out.println(f1.mkdir());
       String pc=folderpath;
       String pcpath=pc+"/";
       String pcFile1 =pcpath.concat(s7);
       System.out.print(pcFile1);
      
       File downloadFile1 = new File(pcFile1);
       OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
       
       boolean success = ftpClient.retrieveFile(remoteFile1, outputStream1);
       
       outputStream1.flush();
       
       
       if (success) {
           System.out.println("File #1 has been downloaded successfully.");
       }
       
      
        
       }     
       
       catch (IOException ex) {
           System.out.println("Error: " + ex.getMessage());
           ex.printStackTrace();
       } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
       
       
		
		
		return getno;
	
	}
	

	@GetMapping("/files")
	public ResponseEntity<List<FileInfo>> getListFiles() {
//		List<FileInfo> fileInfos = storageService.loadAll().map(path -> {
//			String filename = path.getFileName().toString();
//			String url = MvcUriComponentsBuilder
//					.fromMethodName(FilesController.class, "getFile", path.getFileName().toString()).build().toString();
//			return new FileInfo(filename, url);
//		}).collect(Collectors.toList());
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}
}
