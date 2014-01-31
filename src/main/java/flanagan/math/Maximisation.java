/*
*   Class Maximisation
*
*   Contains methods for finding the values of the
*   function parameters that maximise that function
*   using the Nelder and Mead Simplex method.
*
*   The function needed by the maximisation method
*   is supplied through the interface, MaximisationFunction
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:   29 December 2005 (adapted from Minimisation)
*   UPDATES:    28 December 2007, 10/12 May 2008, 4 July 2008, 28 July 2008, 12 January 2010, 23 September 2010
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Maximisation.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2005 - 2008
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

package flanagan.math;

import java.util.*;
import flanagan.math.Fmath;
import flanagan.io.FileOutput;


// Maximisation class
public class Maximisation{

    protected boolean iseOption = true;             // = true if minimis... spelling used
                                                    // = false if minimiz... spelling used
    protected int nParam=0;       		            // number of unknown parameters to be estimated
    protected double[]paramValue = null;            // function parameter values (returned at function maximum)
	protected String[] paraName = null;   	        // names of parameters, eg, c[0], c[1], c[2] . . .
	protected double functValue = 0.0D;             // current value of the function to be maximised
    protected double lastFunctValNoCnstrnt=0.0D;    // Last function value with no constraint penalty
	protected double maximum = 0.0D;                // value of the function to be maximised at the maximum
	protected int prec = 4;               	        // number of places to which double variables are truncated on output to text files
	protected int field = 13;             	        // field width on output to text files
    protected boolean convStatus = false;           // Status of maximisation on exiting maximisation method
                                		            // = true  -  convergence criterion was met
                                		            // = false -  convergence criterion not met - current estimates returned
	protected boolean suppressNoConvergenceMessage = false;     // if true - suppress the print out of a message saying that convergence was not achieved.
    protected int scaleOpt=0;     		            // if = 0; no scaling of initial estimates
                                		            //  if = 1; initial simplex estimates scaled to unity
                                		            //  if = 2; initial estimates scaled by user provided values in scale[]
                                		            //  (default = 0)
    protected double[] scale = null;  	            // values to scale initial estimate (see scaleOpt above)
    protected boolean penalty = false; 	            // true if single parameter penalty function is included
    protected boolean sumPenalty = false; 	        // true if multiple parameter penalty function is included
    protected int nConstraints = 0; 		        // number of single parameter constraints
    protected int nSumConstraints = 0; 		        // number of multiple parameter constraints
    protected int maxConstraintIndex = -1;          // maximum index of constrained parameter/s
    protected ArrayList<Object> penalties = new ArrayList<Object>();    // method index,
                                                                        // number of single parameter constraints,
                                                                        // then repeated for each constraint:
                                                                        //  penalty parameter index,
                                                                        //  below or above constraint flag,
                                                                        //  constraint boundary value
    protected ArrayList<Object> sumPenalties = new ArrayList<Object>(); // constraint method index,
                                                                        // number of multiple parameter constraints,
                                                                        // then repeated for each constraint:
                                                                        //  number of parameters in summation
                                                                        //  penalty parameter indices,
                                                                        //  summation signs
                                                                        //  below or above constraint flag,
                                                                        //  constraint boundary value
    protected int[] penaltyCheck = null;  	        // = -1 values below the single constraint boundary not allowed
                                        	        // = +1 values above the single constraint boundary not allowed
    protected int[] sumPenaltyCheck = null;  	    // = -1 values below the multiple constraint boundary not allowed
                                        	        // = +1 values above the multiple constraint boundary not allowed
    protected double penaltyWeight = 1.0e30;        // weight for the penalty functions
    protected int[] penaltyParam = null;   	        // indices of paramaters subject to single parameter constraint
    protected int[][] sumPenaltyParam = null;       // indices of paramaters subject to multiple parameter constraint
    protected double[][] sumPlusOrMinus = null;     // sign before each parameter in multiple parameter summation
    protected int[] sumPenaltyNumber = null;        // number of paramaters in each multiple parameter constraint
    protected double constraintTolerance = 1e-4;    // tolerance in constraining parameter/s to a fixed value

    protected double[] constraints = null; 	        // single parameter constraint values
    protected double[] sumConstraints = null;       // multiple parameter constraint values
    protected int constraintMethod = 0;             // constraint method number
                                                    // =0: cliff to the power two (only method at present)
    protected int nMax = 3000;    		            //  Nelder and Mead simplex maximum number of iterations
    protected int nIter = 0;      		            //  Nelder and Mead simplex number of iterations performed
    protected int konvge = 3;     		            //  Nelder and Mead simplex number of restarts allowed
    protected int kRestart = 0;       	            //  Nelder and Mead simplex number of restarts taken
    protected double fTol = 1e-13;     	            //  Nelder and Mead simplex convergence tolerance
    protected double rCoeff = 1.0D;   	            //  Nelder and Mead simplex reflection coefficient
    protected double eCoeff = 2.0D;   	            //  Nelder and Mead simplex extension coefficient
    protected double cCoeff = 0.5D;   	            //  Nelder and Mead simplex contraction coefficient
    protected double[] startH = null; 	            //  Nelder and Mead simplex initial estimates
    protected double[] step = null;   	            //  Nelder and Mead simplex step values
    protected double dStep = 0.5D;    	            // Nelder and Mead simplex default step value
    protected int maxTest = 0;    		            // Nelder and Mead maximum test
                                		            //  = 0; tests simplex sd < fTol
                                		            // allows options for further tests to be added later
    protected double simplexSd = 0.0D;    	        // simplex standard deviation


    //Constructors
    public Maximisation(){
        this.iseOption = true;
	}

	// Suppress the print out of a message saying that convergence was not achieved.
	public void suppressNoConvergenceMessage(){
	    this.suppressNoConvergenceMessage = true;
	}

    // Nelder and Mead Simplex maximization
    public void nelderMead(MaximizationFunction gg, double[] start, double[] step, double fTol, int nMax){
        this.nelderMead((Object)gg, start, step, fTol, nMax);
    }

    // Nelder and Mead Simplex maximization
    public void nelderMead(MaximisationFunction gg, double[] start, double[] step, double fTol, int nMax){
        this.nelderMead((Object)gg, start, step, fTol, nMax);
    }

    // Nelder and Mead Simplex maximization
    public void nelderMead(Object g, double[] start, double[] step, double fTol, int nMax){
        boolean testContract=false; // test whether a simplex contraction has been performed
        int np = start.length;  // number of unknown parameters;
        if(this.maxConstraintIndex>=np)throw new IllegalArgumentException("You have entered more constrained parameters ("+this.maxConstraintIndex+") than minimisation parameters (" + np + ")");
        this.nParam = np;
        this.convStatus = true;
        int nnp = np+1; // Number of simplex apices
        this.lastFunctValNoCnstrnt=0.0D;

        if(this.scaleOpt<2)this.scale = new double[np];
        if(scaleOpt==2 && scale.length!=start.length)throw new IllegalArgumentException("scale array and initial estimate array are of different lengths");
        if(step.length!=start.length)throw new IllegalArgumentException("step array length " + step.length + " and initial estimate array length " + start.length + " are of different");

        // check for zero step sizes
        for(int i=0; i<np; i++){
            if(step[i]==0.0D){
                    if(start[i]!=0.0){
                    step[i] = start[i]*0.1;
                }
                else{
                    step[i] = 1.0;
                    System.out.println("As no step size has been entered for an itial estimate of zero an initial step size of unity has been used");
                    System.out.println("You are advised to repeat the maximization using one of the methods allowing the setting of an appropriate non-zero initial step size");
                }
            }
        }

	    // set up arrays
	    this.paramValue = new double[np];
	    this.startH = new double[np];
	    this.step = new double[np];
	    double[]pmax = new double[np];   //Nelder and Mead Pmax

	    double[][] pp = new double[nnp][nnp];   //Nelder and Mead P
	    double[] yy = new double[nnp];          //Nelder and Mead y
	    double[] pbar = new double[nnp];        //Nelder and Mead P with bar superscript
	    double[] pstar = new double[nnp];       //Nelder and Mead P*
	    double[] p2star = new double[nnp];      //Nelder and Mead P**

        // Set any single parameter constraint parameters
        if(this.penalty){
            Integer itemp = (Integer)this.penalties.get(1);
            this.nConstraints = itemp.intValue();
            this.penaltyParam = new int[this.nConstraints];
            this.penaltyCheck = new int[this.nConstraints];
            this.constraints = new double[this.nConstraints];
            Double dtemp = null;
            int j=2;
            for(int i=0;i<this.nConstraints;i++){
                itemp = (Integer)this.penalties.get(j);
                this.penaltyParam[i] = itemp.intValue();
                j++;
                itemp = (Integer)this.penalties.get(j);
                this.penaltyCheck[i] = itemp.intValue();
                j++;
                dtemp = (Double)this.penalties.get(j);
                this.constraints[i] = dtemp.doubleValue();
                j++;
            }
        }

        // Set any multiple parameter constraint parameters
        if(this.sumPenalty){
            Integer itemp = (Integer)this.sumPenalties.get(1);
            this.nSumConstraints = itemp.intValue();
            this.sumPenaltyParam = new int[this.nSumConstraints][];
            this.sumPlusOrMinus = new double[this.nSumConstraints][];
            this.sumPenaltyCheck = new int[this.nSumConstraints];
            this.sumPenaltyNumber = new int[this.nSumConstraints];
            this.sumConstraints = new double[this.nSumConstraints];
            int[] itempArray = null;
            double[] dtempArray = null;
            Double dtemp = null;
            int j=2;
            for(int i=0;i<this.nSumConstraints;i++){
                itemp = (Integer)this.sumPenalties.get(j);
                this.sumPenaltyNumber[i] = itemp.intValue();
                j++;
                itempArray = (int[])this.sumPenalties.get(j);
                this.sumPenaltyParam[i] = itempArray;
                j++;
                dtempArray = (double[])this.sumPenalties.get(j);
                this.sumPlusOrMinus[i] = dtempArray;
                j++;
                itemp = (Integer)this.sumPenalties.get(j);
                this.sumPenaltyCheck[i] = itemp.intValue();
                j++;
                dtemp = (Double)this.sumPenalties.get(j);
                this.sumConstraints[i] = dtemp.doubleValue();
                j++;
            }
        }

        // Store unscaled start values
        for(int i=0; i<np; i++)this.startH[i]=start[i];

        // scale initial estimates and step sizes
        if(this.scaleOpt>0){
            boolean testzero=false;
            for(int i=0; i<np; i++)if(start[i]==0.0D)testzero=true;
            if(testzero){
                System.out.println("Neler and Mead Simplex: a start value of zero precludes scaling");
                System.out.println("Regression performed without scaling");
                this.scaleOpt=0;
            }
        }
        switch(this.scaleOpt){
            case 0: for(int i=0; i<np; i++)scale[i]=1.0D;
                    break;
            case 1: for(int i=0; i<np; i++){
                        scale[i]=1.0/start[i];
                        step[i]=step[i]/start[i];
                        start[i]=1.0D;
                    }
                    break;
            case 2: for(int i=0; i<np; i++){
                        step[i]*=scale[i];
                        start[i]*= scale[i];
                    }
                    break;
        }

        // set class member values
        this.fTol=fTol;
        this.nMax=nMax;
        this.nIter=0;
        for(int i=0; i<np; i++){
            this.step[i]=step[i];
            this.scale[i]=scale[i];
        }

	    // initial simplex
	    double sho=0.0D;
	    for (int i=0; i<np; ++i){
 	        sho=start[i];
	 	    pstar[i]=sho;
		    p2star[i]=sho;
		    pmax[i]=sho;
	    }

	    int jcount=this.konvge;  // count of number of restarts still available

	    for (int i=0; i<np; ++i){
	        pp[i][nnp-1]=start[i];
	    }
	    yy[nnp-1]=this.functionValue(g, start);
	    for (int j=0; j<np; ++j){
		    start[j]=start[j]+step[j];

		    for (int i=0; i<np; ++i)pp[i][j]=start[i];
		    yy[j]=this.functionValue(g, start);
		    start[j]=start[j]-step[j];
	    }

	    // loop over allowed iterations
        double  ynewlo=0.0D;    // current value lowest y
	    double 	ystar = 0.0D;   // Nelder and Mead y*
	    double  y2star = 0.0D;  // Nelder and Mead y**
	    double  ylo = 0.0D;     // Nelder and Mead y(low)
	    double  fMax;   // function value at maximum
	    // variables used in calculating the variance of the simplex at a putative maximum
	    double 	curMin = 00D, sumnm = 0.0D, summnm = 0.0D, zn = 0.0D;
	    int ilo=0;  // index of low apex
	    int ihi=0;  // index of high apex
	    int ln=0;   // counter for a check on low and high apices
	    boolean test = true;    // test becomes false on reaching maximum

	    while(test){
	        // Determine h
	        ylo=yy[0];
	        ynewlo=ylo;
    	    ilo=0;
	        ihi=0;
	        for (int i=1; i<nnp; ++i){
		        if (yy[i]<ylo){
			        ylo=yy[i];
			        ilo=i;
		        }
		        if (yy[i]>ynewlo){
			        ynewlo=yy[i];
			        ihi=i;
		        }
	        }
	        // Calculate pbar
	        for (int i=0; i<np; ++i){
		        zn=0.0D;
		        for (int j=0; j<nnp; ++j){
			        zn += pp[i][j];
		        }
		        zn -= pp[i][ihi];
		        pbar[i] = zn/np;
	        }

	        // Calculate p=(1+alpha).pbar-alpha.ph {Reflection}
	        for (int i=0; i<np; ++i)pstar[i]=(1.0 + this.rCoeff)*pbar[i]-this.rCoeff*pp[i][ihi];

	        // Calculate y*
	        ystar=this.functionValue(g, pstar);

	        ++this.nIter;

	        // check for y*<yi
	        if(ystar < ylo){
                // Form p**=(1+gamma).p*-gamma.pbar {Extension}
	            for (int i=0; i<np; ++i)p2star[i]=pstar[i]*(1.0D + this.eCoeff)-this.eCoeff*pbar[i];
	            // Calculate y**
	            y2star=this.functionValue(g, p2star);
	            ++this.nIter;
                if(y2star < ylo){
                    // Replace ph by p**
		            for (int i=0; i<np; ++i)pp[i][ihi] = p2star[i];
	                yy[ihi] = y2star;
	            }
	            else{
	                //Replace ph by p*
	                for (int i=0; i<np; ++i)pp[i][ihi]=pstar[i];
	                yy[ihi]=ystar;
	            }
	        }
	        else{
	            // Check y*>yi, i!=h
		        ln=0;
	            for (int i=0; i<nnp; ++i)if (i!=ihi && ystar > yy[i]) ++ln;
	            if (ln==np ){
	                // y*>= all yi; Check if y*>yh
                    if(ystar<=yy[ihi]){
                        // Replace ph by p*
	                    for (int i=0; i<np; ++i)pp[i][ihi]=pstar[i];
	                    yy[ihi]=ystar;
	                }
	                // Calculate p** =beta.ph+(1-beta)pbar  {Contraction}
	                for (int i=0; i<np; ++i)p2star[i]=this.cCoeff*pp[i][ihi] + (1.0 - this.cCoeff)*pbar[i];
	                // Calculate y**
	                y2star=this.functionValue(g, p2star);
	                ++this.nIter;
	                // Check if y**>yh
	                if(y2star>yy[ihi]){
	                    //Replace all pi by (pi+pl)/2

	                    for (int j=0; j<nnp; ++j){
		                    for (int i=0; i<np; ++i){
			                    pp[i][j]=0.5*(pp[i][j] + pp[i][ilo]);
			                    pmax[i]=pp[i][j];
		                    }
		                    yy[j]=this.functionValue(g, pmax);
	                    }
	                    this.nIter += nnp;
	                }
	                else{
	                    // Replace ph by p**
		                for (int i=0; i<np; ++i)pp[i][ihi] = p2star[i];
	                    yy[ihi] = y2star;
	                }
	            }
	            else{
	                // replace ph by p*
	                for (int i=0; i<np; ++i)pp[i][ihi]=pstar[i];
	                yy[ihi]=ystar;
	            }
	        }

            // test for convergence
            // calculte sd of simplex and maximum point
            sumnm=0.0;
	        ynewlo=yy[0];
	        ilo=0;
	        for (int i=0; i<nnp; ++i){
	            sumnm += yy[i];
	            if(ynewlo>yy[i]){
	                ynewlo=yy[i];
	                ilo=i;
	            }
	        }
	        sumnm /= (double)(nnp);
	        summnm=0.0;
	        for (int i=0; i<nnp; ++i){
		        zn=yy[i]-sumnm;
	            summnm += zn*zn;
	        }
	        curMin=Math.sqrt(summnm/np);

	        // test simplex sd
	        switch(this.maxTest){
	            case 0:
                    if(curMin<fTol)test=false;
                    break;
			    }
            this.maximum = -ynewlo;
	        if(!test){
	            // store parameter values
	            for (int i=0; i<np; ++i)pmax[i]=pp[i][ilo];
	            yy[nnp-1]=ynewlo;
	            // store simplex sd
	            this.simplexSd = curMin;
	            // test for restart
	            --jcount;
	            if(jcount>0){
	                test=true;
	   	            for (int j=0; j<np; ++j){
		                pmax[j]=pmax[j]+step[j];
		                for (int i=0; i<np; ++i)pp[i][j]=pmax[i];
		                yy[j]=this.functionValue(g, pmax);
		                pmax[j]=pmax[j]-step[j];
	                 }
	            }
	        }

	        if(test && this.nIter>this.nMax){
	            if(!this.suppressNoConvergenceMessage){
	                System.out.println("Maximum iteration number reached, in Maximisation.simplex(...)");
	                System.out.println("without the convergence criterion being satisfied");
	                System.out.println("Current parameter estimates and function value returned");
	            }
	            this.convStatus = false;
	            // store current estimates
	            for (int i=0; i<np; ++i)pmax[i]=pp[i][ilo];
	            yy[nnp-1]=ynewlo;
                test=false;
            }
        }

		for (int i=0; i<np; ++i){
            pmax[i] = pp[i][ilo];
            paramValue[i] = pmax[i]/this.scale[i];
        }
    	this.maximum = -ynewlo;
    	this.kRestart = this.konvge - jcount;

	}

	// Nelder and Mead simplex
	// Default  maximum iterations
    public void nelderMead(MaximizationFunction g, double[] start, double[] step, double fTol){
        int nMaxx = this.nMax;
        this.nelderMead(g, start, step, fTol, nMaxx);
    }

    public void nelderMead(MaximisationFunction g, double[] start, double[] step, double fTol){
        int nMaxx = this.nMax;
        this.nelderMead(g, start, step, fTol, nMaxx);
    }

	// Nelder and Mead simplex
	// Default  tolerance
    public void nelderMead(MaximizationFunction g, double[] start, double[] step, int nMax){
        double fToll = this.fTol;
        this.nelderMead(g, start, step, fToll, nMax);
    }

    public void nelderMead(MaximisationFunction g, double[] start, double[] step, int nMax){
        double fToll = this.fTol;
        this.nelderMead(g, start, step, fToll, nMax);
    }

	// Nelder and Mead simplex
	// Default  tolerance
	// Default  maximum iterations
    public void nelderMead(MaximizationFunction g, double[] start, double[] step){
        double fToll = this.fTol;
        int nMaxx = this.nMax;
        this.nelderMead(g, start, step, fToll, nMaxx);
    }

    public void nelderMead(MaximisationFunction g, double[] start, double[] step){
        double fToll = this.fTol;
        int nMaxx = this.nMax;
        this.nelderMead(g, start, step, fToll, nMaxx);
    }

	// Nelder and Mead simplex
	// Default step option - all step[i] = dStep
    public void nelderMead(MaximizationFunction g, double[] start, double fTol, int nMax){
        int n=start.length;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.nelderMead(g, start, stepp, fTol, nMax);
    }

    public void nelderMead(MaximisationFunction g, double[] start, double fTol, int nMax){
        int n=start.length;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.nelderMead(g, start, stepp, fTol, nMax);
    }

	// Nelder and Mead simplex
	// Default  maximum iterations
	// Default step option - all step[i] = dStep
    public void nelderMead(MaximizationFunction g, double[] start, double fTol){
        int n=start.length;
        int nMaxx = this.nMax;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.nelderMead(g, start, stepp, fTol, nMaxx);
    }

    public void nelderMead(MaximisationFunction g, double[] start, double fTol){
        int n=start.length;
        int nMaxx = this.nMax;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.nelderMead(g, start, stepp, fTol, nMaxx);
    }

	// Nelder and Mead simplex
    // Default  tolerance
	// Default step option - all step[i] = dStep
    public void nelderMead(MaximizationFunction g, double[] start, int nMax){
        int n=start.length;
        double fToll = this.fTol;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.nelderMead(g, start, stepp, fToll, nMax);
    }

    public void nelderMead(MaximisationFunction g, double[] start, int nMax){
        int n=start.length;
        double fToll = this.fTol;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.nelderMead(g, start, stepp, fToll, nMax);
    }

	// Nelder and Mead simplex
    // Default  tolerance
    // Default  maximum iterations
	// Default step option - all step[i] = dStep
    public void nelderMead(MaximizationFunction g, double[] start){
        int n=start.length;
        int nMaxx = this.nMax;
        double fToll = this.fTol;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.nelderMead(g, start, stepp, fToll, nMaxx);
    }

    public void nelderMead(MaximisationFunction g, double[] start){
        int n=start.length;
        int nMaxx = this.nMax;
        double fToll = this.fTol;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.nelderMead(g, start, stepp, fToll, nMaxx);
    }

    // Calculate the function value for maximisation
	protected double functionValue(Object g, double[] x){
	    if(this.iseOption){
	        return functionValue((MaximisationFunction) g, x);
	    }
	    else{
	        return functionValue((MaximizationFunction) g, x);
	    }
	}

    // Calculate the function value for maximisation
	protected double functionValue(MaximizationFunction g, double[] x){

	    double[] param = new double[this.nParam];
	    // rescale
	    for(int i=0; i<this.nParam; i++)param[i]=x[i]/scale[i];

	    boolean test = this.functionValueCommon(x, param);

        if(test){
            this.functValue = -g.function(param);
            this.lastFunctValNoCnstrnt = this.functValue;
        }
	    return this.functValue;
	}

	 // Calculate the function value for maximisation
	protected double functionValue(MaximisationFunction g, double[] x){

	    double[] param = new double[this.nParam];
	    // rescale
	    for(int i=0; i<this.nParam; i++)param[i]=x[i]/scale[i];

	    boolean test = this.functionValueCommon(x, param);

        if(test){
            this.functValue = -g.function(param);
            this.lastFunctValNoCnstrnt = this.functValue;
        }
	    return this.functValue;
	}


    // Common method for the functionValue(..) methods
	protected boolean functionValueCommon(double[] x, double[] param){

        // single parameter penalty functions
        double tempFunctVal = this.lastFunctValNoCnstrnt;
        boolean test=true;
        if(this.penalty){
            int k=0;
            for(int i=0; i<this.nConstraints; i++){
                k = this.penaltyParam[i];
                switch(penaltyCheck[i]){
                    case -1: if(param[k]<constraints[i]){
                                this.functValue = tempFunctVal + this.penaltyWeight*Fmath.square(constraints[i]-param[k]);
                                test=false;
                             }
                             break;
                    case 0: if(param[k]<constraints[i]*(1.0-this.constraintTolerance)){
                                this.functValue = tempFunctVal + this.penaltyWeight*Fmath.square(constraints[i]*(1.0-this.constraintTolerance)-param[k]);
                                test=false;
                             }
                             if(param[k]>constraints[i]*(1.0+this.constraintTolerance)){
                                this.functValue = tempFunctVal + this.penaltyWeight*Fmath.square(param[k]-constraints[i]*(1.0+this.constraintTolerance));
                                test=false;
                             }
                             break;
                    case 1:  if(param[k]>constraints[i]){
                                this.functValue = tempFunctVal + this.penaltyWeight*Fmath.square(param[k]-constraints[i]);
	                            test=false;
	                         }
	                         break;
                }
            }
        }

        // multiple parameter penalty functions
        if(this.sumPenalty){
            int kk = 0;
            double pSign = 0;
            for(int i=0; i<this.nSumConstraints; i++){
                double sumPenaltySum = 0.0D;
                for(int j=0; j<this.sumPenaltyNumber[i]; j++){
                    kk = this.sumPenaltyParam[i][j];
                    pSign = this.sumPlusOrMinus[i][j];
                    sumPenaltySum += param[kk]*pSign;
                }
                switch(this.sumPenaltyCheck[i]){
                    case -1: if(sumPenaltySum<sumConstraints[i]){
                                this.functValue = tempFunctVal + this.penaltyWeight*Fmath.square(sumConstraints[i]-sumPenaltySum);
                                test=false;
                             }
                             break;
                    case 0: if(sumPenaltySum<sumConstraints[i]*(1.0-this.constraintTolerance)){
                                this.functValue = tempFunctVal + this.penaltyWeight*Fmath.square(sumConstraints[i]*(1.0-this.constraintTolerance)-sumPenaltySum);
                                test=false;
                             }
                             if(sumPenaltySum>sumConstraints[i]*(1.0+this.constraintTolerance)){
                                this.functValue = tempFunctVal + this.penaltyWeight*Fmath.square(sumPenaltySum-sumConstraints[i]*(1.0+this.constraintTolerance));
                                test=false;
                             }
                             break;
                    case 1:  if(sumPenaltySum>sumConstraints[i]){
                                this.functValue = tempFunctVal + this.penaltyWeight*Fmath.square(sumPenaltySum-sumConstraints[i]);
	                            test=false;
                             }
                             break;
                }

            }
        }
        return test;
    }


	// add a single parameter constraint boundary for the maximisation
	public void addConstraint(int paramIndex, int conDir, double constraint){
	    this.penalty=true;
        // First element reserved for method number if other methods than 'cliff' are added later
		if(this.penalties.isEmpty())this.penalties.add(new Integer(this.constraintMethod));

		// add constraint
	    if(penalties.size()==1){
		    this.penalties.add(new Integer(1));
		}
		else{
		    int nPC = ((Integer)this.penalties.get(1)).intValue();
            nPC++;
            this.penalties.set(1, new Integer(nPC));
		}
		this.penalties.add(new Integer(paramIndex));
 	    this.penalties.add(new Integer(conDir));
 	    this.penalties.add(new Double(constraint));
 	    if(paramIndex>this.maxConstraintIndex)this.maxConstraintIndex = paramIndex;
 	}

    // add a multiple parameter constraint boundary for the non-linear regression
	public void addConstraint(int[] paramIndices, int[] plusOrMinus, int conDir, double constraint){
	    ArrayMaths am = new ArrayMaths(plusOrMinus);
	    double[] dpom = am.getArray_as_double();
	    addConstraint(paramIndices, dpom, conDir, constraint);
	}

    // add a multiple parameter constraint boundary for the maximisation
	public void addConstraint(int[] paramIndices, double[] plusOrMinus, int conDir, double constraint){
	    int nCon = paramIndices.length;
	    int nPorM = plusOrMinus.length;
	    if(nCon!=nPorM)throw new IllegalArgumentException("num of parameters, " + nCon + ", does not equal number of parameter signs, " + nPorM);
	    this.sumPenalty=true;
        // First element reserved for method number if other methods than 'cliff' are added later
		if(this.sumPenalties.isEmpty())this.sumPenalties.add(new Integer(this.constraintMethod));

		// add constraint
		if(sumPenalties.size()==1){
		    this.sumPenalties.add(new Integer(1));
		}
		else{
		    int nPC = ((Integer)this.sumPenalties.get(1)).intValue();
            nPC++;
            this.sumPenalties.set(1, new Integer(nPC));
		}
		this.sumPenalties.add(new Integer(nCon));
		this.sumPenalties.add(paramIndices);
		this.sumPenalties.add(plusOrMinus);
 	    this.sumPenalties.add(new Integer(conDir));
 	    this.sumPenalties.add(new Double(constraint));
 	    ArrayMaths am = new ArrayMaths(paramIndices);
 	    int maxI = am.getMaximum_as_int();
 	    if(maxI>this.maxConstraintIndex)this.maxConstraintIndex = maxI;
 	}

 	// Set constraint method
 	public void setConstraintMethod(int conMeth){
 	    this.constraintMethod = conMeth;
 	    if(!this.penalties.isEmpty())this.penalties.set(0, new Integer(this.constraintMethod));
 	}

	// remove all constraint boundaries for the maximisation
	public void removeConstraints(){

	    // check if single parameter constraints already set
	    if(!this.penalties.isEmpty()){
		    int m=this.penalties.size();

		    // remove single parameter constraints
    		for(int i=m-1; i>=0; i--){
		        this.penalties.remove(i);
		    }
		}
		this.penalty = false;
		this.nConstraints = 0;

	    // check if mutiple parameter constraints already set
	    if(!this.sumPenalties.isEmpty()){
		    int m=this.sumPenalties.size();

		    // remove multiple parameter constraints
    		for(int i=m-1; i>=0; i--){
		        this.sumPenalties.remove(i);
		    }
		}
		this.sumPenalty = false;
		this.nSumConstraints = 0;
		this.maxConstraintIndex = -1;
	}

	// Reset the tolerance used in a fixed value constraint
	public void setConstraintTolerance(double tolerance){
	    this.constraintTolerance = tolerance;
	}

	// Print the results of the maximisation
	// File name provided
	// prec = truncation precision
	public void print(String filename, int prec){
	    this.prec = prec;
	    this.print(filename);
	}

	// Print the results of the maximisation
	// No file name provided
	// prec = truncation precision
	public void print(int prec){
	    this.prec = prec;
		String filename="MaximisationOutput.txt";
        this.print(filename);
	}

	// Print the results of the maximisation
	// File name provided
	// prec = truncation precision
	public void print(String filename){

	    if(filename.indexOf('.')==-1)filename = filename+".txt";
	    FileOutput fout = new FileOutput(filename, 'n');
	    fout.dateAndTimeln(filename);
	    fout.println(" ");
	    fout.println("Simplex maximisation, using the method of Nelder and Mead,");
        fout.println("of the function y = f(c[0], c[1], c[2] . . .)");
	    this.paraName = new String[this.nParam];
        for(int i=0;i<this.nParam;i++)this.paraName[i]="c["+i+"]";

        fout.println();
        if(!this.convStatus){
            fout.println("Convergence criterion was not satisfied");
            fout.println("The following results are the current estimates on exiting the maximisation method");
            fout.println();
        }

        fout.println("Value of parameters at the maximum");
        fout.println(" ");

        fout.printtab(" ", this.field);
        fout.printtab("Value at", this.field);
        fout.printtab("Initial", this.field);
        fout.println("Initial");

        fout.printtab(" ", this.field);
        fout.printtab("maximium", this.field);
        fout.printtab("estimate", this.field);
        fout.println("step");

        for(int i=0; i<this.nParam; i++){
            fout.printtab(this.paraName[i], this.field);
            fout.printtab(Fmath.truncate(paramValue[i],this.prec), this.field);
            fout.printtab(Fmath.truncate(this.startH[i],this.prec), this.field);
            fout.println(Fmath.truncate(this.step[i],this.prec));
        }
        fout.println();

    	fout.println(" ");

        fout.printtab("Number of paramaters");
		fout.println(this.nParam);
        fout.printtab("Number of iterations taken");
        fout.println(this.nIter);
        fout.printtab("Maximum number of iterations allowed");
        fout.println(this.nMax);
        fout.printtab("Number of restarts taken");
        fout.println(this.kRestart);
        fout.printtab("Maximum number of restarts allowed");
        fout.println(this.konvge);
        fout.printtab("Standard deviation of the simplex at the maximum");
        fout.println(Fmath.truncate(this.simplexSd, this.prec));
        fout.printtab("Convergence tolerance");
        fout.println(this.fTol);
        switch(maxTest){
            case 0: if(this.convStatus){
                        fout.println("simplex sd < the tolerance");
                    }
                    else{
                        fout.println("NOTE!!! simplex sd > the tolerance");
                    }
                    break;
        }
        fout.println();
        fout.println("End of file");
		fout.close();
	}

	// Print the results of the maximisation
	// No file name provided
	public void print(){
		    String filename="MaximisationOutput.txt";
		    this.print(filename);
	}

    // Get the maximisation status
    // true if convergence was achieved
    // false if convergence not achieved before maximum number of iterations
    //  current values then returned
    public boolean getConvStatus(){
        return this.convStatus;
    }

    // Reset scaling factors (scaleOpt 0 and 1, see below for scaleOpt 2)
    public void setScale(int n){
        if(n<0 || n>1)throw new IllegalArgumentException("The argument must be 0 (no scaling) 1(initial estimates all scaled to unity) or the array of scaling factors");
        this.scaleOpt=n;
    }

    // Reset scaling factors (scaleOpt 2, see above for scaleOpt 0 and 1)
    public void setScale(double[] sc){
        this.scale=sc;
        this.scaleOpt=2;
    }

    // Get scaling factors
    public double[] getScale(){
        return this.scale;
    }

	// Reset the maximisation convergence test option
	public void setMaxTest(int n){
	    if(n<0 || n>1)throw new IllegalArgumentException("maxTest must be 0 or 1");
	    this.maxTest=n;
	}

    // Get the maximisation convergence test option
	public int getMaxTest(){
	    return this.maxTest;
	}

	// Get the simplex sd at the maximum
	public double getSimplexSd(){
	    return this.simplexSd;
	}

	// Get the values of the parameters at the maximum
	public double[] getParamValues(){
	    return this.paramValue;
	}

	// Get the function value at maximum
	public double getMaximum(){
	    return this.maximum;
	}

	// Get the number of iterations in Nelder and Mead
	public int getNiter(){
	    return this.nIter;
	}


	// Set the maximum number of iterations allowed in Nelder and Mead
	public void setNmax(int nmax){
	    this.nMax = nmax;
	}

	// Get the maximum number of iterations allowed in Nelder and Mead
	public int getNmax(){
	    return this.nMax;
	}

	// Get the number of restarts in Nelder and Mead
	public int getNrestarts(){
	    return this.kRestart;
	}

    // Set the maximum number of restarts allowed in Nelder and Mead
	public void setNrestartsMax(int nrs){
	    this.konvge = nrs;
	}

	// Get the maximum number of restarts allowed in Nelder amd Mead
	public int getNrestartsMax(){
	    return this.konvge;
	}

	// Reset the Nelder and Mead reflection coefficient [alpha]
	public void setNMreflect(double refl){
	    this.rCoeff = refl;
	}

	// Get the Nelder and Mead reflection coefficient [alpha]
	public double getNMreflect(){
	    return this.rCoeff;
	}

    // Reset the Nelder and Mead extension coefficient [beta]
	public void setNMextend(double ext){
	    this.eCoeff = ext;
	}
	// Get the Nelder and Mead extension coefficient [beta]
	public double getNMextend(){
	    return this.eCoeff;
	}

	// Reset the Nelder and Mead contraction coefficient [gamma]
	public void setNMcontract(double con){
	    this.cCoeff = con;
	}

	// Get the Nelder and Mead contraction coefficient [gamma]
	public double getNMcontract(){
	    return cCoeff;
	}

	// Set the maximisation tolerance
	public void setTolerance(double tol){
	    this.fTol = tol;
	}


	// Get the maximisation tolerance
	public double getTolerance(){
	    return this.fTol;
	}

}
