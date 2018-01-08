package concesionariojava;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class ConectorSQLITE {
    private final String nombrebd;//Será una constante, solamente se inicializará una vez
    private Connection conexion;//No se inicializa en el constructor, aunque podría
    public Statement consulta;//El mecanismo que me permite ejecutar las SQL, lo hacemos público para poder hacer consultas libres desde otra clase
    //Realmente no se debería hacer así, se deberían hacer métodos desde esta clase, pero esto queda fuera de nuestra asignatura de DI
    
    //Constructor
    public ConectorSQLITE(String bd) {
        this.nombrebd = bd;
    }
    
    public void connect(){
        //Método de Conexión a la BBDD
        try {
            Class.forName("org.sqlite.JDBC");//Comprobamos que existe el driver SQLITE-JDBC
        }catch (ClassNotFoundException e) {
            System.out.println("ERROR "+e.getMessage());
        }
        
        try {
            this.conexion = DriverManager.getConnection("jdbc:sqlite:"+this.nombrebd);//Inicializa Conexión
            //Si no existe el fichero de la BBDD, la crea pero vacío
            this.consulta = conexion.createStatement();//Inicializa la sentencia(Statement) que permite realizar consultas SQL
            String query = "select * from sqlite_master where name='Plato'";
            ResultSet rs = this.consulta.executeQuery(query);
            if (rs.next()==false){//Si no existe la tabla la crea
                this.creaBBDD();
            }
            System.out.println("Base de Datos OK");//Si llega aquí es que todo está correcto
        }catch (SQLException e) {
            System.out.println("ERROR "+e.getMessage());
            this.creaBBDD();
        }
    }
    private void creaBBDD(){
        //Crea la BBDD de no existir
        try{
                System.out.println("Creando Tabla");
                String creatabla;
                creatabla = "CREATE TABLE Plato ("+
                    "platoID  INTEGER  PRIMARY KEY,"+
                    "nombre   VARCHAR(60),"+
                    "tipo     CHECK(tipo IN ('primero', 'segundo', 'postre', 'entrante')),"+
                    "calorias INTEGER,"+
                    "precio   DECIMAL(5, 2),"+
                    "img BLOB)";
                this.consulta.executeUpdate(creatabla);
                System.out.println("BBDD Creada");//Si llega aqui es que ha creado la BBDD
                this.consulta.executeUpdate("INSERT INTO Plato VALUES (1, 'Plato de Prueba', 'primero', 0, 0, NULL )");
                                
        }catch (SQLException e){
             System.out.println("ERROR "+e.getMessage());   
        }
        
    }

    public void ReiniciaBBDD(){
        // Borra la tabla de Platos y vuelve a crear la 
        try{
                System.out.println("Borrando Tabla");
                String borratabla="DROP Table Plato";
                this.consulta.executeUpdate(borratabla);
                System.out.println("Tabla Borrada");//Si llega aqui es que ha creado la BBDD
                this.creaBBDD();
        }catch (SQLException e){
             System.out.println("ERROR "+e.getMessage());   
        }
     }   
    public Connection dameconexion(){
         return conexion;
    }   
    
    public void close(){
            try {
              this.conexion.close();
            } catch (SQLException e) {
                System.out.println("ERROR "+e.getMessage());
            }
     }


}
