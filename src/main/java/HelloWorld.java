package java;

import static spark.Spark.*;
/**
 *
 * @author Mateusz Wieczorek
 *
 */
public class HelloWorld {

    public static void main(String[] args) {
        get("/BlaBlaCar", (req, res) -> "Witaj w aplikacji BlaBlaCar :)");
    }
}
