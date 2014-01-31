/*
*   Class   ComplexErrorProp
*
*   Defines an object describing a complex number in which there are
*   errors associted with real and imaginary parts aqnd includes the
*   methods for propagating the error in standard arithmetic operations
*   for both uncorrelated errors only.
*
*   AUTHOR: Dr Michael Thomas Flanagan
*
*   DATE: 27 April 2004
*   UPDATE: 19 January 2005, 28 May 2007
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's library on-line web pages:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/ComplexErrorProp.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) April 2004, May 2007  Michael Thomas Flanagan
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ucl.ee.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/

package flanagan.complex;

import flanagan.math.*;
import flanagan.analysis.*;

public class ComplexErrorProp{

        private ErrorProp eReal = new ErrorProp();  // Real part of a complex number
        private ErrorProp eImag = new ErrorProp();  // Imaginary part of a complex number
        private double corrCoeff = 0.0D;            // correlation coefficient between real and imaginary parts
        private static int monteCarloLength = 10000;// length of Monte Carlo simulation arrays

/*********************************************************/

        // CONSTRUCTORS
        // default constructor - value and error of real and imag = zero
        // no correlation between real and imaginary parts
        public ComplexErrorProp()
        {
                this.eReal.reset(0.0D, 0.0D);
                this.eImag.reset(0.0D, 0.0D);
                this.corrCoeff = 0.0D;
        }

         // constructor - initialises both real and imag with ErrorProp - no correlation between real and imaginary parts
        public ComplexErrorProp(ErrorProp eReal, ErrorProp eImag)
        {
                this.eReal = eReal.copy();
                this.eImag = eImag.copy();
                this.corrCoeff = 0.0D;
        }
        // constructor - initialises both real and imag with ErrorProp with correlation between real and imaginary parts
        public ComplexErrorProp(ErrorProp eReal, ErrorProp eImag, double corrCoeff)
        {
                this.eReal = eReal.copy();
                this.eImag = eImag.copy();
                this.corrCoeff = corrCoeff;
        }

        // constructor - initialises both real and imag with doubles - no correlation between real and imaginary parts
        public ComplexErrorProp(double eRealValue, double eRealError, double eImagValue, double eImagError)
        {
                this.eReal.reset(eRealValue, eRealError);
                this.eImag.reset(eImagValue, eImagError);
                this.corrCoeff = 0.0D;
        }

        // constructor - initialises both real and imag with doubles with correlation between real and imaginary parts
        public ComplexErrorProp(double eRealValue, double eRealError, double eImagValue, double eImagError, double corrCoeff)
        {
                this.eReal.reset(eRealValue, eRealError);
                this.eImag.reset(eImagValue, eImagError);
                this.corrCoeff = corrCoeff;
        }

