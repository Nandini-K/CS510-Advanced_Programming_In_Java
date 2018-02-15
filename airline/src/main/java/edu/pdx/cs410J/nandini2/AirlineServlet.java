package edu.pdx.cs410J.nandini2;

import com.google.common.annotations.VisibleForTesting;
import edu.pdx.cs410J.AirportNames;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import static java.lang.String.valueOf;

/**
 * This servlet ultimately provides a REST API for working with an
 * <code>Airline</code>.  However, in its current state, it is an example
 * of how to use HTTP and Java servlets to store simple key/value pairs.
 */
public class AirlineServlet extends HttpServlet {
  private final Map<String, Collection<Flight>> airlines = new HashMap<>();

  /**
   * Handles an HTTP GET request from a client by retrieving the flights list of airline
   * specified in the "name" HTTP parameter to the HTTP response.  If the "name"
   * parameter is not specified, all of the airlines and their flights are written to the
   * HTTP response.
   * @param request   HTTP request object
   * @param response  HTTP response object
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
      response.setContentType( "text/plain" );
      String name = getParameter( "name", request );
      String source = Formatters.checkWhetherTheStringIsAValidAirportCode(getParameter("src", request));
      String destination = Formatters.checkWhetherTheStringIsAValidAirportCode(getParameter("dest", request));
      if (name != null) {
          String airportCode = null;
          if (source != null && destination != null) {
              if (AirportNames.getName(source) == null || AirportNames.getName(destination) == null) {
                  response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Invalid airport code : Must be the three-letter code of a known airport");
              }
              PrintWriter pw = response.getWriter();
              if (this.airlines.containsKey(name)) {
                  Collection<Flight> flightList = this.airlines.get(name);
                  Collection<Flight> searchResults = searchFlights(source, destination, flightList);
                  if (!searchResults.isEmpty()) {
                      pw.println("\nSearch results for " + name + ":\n\n" + Formatters.prettyPrint(searchResults));
                  } else {
                      pw.println("\nNo flights are available from " + source + " to " + destination + " at this time. Please check again later.\n");
                  }
              } else pw.println("\nThe Airline you asked for, does not exist.\n");

              pw.flush();

              response.setStatus( HttpServletResponse.SC_OK );
          } else if (source != null) {
              missingRequiredParameter(response, "destination");
          } else if (destination != null) {
              missingRequiredParameter(response, "source");
          } else {
              writeValue(name, response);
          }
      } else {
          writeAllMappings(response);
      }

  }

    private static Collection<Flight> searchFlights(String source, String destination, Collection<Flight> flightList) {
        Collection<Flight> newFlightList = new ArrayList<>();
        Iterator<Flight> iterator = flightList.iterator();
        while (iterator.hasNext()) {
            Flight nextFlight = iterator.next();
            if (nextFlight.getSource().equals(source) && nextFlight.getDestination().equals(destination)) {
                newFlightList.add(nextFlight);
            }
        }
        return newFlightList;
    }

  /**
   * Handles an HTTP POST request by adding the flight to the airline specified by the
   * "name" parameter.
   * @param request   HTTP request object
   * @param response  HTTP response object
   * @throws ServletException
   * @throws IOException
*/
  @Override
  protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
  {
      response.setContentType( "text/plain" );

      String name = getParameter("name", request);
      if (name == null) {
          missingRequiredParameter(response, "name");
          return;
      }

      String source = Formatters.checkWhetherTheStringIsAValidAirportCode(getParameter("src", request));
      if (source == null) {
          missingRequiredParameter(response, "src");
          return;
      }

      String destination = Formatters.checkWhetherTheStringIsAValidAirportCode(getParameter("dest", request));
      if (destination == null) {
          missingRequiredParameter(response, "dest");
          return;
      }

      if (AirportNames.getName(source) == null || AirportNames.getName(destination) == null) {
          response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Invalid airport code : Must be the three-letter code of a known airport");
      }

      String number = valueOf(Formatters.checkWhetherTheStringIsAPositiveNumber(getParameter("flightNumber", request)));
      if (number == null) {
          missingRequiredParameter(response, "flightNumber");
          return;
      }

      String departure = getParameter("departTime", request);
      if (departure == null) {
          missingRequiredParameter(response, "departTime");
          return;
      }
      String[] splitDep = departure.split(" ");
      if (splitDep.length != 3) {
          response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Invalid date/time format : expected MM/dd/yyyy hh:mm aa");
      }
      boolean depDateValidity = Formatters.checkWhetherTheStringIsAValidDate(splitDep[0]);
      boolean depTimeValidity = Formatters.checkWhetherTheStringIsAValidTime(splitDep[1]+":"+splitDep[2]);

      String arrival = getParameter("arriveTime", request);
      if (arrival == null) {
          missingRequiredParameter(response, "arriveTime");
          return;
      }
      String[] splitArr = arrival.split(" ");
      if (splitArr.length != 3) {
          response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Invalid date/time format : expected MM/dd/yyyy hh:mm aa");
      }
      boolean arrDateValidity = Formatters.checkWhetherTheStringIsAValidDate(splitArr[0]);
      boolean arrTimeValidity = Formatters.checkWhetherTheStringIsAValidTime(splitArr[1]+":"+splitArr[2]);


      Collection<Flight> flightList = new ArrayList<>();
      if (airlines.containsKey(name)) {
          flightList = this.airlines.get(name);
      }
      DateFormat dateFormat = new SimpleDateFormat("mm/dd/yyyy hh:mm aa");
      Date departTime = null;
      Date arriveTime = null;
      try {
          departTime = dateFormat.parse(departure);
          arriveTime = dateFormat.parse(arrival);

      } catch (Exception e) {

      }
      Flight newFlight = new Flight(Integer.valueOf(number), source.toUpperCase(), destination.toUpperCase(), departTime, arriveTime);
      PrintWriter pw = response.getWriter();
      if(!contains(name, newFlight)) {
          flightList.add(newFlight);
          this.airlines.put(name, flightList);
          pw.println("Added flight to airline " + name);
      } else {
          pw.println("Could not add new flight: an equal flight already exists");
      }
      pw.flush();

      response.setStatus( HttpServletResponse.SC_OK);
  }

