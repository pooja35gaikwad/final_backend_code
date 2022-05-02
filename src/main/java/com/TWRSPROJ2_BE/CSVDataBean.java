package com.TWRSPROJ2_BE;

import java.util.ArrayList;
import java.util.List;

public class CSVDataBean {
	List<Long> freq = new ArrayList();
	List<Float> real = new ArrayList();
	List<Float> img = new ArrayList();
	List<Double> magnitude = new ArrayList();
	List<Double> fftMagnitude = new ArrayList();
	double[][] BScan = new double[201][32];

	public List<Double> getMagnitude() {
		return magnitude;
	}

	public double[][] getBScan() {
		return BScan;
	}

	public void setBScan(double[][] bScan) {
		BScan = bScan;
	}

	public void setMagnitude(List<Double> magnitude) {
		this.magnitude = magnitude;
	}

	/**
	 * @return
	 */
	public List<Long> getFreq() {
		return freq;
	}

	public List<Double> getFftMagnitude() {
		return fftMagnitude;
	}

	public void setFftMagnitude(List<Double> fftMagnitude) {
		this.fftMagnitude = fftMagnitude;
	}

	/**
	 * @param freq
	 */
	public void setFreq(List<Long> freq) {
		this.freq = freq;
	}

	/**
	 * @return
	 */
	public List<Float> getReal() {
		return real;
	}

	/**
	 * @param real
	 */
	public void setReal(List<Float> real) {
		this.real = real;
	}

	/**
	 * @return
	 */
	public List<Float> getImg() {
		return img;
	}

	/**
	 * @param img
	 */
	public void setImg(List<Float> img) {
		this.img = img;
	}
}
