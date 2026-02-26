package logica;

public class Caja <T>{
    private T contenido;

    public void ponerAlgo(T contenido) {
        this.contenido = contenido;
    }

    public T obtenerAlgo() {
        return contenido;
    }
}