  /**
   * Writes an error message about a missing parameter to the HTTP response.
   *
   */
  private void missingRequiredParameter( HttpServletResponse response, String parameterName ) throws IOException {
      String message = String.format("The required parameter \"%s\" is missing", parameterName);;
      response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, message);
  }

  /**
   * Writes string containing the list of flights for airline specified as name.
   *
   */
  private void writeValue( String name, HttpServletResponse response ) throws IOException {
      PrintWriter pw = response.getWriter();
      if (this.airlines.containsKey(name)) {
          Collection<Flight> flights = this.airlines.get(name);
          if (!flights.isEmpty()) {
              pw.println("\nFlight list for " + name + ":\n\n" + Formatters.prettyPrint(flights));
          } else {
              pw.println("\nNo flights are available at this time. Please check again later.\n");
          }
      } else pw.println("\nThe Airline you asked for, does not exist.\n");

      pw.flush();

      response.setStatus( HttpServletResponse.SC_OK );
  }

  /**
   * Writes all airlines and their respective flight-lists.
   *
   */
  private void writeAllMappings( HttpServletResponse response ) throws IOException {
      PrintWriter pw = response.getWriter();
      pw.println(String.format("\nServer contains %d airline(s)\n", this.airlines.size()));

      for (Map.Entry<String, Collection<Flight>> entry : this.airlines.entrySet()) {
          pw.println("Flight list for " + entry.getKey() + ":\n\n" + Formatters.prettyPrint(entry.getValue()));

      }
      pw.flush();

      response.setStatus( HttpServletResponse.SC_OK );
  }

  /**
   * Returns the value of the HTTP request parameter with the given name.
   *
   * @return <code>null</code> if the value of the parameter is
   *         <code>null</code> or is the empty string
   */
  private String getParameter(String name, HttpServletRequest request) {
    String value = request.getParameter(name);
    if (value == null || "".equals(value)) {
      return null;

    } else {
      return value;
    }
  }

  @VisibleForTesting
  void setValueForKey(String key, Collection<Flight> value) {
      this.airlines.put(key, value);
  }

  @VisibleForTesting
  Collection<Flight> getValueForKey(String key) {
      return this.airlines.get(key);
  }

    /**
     * Checks whether the <code>Airline</code> contains a <code>Flight</code> equal to <code>flight</code>.
     * @param flight A <code>Flight</code> whose existence is to be checked.
     * @return boolean
     */
    private boolean contains(String name, Flight flight) {
        Collection<Flight> allFlights = this.airlines.get(name);
        if (allFlights != null) {
            Iterator<Flight> flightIterator = allFlights.iterator();
            while (flightIterator.hasNext()) {
                Flight nextFlight = flightIterator.next();
                if (nextFlight.compareTo(flight) == 0)
                    return true;
            }
        }
        return false;
    }



}
