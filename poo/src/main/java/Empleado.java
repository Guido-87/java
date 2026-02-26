public class Empleado extends Persona {
    int numLegajo;
    String cargo;
    Double sueldo;

    public Empleado() {
    }

    public Empleado(int num, int id, String dni, String nombre, String apellido, String domicilio, String telefono, int edad, int numLegajo, String cargo, Double sueldo) {
        super(num, id, dni, nombre, apellido, domicilio, telefono, edad);
        this.numLegajo = numLegajo;
        this.cargo = cargo;
        this.sueldo = sueldo;
    }

    public int getNumLegajo() {
        return numLegajo;
    }

    public void setNumLegajo(int numLegajo) {
        this.numLegajo = numLegajo;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public Double getSueldo() {
        return sueldo;
    }

    public void setSueldo(Double sueldo) {
        this.sueldo = sueldo;
    }
}
