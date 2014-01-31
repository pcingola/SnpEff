/*  Class Matrix
*
*   Defines a matrix and includes the methods needed
*   for standard matrix manipulations, e.g. multiplation,
*   and related procedures, e.g. solution of linear
*   simultaneous equations
*
*   See class ComplexMatrix and PhasorMatrix for complex matrix arithmetic
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	    June 2002
*   UPDATES:    21 April 2004, 16 February 2006, 31 March 2006, 22 April 2006, 1 July 2007, 17 July 2007
*               18 August 2007, 7 October 2007, 27 February 2008, 7 April 2008, 5 July 2008, 6-15 September 2008
*               7-14 October 2008, 16 February 2009, 16 June 2009, 15 October 2009, 4-5 November 2009
*               12 January 2010, 19 February 2010, 14 November 2010, 12 January 2011, 20 January 2011, 14-16 July 2011
*
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Matrix.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2002 - 2011  Michael Thomas Flanagan
*   PERMISSION TO COPY:
*
* Permission to use, copy and modify this software and its documentation for NON-COMMERCIAL purposes is granted, without fee,
* provided that an acknowledgement to the author, Dr Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies
* and associated documentation or publications.
*
* Redistributions of the source code of this source code, or parts of the source codes, must retain the above copyright notice, this list of conditions
* and the following disclaimer and requires written permission from the Michael Thomas Flanagan:
*
* Redistribution in binary form of all or parts of this class must reproduce the above copyright notice, this list of conditions and
* the following disclaimer in the documentation and/or other materials provided with the distribution and requires written permission from the Michael Thomas Flanagan:
*
* Dr Michael Thomas Flanagan makes no representations about the suitability or fitness of the software for any or for a particular purpose.
* Dr Michael Thomas Flanagan shall not be liable for any damages suffered as a result of using, modifying or distributing this software
* or its derivatives.
*
***************************************************************************************/

package flanagan.math;

import flanagan.analysis.Regression;
import flanagan.analysis.RegressionFunction;
import flanagan.math.ArrayMaths;
import flanagan.math.Conv;
import flanagan.analysis.Stat;

import java.util.ArrayList;
import java.util.Vector;
import java.math.BigDecimal;
import java.math.BigInteger;

public class Matrix{

	    private int numberOfRows = 0;                   // number of rows
	    private int numberOfColumns = 0;                // number of columns
	    private double matrix[][] = null; 	            // 2-D  Matrix
	    private double hessenberg[][] = null; 	        // 2-D  Hessenberg equivalent
	    private boolean hessenbergDone = false;         // = true when Hessenberg matrix calculated
	    private int permutationIndex[] = null;          // row permutation index
	    private double rowSwapIndex = 1.0D;             // row swap index
	    private double[] eigenValues = null;            // eigen values of the matrix
	    private double[][] eigenVector = null;          // eigen vectors of the matrix
	    private double[] sortedEigenValues = null;      // eigen values of the matrix sorted into descending order
	    private double[][] sortedEigenVector = null;    // eigen vectors of the matrix sorted to matching descending eigen value order
	    private int numberOfRotations = 0;              // number of rotations in Jacobi transformation
	    private int[] eigenIndices = null;              // indices of the eigen values before sorting into descending order
	    private int maximumJacobiIterations = 100;      // maximum number of Jacobi iterations
	    private boolean eigenDone = false;              // = true when eigen values and vectors calculated
	    private boolean matrixCheck = true;             // check on matrix status
	                                    	            // true - no problems encountered in LU decomposition
	                                    	            // false - attempted a LU decomposition on a singular matrix

	    private boolean supressErrorMessage = false;    // true - LU decompostion failure message supressed

	    private double tiny = 1.0e-100;                 // small number replacing zero in LU decomposition

	    // CONSTRUCTORS
	    // Construct a numberOfRows x numberOfColumns matrix of variables all equal to zero
        public Matrix(int numberOfRows, int numberOfColumns){
		    this.numberOfRows = numberOfRows;
		    this.numberOfColumns = numberOfColumns;
	        this.matrix = new double[numberOfRows][numberOfColumns];
		    this.permutationIndex = new int[numberOfRows];
            for(int i=0;i<numberOfRows;i++)this.permutationIndex[i]=i;
        }

	    // Construct a numberOfRows x numberOfColumns matrix of variables all equal to the number const
        public Matrix(int numberOfRows, int numberOfColumns, double constant){
		    this.numberOfRows = numberOfRows;
		    this.numberOfColumns = numberOfColumns;
		    this.matrix = new double[numberOfRows][numberOfColumns];
            for(int i=0;i<numberOfRows;i++){
            		for(int j=0;j<numberOfColumns;j++)this.matrix[i][j]=constant;
		    }
		    this.permutationIndex = new int[numberOfRows];
            for(int i=0;i<numberOfRows;i++)this.permutationIndex[i]=i;
 	    }

	    // Construct matrix with a copy of an existing numberOfRows x numberOfColumns 2-D array of variables
        public Matrix(double[][] twoD){
		    this.numberOfRows = twoD.length;
		    this.numberOfColumns = twoD[0].length;
		    this.matrix = new double[this.numberOfRows][this.numberOfColumns];
		    for(int i=0; i<numberOfRows; i++){
		    	if(twoD[i].length!=numberOfColumns)throw new IllegalArgumentException("All rows must have the same length");
    			for(int j=0; j<numberOfColumns; j++){
    		    		this.matrix[i][j]=twoD[i][j];
    			}
		    }
		    this.permutationIndex = new int[numberOfRows];
            for(int i=0;i<numberOfRows;i++)this.permutationIndex[i]=i;
   	    }

   	    // Construct matrix with a copy of an existing numberOfRows x numberOfColumns 2-D array of floats
        public Matrix(float[][] twoD){
		    this.numberOfRows = twoD.length;
		    this.numberOfColumns = twoD[0].length;
		    for(int i=1; i<numberOfRows; i++){
		        if(twoD[i].length!=numberOfColumns)throw new IllegalArgumentException("All rows must have the same length");
		    }
		    this.matrix = new double[this.numberOfRows][this.numberOfColumns];
		    for(int i=0; i<numberOfRows; i++){
		        for(int j=0; j<numberOfColumns; j++){
		            this.matrix[i][j] = (double)twoD[i][j];
		        }
		    }
		    this.permutationIndex = new int[numberOfRows];
            for(int i=0;i<numberOfRows;i++)this.permutationIndex[i]=i;
   	    }

   	    // Construct matrix with a copy of an existing numberOfRows x numberOfColumns 2-D array of longs
        public Matrix(long[][] twoD){
		    this.numberOfRows = twoD.length;
		    this.numberOfColumns = twoD[0].length;
		    for(int i=1; i<numberOfRows; i++){
		        if(twoD[i].length!=numberOfColumns)throw new IllegalArgumentException("All rows must have the same length");
		    }
		    this.matrix = new double[this.numberOfRows][this.numberOfColumns];
		    for(int i=0; i<numberOfRows; i++){
		        for(int j=0; j<numberOfColumns; j++){
		            this.matrix[i][j] = (double)twoD[i][j];
		        }
		    }
		    this.permutationIndex = new int[numberOfRows];
            for(int i=0;i<numberOfRows;i++)this.permutationIndex[i]=i;
   	    }


   	    // Construct matrix with a copy of an existing numberOfRows x numberOfColumns 2-D array of ints
        public Matrix(int[][] twoD){
		    this.numberOfRows = twoD.length;
		    this.numberOfColumns = twoD[0].length;
		    for(int i=1; i<numberOfRows; i++){
		        if(twoD[i].length!=numberOfColumns)throw new IllegalArgumentException("All rows must have the same length");
		    }
		    this.matrix = new double[this.numberOfRows][this.numberOfColumns];
		    for(int i=0; i<numberOfRows; i++){
		        for(int j=0; j<numberOfColumns; j++){
		            this.matrix[i][j] = (double)twoD[i][j];
		        }
		    }
		    this.permutationIndex = new int[numberOfRows];
            for(int i=0;i<numberOfRows;i++)this.permutationIndex[i]=i;
   	    }

	    // Construct matrix with a copy of an existing numberOfRows 1-D array of ArrayMaths
        public Matrix(ArrayMaths[] twoD){
		    this.numberOfRows = twoD.length;
		    this.numberOfColumns = twoD[0].length();
		    this.matrix = new double[this.numberOfRows][this.numberOfColumns];
		    for(int i=0; i<numberOfRows; i++){
		        double[] arrayh = (twoD[i].copy()).array();
		        if(arrayh.length!=numberOfColumns)throw new IllegalArgumentException("All rows must have the same length");
		        this.matrix[i] = arrayh;
		    }
		    this.permutationIndex = new int[numberOfRows];
            for(int i=0;i<numberOfRows;i++)this.permutationIndex[i]=i;
   	    }

   	    // Construct matrix with a copy of an existing numberOfRows 1-D array of ArrayLists<Object>
        public Matrix(ArrayList<Object>[] twoDal){
            this.numberOfRows = twoDal.length;
            ArrayMaths[] twoD = new ArrayMaths[this.numberOfRows];
            for(int i=0; i<this.numberOfRows; i++){
                twoD[i] = new ArrayMaths(twoDal[i]);
            }

		    this.numberOfColumns = twoD[0].length();
		    this.matrix = new double[this.numberOfRows][this.numberOfColumns];
		    for(int i=0; i<numberOfRows; i++){
		        double[] arrayh = (twoD[i].copy()).array();
		        if(arrayh.length!=numberOfColumns)throw new IllegalArgumentException("All rows must have the same length");
		        this.matrix[i] = arrayh;
		    }
		    this.permutationIndex = new int[numberOfRows];
            for(int i=0;i<numberOfRows;i++)this.permutationIndex[i]=i;
   	    }

        // Construct matrix with a copy of an existing numberOfRows 1-D array of Vector<Object>
        public Matrix(Vector<Object>[] twoDv){
            this.numberOfRows = twoDv.length;
            ArrayMaths[] twoD = new ArrayMaths[this.numberOfRows];
            for(int i=0; i<this.numberOfRows; i++){
                twoD[i] = new ArrayMaths(twoDv[i]);
            }

		    this.numberOfColumns = twoD[0].length();
		    this.matrix = new double[this.numberOfRows][this.numberOfColumns];
		    for(int i=0; i<numberOfRows; i++){
		        double[] arrayh = (twoD[i].copy()).array();
		        if(arrayh.length!=numberOfColumns)throw new IllegalArgumentException("All rows must have the same length");
		        this.matrix[i] = arrayh;
		    }
		    this.permutationIndex = new int[numberOfRows];
            for(int i=0;i<numberOfRows;i++)this.permutationIndex[i]=i;
   	    }

   	    // Construct matrix with a copy of an existing numberOfRows x numberOfColumns 2-D array of BigDecimals
        public Matrix(BigDecimal[][] twoD){
		    this.numberOfRows = twoD.length;
		    this.numberOfColumns = twoD[0].length;
		    for(int i=1; i<numberOfRows; i++){
		        if(twoD[i].length!=numberOfColumns)throw new IllegalArgumentException("All rows must have the same length");
		    }
		    this.matrix = new double[this.numberOfRows][this.numberOfColumns];
		    for(int i=0; i<numberOfRows; i++){
		        for(int j=0; j<numberOfColumns; j++){
		            this.matrix[i][j] = twoD[i][j].doubleValue();
		        }
		    }
		    this.permutationIndex = new int[numberOfRows];
            for(int i=0;i<numberOfRows;i++)this.permutationIndex[i]=i;
   	    }

   	    // Construct matrix with a copy of an existing numberOfRows x numberOfColumns 2-D array of BigIntegers
        public Matrix(BigInteger[][] twoD){
		    this.numberOfRows = twoD.length;
		    this.numberOfColumns = twoD[0].length;
		    for(int i=1; i<numberOfRows; i++){
		        if(twoD[i].length!=numberOfColumns)throw new IllegalArgumentException("All rows must have the same length");
		    }
		    this.matrix = new double[this.numberOfRows][this.numberOfColumns];
		    for(int i=0; i<numberOfRows; i++){
		        for(int j=0; j<numberOfColumns; j++){
		            this.matrix[i][j] = twoD[i][j].doubleValue();
		        }
		    }
		    this.permutationIndex = new int[numberOfRows];
            for(int i=0;i<numberOfRows;i++)this.permutationIndex[i]=i;
   	    }



