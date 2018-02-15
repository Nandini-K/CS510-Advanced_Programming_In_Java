package edu.pdx.cs410J.nandini2;

import edu.pdx.cs410J.AbstractFlight;

import java.text.DateFormat;
import java.util.Date;

/**
 * This class implements a <code>Flight</code> object.
 * Each <code>Flight</code> has an airline name, flight number, source and destination, arrival and departure times associated with it.
 */
public class Flight extends AbstractFlight implements Comparable<Flight>{

  private final int flightNumber;
  private String source;
  private String destination;
  private Date arrivalDate;
  private Date departureDate;

  /**
   * Constructs a new <code>Flight</code> with given parameters.
   * @param number      Flight number.
   * @param src         Source airport code.
   * @param dest        Destination airport code.
   * @param departure   Departure date and time.
   * @param arrival     Arrival date and time.
   */
  public Flight(int number, String src, String dest, Date departure, Date arrival) {
    flightNumber = number;
    source = src;
    destination = dest;
    departureDate = departure;
    arrivalDate = arrival;
  }

  /**
   * @return Flight number
   */
  @Override
  public int getNumber() {
    return this.flightNumber;
  }

  /**
   * @return Source airport code.
   */
  @Override
  public String getSource() {
    return this.source;
  }

  /**
   * @return Departure date and time.
   */
  @Override
  public String getDepartureString() {
    DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    return dateFormat.format(new Date(this.departureDate.getTime()));
  }

  /**
   * @return Arrival airport code.
   */
  @Override
  public String getDestination() {
    return this.destination;
  }

  /**
   * @return Arrival date and time.
   */
  @Override
  public String getArrivalString() {
    DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    return dateFormat.format(new Date(this.arrivalDate.getTime()));
  }

  /**
   * @return Departure date and time as <code>Date</code>
   */
  @Override
  public Date getDeparture(){
    return this.departureDate;
  }

  /**
   * @return Arrival date and time as <code>Date</code>
   */
  @Override
  public Date getArrival() {
    return this.arrivalDate;
  }

  /**
   * Compares two <code>Flight</code>s based on source and departure time.
   * @param f A <code>Flight</code>
   * @return <code>int</code> difference of the flights
   */
  @Override
  public int compareTo(Flight f) {
    int srcDiff = this.source.compareTo(f.getSource());
    if (srcDiff < 0)
      return -1;
    else if (srcDiff > 0)
      return 1;
    else if (srcDiff == 0) {
      if (this.departureDate.before(f.departureDate))
        return -1;
      else if (this.departureDate.after(f.departureDate))
        return 1;
    }
    return 0;
  }
}
