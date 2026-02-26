public class Jefe extends Persona{
    int idJefe;
    String departamentoJefe;

    public Jefe() {

    }

    public Jefe(int num, int id, String dni, String nombre, String apellido, String domicilio, String telefono, int edad, int idJefe, String departamentoJefe) {
        super(num, id, dni, nombre, apellido, domicilio, telefono, edad);
        this.idJefe = idJefe;
        this.departamentoJefe = departamentoJefe;
    }

    public int getIdJefe() {
        return idJefe;
    }

    public void setIdJefe(int idJefe) {
        this.idJefe = idJefe;
    }

    public String getDepartamentoJefe() {
        return departamentoJefe;
    }

    public void setDepartamentoJefe(String departamentoJefe) {
        this.departamentoJefe = departamentoJefe;
    }
}