	    // Construct matrix with a copy of the 2D matrix and permutation index of an existing Matrix bb.
        public Matrix(Matrix bb){
		    this.numberOfRows = bb.numberOfRows;
		    this.numberOfColumns = bb.numberOfColumns;
		    this.matrix = new double[this.numberOfRows][this.numberOfColumns];
		    for(int i=0; i<numberOfRows; i++){
		        for(int j=0; j<numberOfColumns; j++){
		            this.matrix[i][j] = bb.matrix[i][j];
		        }
		    }
		    this.permutationIndex = Conv.copy(bb.permutationIndex);
            this.rowSwapIndex = bb.rowSwapIndex;
 	    }


        // METHODS
        // SET VALUES
        // reset value of tiny used to replace zero in LU decompostions
        // If not set: 1e-100 used
        public void resetLUzero(double zeroValue){
            this.tiny = zeroValue;
        }

        // Set the matrix with a copy of an existing numberOfRows x numberOfColumns 2-D matrix of variables
        public void setTwoDarray(double[][] aarray){
		    if(this.numberOfRows != aarray.length)throw new IllegalArgumentException("row length of this Matrix differs from that of the 2D array argument");
		    if(this.numberOfColumns != aarray[0].length)throw new IllegalArgumentException("column length of this Matrix differs from that of the 2D array argument");
		    for(int i=0; i<numberOfRows; i++){
		    	if(aarray[i].length!=numberOfColumns)throw new IllegalArgumentException("All rows must have the same length");
    			for(int j=0; j<numberOfColumns; j++){
    		    		this.matrix[i][j]=aarray[i][j];
    			}
		    }
	    }

    	// Set an individual array element
    	// i = row index
    	// j = column index
    	// aa = value of the element
    	public void setElement(int i, int j, double aa){
        	this.matrix[i][j]=aa;
    	}


    	// Set a sub-matrix starting with row index i, column index j
    	public void setSubMatrix(int i, int j, double[][] subMatrix){
    	    int k = subMatrix.length;
    	    int l = subMatrix[0].length;
        	if(i+k-1>=this.numberOfRows)throw new IllegalArgumentException("Sub-matrix position is outside the row bounds of this Matrix");
        	if(j+l-1>=this.numberOfColumns)throw new IllegalArgumentException("Sub-matrix position is outside the column bounds of this Matrix");

        	int m = 0;
        	int n = 0;
        	for(int p=0; p<k; p++){
        	    n = 0;
                for(int q=0; q<l; q++){
                    this.matrix[i+p][j+q] = subMatrix[m][n];
                    n++;
                }
                m++;
        	}
    	}

    	// Set a sub-matrix starting with row index i, column index j
    	// and ending with row index k, column index l
    	// See setSubMatrix above - this method has been retained for compatibility purposes
    	public void setSubMatrix(int i, int j, int k, int l, double[][] subMatrix){
    	    this.setSubMatrix(i, j, subMatrix);
    	}

    	// Set a sub-matrix
    	// row = array of row indices
    	// col = array of column indices
    	public void setSubMatrix(int[] row, int[] col, double[][] subMatrix){
        	int n=row.length;
        	int m=col.length;
        	for(int p=0; p<n; p++){
            		for(int q=0; q<m; q++){
                		this.matrix[row[p]][col[q]] = subMatrix[p][q];
            		}
        	}
    	}

    	// Get the value of matrixCheck
    	public boolean getMatrixCheck(){
        	return this.matrixCheck;
    	}

    	// SPECIAL MATRICES
    	// Construct an identity matrix
    	public static Matrix identityMatrix(int numberOfRows){
        	Matrix special = new Matrix(numberOfRows, numberOfRows);
        	for(int i=0; i<numberOfRows; i++){
            		special.matrix[i][i]=1.0;
        	}
        	return special;
    	}

    	// Construct a square unit matrix
    	public static Matrix unitMatrix(int numberOfRows){
        	Matrix special = new Matrix(numberOfRows, numberOfRows);
        	for(int i=0; i<numberOfRows; i++){
        	    for(int j=0; j<numberOfRows; j++){
            		special.matrix[i][j]=1.0;
            	}
        	}
        	return special;
    	}

    	// Construct a rectangular unit matrix
    	public static Matrix unitMatrix(int numberOfRows, int numberOfColumns){
        	Matrix special = new Matrix(numberOfRows, numberOfColumns);
        	for(int i=0; i<numberOfRows; i++){
        	    for(int j=0; j<numberOfColumns; j++){
            		special.matrix[i][j]=1.0;
            	}
        	}
        	return special;
    	}

    	// Construct a square scalar matrix
    	public static Matrix scalarMatrix(int numberOfRows, double diagconst){
        	Matrix special = new Matrix(numberOfRows, numberOfRows);
        	double[][] specialArray = special.getArrayReference();
        	for(int i=0; i<numberOfRows; i++){
            		for(int j=i; j<numberOfRows; j++){
                		if(i==j){
                    			specialArray[i][j]= diagconst;
                		}
            		}
        	}
        	return special;
    	}

    	 // Construct a rectangular scalar matrix
    	public static Matrix scalarMatrix(int numberOfRows, int numberOfColumns, double diagconst){
        	Matrix special = new Matrix(numberOfRows, numberOfColumns);
        	double[][] specialArray = special.getArrayReference();
        	for(int i=0; i<numberOfRows; i++){
            		for(int j=i; j<numberOfColumns; j++){
                		if(i==j){
                    			specialArray[i][j]= diagconst;
                		}
            		}
        	}
        	return special;
    	}

    	// Construct a square diagonal matrix
    	public static Matrix diagonalMatrix(int numberOfRows, double[] diag){
        	if(diag.length!=numberOfRows)throw new IllegalArgumentException("matrix dimension differs from diagonal array length");
        	Matrix special = new Matrix(numberOfRows, numberOfRows);
        	double[][] specialArray = special.getArrayReference();
            for(int i=0; i<numberOfRows; i++){
            		specialArray[i][i]=diag[i];
        	}
        	return special;
    	}

    	// Construct a rectangular diagonal matrix
    	public static Matrix diagonalMatrix(int numberOfRows, int numberOfColumns, double[] diag){
        	if(diag.length!=numberOfRows)throw new IllegalArgumentException("matrix dimension differs from diagonal array length");
        	Matrix special = new Matrix(numberOfRows, numberOfColumns);
        	double[][] specialArray = special.getArrayReference();
        	for(int i=0; i<numberOfRows; i++){
            		for(int j=i; j<numberOfColumns; j++){
                		if(i==j){
                    			specialArray[i][j]= diag[i];
                		}
            		}
        	}
        	return special;
    	}

    	// GET VALUES
        // Return the number of rows
    	public int getNumberOfRows(){
        	return this.numberOfRows;
    	}

    	// Return the number of rows
    	public int getNrow(){
        	return this.numberOfRows;
    	}

    	// Return the number of columns
    	public int getNumberOfColumns(){
        	return this.numberOfColumns;
    	}

    	// Return the number of columns
    	public int getNcol(){
        	return this.numberOfColumns;
    	}

    	// Return a reference to the internal 2-D array
    	public double[][] getArrayReference(){
        	return this.matrix;
    	}

    	// Return a reference to the internal 2-D array
    	// included for backward compatibility with incorrect earlier documentation
    	public double[][] getArrayPointer(){
        	return this.matrix;
    	}

    	// Return a copy of the internal 2-D array
    	public double[][] getArrayCopy(){
        	double[][] c = new double[this.numberOfRows][this.numberOfColumns];
		    for(int i=0; i<numberOfRows; i++){
		    	for(int j=0; j<numberOfColumns; j++){
		        	c[i][j]=this.matrix[i][j];
		    	}
		    }
        	return c;
    	}

    	// Return a copy of a row
    	public double[] getRowCopy(int i){
    	    if(i>=this.numberOfRows)throw new IllegalArgumentException("Row index, " + i + ", must be less than the number of rows, " + this.numberOfRows);
    	    if(i<0)throw new IllegalArgumentException("Row index, " + i + ", must be zero or positive");
        	return Conv.copy(this.matrix[i]);
    	}

        // Return a copy of a column
    	public double[] getColumnCopy(int ii){
    	    if(ii>=this.numberOfColumns)throw new IllegalArgumentException("Column index, " + ii + ", must be less than the number of columns, " + this.numberOfColumns);
    	    if(ii<0)throw new IllegalArgumentException("column index, " + ii + ", must be zero or positive");
        	double[] col = new double[this.numberOfRows];
		    for(int i=0; i<numberOfRows; i++){
		        col[i]=this.matrix[i][ii];
		    }
        	return col;
    	}


        // Return  a single element of the internal 2-D array
    	public double getElement(int i, int j){
         	return this.matrix[i][j];
    	}

    	// Return a single element of the internal 2-D array
    	// included for backward compatibility with incorrect earlier documentation
    	public double getElementCopy(int i, int j){
         	return this.matrix[i][j];
    	}

    	// Return a single element of the internal 2-D array
    	// included for backward compatibility with incorrect earlier documentation
    	public double getElementPointer(int i, int j){
         	return this.matrix[i][j];
    	}

    	// Return a sub-matrix starting with row index i, column index j
    	// and ending with row index k, column index l
    	public Matrix getSubMatrix(int i, int j, int k, int l){
    	    if(i>k)throw new IllegalArgumentException("row indices inverted");
        	if(j>l)throw new IllegalArgumentException("column indices inverted");
        	if(k>=this.numberOfRows)throw new IllegalArgumentException("Sub-matrix position is outside the row bounds of this Matrix" );
        	if(l>=this.numberOfColumns)throw new IllegalArgumentException("Sub-matrix position is outside the column bounds of this Matrix" + i + " " +l);

        	int n=k-i+1, m=l-j+1;
        	Matrix subMatrix = new Matrix(n, m);
        	double[][] sarray = subMatrix.getArrayReference();
        	for(int p=0; p<n; p++){
            		for(int q=0; q<m; q++){
                		sarray[p][q]= this.matrix[i+p][j+q];
            		}
        	}
        	return subMatrix;
    	}

    	// Return a sub-matrix
    	// row = array of row indices
   	    // col = array of column indices
    	public Matrix getSubMatrix(int[] row, int[] col){
        	int n = row.length;
        	int m = col.length;
        	Matrix subMatrix = new Matrix(n, m);
        	double[][] sarray = subMatrix.getArrayReference();
        	for(int i=0; i<n; i++){
            		for(int j=0; j<m; j++){
                		sarray[i][j]= this.matrix[row[i]][col[j]];
            		}
        	}
        	return subMatrix;
    	}

    	// Return a reference to the permutation index array
    	public int[]  getIndexReference(){
         	return this.permutationIndex;
    	}

    	// Return a reference to the permutation index array
    	// included for backward compatibility with incorrect earlier documentation
    	public int[]  getIndexPointer(){
         	return this.permutationIndex;
    	}

    	// Return a copy of the permutation index array
    	public int[]  getIndexCopy(){
        	int[] indcopy = new int[this.numberOfRows];
        	for(int i=0; i<this.numberOfRows; i++){
            		indcopy[i]=this.permutationIndex[i];
        	}
        	return indcopy;
    	}

    	// Return the row swap index
    	public double getSwap(){
         	return this.rowSwapIndex;
    	}

