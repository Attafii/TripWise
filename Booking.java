package model;

public class Booking {
    private int id;
    private String passengerName;
    private int flightId;
    private String seatNumber;

    public Booking(int id, String passengerName, int flightId, String seatNumber){
        this.id=id; this.passengerName=passengerName; this.flightId=flightId; this.seatNumber=seatNumber;
    }
    public Booking(String passengerName, int flightId, String seatNumber){
        this(0, passengerName, flightId, seatNumber);
    }

    public int getId(){return id;}
    public String getPassengerName(){return passengerName;}
    public int getFlightId(){return flightId;}
    public String getSeatNumber(){return seatNumber;}

    public void setId(int id){this.id=id;}
    public void setPassengerName(String v){this.passengerName=v;}
    public void setFlightId(int v){this.flightId=v;}
    public void setSeatNumber(String v){this.seatNumber=v;}
}
