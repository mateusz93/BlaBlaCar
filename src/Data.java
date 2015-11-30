import exception.NoFreeSeatsException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * Created by pai-48 on 23.11.15.
 *
 */
public class Data {
    Set<Trip> trips;

    public Data() {
        trips = new HashSet<Trip>();
    }
    public Boolean addTrip(Trip trip) {
        return trips.add(trip);
    }

    public Boolean cancelTrip(Trip trip) {
        return trips.remove(trip);
    }

    public Boolean reserveSeat(Trip trip) throws NoFreeSeatsException {
        for (Trip t : trips) {
            if (t.equals(trip) || t == trip) {
                if (t.getFreeSeats() > 0) {
                    t.setFreeSeats(t.getFreeSeats() - 1);
                    return true;
                } else {
                    throw new NoFreeSeatsException("Brak wolnych miejsc");
                }
            }
        }
        return false;
    }
}
