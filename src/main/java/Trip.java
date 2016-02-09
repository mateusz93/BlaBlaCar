import org.eclipse.jetty.websocket.api.Session;

import java.util.Date;

/**
 *
 * @author Mateusz Wieczorek
 *
 */
public class Trip {

    private User owner;
    private Date startingDay;
    private String startingPlace;
    private String destination;
    private double price;
    private int freeSeats;
    private Session user;

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Date getStartingDay() {
        return startingDay;
    }

    public void setStartingDay(Date startingDay) {
        this.startingDay = startingDay;
    }

    public String getStartingPlace() {
        return startingPlace;
    }

    public void setStartingPlace(String startingPlace) {
        this.startingPlace = startingPlace;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getFreeSeats() {
        return freeSeats;
    }

    public void setFreeSeats(int freeSeats) {
        this.freeSeats = freeSeats;
    }

    public Session getUser() {
        return user;
    }

    public void setUser(Session user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "java.Trip{" +
                "owner=" + owner +
                ", startDay=" + startingDay +
                ", startingPlace='" + startingPlace + '\'' +
                ", destination='" + destination + '\'' +
                ", price=" + price +
                ", freeSeats=" + freeSeats +
                '}';
    }
}
