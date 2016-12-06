import org.eclipse.jetty.websocket.api.Session;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private List<User> users;

    public Trip() {
        users = new ArrayList<User>();
    }

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

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "Kierowca: " + owner +
                ", Dzień startu: " + startingDay +
                ", Miejsca startu: " + startingPlace +
                ", Miejsce docelowe: " + destination +
                ", Cena: " + price +
                ", Wolne miejsca: " + freeSeats;
    }
}
