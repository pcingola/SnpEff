/*      Class OpenLoop
*
*       This class supports the creation of a path of Black Boxes
*       i.e. of instances of BlackBox and of any of its subclasses,
*       e.g. PropIntDeriv, FirstOrder, and the methods to combine
*       these into both a single instance of BlackBox and a Vector
*       of analogue segments, digital segments and converters.
*
*       Author:  Michael Thomas Flanagan.
*
*       Created: August 2002
*	    Updated: 12 July 2003, 10 May 2005, 2 July 2006, 7 June 2007, 6 April 2008, 5 July 2008, 2-7 November 2009
*
*       DOCUMENTATION:
*       See Michael T Flanagan's JAVA library on-line web page:
*       http://www.ee.ucl.ac.uk/~mflanaga/java/OpenLoop.html
*       http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*
*   Copyright (c) 2002 - 2009   Michael Thomas Flanagan
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

package flanagan.control;

import java.util.ArrayList;
import java.util.Vector;
import flanagan.complex.Complex;
import flanagan.complex.ComplexPoly;

public class OpenLoop extends BlackBox{
    private ArrayList<BlackBox> openPath = new ArrayList<BlackBox>(); // open path boxes
    private ArrayList<Object> segments = new ArrayList<Object>();     // start of segment, end of segment, type of each segment, i.e. analogue, digital, AtoD, DtoA, ZOH

    private int nBoxes = 0;                     // number of boxes in original path
    private int nSeg = 0;                       // number of analogue, digital, AtoD, ZOH segments

    private boolean checkPath = false;          // true if segment has been called
    private boolean checkNoMix = true;          // true - no ADC or DAC
    private boolean checkConsolidate = false;   // true if consolidate has been called

    private boolean[] adcs = null;              // true if box = ADC
    private boolean[] dacs = null;              // true ifbox = DAC
    private boolean[] zeroHolds = null;         // true ifbox = Zero Order Hold

    // Constructor
    public OpenLoop(){
        super("OpenLoop");
    }

    // Add box to the open path
    public void addBoxToPath(BlackBox box){
        this.openPath.add(box);
        this.nBoxes++;
    }

    // Consolidate all boxes into appropriate segments and combine all boxes into one box
    public void consolidate(){
        // Empty segments ArrayList if openPath ArrayList has been updated
        if(!segments.isEmpty()){
            segments.clear();
            this.nBoxes = 0;
            this.nSeg = 0;
            this.checkNoMix = true;
            this.checkPath = false;
        }

        // Find analogue, digital and conversion segments in OpenLoop
        this.segment();

        // Combine all boxes into a single box and make this instance that combined box
         BlackBox aa = null;
        if(this.nSeg==1){
            if(this.nBoxes==1){
                aa = (BlackBox) this.openPath.get(0);
            }
            else{
                aa = (BlackBox) this.segments.get(3);
            }
        }
        else{
            aa = this.combineSegment(0, this.nBoxes);
        }
        super.sNumer = aa.sNumer.copy();
        super.sDenom = aa.sDenom.copy();
        super.sNumerPade = aa.sNumerPade.copy();
        super.sDenomPade = aa.sDenomPade.copy();
        super.sNumerDeg = aa.sNumerDeg;
        super.sDenomDeg = aa.sDenomDeg;
        super.sNumerDegPade = aa.sNumerDegPade;
        super.sDenomDegPade = aa.sDenomDegPade;
        super.sNumerSet = true;
        super.sDenomSet = true;
        super.deadTime = aa.deadTime;
        super.sZeros = Complex.copy(aa.sZeros);
        super.sPoles = Complex.copy(aa.sPoles);
        super.sZerosPade = Complex.copy(aa.sZerosPade);
        super.sPolesPade = Complex.copy(aa.sPolesPade);
        super.padeAdded=true;
        if(super.sNumerDeg==0){
            super.sNumerScaleFactor = super.sNumer.coeffCopy(0);
        }
        else{
            super.sNumerScaleFactor = BlackBox.scaleFactor(super.sNumer, super.sZeros);
        }
        if(super.sDenomDeg==0){
            super.sDenomScaleFactor = super.sDenom.coeffCopy(0);
        }
        else{
            super.sDenomScaleFactor = BlackBox.scaleFactor(super.sDenom, super.sPoles);
        }
        this.checkConsolidate = true;
    }

        // Find analogue and digital segments
    public void segment(){
        // Find ADCs, DACs and ZeroOrderHolds
        this.adcs = new boolean[nBoxes];
        int nADCs = 0;
        this.dacs = new boolean[nBoxes];
        int nDACs = 0;
        this.zeroHolds = new boolean[nBoxes];
        int nZeroHolds = 0;
        String thisName = null;
        for(int i=0; i<nBoxes; i++){
            adcs[i] = false;
            dacs[i] = false;
            zeroHolds[i] = false;
            BlackBox aa = openPath.get(i);
            thisName = aa.fixedName;
            if(thisName.equals("ADC")){
                adcs[i]=true;
                nADCs++;
            }
            else{
                if(thisName.equals("DAC")){
                    dacs[i]=true;
                    nDACs++;
                }
                else{
                    if(thisName.equals("ZeroOrderHold")){
                        zeroHolds[i]=true;
                        nZeroHolds++;
                    }
                }
            }
        }

        if(nADCs==0 && nDACs==0){
            this.nSeg = 1;                          // number of analogue, digital, AtoD, ZOH segments
            this.checkNoMix = true;                 // true - no ADC or DAC
            this.checkPath = true;                  // true if segment has been called
            this.segments.add(new Integer(0));
            this.segments.add(new Integer(nBoxes-1));
            this.segments.add("analogue");
            BlackBox bb = this.combineSegment(0, nBoxes-1);
            this.segments.add(bb);
        }
        else{
            this.nSeg = 0;
            int adc0 = 0;
            int dac0 = 0;
            boolean adcFirst = false;
            if(nADCs>0 && nDACs>0){
                // first adc or dac
                boolean test0 = true;
                adc0 = 0;
                while(test0){
                    if(adcs[adc0]){
                        test0 = false;
                    }
                    else{
                        adc0++;
                        if(adc0>=nBoxes){
                            test0 = false;
                        }
                    }
                }
                test0 = true;
                while(test0){
                    if(dacs[dac0]){
                        test0 = false;
                    }
                    else{
                        dac0++;
                        if(dac0>=nBoxes){
                            test0 = false;
                        }
                    }
                }
                if(adc0<dac0)adcFirst =true;
            }
            else{
                if(nADCs>0)adcFirst=true;
            }
            boolean adswitch = adcFirst;
            this.nSeg++;

            int nextStart = 0;
            if(adcFirst){
                this.segments.add(new Integer(0));
                this.segments.add(new Integer(adc0));
                this.segments.add("digital");
                BlackBox bb = this.combineSegment(0, adc0);
                this.segments.add(bb);
                nextStart = adc0+1;
            }
            else{
                this.segments.add(new Integer(0));
                this.segments.add(new Integer(dac0));
                this.segments.add("analogue");
                BlackBox bb = this.combineSegment(0, dac0);
                this.segments.add(bb);
                nextStart = dac0+1;
            }

            // Find all analogue and digital segments
            boolean test1 = true;
            if(nextStart>=this.nBoxes)test1 = false;
            while(test1){
                if(adswitch){
                    nextStart = nextDigitalSegment(nextStart);
                    adswitch = false;
                }
                else{
                   nextStart = nextAnalogueSegment(nextStart);
                   adswitch = true;
                }
                if(nextStart>=this.nBoxes)test1 = false;
            }

        }



    }

    // Find next digital segment
    private int nextDigitalSegment(int box0){
            // next adc
            int nextAdc = nBoxes;
            boolean endFound = false;
            boolean test = true;
            int ii = box0;
            while(test){
                if(this.adcs[ii]){
                    nextAdc = ii;
                    test = false;
                 }
                 else{
                    ii++;
                    if(ii>=nBoxes)test = false;
                }
            }

            // next dac
            int nextDac = nBoxes;
            test = true;
            ii = box0;
            while(test){
                if(dacs[ii]){
                    nextDac = ii;
                    test = false;
                }
                else{
                    ii++;
                    if(ii>=nBoxes){
                        test = false;
                        endFound = true;
                    }
                }
            }
            if(endFound)nextDac = nBoxes-1;
            if(nextAdc<nextDac)throw new IllegalArgumentException("Two consecutive ADCs with no intervening DAC");
            this.nSeg++;
            this.segments.add(new Integer(0));
            this.segments.add(new Integer(nextDac));
            this.segments.add("digital");
            BlackBox bb = this.combineSegment(0, nextDac);
            this.segments.add(bb);

            return nextDac + 1;
    }

        // Find next analogue segment
    private int nextAnalogueSegment(int box0){
            // next adc
            int nextAdc = nBoxes;
            boolean endFound = false;
            boolean test = true;
            int ii = box0;
            while(test){
                if(this.adcs[ii]){
                    nextAdc = ii;
                    test = false;
                 }
                 else{
                    ii++;
                    if(ii>=nBoxes){
                        test = false;
                        endFound = true;
                    }
                }
            }

            // next dac
            int nextDac = nBoxes;
            test = true;
            ii = box0;
            while(test){
                if(dacs[ii]){
                    nextDac = ii;
                    test = false;
                }
                else{
                    ii++;
                    if(ii>=nBoxes){
                        test = false;
                    }
                }
            }
            if(endFound)nextAdc = nBoxes-1;
            if(nextDac<nextAdc)throw new IllegalArgumentException("Two consecutive DACs with no intervening ADC");
            this.nSeg++;
            this.segments.add(new Integer(0));
            this.segments.add(new Integer(nextAdc));
            this.segments.add("digital");
            BlackBox bb = this.combineSegment(0, nextAdc);
            this.segments.add(bb);

            return nextAdc+1;
    }

    // Combine all boxes between iLow and iHigh into one box
    public BlackBox combineSegment(int iLow, int iHigh){
        ArrayList<Complex> zeros = new ArrayList<Complex>();
        ArrayList<Complex> poles = new ArrayList<Complex>();
        ArrayList<Complex> zerosPade = new ArrayList<Complex>();
        ArrayList<Complex> polesPade = new ArrayList<Complex>();


        BlackBox aa = new BlackBox();           // Black Box to be returned

        int nBoxSeg = iHigh - iLow + 1;         // number of boxes in segment

        BlackBox bb = openPath.get(iLow);       // first box in segment
        if(!bb.padeAdded)bb.transferPolesZeros();

        aa.sNumerPade = bb.sNumerPade.copy();
        aa.sDenomPade = bb.sDenomPade.copy();
        aa.sNumer = bb.sNumer.copy();
        aa.sDenom = bb.sDenom.copy();

        aa.sNumerDegPade = bb.sNumerDegPade;
        aa.sDenomDegPade = bb.sDenomDegPade;
        aa.sNumerDeg = bb.sNumerDeg;
        aa.sDenomDeg = bb.sDenomDeg;

        if(aa.sNumerDegPade>0){
            Complex[] bbsZerosPade = Complex.copy(bb.sZerosPade);
            for(int i=0; i<aa.sNumerDegPade; i++)zerosPade.add(bbsZerosPade[i]);
        }
        if(aa.sDenomDegPade>0){
            Complex[] bbsPolesPade = Complex.copy(bb.sPolesPade);
            for(int i=0; i<aa.sDenomDegPade; i++)polesPade.add(bbsPolesPade[i]);
        }
        if(aa.sNumerDeg>0){
            Complex[] bbsZeros = Complex.copy(bb.sZeros);
            for(int i=0; i<aa.sNumerDeg; i++)zeros.add(bbsZeros[i]);
        }
        if(aa.sDenomDeg>0){
            Complex[] bbsPoles = Complex.copy(bb.sPoles);
            for(int i=0; i<aa.sDenomDeg; i++)poles.add(bbsPoles[i]);
        }

        aa.deadTime = bb.deadTime;
        aa.sNumerScaleFactor = bb.sNumerScaleFactor.copy();
        aa.sDenomScaleFactor = bb.sDenomScaleFactor.copy();

        for(int i=1; i<nBoxSeg; i++){
            bb = this.openPath.get(i+iLow);
            if(!bb.padeAdded)bb.transferPolesZeros();
            if(aa.sNumerPade==null){
                if(bb.sNumerPade!=null){
                    aa.sNumerPade = bb.sNumerPade.copy();
                }
            }
            else{
                if(bb.sNumerPade!=null){
                    aa.sNumerPade = aa.sNumerPade.times(bb.sNumerPade);
                }
            }

            if(aa.sNumer==null){
                if(bb.sNumer!=null){
                    aa.sNumer = bb.sNumer.copy();
                }
            }
            else{
                if(bb.sNumer!=null){
                    aa.sNumer = aa.sNumer.times(bb.sNumer);
                }
            }

            if(aa.sDenom==null){
                if(bb.sDenom!=null){
                    aa.sDenom = bb.sDenom.copy();
                }
            }
            else{
                if(bb.sDenom!=null){
                     aa.sDenom = aa.sDenom.times(bb.sDenom);
                }
            }

            if(aa.sDenomPade==null){
                if(bb.sDenomPade!=null){
                    aa.sDenomPade = bb.sDenomPade.copy();
                }
            }
            else{
                if(bb.sDenomPade!=null){
                     aa.sDenomPade = aa.sDenomPade.times(bb.sDenomPade);
                }
            }

            aa.sNumerDegPade += bb.sNumerDegPade;
            aa.sDenomDegPade += bb.sDenomDegPade;
            aa.sNumerDeg += bb.sNumerDeg;
            aa.sDenomDeg += bb.sDenomDeg;

            aa.sNumerScaleFactor = bb.sNumerScaleFactor.times(aa.sNumerScaleFactor);
            aa.sDenomScaleFactor = bb.sDenomScaleFactor.times(aa.sDenomScaleFactor);

            aa.deadTime += bb.deadTime;

            if(bb.sNumerDegPade>0){
                Complex[] bbsZerosPade = Complex.copy(bb.sZerosPade);
                for(int ii=0; ii<bb.sNumerDegPade; ii++)zerosPade.add(bbsZerosPade[ii]);
            }
            if(bb.sDenomDegPade>0){
                Complex[] bbsPolesPade = Complex.copy(bb.sPolesPade);
                for(int ii=0; ii<bb.sDenomDegPade; ii++)polesPade.add(bbsPolesPade[ii]);
            }
            if(bb.sNumerDeg>0){
                Complex[] bbsZeros = Complex.copy(bb.sZeros);
                for(int ii=0; ii<bb.sNumerDeg; ii++)zeros.add(bbsZeros[ii]);
            }
            if(bb.sDenomDeg>0){
                Complex[] bbsPoles = Complex.copy(bb.sPoles);
                for(int ii=0; ii<bb.sDenomDeg; ii++)poles.add(bbsPoles[ii]);
            }
        }

        if(aa.sNumerDegPade>0){
            aa.sZerosPade = Complex.oneDarray(aa.sNumerDegPade);
            for(int ii=0; ii<aa.sNumerDegPade; ii++)aa.sZerosPade[ii] = zerosPade.get(ii);
        }
        if(aa.sDenomDegPade>0){
            aa.sPolesPade = Complex.oneDarray(aa.sDenomDegPade);
            for(int ii=0; ii<aa.sDenomDegPade; ii++)aa.sPolesPade[ii] = polesPade.get(ii);
        }
        if(aa.sNumerDeg>0){
            aa.sZeros = Complex.oneDarray(aa.sNumerDeg);
            for(int ii=0; ii<aa.sNumerDeg; ii++)aa.sZeros[ii] = zeros.get(ii);
        }
        if(aa.sDenomDeg>0){
            aa.sPoles = Complex.oneDarray(aa.sDenomDeg);
            for(int ii=0; ii<aa.sDenomDeg; ii++)aa.sPoles[ii] = poles.get(ii);
        }
        return aa;

    }

    // Return number of boxes in path
    public int getNumberOfBoxes(){
        if(!checkConsolidate)this.consolidate();
        return this.nBoxes;
    }

    // Return segment ArrayList
    public ArrayList<Object> getSegmentsArrayList(){
        if(!checkConsolidate)this.consolidate();
        return this.segments;
    }

    // Return segment Vector
    public Vector<Object> getSegmentsVector(){
        if(!checkConsolidate)this.consolidate();
        ArrayList<Object> seg = this.segments;
        Vector<Object> ret = null;
        if(seg!=null){
            int n = seg.size();
            ret = new Vector<Object>(n);
            for(int i=0; i<n; i++)ret.addElement(seg.get(i));
        }
        return ret;
    }

    // Return number of segments in path
    public int getNumberOfSegments(){
        if(!checkConsolidate)this.consolidate();
        return this.nSeg;
    }

    // Return name of all boxes in path
    public String getNamesOfBoxes(){
        if(!checkConsolidate)this.consolidate();
        String names = "";
        for(int i=0; i<this.nBoxes; i++){
            BlackBox bb = openPath.get(i);
            names = names + i +": "+bb.getName() + "   ";
        }
        return names;
    }

    // Remove all boxes from the path
    public void removeAllBoxes(){
         // Empty openPath ArrayList
        if(!openPath.isEmpty()){
            openPath.clear();
        }

        // Empty segments ArrayList
        if(!segments.isEmpty()){
            segments.clear();
        }
        this.nSeg = 0;
        this.checkNoMix = true;
        this.checkPath = false;
        this.nBoxes = 0;
        this.checkConsolidate = false;
        this.adcs = null;
        this.dacs = null;
        this.zeroHolds = null;

    }

    // return checkNoMix
    public boolean getCheckNoMix(){
        return this.checkNoMix;
    }


    // Deep copy
    public OpenLoop copy(){
        if(this==null){
            return null;
        }
        else{
            OpenLoop bb = new OpenLoop();
            this.copyBBvariables(bb);

            bb.nBoxes = this.nBoxes;
            bb.nSeg = this.nSeg;
            bb.checkPath = this.checkPath;
            bb.checkNoMix = this.checkNoMix;
            bb.checkConsolidate = this.checkConsolidate;
            if(this.openPath.size()==0){
                bb.openPath = new ArrayList<BlackBox>();
            }
            else{
                for(int i=0; i<openPath.size(); i++)bb.openPath.add((this.openPath.get(i)).copy());
            }
            if(this.segments.size()==0){
                bb.segments = new ArrayList<Object>();
            }
            else{
                int j=0;
                for(int i=0; i<this.nSeg; i++){
                    Integer holdI1 = (Integer)this.segments.get(j);
                    int ii = holdI1.intValue();
                    bb.segments.add(new Integer(ii));
                    j++;
                    Integer holdI2 = (Integer)this.segments.get(j);
                    ii = holdI2.intValue();
                    bb.segments.add(new Integer(ii));
                    j++;
                    String holdS = (String)this.segments.get(j);
                    bb.segments.add(holdS);
                    j++;
                    bb.segments.add(((BlackBox)this.segments.get(j)).copy());
                    j++;
                }
            }

            return bb;
        }
    }

    // Clone - overrides Java.Object method clone
    public Object clone(){
        return (Object)this.copy();
    }
}
