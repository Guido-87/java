public class Consultor extends Persona {
    String nombreConsultora;
    int numConsultor;

    public Consultor() {
    }

    public Consultor(int num, int id, String dni, String nombre, String apellido, String domicilio, String telefono, int edad, String nombreConsultora, int numConsultor) {
        super(num, id, dni, nombre, apellido, domicilio, telefono, edad);
        this.nombreConsultora = nombreConsultora;
        this.numConsultor = numConsultor;
    }

    public String getNombreConsultora() {
        return nombreConsultora;
    }

    public void setNombreConsultora(String nombreConsultora) {
        this.nombreConsultora = nombreConsultora;
    }

    public int getNumConsultor() {
        return numConsultor;
    }

    public void setNumConsultor(int numConsultor) {
        this.numConsultor = numConsultor;
    }
}
