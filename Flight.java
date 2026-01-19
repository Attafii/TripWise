package model;

public class Flight {
    private int id;
    private String flightNumber;
    private String origin;
    private String destination;

    public Flight(int id, String flightNumber, String origin, String destination){
        this.id=id; this.flightNumber=flightNumber; this.origin=origin; this.destination=destination;
    }
    public Flight(String flightNumber, String origin, String destination){
        this(0, flightNumber, origin, destination);
    }
    public int getId(){return id;}
    public String getFlightNumber(){return flightNumber;}
    public String getOrigin(){return origin;}
    public String getDestination(){return destination;}

    public void setId(int id){this.id=id;}
    public void setFlightNumber(String v){this.flightNumber=v;}
    public void setOrigin(String v){this.origin=v;}
    public void setDestination(String v){this.destination=v;}
}
