package exception;

/**
 *
 * @author Mateusz Wieczorek
 *
 */

public class NoFreeSeatsException extends Exception {
    public NoFreeSeatsException(String message) {
        super(message);
    }
}
