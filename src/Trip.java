import java.util.Date;

/**
 *
 * Created by pai-48 on 23.11.15.
 *
 */
public class Trip {
    private User owner;
    private Date start;
    private String startingDay;
    private String destination;
    private double price;
    private int freeSeats;

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public void setStartingDay(String startingDay) {
        this.startingDay = startingDay;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setFreeSeats(int freeSeats) {
        this.freeSeats = freeSeats;
    }

    public int getFreeSeats() {
        return freeSeats;
    }

    public User getOwner() {
        return owner;
    }

    public Date getStart() {
        return start;
    }

    public String getStartingDay() {
        return startingDay;
    }

    public String getDestination() {
        return destination;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "owner=" + owner +
                ", start=" + start +
                ", startingDay='" + startingDay + '\'' +
                ", destination='" + destination + '\'' +
                ", price=" + price +
                ", freeSeats=" + freeSeats +
                '}';
    }
}
