import com.sun.net.httpserver.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;

public class EcommerceServer {

    static List<Product> products = Arrays.asList(
        new Product("Phone", 599.99),
        new Product("Laptop", 999.99),
        new Product("Watch", 199.99)
    );

    static List<Product> cart = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        server.createContext("/", EcommerceServer::handleHome);
        server.createContext("/add", EcommerceServer::handleAddToCart);
        server.createContext("/cart", EcommerceServer::handleCart);

        server.setExecutor(null);
        System.out.println("Server started at http://localhost:8000");
        server.start();
    }

    static void handleHome(HttpExchange exchange) throws IOException {
        StringBuilder response = new StringBuilder();
        response.append("<html><head><title>Shop</title></head><body>");
        response.append("<h1>Welcome to Mini Shop</h1><ul>");

        for (Product p : products) {
            response.append("<li>")
                    .append(p.name).append(" - $").append(p.price)
                    .append(" <a href='/add?name=").append(p.name)
                    .append("'>Add to Cart</a></li>");
        }

        response.append("</ul>");
        response.append("<a href='/cart'>View Cart</a>");
        response.append("</body></html>");

        send(exchange, response.toString());
    }

    static void handleAddToCart(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query != null && query.startsWith("name=")) {
            String name = query.substring(5);
            for (Product p : products) {
                if (p.name.equalsIgnoreCase(name)) {
                    cart.add(p);
                    break;
                }
            }
        }
        redirect(exchange, "/cart");
    }

    static void handleCart(HttpExchange exchange) throws IOException {
        StringBuilder response = new StringBuilder();
        response.append("<html><head><title>Cart</title></head><body>");
        response.append("<h1>Your Cart</h1><ul>");

        if (cart.isEmpty()) {
            response.append("<p>Cart is empty.</p>");
        } else {
            for (Product p : cart) {
                response.append("<li>").append(p.name).append(" - $").append(p.price).append("</li>");
            }
        }

        response.append("</ul><a href='/'>Back to Shop</a>");
        response.append("</body></html>");

        send(exchange, response.toString());
    }

    static void send(HttpExchange exchange, String response) throws IOException {
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    static class Product {
        String name;
        double price;
        Product(String name, double price) {
            this.name = name;
            this.price = price;
        }
    }
}
