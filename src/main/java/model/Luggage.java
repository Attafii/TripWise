package model;

public class Luggage {
    private int id;
    private int bookingId;
    private double weight;
    private String status;

    public Luggage(int id, int bookingId, double weight, String status){
        this.id=id; this.bookingId=bookingId; this.weight=weight; this.status=status;
    }
    public Luggage(int bookingId, double weight, String status){
        this(0, bookingId, weight, status);
    }

    public int getId(){return id;}
    public int getBookingId(){return bookingId;}
    public double getWeight(){return weight;}
    public String getStatus(){return status;}

    public void setId(int id){this.id=id;}
    public void setBookingId(int v){this.bookingId=v;}
    public void setWeight(double v){this.weight=v;}
    public void setStatus(String v){this.status=v;}
}
