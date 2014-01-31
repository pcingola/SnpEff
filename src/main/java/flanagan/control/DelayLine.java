/*          Class DelayLine
*
*           This class contains the constructor to create an instance of
*           a dead time delay, independently of any of the existing BlackBox
*           subclasses or of the BlackBox superclass in which a dead time
*           may be set, and the methods needed to use this delay in
*           control loops in the time domain, Laplace transform s domain
*           or the z-transform z domain.
*
*           s-domain transfer function = exp(-Td.s)
*           Td is the delay time.
*           1 to 4 order Pade approximations available
*
*           This class is a subclass of the superclass BlackBox.
*
*           Author:  Michael Thomas Flanagan.
*
*           Created: August 2002.
*           Revised: 21 April 2003, 3 May 2005, 2 July 2006, 6 April 2008, 7 November 2009
*
*
*           DOCUMENTATION:
*           See Michael T Flanagan's JAVA library on-line web page:
*           http://www.ee.ucl.ac.uk/~mflanaga/java/DelayLine.html
*           http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2002 - 2009  Michael Thomas Flanagan
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/



package flanagan.control;

import flanagan.complex.Complex;

public class DelayLine extends BlackBox{

    // Constructor
    public DelayLine(double delayTime, int orderPade){
        super("DelayLine");
        super.setDeadTime(delayTime, orderPade);
    }

    // Constructor
    // Default Pade approximation order = 2
    public DelayLine(double delayTime){
        super("DelayLine");
        super.fixedName="DelayLine";
        super.setDeadTime(delayTime);
    }

    // Constructor
    // for deep copy purposes
    private DelayLine(){
        super("DelayLine");
    }

    // Set the delay time
    public void setDelayTime(double delayTime){
        super.setDeadTime(delayTime);
    }

    // Set the delay time and the Pade approximation order
    public void setDelayTime(double delayTime, int orderPade){
        super.setDeadTime(delayTime, orderPade);
    }

    // Get the delay time
    public double getDelayTime(){
        return super.deadTime;
    }

    // Deep copy
    public DelayLine copy(){
        if(this==null){
            return null;
        }
        else{
            DelayLine bb = new DelayLine();
            this.copyBBvariables(bb);

            return bb;
        }
    }

    // Clone - overrides Java.Object method clone
    public Object clone(){
        return (Object)this.copy();
    }
}