/*********************************************************/

        // PUBLIC METHODS

        // SET VALUES
        // Set the values of real and imag - no correlation between real and imaginary
        public void reset(ErrorProp eReal, ErrorProp eImag){
                this.eReal = eReal.copy();
                this.eImag = eImag.copy();
                this.corrCoeff = 0.0D;
        }

        // Set the values of real and imag with correlation between real and imaginary
        public void reset(ErrorProp eReal, ErrorProp eImag, double corrCoeff){
                this.eReal = eReal.copy();
                this.eImag = eImag.copy();
                this.corrCoeff = corrCoeff;
        }

        // Set the values of real and imag - no correlation between real and imaginary
        public void reset(double eRealValue, double eRealError, double eImagValue, double eImagError){
            this.eReal.setValue(eRealValue);
            this.eReal.setError(eRealError);
            this.eImag.setValue(eImagValue);
            this.eImag.setError(eImagError);
            this.corrCoeff = 0.0D;
        }


        // Set the values of real and imag with correlation between real and imaginary
        public void reset(double eRealValue, double eRealError, double eImagValue, double eImagError, double corrCoeff){
            this.eReal.setValue(eRealValue);
            this.eReal.setError(eRealError);
            this.eImag.setValue(eImagValue);
            this.eImag.setError(eImagError);
            this.corrCoeff = corrCoeff;
        }

        // Set the values of magnitude and phase - no correlation between real and imaginary parts
        public void polar(ErrorProp eMag, ErrorProp ePhase){
                polar(eMag, ePhase, 0.0D);
        }

        // Set the values of magnitude and phase with correlation between real and imaginary parts
        public void polar(ErrorProp eMag, ErrorProp ePhase, double corrCoeff)
        {
                // calculate values and errors
                ErrorProp a = new ErrorProp();
                a = eMag.times(ErrorProp.cos(ePhase), corrCoeff);
                this.eReal = a;
                a = eMag.times(ErrorProp.sin(ePhase), corrCoeff);
                this.eImag = a;

                // calculate the new correlation coefficient
                PsRandom rr = new PsRandom();
                double[][] ran  =  rr.correlatedGaussianArrays(eMag.getValue(), ePhase.getValue(), eMag.getError(), ePhase.getError(), corrCoeff,  monteCarloLength);

                double[] rV = new double[monteCarloLength];
                double[] iV = new double[monteCarloLength];
                for(int i=0; i<monteCarloLength; i++){
                   rV[i] = ran[0][i]*Math.cos(ran[1][i]);
                   iV[i] = ran[0][i]*Math.sin(ran[1][i]);
                }

                this.corrCoeff = calcRho(rV, iV);
        }

        /// calculates the correlation coefficient between x and y
        public static double calcRho(double[] x, double[] y){
                int n = x.length;
                if(n!=y.length)throw new IllegalArgumentException("length of x and y must be the same");

                double meanX = 0.0D;
                double meanY = 0.0D;
                for(int i=0; i<n; i++){
                   meanX += x[i];
                   meanY += y[i];
                }
                meanX /= n;
                meanY /= n;
                double varX = 0.0D;
                double varY = 0.0D;
                double covarXY = 0.0D;
                for(int i=0; i<n; i++){
                   varX+= Fmath.square(x[i]-meanX);
                   varY += Fmath.square(y[i]-meanY);
                   covarXY += (x[i]-meanX)*(y[i]-meanY);
                }
                varX = Math.sqrt(varX/(n-1));
                varY = Math.sqrt(varY/(n-1));
                covarXY = covarXY/(n-1);

                return covarXY/(varX*varY);
        }

        // Set the values of magnitude and phase - no correlation between real and imaginary parts
        public void polar(double eMagValue, double eMagError, double ePhaseValue, double ePhaseError){
                ErrorProp eMag = new ErrorProp(eMagValue, eMagError);
                ErrorProp ePhase = new ErrorProp(ePhaseValue, ePhaseError);
                polar(eMag, ePhase, 0.0D);
        }

        // Set the values of magnitude and phase with correlation between real and imaginary parts
        public void polar(double eMagValue, double eMagError, double ePhaseValue, double ePhaseError, double corrCoeff){
                ErrorProp eMag = new ErrorProp(eMagValue, eMagError);
                ErrorProp ePhase = new ErrorProp(ePhaseValue, ePhaseError);
                polar(eMag, ePhase, corrCoeff);
        }

        // Set the value of real
        public void setReal(ErrorProp eReal){
            this.eReal = eReal.copy();
        }

        // Set the value of real
        public void setReal(double eRealValue, double eRealError){
            this.eReal.setValue(eRealValue);
            this.eReal.setError(eRealError);
        }

        // Set the value of imag
        public void setImag(ErrorProp eImag){
            this.eImag = eImag.copy();
        }

        // Set the value of imag
        public void setImag(double eImagValue, double eImagError){
            this.eImag.setValue(eImagValue);
            this.eImag.setError(eImagError);
        }

        // Set the values of an error free double as ComplexErrorProp
        public void setDouble(double errorFree){
            this.eReal.reset(errorFree, 0.0D);
            this.eImag.reset(0.0D, 0.0D);
        }

        // Set the value of the correlation coefficient between the real and imaginary parts
        public void setCorrCoeff(double corrCoeff){
            this.corrCoeff = corrCoeff;
        }

        // set value of the Monte Carlo simulation array length
        public static void setMonteCarloLength(int length){
            ComplexErrorProp.monteCarloLength = length;
        }


        // GET VALUES
        // Get the real part
        public ErrorProp getReal(){
                return eReal.copy();
        }

        // Get the value of real
        public double getRealValue(){
                return eReal.getValue();
        }

        // Get the error of real
        public double getRealError(){
                return eReal.getError();
        }

        // Get the imag part
        public ErrorProp getImag(){
                return eImag.copy();
        }

        // Get the value of imag
        public double getImagValue(){
                return eImag.getValue();
        }

        // Get the error of eImag
        public double getImagError(){
                return eImag.getError();
        }

        // Get the correlation coefficient
        public double getCorrCoeff(){
                return this.corrCoeff;
        }

        // Get value of the Monte Carlo simulation array length
        public static int getMonteCarloLength(){
            return ComplexErrorProp.monteCarloLength;
        }

        // COPY
        // Copy a single complex error prop number [static method]
        public static ComplexErrorProp copy(ComplexErrorProp a){
            if(a==null){
    	        return null;
    	    }
    	    else{
    	        ComplexErrorProp b = new ComplexErrorProp();
                b.eReal=a.eReal.copy();
                b.eImag=a.eImag.copy();
                return b;
            }
        }

        // Copy a single complex error prop number [instance method]
        public ComplexErrorProp copy(){
            if(this==null){
    	        return null;
    	    }
    	    else{
                ComplexErrorProp b = new ComplexErrorProp();
                b.eReal=this.eReal.copy();
                b.eImag=this.eImag.copy();
                return b;
            }
        }

        //CLONE
        // Clone a single complex error prop number
        public Object clone(){
            if(this==null){
    	        return null;
    	    }
    	    else{
                ComplexErrorProp b = new ComplexErrorProp();
                b.eReal=this.eReal.copy();
                b.eImag=this.eImag.copy();
                return (Object) b;
            }
        }

        // ADDITION
        // Add two  ComplexErrorProp numbers [static method]
        public static ComplexErrorProp plus(ComplexErrorProp a, ComplexErrorProp b){
                ComplexErrorProp c = new ComplexErrorProp();
                c.eReal=a.eReal.plus(b.eReal);
                c.eImag=a.eImag.plus(b.eImag);
                return c;
        }

        //Add a  ComplexErrorProp number to this  ComplexErrorProp number [instance method]
        // this  ComplexErrorProp number remains unaltered
        public ComplexErrorProp plus(ComplexErrorProp a){
                ComplexErrorProp b = new ComplexErrorProp();
                b.eReal=this.eReal.plus(a.eReal);
                b.eImag=this.eImag.plus(a.eImag);
                return b;
        }

        //  SUBTRACTION
        //Subtract two  ComplexErrorProp numbers [static method]
        public static ComplexErrorProp minus (ComplexErrorProp a, ComplexErrorProp b){
                ComplexErrorProp c = new ComplexErrorProp();
                c.eReal=a.eReal.minus(b.eReal);
                c.eImag=a.eImag.minus(b.eImag);
                return c;
        }

        //Subtract a  ComplexErrorProp number from this  ComplexErrorProp number [instance method]
        // this  ComplexErrorProp number remains unaltered
        public ComplexErrorProp minus(ComplexErrorProp a){
                ComplexErrorProp b = new ComplexErrorProp();
                b.eReal=this.eReal.minus(a.eReal);
                b.eImag=this.eImag.minus(a.eImag);
                return b;
        }

        // MULTIPLICATION
        //Multiply two  ComplexErrorProp numbers [static method]
        public static ComplexErrorProp times(ComplexErrorProp a, ComplexErrorProp b){
                ComplexErrorProp c = new ComplexErrorProp();
                c.eReal=a.eReal.times(b.eReal).minus(a.eImag.times(b.eImag));
                c.eImag=a.eReal.times(b.eImag).plus(a.eImag.times(b.eReal));
                return c;
        }

         //Multiply two  ComplexErrorProp numbers [instance method]
        public ComplexErrorProp times(ComplexErrorProp b){
                ComplexErrorProp c = new ComplexErrorProp();
                c.eReal=this.eReal.times(b.eReal).minus(this.eImag.times(b.eImag));
                c.eImag=this.eReal.times(b.eImag).plus(this.eImag.times(b.eReal));
                return c;
        }

        //Multiply this ComplexErrorProp number by a ComplexErrorProp number and replace this by the product
        public void timesEquals(ComplexErrorProp a){
                ComplexErrorProp b = new ComplexErrorProp();
                b.eReal=a.eReal.times(this.eReal).minus(a.eImag.times(this.eImag));
                b.eImag=a.eReal.times(this.eImag).plus(a.eImag.times(this.eReal));
                this.eReal=b.eReal.copy();
                this.eImag=b.eImag.copy();
        }


        // DIVISION
        //Division of two ComplexErrorProp numbers a/b [static method]
        public static ComplexErrorProp over(ComplexErrorProp a, ComplexErrorProp b){
                ComplexErrorProp c = new ComplexErrorProp();
                PsRandom ran = new PsRandom();
                double[] aReal = ran.gaussianArray(a.eReal.getValue(), a.eReal.getError(), ComplexErrorProp.monteCarloLength);
                double[] aImag = ran.gaussianArray(a.eImag.getValue(), a.eImag.getError(), ComplexErrorProp.monteCarloLength);
                double[] bReal = ran.gaussianArray(b.eReal.getValue(), b.eReal.getError(), ComplexErrorProp.monteCarloLength);
                double[] bImag = ran.gaussianArray(b.eImag.getValue(), b.eImag.getError(), ComplexErrorProp.monteCarloLength);
                double[] rat = new double[ComplexErrorProp.monteCarloLength];
                double[] denm = new double[ComplexErrorProp.monteCarloLength];
                double[] cReal = new double[ComplexErrorProp.monteCarloLength];
                double[] cImag = new double[ComplexErrorProp.monteCarloLength];

                for(int i=0; i<ComplexErrorProp.monteCarloLength; i++){

                    if(Math.abs(bReal[i])>=Math.abs(bImag[i])){
                        rat[i]   = bImag[i]/bReal[i];
                        denm[i]  = bReal[i] + bImag[i]*rat[i];
                        cReal[i] = (aReal[i] + aImag[i]*rat[i])/denm[i];
                        cImag[i] = (aImag[i] - aReal[i]*rat[i])/denm[i];
                    }
                    else{
                        rat[i]   = bReal[i]/bImag[i];
                        denm[i]  = bReal[i]*rat[i] + bImag[i];
                        cReal[i] = (aReal[i]*rat[i] + aImag[i])/denm[i];
                        cImag[i] = (aImag[i]*rat[i] - aReal[i])/denm[i];
                    }
                }
                double cRealSum = 0.0D;
                double cImagSum = 0.0D;
                double cRealErrorSum = 0.0D;
                double cImagErrorSum = 0.0D;
                for(int i=0; i<ComplexErrorProp.monteCarloLength; i++){
                    cRealSum += cReal[i];
                    cImagSum += cImag[i];
                }
                cRealSum /= ComplexErrorProp.monteCarloLength;
                cImagSum /= ComplexErrorProp.monteCarloLength;
                for(int i=0; i<ComplexErrorProp.monteCarloLength; i++){
                    cRealErrorSum += Fmath.square(cRealSum - cReal[i]);
                    cImagErrorSum += Fmath.square(cImagSum - cImag[i]);
                }
                cRealErrorSum = Math.sqrt(cRealErrorSum/(ComplexErrorProp.monteCarloLength-1));
                cImagErrorSum = Math.sqrt(cImagErrorSum/(ComplexErrorProp.monteCarloLength-1));
                c.eReal.setError(cRealErrorSum);
                c.eImag.setError(cImagErrorSum);

                double denom = 0.0D;
                double ratio = 0.0D;
                if(Math.abs(b.eReal.getValue())>=Math.abs(b.eImag.getValue())){
                        ratio=b.eImag.getValue()/b.eReal.getValue();
                        denom=b.eReal.getValue()+b.eImag.getValue()*ratio;
                        c.eReal.setValue((a.eReal.getValue()+a.eImag.getValue()*ratio)/denom);
                        c.eImag.setValue((a.eImag.getValue()-a.eReal.getValue()*ratio)/denom);
                }
                else{
                        ratio=b.eReal.getValue()/b.eImag.getValue();
                        denom=b.eReal.getValue()*ratio+b.eImag.getValue();
                        c.eReal.setValue((a.eReal.getValue()*ratio+a.eImag.getValue())/denom);
                        c.eImag.setValue((a.eImag.getValue()*ratio-a.eReal.getValue())/denom);
                }
                return c;
        }
        //Division of this ComplexErrorProp number by a ComplexErrorProp number [instance method]
        // this ComplexErrorProp number remains unaltered
        public ComplexErrorProp over(ComplexErrorProp b){
                ComplexErrorProp c = new ComplexErrorProp();
                PsRandom ran = new PsRandom();
                double[] aReal = ran.gaussianArray(this.eReal.getValue(), this.eReal.getError(), ComplexErrorProp.monteCarloLength);
                double[] aImag = ran.gaussianArray(this.eImag.getValue(), this.eImag.getError(), ComplexErrorProp.monteCarloLength);
                double[] bReal = ran.gaussianArray(b.eReal.getValue(), b.eReal.getError(), ComplexErrorProp.monteCarloLength);
                double[] bImag = ran.gaussianArray(b.eImag.getValue(), b.eImag.getError(), ComplexErrorProp.monteCarloLength);
                double[] rat = new double[ComplexErrorProp.monteCarloLength];
                double[] denm = new double[ComplexErrorProp.monteCarloLength];
                double[] cReal = new double[ComplexErrorProp.monteCarloLength];
                double[] cImag = new double[ComplexErrorProp.monteCarloLength];

                for(int i=0; i<ComplexErrorProp.monteCarloLength; i++){

                    if(Math.abs(bReal[i])>=Math.abs(bImag[i])){
                        rat[i]   = bImag[i]/bReal[i];
                        denm[i]  = bReal[i] + bImag[i]*rat[i];
                        cReal[i] = (aReal[i] + aImag[i]*rat[i])/denm[i];
                        cImag[i] = (aImag[i] - aReal[i]*rat[i])/denm[i];
                    }
                    else{
                        rat[i]   = bReal[i]/bImag[i];
                        denm[i]  = bReal[i]*rat[i] + bImag[i];
                        cReal[i] = (aReal[i]*rat[i] + aImag[i])/denm[i];
                        cImag[i] = (aImag[i]*rat[i] - aReal[i])/denm[i];
                    }
                }
                double cRealSum = 0.0D;
                double cImagSum = 0.0D;
                double cRealErrorSum = 0.0D;
                double cImagErrorSum = 0.0D;
                for(int i=0; i<ComplexErrorProp.monteCarloLength; i++){
                    cRealSum += cReal[i];
                    cImagSum += cImag[i];
                }
                cRealSum /= ComplexErrorProp.monteCarloLength;
                cImagSum /= ComplexErrorProp.monteCarloLength;
                for(int i=0; i<ComplexErrorProp.monteCarloLength; i++){
                    cRealErrorSum += Fmath.square(cRealSum - cReal[i]);
                    cImagErrorSum += Fmath.square(cImagSum - cImag[i]);
                }
                cRealErrorSum = Math.sqrt(cRealErrorSum/(ComplexErrorProp.monteCarloLength-1));
                cImagErrorSum = Math.sqrt(cImagErrorSum/(ComplexErrorProp.monteCarloLength-1));
                c.eReal.setError(cRealErrorSum);
                c.eImag.setError(cImagErrorSum);

                double denom = 0.0D;
                double ratio = 0.0D;
                if(Math.abs(b.eReal.getValue())>=Math.abs(b.eImag.getValue())){
                        ratio=b.eImag.getValue()/b.eReal.getValue();
                        denom=b.eReal.getValue()+b.eImag.getValue()*ratio;
                        c.eReal.setValue((this.eReal.getValue()+this.eImag.getValue()*ratio)/denom);
                        c.eImag.setValue((this.eImag.getValue()-this.eReal.getValue()*ratio)/denom);
                }
                else{
                        ratio=b.eReal.getValue()/b.eImag.getValue();
                        denom=b.eReal.getValue()*ratio+b.eImag.getValue();
                        c.eReal.setValue((this.eReal.getValue()*ratio+this.eImag.getValue())/denom);
                        c.eImag.setValue((this.eImag.getValue()*ratio-this.eReal.getValue())/denom);
                }
                return c;
        }

        //Exponential [static method]
        public static ComplexErrorProp exp(ComplexErrorProp aa){
            ComplexErrorProp bb = new ComplexErrorProp();
            ErrorProp pre = ErrorProp.exp(aa.eReal);
            bb.eReal = pre.times(ErrorProp.cos(aa.eImag),aa.corrCoeff);
            bb.eImag = pre.times(ErrorProp.sin(aa.eImag),aa.corrCoeff);
            return bb;
        }

        //Exponential [instance method]
        public ComplexErrorProp exp(){
            ComplexErrorProp bb = new ComplexErrorProp();
            ErrorProp pre = ErrorProp.exp(this.eReal);
            bb.eReal = pre.times(ErrorProp.cos(this.eImag),this.corrCoeff);
            bb.eImag = pre.times(ErrorProp.sin(this.eImag),this.corrCoeff);
            return bb;

        }

        //Absolute value (modulus) [static method]
        public static ErrorProp abs(ComplexErrorProp aa){
                ErrorProp bb = new ErrorProp();
                double realV = aa.eReal.getValue();
                double imagV = aa.eImag.getValue();

                double rmod = Math.abs(realV);
                double imod = Math.abs(imagV);
                double ratio = 0.0D;
                double res = 0.0D;

                if(rmod==0.0){
                        res=imod;
                }
                else{
                        if(imod==0.0){
                                res=rmod;
                        }
                        if(rmod>=imod){
                                ratio=imagV/realV;
                                res=rmod*Math.sqrt(1.0 + ratio*ratio);
                        }
                        else
                        {
                                ratio=realV/imagV;
                                res=imod*Math.sqrt(1.0 + ratio*ratio);
                        }
                }
                bb.setValue(res);

                double realE = aa.eReal.getError();
                double imagE = aa.eImag.getError();
                res = hypotWithRho(2.0D*realE*realV, 2.0D*imagE*imagV, aa.corrCoeff);
                bb.setError(res);

                return bb;
        }

        //Absolute value (modulus) [instance method]
        public ErrorProp abs(){
                ErrorProp aa = new ErrorProp();
                double realV = this.eReal.getValue();
                double imagV = this.eImag.getValue();

                double rmod = Math.abs(realV);
                double imod = Math.abs(imagV);
                double ratio = 0.0D;
                double res = 0.0D;

                if(rmod==0.0){
                        res=imod;
                }
                else{
                        if(imod==0.0){
                                res=rmod;
                        }
                        if(rmod>=imod){
                                ratio=imagV/realV;
                                res=rmod*Math.sqrt(1.0 + ratio*ratio);
                        }
                        else
                        {
                                ratio=realV/imagV;
                                res=imod*Math.sqrt(1.0 + ratio*ratio);
                        }
                }
                aa.setValue(res);

                double realE = this.eReal.getError();
                double imagE = this.eImag.getError();
                res = hypotWithRho(2.0D*realE*realV, 2.0D*imagE*imagV, this.corrCoeff);
                aa.setError(res);

                return aa;
        }

        //Argument of a ComplexErrorProp  [static method]
        public static ErrorProp arg(ComplexErrorProp a){
            ErrorProp b = new ErrorProp();
            b = ErrorProp.atan2(a.eReal, a.eImag, a.corrCoeff);
            return b;
        }

        //Argument of a ComplexErrorProp  [instance method]
        public ErrorProp arg(double rho){
            ErrorProp a = new ErrorProp();
            a = ErrorProp.atan2(this.eReal, this.eImag, this.corrCoeff);
            return a;
        }

        // Returns sqrt(a*a+b*b + 2*a*b*rho) [without unecessary overflow or underflow]
        public static double hypotWithRho(double aa, double bb, double rho){
                double amod=Math.abs(aa);
                double bmod=Math.abs(bb);
                double cc = 0.0D, ratio = 0.0D;
                if(amod==0.0){
                        cc=bmod;
                }
                else{
                        if(bmod==0.0){
                                cc=amod;
                        }
                        else{
                                if(amod>=bmod){
                                        ratio=bmod/amod;
                                        cc=amod*Math.sqrt(1.0 + ratio*ratio + 2.0*rho*ratio);
                                }
                                else{
                                        ratio=amod/bmod;
                                        cc=bmod*Math.sqrt(1.0 + ratio*ratio + 2.0*rho*ratio);
                                }
                        }
                }
                return cc;
        }

        // TRUNCATION
        // Rounds the mantissae of both the value and error parts of Errorprop to prec places
        public static ComplexErrorProp truncate(ComplexErrorProp x, int prec){
                if(prec<0)return x;

                double rV = x.eReal.getValue();
                double rE = x.eReal.getError();
                double iV = x.eImag.getValue();
                double iE = x.eImag.getError();
                ComplexErrorProp y = new ComplexErrorProp();

                rV = Fmath.truncate(rV, prec);
                rE = Fmath.truncate(rE, prec);
                iV = Fmath.truncate(iV, prec);
                iE = Fmath.truncate(iE, prec);

                y.reset(rV, rE, iV, iE);
                return y;
        }

        // instance method
        public  ComplexErrorProp truncate(int prec){
                if(prec<0)return this;

                double rV = this.eReal.getValue();
                double rE = this.eReal.getError();
                double iV = this.eImag.getValue();
                double iE = this.eImag.getError();

                ComplexErrorProp y = new ComplexErrorProp();

                rV = Fmath.truncate(rV, prec);
                rE = Fmath.truncate(rE, prec);
                iV = Fmath.truncate(iV, prec);
                iE = Fmath.truncate(iE, prec);

                y.reset(rV, rE, iV, iE);
                return y;
        }

        // CONVERSIONS
        // Format a ComplexErrorProp number as a string
        // Overides java.lang.String.toString()
        public String toString(){
            return "Real part: " + this.eReal.getValue() + ", error = " + this.eReal.getError() + "; Imaginary part: "  + this.eImag.getValue() + ", error = " + this.eImag.getError();
        }

        // Format a ComplexErrorProp number as a string
        // See static method above for comments
        public static String toString(ComplexErrorProp aa){
            return "Real part: " + aa.eReal.getValue() + ", error = " + aa.eReal.getError() + "; Imaginary part: "  + aa.eImag.getValue() + ", error = " + aa.eImag.getError();
        }

        //PRINT AN COMPLEX ERROR NUMBER
        // Print to terminal window with text (message) and a line return
        public void println(String message){
                System.out.println(message + " " + this.toString());
        }

        // Print to terminal window without text (message) but with a line return
        public void println(){
                System.out.println(" " + this.toString());
        }

        // Print to terminal window with text (message) but without line return
        public void print(String message){
                System.out.print(message + " " + this.toString());
        }

        // Print to terminal window without text (message) and without line return
        public void print(){
                System.out.print(" " + this.toString());
        }
}