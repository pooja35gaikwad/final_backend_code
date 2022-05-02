package com.TWRSPROJ2_BE;

public class FFT {

	// compute the DFT of x[] via brute force (n^2 time)
	public static Complex[] dft(Complex[] x) {
		int n = x.length;
		Complex ZERO = new Complex(0, 0);
		Complex[] y = new Complex[n];
		for (int k = 0; k < n; k++) {
			y[k] = ZERO;
			for (int j = 0; j < n; j++) {
				int power = (k * j) % n;
				double kth = 2 * power * Math.PI / n;
				Complex wkj = new Complex(Math.cos(kth), Math.sin(kth));
				y[k] = y[k].plus(x[j].times(wkj));
			}
		}
		return y;
	}

	public static Complex[] fft(Complex[] x) {
		int n = x.length;
		// compute FFT of even terms
		Complex[] even = new Complex[n / 2];
		for (int k = 0; k < n / 2; k++) {
			even[k] = x[2 * k];
		}
		Complex[] evenFFT = fft(even);

		// compute FFT of odd terms
		Complex[] odd = even; // reuse the array (to avoid n log n space)
		for (int k = 0; k < n / 2; k++) {
			odd[k] = x[2 * k + 1];
		}
		Complex[] oddFFT = fft(odd);

		// combine
		Complex[] y = new Complex[n];
		for (int k = 0; k < n / 2; k++) {
			double kth = -2 * k * Math.PI / n;
			Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
			y[k] = evenFFT[k].plus(wk.times(oddFFT[k]));
			y[k + n / 2] = evenFFT[k].minus(wk.times(oddFFT[k]));
		}
		return y;
	}

	public static Complex[] ifft(Complex[] x) {
		int n = x.length;
		Complex[] y = new Complex[n];

		// take conjugate
		for (int i = 0; i < n; i++) {
			y[i] = x[i].conjugate();
		}

		// compute forward FFT
		y = fft(y);

		// take conjugate again
		for (int i = 0; i < n; i++) {
			y[i] = y[i].conjugate();
		}

		// divide by n
		for (int i = 0; i < n; i++) {
			y[i] = y[i].scale(1.0 / n);
		}

		return y;

	}
}
