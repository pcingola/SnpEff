/*      Class ClosedLoop
*
*       This class supports the creation of a path of Black Boxes
*       i.e. of instances of BlackBox and of any of its subclasses,
*       e.g. PropIntDeriv, FirstOrder, and the methods to combine
*       these into both a single instance of BlackBox and a Vector
*       of analogue segments, digital segments and converters,
*       with a feedback path from the last box on the forward path to the first box on the forward path
*
*       Author:  Michael Thomas Flanagan.
*
*       Created: August 2002
*	    Updated: 14 May 2005, 6 April 2008, 5 July 2008, 2-7 November 2009
*
*       DOCUMENTATION:
*       See Michael T Flanagan's JAVA library on-line web page:
*       http://www.ee.ucl.ac.uk/~mflanaga/java/OpenLoop.html
*       http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*
*   Copyright (c) 2002 - 2008   Michael Thomas Flanagan
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

import java.util.Vector;
import java.util.ArrayList;
import flanagan.complex.Complex;
import flanagan.complex.ComplexPoly;
import flanagan.control.OpenLoop;

public class ClosedLoop extends BlackBox{
    private OpenLoop forwardPath = new OpenLoop();  // forward path boxes
    private OpenLoop closedPath = new OpenLoop();   // full closed path boxes

    private ArrayList<BlackBox> feedbackPath = new ArrayList<BlackBox>(); // feedback path boxes
    private int nFeedbackBoxes = 0;             // number of boxes in feedback path

    private boolean checkNoMix = true;          // true - no ADC or DAC
    private boolean checkConsolidate = false;   // true if consolidate has been called

    private double deadTimeSum = 0.0;           // dead time is replaced by Pade approximation to facilitate division
                                                // sNumer and sDenom equated to sNumerPade and sDenomPade
                                                // super.deadTime is replaced by zero
                                                // true dead time stored in deadTimeSum which is returned by this classes getDeadTime method

    // Constructor
    public ClosedLoop(){
        super("ClosedLoop");
    }

    // Add box to the forward path
    public void addBoxToForwardPath(BlackBox box){
        this.forwardPath.addBoxToPath(box);
    }

    // Add box to the open path
    public void addBoxToFeedbackPath(BlackBox box){
        this.feedbackPath.add(box);
        this.nFeedbackBoxes++;
    }

    // Consolidate all boxes into appropriate segments and
    //  combine all boxes into either on forward path box or one closed loop box
    public void consolidate(){

        // add feedback boxes to forward path boxes
        this.closedPath = this.forwardPath.copy();
        for(int i=0; i<this.nFeedbackBoxes; i++){
            this.closedPath.addBoxToPath(this.feedbackPath.get(i));
        }

        // combine forward path boxes
        this.forwardPath.consolidate();
        if(!this.forwardPath.getCheckNoMix())this.checkNoMix = false;

        // combine closed path boxes
        this.closedPath.consolidate();
        if(!this.closedPath.getCheckNoMix())this.checkNoMix = false;

        // Calculate transfer function
        ComplexPoly fpNumer = this.forwardPath.getSnumer();
        ComplexPoly fpDenom = this.forwardPath.getSdenom();
        ComplexPoly cpNumer = this.closedPath.getSnumer();
        ComplexPoly cpDenom = this.closedPath.getSdenom();
        if(fpDenom.isEqual(cpDenom)){
            super.setSnumer(fpNumer.copy());
            super.setSdenom((cpNumer.plus(fpDenom)).copy());
        }
        else{
            super.setSnumer(fpNumer.times(cpDenom));
            super.setSdenom((cpNumer.times(fpDenom)).plus(cpDenom.times(fpDenom)));
        }
        this.checkConsolidate = true;
        this.deadTimeSum  = this.closedPath.getDeadTime();
        super.deadTime = 0.0;
        this.checkConsolidate = true;
    }

    // Return number of boxes in the forward path
    public int getNumberOfBoxesInForwardPath(){
        if(!checkConsolidate)this.consolidate();
        return this.forwardPath.getNumberOfBoxes();
    }

    // Return number of boxes in the closed path
    public int getNumberOfBoxesInClosedLoop(){
        if(!checkConsolidate)this.consolidate();
        return this.closedPath.getNumberOfBoxes();
    }

    // Return segment ArrayList for forward path
    public ArrayList<Object> getForwardPathSegmentsArrayList(){
        if(!checkConsolidate)this.consolidate();
        return this.forwardPath.getSegmentsArrayList();
    }

    // Return segment Vector for forward path
    public Vector<Object> getForwardPathSegmentsVector(){
        if(!checkConsolidate)this.consolidate();
        return this.forwardPath.getSegmentsVector();
    }


    // Return segment ArrayList for closed path
    public ArrayList<Object> getClosedLoopSegmentsArrayList(){
        if(!checkConsolidate)this.consolidate();
        return this.closedPath.getSegmentsArrayList();
    }

    // Return segment Vector for closed path
    public Vector<Object> getClosedLoopSegmentsVector(){
        if(!checkConsolidate)this.consolidate();
        return this.closedPath.getSegmentsVector();
    }

   // Return number of segments in the forward path
    public int getNumberOfSegmentsInForwardPath(){
        if(!checkConsolidate)this.consolidate();
        return this.forwardPath.getNumberOfSegments();
    }

    // Return number of segments in the closed path
    public int getNumberOfSegmentsInClosedLoop(){
        if(!checkConsolidate)this.consolidate();
        return this.closedPath.getNumberOfSegments();
    }

    // Return name of all boxes in forward path
    public String getNamesOfBoxesInForwardPath(){
        if(!checkConsolidate)this.consolidate();
        return this.forwardPath.getNamesOfBoxes();
    }

    // Return name of all boxes in closed path
    public String getNamesOfBoxesInClosedLoop(){
        if(!checkConsolidate)this.consolidate();
        return this.closedPath.getNamesOfBoxes();
    }

    // Remove all boxes from the path
    public void removeAllBoxes(){
        this.forwardPath.removeAllBoxes();
        this.closedPath.removeAllBoxes();
        this.feedbackPath.clear();
        this.checkNoMix = true;
        this.checkConsolidate = false;
        this.nFeedbackBoxes = 0;
    }

    // Get stored dead times
    public double getDeadTime(){
        return this.deadTimeSum;
    }

    // Deep copy
     // Deep copy
    public ClosedLoop copy(){
        if(this==null){
            return null;
        }
        else{
            ClosedLoop bb = new ClosedLoop();
            this.copyBBvariables(bb);

            bb.nFeedbackBoxes = this.nFeedbackBoxes;
            bb.checkNoMix = this.checkNoMix;
            bb.checkConsolidate = this.checkConsolidate;
            bb.forwardPath = this.forwardPath.copy();
            bb.closedPath = this.closedPath.copy();
            bb.feedbackPath = new ArrayList<BlackBox>();
            if(this.feedbackPath.size()!=0){
                for(int i=0; i<feedbackPath.size(); i++)bb.feedbackPath.add((this.feedbackPath.get(i)).copy());
            }

            return bb;
        }
    }

    // Clone - overrides Java.Object method clone
    public Object clone(){
        return (Object)this.copy();
    }
}