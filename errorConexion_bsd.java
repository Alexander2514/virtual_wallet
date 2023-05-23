
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

class Usuario {

    
    private String nombreUsuario;
    private String contraseña;
    private String nombre;
    private double saldo;

    public Usuario(String nombreUsuario, String contraseña, String nombre, double saldo) {
        this.nombreUsuario = nombreUsuario;
        this.contraseña = contraseña;
        this.nombre = nombre;
        this.saldo = saldo;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getContraseña() {
        return contraseña;
    }

    public String getNombre() {
        return nombre;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }
    
    public void guardarDatos() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/virtual_wallet", "root", "123456")) {
            String query = "INSERT INTO usuarios (nombre_usuario, contrasenia, nombre, saldo) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, nombreUsuario);
            statement.setString(2, contraseña);
            statement.setString(3, nombre);
            statement.setDouble(4, saldo);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Usuario cargarDatos(String nombreUsuario) {
        Usuario usuario = null;
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/virtual_wallet", "root", "123456")) {
            String query = "SELECT nombre_usuario, contrasenia, nombre, saldo FROM usuarios WHERE nombre_usuario = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, nombreUsuario);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String contraseña = resultSet.getString("contrasenia");
                String nombre = resultSet.getString("nombre");
                double saldo = resultSet.getDouble("saldo");
                usuario = new Usuario(nombreUsuario, contraseña, nombre, saldo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuario;
    }
}

public class index {
    
    private static Map<String, Usuario> usuarios = new HashMap<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            
            Class.forName("com.mysql.cj.jdbc.Driver");
        
        } catch (Exception e) {
            System.out.println("Ha surgido un error" + e);
        }
        System.out.println("¡Bienvenido/a a la aplicación de billetera virtual!");
        boolean salir = false;
        while (!salir) {
            System.out.println("\n¿Qué te gustaría hacer?");
            System.out.println("1. Iniciar sesión");
            System.out.println("2. Crear un nuevo usuario");
            System.out.println("3. Eliminar un usuario existente");
            System.out.println("4. Salir");
            int opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {
                case 1:
                    iniciarSesion();
                    break;
                case 2:
                    crearUsuario();
                    break;
                case 3:
                    eliminarUsuario();
                    break;
                case 4:
                    salir = true;
                    break;
                default:
                    System.out.println("Opción inválida. Por favor, intenta nuevamente.");
            }
        }
        System.out.println("¡Gracias por usar la aplicación de billetera virtual!");
    }
    
    private static void iniciarSesion() {
        System.out.println("Por favor, ingresa tu nombre de usuario:");
        String nombreUsuario = scanner.nextLine();
        System.out.println("Por favor, ingresa tu contraseña:");
        String contraseña = scanner.nextLine();

        Usuario usuario = Usuario.cargarDatos(nombreUsuario);

        if (usuario != null && usuario.getContraseña().equals(contraseña)) {
            usuarios.put(nombreUsuario, usuario);
            System.out.println("¡Inicio de sesión exitoso! Bienvenido/a, " + usuario.getNombre() + ".");
            System.out.println("Tu saldo actual es $" + usuario.getSaldo() + ".");

            boolean salir = false;
            while (!salir) {
                System.out.println("\n¿Qué te gustaría hacer?");
                System.out.println("1. Enviar dinero a otro usuario");
                System.out.println("2. Cerrar sesión");
                int opcion = scanner.nextInt();
                scanner.nextLine();

                switch (opcion) {
                    case 1:
                        enviarDinero(usuario);
                        break;
                    case 2:
                        System.out.println("Cierre de sesión exitoso. ¡Adiós, " + usuario.getNombre() + "!");
                        salir = true;
                        break;
                    default:
                        System.out.println("Opción inválida. Por favor, intenta nuevamente.");
                }
            }
        } else {
            System.out.println("Inicio de sesión fallido. Nombre de usuario o contraseña incorrectos.");
        }
    }

    private static void crearUsuario() {
        System.out.println("Por favor, ingresa un nombre de usuario:");
        String nombreUsuario = scanner.nextLine();

        if (usuarios.containsKey(nombreUsuario)) {
            System.out.println("Error: El nombre de usuario ya está en uso.");
            return;
        }

        System.out.println("Por favor, ingresa una contraseña:");
        String contraseña = scanner.nextLine();
        System.out.println("Por favor, ingresa tu nombre:");
        String nombre = scanner.nextLine();
        System.out.println("Por favor, ingresa tu saldo inicial:");
        double saldo = scanner.nextDouble();
        scanner.nextLine();

        Usuario nuevoUsuario = new Usuario(nombreUsuario, contraseña, nombre, saldo);
        nuevoUsuario.guardarDatos();

        usuarios.put(nombreUsuario, nuevoUsuario);

        System.out.println("¡Usuario creado exitosamente! Ahora puedes iniciar sesión con tus credenciales.");
    }

    private static void eliminarUsuario() {
        System.out.println("Por favor, ingresa el nombre de usuario del usuario que deseas eliminar:");
        String nombreUsuario = scanner.nextLine();

        if (usuarios.containsKey(nombreUsuario)) {
            usuarios.remove(nombreUsuario);
            System.out.println("Usuario eliminado exitosamente.");
        } else {
            System.out.println("El usuario especificado no existe. Verifica el nombre de usuario e intenta nuevamente.");
        }
    }

    private static void enviarDinero(Usuario remitente) {
        System.out.println("Por favor, ingresa el nombre de usuario del destinatario:");
        String nombreDestinatario = scanner.nextLine();

        if (usuarios.containsKey(nombreDestinatario)) {
            Usuario destinatario = usuarios.get(nombreDestinatario);

            System.out.println("Por favor, ingresa la cantidad a enviar:");
            double cantidad = scanner.nextDouble();
            scanner.nextLine();

            if (remitente.getSaldo() >= cantidad) {
                remitente.setSaldo(remitente.getSaldo() - cantidad);
                destinatario.setSaldo(destinatario.getSaldo() + cantidad);
                System.out.println("¡Transferencia exitosa!");
                System.out.println("Saldo actual de " + remitente.getNombre() + ": $" + remitente.getSaldo());
                System.out.println("Saldo actual de " + destinatario.getNombre() + ": $" + destinatario.getSaldo());
            } else {
                System.out.println("No tienes suficiente saldo para realizar la transferencia.");
            }
        } else {
            System.out.println("El usuario especificado no existe. Verifica el nombre de usuario e intenta nuevamente.");
        }
    }
}
