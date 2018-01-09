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
            String query = "select * from sqlite_master where name='Coche'";
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
            System.out.println("Creando Tablas");
            String creatablaCoche;
            creatablaCoche = "CREATE TABLE Coche ("+
                "N_Bastidor     TEXT  PRIMARY KEY,"+
                "Marca      TEXT,"+
                "Modelo     TEXT,"+
                "Motor      TEXT,"+
                "CV         INTEGER,"+
                "Tipo       TEXT,"+
                "Color      TEXT,"+
                "Precio     REAL,"+
                "Img        BLOB)";
            this.consulta.executeUpdate(creatablaCoche);
            this.consulta.executeUpdate("INSERT INTO Coche VALUES ('324AER57G4ED349GX', 'NISSAN', 'PRIMERA', 'GASOLINA', 100, 'TURISMO', 'PLATA', 1000, NULL)");
            
            String creatablaCliente;
            creatablaCliente = "CREATE TABLE Cliente ("+
                "Dni        TEXT  PRIMARY KEY,"+
                "Nombre     TEXT,"+
                "Apellidos      TEXT,"+
                "Telefono       TEXT,"+
                "Domicilio      TEXT)";
            this.consulta.executeUpdate(creatablaCliente);
            this.consulta.executeUpdate("INSERT INTO Cliente VALUES ('05983762J', 'Juan Carlos', 'Expósito Romero', '722256261', 'Poro 3, Torrecampo, Córdoba')");
            
            String creatablaRevision;
            creatablaRevision = "CREATE TABLE Revision ("+
                "N_Revision     INTEGER DEFAULT 1 PRIMARY KEY,"+
                "Fecha      TEXT,"+
                "Frenos     TEXT,"+
                "Aceite     TEXT,"+
                "Filtro     TEXT,"+
                "N_Bastidor     TEXT  REFERENCES Coche(N_Bastidor) "+
                    "ON DELETE CASCADE ON UPDATE CASCADE)";
            this.consulta.executeUpdate(creatablaRevision);
            this.consulta.executeUpdate("INSERT INTO Revision VALUES (1, '08/01/2018', 'Sí', 'No', 'Sí', '324AER57G4ED349GX')");
            
            String creatablaVenta;
            creatablaVenta = "CREATE TABLE Venta ("+
                "N_Bastidor     TEXT  REFERENCES Coche(N_Bastidor) "+
                    "ON DELETE CASCADE ON UPDATE CASCADE,"+
                "Dni        TEXT  REFERENCES Cliente(Dni) "+
                    "ON DELETE CASCADE ON UPDATE CASCADE,"+
                "Fecha      TEXT,"+
                "Precio     REAL,"+
                "PRIMARY KEY(N_Bastidor,Dni))";
            this.consulta.executeUpdate(creatablaVenta);
            this.consulta.executeUpdate("INSERT INTO Venta VALUES ('324AER57G4ED349GX', '05983762J', '08/01/2018', 1000)");
            
            this.consulta.execute("PRAGMA foreign_keys = ON");
            
            System.out.println("BBDD Creada");//Si llega aqui es que ha creado la BBDD
        }catch (SQLException e){
            System.out.println("ERROR "+e.getMessage());
        }
    }

    public void ReiniciaBBDD(){
        // Borra la tabla de Platos y vuelve a crear la 
        try{
            System.out.println("Borrando Tablas");
            this.consulta.executeUpdate("DROP Table Venta");
            this.consulta.executeUpdate("DROP Table Revision");
            this.consulta.executeUpdate("DROP Table Cliente");
            this.consulta.executeUpdate("DROP Table Coche");
            System.out.println("Tablas Borradas");//Si llega aqui es que ha eliminado la BBDD
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
