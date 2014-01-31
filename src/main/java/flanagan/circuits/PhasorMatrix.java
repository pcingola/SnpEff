/*
*   Class   PhasorMatrix
*
*   Defines a complex matrix and includes the methods
*   needed for standard matrix manipulations, e.g. multiplation,
*   and related procedures, e.g. solution of complex linear
*   simultaneous equations
*
*   See class ComplexMatrix for rectangular complex matrix manipulations
*
* 	WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	    July 2007
*   AMENDED:    19 April 2008, 14 November 2010
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web pages:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/PhasorMatrix.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2007 - 2010
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/

package flanagan.circuits;

import flanagan.math.Fmath;
import flanagan.math.Matrix;
import flanagan.complex.Complex;
import flanagan.complex.ComplexMatrix;

public class PhasorMatrix{

	    private int nrow = 0;               // number of rows
	    private int ncol = 0;               // number of columns
	    private Phasor matrix[][] = null;   // 2-D Phasor Matrix
	    private int index[] = null;         // row permutation index
	    private double dswap = 1.0D;        // row swap index
	    private static final double TINY = 1.0e-30;

/*********************************************************/

	    // CONSTRUCTORS
	    // Construct a nrow x ncol matrix of complex variables all equal to zero
    	public PhasorMatrix(int nrow, int ncol){
		    this.nrow = nrow;
		    this.ncol = ncol;
	    	this.matrix = Phasor.twoDarray(nrow, ncol);
		    this.index = new int[nrow];
        	for(int i=0;i<nrow;i++)this.index[i]=i;
        	this.dswap=1.0;
    	}

	    // Construct a nrow x ncol matrix of complex variables all equal to the complex number const
    	public PhasorMatrix(int nrow, int ncol, Phasor constant){
		    this.nrow = nrow;
		    this.ncol = ncol;
		    this.matrix = Phasor.twoDarray(nrow, ncol, constant);
		    this.index = new int[nrow];
        	for(int i=0;i<nrow;i++)this.index[i]=i;
        	this.dswap=1.0;
	    }

	    // Construct matrix with a reference to an existing nrow x ncol 2-D array of complex variables
        public PhasorMatrix(Phasor[][] twoD){
		    this.nrow = twoD.length;
		    this.ncol = twoD[0].length;
		    for(int i=0; i<nrow; i++){
		        if(twoD[i].length!=ncol)throw new IllegalArgumentException("All rows must have the same length");
		    }
		    this.matrix = twoD;
		    this.index = new int[nrow];
	        for(int i=0;i<nrow;i++)this.index[i]=i;
        	this.dswap=1.0;
	    }

	    // Construct matrix with a reference to the 2D matrix and permutation index of an existing PhasorMatrix bb.
        public PhasorMatrix(PhasorMatrix bb){
		    this.nrow = bb.nrow;
		    this.ncol = bb.ncol;
		    this.matrix = bb.matrix;
		    this.index = bb.index;
        	this.dswap = bb.dswap;
	    }


    	// SET VALUES
    	// Set the matrix with a copy of an existing nrow x ncol 2-D matrix of Phasor variables
    	public void setTwoDarray(Phasor[][] aarray){
		    if(this.nrow != aarray.length)throw new IllegalArgumentException("row length of this PhasorMatrix differs from that of the 2D array argument");
		    if(this.ncol != aarray[0].length)throw new IllegalArgumentException("column length of this PhasorMatrix differs from that of the 2D array argument");
		    for(int i=0; i<nrow; i++){
		    	if(aarray[i].length!=ncol)throw new IllegalArgumentException("All rows must have the same length");
    			for(int j=0; j<ncol; j++){
    		    		this.matrix[i][j]=Phasor.copy(aarray[i][j]);
    			}
		    }
	    }

    	// Set an individual array element
    	// i = row index
    	// j = column index
    	// aa = value of the element
    	public void setElement(int i, int j, Phasor aa){
        	this.matrix[i][j]=Phasor.copy(aa);
    	}

    	// Set an individual array element
    	// i = row index
    	// j = column index
    	// aa = magnitude of the element
    	// bb = phase of the element
    	public void setElement(int i, int j, double aa, double bb){
        	this.matrix[i][j].reset(aa, bb);
    	}

        // Set a sub-matrix starting with row index i, column index j

    	public void setSubMatrix(int i, int j, Phasor[][] subMatrix){
    	    int k = subMatrix.length;
    	    int l = subMatrix[0].length;
        	if(i>k)throw new IllegalArgumentException("row indices inverted");
        	if(j>l)throw new IllegalArgumentException("column indices inverted");
        	int n=k-i+1, m=l-j+1;
        	for(int p=0; p<n; p++){
            		for(int q=0; q<m; q++){
                		this.matrix[i+p][j+q] = Phasor.copy(subMatrix[p][q]);
            		}
        	}
    	}

    	// Set a sub-matrix starting with row index i, column index j
    	// and ending with row index k, column index l
    	public void setSubMatrix(int i, int j, int k, int l, Phasor[][] subMatrix){
        	if(i+k-1>=this.nrow)throw new IllegalArgumentException("Sub-matrix position is outside the row bounds of this Matrix");
        	if(j+l-1>=this.ncol)throw new IllegalArgumentException("Sub-matrix position is outside the column bounds of this Matrix");
        	int n=k-i+1, m=l-j+1;
        	for(int p=0; p<n; p++){
            		for(int q=0; q<m; q++){
                		this.matrix[i+p][j+q] = Phasor.copy(subMatrix[p][q]);
            		}
        	}
    	}


    	// Set a sub-matrix
    	// row = array of row indices
    	// col = array of column indices
    	public void setSubMatrix(int[] row, int[] col, Phasor[][] subMatrix){
        	int n=row.length;
        	int m=col.length;
        	for(int p=0; p<n; p++){
            		for(int q=0; q<m; q++){
                		this.matrix[row[p]][col[q]] = Phasor.copy(subMatrix[p][q]);
            		}
        	}
    	}

    	// SPECIAL MATRICES
    	// Construct a Phasor identity matrix
    	public static PhasorMatrix identityMatrix(int nrow){
        	PhasorMatrix u = new PhasorMatrix(nrow, nrow);
        	for(int i=0; i<nrow; i++){
            		u.matrix[i][i]=Phasor.plusOne();
        	}
        	return u;
    	}

    	// Construct a Phasor scalar matrix
    	public static PhasorMatrix scalarMatrix(int nrow, Phasor diagconst){
        	PhasorMatrix u = new PhasorMatrix(nrow, nrow);
        	Phasor[][] uarray = u.getArrayReference();
        	for(int i=0; i<nrow; i++){
            		for(int j=i; j<nrow; j++){
                		if(i==j){
                    			uarray[i][j]=Phasor.copy(diagconst);
                		}
            		}
        	}
        	return u;
    	}

    	// Construct a Phasor diagonal matrix
    	public static PhasorMatrix diagonalMatrix(int nrow, Phasor[] diag){
        	if(diag.length!=nrow)throw new IllegalArgumentException("matrix dimension differs from diagonal array length");
        	PhasorMatrix u = new PhasorMatrix(nrow, nrow);
        	Phasor[][] uarray = u.getArrayReference();
        	for(int i=0; i<nrow; i++){
            		for(int j=i; j<nrow; j++){
                		if(i==j){
                    			uarray[i][j]=Phasor.copy(diag[i]);
                		}
            		}
        	}
        	return u;
    	}

        // COLUMN MATRICES
    	// Converts a 1-D array of Phasor to a column  matrix
        public static PhasorMatrix columnMatrix(Phasor[] darray){
            int nr = darray.length;
            PhasorMatrix pp = new PhasorMatrix(nr, 1);
            for(int i=0; i<nr; i++)pp.matrix[i][0] = darray[i];
            return pp;
        }

        // ROW MATRICES
    	// Converts a 1-D array of Phasor to a row matrix
        public static PhasorMatrix rowMatrix(Phasor[] darray){
            int nc = darray.length;
            PhasorMatrix pp = new PhasorMatrix(1, nc);
            for(int i=0; i<nc; i++)pp.matrix[0][i] = darray[i];
            return pp;
        }


    	// GET VALUES
        // Return the number of rows
    	public int getNrow(){
        	return this.nrow;
    	}

    	// Return the number of columns
    	public int getNcol(){
        	return this.ncol;
    	}

    	// Return a reference to the internal 2-D array
    	public Phasor[][] getArrayReference(){
        	return this.matrix;
    	}

    	// Return a reference to the internal 2-D array
    	public Phasor[][] getArray(){
        	return this.matrix;
    	}

    	// Return a reference to the internal 2-D array
    	// included for backward compatibility with earlier incorrect documentation
    	public Phasor[][] getArrayPointer(){
        	return this.matrix;
    	}

    	// Return a copy of the internal 2-D array
    	public Phasor[][] getArrayCopy(){
        	Phasor[][] c = new Phasor[this.nrow][this.ncol];
		for(int i=0; i<nrow; i++){
		    	for(int j=0; j<ncol; j++){
		        	c[i][j]=Phasor.copy(matrix[i][j]);
		    	}
		}
        	return c;
    	}

        // Return a single element of the internal 2-D array
    	public Phasor getElementReference(int i, int j){
         	return this.matrix[i][j];
    	}

    	// Return a reference to a single element of the internal 2-D array
    	// included for backward compatibility with earlier incorrect documentation
    	public Phasor getElementPointer(int i, int j){
         	return this.matrix[i][j];
    	}

    	// Return a copy of a single element of the internal 2-D array
    	public Phasor getElementCopy(int i, int j){
         	return Phasor.copy(this.matrix[i][j]);
    	}

    	// Return a sub-matrix starting with row index i, column index j
    	// and ending with column index k, row index l
    	public PhasorMatrix getSubMatrix(int i, int j, int k, int l){
         	if(i+k-1>=this.nrow)throw new IllegalArgumentException("Sub-matrix position is outside the row bounds of this Matrix");
        	if(j+l-1>=this.ncol)throw new IllegalArgumentException("Sub-matrix position is outside the column bounds of this Matrix");

        	int n=k-i+1, m=l-j+1;
        	PhasorMatrix subMatrix = new PhasorMatrix(n, m);
        	Phasor[][] sarray = subMatrix.getArrayReference();
        	for(int p=0; p<n; p++){
            		for(int q=0; q<m; q++){
                		sarray[p][q]=Phasor.copy(this.matrix[i+p][j+q]);
            		}
        	}
        	return subMatrix;
    	}

    	// Return a sub-matrix
    	// row = array of row indices
    	// col = array of column indices
    	public PhasorMatrix getSubMatrix(int[] row, int[] col){
        	int n = row.length;
        	int m = col.length;
        	PhasorMatrix subMatrix = new PhasorMatrix(n, m);
        	Phasor[][] sarray = subMatrix.getArrayReference();
        	for(int i=0; i<n; i++){
            		for(int j=0; j<m; j++){
                		sarray[i][j]=Phasor.copy(this.matrix[row[i]][col[j]]);
            		}
        	}
        	return subMatrix;
    	}

    	// Return a reference to the permutation index array
    	public int[]  getIndexReference(){
         	return this.index;
    	}

    	// Return a reference to the permutation index array
    	public int[]  getIndexPointer(){
         	return this.index;
    	}

    	// Return a copy of the permutation index array
    	public int[]  getIndexCopy(){
        	int[] indcopy = new int[this.nrow];
        	for(int i=0; i<this.nrow; i++){
            		indcopy[i]=this.index[i];
        	}
        	return indcopy;
    	}

    	// Return the row swap index
    	public double getSwap(){
         	return this.dswap;
    	}

    	// COPY
    	// Copy a PhasorMatrix [static method]
    	public static PhasorMatrix copy(PhasorMatrix a){
    	    if(a==null){
    	        return null;
    	    }
    	    else{
        	    int nr = a.getNrow();
        	    int nc = a.getNcol();
        	    Phasor[][] aarray = a.getArrayReference();
        	    PhasorMatrix b = new PhasorMatrix(nr,nc);
        	    b.nrow = nr;
        	    b.ncol = nc;
        	    Phasor[][] barray = b.getArrayReference();
        	    for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		barray[i][j]=Phasor.copy(aarray[i][j]);
        	    	}
        	    }
        	    for(int i=0; i<nr; i++)b.index[i] = a.index[i];
        	    return b;
        	}
    	}

    	// Copy a PhasorMatrix [instance method]
    	public PhasorMatrix copy(){
    	    if(this==null){
    	        return null;
    	    }
    	    else{
        	    int nr = this.nrow;
        	    int nc = this.ncol;
        	    PhasorMatrix b = new PhasorMatrix(nr,nc);
        	    Phasor[][] barray = b.getArrayReference();
        	    b.nrow = nr;
        	    b.ncol = nc;
        	    for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		barray[i][j]=Phasor.copy(this.matrix[i][j]);
            		}
        	    }
        	    for(int i=0; i<nr; i++)b.index[i] = this.index[i];
        	    return b;
        	}
    	}

    	// Clone a PhasorMatrix
    	public Object clone(){
    	    if(this==null){
    	        return null;
    	    }
    	    else{
        	    int nr = this.nrow;
        	    int nc = this.ncol;
        	    PhasorMatrix b = new PhasorMatrix(nr,nc);
        	    Phasor[][] barray = b.getArrayReference();
        	    b.nrow = nr;
        	    b.ncol = nc;
        	    for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		barray[i][j]=Phasor.copy(this.matrix[i][j]);
            		}
        	    }
        	    for(int i=0; i<nr; i++)b.index[i] = this.index[i];
        	    return (Object) b;
        	}
    	}

        // CONVERSIONS
        // Converts a 1-D array of Phasors to a row phasor matrix
        public static PhasorMatrix toPhasorRowMatrix(Phasor[] parray){
            int nc = parray.length;
            PhasorMatrix pp = new PhasorMatrix(1, nc);
            for(int i=0; i<nc; i++)pp.matrix[0][i] = parray[i].copy();
            return pp;
        }

        // Converts a 1-D array of Complex to a row phasor matrix
        public static PhasorMatrix toPhasorRowMatrix(Complex[] carray){
            int nc = carray.length;
            PhasorMatrix pp = new PhasorMatrix(1, nc);
            for(int i=0; i<nc; i++)pp.matrix[0][i] = Phasor.toPhasor(carray[i]).copy();
            return pp;
        }

        // Converts a 1-D array of doubles to a row phasor matrix
        public static PhasorMatrix toPhasorRowMatrix(double[] darray){
            int nc = darray.length;
            PhasorMatrix pp = new PhasorMatrix(1, nc);
            for(int i=0; i<nc; i++)pp.matrix[0][i] = new Phasor(darray[i], 0.0D);
            return pp;
        }

        // Converts a 1-D array of Phasors to a column phasor matrix
        public static PhasorMatrix toPhasorColumnMatrix(Phasor[] parray){
            int nr = parray.length;
            PhasorMatrix pp = new PhasorMatrix(nr, 1);
            for(int i=0; i<nr; i++)pp.matrix[i][0] = parray[i].copy();
            return pp;
        }

        // Converts a 1-D array of Complex to a column phasor matrix
        public static PhasorMatrix toPhasorColumnMatrix(Complex[] carray){
            int nr = carray.length;
            PhasorMatrix pp = new PhasorMatrix(nr, 1);
            for(int i=0; i<nr; i++)pp.matrix[i][0] = Phasor.toPhasor(carray[i]).copy();
            return pp;
        }

        // Converts a 1-D array of doubles to a column phasor matrix
        public static PhasorMatrix toPhasorColumnMatrix(double[] darray){
            int nr = darray.length;
            PhasorMatrix pp = new PhasorMatrix(nr, 1);
            for(int i=0; i<nr; i++)pp.matrix[i][0] = new Phasor(darray[i], 0.0D);
            return pp;
        }

        // Converts a complex matrix (ComplexMatrix) to a phasor matrix (PhasorMatix)
        public static PhasorMatrix toPhasorMatrix(ComplexMatrix cc){
            PhasorMatrix pp = new PhasorMatrix(cc.getNrow(), cc.getNcol() );
            pp.index = cc.getIndexCopy();
            pp.dswap = cc.getSwap();
            for(int i=0; i<pp.nrow; i++){
                for(int j=0; j<pp.ncol; i++){
                    pp.matrix[i][j] = Phasor.toPhasor(cc.getElementCopy(i,j));
                }
            }
            return pp;
        }

        // Converts a 2D complex array to a phasor matrix (PhasorMatix)
        public static PhasorMatrix toPhasorMatrix(Complex[][] carray){
            ComplexMatrix cc = new ComplexMatrix(carray);
            PhasorMatrix pp = new PhasorMatrix(cc.getNrow(), cc.getNcol() );
            for(int i=0; i<pp.nrow; i++){
                for(int j=0; j<pp.ncol; i++){
                    pp.matrix[i][j] = Phasor.toPhasor(cc.getElementCopy(i,j));
                }
            }
            return pp;
        }

        // Converts a matrix of doubles (Matrix) to a phasor matrix (PhasorMatix)
        public static PhasorMatrix toPhasorMatrix(Matrix marray){
            int nr = marray.getNrow();
		    int nc = marray.getNcol();

            PhasorMatrix pp = new PhasorMatrix(nr, nc);
            for(int i=0; i<nr; i++){
                for(int j=0; j<nc; j++){
                    pp.matrix[i][j].reset(marray.getElementCopy(i, j), 0.0D);
                }
            }
            return pp;
        }

        // Converts a 2D array of doubles to a phasor matrix (PhasorMatix)
        public static PhasorMatrix toPhasorMatrix(double[][] darray){
            int nr = darray.length;
		    int nc = darray[0].length;
		    for(int i=1; i<nr; i++){
		        if(darray[i].length!=nc)throw new IllegalArgumentException("All rows must have the same length");
		    }
            PhasorMatrix pp = new PhasorMatrix(nr, nc);
            for(int i=0; i<pp.nrow; i++){
                for(int j=0; j<pp.ncol; j++){
                    pp.matrix[i][j].reset(darray[i][j], 0.0D);
                }
            }
            return pp;
        }

        // Converts a phasor matrix (PhasorMatix) to a complex matrix (ComplexMatrix) - instance method
        public ComplexMatrix toComplexMatrix(){
            int nr = this.getNrow();
            int nc = this.getNcol();
            ComplexMatrix cc = new ComplexMatrix(nr, nc);
            for(int i=0; i<nr; i++){
                for(int j=0; j<nc; i++){
                    cc.setElement(i, j, this.matrix[i][j].toRectangular());
                }
            }
            return cc;
        }

        // Converts a phasor matrix (PhasorMatix) to a complex matrix (ComplexMatrix) - static method
        public static ComplexMatrix toComplexMatrix(PhasorMatrix pp){
            int nr = pp.getNrow();
            int nc = pp.getNcol();
            ComplexMatrix cc = new ComplexMatrix(nr, nc);
            for(int i=0; i<nr; i++){
                for(int j=0; j<nc; i++){
                    cc.setElement(i, j, pp.matrix[i][j].toRectangular());
                }
            }
            return cc;
        }


    	// ADDITION
    	// Add this matrix to matrix B.  This matrix remains unaltered
    	public PhasorMatrix plus(PhasorMatrix bmat){
        	if((this.nrow!=bmat.nrow)||(this.ncol!=bmat.ncol)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	int nr=bmat.nrow;
        	int nc=bmat.ncol;
        	PhasorMatrix cmat = new PhasorMatrix(nr,nc);
        	Phasor[][] carray = cmat.getArrayReference();
        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		carray[i][j]=this.matrix[i][j].plus(bmat.matrix[i][j]);
            		}
        	}
       	 	return cmat;
    	}

    	// Add this matrix to a Phasor 2-D array.
    	public PhasorMatrix plus(Phasor[][] bmat){
    	    int nr=bmat.length;
        	int nc=bmat[0].length;
        	if((this.nrow!=nr)||(this.ncol!=nc)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	PhasorMatrix cmat = new PhasorMatrix(nr,nc);
        	Phasor[][] carray = cmat.getArrayReference();
        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		carray[i][j]=this.matrix[i][j].plus(bmat[i][j]);
            		}
        	}
       	 	return cmat;
    	}

    	// Add this PhasorMatrix to a ComplexMatrix.
    	public PhasorMatrix plus(ComplexMatrix bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    return this.plus(pmat);
    	}

    	// Add this PhasorMatrix to a 2D array of Complex.
    	public PhasorMatrix plus(Complex[][] bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    return this.plus(pmat);
    	}

    	// Add this PhasorMatrix to a Matrix.
    	public PhasorMatrix plus(Matrix bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    return this.plus(pmat);
    	}

    	// Add this PhasorMatrix to a 2D array of double.
    	public PhasorMatrix plus(double[][] bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    return this.plus(pmat);
    	}

    	// Add a PhasorMatrix to this matrix [equivalence of +=]
    	public void plusEquals(PhasorMatrix bmat){
        	if((this.nrow!=bmat.nrow)||(this.ncol!=bmat.ncol)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	int nr=bmat.nrow;
        	int nc=bmat.ncol;

        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		this.matrix[i][j].plusEquals(bmat.matrix[i][j]);
            		}
        	}
    	}

    	// Add a 2D array of Phasors to this matrix [equivalence of +=]
    	public void plusEquals(Phasor[][] bmat){
    	    PhasorMatrix pmat = new PhasorMatrix(bmat);
    	    this.plusEquals(pmat);
    	}

    	// Add a ComplexMatrix of complex to this matrix [equivalence of +=]
    	public void plusEquals(ComplexMatrix bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    this.plusEquals(pmat);
    	}

    	// Add a 2D array of complex to this matrix [equivalence of +=]
    	public void plusEquals(Complex[][] bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    this.plusEquals(pmat);
    	}

    	// Add a Matrix to this PhasorMatrix [equivalence of +=]
    	public void plusEquals(Matrix bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    this.plusEquals(pmat);
    	}

    	// Add a 2D array of doubles to this matrix [equivalence of +=]
    	public void plusEquals(double[][] bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    this.plusEquals(pmat);
    	}

    	// SUBTRACTION
    	// Subtract matrix B from this matrix.   This matrix remains unaltered [instance method]
    	public PhasorMatrix minus(PhasorMatrix bmat){
        	if((this.nrow!=bmat.nrow)||(this.ncol!=bmat.ncol)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	int nr=this.nrow;
        	int nc=this.ncol;
        	PhasorMatrix cmat = new PhasorMatrix(nr,nc);
        	Phasor[][] carray = cmat.getArrayReference();
        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		carray[i][j]=this.matrix[i][j].minus(bmat.matrix[i][j]);
            		}
        	}
        	return cmat;
    	}

 	    // Subtract  Phasor 2-D array from this matrix.  [instance method]
    	public PhasorMatrix minus(Phasor[][] bmat){
    	    int nr=bmat.length;
        	int nc=bmat[0].length;
        	if((this.nrow!=nr)||(this.ncol!=nc)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	PhasorMatrix cmat = new PhasorMatrix(nr,nc);
        	Phasor[][] carray = cmat.getArrayReference();
        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		carray[i][j]=this.matrix[i][j].minus(bmat[i][j]);
            		}
        	}
       	 	return cmat;
    	}

        // Subtract a ComplexMatrix from this PhasorMatrix
   	    public PhasorMatrix minus(ComplexMatrix bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    return this.minus(pmat);
    	}

    	// Subtract a 2D array of Complex from this PhasorMatrix.
    	public PhasorMatrix minus(Complex[][] bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    return this.minus(pmat);
    	}

    	// Subtract a Matrix from this PhasorMatrix.
    	public PhasorMatrix minus(Matrix bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    return this.minus(pmat);
    	}

    	// Subtract a 2D array of doubles from this PhasorMatrix.
    	public PhasorMatrix minus(double[][] bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    return this.minus(pmat);
    	}


    	// Subtract a PhasorMatrix from this matrix [equivalence of -=]
    	public void minusEquals(PhasorMatrix bmat){
        	if((this.nrow!=bmat.nrow)||(this.ncol!=bmat.ncol)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	int nr=bmat.nrow;
        	int nc=bmat.ncol;

        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		this.matrix[i][j].minusEquals(bmat.matrix[i][j]);
            		}
        	}
    	}

    	// Subtract a 2D array of Phasors from this matrix [equivalence of -=]
    	public void minusEquals(Phasor[][] bmat){
    	    PhasorMatrix pmat = new PhasorMatrix(bmat);
    	    this.minusEquals(pmat);
    	}

    	// Subtract a ComplexMatrix from this matrix [equivalence of -=]
    	public void minusEquals(ComplexMatrix bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    this.minusEquals(pmat);
    	}

    	// Subtract a 2D array of complex from this matrix [equivalence of -=]
    	public void minusEquals(Complex[][] bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    this.minusEquals(pmat);
    	}

    	// Subtract a Matrix from this phasorMatrix [equivalence of -=]
    	public void minusEquals(Matrix bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    this.minusEquals(pmat);
    	}

    	// Subtract a 2D array of doubles from this matrix [equivalence of -=]
    	public void minusEquals(double[][] bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    this.minusEquals(pmat);
    	}

    	// MULTIPLICATION
    	// Multiply this Phasor matrix by a Phasor matrix.
    	// This matrix remains unaltered.
   	    public PhasorMatrix times(PhasorMatrix bmat){
        	if(this.ncol!=bmat.nrow)throw new IllegalArgumentException("Nonconformable matrices");

        	PhasorMatrix cmat = new PhasorMatrix(this.nrow, bmat.ncol);
        	Phasor [][] carray = cmat.getArrayReference();
        	Phasor sum = new Phasor();

        	for(int i=0; i<this.nrow; i++){
            		for(int j=0; j<bmat.ncol; j++){
                		sum=Phasor.zero();
                		for(int k=0; k<this.ncol; k++){
                       			sum.plusEquals(this.matrix[i][k].times(bmat.matrix[k][j]));
                		}
                		carray[i][j]=Phasor.copy(sum);
            		}
        	}
        	return cmat;
    	}

    	// Multiply this Phasor matrix by a Phasor 2-D array.
   	    public PhasorMatrix times(Phasor[][] bmat){
   	        int nr=bmat.length;
        	int nc=bmat[0].length;
        	if(this.ncol!=nr)throw new IllegalArgumentException("Nonconformable matrices");

        	PhasorMatrix cmat = new PhasorMatrix(this.nrow, nc);
        	Phasor [][] carray = cmat.getArrayReference();
        	Phasor sum = new Phasor();

        	for(int i=0; i<this.nrow; i++){
            		for(int j=0; j<nc; j++){
                		sum=Phasor.zero();
                		for(int k=0; k<this.ncol; k++){
                       			sum.plusEquals(this.matrix[i][k].times(bmat[k][j]));
                		}
                		carray[i][j]=Phasor.copy(sum);
            		}
        	}
        	return cmat;
    	}

        // Multiply a ComplexMatrix by this PhasorMatrix
   	    public PhasorMatrix times(ComplexMatrix bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    return this.times(pmat);
    	}

    	// Multiply a 2D array of Complex by this PhasorMatrix.
    	public PhasorMatrix times(Complex[][] bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    return this.times(pmat);
    	}

    	// Multiply a Matrix by this PhasorMatrix.
    	public PhasorMatrix times(Matrix bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    return this.times(pmat);
    	}

    	// Multiply a 2D array of doubles by this PhasorMatrix.
    	public PhasorMatrix times(double[][] bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    return this.times(pmat);
    	}

    	// Multiply this Phasor matrix by a Phasor constant
    	// This matrix remains unaltered
    	public PhasorMatrix times(Phasor constant){
        	PhasorMatrix cmat = new PhasorMatrix(this.nrow, this.ncol);
        	Phasor [][] carray = cmat.getArrayReference();

        	for(int i=0; i<this.nrow; i++){
            		for(int j=0; j<this.ncol; j++){
                  		carray[i][j] = this.matrix[i][j].times(constant);
            		}
        	}
        	return cmat;
    	}

    	// Multiply this Phasor matrix by a real (double) constant
    	// This matrix remains unaltered.
    	public PhasorMatrix times(double constant){
        	PhasorMatrix cmat = new PhasorMatrix(this.nrow, this.ncol);
        	Phasor [][] carray = cmat.getArrayReference();
        	Phasor cconstant = new Phasor(constant, 0.0);

        	for(int i=0; i<this.nrow; i++){
            		for(int j=0; j<this.ncol; j++){
                  		carray[i][j] = this.matrix[i][j].times(cconstant);
            		}
        	}
        	return cmat;
    	}


    	// Multiply this matrix by a Phasor matrix [equivalence of *=]
    	public void timesEquals(PhasorMatrix bmat){
        	if(this.ncol!=bmat.nrow)throw new IllegalArgumentException("Nonconformable matrices");

        	Phasor sum = new Phasor();

        	for(int i=0; i<this.nrow; i++){
            		for(int j=0; j<bmat.ncol; j++){
                		sum=Phasor.zero();
                		for(int k=0; k<this.ncol; k++){
                       			sum.plusEquals(this.matrix[i][k].times(bmat.matrix[k][j]));
                		}
                		this.matrix[i][j] = Phasor.copy(sum);
            		}
        	}
    	}

    // Multiply a 2D array of Phasors by this matrix [equivalence of *=]
    	public void timesEquals(Phasor[][] bmat){
    	    PhasorMatrix pmat = new PhasorMatrix(bmat);
    	    this.timesEquals(pmat);
    	}

    	// Multiply a ComplexMatrix of complex by this matrix [equivalence of *=]
    	public void timesEquals(ComplexMatrix bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    this.timesEquals(pmat);
    	}

    	// Multiply a 2D array of complex by this matrix [equivalence of *=]
    	public void timesEquals(Complex[][] bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    this.timesEquals(pmat);
    	}

    	// Multiply a Matrix by this PhasorMatrix [equivalence of *=]
    	public void timesEquals(Matrix bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    this.timesEquals(pmat);
    	}

    	// Multiply a 2D array of doubles by this matrix [equivalence of *=]
    	public void timesEquals(double[][] bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    this.timesEquals(pmat);
    	}

   	    // Multiply this matrix by a Phasor constant [equivalence of *=]
    	public void timesEquals(Phasor constant){

        	for(int i=0; i<this.nrow; i++){
            		for(int j=0; j<this.ncol; j++){
                  		this.matrix[i][j].timesEquals(constant);
            		}
        	}
    	}

   	    // Multiply this matrix by a Complex constant [equivalence of *=]
    	public void timesEquals(Complex constant){

        	for(int i=0; i<this.nrow; i++){
            		for(int j=0; j<this.ncol; j++){
                  		this.matrix[i][j].timesEquals(constant);
            		}
        	}
    	}

   	    // Multiply this matrix by a real (double) constant [equivalence of *=]
    	public void timesEquals(double constant){
        	Phasor cconstant = new Phasor(constant, 0.0);

        	for(int i=0; i<this.nrow; i++){
            		for(int j=0; j<this.ncol; j++){
                  		this.matrix[i][j].timesEquals(cconstant);
            		}
        	}
    	}

   	    // Multiply this matrix by a real integer(int) constant [equivalence of *=]
    	public void timesEquals(int constant){
        	Phasor cconstant = new Phasor((double)constant, 0.0);

        	for(int i=0; i<this.nrow; i++){
            		for(int j=0; j<this.ncol; j++){
                  		this.matrix[i][j].timesEquals(cconstant);
            		}
        	}
    	}

    	// DIVISION
    	// Divide this PhasorMatrix by a PhasorMatrix.
    	public PhasorMatrix over(PhasorMatrix bmat){
        	if((this.nrow!=bmat.nrow)||(this.ncol!=bmat.ncol)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	return this.times(bmat.inverse());
    	}

    	// Divide this matrix by a Phasor 2-D array.
    	public PhasorMatrix over(Phasor[][] bmat){
    	    int nr=bmat.length;
        	int nc=bmat[0].length;
        	if((this.nrow!=nr)||(this.ncol!=nc)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}

        	PhasorMatrix cmat = new PhasorMatrix(bmat);
        	return this.times(cmat.inverse());
    	}

    	// Divide this PhasorMatrix by a ComplexMatrix.
    	public PhasorMatrix over(ComplexMatrix bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    return this.over(pmat);
    	}

    	// Divide this PhasorMatrix by a 2D array of Complex.
    	public PhasorMatrix over(Complex[][] bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    return this.over(pmat);
    	}

    	// Divide this PhasorMatrix by a Matrix.
    	public PhasorMatrix over(Matrix bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    return this.over(pmat);
    	}

    	// Divide this PhasorMatrix by a 2D array of double.
    	public PhasorMatrix over(double[][] bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    return this.over(pmat);
    	}

    	// Divide this matrix by a PhasorMatrix [equivalence of /=]
    	public void overEquals(PhasorMatrix bmat){
        	if((this.nrow!=bmat.nrow)||(this.ncol!=bmat.ncol)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	PhasorMatrix cmat = new PhasorMatrix(bmat);
    	    this.timesEquals(cmat.inverse());
    	}

    	// Divide this matrix by a 2D array of Phasors [equivalence of /=]
    	public void overEquals(Phasor[][] bmat){
    	    PhasorMatrix pmat = new PhasorMatrix(bmat);
    	    this.overEquals(pmat);
    	}

    	// Divide this matrix by a ComplexMatrix    [equivalence of /=]
    	public void overEquals(ComplexMatrix bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    this.overEquals(pmat);
    	}

    	// Divide this matrix  by a 2D array of complex     [equivalence of /=]
    	public void overEquals(Complex[][] bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    this.overEquals(pmat);
    	}

    	// Divide this PhasorMatrix a Matrix   [equivalence of /=]
    	public void overEquals(Matrix bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    this.overEquals(pmat);
    	}

    	// Divide this matrix by a 2D array of doubles   [equivalence of /=]
    	public void overEquals(double[][] bmat){
    	    PhasorMatrix pmat = PhasorMatrix.toPhasorMatrix(bmat);
    	    this.overEquals(pmat);
    	}

    	// INVERSE
    	// Inverse of a square Phasor matrix
    	public PhasorMatrix inverse(){
        	int n = this.nrow;
        	if(n!=this.ncol)throw new IllegalArgumentException("Matrix is not square");
        	Phasor[] col = new Phasor[n];
        	Phasor[] xvec = new Phasor[n];
        	PhasorMatrix invmat = new PhasorMatrix(n, n);
        	Phasor[][] invarray = invmat.getArrayReference();
        	PhasorMatrix ludmat;

	    	ludmat = this.luDecomp();
        	for(int j=0; j<n; j++){
            		for(int i=0; i<n; i++)col[i]=Phasor.zero();
            		col[j]=Phasor.plusOne();
            		xvec=ludmat.luBackSub(col);
           		for(int i=0; i<n; i++)invarray[i][j]=Phasor.copy(xvec[i]);
        	}
        	return invmat;
    	}



    	// TRANSPOSE
    	// Transpose of a Phasor matrix
    	public PhasorMatrix transpose(){
        	PhasorMatrix tmat = new PhasorMatrix(this.ncol, this.nrow);
        	Phasor[][] tarray = tmat.getArrayReference();
        	for(int i=0; i<this.ncol; i++){
            		for(int j=0; j<this.nrow; j++){
                		tarray[i][j]=Phasor.copy(this.matrix[j][i]);
            		}
        	}
        	return tmat;
    	}


    	// COMPLEX CONJUGATE
    	// Complex Conjugate of a Phasor matrix
    	public PhasorMatrix conjugate(){
        	PhasorMatrix conj = PhasorMatrix.copy(this);
        	for(int i=0; i<this.nrow; i++){
            		for(int j=0; j<this.ncol; j++){
                		conj.matrix[i][j]=this.matrix[i][j].conjugate();
            		}
        	}
        	return conj;
    	}


    	// ADJOIN
    	// Adjoin of a Phasor matrix
    	public PhasorMatrix adjoin(){
        	PhasorMatrix adj = PhasorMatrix.copy(this);
        	adj=adj.transpose();
        	adj=adj.conjugate();
        	return adj;
    	}


    	// OPPOSITE
    	// Opposite of a Phasor matrix
    	public PhasorMatrix opposite(){
        	PhasorMatrix opp = PhasorMatrix.copy(this);
        	for(int i=0; i<this.nrow; i++){
            		for(int j=0; j<this.ncol; j++){
                		opp.matrix[i][j]=this.matrix[i][j].times(Phasor.minusOne());
            		}
        	}
        	return opp;
    	}


    	// TRACE
    	// Trace of a Phasor matrix
    	public Phasor trace(){
        	Phasor trac = new Phasor(0.0, 0.0);
        	for(int i=0; i<Math.min(this.ncol,this.ncol); i++){
                	trac.plusEquals(this.matrix[i][i]);
        	}
        	return trac;
    	}

    	// DETERMINANT
    	//  Returns the determinant of a Phasor square matrix
    	public Phasor determinant(){
        	int n = this.nrow;
        	if(n!=this.ncol)throw new IllegalArgumentException("Matrix is not square");
       	 	Phasor det = new Phasor();
        	PhasorMatrix ludmat;

	    	ludmat = this.luDecomp();
	    	det.reset(ludmat.dswap,0.0);
        	for(int j=0; j<n; j++){
            		det.timesEquals(ludmat.matrix[j][j]);
         	}
        	return det;
    	}

    	// Returns the log(determinant) of a Phasor square matrix
    	// Useful if determinant() underflows or overflows.
    	public Phasor logDeterminant(){
        	int n = this.nrow;
        	if(n!=this.ncol)throw new IllegalArgumentException("Matrix is not square");
        	Phasor det = new Phasor();
        	PhasorMatrix ludmat;

	    	ludmat = this.luDecomp();
	    	det.reset(ludmat.dswap,0.0);
	    	det=Phasor.log(det);
        	for(int j=0; j<n; j++){
            		det.plusEquals(Phasor.log(ludmat.matrix[j][j]));
        	}
        	return det;
    	}


    	// FROBENIUS (EUCLIDEAN) NORM of a Phasor matrix
    	public double frobeniusNorm(){
        	double norm=0.0D;
        	for(int i=0; i<this.nrow; i++){
            		for(int j=0; j<this.ncol; j++){
                		norm=Fmath.hypot(norm, matrix[i][j].abs());
            		}
        	}
        	return norm;
    	}

    	// ONE NORM of a Phasor matrix
    	public double oneNorm(){
        	double norm=0.0D;
        	double sum = 0.0D;
        	for(int i=0; i<this.nrow; i++){
            		sum=0.0D;
            		for(int j=0; j<this.ncol; j++){
                		sum+=this.matrix[i][j].abs();
            		}
            		norm=Math.max(norm,sum);
        	}
        	return norm;
    	}

    	// INFINITY NORM of a Phasor matrix
    	public double infinityNorm(){
        	double norm=0.0D;
        	double sum=0.0D;
        	for(int i=0; i<this.nrow; i++){
            		sum=0.0D;
            		for(int j=0; j<this.ncol; j++){
                		sum+=this.matrix[i][j].abs();
            		}
            		norm=Math.max(norm,sum);
        	}
        	return norm;
    	}


    	// LU DECOMPOSITION OF COMPLEX MATRIX A
    	// For details of LU decomposition
    	// See Numerical Recipes, The Art of Scientific Computing
    	// by W H Press, S A Teukolsky, W T Vetterling & B P Flannery
	    // Cambridge University Press,   http://www.nr.com/
	    // PhasorMatrix ludmat is the returned LU decompostion
	    // int[] index is the vector of row permutations
	    // dswap returns +1.0 for even number of row interchanges
	    //       returns -1.0 for odd number of row interchanges
	    public PhasorMatrix luDecomp(){
        	if(this.nrow!=this.ncol)throw new IllegalArgumentException("A matrix is not square");
        	int n=this.nrow;
	    	int imax=0;
	    	double dum=0.0D, temp=0.0D, big=0.0D;
	    	double[] vv = new double[n];
	    	Phasor sum = new Phasor();
	    	Phasor dumm = new Phasor();

	    	PhasorMatrix ludmat=PhasorMatrix.copy(this);
		    Phasor[][] ludarray = ludmat.getArrayReference();

    		ludmat.dswap=1.0;
	    	for (int i=0;i<n;i++) {
		    	big=0.0;
		    	for (int j=0;j<n;j++){
			    	if ((temp=ludarray[i][j].abs()) > big) big=temp;
			    }
        		if (big == 0.0) throw new ArithmeticException("Singular matrix");
    			vv[i]=1.0/big;
	    	}
	    	for (int j=0;j<n;j++) {
		    	for (int i=0;i<j;i++) {
			    	sum=Phasor.copy(ludarray[i][j]);
			    	for (int k=0;k<i;k++) sum.minusEquals(ludarray[i][k].times(ludarray[k][j]));
			    	ludarray[i][j]=Phasor.copy(sum);
		    	}
		    	big=0.0;
		    	for (int i=j;i<n;i++) {
    				sum=Phasor.copy(ludarray[i][j]);
	    			for (int k=0;k<j;k++){
				    	sum.minusEquals(ludarray[i][k].times(ludarray[k][j]));
				    }
			    	ludarray[i][j]=Phasor.copy(sum);
     				if ((dum=vv[i]*sum.abs()) >= big) {
				        big=dum;
				        imax=i;
			    	}
		        }
		    	if (j != imax) {
			        for (int k=0;k<n;k++) {
				        dumm=Phasor.copy(ludarray[imax][k]);
				    	ludarray[imax][k]=Phasor.copy(ludarray[j][k]);
    					ludarray[j][k]=Phasor.copy(dumm);
			        }
			    	ludmat.dswap = -ludmat.dswap;
    				vv[imax]=vv[j];
	    	    }
		    	ludmat.index[j]=imax;

		    	if(ludarray[j][j].isZero()){
		            ludarray[j][j].reset(TINY, 0.0D);
		    	}
		    	if(j != n-1) {
			        dumm=ludarray[j][j].inverse();
    				for (int i=j+1;i<n;i++){
    			        ludarray[i][j].timesEquals(dumm);
	    	        }
	    	    }
	        }
	    	return ludmat;
	    }

    	// Solves the set of n linear Phasor equations A.X=B using not A but its LU decomposition
    	// Phasor bvec is the vector B (input)
    	// Phasor xvec is the vector X (output)
    	// index is the permutation vector produced by luDecomp()
    	public Phasor[] luBackSub(Phasor[] bvec){
	    	int ii=0,ip=0;
	    	int n=bvec.length;
	    	if(n!=this.ncol)throw new IllegalArgumentException("vector length is not equal to matrix dimension");
	    	if(this.ncol!=this.nrow)throw new IllegalArgumentException("matrix is not square");
	    	Phasor sum=new Phasor();
	    	Phasor[] xvec=new Phasor[n];
	    	for(int i=0; i<n; i++){
	        	xvec[i]=Phasor.copy(bvec[i]);
	    	}
	    	for (int i=0;i<n;i++) {
		    	ip=this.index[i];
		    	sum=Phasor.copy(xvec[ip]);
		    	xvec[ip]=Phasor.copy(xvec[i]);
		    	if (ii==0){
			    	for (int j=ii;j<=i-1;j++){
			        	sum.minusEquals(this.matrix[i][j].times(xvec[j]));
			    	}
			    }
		    	else{
		        	if(sum.isZero()) ii=i;
		    	}
		    	xvec[i]=Phasor.copy(sum);
	    	}
	    	for(int i=n-1;i>=0;i--) {
		    	sum=Phasor.copy(xvec[i]);
		    	for (int j=i+1;j<n;j++){
		        	sum.minusEquals(this.matrix[i][j].times(xvec[j]));
		    	}
		    	xvec[i]= sum.over(this.matrix[i][i]);
	    	}
	    	return xvec;
    	}

    	// Solves the set of n linear Phasor equations A.X=B
    	// Phasor bvec is the vector B (input)
    	// Phasor xvec is the vector X (output)
    	public Phasor[] solveLinearSet(Phasor[] bvec){
        	PhasorMatrix ludmat;

	    	ludmat=this.luDecomp();
        	return ludmat.luBackSub(bvec);
    	}
}

