package edu.pdx.cs410J.nandini2;

import edu.pdx.cs410J.AirportNames;

import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static edu.pdx.cs410J.nandini2.Formatters.*;

/**
 * The main class that parses the command line and communicates with the
 * Airline server using REST.
 */
public class Project4 {

    public static final String MISSING_ARGS = "Missing command line arguments";

    public static void main(String[] args) {
        String hostName = null;
        String portString = null;
        List<String> searchKeyList = null;
        String name = null;
        String flightNumber = null;
        String src = null;
        String departDate = null;
        String departTime = null;
        String departTimeDayHalf = null;
        String dest = null;
        String arriveDate = null;
        String arriveTime = null;
        String arriveTimeDayHalf = null;

        boolean printFlight = false;
        boolean flightSearch = false;
        int noOfOptions = 0;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-README")) {
                printREADME();
                System.exit(0);
            } else if (args[i].equals("-print")) {
                printFlight = true;
                noOfOptions++;
            } else if (args[i].equals("-host")) {
                try {
                    if (!args[i+1].contains("-")) {
                        hostName = args[++i];
                        noOfOptions += 2;
                    } else {
                        noOfOptions += 1;
                    }
                } catch (Exception e) {
                    usage("Missing host");
                }

            } else if (args[i].equals("-port")) {
                try {
                    if (!args[i+1].contains("-")) {
                        portString = args[++i];
                        noOfOptions += 2;
                    } else {
                        noOfOptions += 1;
                    }
                } catch (Exception e) {
                    usage("Missing port");
                }

            } else if (args[i].equals("-search")) {
                flightSearch = true;
                noOfOptions +=1;
            }
            if (i == 8)
                break;
        }

        if (hostName == null && portString != null) {
                usage( "Missing host" );

        } else if (hostName != null && portString == null) {
            usage( "Missing port" );
        }


        int port;
        try {
            if (portString != null) {
                port = Integer.parseInt(portString);
            } else {
                port = 0;
            }
        } catch (NumberFormatException ex) {
            usage("Port \"" + portString + "\" must be an integer");
            return;
        }

        int i = noOfOptions;
        final int argLen = args.length - noOfOptions;
        if (argLen > 1 && argLen < 10 && !flightSearch) {
            usage("Too few arguments given\nExpected: 10\nGiven: " + argLen);
        } else if (argLen > 10 && !flightSearch) {
            usage("Too many arguments given\nExpected: 10\nGiven: " + argLen);
        } else if (argLen > 3 && flightSearch) {
            usage("Too many arguments given\nExpected: 3\nGiven: " + (3+argLen));
        }

        if (argLen+noOfOptions == 0) {
            usage(MISSING_ARGS);
        }
        AirlineRestClient client = new AirlineRestClient(hostName, port);
        String message = "";

        if (flightSearch) {
            if (hostName == null || portString == null) {
                usage("Missing host or port");
            }
            try {
                searchKeyList = Arrays.asList(args).subList(i, i+3);
            } catch (Exception e) {
                usage("\nToo few arguments given\nExpected: <airline name> <source> <destination>");
            }
            name = searchKeyList.get(0);
            src = checkWhetherTheStringIsAValidAirportCode(searchKeyList.get(1));
            dest = checkWhetherTheStringIsAValidAirportCode(searchKeyList.get(2));
            try {
                message = client.getFlights(name, src, dest);
            } catch ( IOException ex ) {
                error("Unable to connect to the server : ", "The server has either went down, or refused connection request, or does not exist. ", "Try reconnecting after some time.");
                return;
            }
            System.out.println(message);
            System.exit(0);
        } else if (argLen == 1) {
            name = args[i];
        } else if (argLen > 1){
            name = args[i++];
            flightNumber = args[i++];
            src = args[i++];
            departDate = args[i++];
            departTime = args[i++];
            departTimeDayHalf = args[i++];
            dest = args[i++];
            arriveDate = args[i++];
            arriveTime = args[i++];
            arriveTimeDayHalf = args[i];

            int number = checkWhetherTheStringIsAPositiveNumber(flightNumber);
            src = checkWhetherTheStringIsAValidAirportCode(src);
            boolean departDateValidity = checkWhetherTheStringIsAValidDate(departDate);
            boolean departTimeValidity = checkWhetherTheStringIsAValidTime(departTime+":"+departTimeDayHalf);
            dest = checkWhetherTheStringIsAValidAirportCode(dest);
            boolean arrivalDateValidity = checkWhetherTheStringIsAValidDate(arriveDate);
            boolean arrivalTimeValidity = checkWhetherTheStringIsAValidTime(arriveTime+":"+arriveTimeDayHalf);

            if (AirportNames.getName(src) == null || AirportNames.getName(dest) == null) {
                error("Invalid airport code: ", "", "Must be the three-letter code of a known airport");
            }

            departTime = departDate + " " + departTime + " " + departTimeDayHalf;
            arriveTime = arriveDate + " " + arriveTime + " " + arriveTimeDayHalf;
        }

        try {
            if (name == null) {
                // Print all key/value pairs
                message = client.getAllAirlinesAndFlights();

            } else if (flightNumber == null) {
                // Print all values of key
                message = client.getFlights(name);

            } else {
                // Post the key/value pair
                if (hostName != null && portString != null) {
                    message = client.addFlightToAirline(name, flightNumber, src, departTime, dest, arriveTime);
                }
                if (printFlight && !message.contains("Could not add new flight")) {
                    DateFormat dateFormat = new SimpleDateFormat("mm/dd/yyyy hh:mm aa");
                    Date departure = null;
                    Date arrival = null;
                    try {
                        departure = dateFormat.parse(departTime);
                        arrival = dateFormat.parse(arriveTime);

                    } catch (Exception e) {

                    }
                    Flight value = new Flight(Integer.valueOf(flightNumber), src, dest, departure, arrival);
                    System.out.println(value.toString());
                }
            }

        } catch ( IOException ex ) {
            error("Unable to connect to the server : ", "The server has either went down, or refused connection request, or does not exist. ", "Try reconnecting after some time.");
            return;
        }

        System.out.println(message);

        System.exit(0);
    }

    /**
     * Prints usage information for this program and exits.
     * @param message An error message to print
     */
    private static void usage( String message )
    {
        PrintStream err = System.err;
        err.println("** " + message);
        err.println();
        err.println("usage: java -jar Project4 [-README] [-host host -port port] [-print] [-search] [name] [number] [src] [departTime] [dest] [arriveTime]");
        err.println("  host          Host of web server");
        err.println("  port          Port of web server");
        err.println("  name          Name of the airline");
        err.println("  number        Flight number");
        err.println("  src           Departure airport code");
        err.println("  departTime    Departure date/time");
        err.println("  dest          Destination airport code");
        err.println("  arriveTime    Arrival date/time");
        err.println();
        err.println("This program adds flights to the specified airline stored at remote server");
        err.println("If no flight is given with airline name, then all flights of airline are printed");
        err.println("If no airline is given, all airlines and their flights are printed.");
        err.println();

        System.exit(1);
    }

    private static void printREADME() {
        System.out.println(
                "\nREADME\nAuthor: Nandini Khanwalkar\n" +
                "\n" +
                "Welcome to the Project-4, a web application of CS510J Airline Project at Portland State University.\n" +
                "This project implements a simple client server model using REST API to store an airline's flights on a remote server and retrieve them when needed. \n" +
                "It is mandatory to specify both a host and port, using flags -host and -port, in order to connect to the server." +
                "The program takes ten command line arguments as the new flight's information.\n" +
                "If the given flight information has correct formatting and valid values, an HTTP POST request is sent to the server, who creates a new Flight object with it and adds it to corresponding Airline. \n" +
                "If only airline name is given and no flight arguments are given, an HTTP GET request is sent to the server which returns the list of flights for that airline.\n" +
                "If no airline name is given, an HTTP GET request is sent to the server which returns the number of airlines stored at the server and their list of flights.\n" +
                "If -search flag is enabled and airline name, source airport code, and destination airport code is given, an HTTP GET request is set to the server which returns a list of direct flights, from that airline's list of flights, between those two airports.\n" +
                "\n" +
                "Enabling the -README flag will print this README and the program will exit irrespective of any other arguments that might have been passed.\n" +
                "Enabling the -print flag before arguments will print the new Flight's information as a string to stdout.\n" +
                "If no host and port is specified the program behaves identical to Project-1.\n" +
                "\n" +
                "The detailed command line usage is as follows:\n" +
                "\t$ java <executableFileName> [-host host -port port] [-README] [-print] [-search] [airlineName] [flightNumber] [sourceAirportCode] [departureDate] [departureTime] [destinationAirportCode] [arrivalDate] [arrivalTime]\n" +
                "\n" +
                "Constraints:\n" +
                "\t1. Multi-word arguments (such as airline names) must be delimited by double quotes.\n" +
                "\t2. Flight number must be a positive integer.\n" +
                "\t3. Source and destination airport codes must be three-lettered codes for the respective airports.\n" +
                "\t4. Departure and arrival dates must have format MM/dd/yyyy.\n" +
                "\t5. Departure and arrival times must have format hh:mm aa(12-hour time).\n" +
                "\t6. A port must be specified when host is specified and vice versa.\n");
    }


}