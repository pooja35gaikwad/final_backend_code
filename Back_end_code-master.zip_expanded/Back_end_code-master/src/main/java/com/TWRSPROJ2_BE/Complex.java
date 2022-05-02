package com.TWRSPROJ2_BE;

public class Complex {

	private final double re; // the real part
	private final double im; // the imaginary part
	// create a new object with the given real and imaginary parts

	public Complex(double real, double imag) {
		re = real;
		im = imag;
	}

	// return a string representation of the invoking Complex object
	public String toString() {
		if (im == 0)
			return re + "";
		if (re == 0)
			return im + "i";
		if (im < 0)
			return re + " - " + (-im) + "i";
		return re + " + " + im + "i";
	}

	// return abs/modulus/magnitude
	public double abs() {
		return Math.hypot(re, im);
	}

	// return angle/phase/argument, normalized to be between -pi and pi
	public double phase() {
		return Math.atan2(im, re);
	}

	// return a new Complex object whose value is (this + b)
	public Complex plus(Complex b) {
		Complex a = this; // invoking object
		double real = a.re + b.re;
		double imag = a.im + b.im;
		return new Complex(real, imag);
	}

	// return a new Complex object whose value is (this - b)
	public Complex minus(Complex b) {
		Complex a = this;
		double real = a.re - b.re;
		double imag = a.im - b.im;
		return new Complex(real, imag);
	}

	// return a new Complex object whose value is (this * b)
	public Complex times(Complex b) {
		Complex a = this;
		double real = 0;
		double imag = 0;
		if (b == null) {
			real = a.re - a.im;
			imag = a.re + a.im;
		} else {
			real = a.re * b.re - a.im * b.im;
			imag = a.re * b.im + a.im * b.re;
		}

		return new Complex(real, imag);
	}

	public Complex conjugate() {
		return new Complex(re, -im);
	}

	// return a new object whose value is (this * alpha)
	public Complex scale(double alpha) {
		return new Complex(alpha * re, alpha * im);
	}
}