    	// COPY
    	// Copy a Matrix [static method]
    	public static Matrix copy(Matrix a){
    	    if(a==null){
    	        return null;
    	    }
    	    else{
        	    int nr = a.getNumberOfRows();
        	    int nc = a.getNumberOfColumns();
        	    double[][] aarray = a.getArrayReference();
        	    Matrix b = new Matrix(nr,nc);
        	    b.numberOfRows = nr;
        	    b.numberOfColumns = nc;
        	    double[][] barray = b.getArrayReference();
        	    for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		barray[i][j]=aarray[i][j];
            		}
        	    }
        	    for(int i=0; i<nr; i++)b.permutationIndex[i] = a.permutationIndex[i];
        	    return b;
        	}
    	}

    	// Copy a Matrix [instance method]
    	public Matrix copy(){
    	    if(this==null){
    	        return null;
    	    }
    	    else{
        	    int nr = this.numberOfRows;
        	    int nc = this.numberOfColumns;
        	    Matrix b = new Matrix(nr,nc);
        	    double[][] barray = b.getArrayReference();
        	    b.numberOfRows = nr;
        	    b.numberOfColumns = nc;
        	    for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		barray[i][j]=this.matrix[i][j];
            		}
        	    }
        	    for(int i=0; i<nr; i++)b.permutationIndex[i] = this.permutationIndex[i];
        	    return b;
        	}
    	}

    	// Clone a Matrix
    	public Object clone(){
            if(this==null){
    	        return null;
    	    }
    	    else{
    	        int nr = this.numberOfRows;
        	    int nc = this.numberOfColumns;
        	    Matrix b = new Matrix(nr,nc);
        	    double[][] barray = b.getArrayReference();
        	    b.numberOfRows = nr;
        	    b.numberOfColumns = nc;
        	    for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		barray[i][j]=this.matrix[i][j];
            		}
        	    }
        	    for(int i=0; i<nr; i++)b.permutationIndex[i] = this.permutationIndex[i];
        	    return (Object) b;
        	}
    	}

    	// COLUMN MATRICES
    	// Converts a 1-D array of doubles to a column  matrix
        public static Matrix columnMatrix(double[] darray){
            int nr = darray.length;
            Matrix pp = new Matrix(nr, 1);
            for(int i=0; i<nr; i++)pp.matrix[i][0] = darray[i];
            return pp;
        }

        // ROW MATRICES
    	// Converts a 1-D array of doubles to a row matrix
        public static Matrix rowMatrix(double[] darray){
            int nc = darray.length;
            Matrix pp = new Matrix(1, nc);
            for(int i=0; i<nc; i++)pp.matrix[0][i] = darray[i];
            return pp;
        }

    	// ADDITION
    	// Add this matrix to matrix B.  This matrix remains unaltered [instance method]
    	public Matrix plus(Matrix bmat){
        	if((this.numberOfRows!=bmat.numberOfRows)||(this.numberOfColumns!=bmat.numberOfColumns)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	int nr=bmat.numberOfRows;
        	int nc=bmat.numberOfColumns;
        	Matrix cmat = new Matrix(nr,nc);
        	double[][] carray = cmat.getArrayReference();
        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		carray[i][j]=this.matrix[i][j] + bmat.matrix[i][j];
            		}
        	}
        	return cmat;
    	}

    	// Add this matrix to 2-D array B.  This matrix remains unaltered [instance method]
    	public Matrix plus(double[][] bmat){
    	    int nr=bmat.length;
        	int nc=bmat[0].length;
        	if((this.numberOfRows!=nr)||(this.numberOfColumns!=nc)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}

        	Matrix cmat = new Matrix(nr,nc);
        	double[][] carray = cmat.getArrayReference();
        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		carray[i][j]=this.matrix[i][j] + bmat[i][j];
            		}
        	}
        	return cmat;
    	}


    	// Add matrices A and B [static method]
    	public static Matrix plus(Matrix amat, Matrix bmat){
        	if((amat.numberOfRows!=bmat.numberOfRows)||(amat.numberOfColumns!=bmat.numberOfColumns)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	int nr=amat.numberOfRows;
        	int nc=amat.numberOfColumns;
        	Matrix cmat = new Matrix(nr,nc);
        	double[][] carray = cmat.getArrayReference();
        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		carray[i][j]=amat.matrix[i][j] + bmat.matrix[i][j];
            		}
        	}
        	return cmat;
    	}

    	// Add matrix B to this matrix [equivalence of +=]
    	public void plusEquals(Matrix bmat){
        	if((this.numberOfRows!=bmat.numberOfRows)||(this.numberOfColumns!=bmat.numberOfColumns)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	int nr=bmat.numberOfRows;
        	int nc=bmat.numberOfColumns;

        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		this.matrix[i][j] += bmat.matrix[i][j];
	            	}
        	}
    	}

    	// SUBTRACTION
    	// Subtract matrix B from this matrix.   This matrix remains unaltered [instance method]
    	public Matrix minus(Matrix bmat){
        	if((this.numberOfRows!=bmat.numberOfRows)||(this.numberOfColumns!=bmat.numberOfColumns)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	int nr=this.numberOfRows;
        	int nc=this.numberOfColumns;
        	Matrix cmat = new Matrix(nr,nc);
        	double[][] carray = cmat.getArrayReference();
        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		carray[i][j]=this.matrix[i][j] - bmat.matrix[i][j];
            		}
        	}
        	return cmat;
    	}

    	// Subtract a  2-D array from this matrix.  This matrix remains unaltered [instance method]
    	public Matrix minus(double[][] bmat){
    	    int nr=bmat.length;
        	int nc=bmat[0].length;
        	if((this.numberOfRows!=nr)||(this.numberOfColumns!=nc)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}

        	Matrix cmat = new Matrix(nr,nc);
        	double[][] carray = cmat.getArrayReference();
        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		carray[i][j]=this.matrix[i][j] - bmat[i][j];
            		}
        	}
        	return cmat;
    	}


    	// Subtract matrix B from matrix A [static method]
    	public static Matrix minus(Matrix amat, Matrix bmat){
        	if((amat.numberOfRows!=bmat.numberOfRows)||(amat.numberOfColumns!=bmat.numberOfColumns)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	int nr=amat.numberOfRows;
        	int nc=amat.numberOfColumns;
        	Matrix cmat = new Matrix(nr,nc);
        	double[][] carray = cmat.getArrayReference();
        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		carray[i][j]=amat.matrix[i][j] - bmat.matrix[i][j];
            		}
        	}
        	return cmat;
    	}

    	// Subtract matrix B from this matrix [equivlance of -=]
    	public void minusEquals(Matrix bmat){
        	if((this.numberOfRows!=bmat.numberOfRows)||(this.numberOfColumns!=bmat.numberOfColumns)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	int nr=bmat.numberOfRows;
        	int nc=bmat.numberOfColumns;

        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		this.matrix[i][j] -= bmat.matrix[i][j];
            		}
        	}
    	}

    	// MULTIPLICATION
    	// Multiply this  matrix by a matrix.   [instance method]
    	// This matrix remains unaltered.
    	public Matrix times(Matrix bmat){
        	if(this.numberOfColumns!=bmat.numberOfRows)throw new IllegalArgumentException("Nonconformable matrices");

        	Matrix cmat = new Matrix(this.numberOfRows, bmat.numberOfColumns);
        	double [][] carray = cmat.getArrayReference();
        	double sum = 0.0D;

        	for(int i=0; i<this.numberOfRows; i++){
            		for(int j=0; j<bmat.numberOfColumns; j++){
                		sum=0.0D;
                		for(int k=0; k<this.numberOfColumns; k++){
                       			sum += this.matrix[i][k]*bmat.matrix[k][j];
                		}
                		carray[i][j]=sum;
            		}
        	}
        	return cmat;
    	}

    	// Multiply this  matrix by a 2-D array.   [instance method]
    	// This matrix remains unaltered.
    	public Matrix times(double[][] bmat){
    	    int nr=bmat.length;
        	int nc=bmat[0].length;

        	if(this.numberOfColumns!=nr)throw new IllegalArgumentException("Nonconformable matrices");

        	Matrix cmat = new Matrix(this.numberOfRows, nc);
        	double [][] carray = cmat.getArrayReference();
        	double sum = 0.0D;

        	for(int i=0; i<this.numberOfRows; i++){
            		for(int j=0; j<nc; j++){
                		sum=0.0D;
                		for(int k=0; k<this.numberOfColumns; k++){
                       			sum += this.matrix[i][k]*bmat[k][j];
                		}
                		carray[i][j]=sum;
            		}
        	}
        	return cmat;
    	}

    	// Multiply this matrix by a constant [instance method]
    	// This matrix remains unaltered
    	public Matrix times(double constant){
        	Matrix cmat = new Matrix(this.numberOfRows, this.numberOfColumns);
        	double [][] carray = cmat.getArrayReference();

        	for(int i=0; i<this.numberOfRows; i++){
            		for(int j=0; j<this.numberOfColumns; j++){
                  		carray[i][j] = this.matrix[i][j]*constant;
            		}
        	}
        	return cmat;
    	}

    	// Multiply two matrices {static method]
    	public static Matrix times(Matrix amat, Matrix bmat){
        	if(amat.numberOfColumns!=bmat.numberOfRows)throw new IllegalArgumentException("Nonconformable matrices");

        	Matrix cmat = new Matrix(amat.numberOfRows, bmat.numberOfColumns);
        	double [][] carray = cmat.getArrayReference();
        	double sum = 0.0D;

        	for(int i=0; i<amat.numberOfRows; i++){
            		for(int j=0; j<bmat.numberOfColumns; j++){
                		sum=0.0D;
                		for(int k=0; k<amat.numberOfColumns; k++){
                       			sum += (amat.matrix[i][k]*bmat.matrix[k][j]);
                		}
                		carray[i][j]=sum;
            		}
        	}
        	return cmat;
    	}

    	// Multiply a Matrix by a 2-D array of doubles [static method]
    	public static Matrix times(Matrix amat, double[][] bmat){
        	if(amat.numberOfColumns!=bmat.length)throw new IllegalArgumentException("Nonconformable matrices");

        	Matrix cmat = new Matrix(amat.numberOfRows, bmat[0].length);
        	Matrix dmat = new Matrix(bmat);
        	double [][] carray = cmat.getArrayReference();
        	double sum = 0.0D;

        	for(int i=0; i<amat.numberOfRows; i++){
            		for(int j=0; j<dmat.numberOfColumns; j++){
                		sum=0.0D;
                		for(int k=0; k<amat.numberOfColumns; k++){
                       			sum += (amat.matrix[i][k]*dmat.matrix[k][j]);
                		}
                		carray[i][j]=sum;
            		}
        	}
        	return cmat;
    	}

   	    // Multiply a matrix by a constant [static method]
    	public static Matrix times(Matrix amat, double constant){
        	Matrix cmat = new Matrix(amat.numberOfRows, amat.numberOfColumns);
        	double [][] carray = cmat.getArrayReference();

 	       	for(int i=0; i<amat.numberOfRows; i++){
            		for(int j=0; j<amat.numberOfColumns; j++){
                  		carray[i][j] = amat.matrix[i][j]*constant;
            		}
        	}
        	return cmat;
    	}

    	// Multiply this matrix by a matrix [equivalence of *=]
    	public void timesEquals(Matrix bmat){
        	if(this.numberOfColumns!=bmat.numberOfRows)throw new IllegalArgumentException("Nonconformable matrices");

        	Matrix cmat = new Matrix(this.numberOfRows, bmat.numberOfColumns);
        	double [][] carray = cmat.getArrayReference();
        	double sum = 0.0D;

        	for(int i=0; i<this.numberOfRows; i++){
            		for(int j=0; j<bmat.numberOfColumns; j++){
                		sum=0.0D;
                		for(int k=0; k<this.numberOfColumns; k++){
                       			sum += this.matrix[i][k]*bmat.matrix[k][j];
                		}
                		carray[i][j]=sum;
            		}
        	}

        	this.numberOfRows = cmat.numberOfRows;
	        this.numberOfColumns = cmat.numberOfColumns;
	        for(int i=0; i<this.numberOfRows; i++){
	            for(int j=0; j<this.numberOfColumns; j++){
	                this.matrix[i][j] = cmat.matrix[i][j];
	            }
	        }
    	}

   	    // Multiply this matrix by a constant [equivalence of *=]
    	public void timesEquals(double constant){

        	for(int i=0; i<this.numberOfRows; i++){
            		for(int j=0; j<this.numberOfColumns; j++){
                  		this.matrix[i][j] *= constant;
            		}
        	}
    	}

    	// DIVISION
    	// Divide this Matrix by a Matrix  - instance method
    	public Matrix over(Matrix bmat){
        	if((this.numberOfRows!=bmat.numberOfRows)||(this.numberOfColumns!=bmat.numberOfColumns)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	return this.times(bmat.inverse());
    	}

    	// Divide a Matrix by a Matrix - static method.
    	public Matrix over(Matrix amat, Matrix bmat){
        	if((amat.numberOfRows!=bmat.numberOfRows)||(amat.numberOfColumns!=bmat.numberOfColumns)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	return amat.times(bmat.inverse());
    	}


    	// Divide this Matrix by a  2-D array of doubles.
    	public Matrix over(double[][] bmat){
    	    int nr=bmat.length;
        	int nc=bmat[0].length;
        	if((this.numberOfRows!=nr)||(this.numberOfColumns!=nc)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}

        	Matrix cmat = new Matrix(bmat);
        	return this.times(cmat.inverse());
    	}

    	// Divide a Matrix by a  2-D array of doubles - static method.
    	public Matrix over(Matrix amat, double[][] bmat){
    	    int nr=bmat.length;
        	int nc=bmat[0].length;
        	if((amat.numberOfRows!=nr)||(amat.numberOfColumns!=nc)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}

        	Matrix cmat = new Matrix(bmat);
        	return amat.times(cmat.inverse());
    	}

    	// Divide a 2-D array of doubles by a Matrix - static method.
    	public Matrix over(double[][] amat, Matrix bmat){
    	    int nr=amat.length;
        	int nc=amat[0].length;
        	if((bmat.numberOfRows!=nr)||(bmat.numberOfColumns!=nc)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}

        	Matrix cmat = new Matrix(amat);
        	return cmat.times(bmat.inverse());
    	}

    	// Divide a 2-D array of doubles by a 2-D array of doubles - static method.
    	public Matrix over(double[][] amat, double[][] bmat){
    	    int nr=amat.length;
        	int nc=amat[0].length;
        	if((bmat.length!=nr)||(bmat[0].length!=nc)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}

        	Matrix cmat = new Matrix(amat);
        	Matrix dmat = new Matrix(bmat);
        	return cmat.times(dmat.inverse());
    	}

    	// Divide a this matrix by a matrix[equivalence of /=]
    	public void overEquals(Matrix bmat){
        	if((this.numberOfRows!=bmat.numberOfRows)||(this.numberOfColumns!=bmat.numberOfColumns)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	Matrix cmat = new Matrix(bmat);
    	    this.timesEquals(cmat.inverse());
    	}

    	// Divide this Matrix by a 2D array of doubles [equivalence of /=]
    	public void overEquals(double[][] bmat){
    	    Matrix pmat = new Matrix(bmat);
    	    this.overEquals(pmat);
    	}

    	// INVERSE
    	// Inverse of a square matrix [instance method]
    	public Matrix inverse(){
        	int n = this.numberOfRows;
        	if(n!=this.numberOfColumns)throw new IllegalArgumentException("Matrix is not square");
        	Matrix invmat = new Matrix(n, n);

            if(n==1){
                double[][] hold = this.getArrayCopy();
                if(hold[0][0]==0.0)throw new IllegalArgumentException("Matrix is singular");
                hold[0][0] = 1.0/hold[0][0];
                invmat = new Matrix(hold);
            }
            else{
                if(n==2){
                    double[][] hold = this.getArrayCopy();
                    double det = hold[0][0]*hold[1][1] - hold[0][1]*hold[1][0];
                    if(det==0.0)throw new IllegalArgumentException("Matrix is singular");
                    double[][] hold2 = new double[2][2];
                    hold2[0][0] = hold[1][1]/det;
                    hold2[1][1] = hold[0][0]/det;
                    hold2[1][0] = -hold[1][0]/det;
                    hold2[0][1] = -hold[0][1]/det;
                    invmat = new Matrix(hold2);
                }
                else{
        	        double[] col = new double[n];
        	        double[] xvec = new double[n];
        	        double[][] invarray = invmat.getArrayReference();
        	        Matrix ludmat;

	    	        ludmat = this.luDecomp();
        	        for(int j=0; j<n; j++){
            		    for(int i=0; i<n; i++)col[i]=0.0D;
            		    col[j]=1.0;
            		    xvec=ludmat.luBackSub(col);
            		    for(int i=0; i<n; i++)invarray[i][j]=xvec[i];
        	        }
        	    }
        	}
       		return invmat;
    	}

    	// Inverse of a square matrix [static method]
       	public static Matrix inverse(Matrix amat){
        	int n = amat.numberOfRows;
        	if(n!=amat.numberOfColumns)throw new IllegalArgumentException("Matrix is not square");
        	Matrix invmat = new Matrix(n, n);

            if(n==1){
                double[][] hold = amat.getArrayCopy();
                if(hold[0][0]==0.0)throw new IllegalArgumentException("Matrix is singular");
                hold[0][0] = 1.0/hold[0][0];
                invmat = new Matrix(hold);
            }
            else{
                if(n==2){
                    double[][] hold = amat.getArrayCopy();
                    double det = hold[0][0]*hold[1][1] - hold[0][1]*hold[1][0];
                    if(det==0.0)throw new IllegalArgumentException("Matrix is singular");
                    double[][] hold2 = new double[2][2];
                    hold2[0][0] = hold[1][1]/det;
                    hold2[1][1] = hold[0][0]/det;
                    hold2[1][0] = -hold[1][0]/det;
                    hold2[0][1] = -hold[0][1]/det;
                    invmat = new Matrix(hold2);
                }
                else{
                    double[] col = new double[n];
        	        double[] xvec = new double[n];
        	        double[][] invarray = invmat.getArrayReference();
        	        Matrix ludmat;

	    	        ludmat = amat.luDecomp();
        	        for(int j=0; j<n; j++){
            		    for(int i=0; i<n; i++)col[i]=0.0D;
            		    col[j]=1.0;
            		    xvec=ludmat.luBackSub(col);
            		    for(int i=0; i<n; i++)invarray[i][j]=xvec[i];
        	        }
        	    }
        	}
        	return invmat;
    	}

    	// TRANSPOSE
    	// Transpose of a matrix [instance method]
    	public Matrix transpose(){
        	Matrix tmat = new Matrix(this.numberOfColumns, this.numberOfRows);
        	double[][] tarray = tmat.getArrayReference();
        	for(int i=0; i<this.numberOfColumns; i++){
            		for(int j=0; j<this.numberOfRows; j++){
                		tarray[i][j]=this.matrix[j][i];
            		}
        	}
        	return tmat;
    	}

    	// Transpose of a matrix [static method]
    	public static Matrix transpose(Matrix amat){
        	Matrix tmat = new Matrix(amat.numberOfColumns, amat.numberOfRows);
        	double[][] tarray = tmat.getArrayReference();
        	for(int i=0; i<amat.numberOfColumns; i++){
            		for(int j=0; j<amat.numberOfRows; j++){
                		tarray[i][j]=amat.matrix[j][i];
            		}
        	}
        	return tmat;
    	}

    	// OPPOSITE
    	// Opposite of a matrix [instance method]
    	public Matrix opposite(){
        	Matrix opp = Matrix.copy(this);
        	for(int i=0; i<this.numberOfRows; i++){
            		for(int j=0; j<this.numberOfColumns; j++){
                		opp.matrix[i][j]=-this.matrix[i][j];
            		}
        	}
        	return opp;
    	}

    	// Opposite of a matrix [static method]
    	public static Matrix opposite(Matrix amat){
        	Matrix opp = Matrix.copy(amat);
        	for(int i=0; i<amat.numberOfRows; i++){
            		for(int j=0; j<amat.numberOfColumns; j++){
                		opp.matrix[i][j]=-amat.matrix[i][j];
            		}
        	}
        	return opp;
    	}

    	// TRACE
    	// Trace of a  matrix [instance method]
    	public double trace(){
        	double trac = 0.0D;
        	for(int i=0; i<Math.min(this.numberOfColumns,this.numberOfColumns); i++){
                	trac += this.matrix[i][i];
        	}
        	return trac;
    	}

    	// Trace of a matrix [static method]
    	public static double trace(Matrix amat){
        	double trac = 0.0D;
        	for(int i=0; i<Math.min(amat.numberOfColumns,amat.numberOfColumns); i++){
                	trac += amat.matrix[i][i];
        	}
        	return trac;
    	}

    	// DETERMINANT
    	//  Returns the determinant of a square matrix [instance method]
    	public double determinant(){
        	int n = this.numberOfRows;
        	if(n!=this.numberOfColumns)throw new IllegalArgumentException("Matrix is not square");
        	double det = 0.0D;
        	if(n==2){
        	     det = this.matrix[0][0]*this.matrix[1][1] - this.matrix[0][1]*this.matrix[1][0];
        	}
        	else{
        	    Matrix ludmat = this.luDecomp();
    	    	det = ludmat.rowSwapIndex;
            	for(int j=0; j<n; j++){
            		det *= ludmat.matrix[j][j];
             	}
            }
        	return det;
    	}

    	//  Returns the determinant of a square matrix [static method] - Matrix input
    	public static double determinant(Matrix amat){
        	int n = amat.numberOfRows;
        	if(n!=amat.numberOfColumns)throw new IllegalArgumentException("Matrix is not square");
        	double det = 0.0D;

        	if(n==2){
        	    double[][] hold = amat.getArrayCopy();
        	    det = hold[0][0]*hold[1][1] - hold[0][1]*hold[1][0];
        	}
        	else{
        	    Matrix ludmat = amat.luDecomp();
	    	    det = ludmat.rowSwapIndex;
        	    for(int j=0; j<n; j++){
            		det *= (ludmat.matrix[j][j]);
         	    }
         	}
        	return det;
    	}

    	//  Returns the determinant of a square matrix [static method] - [][] array input
    	public static double determinant(double[][]mat){
        	int n = mat.length;
        	for(int i=0; i<n; i++)if(n!=mat[i].length)throw new IllegalArgumentException("Matrix is not square");
        	double det = 0.0D;

        	if(n==2){
        	    det = mat[0][0]*mat[1][1] - mat[0][1]*mat[1][0];
        	}
        	else{
        	    Matrix amat = new Matrix(mat);
        	    Matrix ludmat = amat.luDecomp();
	    	    det = ludmat.rowSwapIndex;
        	    for(int j=0; j<n; j++){
            		det *= (ludmat.matrix[j][j]);
         	    }
         	}
        	return det;
    	}

    	// Returns the log(determinant) of a square matrix [instance method].
    	// Useful if determinant() underflows or overflows.
    	public double logDeterminant(){
        	int n = this.numberOfRows;
        	if(n!=this.numberOfColumns)throw new IllegalArgumentException("Matrix is not square");
        	double det = 0.0D;
        	Matrix ludmat = this.luDecomp();

	    	det = ludmat.rowSwapIndex;
	    	det=Math.log(det);
        	for(int j=0; j<n; j++){
            		det += Math.log(ludmat.matrix[j][j]);
        	}
        	return det;
    	}

    	// Returns the log(determinant) of a square matrix [static method] - matrix input.
    	// Useful if determinant() underflows or overflows.
    	public static double logDeterminant(Matrix amat){
        	int n = amat.numberOfRows;
        	if(n!=amat.numberOfColumns)throw new IllegalArgumentException("Matrix is not square");
        	double det = 0.0D;
        	Matrix ludmat = amat.luDecomp();

	    	det = ludmat.rowSwapIndex;
	    	det=Math.log(det);
        	for(int j=0; j<n; j++){
            		det += Math.log(ludmat.matrix[j][j]);
        	}
       		return det;
    	}

    	// Returns the log(determinant) of a square matrix [static method] double[][] input.
    	// Useful if determinant() underflows or overflows.
    	public static double logDeterminant(double[][] mat){
    	    int n = mat.length;
        	for(int i=0; i<n; i++)if(n!=mat[i].length)throw new IllegalArgumentException("Matrix is not square");
            Matrix amat = new Matrix(mat);
            return amat.logDeterminant();
    	}

    	// COFACTOR
    	// Returns the Matrix of all cofactors
    	public Matrix cofactor(){
    	    double[][] cof = new double[this.numberOfRows][this.numberOfColumns];
    	    for(int i=0; i<this.numberOfRows; i++){
    	        for(int j=0; j<this.numberOfColumns; j++){
    	            cof[i][j] = this.cofactor(i,j);
    	        }
    	    }
    	    return new Matrix(cof);
    	}

    	// Returns the ii,jjth cofactor
    	public double cofactor(int ii, int jj){
    	    if(ii<0 || ii>=this.numberOfRows)throw new IllegalArgumentException("The entered row index, " + ii + " must lie between 0 and " + (this.numberOfRows-1) + " inclusive");
    	    if(jj<0 || jj>=this.numberOfColumns)throw new IllegalArgumentException("The entered column index, " + jj + " must lie between 0 and " + (this.numberOfColumns-1) + " inclusive");
    	    int[] rowi = new int[this.numberOfRows - 1];
    	    int[] colj = new int[this.numberOfColumns - 1];
    	    int kk = 0;
    	    for(int i=0; i<this.numberOfRows; i++){
    	        if(i!=ii){
    	            rowi[kk]=i;
    	            kk++;
    	        }
    	    }
    	    kk = 0;
    	    for(int j=0; j<this.numberOfColumns; j++){
    	        if(j!=jj){
    	            colj[kk]=j;
    	            kk++;
    	        }
    	    }
            Matrix aa = this.getSubMatrix(rowi, colj);
            double aadet = aa.determinant();
            return aadet*Math.pow(-1.0, (ii+jj));
        }

    	// REDUCED ROW ECHELON FORM
        public Matrix reducedRowEchelonForm(){

            double[][] mat = new double[this.numberOfRows][this.numberOfColumns];
            for(int i=0; i<this.numberOfRows; i++){
                for(int j=0; j<this.numberOfColumns; j++){
                    mat[i][j] = this.matrix[i][j];
                }
            }

            int leadingCoeff = 0;
            int rowPointer = 0;

            boolean testOuter = true;
            while(testOuter){
                int counter = rowPointer;
                boolean testInner = true;
                while(testInner && mat[counter][leadingCoeff] == 0) {
                    counter++;
                    if(counter == this.numberOfRows){
                        counter = rowPointer;
                        leadingCoeff++;
                        if(leadingCoeff == this.numberOfColumns)testInner=false;
                    }
                }
                if(testInner){
                    double[] temp = mat[rowPointer];
                    mat[rowPointer] = mat[counter];
                    mat[counter] = temp;

                    double pivot = mat[rowPointer][leadingCoeff];
                    for(int j=0; j<this.numberOfColumns; j++)mat[rowPointer][j] /= pivot;

                    for(int i=0; i<this.numberOfRows; i++){
                        if (i!=rowPointer) {
                            pivot = mat[i][leadingCoeff];
                            for (int j=0; j<this.numberOfColumns; j++)mat[i][j] -= pivot * mat[rowPointer][j];
                        }
                    }
                    leadingCoeff++;
                    if(leadingCoeff>=this.numberOfColumns)testOuter = false;
                }
                rowPointer++;
                if(rowPointer>=this.numberOfRows || !testInner)testOuter = false;
            }

            for(int i=0; i<this.numberOfRows; i++){
                for (int j=0; j<this.numberOfColumns; j++){
                    if(mat[i][j]==-0.0)mat[i][j] = 0.0;
                }
            }

            return new Matrix(mat);
        }

    	// FROBENIUS NORM of a matrix
    	// Sometimes referred to as the EUCLIDEAN NORM
    	public double frobeniusNorm(){
        	double norm=0.0D;
        	for(int i=0; i<this.numberOfRows; i++){
            		for(int j=0; j<this.numberOfColumns; j++){
                		norm=hypot(norm, Math.abs(matrix[i][j]));
            		}
        	}
        	return norm;
    	}


    	// ONE NORM of a matrix
    	public double oneNorm(){
        	double norm = 0.0D;
        	double sum = 0.0D;
        	for(int i=0; i<this.numberOfRows; i++){
            		sum=0.0D;
            		for(int j=0; j<this.numberOfColumns; j++){
                		sum+=Math.abs(this.matrix[i][j]);
            		}
            		norm=Math.max(norm,sum);
        	}
        	return norm;
    	}

    	// INFINITY NORM of a matrix
    	public double infinityNorm(){
        	double norm = 0.0D;
        	double sum = 0.0D;
        	for(int i=0; i<this.numberOfRows; i++){
            		sum=0.0D;
            		for(int j=0; j<this.numberOfColumns; j++){
                		sum+=Math.abs(this.matrix[i][j]);
            		}
            		norm=Math.max(norm,sum);
        	}
        	return norm;
    	}

        // SUM OF THE ELEMENTS
        // Returns sum of all elements
        public double sum(){
            double sum = 0.0;
            for(int i=0; i<this.numberOfRows; i++){
                for(int j=0; j<this.numberOfColumns; j++){
                    sum += this.matrix[i][j];
                }
            }
            return sum;
        }

        // Returns sums of the rows
        public double[] rowSums(){
            double[] sums = new double[this.numberOfRows];
            for(int i=0; i<this.numberOfRows; i++){
                sums[i] = 0.0;
                for(int j=0; j<this.numberOfColumns; j++){
                    sums[i] += this.matrix[i][j];
                }
            }
            return sums;
        }

        // Returns sums of the columns
        public double[] columnSums(){
            double[] sums = new double[this.numberOfColumns];
            for(int i=0; i<this.numberOfColumns; i++){
                sums[i] = 0.0;
                for(int j=0; j<this.numberOfRows; j++){
                    sums[i] += this.matrix[j][i];
                }
            }
            return sums;
        }



        // MEAN OF THE ELEMENTS
        // Returns mean of all elements
        public double mean(){
            double mean = 0.0;
            for(int i=0; i<this.numberOfRows; i++){
                for(int j=0; j<this.numberOfColumns; j++){
                    mean += this.matrix[i][j];
                }
            }
            mean /= this.numberOfRows*this.numberOfColumns;
            return mean;
        }

        // Returns means of the rows
        public double[] rowMeans(){
            double[] means = new double[this.numberOfRows];
            for(int i=0; i<this.numberOfRows; i++){
                means[i] = 0.0;
                for(int j=0; j<this.numberOfColumns; j++){
                    means[i] += this.matrix[i][j];
                }
                means[i] /= this.numberOfColumns;
            }
            return means;
        }

        // Returns means of the columns
        public double[] columnMeans(){
            double[] means = new double[this.numberOfColumns];
            for(int i=0; i<this.numberOfColumns; i++){
                means[i] = 0.0;
                for(int j=0; j<this.numberOfRows; j++){
                    means[i] += this.matrix[j][i];
                }
                means[i] /= this.numberOfRows;
            }
            return means;
        }

        // SUBTRACT THE MEAN OF THE ELEMENTS
        // Returns a matrix whose elements are the original elements minus the mean of all elements
        public Matrix subtractMean(){
            Matrix mat = new Matrix(this.numberOfRows, this.numberOfColumns);

            double mean = 0.0;
            for(int i=0; i<this.numberOfRows; i++){
                for(int j=0; j<this.numberOfColumns; j++){
                    mean += this.matrix[i][j];
                }
            }
            mean /= this.numberOfRows*this.numberOfColumns;
            for(int i=0; i<this.numberOfRows; i++){
                for(int j=0; j<this.numberOfColumns; j++){
                    mat.matrix[i][j] = this.matrix[i][j] - mean;
                }
            }
            return mat;
        }

        // Returns a matrix whose rows are the elements are the original row minus the mean of the elements of that row
        public Matrix subtractRowMeans(){
            Matrix mat = new Matrix(this.numberOfRows, this.numberOfColumns);

            for(int i=0; i<this.numberOfRows; i++){
                double mean = 0.0;
                for(int j=0; j<this.numberOfColumns; j++){
                    mean += this.matrix[i][j];
                }
                mean /= this.numberOfColumns;
                for(int j=0; j<this.numberOfColumns; j++){
                    mat.matrix[i][j] = this.matrix[i][j] - mean;
                }
            }
            return mat;
        }

        // Returns matrix whose columns are the elements are the original column minus the mean of the elements of that olumnc
        public Matrix subtractColumnMeans(){
            Matrix mat = new Matrix(this.numberOfRows, this.numberOfColumns);

            for(int i=0; i<this.numberOfColumns; i++){
                double mean = 0.0;
                for(int j=0; j<this.numberOfRows; j++){
                    mean += this.matrix[j][i];
                }
                mean /= this.numberOfRows;
                for(int j=0; j<this.numberOfRows; j++){
                    mat.matrix[j][i] = this.matrix[j][i] - mean;
                }
            }
            return mat;
        }



        // MEDIAN OF THE ELEMENTS
        // Returns median of all elements
        public double median(){
            Stat st = new Stat(this.matrix[0]);

            for(int i=1; i<this.numberOfRows; i++){
                st.concatenate(this.matrix[i]);
            }

            return st.median();
        }

        // Returns medians of the rows
        public double[] rowMedians(){
            double[] medians = new double[this.numberOfRows];
            for(int i=0; i<this.numberOfRows; i++){
                Stat st = new Stat(this.matrix[i]);
                medians[i] = st.median();
            }

            return medians;
        }

        // Returns medians of the columns
        public double[] columnMedians(){
            double[] medians = new double[this.numberOfRows];
            for(int i=0; i<this.numberOfColumns; i++){
                double[] hold = new double[this.numberOfRows];
                for(int j=0; j<this.numberOfRows; j++){
                    hold[i] = this.matrix[j][i];
                }
                Stat st = new Stat(hold);
                medians[i] = st.median();
            }

            return medians;
        }

        // SET THE DENOMINATOR OF THE VARIANCES AND STANDARD DEVIATIONS TO NUMBER OF ELEMENTS, n
        // Default value = n-1
        public void setDenominatorToN(){
            Stat.setStaticDenominatorToN();
        }


        // VARIANCE OF THE ELEMENTS
        // Returns variance of all elements
        public double variance(){
            Stat st = new Stat(this.matrix[0]);

            for(int i=1; i<this.numberOfRows; i++){
                st.concatenate(this.matrix[i]);
            }

            return st.variance();
        }

        // Returns variances of the rows
        public double[] rowVariances(){
            double[] variances = new double[this.numberOfRows];
            for(int i=0; i<this.numberOfRows; i++){
                Stat st = new Stat(this.matrix[i]);
                variances[i] = st.variance();
            }

            return variances;
        }

        // Returns variances of the columns
        public double[] columnVariances(){
            double[] variances = new double[this.numberOfColumns];
            for(int i=0; i<this.numberOfColumns; i++){
                double[] hold = new double[this.numberOfRows];
                for(int j=0; j<this.numberOfRows; j++){
                    hold[i] = this.matrix[j][i];
                }
                Stat st = new Stat(hold);
                variances[i] = st.variance();
            }

            return variances;
        }



        // STANDARD DEVIATION OF THE ELEMENTS
        // Returns standard deviation of all elements
        public double standardDeviation(){
            Stat st = new Stat(this.matrix[0]);

            for(int i=1; i<this.numberOfRows; i++){
                st.concatenate(this.matrix[i]);
            }

            return st.standardDeviation();
        }

        // Returns standard deviations of the rows
        public double[] rowStandardDeviations(){
            double[] standardDeviations = new double[this.numberOfRows];
            for(int i=0; i<this.numberOfRows; i++){
                Stat st = new Stat(this.matrix[i]);
                standardDeviations[i] = st.standardDeviation();
            }

            return standardDeviations;
        }

        // Returns standard deviations of the columns
        public double[] columnStandardDeviations(){
            double[] standardDeviations = new double[this.numberOfColumns];
            for(int i=0; i<this.numberOfColumns; i++){
                double[] hold = new double[this.numberOfRows];
                for(int j=0; j<this.numberOfRows; j++){
                    hold[i] = this.matrix[j][i];
                }
                Stat st = new Stat(hold);
                standardDeviations[i] = st.standardDeviation();
            }

            return standardDeviations;
        }


        // STANDARD ERROR OF THE ELEMENTS
        // Returns standard error of all elements
        public double stanadardError(){
            Stat st = new Stat(this.matrix[0]);

            for(int i=1; i<this.numberOfRows; i++){
                st.concatenate(this.matrix[i]);
            }

            return st.standardError();
        }

        // Returns standard errors of the rows
        public double[] rowStandardErrors(){
            double[] standardErrors = new double[this.numberOfRows];
            for(int i=0; i<this.numberOfRows; i++){
                Stat st = new Stat(this.matrix[i]);
                standardErrors[i] = st.standardError();
            }

            return standardErrors;
        }

        // Returns standard errors of the columns
        public double[] columnStandardErrors(){
            double[] standardErrors = new double[this.numberOfRows];
            for(int i=0; i<this.numberOfColumns; i++){
                double[] hold = new double[this.numberOfRows];
                for(int j=0; j<this.numberOfRows; j++){
                    hold[i] = this.matrix[j][i];
                }
                Stat st = new Stat(hold);
                standardErrors[i] = st.standardError();
            }

            return standardErrors;
        }



    	// MAXIMUM ELEMENT
    	// Returns the value, row index and column index of the maximum element
    	public double[] maximumElement(){
    	    double[] ret = new double[3];
    	    double[] holdD = new double[this.numberOfRows];
    	    ArrayMaths am = null;
    	    int[] holdI = new int [this.numberOfRows];
    	    for(int i=0; i<this.numberOfRows; i++){
    	        am = new ArrayMaths(this.matrix[i]);
    	        holdD[i] = am.maximum();
    	        holdI[i] = am.maximumIndex();
    	    }
    	    am = new ArrayMaths(holdD);
    	    ret[0] = am.maximum();
    	    int maxI = am.maximumIndex();
    	    ret[1] = (double)maxI;
    	    ret[2] = (double)holdI[maxI];

    	    return ret;
    	}

    	// Returns maxima of the rows
        public double[] rowMaxima(){
            double[] maxima = new double[this.numberOfRows];
            for(int i=0; i<this.numberOfRows; i++){
                Stat st = new Stat(this.matrix[i]);
                maxima[i] = st.maximum();
            }

            return maxima;
        }

        // Returns maxima of the columns
        public double[] columnMaxima(){
            double[] maxima = new double[this.numberOfRows];
            for(int i=0; i<this.numberOfColumns; i++){
                double[] hold = new double[this.numberOfRows];
                for(int j=0; j<this.numberOfRows; j++){
                    hold[i] = this.matrix[j][i];
                }
                Stat st = new Stat(hold);
                maxima[i] = st.maximum();
            }

            return maxima;
        }

    	// MINIMUM ELEMENT
    	// Returns the value, row index and column index of the minimum element
    	public double[] minimumElement(){
    	    double[] ret = new double[3];
    	    double[] holdD = new double[this.numberOfRows];
    	    ArrayMaths am = null;
    	    int[] holdI = new int [this.numberOfRows];
    	    for(int i=0; i<this.numberOfRows; i++){
    	        am = new ArrayMaths(this.matrix[i]);
    	        holdD[i] = am.minimum();
    	        holdI[i] = am.minimumIndex();
    	    }
    	    am = new ArrayMaths(holdD);
    	    ret[0] = am.minimum();
    	    int minI = am.minimumIndex();
    	    ret[1] = (double)minI;
    	    ret[2] = (double)holdI[minI];

    	    return ret;
    	}

    	// Returns minima of the rows
        public double[] rowMinima(){
            double[] minima = new double[this.numberOfRows];
            for(int i=0; i<this.numberOfRows; i++){
                Stat st = new Stat(this.matrix[i]);
                minima[i] = st.minimum();
            }

            return minima;
        }

        // Returns minima of the columns
        public double[] columnMinima(){
            double[] minima = new double[this.numberOfRows];
            for(int i=0; i<this.numberOfColumns; i++){
                double[] hold = new double[this.numberOfRows];
                for(int j=0; j<this.numberOfRows; j++){
                    hold[i] = this.matrix[j][i];
                }
                Stat st = new Stat(hold);
                minima[i] = st.minimum();
            }

            return minima;
        }

    	// RANGE
    	// Returns the range of all the elements
    	public double range(){
    	    return this.maximumElement()[0] - this.minimumElement()[0];
    	}

    	// Returns ranges of the rows
        public double[] rowRanges(){
            double[] ranges = new double[this.numberOfRows];
            for(int i=0; i<this.numberOfRows; i++){
                Stat st = new Stat(this.matrix[i]);
                ranges[i] = st.maximum() - st.minimum();
            }

            return ranges;
        }

        // Returns ranges of the columns
        public double[] columnRanges(){
            double[] ranges = new double[this.numberOfRows];
            for(int i=0; i<this.numberOfColumns; i++){
                double[] hold = new double[this.numberOfRows];
                for(int j=0; j<this.numberOfRows; j++){
                    hold[i] = this.matrix[j][i];
                }
                Stat st = new Stat(hold);
                ranges[i] = st.maximum() - st.minimum();
            }

            return ranges;
        }

    	// PIVOT
    	// Swaps rows and columns to place absolute maximum element in positiom matrix[0][0]
    	public int[] pivot(){
    	    double[] max = this.maximumElement();
    	    int maxI = (int)max[1];
    	    int maxJ = (int)max[2];
    	    double[] min = this.minimumElement();
    	    int minI = (int)min[1];
    	    int minJ = (int)min[2];
    	    if(Math.abs(min[0])>Math.abs(max[0])){
    	        maxI = minI;
    	        maxJ = minJ;
    	    }
    	    int[] ret = {maxI, maxJ};

    	    double[] hold1 = this.matrix[0];
    	    this.matrix[0] = this.matrix[maxI];
    	    this.matrix[maxI] = hold1;
    	    double hold2 = 0.0;
    	    for(int i=0; i<this.numberOfRows; i++){
    	        hold2 = this.matrix[i][0];
    	        this.matrix[i][0] = this.matrix[i][maxJ];
    	        this.matrix[i][maxJ] = hold2;
    	    }

    	    return ret;
    	}

        // MATRIX TESTS

        // Check if a matrix is square
    	public boolean isSquare(){
    	    boolean test = false;
    	    if(this.numberOfRows==this.numberOfColumns)test = true;
    	    return test;
    	}

    	// Check if a matrix is symmetric
    	public boolean isSymmetric(){
    	    boolean test = true;
    	    if(this.numberOfRows==this.numberOfColumns){
    	        for(int i=0; i<this.numberOfRows; i++){
    	            for(int j=i+1; j<this.numberOfColumns; j++){
    	                if(this.matrix[i][j]!=this.matrix[j][i])test = false;
    	            }
    	        }
    	    }
    	    else{
    	        test = false;
    	    }
    	    return test;
    	}

    	// Check if a matrix is zero
    	public boolean isZero(){
    	    boolean test = true;
    	    for(int i=0; i<this.numberOfRows; i++){
    	        for(int j=0; j<this.numberOfColumns; j++){
    	            if(this.matrix[i][j]!=0.0D)test = false;
    	        }
    	    }
    	    return test;
    	}

    	// Check if a matrix is unit
    	public boolean isUnit(){
    	    boolean test = true;
    	    for(int i=0; i<this.numberOfRows; i++){
    	        for(int j=0; j<this.numberOfColumns; j++){
    	            if(this.matrix[i][j]!=1.0D)test = false;
    	        }
    	    }
    	    return test;
    	}

    	// Check if a matrix is diagonal
    	public boolean isDiagonal(){
    	    boolean test = true;
    	    for(int i=0; i<this.numberOfRows; i++){
    	        for(int j=0; j<this.numberOfColumns; j++){
    	            if(i!=j && this.matrix[i][j]!=0.0D)test = false;
    	        }
    	    }
    	    return test;
    	}

   	    // Check if a matrix is upper triagonal
    	public boolean isUpperTriagonal(){
    	    boolean test = true;
    	    for(int i=0; i<this.numberOfRows; i++){
    	        for(int j=0; j<this.numberOfColumns; j++){
        	        if(j<i && this.matrix[i][j]!=0.0D)test = false;
    	        }
    	    }
    	    return test;
    	}

    	// Check if a matrix is lower triagonal
    	public boolean isLowerTriagonal(){
    	    boolean test = true;
    	    for(int i=0; i<this.numberOfRows; i++){
    	        for(int j=0; j<this.numberOfColumns; j++){
    	            if(i>j && this.matrix[i][j]!=0.0D)test = false;
    	        }
    	    }
    	    return test;
    	}

    	// Check if a matrix is tridiagonal
    	public boolean isTridiagonal(){
    	    boolean test = true;
    	    for(int i=0; i<this.numberOfRows; i++){
    	        for(int j=0; j<this.numberOfColumns; j++){
    	            if(i<(j+1) && this.matrix[i][j]!=0.0D)test = false;
    	            if(j>(i+1) && this.matrix[i][j]!=0.0D)test = false;
    	        }
    	    }
    	    return test;
    	}

    	// Check if a matrix is upper Hessenberg
    	public boolean isUpperHessenberg(){
    	    boolean test = true;
    	    for(int i=0; i<this.numberOfRows; i++){
    	        for(int j=0; j<this.numberOfColumns; j++){
    	            if(j<(i+1) && this.matrix[i][j]!=0.0D)test = false;
    	        }
    	    }
    	    return test;
    	}

    	// Check if a matrix is lower Hessenberg
    	public boolean isLowerHessenberg(){
    	    boolean test = true;
    	    for(int i=0; i<this.numberOfRows; i++){
    	        for(int j=0; j<this.numberOfColumns; j++){
    	            if(i>(j+1) && this.matrix[i][j]!=0.0D)test = false;
    	        }
    	    }
    	    return test;
    	}

    	// Check if a matrix is a identity matrix
    	public boolean isIdentity(){
    	    boolean test = true;
    	    if(this.numberOfRows==this.numberOfColumns){
    	        for(int i=0; i<this.numberOfRows; i++){
    	            if(this.matrix[i][i]!=1.0D)test = false;
    	            for(int j=i+1; j<this.numberOfColumns; j++){
    	                if(this.matrix[i][j]!=0.0D)test = false;
    	                if(this.matrix[j][i]!=0.0D)test = false;
    	            }
    	        }
    	    }
    	    else{
    	        test = false;
    	    }
    	    return test;
    	}

    	// Check if a matrix is symmetric within a given tolerance
    	public boolean isNearlySymmetric(double tolerance){
    	    boolean test = true;
    	    if(this.numberOfRows==this.numberOfColumns){
    	        for(int i=0; i<this.numberOfRows; i++){
    	            for(int j=i+1; j<this.numberOfColumns; j++){
    	                if(Math.abs(this.matrix[i][j]-this.matrix[j][i])>Math.abs(tolerance))test = false;
    	            }
    	        }
    	    }
    	    else{
    	        test = false;
    	    }
    	    return test;
    	}

    	// Check if a matrix is zero within a given tolerance
    	public boolean isNearlyZero(double tolerance){
    	    boolean test = true;
    	    for(int i=0; i<this.numberOfRows; i++){
    	        for(int j=0; j<this.numberOfColumns; j++){
    	            if(Math.abs(this.matrix[i][j])>Math.abs(tolerance))test = false;
    	        }
    	    }
    	    return test;
    	}

    	// Check if a matrix is unit within a given tolerance
    	public boolean isNearlyUnit(double tolerance){
    	    boolean test = true;
    	    for(int i=0; i<this.numberOfRows; i++){
    	        for(int j=0; j<this.numberOfColumns; j++){
    	            if(Math.abs(this.matrix[i][j] - 1.0D)>Math.abs(tolerance))test = false;
    	        }
    	    }
    	    return test;
    	}


    	// Check if a matrix is upper triagonal within a given tolerance
    	public boolean isNearlyUpperTriagonal(double tolerance){
    	    boolean test = true;
     	    for(int i=0; i<this.numberOfRows; i++){
    	        for(int j=0; j<this.numberOfColumns; j++){
    	            if(j<i && Math.abs(this.matrix[i][j])>Math.abs(tolerance))test = false;
    	        }
    	    }
    	    return test;
    	}

  	    // Check if a matrix is lower triagonal within a given tolerance
    	public boolean isNearlyLowerTriagonal(double tolerance){
    	    boolean test = true;
    	    for(int i=0; i<this.numberOfRows; i++){
    	        for(int j=0; j<this.numberOfColumns; j++){
    	            if(i>j && Math.abs(this.matrix[i][j])>Math.abs(tolerance))test = false;
    	        }
    	    }
    	    return test;
    	}



    	// Check if a matrix is an identy matrix within a given tolerance
    	public boolean isNearlyIdenty(double tolerance){
    	    boolean test = true;
    	    if(this.numberOfRows==this.numberOfColumns){
    	        for(int i=0; i<this.numberOfRows; i++){
    	            if(Math.abs(this.matrix[i][i]-1.0D)>Math.abs(tolerance))test = false;
    	            for(int j=i+1; j<this.numberOfColumns; j++){
    	                if(Math.abs(this.matrix[i][j])>Math.abs(tolerance))test = false;
    	                if(Math.abs(this.matrix[j][i])>Math.abs(tolerance))test = false;
    	            }
    	        }
    	    }
    	    else{
    	        test = false;
    	    }
    	    return test;
    	}

    	// Check if a matrix is tridiagonal within a given tolerance
    	public boolean isTridiagonal(double tolerance){
    	    boolean test = true;
    	    for(int i=0; i<this.numberOfRows; i++){
    	        for(int j=0; j<this.numberOfColumns; j++){
    	            if(i<(j+1) && Math.abs(this.matrix[i][j])>Math.abs(tolerance))test = false;
    	            if(j>(i+1) && Math.abs(this.matrix[i][j])>Math.abs(tolerance))test = false;
    	        }
    	    }
    	    return test;
    	}

    	// Check if a matrix is tridiagonal within a given tolerance
    	public boolean isNearlyTridiagonal(double tolerance){
    	    boolean test = true;
    	    for(int i=0; i<this.numberOfRows; i++){
    	        for(int j=0; j<this.numberOfColumns; j++){
    	            if(i<(j+1) && Math.abs(this.matrix[i][j])>Math.abs(tolerance))test = false;
    	            if(j>(i+1) && Math.abs(this.matrix[i][j])>Math.abs(tolerance))test = false;
    	        }
    	    }
    	    return test;
    	}

    	// Check if a matrix is upper Hessenberg within a given tolerance
    	public boolean isNearlyUpperHessenberg(double tolerance){
    	    boolean test = true;
    	    for(int i=0; i<this.numberOfRows; i++){
    	        for(int j=0; j<this.numberOfColumns; j++){
    	            if(j<(i+1) && Math.abs(this.matrix[i][j])>Math.abs(tolerance))test = false;
    	        }
    	    }
    	    return test;
    	}

    	// Check if a matrix is lower Hessenberg within a given tolerance
    	public boolean isNearlyLowerHessenberg(double tolerance){
    	    boolean test = true;
    	    for(int i=0; i<this.numberOfRows; i++){
    	        for(int j=0; j<this.numberOfColumns; j++){
    	            if(i>(j+1) && Math.abs(this.matrix[i][j])>Math.abs(tolerance))test = false;
    	        }
    	    }
    	    return test;
    	}

    	// Check if a matrix is singular
    	public boolean isSingular(){
    	    boolean test = false;
    	    double det = this.determinant();
    	    if(det==0.0)test = true;
    	    return test;
    	}

    	// Check if a matrix is singular within a given tolerance
    	public boolean isNearlySingular(double tolerance){
    	    boolean test = false;
    	    double det = this.determinant();
    	    if(Math.abs(det)<=Math.abs(tolerance))test = true;
    	    return test;
    	}


    	// Check for identical rows
    	// Returns the number of pairs of identical rows followed by the row indices of the identical row pairs
    	public ArrayList<Integer> identicalRows(){
    	    ArrayList<Integer> ret = new ArrayList<Integer>();
    	    int nIdentical = 0;
    	    for(int i=0; i<this.numberOfRows-1; i++){
    	        for(int j=i+1; j<this.numberOfRows; j++){
    	            int m = 0;
    	            for(int k=0; k<this.numberOfColumns; k++){
    	                if(this.matrix[i][k]==this.matrix[j][k])m++;
    	            }
    	            if(m==this.numberOfColumns){
    	                nIdentical++;
    	                ret.add(new Integer(i));
    	                ret.add(new Integer(j));
    	            }
    	        }
    	    }
    	    ret.add(0,new Integer(nIdentical));
    	    return ret;
    	}

    	// Check for identical columnss
    	// Returns the number of pairs of identical columns followed by the column indices of the identical column pairs
    	public ArrayList<Integer> identicalColumns(){
    	    ArrayList<Integer> ret = new ArrayList<Integer>();
    	    int nIdentical = 0;
    	    for(int i=0; i<this.numberOfColumns; i++){
    	        for(int j=i+1; j<this.numberOfColumns-1; j++){
    	            int m = 0;
    	            for(int k=0; k<this.numberOfRows; k++){
    	                if(this.matrix[k][i]==this.matrix[k][j])m++;
    	            }
    	            if(m==this.numberOfRows){
    	                nIdentical++;
    	                ret.add(new Integer(i));
    	                ret.add(new Integer(j));
    	            }
    	        }
    	    }
    	    ret.add(0,new Integer(nIdentical));
    	    return ret;
    	}

    	// Check for zero rows
    	// Returns the number of columns of all zeros followed by the column indices
    	public ArrayList<Integer> zeroRows(){
    	    ArrayList<Integer> ret = new ArrayList<Integer>();
    	    int nZero = 0;
    	    for(int i=0; i<this.numberOfRows; i++){
    	        int m = 0;
    	        for(int k=0; k<this.numberOfColumns; k++){
    	            if(this.matrix[i][k]==0.0)m++;
    	        }
    	        if(m==this.numberOfColumns){
    	            nZero++;
    	            ret.add(new Integer(i));
    	        }
    	    }
    	    ret.add(0,new Integer(nZero));
    	    return ret;
    	}

    	// Check for zero columns
    	// Returns the number of columns of all zeros followed by the column indices
    	public ArrayList<Integer> zeroColumns(){
    	    ArrayList<Integer> ret = new ArrayList<Integer>();
    	    int nZero = 0;
    	    for(int i=0; i<this.numberOfColumns; i++){
    	        int m = 0;
    	        for(int k=0; k<this.numberOfRows; k++){
    	            if(this.matrix[k][i]==0.0)m++;
    	        }
    	        if(m==this.numberOfRows){
    	            nZero++;
    	            ret.add(new Integer(i));
    	        }
    	    }
    	    ret.add(0,new Integer(nZero));
    	    return ret;
    	}


    	// LU DECOMPOSITION OF MATRIX A
    	// For details of LU decomposition
    	// See Numerical Recipes, The Art of Scientific Computing
    	// by W H Press, S A Teukolsky, W T Vetterling & B P Flannery
	    // Cambridge University Press,   http://www.nr.com/
	    // This method has followed their approach but modified to an object oriented language
	    // Matrix ludmat is the returned LU decompostion
	    // int[] index is the vector of row permutations
	    // rowSwapIndex returns +1.0 for even number of row interchanges
	    //       returns -1.0 for odd number of row interchanges
	    public Matrix luDecomp(){
        	if(this.numberOfRows!=this.numberOfColumns)throw new IllegalArgumentException("A matrix is not square");
        	int n = this.numberOfRows;
	    	int imax = 0;
	    	double dum = 0.0D, temp = 0.0D, big = 0.0D;
	    	double[] vv = new double[n];
	    	double sum = 0.0D;
	    	double dumm = 0.0D;

	    	this.matrixCheck = true;

	      	Matrix ludmat = Matrix.copy(this);
	    	double[][] ludarray = ludmat.getArrayReference();

    		ludmat.rowSwapIndex=1.0D;
	    	for (int i=0;i<n;i++) {
		    	big=0.0D;
		    	for (int j=0;j<n;j++)if  ((temp=Math.abs(ludarray[i][j])) > big) big=temp;
        		if (big == 0.0D){
        		    if(!this.supressErrorMessage){
        	    		System.out.println("Attempted LU Decomposition of a singular matrix in Matrix.luDecomp()");
         	    		System.out.println("NaN matrix returned and matrixCheck set to false");
         	        }
         	        this.matrixCheck=false;
         	    	for(int k=0;k<n;k++)for(int j=0;j<n;j++)ludarray[k][j]=Double.NaN;
         	    	return ludmat;
         		}
    			vv[i]=1.0/big;
	    	}
	    	for (int j=0;j<n;j++) {
		    	for (int i=0;i<j;i++) {
			    	sum=ludarray[i][j];
			    	for (int k=0;k<i;k++) sum -= ludarray[i][k]*ludarray[k][j];
			    	ludarray[i][j]=sum;
		    	}
		    	big=0.0D;
		    	for (int i=j;i<n;i++) {
    				sum=ludarray[i][j];
	    			for (int k=0;k<j;k++)
				    	sum -= ludarray[i][k]*ludarray[k][j];
			    		ludarray[i][j]=sum;
     					if ((dum=vv[i]*Math.abs(sum)) >= big) {
				        	big=dum;
				        	imax=i;
			    		}
		    		}
		    		if (j != imax) {
			    	for (int k=0;k<n;k++) {
				    	dumm=ludarray[imax][k];
				    	ludarray[imax][k]=ludarray[j][k];
    					ludarray[j][k]=dumm;
			    	}
			    	ludmat.rowSwapIndex = -ludmat.rowSwapIndex;
    				vv[imax]=vv[j];
	    		}
		    	ludmat.permutationIndex[j]=imax;

		    	if(ludarray[j][j]==0.0D){
		        	ludarray[j][j]=this.tiny;
		    	}
		    	if(j != n-1) {
			    	dumm=1.0/ludarray[j][j];
    				for (int i=j+1;i<n;i++){
    			    		ludarray[i][j]*=dumm;
	    	    		}
	    		}
	    	}
	    	return ludmat;
	    }

    	// Solves the set of n linear equations A.X=B using not A but its LU decomposition
    	// bvec is the vector B (input)
    	// xvec is the vector X (output)
    	// index is the permutation vector produced by luDecomp()
    	public double[] luBackSub(double[] bvec){
	    	int ii = 0,ip = 0;
	    	int n=bvec.length;
	    	if(n!=this.numberOfColumns)throw new IllegalArgumentException("vector length is not equal to matrix dimension");
	    	if(this.numberOfColumns!=this.numberOfRows)throw new IllegalArgumentException("matrix is not square");
	    	double sum= 0.0D;
	    	double[] xvec=new double[n];
	    	for(int i=0; i<n; i++){
	        	xvec[i]=bvec[i];
	    	}
	    	for (int i=0;i<n;i++) {
		    	ip=this.permutationIndex[i];
		    	sum=xvec[ip];
		    	xvec[ip]=xvec[i];
		    	if (ii==0){
			    	for (int j=ii;j<=i-1;j++){
			        	sum -= this.matrix[i][j]*xvec[j];
			    	}
			    }
		    	else{
		        	if(sum==0.0) ii=i;
		    	}
		    	xvec[i]=sum;
	    	}
	    	for(int i=n-1;i>=0;i--) {
		    	sum=xvec[i];
		    	for (int j=i+1;j<n;j++){
		        	sum -= this.matrix[i][j]*xvec[j];
		    	}
		    	xvec[i]= sum/matrix[i][i];
	    	}
	    	return xvec;
    	}

    	// Solves the set of n linear equations A.X=B
    	// bvec is the vector B (input)
    	// xvec is the vector X (output)
    	public double[] solveLinearSet(double[] bvec){
    	    double[] xvec = null;
    	    if(this.numberOfRows==this.numberOfColumns){
    	        // square matrix - LU decomposition used
        	    Matrix ludmat =	this.luDecomp();
       	        xvec = ludmat.luBackSub(bvec);
       	    }
       	    else{
       	        if(this.numberOfRows>this.numberOfColumns){
       	            // overdetermined equations - least squares used - must be used with care
       	            int n = bvec.length;
       	            if(this.numberOfRows!=n)throw new IllegalArgumentException("Overdetermined equation solution - vector length is not equal to matrix column length");
       	            Matrix avecT = this.transpose();
       	            double[][] avec = avecT.getArrayCopy();
       	            Regression reg = new Regression(avec, bvec);
                    reg.linearGeneral();
                    xvec = reg.getCoeff();
       	        }
       	        else{
       	            throw new IllegalArgumentException("This class does not handle underdetermined equations");
       	        }
       	    }
       	    return xvec;
    	}

    	//Supress printing of LU decompostion failure message
    	public void supressErrorMessage(){
    	    this.supressErrorMessage = true;
    	}


        // HESSENBERG MARTIX

        // Calculates the Hessenberg equivalant of this matrix
        public void hessenbergMatrix(){

            this.hessenberg = this.getArrayCopy();
            double pivot = 0.0D;
            int pivotIndex = 0;
            double hold = 0.0D;

            for(int i = 1; i<this.numberOfRows-1; i++){
                // identify pivot
                pivot = 0.0D;
                pivotIndex = i;
                for(int j=i; j<this.numberOfRows; j++){
                    if(Math.abs(this.hessenberg[j][i-1])> Math.abs(pivot)){
                        pivot = this.hessenberg[j][i-1];
                        pivotIndex = j;
                    }
                }

                // row and column interchange
                if(pivotIndex != i){
                    for(int j = i-1; j<this.numberOfRows; j++){
                        hold = this.hessenberg[pivotIndex][j];
                        this.hessenberg[pivotIndex][j] = this.hessenberg[i][j];
                        this.hessenberg[i][j] = hold;
                    }
                    for(int j = 0; j<this.numberOfRows; j++){
                        hold = this.hessenberg[j][pivotIndex];
                        this.hessenberg[j][pivotIndex] = this.hessenberg[j][i];
                        this.hessenberg[j][i] = hold;
                    }

                    // elimination
                    if(pivot!=0.0){
                        for(int j=i+1; j<this.numberOfRows; j++){
                            hold = this.hessenberg[j][i-1];
                            if(hold!=0.0){
                                hold /= pivot;
                                this.hessenberg[j][i-1] = hold;
                                for(int k=i; k<this.numberOfRows; k++){
                                    this.hessenberg[j][k] -= hold*this.hessenberg[i][k];
                                }
                                for(int k=0; k<this.numberOfRows; k++){
                                    this.hessenberg[k][i] += hold*this.hessenberg[k][j];
                                }
                            }
                        }
                    }
                }
            }
            for(int i = 2; i<this.numberOfRows; i++){
                for(int j = 0; j<i-1; j++){
                    this.hessenberg[i][j] = 0.0;
                }
            }
            this.hessenbergDone = true;
        }

        // return the Hessenberg equivalent
        public double[][] getHessenbergMatrix(){
            if(!hessenbergDone)this.hessenbergMatrix();
            return this.hessenberg;
        }


        // EIGEN VALUES AND EIGEN VECTORS
    	// For a discussion of eigen systems see
    	// Numerical Recipes, The Art of Scientific Computing
    	// by W H Press, S A Teukolsky, W T Vetterling & B P Flannery
	    // Cambridge University Press,   http://www.nr.com/
	    // These methods follow their approach but modified to an object oriented language

        // Return eigen values as calculated
        public double[] getEigenValues(){
            if(!this.eigenDone)symmetricEigen();
            return this.eigenValues;
        }

        // Return eigen values in descending order
        public double[] getSortedEigenValues(){
            if(!this.eigenDone)symmetricEigen();
            return this.sortedEigenValues;
        }

        // Return eigen vectors as calculated as columns
        // Each vector as a column
        public double[][] getEigenVectorsAsColumns(){
            if(!this.eigenDone)symmetricEigen();
            return this.eigenVector;
        }
        // Return eigen vectors as calculated as columns
        // Each vector as a column
        public double[][] getEigenVector(){
            if(!this.eigenDone)symmetricEigen();
            return this.eigenVector;
        }

        // Return eigen vectors as calculated as rows
        // Each vector as a row
        public double[][] getEigenVectorsAsRows(){
            if(!this.eigenDone)symmetricEigen();
            double[][] ret = new double[this.numberOfRows][this.numberOfRows];
            for(int i=0; i<this.numberOfRows;i++){
                for(int j=0; j<this.numberOfRows;j++){
                    ret[i][j] = this.eigenVector[j][i];
                }
            }
            return ret;
        }

        // Return eigen vectors reordered to match a descending order of eigen values
        // Each vector as a column
        public double[][] getSortedEigenVectorsAsColumns(){
            if(!this.eigenDone)symmetricEigen();
            return this.sortedEigenVector;
        }

        // Return eigen vectors reordered to match a descending order of eigen values
        // Each vector as a column
        public double[][] getSortedEigenVector(){
            if(!this.eigenDone)symmetricEigen();
            return this.sortedEigenVector;
        }

        // Return eigen vectors reordered to match a descending order of eigen values
        // Each vector as a row
        public double[][] getSortedEigenVectorsAsRows(){
            if(!this.eigenDone)symmetricEigen();
            double[][] ret = new double[this.numberOfRows][this.numberOfRows];
            for(int i=0; i<this.numberOfRows;i++){
                for(int j=0; j<this.numberOfRows;j++){
                    ret[i][j] = this.sortedEigenVector[j][i];
                }
            }
            return ret;
        }

        // Return the number of rotations used in the Jacobi procedure
        public int getNumberOfJacobiRotations(){
            return this.numberOfRotations;
        }

        // Returns the eigen values and eigen vectors of a symmetric matrix
        // Follows the approach of Numerical methods but adapted to object oriented programming (see above)
        private void symmetricEigen(){

            if(!this.isSymmetric())throw new IllegalArgumentException("matrix is not symmetric");
            double[][] amat = this.getArrayCopy();
            this.eigenVector = new double[this.numberOfRows][this.numberOfRows];
            this.eigenValues = new double[this.numberOfRows];
	        double threshold = 0.0D;
	        double cot2rotationAngle = 0.0D;
	        double tanHalfRotationAngle = 0.0D;
	        double offDiagonalSum = 0.0D;
	        double scaledOffDiagonal = 0.0D;
	        double sElement = 0.0D;
	        double cElement = 0.0D;
	        double sOverC = 0.0D;
	        double vectorDifference = 0.0D;
	        double[] holdingVector1 = new double[this.numberOfRows];
	        double[] holdingVector2 = new double[this.numberOfRows];

	        for(int p=0;p<this.numberOfRows;p++){
		        for(int q=0;q<this.numberOfRows;q++) this.eigenVector[p][q] = 0.0;
		        this.eigenVector[p][p] = 1.0;
	        }
	        for(int p=0;p<this.numberOfRows;p++){
		        holdingVector1[p] = amat[p][p];
		        this.eigenValues[p] = amat[p][p];
		        holdingVector2[p] = 0.0;
	        }
	        this.numberOfRotations = 0;
	        for(int i=1;i<=this.maximumJacobiIterations;i++){
		        offDiagonalSum = 0.0;
		        for(int p=0;p<this.numberOfRows-1;p++){
			        for(int q=p+1;q<this.numberOfRows;q++){
			            offDiagonalSum += Math.abs(amat[p][q]);
			        }
		        }
                if(offDiagonalSum==0.0){
                    this.eigenDone = true;
                    this.eigenSort();
                    return;
                }
		        if (i < 4){
			        threshold = 0.2*offDiagonalSum/(this.numberOfRows*this.numberOfRows);
			    }
		        else{
			        threshold = 0.0;
			    }
		        for(int p=0;p<this.numberOfRows-1;p++){
			        for(int q=p+1;q<this.numberOfRows;q++){
				        scaledOffDiagonal = 100.0*Math.abs(amat[p][q]);
				        if (i > 4 && (Math.abs(this.eigenValues[p]) + scaledOffDiagonal) == Math.abs(this.eigenValues[p]) && (Math.abs(this.eigenValues[q]) + scaledOffDiagonal) == Math.abs(this.eigenValues[q])){
				            amat[p][q] = 0.0;
				        }
				        else if(Math.abs(amat[p][q]) > threshold){
					        vectorDifference = this.eigenValues[q] - this.eigenValues[p];
					        if ((Math.abs(vectorDifference) + scaledOffDiagonal) == Math.abs(vectorDifference))
					            sOverC = amat[p][q]/vectorDifference;
					        else{
						        cot2rotationAngle = 0.5*vectorDifference/amat[p][q];
						        sOverC = 1.0/(Math.abs(cot2rotationAngle) + Math.sqrt(1.0 + cot2rotationAngle*cot2rotationAngle));
						        if (cot2rotationAngle < 0.0) sOverC = -sOverC;
					        }
					        cElement = 1.0/Math.sqrt(1.0 + sOverC*sOverC);
					        sElement = sOverC*cElement;
					        tanHalfRotationAngle = sElement/(1.0 + cElement);
					        vectorDifference = sOverC*amat[p][q];
					        holdingVector2[p] -= vectorDifference;
					        holdingVector2[q] += vectorDifference;
			                this.eigenValues[p] -= vectorDifference;
				            this.eigenValues[q] += vectorDifference;
				            amat[p][q] = 0.0;
					        for(int j=0;j<=p-1;j++) rotation(amat, tanHalfRotationAngle, sElement, j, p, j, q);
					        for(int j=p+1;j<=q-1;j++) rotation(amat, tanHalfRotationAngle, sElement, p, j, j, q);
	                        for(int j=q+1;j<this.numberOfRows;j++) rotation(amat, tanHalfRotationAngle, sElement,p, j, q, j);
					        for(int j=0;j<this.numberOfRows;j++) rotation(this.eigenVector, tanHalfRotationAngle, sElement, j, p, j, q);
            			    ++this.numberOfRotations;
			            }
		            }
		        }
		        for(int p=0;p<this.numberOfRows;p++){
			        holdingVector1[p] += holdingVector2[p];
	                this.eigenValues[p] = holdingVector1[p];
		            holdingVector2[p] = 0.0;
	            }
	        }
	        System.out.println("Maximum iterations, " + this.maximumJacobiIterations + ", reached - values at this point returned");
	        this.eigenDone = true;
            this.eigenSort();
	    }

        // matrix rotaion required by symmetricEigen
	    private void rotation(double[][] a, double tau, double sElement, int i, int j, int k, int l){
            double aHold1 = a[i][j];
            double aHold2 = a[k][l];
            a[i][j] = aHold1 - sElement*(aHold2 + aHold1*tau);
	        a[k][l] = aHold2 + sElement*(aHold1 - aHold2*tau);
        }

        // Sorts eigen values into descending order and rearranges eigen vecors to match
        // follows Numerical Recipes (see above)
        private void eigenSort(){
	        int k = 0;
	        double holdingElement;
	        this.sortedEigenValues = Conv.copy(this.eigenValues);
	        this.sortedEigenVector = Conv.copy(this.eigenVector);
	        this.eigenIndices = new int[this.numberOfRows];

	        for(int i=0; i<this.numberOfRows-1; i++){
		        holdingElement = this.sortedEigenValues[i];
		        k = i;
		        for(int j=i+1; j<this.numberOfRows; j++){
			        if (this.sortedEigenValues[j] >= holdingElement){
			            holdingElement = this.sortedEigenValues[j];
			            k = j;
			        }
			    }
		        if (k != i){
			        this.sortedEigenValues[k] = this.sortedEigenValues[i];
			        this.sortedEigenValues[i] = holdingElement;

			        for(int j=0; j<this.numberOfRows; j++){
				        holdingElement = this.sortedEigenVector[j][i];
				        this.sortedEigenVector[j][i] = this.sortedEigenVector[j][k];
				        this.sortedEigenVector[j][k] = holdingElement;
		            }
	            }
            }
            this.eigenIndices = new int[this.numberOfRows];
            for(int i=0; i<this.numberOfRows; i++){
                boolean test = true;
                int j = 0;
                while(test){
                    if(this.sortedEigenValues[i]==this.eigenValues[j]){
                        this.eigenIndices[i] = j;
                        test = false;
                    }
                    else{
                        j++;
                    }
                }
            }
        }

        // Return indices of the eigen values before sorting into descending order
	    public int[] eigenValueIndices(){
	        if(!this.eigenDone)symmetricEigen();
	        return this.eigenIndices;
        }


        // Method not in java.lang.maths required in this Class
    	// See Fmath.class for public versions of this method
    	private static double hypot(double aa, double bb){
        	double cc = 0.0D, ratio = 0.0D;
        	double amod=Math.abs(aa);
        	double bmod=Math.abs(bb);

        	if(amod==0.0D){
         	   	cc=bmod;
        	}
        	else{
            		if(bmod==0.0D){
                		cc=amod;
            		}
            		else{
                		if(amod<=bmod){
                    			ratio=amod/bmod;
                    			cc=bmod*Math.sqrt(1.0D+ratio*ratio);
                		}
                		else{
                    			ratio=bmod/amod;
                    			cc=amod*Math.sqrt(1.0D+ratio*ratio);
                		}
            		}
        	}
        	return cc;
    	}

}




