import static spark.Spark.*;
/**
 *
 * Created by pai-48 on 23.11.15.
 *
 */
public class HelloWorld {

    public static void main(String[] args) {
        get("/hello", (req, res) -> "Hello World");
    }
}
