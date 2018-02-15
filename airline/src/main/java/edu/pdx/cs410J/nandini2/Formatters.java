package edu.pdx.cs410J.nandini2;

import edu.pdx.cs410J.AirportNames;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.*;

import static java.lang.Math.ceil;

class Formatters {
    /**
     * Returns <code>true</code> if the time argument is a properly formatted valid representation of time.
     * @param time  String to be analyzed.
     * @return boolean
     */
    static boolean checkWhetherTheStringIsAValidTime(String time){
        String [] splitTime = time.split(":");
        if(splitTime[1].length()<2 || splitTime.length!=3 ){
            error("Invalid time format: ", "", " expected hh:mm aa");
        }
        int hour = 0, minute = 0;
        String dayHalf = splitTime[2];
        try{
            hour = Integer.parseInt(splitTime[0]);
            minute = Integer.parseInt(splitTime[1]);
        } catch (NumberFormatException|ArrayIndexOutOfBoundsException e2) {
            error("Invalid time format: ", "", " expected hh:mm aa");
        }
        List<String> dayHalves = Arrays.asList("am", "pm");
        if(hour<1 || hour>12 || minute<0 || minute>59 || !dayHalves.contains(dayHalf)) {
            error("Invalid time format: ", "", " expected hh:mm aa");
        }
        return true;
    }

    /**
     * Returns <code>true</code> if the date argument is a properly formatted valid representation of date.
     * @param date  String to be analyzed.
     * @return boolean
     */
    static boolean checkWhetherTheStringIsAValidDate(String date) {
        String[] splitDate = date.split("/");
        if(splitDate.length>3 || splitDate[2].length()!=4 || date.length()<8 || date.length()>10){
            error("Invalid date format: ", "", " expected MM/dd/yyyy");
        }
        int month = 0, day = 0, year = 0;
        try{
            month = Integer.parseInt(splitDate[0]);
            day = Integer.parseInt(splitDate[1]);
            year = Integer.parseInt(splitDate[2]);
        } catch(NumberFormatException|ArrayIndexOutOfBoundsException e2) {
            error("Invalid date format: ", "", " expected MM/dd/yyyy");
        }
        List<Integer> monthsWith30Days = Arrays.asList(4, 6, 9, 11);
        if(month<1 || month>12 || day<1 || day>31 ||  (month==2 && day>29)
                || (month==2 && day==29 && year%4!=0) || (monthsWith30Days.contains(month) && day>30)) {
            error("Invalid date format: ", "", " expected MM/dd/yyyy");
        }
        return true;
    }

    /**
     * Checks whether the given string argument is a valid airport code.
     * @param airportCode String to be analyzed
     * @return            Airport code(if a valid code) converted to uppercase.
     */
    static String checkWhetherTheStringIsAValidAirportCode(String airportCode) {
        if (airportCode == null) {
            return null;
        }
        if(airportCode.length()!=3 || airportCode.matches(".*[^a-zA-Z].*")){
            error("Invalid argument for airport code: ", "", "must be the three-letter code of a known airport.");
        }
        return airportCode.toUpperCase();
    }

    /**
     * Checks whether the given string is a positive integer.
     * @param number String to be analyzed
     * @return       Number(if a valid number) converted to <code>int</code>
     */
    static int checkWhetherTheStringIsAPositiveNumber(String number) {
        int num = 0;
        try{
            num = Integer.parseInt(number);
        } catch(NumberFormatException e1) {
            error("Invalid argument type for Flight Number: ", "", "must be a positive Integer");
        }
        if (num<0){
            error("Invalid argument type for Flight Number: ", "", "must be a positive Integer");
        }
        return num;
    }

    /**
     * Writes the given error message and exits.
     * @param message      Error message
     * @param errorSource  Cause for error
     * @param expected     Expected behavior
     */
    static void error(String message, String errorSource, String expected) {
        PrintStream err = System.err;
        err.println("** " + message + errorSource + expected);
        System.exit(1);
    }

    /**
     * Sorts an airline's flight list.
     * @param  allFlights List of flights to be sorted
     * @return            Sorted flight list
     */
    static Collection<Flight> sort(Collection<Flight> allFlights) {
        Collections.sort((List) allFlights, new Comparator<Flight>() {
            @Override
            public int compare(Flight o1, Flight o2) {
                return o1.compareTo(o2);
            }
        });
        return allFlights;
    }

    /**
     * Returns a string containing flight list in a pretty format.
     * @param allFlights    List of flights to be pretty printed.
     * @return              String containing list of flights in pretty format
     */
    static String prettyPrint(Collection<Flight> allFlights) {
        allFlights = sort(allFlights);
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        StringBuilder sb = new StringBuilder();
        int i=0;
        while (true){
            Flight flight = (Flight) allFlights.toArray()[i];
            double duration = ceil((flight.getArrival().getTime() - flight.getDeparture().getTime())/60000);
            sb.append("Flight "+flight.getNumber() + " from " + AirportNames.getName(flight.getSource()) + " departs on " + dateFormat.format(flight.getDeparture()) + " and arrives at " + AirportNames.getName(flight.getDestination()) + " on " + dateFormat.format(flight.getArrival()) + ". The duration of the flight is " + duration + " minutes.\n");
            i++;
            if (i==allFlights.size()){
                break;
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}
