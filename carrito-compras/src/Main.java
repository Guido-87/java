import java.util.*;

abstract class Producto {
    protected String nombre;
    protected double precio;

    public Producto(String nombre, double precio) {
        this.nombre = nombre;
        this.precio = precio;
    }

    protected abstract double getPrecio();
    protected abstract double getStock();
}

class ProductoPorKilo extends Producto {
    public double peso;
    public double stock;

    public ProductoPorKilo(String nombre, double precioPorKilo, double peso, double stock) {
        super(nombre, precioPorKilo);
        this.peso = peso;
        this.stock = stock;
    }

    public double getPrecio() {
        return precio * peso;
    }
    public double getStock() {
        return stock;
    }
}

class CarritoDeCompra {
    private final List<Producto> productos;

    public CarritoDeCompra() {
        productos = new ArrayList<>();
    }

    /* public CarritoDeCompra(List<Producto> productos) {
        this.productos = productos;
    } */

    public void agregarProducto(Producto producto) {
        productos.add(producto);
    }

    public double calcularTotalProductos() {
        double total = 0;
        for (Producto producto : productos) {
            total = producto.getStock() > 0 ? total + producto.getPrecio() : total;
        }
        return total;
    }
}

public class Main {
    public static void main(String[] args) {
        // TODO: Refactor con Factory
        ProductoPorKilo producto1 = new ProductoPorKilo("Manzanas", 2.5, 3, 0);
        ProductoPorKilo producto2 = new ProductoPorKilo("Plátanos", 3.0, 2.5, 1);
        CarritoDeCompra carrito = new CarritoDeCompra();

        /* Alternativa
            List<Producto> productos = new ArrayList<>();
            productos.add(producto1);
            productos.add(producto2);
            CarritoDeCompra carritoProductos = new CarritoDeCompra(productos);
        */

        carrito.agregarProducto(producto1);
        carrito.agregarProducto(producto2);
        double total = carrito.calcularTotalProductos();
        System.out.println("Total de la compra: $" + total);
    }
}