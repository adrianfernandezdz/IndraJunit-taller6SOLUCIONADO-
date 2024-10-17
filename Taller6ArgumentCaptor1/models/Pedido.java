package Taller6.Taller6ArgumentCaptor1.models;

import java.util.List;

public class Pedido {
    private List<String> productos;
    private double total;

    public Pedido(List<String> productos) {
        this.productos = productos;
    }

    public List<String> getProductos() {
        return productos;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getTotal() {
        return total;
    }
}