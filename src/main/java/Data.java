package java;

import java.exception.NoFreeSeatsException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Mateusz Wieczorek
 *
 */
public class Data {
    Set<Trip> trips;
    Set<Trip> subscribes;

    public Data() {
        trips = new HashSet<Trip>();
        subscribes = new HashSet<Trip>();
    }

    public Trip addTrip(Trip trip) {
        for (Trip s : subscribes) {
            if (s.getStartingPlace().equalsIgnoreCase(trip.getStartingPlace()) &&
                    s.getDestination().equalsIgnoreCase(trip.getDestination())) {
                trips.add(trip);
                return trip;
            }
        }
        trips.add(trip);
        return null;
    }

    public Boolean cancelTrip(Trip trip) {
        return trips.remove(trip);
    }

    public Boolean subscribe(String startingPlace, String destination, User subscriber) {
        Trip trip = new Trip();
        trip.setStartingPlace(startingPlace);
        trip.setDestination(destination);
        trip.setOwner(subscriber);
        for (Trip t : trips) {
            if (t.getStartingPlace().equalsIgnoreCase(startingPlace) && t.getDestination().equalsIgnoreCase(destination)) {
                subscribes.add(trip);
                return true;
            }
        }
        subscribes.add(trip);
        return false;
    }

    public Boolean cancelSubscribe(String startingPlace, String destination, User subscriber) {
        for (Trip t : trips) {
            if (t.getStartingPlace().equalsIgnoreCase(startingPlace) &&
                    t.getDestination().equalsIgnoreCase(destination) &&
                    t.getOwner().equals(subscriber)) {
                trips.remove(t);
                return true;
            }
        }
        return false;
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
