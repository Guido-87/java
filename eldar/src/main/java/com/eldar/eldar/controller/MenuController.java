package com.eldar.eldar.controller;

import com.eldar.eldar.exception.AppException;
import com.eldar.eldar.model.Persona;
import com.eldar.eldar.model.Tarjeta;
import com.eldar.eldar.service.PersonaService;
import com.eldar.eldar.service.TarjetaService;
import com.eldar.eldar.utils.EncryptUtil;
import com.eldar.eldar.utils.MarcaTarjeta;
import com.eldar.eldar.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

@Controller
@Component
@Profile("!test")
public class MenuController implements CommandLineRunner {
    @Autowired
    private PersonaService personaService;

    @Autowired
    private TarjetaService tarjetaService;

    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void run(String... args) throws Exception {
        while (true) {
            mostrarMenu();
            int opcion = scanner.nextInt();
            scanner.nextLine();
            try {
                switch (opcion) {
                    case 1:
                        registrarPersona();
                        break;
                    case 2:
                        registrarTarjeta();
                        break;
                    case 3:
                        obtenerTarjetasPorDni();
                        break;
                    case 4:
                        consultarTasasPorFecha();
                        break;
                    case 0:
                        System.out.println("¡Hasta luego!");
                        SpringApplication.exit(SpringApplication.run(MenuController.class, args), () -> 0);
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (AppException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void mostrarMenu() {
        System.out.println("Selecciona una opción:");
        System.out.println("1 - Registrar una persona");
        System.out.println("2 - Registrar una tarjeta");
        System.out.println("3 - Retornar información de las tarjetas asociadas de un usuario");
        System.out.println("4 - Consultar las tasas de todas las marcas por fecha");
        System.out.println("0 - Salir");
    }

    private void registrarPersona() {
        System.out.println("Ingrese el nombre:");
        String nombre = scanner.nextLine();
        System.out.println("Ingrese el apellido:");
        String apellido = scanner.nextLine();
        System.out.println("Ingrese el DNI:");
        String dni = scanner.nextLine();
        System.out.println("Ingrese la fecha de nacimiento (dd-MM-yyyy):");
        String fechaNacimiento = scanner.nextLine();
        System.out.println("Ingrese el email:");
        String email = scanner.nextLine();

        Persona persona = new Persona();
        persona.setNombre(nombre);
        persona.setApellido(apellido);
        persona.setDni(dni);
        persona.setFechaNacimiento(Utils.formatearFecha(fechaNacimiento));
        persona.setEmail(email);

        personaService.registrarPersona(persona);
        System.out.println("Persona registrada con éxito.");
    }

    private void registrarTarjeta() throws Exception {
        System.out.println("Ingrese el DNI del titular de la tarjeta:");
        String dni = scanner.nextLine();
        Persona persona = personaService.obtenerPorDni(dni);

        if (persona != null) {
            System.out.println("Ingrese la marca de la tarjeta:");
            String marca = scanner.nextLine();
            System.out.println("Ingrese el número de la tarjeta:");
            String numero = scanner.nextLine();
            System.out.println("Ingrese la fecha de vencimiento (dd-MM-yyyy):");
            String fechaVencimiento = scanner.nextLine();
            System.out.println("Ingrese el nombre completo del titular:");
            String titularNombreCompleto = scanner.nextLine();

            Tarjeta tarjeta = new Tarjeta();
            tarjeta.setMarca(marca);
            tarjeta.setNumero(numero);
            tarjeta.setFechaVencimiento(Utils.formatearFecha(fechaVencimiento));
            tarjeta.setTitularNombreCompleto(titularNombreCompleto);
            tarjeta.setPersona(persona);

            EncryptUtil.generarClaveAES();
            tarjeta.setCodigoSeguridad(EncryptUtil.encriptar(new Random().toString()));
            tarjeta.setPan(EncryptUtil.encriptar(new Random().toString()));
            tarjetaService.registrarTarjeta(tarjeta, persona.getEmail());
            System.out.println("Tarjeta registrada con éxito.");
        } else {
            System.out.println("Persona no encontrada.");
        }
    }

    private void obtenerTarjetasPorDni() {
        System.out.println("Ingrese el DNI del usuario:");
        String dni = scanner.nextLine();
        List<Tarjeta> tarjetas = tarjetaService.obtenerTarjetasPorDni(dni);

        if (tarjetas.isEmpty()) {
            System.out.println("No se encontraron tarjetas asociadas a este DNI.");
        } else {
            for (Tarjeta tarjeta : tarjetas) {
                System.out.println("Marca: " + tarjeta.getMarca());
                System.out.println("Número: " + tarjeta.getNumero());
                System.out.println("Fecha de vencimiento: " + tarjeta.getFechaVencimiento());
                System.out.println("Titular: " + tarjeta.getTitularNombreCompleto());
            }
        }
    }

    private void consultarTasasPorFecha() {
        System.out.println("Ingrese la fecha en formato dd-MM-yyyy (deje vacío para usar la fecha actual):");
        String fechaStr = scanner.nextLine();
        LocalDate fecha = fechaStr.isEmpty() ? LocalDate.now() : Utils.formatearFecha(fechaStr);
        System.out.println("Consultando tasas para la fecha: " + fecha);
        System.out.println("Tasa VISA: " + MarcaTarjeta.VISA.calcularTasa(fecha));
        System.out.println("Tasa NARA: " + MarcaTarjeta.NARA.calcularTasa(fecha));
        System.out.println("Tasa AMEX: " + MarcaTarjeta.AMEX.calcularTasa(fecha));
    }
}