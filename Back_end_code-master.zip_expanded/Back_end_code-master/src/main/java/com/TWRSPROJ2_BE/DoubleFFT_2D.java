//package com.TWRSPROJ2_BE;
//
//public class DoubleFFT_2D {
//
//	private int				rows;
//	private int				columns;
//	private double[]		t;
//	private DoubleFFT_1D	fftColumns, fftRows;
//	private int				oldNthreads;
//	private int				nt;
//	private boolean			isPowerOfTwo	= false;
//	private boolean			useThreads		= false;
//
//	/**
//	 * Creates new instance of DoubleFFT_2D.
//	 * 
//	 * @param rows
//	 *            number of rows
//	 * @param columns
//	 *            number of columns
//	 */
//	public DoubleFFT_2D(int rows, int columns)
//	{
//		if (rows <= 1 || columns <= 1)
//			throw new IllegalArgumentException(
//					"rows and columns must be greater than 1");
//		this.rows = rows;
//		this.columns = columns;
//		if (rows * columns >= ConcurrencyUtils.getThreadsBeginN_2D())
//			useThreads = true;
//		if (ConcurrencyUtils.isPowerOf2(rows)
//				&& ConcurrencyUtils.isPowerOf2(columns))
//		{
//			isPowerOfTwo = true;
//			oldNthreads = ConcurrencyUtils.getNumberOfThreads();
//			nt = 8 * oldNthreads * rows;
//			if (2 * columns == 4 * oldNthreads)
//				nt >>= 1;
//			else if (2 * columns < 4 * oldNthreads)
//				nt >>= 2;
//			t = new double[nt];
//		}
//		fftRows = new DoubleFFT_1D(rows);
//		if (rows == columns)
//			fftColumns = fftRows;
//		else
//			fftColumns = new DoubleFFT_1D(columns);
//	}
//
//}
