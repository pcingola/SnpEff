/*
*   Class   TimeAndDate
*
*   USAGE:  Methods concerned with returning times dates and timing events
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:       28 September 2009
*   AMENDED:    2-3 October 2009
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web pages:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*   http://www.ee.ucl.ac.uk/~mflanaga/java/TimeAndDate.html
*
*   Copyright (c) 2009
*
*   PERMISSION TO COPY:
*
*   Redistributions of this source code, or parts of, must retain the above
*   copyright notice, this list of conditions and the following disclaimer.
*
*   Redistribution in binary form of all or parts of this class, must reproduce
*   the above copyright, this list of conditions and the following disclaimer in
*   the documentation and/or other materials provided with the distribution.
*
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all
*   copies and associated documentation or publications.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/

package flanagan.math;

import java.util.Calendar;

public class TimeAndDate{

    private Calendar cal = Calendar.getInstance();      // instance of abstract class Calendar
    private String dayOfTheWeek = null;                 // day of the week
    private int dayOfTheMonth = 0;                      // day of the month
    private String monthOfTheYear = null;               // month of the year
    private int monthAsInteger = 0;                     // month as integer
    private int year = 0;                               // year
    private String fullDate = null;                     // date as 'day name', 'day of month' 'month name' year
    private String date = null;                         // 'day of month' 'month name' 'year'
    private String shortDateUK = null;                  // UK Format - 'day of month'.'month'.'year final two digits'
    private String shortDateUS = null;                  // US Format - 'month'/'day of month'/'year final two digits'

    private String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    private int[] monthDays = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    private int hour24 = -1;                            // hour of the day (24 hour clock)
    private String hour12 = null;                       // hour of the day as am or pm (12 hour clock)
    private int minute = -1;                            // minute of the hour
    private int second = -1;                            // seconds of the minute
    private int millisecond = -1;                       // milliseconds of the second
    private String shortTime24 = null;                  // time as hour.minute (24 hour clock)
    private String shortTime12 = null;                  // time as hour.minute AM or PM (12 hour clock)
    private String midTime24 = null;                    // time as hour.minute.second (24 hour clock)
    private String midTime12 = null;                    // time as hour.minute.second AM or PM (12 hour clock)
    private String fullTime24 = null;                   // time as hour.minute.second.millisecond (24 hour clock)
    private String fullTime12 = null;                   // time as hour.minute.second.millisecond AM OR PM (12 hour clock)

    private long tStart = 0L;                           // start time
    private boolean startCheck = false;                 // = true when timing start set
    private long tEnd = 0L;                             // end time
    private boolean endCheck = false;                   // = true when timing end set
    private long totalTime = 0L;                        // tEnd - tStart

    private String changeDate = null;                   // next BST change
    private boolean backForw = true;                    // = true next BST change is back, i.e if date is within BST; = false next BST change = forward

    private int easterMonth = 0;                        // Easter Sunday month
    private int easterDay = 0;                          // Easter Sunday day of the month
    private String easterDayName = null;                // Easter Sunday day name, i.e. "Sunday" - calculated as a check of the method

    private int yearHold = 0;                           // Last calculated year
    private int monthHold = 0;                          // Last calculated month
    private int dayHold = 0;                            // Last calculated day of the month
    private String dayNameHold = null;                  // Last calculated day name


    // CONSTRUCTOR
    public TimeAndDate(){

    }

    // Causes the program to wait for nSeconds seconds before continuing (int)
    public void waitFor(int nSeconds){
        long t0,t1;
        t0=System.currentTimeMillis();
        do{
            t1=System.currentTimeMillis();
        }
        while ((t1-t0)<nSeconds*1000);
    }

    // Causes the program to wait for nSeconds seconds before continuing (long)
    public void waitFor(long nSeconds){
        if(nSeconds>Long.MAX_VALUE/1000){
            System.out.println("Class: TimeAndDate, method: wait(long nSeconds), nSeconds is too large for this method - the value has been replaced by " + Long.MAX_VALUE/1000);
            nSeconds = Long.MAX_VALUE/1000;
        }
        long t0,t1;
        t0=System.currentTimeMillis();
        do{
            t1=System.currentTimeMillis();
        }
        while ((t1-t0)<nSeconds*1000);
    }

    // Causes the program to wait for nSeconds seconds before continuing (double)
    public void waitFor(double nSeconds){
        long tt = 0L;
        if(nSeconds>Math.pow(2.0, 63)-1.0){
            System.out.println("Class: TimeAndDate, method: wait(double nSeconds), nSeconds is too large for this method - the value has been replaced by " + Long.MAX_VALUE/1000);
            tt = Long.MAX_VALUE;
        }
        else{
            tt = Conv.convert_double_to_long(nSeconds*1000);
        }
        long t0,t1;
        t0=System.currentTimeMillis();
        do{
            t1=System.currentTimeMillis();
        }
        while ((t1-t0)<tt);
    }

    // Marker method for starting the timing of a bloc of code
    public void blocStart(){
        this.tStart = System.currentTimeMillis();
        this.startCheck = true;
    }

     // Marker method for ending the timing of a bloc of code and for returning the total time
     public long blocEnd(){
        if(this.startCheck){
            this.tEnd = System.currentTimeMillis();
            this.totalTime = this.tEnd - this.tStart;
            this.endCheck = true;
        }
        else{
            throw new IllegalArgumentException("No start marker has been set");
        }
        return this.totalTime;
    }

    // Returns total time taken to run a bloc of code
    public long blocTime(){
        if(this.endCheck){
            return this.totalTime;
        }
        else{
            if(!this.startCheck){
                System.out.println("Class Time: method totalTime:  No start marker has been set - -9999 rturned");
                return -9999L;
            }
            else{
                System.out.println("Class Time: method totalTime:  No end marker has been set - -8888 rturned");
                return -8888L;
            }
        }
    }

    // Get the hour of the day (24 hour clock)
    public int getHour24(){
        this.hour24 = cal.get(Calendar.HOUR_OF_DAY);
        return this.hour24;
    }

     // Get the hour of the day, am or pm (12 hour clock)
    public String getHour12(){
        int hour = cal.get(Calendar.HOUR);
        int amPm =  cal.get(Calendar.AM_PM);
        if(amPm==0){
            this.hour12 = (new Integer(hour)).toString() + " AM";
        }
        else{
            this.hour12 = (new Integer(hour)).toString() + " PM";
        }
        return this.hour12;
    }

    // Get the minute of the hour
    public int getMinute(){
        this.minute = cal.get(Calendar.MINUTE);
        return this.minute;
    }

    // Get the second of the minute
    public int getSecond(){
        this.second = cal.get(Calendar.SECOND);
        return this.second;
    }

    // Get the millisecond of the second
    public int getMilliSecond(){
        this.millisecond = cal.get(Calendar.MILLISECOND);
        return this.millisecond;
    }

    // Get time as hour.minute (24 hour clock)
    public String getShortTime24(){
        int hourI = this.getHour24();
        this.shortTime24 = (new Integer(hourI)).toString();
        int minI = this.getMinute();
        if(minI<10){
            this.shortTime24 += ".0" + minI;
        }
        else{
            this.shortTime24 += "." + minI;
        }
        return this.shortTime24;
    }

    // Get time as hour.minute AM or PM (12 hour clock)
    public String getShortTime12(){
        int hourI = cal.get(Calendar.HOUR);
        int amPm =  cal.get(Calendar.AM_PM);
        this.shortTime12 = (new Integer(hourI)).toString();
        int minI = this.getMinute();
        if(minI<10){
            this.shortTime12 += ".0" + minI;
        }
        else{
            this.shortTime12 += "." + minI;
        }
        if(amPm==0){
            this.shortTime12 += " " + "AM";
        }
        else{
            this.shortTime12 += " " + "PM";
        }
        return this.shortTime12;
    }

    // Get time as hour.minute.second (24 hour clock)
    public String getMidTime24(){
        int hourI = this.getHour24();
        this.midTime24 = (new Integer(hourI)).toString();
        int minI = this.getMinute();
        if(minI<10){
            this.midTime24 += ".0" + minI;
        }
        else{
            this.midTime24 += "." + minI;
        }
        int secI = this.getSecond();
        if(secI<10){
            this.midTime24 += ".0" + secI;
        }
        else{
            this.midTime24 += "." + secI;
        }
        return this.midTime24;
    }

    // Get time as hour.minute.second AM or PM (12 hour clock)
    public String getMidTime12(){
        int hourI = cal.get(Calendar.HOUR);
        int amPm =  cal.get(Calendar.AM_PM);
        this.midTime12 = (new Integer(hourI)).toString();
        int minI = this.getMinute();
        if(minI<10){
            this.midTime12 += ".0" + minI;
        }
        else{
            this.midTime12 += "." + minI;
        }
        int secI = this.getSecond();
        if(secI<10){
            this.midTime12 += ".0" + secI;
        }
        else{
            this.midTime12 += "." + secI;
        }
        if(amPm==0){
            this.midTime12 += " " + "AM";
        }
        else{
            this.midTime12 += " " + "PM";
        }
        return this.midTime12;
    }

    // Get time as hour.minute.second.millisecond (24 hour clock)
    public String getFullTime24(){
        int hourI = this.getHour24();
        this.fullTime24 = (new Integer(hourI)).toString();
        int minI = this.getMinute();
        if(minI<10){
            this.fullTime24 += ".0" + minI;
        }
        else{
            this.fullTime24 += "." + minI;
        }
        int secI = this.getSecond();
        if(secI<10){
            this.fullTime24 += ".0" + secI;
        }
        else{
            this.fullTime24 += "." + secI;
        }
        int msecI = this.getMilliSecond();
        if(msecI<10){
            this.fullTime24 += ".00" + msecI;
        }
        else{
            if(msecI<100){
                this.fullTime24 += ".0" + msecI;
            }
            else{
                this.fullTime24 += "." + msecI;
            }
        }
        return this.fullTime24;
    }

    // Get time as hour.minute.second.millisecond AM OR PM (12 hour clock)
    public String getFullTime12(){
        int hourI = cal.get(Calendar.HOUR);
        int amPm =  cal.get(Calendar.AM_PM);
        this.fullTime12 = (new Integer(hourI)).toString();
        int minI = this.getMinute();
        if(minI<10){
            this.fullTime12 += ".0" + minI;
        }
        else{
            this.fullTime12 += "." + minI;
        }
        int secI = this.getSecond();
        if(secI<10){
            this.fullTime12 += ".0" + secI;
        }
        else{
            this.fullTime12 += "." + secI;
        }
        int msecI = this.getMilliSecond();
        if(msecI<10){
            this.fullTime12 += ".00" + msecI;
        }
        else{
            if(msecI<100){
                this.fullTime12 += ".0" + msecI;
            }
            else{
                this.fullTime12 += "." + msecI;
            }
        }
        if(amPm==0){
            this.fullTime12 += " " + "AM";
        }
        else{
            this.fullTime12 += " " + "PM";
        }
        return this.fullTime12;
    }

    // Return the current computer time in milliseconds
     public long getComputerTime(){
        return System.currentTimeMillis();
    }

    // Converts a date to milliseconds since 0 hours 0 minutes 0 seconds on 1 Jan 1970
    public long dateToJavaMilliSecondsUK(int year, int month, int dayOfTheMonth, String dayOfTheWeek, int hour, int min, int sec, int millisec){

        long ms = 0L;  // milliseconds since  0 hours 0 minutes 0 seconds and o milliseconds on 1 Jan 1970

        // Day of the week as integer
        int dayIndicator = this.getDayOfTheWeekAsInteger(dayOfTheWeek);

        // British Summer Time adjustment
        long bst = 0;
        this.backForw = checkBST(dayOfTheWeek, dayOfTheMonth, hour, month, dayIndicator);
        if(this.backForw)bst = 1;

        // millisecond calculation
        if(year>=1970){
            // Date after the zero computer time
            long yearDiff = 0L;
            int yearTest = year-1;
            while(yearTest>=1970){
                yearDiff += 365;
                if(this.leapYear(yearTest))yearDiff++;
                yearTest--;
            }
            yearDiff *= 24L*60L*60L*1000L;

            long monthDiff = 0L;
            int monthTest = month - 1;
            while(monthTest>0){
                monthDiff += monthDays[monthTest-1];
                if(this.leapYear(year) && monthTest==2)monthDiff++;
                monthTest--;
            }
            monthDiff *= 24L*60L*60L*1000L;

            long dayDiff = (dayOfTheMonth - 1)*24L*60L*60L*1000L;

            ms = yearDiff + monthDiff + dayDiff + (hour - bst)*60L*60L*1000L + min*60L*1000L + sec*1000L + millisec;
        }
        else{
            // Date before the zero computer time
            long yearDiff = 0L;
            int yearTest = year + 1;
            while(yearTest<1970){
                yearDiff += 365;
                if(this.leapYear(yearTest))yearDiff++;
                yearTest++;
            }
            yearDiff *= 24L*60L*60L*1000L;

            long monthDiff = 0L;
            int monthTest = month - 1;
            while(monthTest>0){
                monthDiff += monthDays[monthTest-1];
                if(this.leapYear(year) && monthTest==2)monthDiff++;
                monthTest--;
            }

            monthDiff *= 24L*60L*60L*1000L;

            long dayDiff = (dayOfTheMonth - 1)*24L*60L*60L*1000L;

            monthDiff = monthDiff + dayDiff + (hour - bst)*60L*60L*1000L + min*60L*1000L + sec*1000L + millisec;

            long myear = 365L;
            if(this.leapYear(year))myear++;
            myear *= 24L*60L*60L*1000L;

            ms = myear - monthDiff;
            ms += yearDiff;
            ms = -ms;
        }

        return ms;
    }

    // Check whether within British summer time period
    public boolean checkBST(){

        String dayOfTheWeek = this.getDayOfTheWeek();
        int dayOfTheMonth = this.getDayOfTheMonth();
        int hour = this.getMonthAsInteger();
        int month = this.getMonthAsInteger();
        int dayIndicator = this.getDayOfTheWeekAsInteger(dayOfTheWeek);

        return this.checkBST(dayOfTheWeek, dayOfTheMonth, hour, month, dayIndicator);
    }

    // Check whether within British summer time period - private method for internal use
    private boolean checkBST(String dayOfTheWeek, int dayOfTheMonth, int hour, int month, int dayIndicator){

        if(month>3 && month<10){
            this.backForw = true;
        }
        else{
            if(month==3 && dayOfTheMonth>24){
                if(dayIndicator==0){
                    if(hour>=1)this.backForw = true;
                }
                else{
                    if(dayIndicator>0 && dayIndicator<dayOfTheMonth-24)this.backForw = true;
                }
            }
            else{
                if(month==10 && dayOfTheMonth>24){
                    if(dayIndicator==0){
                        if(hour<=2)this.backForw = true;
                    }
                    else{
                        this.backForw = true;
                        if(dayIndicator>0 && dayIndicator<dayOfTheMonth-24)this.backForw = false;
                    }
                }
            }
        }

        return this.backForw;
    }

    // Returns the day of the week as an integer (Sunday = 1, Monday = 1 etc)
    public int getDayOfTheWeekAsInteger(){

        String dayOfTheWeek = this.getDayOfTheWeek();

        return this.getDayOfTheWeekAsInteger(dayOfTheWeek) + 1;
    }

    // Returns the day of the week as an integer (Sunday = 0, Monday = 1 etc) - private method for internal use
    private int getDayOfTheWeekAsInteger(String dayOfTheWeek){


        // Day of the week as integer
        int counter = 0;
        int dayIndicator = 0;
        boolean test = true;
        while(test){
            if(dayOfTheWeek.equals(days[counter])){
                dayIndicator = counter;
                test = false;
            }
            else{
                counter++;
                if(counter>6)throw new IllegalArgumentException(dayOfTheWeek + " is not recognised as a day of the week");
            }
        }

        return dayIndicator;
    }

    // Calculates the next British Summer Time clock change for the current date
    public String nextBstClockChange(){

        this.backForw = true;    // = true change back, i.e if date is within bst; = false change = forward

        String dayOfTheWeek = this.getDayOfTheWeek();
        int dayOfTheMonth = this.getDayOfTheMonth();
        int hour = this.getMonthAsInteger();
        int month = this.getMonthAsInteger();

        // Day of the week as integer
        int dayIndicator = this.getDayOfTheWeekAsInteger(dayOfTheWeek);

        // Check whether within British summer time period
        this.backForw = checkBST(dayOfTheWeek, dayOfTheMonth, hour, month, dayIndicator);

        // Find next Sunday to today's date
        int daysDiff = 0;
        int newDayOfTheMonth = dayOfTheMonth;
        int newMonth = month;
        int newYear = year;
        int oldNewDayOfTheMonth = newDayOfTheMonth;
        int oldMonth = newMonth;
        int oldYear = newYear;
        if(dayIndicator!=0)daysDiff = 7-dayIndicator;
        newDayOfTheMonth = dayOfTheMonth + daysDiff;
        int monthD = monthDays[newMonth-1];
        if(newMonth==2 && this.leapYear(newYear))monthD++;
        if(newDayOfTheMonth>monthD){
            newDayOfTheMonth -= monthD;
            newMonth++;
            if(newMonth==13){
                newMonth = 1;
                newYear = oldYear + 1;
            }
        }

        if(!backForw){
            boolean test = true;
            while(test){
                if(newMonth==3 && newDayOfTheMonth>24){
                    this.changeDate = "Sunday, " + newDayOfTheMonth + " March " + year + ", one hour forward";
                    test = false;
                }
                else{
                    newDayOfTheMonth += 7;
                    monthD = monthDays[newMonth-1];
                    if(newMonth==2 && this.leapYear(newYear))monthD++;
                    if(newDayOfTheMonth>monthD){
                        newDayOfTheMonth -= monthD;
                        newMonth++;
                        if(newMonth==13){
                            newMonth = 1;
                            newYear = newYear + 1;
                        }
                    }
                }
            }
        }
        else{
            boolean test = true;
            while(test){
                if(newMonth==10 && newDayOfTheMonth>24){
                    this.changeDate = "Sunday, " + newDayOfTheMonth + " October " + year + ", one hour back";
                    test = false;
                }
                else{
                    newDayOfTheMonth += 7;
                    monthD = monthDays[newMonth-1];
                    if(newMonth==2 && this.leapYear(newYear))monthD++;
                    if(newDayOfTheMonth>monthD){
                        newDayOfTheMonth -= monthD;
                        newMonth++;
                        if(newMonth==13){
                            newMonth = 1;
                            newYear = newYear + 1;
                        }
                    }
                }
            }
        }
        return this.changeDate;
    }


    // Returns the the day of the week by name, e.g. Sunday
    public String getDayOfTheWeek(){
        int dayAsInt = cal.get(Calendar.DAY_OF_WEEK);
        this.dayOfTheWeek = this.days[dayAsInt - 1];
        return this.dayOfTheWeek;
    }

    // Returns the the day of the month as integer, e.g. 24 for the 24th day of the month
    public int getDayOfTheMonth(){
        this.dayOfTheMonth = cal.get(Calendar.DAY_OF_MONTH);
        return this.dayOfTheMonth;
    }

    // Returns the the month by name, e.g. January
    public String getMonth(){
        int monthAsInt = cal.get(Calendar.MONTH);
        this.monthOfTheYear = this.months[monthAsInt];
        return this.monthOfTheYear;
    }

    // Returns the month as an integer, e.g. January as 1
    public int getMonthAsInteger(){
        this.monthAsInteger = cal.get(Calendar.MONTH) + 1;
        return this.monthAsInteger;
    }

    // Returns the month as an integer, e.g. January as 1, private method for internal use
    public int getMonthAsInteger(String month){
        int monthI = 0;
        boolean test = true;
        int counter = 0;
        while(test){
            if(month.equals(this.months[counter])){
                monthI = counter + 1;
                test = false;
            }
            else{
                counter++;
                if(counter==12)throw new IllegalArgumentException(month + " is not recognised as a valid month name");
            }
        }
        return monthI;
    }

    // Returns the year as four digit number
    public int getYear(){
        this.year = cal.get(Calendar.YEAR);
        return this.year;
    }

    // Returns the date as 'day of month' 'month name' 'year'
    public String getDate(){
        this.date = (new Integer(this.getDayOfTheMonth())).toString();
        this.date += " " + this.getMonth();
        this.date += " " + this.getYear();
        return this.date;
    }

    // Returns the date as 'day name', 'day of month' 'month name' year
    public String getFullDate(){
        this.fullDate = this.getDayOfTheWeek();
        this.fullDate += ", " + this.getDayOfTheMonth();
        this.fullDate += " " + this.getMonth();
        this.fullDate += " " + this.getYear();
        return this.fullDate;
    }

    // Returns the date as the UK short format - 'day of month'.'month number'.'year final two digits'
    public String getShortDateUK(){
        this.shortDateUK = (new Integer(this.getDayOfTheMonth())).toString();
        if(this.shortDateUK.length()<2)this.shortDateUK = "0" + this.shortDateUK;
        int monthI = this.getMonthAsInteger();
        if(monthI<10){
            this.shortDateUK += ".0" + monthI;
        }
        else{
            this.shortDateUK += "." + monthI;
        }
        String yearS = (new Integer(this.getYear())).toString();
        this.shortDateUK += "." + yearS.substring(2);
        return this.shortDateUK;
    }

    // Returns the date as the US short format - 'month number'/'day of month'/'year final two digits'
     public String getShortDateUS(){
        this.shortDateUS = (new Integer(this.getMonthAsInteger())).toString();
        if(this.shortDateUS.length()<2)this.shortDateUS = "0" + this.shortDateUS;
        int dayI = this.getDayOfTheMonth();
        if(dayI<10){
            this.shortDateUS += "/0" + dayI;
        }
        else{
            this.shortDateUS += "/" + dayI;
        }
        String yearS = (new Integer(this.getYear())).toString();
        this.shortDateUS+= "/" + yearS.substring(2);
        return this.shortDateUS;
    }

    // Returns true if entered date (xxxT) is later than the current date (xxx) otherwise returns false, private method for internal use
    private boolean direction(int dayOfTheMonthT, int monthT, int yearT, int dayOfTheMonth, int month, int year){
        boolean test = true;
        boolean direction = false;
        if(year>yearT){
            direction = true;
        }
        else{
            if(year<yearT){
                direction = false;
            }
            else{
                if(month>monthT){
                    direction = true;
                }
                else{
                    if(month<monthT){
                        direction = false;
                    }
                    else{
                        if(dayOfTheMonth>=dayOfTheMonthT){
                            direction = true;
                        }
                        else{
                            direction = false;
                        }
                    }
                }
            }
        }
        return direction;
    }

    // Returns the day of the week for a given date - month as String, e.g. January
    public String getDayOfDate(int dayOfTheMonth, String month, int year){
        int monthI = this.getMonthAsInteger(month);
        return getDayOfDate(dayOfTheMonth, monthI, year);
    }


    // Returns the day of the week for a given date - month as integer, January = 1
    public String getDayOfDate(int dayOfTheMonth, int month, int year){

        String dayOfDate = null;
        int yearT = this.getYear();
        int monthT = this.getMonthAsInteger();
        int dayOfTheMonthT = this.getDayOfTheMonth();
        int dayI = this.getDayOfTheWeekAsInteger();
        int febOrRest = 0;


        boolean direction = direction(dayOfTheMonthT, monthT, yearT, dayOfTheMonth, month, year);

        if(direction){
            boolean test = true;
            while(test){
                 if(yearT==year && monthT==month && dayOfTheMonthT==dayOfTheMonth){
                    dayOfDate = days[dayI-1];
                    test = false;
                }
                else{
                    dayOfTheMonthT++;
                    febOrRest = this.monthDays[monthT-1];
                    if(this.leapYear(yearT) && monthT==2)febOrRest++;
                    if(dayOfTheMonthT>febOrRest){
                        dayOfTheMonthT -= febOrRest;
                        monthT++;
                    }
                    if(monthT==13){
                        monthT = 1;
                        yearT++;
                    }
                    dayI++;
                    if(dayI==8)dayI=1;
                }
            }
        }
        else{
            boolean test = true;
            while(test){
                if(yearT==year && monthT==month && dayOfTheMonthT==dayOfTheMonth){
                    dayOfDate = days[dayI-1];
                    test = false;
                }
                else{
                    dayOfTheMonthT--;
                    int monthIndex = monthT - 2;
                    if(monthIndex<0)monthIndex = 11;
                    febOrRest = this.monthDays[monthIndex];
                    if(this.leapYear(yearT) && monthT==3)febOrRest++;
                    if(dayOfTheMonthT==0){
                        dayOfTheMonthT = febOrRest;
                        monthT--;
                    }
                    if(monthT==0){
                        monthT = 12;
                        yearT--;
                    }
                    dayI--;
                    if(dayI==0)dayI=7;
                }
            }
        }


        return dayOfDate;
    }

    // Returns date of next Easter Sunday  (1700 - 2299)
    // Western Church   - Gregorian calendar
    // Uses the 'BBC algorithm' (http://www.bbc.co.uk/dna/h2g2/A653267) - checked only between 1700 and 2299
    public String easterSunday(){
        int year = this.getYear();
        if(year>2299)System.out.println(year + " is outside the range for which this algorithm has been checked, 1700 - 2299");
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();

        int rem1 = year%19;
        int quo1 = year/100;
        int rem2 = year%100;
        int quo2 = quo1/4;
        int rem3 = quo1%4;
        int quo3 = rem2/4;
        int rem4 = rem2%4;

        int quo4 = (quo1 + 8)/25;
        int quo5 = (quo1 - quo4 + 1)/3;
        int rem5 = (19*rem1 + quo1 - quo2 - quo5 + 15)%30;
        int rem6 = (32 + 2*rem3 + 2*quo3 - rem5 - rem4)%7;
        int quo6 = (rem1 + 11*rem5 + 22*rem6)/451;
        int sum1 = rem5 + rem6 - 7*quo6 + 114;

        this.easterMonth = sum1/31;
        this.easterDay = (sum1%31) + 1;

        boolean direction = this.direction(day, month, year, this.easterDay, this.easterMonth, year);
        if(direction){
            this.dayHold = this.easterDay;
            this.monthHold = this.easterMonth;
            this.yearHold = year;
            this.dayNameHold = "Sunday";
            this.easterDayName = this.getDayOfDate(this.easterDay, this.easterMonth, year);
            return  this.easterDayName + ", " + this.easterDay + " " + months[this.easterMonth-1] + " " + year;
        }
        else{
            return easterSunday(++year);
        }
    }

    // Returns date of the Easter Sunday  (1700 - 2299)
    // Western Church   - Gregorian calendar
    // Uses the 'BBC algorithm' (http://www.bbc.co.uk/dna/h2g2/A653267) - checked only between 1700 and 2299
    public String easterSunday(int year){
        if(year<1700 || year>2299)System.out.println(year + " is outside the range for which this algorithm has been checked, 1700 - 2299");

        int rem1 = year%19;
        int quo1 = year/100;
        int rem2 = year%100;
        int quo2 = quo1/4;
        int rem3 = quo1%4;
        int quo3 = rem2/4;
        int rem4 = rem2%4;

        int quo4 = (quo1 + 8)/25;
        int quo5 = (quo1 - quo4 + 1)/3;
        int rem5 = (19*rem1 + quo1 - quo2 - quo5 + 15)%30;
        int rem6 = (32 + 2*rem3 + 2*quo3 - rem5 - rem4)%7;
        int quo6 = (rem1 + 11*rem5 + 22*rem6)/451;
        int sum1 = rem5 + rem6 - 7*quo6 + 114;

        this.easterMonth = sum1/31;
        this.easterDay = (sum1%31) + 1;
        this.dayHold = this.easterDay;
        this.monthHold = this.easterMonth;
        this.yearHold = year;
        this.dayNameHold = "Sunday";

        this.easterDayName = this.getDayOfDate(this.easterDay, this.easterMonth, year);

        return  this.easterDayName + ", " + this.easterDay + " " + months[this.easterMonth-1] + " " + year;

    }

    // Returns date of next Good Friday
    // See easterDay() for limitations of the method
    public String goodFriday(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        this.easterSunday(year);
        int monthGF = this.easterMonth;
        int dayGF = this.easterDay - 2;
        if(dayGF<1){
            int dayCheck = monthDays[monthGF-2];
            if(this.leapYear(year) && monthGF==3)dayCheck++;
            dayGF = dayCheck + dayGF;
            monthGF--;
        }
        boolean direction = this.direction(day, month, year, dayGF, monthGF, year);
        if(!direction)year++;
        return goodFriday(year);
    }

    // Returns date of Good Friday for the entered year
    // See easterDay() for limitations of the method
    public String goodFriday(int year){
        this.easterSunday(year);
        int monthGF = this.easterMonth;
        int dayGF = this.easterDay - 2;
        if(dayGF<1){
            int dayCheck = monthDays[monthGF-2];
            if(this.leapYear(year) && monthGF==3)dayCheck++;
            dayGF = dayCheck + dayGF;
            monthGF--;
        }
        this.dayHold = dayGF;
        this.monthHold = monthGF;
        this.yearHold = year;
        this.dayNameHold = "Friday";
        return "Friday, " + dayGF + " " + months[monthGF-1] + " " + year;
    }

    // Returns date of next Maundy Thursday
    // See easterDay() for limitations of the method
    public String maundyThursday(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        this.maundyThursday(year);
        int monthMT = this.monthHold;
        int dayMT = this.dayHold;
        boolean direction = this.direction(day, month, year, dayMT, monthMT, year);
        if(!direction)year++;
        return maundyThursday(year);
    }

    // Returns date of Maundy Thursday for the entered year
    // See easterDay() for limitations of the method
    public String maundyThursday(int year){
        this.goodFriday(year);
        int monthMT = this.monthHold;
        int dayMT = this.dayHold - 1;
        if(dayMT<1){
            int dayCheck = monthDays[monthMT-2];
            if(this.leapYear(year) && monthMT==3)dayCheck++;
            dayMT = dayCheck + dayMT;
            monthMT--;
        }
        this.dayHold = dayMT;
        this.monthHold = monthMT;
        this.yearHold = year;
        this.dayNameHold = "Friday";
        return "Thursday, " + dayMT + " " + months[monthMT-1] + " " + year;
    }

    // Returns date of next Ash Wednesday
    // See easterDay() for limitations of the method
    public String ashWednesday(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        this.ashWednesday(year);
        int monthAW = this.monthHold;
        int dayAW = this.dayHold;
        boolean direction = this.direction(day, month, year, dayAW, monthAW, year);
        if(!direction)year++;
        return ashWednesday(year);
    }

    // Returns date of Ash Wednesday for the entered year
    // See easterDay() for limitations of the method
    public String ashWednesday(int year){
        this.easterSunday(year);
        int monthAW = this.easterMonth;
        int dayAW = this.easterDay;
        int counter = 1;
        while(counter<=40){
            dayAW --;
            if(dayAW<1){
                int dayCheck = monthDays[monthAW-2];
                if(this.leapYear(year) && monthAW==3)dayCheck++;
                dayAW = dayCheck + dayAW;
                monthAW--;
            }
            if(this.getDayOfDate(dayAW, monthAW, year).equals("Sunday")){
                // Sunday - day does not counts
            }
            else{
                // Not a Sunday - day counts
                counter++;
            }
        }
        this.dayHold = dayAW;
        this.monthHold = monthAW;
        this.yearHold = year;
        this.dayNameHold = "Wednesday";

        return "Wednesday, " + dayAW + " " + months[monthAW-1] + " " + year;
    }

    // Returns date of next Shrove Tuesday
    // See easterDay() for limitations of the method
    public String shroveTuesday(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        this.shroveTuesday(year);
        int monthST = this.monthHold;
        int dayST = this.dayHold;
        boolean direction = this.direction(day, month, year, dayST, monthST, year);
        if(!direction)year++;
        return shroveTuesday(year);
    }

    // Returns date of Shrove Tuesday for the entered year
    // See easterDay() for limitations of the method
    public String shroveTuesday(int year){
        this.ashWednesday(year);
        int monthST = this.monthHold;
        int dayST = this.dayHold - 1;
        if(dayST<1){
            int dayCheck = monthDays[monthST-2];
            if(this.leapYear(year) && monthST==3)dayCheck++;
            dayST = dayCheck + dayST;
            monthST--;
        }
        this.dayHold = dayST;
        this.monthHold = monthST;
        this.yearHold = year;
        this.dayNameHold = "Tuesday";

        return "Tuesday, " + dayST + " " + months[monthST-1] + " " + year;
    }

    // Returns date of next Palm Sunday
    // See easterDay() for limitations of the method
    public String palmSunday(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        this.palmSunday(year);
        int monthPS = this.monthHold;
        int dayPS = this.dayHold;
        boolean direction = this.direction(day, month, year, dayPS, monthPS, year);
        if(!direction)year++;
        return palmSunday(year);
    }

    // Returns date of Palm Sunday for the entered year
    // See easterDay() for limitations of the method
    public String palmSunday(int year){
        this.easterSunday(year);
        int monthPS = this.easterMonth;
        int dayPS = this.easterDay - 7;
        if(dayPS<1){
            int dayCheck = monthDays[monthPS-2];
            if(this.leapYear(year) && monthPS==3)dayCheck++;
            dayPS = dayCheck + dayPS;
            monthPS--;
        }
        this.dayHold = dayPS;
        this.monthHold = monthPS;
        this.yearHold = year;
        this.dayNameHold = "Sunday";

        return "Sunday, " + dayPS  + " " + months[monthPS-1] + " " + year;
    }

    // Returns date of next Advent Sunday
    // See easterDay() for limitations of the method
    public String adventSunday(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        this.adventSunday(year);
        int monthPS = this.monthHold;
        int dayPS = this.dayHold;
        boolean direction = this.direction(day, month, year, dayPS, monthPS, year);
        if(!direction)year++;
        return adventSunday(year);
    }

    // Returns date of Advent Sunday for the entered year
    // See easterDay() for limitations of the method
    public String adventSunday(int year){
        this.saintAndrewsDay(year);
        int monthAS = this.monthHold;
        int dayAS = this.dayHold;
        String dayNameAS = this.dayNameHold;
        int dayASI = this.getDayOfTheWeekAsInteger(dayNameAS);
        if(dayASI<4){
            dayAS -= dayASI;
            if(dayAS<1){
                int dayCheck = monthDays[monthAS-2];
                if(this.leapYear(year) && monthAS==3)dayCheck++;
                dayAS = dayCheck + dayAS;
                monthAS--;
            }
        }
        else{
            dayAS += (7 - dayASI);
            int dayCheck = monthDays[monthAS-1];
            if(this.leapYear(year) && monthAS==2)dayCheck++;
            if(dayAS>dayCheck){
                dayAS = dayAS - dayCheck;
                monthAS++;
            }
        }

        this.dayHold = dayAS;
        this.monthHold = monthAS;
        this.yearHold = year;
        this.dayNameHold = "Sunday";

        return "Sunday, " + dayAS  + " " + months[monthAS-1] + " " + year;
    }


    // Returns date of next Trinity Sunday
    // See easterDay() for limitations of the method
    public String trinitySunday(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        this.trinitySunday(year);
        int monthTS = this.monthHold;
        int dayTS = this.dayHold;
        boolean direction = this.direction(day, month, year, dayTS, monthTS, year);
        if(!direction)year++;
        return trinitySunday(year);
    }

    // Returns date of Trinity Sunday for the entered year
    // See easterDay() for limitations of the method
    public String trinitySunday(int year){
        this.whitSunday(year);
        int monthTS = this.monthHold;
        int dayTS = this.dayHold + 7;
        int dayCheck = monthDays[monthTS-1];
        if(this.leapYear(year) && monthTS==2)dayCheck++;
        if(dayTS>dayCheck){
            dayTS = dayTS - dayCheck;
            monthTS++;
        }
        this.dayHold = dayTS;
        this.monthHold = monthTS;
        this.yearHold = year;
        this.dayNameHold = "Sunday";

        return "Sunday, " + dayTS  + " " + months[monthTS-1] + " " + year;
    }

    // Returns date of next Corpus Christi
    // See easterDay() for limitations of the method
    public String corpusChristi(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        this.corpusChristi(year);
        int monthTS = this.monthHold;
        int dayTS = this.dayHold;
        boolean direction = this.direction(day, month, year, dayTS, monthTS, year);
        if(!direction)year++;
        return corpusChristi(year);
    }

    // Returns date of Corpus Christi for the entered year
    // See easterDay() for limitations of the method
    public String corpusChristi(int year){
        this.trinitySunday(year);
        int monthTS = this.monthHold;
        int dayTS = this.dayHold + 4;
        int dayCheck = monthDays[monthTS-1];
        if(this.leapYear(year) && monthTS==2)dayCheck++;
        if(dayTS>dayCheck){
            dayTS = dayTS - dayCheck;
            monthTS++;
        }
        this.dayHold = dayTS;
        this.monthHold = monthTS;
        this.yearHold = year;
        this.dayNameHold = "Thursday";

        return "Thursday, " + dayTS  + " " + months[monthTS-1] + " " + year;
    }

    // Returns date of next Sunday after Corpus Christi
    // See easterDay() for limitations of the method
    public String sundayAfterCorpusChristi(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        this.sundayAfterCorpusChristi(year);
        int monthTS = this.monthHold;
        int dayTS = this.dayHold;
        boolean direction = this.direction(day, month, year, dayTS, monthTS, year);
        if(!direction)year++;
        return sundayAfterCorpusChristi(year);
    }

    // Returns date of Sunday after Corpus Christi for the entered year
    // See easterDay() for limitations of the method
    public String sundayAfterCorpusChristi(int year){
        this.corpusChristi(year);
        int monthTS = this.monthHold;
        int dayTS = this.dayHold + 3;
        int dayCheck = monthDays[monthTS-1];
        if(this.leapYear(year) && monthTS==2)dayCheck++;
        if(dayTS>dayCheck){
            dayTS = dayTS - dayCheck;
            monthTS++;
        }
        this.dayHold = dayTS;
        this.monthHold = monthTS;
        this.yearHold = year;
        this.dayNameHold = "Sunday";

        return "Sunday, " + dayTS  + " " + months[monthTS-1] + " " + year;
    }

    // Returns date of next Ascension Thursday
    // See easterDay() for limitations of the method
    public String ascensionThursday(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        this.ascensionThursday(year);
        int monthAT = this.monthHold;
        int dayAT = this.dayHold;
        boolean direction = this.direction(day, month, year, dayAT, monthAT, year);
        if(!direction)year++;
        return ascensionThursday(year);
    }

    // Returns date of Ascension Thursday for the entered year
    // See easterDay() for limitations of the method
    public String ascensionThursday(int year){
        this.easterSunday(year);
        int monthAT = this.easterMonth;
        int dayAT = this.easterDay + 39;
        int dayCheck1 = monthDays[monthAT-1];
        if(this.leapYear(year) && monthAT==2)dayCheck1++;
        int dayCheck2 = monthDays[monthAT];
        if(this.leapYear(year) && monthAT==1)dayCheck2++;
        if(dayAT>(dayCheck1 + dayCheck2)){
            dayAT = dayAT - (dayCheck1 + dayCheck2);
            monthAT += 2;
        }
        else{
            if(dayAT>dayCheck1){
                dayAT = dayAT - dayCheck1;
                monthAT += 1;
            }
        }

        this.dayHold = dayAT;
        this.monthHold = monthAT;
        this.yearHold = year;
        this.dayNameHold = "Thursday";

        return "Thursday, " + dayAT  + " " + months[monthAT-1] + " " + year;
    }

    // Returns date of next Sunday after Ascension Thursday
    // See easterDay() for limitations of the method
    public String sundayAfterAscension(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        this.sundayAfterAscension(year);
        int monthAT = this.monthHold;
        int dayAT = this.dayHold;
        boolean direction = this.direction(day, month, year, dayAT, monthAT, year);
        if(!direction)year++;
        return sundayAfterAscension(year);
    }

    // Returns date of Sunday after Ascension Thursday for the entered year
    // See easterDay() for limitations of the method
    public String sundayAfterAscension(int year){
        this.ascensionThursday(year);
        int monthAT = this.monthHold;
        int dayAT = this.dayHold + 3;
        int dayCheck1 = monthDays[monthAT-1];
        if(this.leapYear(year) && monthAT==2)dayCheck1++;
        if(dayAT>dayCheck1){
            dayAT = dayAT - dayCheck1;
            monthAT += 1;
        }

        this.dayHold = dayAT;
        this.monthHold = monthAT;
        this.yearHold = year;
        this.dayNameHold = "Sunday";

        return "Sunday, " + dayAT  + " " + months[monthAT-1] + " " + year;
    }

    // Returns date of next Whit Sunday (Pentecost)
    // See easterDay() for limitations of the method
    public String whitSunday(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        this.whitSunday(year);
        int monthWS = this.monthHold;
        int dayWS = this.dayHold;
        boolean direction = this.direction(day, month, year, dayWS, monthWS, year);
        if(!direction)year++;
        return whitSunday(year);
    }

    // Returns date of Whit Sunday (Pentecost)for the entered year
    // See easterDay() for limitations of the method
    public String whitSunday(int year){
        this.easterSunday(year);
        int dayWS = this.easterDay + 49;
        int monthWS = this.easterMonth;
        int dayCheck1 = monthDays[monthWS-1];
        if(this.leapYear(year) && monthWS==2)dayCheck1++;
        int dayCheck2 = monthDays[monthWS];
        if(this.leapYear(year) && monthWS==1)dayCheck2++;

        if(dayWS>(dayCheck1+dayCheck2)){
            dayWS -= (dayCheck1 + dayCheck2) ;
            monthWS += 2;
        }
        else{
            if(dayWS>dayCheck1){
                dayWS -= dayCheck1 ;
                monthWS += 1;
            }
        }
        this.dayHold = dayWS;
        this.monthHold = monthWS;
        this.yearHold = year;
        this.dayNameHold = "Sunday";

        return "Sunday, " + dayWS + " " + months[this.monthHold-1] + " " + year;
    }

    // Returns date of next Mother's Day (Mothering Sunday) in the UK
    // See easterDay() for limitations of the method
    public String mothersDayUK(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        this.mothersDayUK(year);
        int monthMS = this.monthHold;
        int dayMS = this.dayHold;
        boolean direction = this.direction(day, month, year, dayMS, monthMS, year);
        if(!direction)year++;
        return mothersDayUK(year);
    }

    // Returns date of Mother's Day (Mothering Sunday) in the UK for the entered year
    // See easterDay() for limitations of the method
    public String mothersDayUK(int year){
        this.ashWednesday(year);
        int dayMS = this.dayHold + 25;
        int monthMS = this.monthHold;
        int dayCheck = monthDays[monthMS-1];
        if(this.leapYear(year) && monthMS==2)dayCheck++;
        if(dayMS>dayCheck){
            dayMS -= dayCheck;
            monthMS++;
        }
        this.dayHold = dayMS;
        this.monthHold = monthMS;
        this.yearHold = year;
        this.dayNameHold = "Sunday";

        return "Sunday, " + dayMS + " " + months[this.monthHold-1] + " " + year;
    }

    // Returns date of next Mothers Day in the US
    public String mothersDayUS(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        this.mothersDayUS(year);
        boolean direction = this.direction(day, month, year, this.dayHold, this.monthHold, year);
        if(!direction)year++;
        return mothersDayUS(year);
    }

    // Returns date of Mother's Day (Mothering Sunday) in the US for the entered year
    public String mothersDayUS(int year){
        String dayMSN = this.getDayOfDate(1, "May", year);
        int monthMS = 5;
        int dayOwI = this.getDayOfTheWeekAsInteger(dayMSN) + 1;
        int dayMS = 0;
        if(dayOwI==1){
            dayMS = dayOwI + 7;
        }
        else{
            dayMS = 16 - dayOwI;
        }
        this.dayHold = dayMS;
        this.monthHold = monthMS;
        this.yearHold = year;
        this.dayNameHold = "Sunday";

        return "Sunday, " + dayMS + " May " + year;
    }

    // Returns date of next Father's Day
    public String fathersDay(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        this.fathersDay(year);
        boolean direction = this.direction(day, month, year, this.dayHold, this.monthHold, year);
        if(!direction)year++;
        return fathersDay(year);
    }

    // Returns date of Father's Day  for the entered year
    public String fathersDay(int year){
        String dayMSN = this.getDayOfDate(1, "June", year);
        int monthFD = 6;
        int dayOwI = this.getDayOfTheWeekAsInteger(dayMSN) + 1;
        int dayFD = 0;
        if(dayOwI==1){
            dayFD = dayOwI + 14;
        }
        else{
            dayFD = 23 - dayOwI;
        }
        this.dayHold = dayFD;
        this.monthHold = monthFD;
        this.yearHold = year;
        this.dayNameHold = "Sunday";

        return "Sunday, " + dayFD + " June " + year;
    }

    // Returns date of next Christmas Day
    public String christmasDay(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 25, 12, year);
        if(!direction)year++;
        return christmasDay(year);
    }

    // Returns date of Christmas Day for the entered year
    public String christmasDay(int year){
        String day = this.getDayOfDate(25, 12, year);
        this.dayHold = 25;
        this.monthHold = 12;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 25 December " + year;
    }

    // Returns date of next New Year's Day
    public String newYearsDay(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 1, 1, year);
        if(!direction)year++;
        return newYearsDay(year);
    }

    // Returns date of New Year's Day for the entered year
    public String newYearsDay(int year){
        String day = this.getDayOfDate(1, 1, year);
        this.dayHold = 1;
        this.monthHold = 1;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 1 January " + year;
    }

    // Returns date of next Epiphany day
    public String epiphany(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 6, 1, year);
        if(!direction)year++;
        return epiphany(year);
    }

    // Returns date of Epiphany day for the entered year
    public String epiphany(int year){
        String day = this.getDayOfDate(6, 1, year);
        this.dayHold = 6;
        this.monthHold = 1;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 6 January " + year;
    }

    // Returns date of next Sunday after Epiphany day
    public String sundayAfterEpiphany(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        String dayName = this.getDayOfDate(6, 1, year);
        int dayI = this.getDayOfTheWeekAsInteger(dayName);
        int day6plus = 6;
        if(dayI>0)day6plus += (7 - dayI);
        boolean direction = this.direction(day, month, year, day6plus, 1, year);
        if(!direction)year++;
        return sundayAfterEpiphany(year);
    }

    // Returns date of Sunday after Epiphany day for the entered year
    public String sundayAfterEpiphany(int year){
        String dayName = this.getDayOfDate(6, 1, year);
        int dayI = this.getDayOfTheWeekAsInteger(dayName);
        int day6plus = 6;
        if(dayI>0)day6plus += (7 - dayI);
        this.dayHold = day6plus;
        this.monthHold = 1;
        this.yearHold = year;
        this.dayNameHold = "Sunday";

        return "Sunday, " + day6plus  + " January " + year;
    }

    // Returns date of the next Feast of the Annunciation
    public String annunciation(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 25, 3, year);
        if(!direction)year++;
        return annunciation(year);
    }
    // Returns date of the Feast of the Annunciation for the entered year
    public String annunciation(int year){
        String day = this.getDayOfDate(25, 3, year);
        this.dayHold = 25;
        this.monthHold = 3;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 25 March " + year;
    }

    // Returns date of the next Feast of the Assumption
    public String assumption(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 15, 8, year);
        if(!direction)year++;
        return assumption(year);
    }

    // Returns date of the Feast of the Assumption for the entered year
    public String assumption(int year){
        String day = this.getDayOfDate(15, 8, year);
        this.dayHold = 15;
        this.monthHold = 8;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 15 August " + year;
    }

    // Returns date of the next Feast of the Nativity of the Blessed Virgin
    public String nativityBlessedVirgin(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 8, 9, year);
        if(!direction)year++;
        return nativityBlessedVirgin(year);
    }

    // Returns date of the Feast of the Nativity of the Blessed Virgin for the entered year
    public String nativityBlessedVirgin(int year){
        String day = this.getDayOfDate(8, 9, year);
        this.dayHold = 8;
        this.monthHold = 9;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 8 September " + year;
    }

    // Returns date of the next Feast of the Immaculate Conception
    public String immaculateConception(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 8, 12, year);
        if(!direction)year++;
        return immaculateConception(year);
    }

    // Returns date of the Feast of the Immaculate Conception for the entered year
    public String immaculateConception(int year){
        String day = this.getDayOfDate(8, 12, year);
        this.dayHold = 8;
        this.monthHold = 12;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 8 December " + year;
    }

    // Returns date of the next Feast of the Purification of the Virgin
    // [Candlemas, Feast of the Presentation of Jesus at the Temple]
    public String purification(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 2, 2, year);
        if(!direction)year++;
        return purification(year);
    }

    public String presentation(){
        return this.purification();
    }

    public String candlemas(){
        return this.purification();
    }

    // Returns date of the next Feast of the Purification of the Virgin
    // [Candlemas, Feast of the Presentation of Jesus at the temple]
    public String purification(int year){
        String day = this.getDayOfDate(2, 2, year);
        this.dayHold = 2;
        this.monthHold = 2;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 2 February " + year;
    }

    public String presentation(int year){
        return this.purification(year);
    }

    public String candlemas(int year){
        return this.purification(year);
    }

    // Returns date of the next Feast of the Transfiguration of Christ
    public String transfiguration(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 6, 8, year);
        if(!direction)year++;
        return transfiguration(year);
    }

    // Returns date of the Feast of the Transfiguration of Christ for the entered year
    public String transfiguration(int year){
        String day = this.getDayOfDate(6, 8, year);
        this.dayHold = 6;
        this.monthHold = 8;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 6 August " + year;
    }

    // Returns date of next Remembrance Sunday
    public String remembranceSunday(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        this.remembranceSunday(year);
        int monthRS = this.monthHold;
        int dayRS = this.dayHold;
        boolean direction = this.direction(day, month, year, dayRS, monthRS, year);
        if(!direction)year++;
        return remembranceSunday(year);
    }

    // Returns date of Remembrance Sunday for the entered year
    public String remembranceSunday(int year){
        int monthRS = 11;
        int dayRS = 11;
        String dayNameRS = this.getDayOfDate(11, 11, year);
        int dayRSI = this.getDayOfTheWeekAsInteger(dayNameRS);
        if(dayRSI<4){
            dayRS -= dayRSI;
            if(dayRS<1){
                int dayCheck = monthDays[monthRS-2];
                if(this.leapYear(year) && monthRS==3)dayCheck++;
                dayRS = dayCheck + dayRS;
                monthRS--;
            }
        }
        else{
            dayRS += (7 - dayRSI);
            int dayCheck = monthDays[monthRS-1];
            if(this.leapYear(year) && monthRS==2)dayCheck++;
            if(dayRS>dayCheck){
                dayRS = dayRS - dayCheck;
                monthRS++;
            }
        }

        this.dayHold = dayRS;
        this.monthHold = monthRS;
        this.yearHold = year;
        this.dayNameHold = "Sunday";

        return "Sunday, " + dayRS  + " " + months[monthRS-1] + " " + year;
    }


    // Returns date of next Holocaust Memorial Day
    public String holocaustMemorialDay(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 27, 1, year);
        if(!direction)year++;
        return holocaustMemorialDay(year);
    }

    // Returns date of Holocaust Memorial Day for the entered year
    public String holocaustMemorialDay(int year){
        String day = this.getDayOfDate(27, 1, year);
        this.dayHold = 25;
        this.monthHold = 12;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 27 January " + year;
    }

    // Returns date of next St Patrick's Day
    public String saintPatricksDay(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 17, 3, year);
        if(!direction)year++;
        return saintPatricksDay(year);
    }

    // Returns date of St Patrick's Day for the entered year
    public String saintPatricksDay(int year){
        String day = this.getDayOfDate(17, 3, year);
        this.dayHold = 17;
        this.monthHold = 3;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 17 March " + year;
    }

    // Returns date of next St Brigid's Day
    public String saintBrigidsDay(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 1, 2, year);
        if(!direction)year++;
        return saintBrigidsDay(year);
    }

    // Returns date of St Brigid's Day for the entered year
    public String saintBrigidsDay(int year){
        String day = this.getDayOfDate(1, 2, year);
        this.dayHold = 1;
        this.monthHold = 2;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 1 February " + year;
    }

    // Returns date of next St Colm Cille's (St Columba's)Day
    public String saintColmCillesDay(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 9, 6, year);
        if(!direction)year++;
        return saintColmCillesDay(year);
    }

    public String saintColumbasDay(){
        return this.saintColmCillesDay();
    }

    public String saintColmcillesDay(){
        return this.saintColmCillesDay();
    }

    // Returns date of St Colm Cille's (St Columba's) Day for the entered year
    public String saintColmCillesDay(int year){
        String day = this.getDayOfDate(9, 6, year);
        this.dayHold = 9;
        this.monthHold = 6;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 9 June " + year;
    }

    public String saintColumbasDay(int year){
        return this.saintColmCillesDay(year);
    }

    public String saintColmcillesDay(int year){
        return this.saintColmCillesDay(year);
    }

    // Returns date of next St Georges's day
    public String saintGeorgesDay(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 23, 4, year);
        if(!direction)year++;
        return saintGeorgesDay(year);
    }

    // Returns date of St George's day for the entered year
    public String saintGeorgesDay(int year){
        String day = this.getDayOfDate(23, 4, year);
        this.dayHold = 23;
        this.monthHold = 4;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 23 April " + year;
    }

    // Returns date of next St Andrew's day
    public String saintAndrewsDay(){int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 30, 11, year);
        if(!direction)year++;
        return saintAndrewsDay(year);
    }

    // Returns date of St Andrew's day for the entered year
    public String saintAndrewsDay(int year){
        String day = this.getDayOfDate(30, 11, year);
        this.dayHold = 30;
        this.monthHold = 11;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 30 November " + year;
    }

    // Returns date of next St David's day
    public String saintDavidsDay(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 1, 3, year);
        if(!direction)year++;
        return saintDavidsDay(year);
    }

    // Returns date of St David's day for the entered year
    public String saintDavidsDay(int year){
        String day = this.getDayOfDate(1, 3, year);
        this.dayHold = 1;
        this.monthHold = 3;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 1 March " + year;
    }

    // Returns date of next St Stephen's day
    public String saintStephensDay(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 26, 12, year);
        if(!direction)year++;
        return saintStephensDay(year);
    }

    // Returns date of St Stephen's day for the entered year
    public String saintStephensDay(int year){
        String day = this.getDayOfDate(26, 12, year);
        this.dayHold = 26;
        this.monthHold = 12;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 26 December " + year;
    }

    // Returns date of next St Valentine's day
    public String saintValentinesDay(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 14, 2, year);
        if(!direction)year++;
        return saintValentinesDay(year);
    }

    // Returns date of St Valentines's day for the entered year
    public String saintValentinesDay(int year){
        String day = this.getDayOfDate(14, 2, year);
        this.dayHold = 14;
        this.monthHold = 2;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 14 February " + year;
    }


    // Returns date of next Burns' night
    public String burnsNight(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 25, 1, year);
        if(!direction)year++;
        return burnsNight(year);
    }

    // Returns date of Burns night for the entered year
    public String burnsNight(int year){
        String day = this.getDayOfDate(25, 1, year);
        this.dayHold = 25;
        this.monthHold = 1;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 25 January " + year;
    }

    // Returns date of next Twelfth of July
    public String twelfthJuly(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 12, 7, year);
        if(!direction)year++;
        return twelfthJuly(year);
    }

    // Returns date of the Twelfth of July for the entered year
    public String twelfthJuly(int year){
        String day = this.getDayOfDate(12, 7, year);
        this.dayHold = 12;
        this.monthHold = 7;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 12 July " + year;
    }

    // Returns date of next Fourth of July (US Independence Day)
    public String fourthJuly(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 12, 7, year);
        if(!direction)year++;
        return fourthJuly(year);
    }

    // Returns date of the Fourth of July (US Independence Day)for the entered year
    public String fourthJuly(int year){
        String day = this.getDayOfDate(4, 7, year);
        this.dayHold = 4;
        this.monthHold = 7;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 4 July " + year;
    }

    // Returns date of next US Thanksgiving Day
    public String thanksgivingDay(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();

        String day1 = this.getDayOfDate(1, "November", year);
        int day1I = this.getDayOfTheWeekAsInteger(day1) + 1;
        int day2I = 6 - day1I;
        if(day2I<=0)day2I += 7;
        day2I += 14;
        boolean direction = this.direction(day, month, year, day2I, 11, year);
        if(direction){
            return "Thursday, " + day2I + " November " + year;
        }
        else{
            return thanksgivingDay(++year);
        }
    }

    // Returns date of the US Thanksgiving Day
    public String thanksgivingDay(int year){
        String day1 = this.getDayOfDate(1, "November", year);
        int day1I = this.getDayOfTheWeekAsInteger(day1) + 1;
        int day2I = 6 - day1I;
        if(day2I<=0)day2I += 7;
        day2I += 14;
        this.dayHold = day2I;
        this.monthHold = 11;
        this.yearHold = year;
        this.dayNameHold = "Thursday";
        return "Thursday, " + day2I + " November " + year;
    }

    // Returns date of next Commonwealth Day
    public String commonwealthDay(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();

        String day1 = this.getDayOfDate(1, "March", year);
        int day1I = this.getDayOfTheWeekAsInteger(day1);

        int day2I = 0;
        if(day1I>1){
            day2I = 15 - day1I;
        }
        else{
            if(day1I==0){
                day2I = 8;
            }
            else{
                day2I = 9;
            }
        }
        boolean direction = this.direction(day, month, year, day2I, 3, year);
        if(direction){
            this.dayHold = day2I;
            this.monthHold = 3;
            this.yearHold = year;
            this.dayNameHold = "Monday";
            return "Monday, " + day2I + " November " + year;
        }
        else{
            return commonwealthDay(++year);
        }
    }

    // Returns date of the Commonwealth Day
    public String commonwealthDay(int year){
        String day1 = this.getDayOfDate(1, "March", year);
        int day1I = this.getDayOfTheWeekAsInteger(day1);

        int day2I = 0;
        if(day1I>1){
            day2I = 16 - day1I;
        }
        else{
            if(day1I==0){
                day2I = 9;
            }
            else{
                day2I = 8;
            }
        }
        this.dayHold = day2I;
        this.monthHold = 3;
        this.yearHold = year;
        this.dayNameHold = "Monday";
        return "Monday, " + day2I + " March " + year;
    }

    // Returns date of next Armed Forces Day (UK Veterans' Day)
    public String armedForcesDay(){
        int year = this.getYear();
        int month = this.getMonthAsInteger();
        int day = this.getDayOfTheMonth();
        boolean direction = this.direction(day, month, year, 27, 6, year);
        if(!direction)year++;
        return armedForcesDay(year);
    }

    public String veteransDayUK(){
        return this.armedForcesDay();
    }

    // Returns date of the Armed Forces Day (UK Veterans' Day)for the entered year
    public String armedForcesDay(int year){
        String day = this.getDayOfDate(27, 6, year);
        this.dayHold = 27;
        this.monthHold = 6;
        this.yearHold = year;
        this.dayNameHold = day;
        return day + ", 27 June " + year;
    }

    public String veteransDayUK(int year){
        return this.armedForcesDay(year);
    }

    // Returns true if year (argument) is a leap year
    public boolean leapYear(int year){
            boolean test = false;

            if(year%4 != 0){
                 test = false;
            }
            else{
                if(year%400 == 0){
                    test=true;
                }
                else{
                    if(year%100 == 0){
                        test=false;
                    }
                    else{
                        test=true;
                    }
                }
            }
            return test;
    }
}

