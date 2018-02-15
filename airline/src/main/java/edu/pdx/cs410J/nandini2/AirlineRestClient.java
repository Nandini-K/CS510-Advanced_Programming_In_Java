package edu.pdx.cs410J.nandini2;

import com.google.common.annotations.VisibleForTesting;
import edu.pdx.cs410J.web.HttpRequestHelper;

import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * A helper class for accessing the rest client.
 */
public class AirlineRestClient extends HttpRequestHelper
{
    private static final String WEB_APP = "airline";
    private static final String SERVLET = "flights";

    private final String url;


    /**
     * Creates a client to the airline REST service running on the given host and port.
     * @param hostName The name of the host
     * @param port     The port number
     */
    public AirlineRestClient( String hostName, int port )
    {
        this.url = String.format( "http://%s:%d/%s/%s", hostName, port, WEB_APP, SERVLET );
    }

    /**
     * Returns all airlines with flights from the server.
     * @return String containing all airlines and flights.
     * @throws IOException
     */
    public String getAllAirlinesAndFlights() throws IOException {
      Response response = get(this.url);
      return response.getContent();
    }

    /**
     * Returns the flights for the given airline.
     * @param name   Name of Airline
     * @param src    Airport code of source
     * @param dest   Airport code of destination
     * @return       String containing Airline's list of flights between source and destination
     * @throws IOException
     */
    public String getFlights(String name, String src, String dest) throws IOException {
        Response response = get(this.url, "name", name, "src", src, "dest", dest);
        throwExceptionIfNotOkayHttpStatus(response);
        return response.getContent();
    }

    /**
     * Returns a string containing flight-list of given airline.
     * @param name   Name of Airline
     * @return       String containing all flights of Airline
     * @throws IOException
     */
    public String getFlights(String name) throws IOException {
        Response response = get(this.url, "name", name);
        throwExceptionIfNotOkayHttpStatus(response);
        return response.getContent();
    }

    /**
     * Sends http post request to the server.
     * @param name           Name of Airline
     * @param flightNumber   Flight number
     * @param src            Airport code of source
     * @param departTime     Date and time of departure
     * @param dest           Airport code of destination
     * @param arriveTime     Date and time of arrival
     * @return               String containing server response
     * @throws IOException
     */
    public String addFlightToAirline(String name, String flightNumber, String src, String departTime, String dest, String arriveTime) throws IOException {
      Response response = postToMyURL("name", name, "flightNumber", flightNumber, "src", src, "dest", dest, "departTime", departTime, "arriveTime", arriveTime);
      throwExceptionIfNotOkayHttpStatus(response);
      return response.getContent();
    }

    @VisibleForTesting
    Response postToMyURL(String... airlineAndFlight) throws IOException {
      return post(this.url, airlineAndFlight);
    }

    private Response throwExceptionIfNotOkayHttpStatus(Response response) {
      int code = response.getCode();
      if (code != HTTP_OK) {
        throw new AppointmentBookRestException(code);
      }
      return response;
    }

    private class AppointmentBookRestException extends RuntimeException {
      public AppointmentBookRestException(int httpStatusCode) {
        super("Got an HTTP Status Code of " + httpStatusCode);
      }
    }
}
