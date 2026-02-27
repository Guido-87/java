package logica;

import java.util.Date;

public class Alumno {
    int id;
    String nombre;
    String apellido;
    private Date fechaNac;

   public Alumno() {

   }

   public Alumno(int id, String nombre, String apellido) {
       this.id = id;
       this.nombre = nombre;
       this.apellido = apellido;
   }

    public Alumno(int id, String nombre, String apellido, Date fechaNac) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNac = fechaNac;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }


    public Date getFechaNac() {
        return fechaNac;
    }

    public void setFechaNac(Date fechaNac) {
        this.fechaNac = fechaNac;
    }

    public void mostrarNombre() {
        System.out.println("Hola, soy un alumno y se decir mi nombre");
   }

   public void saberAprobado(double calificacion) {
        if (calificacion >= 7) {
            System.out.println("Aprobé la materia");
        } else {
            System.out.println("No aprobé");
        }
   }
}
