package concesionariojava;

import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

public class Main extends javax.swing.JFrame {
    private final ConectorSQLITE con;
    private byte[] imagenblob;
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private Date date = new Date();

    /**
     * Creates new form Main
     */
    public Main() {
        try{
            Splash sp=new Splash("/imagenes/coche.png",new JFrame(), 5000);//5 segundos de fondo, en cuanto la APP aparezca lo tapará
            Thread.sleep(500);//Espera para que se vea el logo antes de pasar a cargar la GUI
        }catch ( InterruptedException e) {
            e.printStackTrace();
        }
        initComponents();
        con = new ConectorSQLITE("concesionario.db");
        con.connect();
        PreparedStatement ps;
        try {
            ps = con.dameconexion().prepareStatement("PRAGMA foreign_keys = 1");
            ps.execute();
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        listarCoches();
        listarCoches2();
        listarRevisiones();
        listarVentas();
        listarClientes();
        listarClientes2();
        listarClientes3();
        rellenarComboMarca();
        rellenarComboMarcaInformes();
        rellenarComboMotor();
        cerrar_app();
    }

    @Override
    public Image getIconImage() {
        Image imagen = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("imagenes/coche.png"));
        return imagen;
    }
    
    private void cerrar_app(){
        //Método que añade el listener para el botón X del JFrame
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);//Esto es para que no lo cierre prematuramente, sino que me deje cerrar la conexión con la BBDD
        this.addWindowListener(new WindowAdapter()
	{
            @Override
            public void windowClosing(WindowEvent e)
            {
                System.out.println("Cerrando mediante botón X");
                con.close();
                System.out.println("Cerrando base de datos: OK");
                System.exit(0);
            }
	});
    }
    
    private void listarCoches(){
        DefaultTableModel modeloCoches = (DefaultTableModel) tablaMain.getModel();
        for(int i=modeloCoches.getRowCount()-1; i>=0; i--) {
            modeloCoches.removeRow(i);
        }
        
        try{
            ResultSet rs;
            PreparedStatement consulta;
            consulta = this.con.dameconexion().prepareStatement("SELECT N_Bastidor,Marca,Modelo,Motor,CV,Tipo,Color,Precio FROM Coche");
            rs = consulta.executeQuery();
            
            while (rs.next()){ 
                modeloCoches.addRow(new Object[]{rs.getString("N_Bastidor"), rs.getString("Marca"), rs.getString("Modelo"), rs.getString("Motor"), rs.getInt("CV"), rs.getString("Tipo"), rs.getString("Color"), rs.getFloat("Precio")});
            }
            rs.close();
            btnVentaNueva.setEnabled(false);
            btnRevisionNueva.setEnabled(false);
            btnCocheModificar.setEnabled(false);
            btnCocheBorrar.setEnabled(false);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    private void listarCochesFiltro(String filtro, String dato){
        DefaultTableModel modeloCoches = (DefaultTableModel) tablaMain.getModel();
        for(int i=modeloCoches.getRowCount()-1; i>=0; i--) {
            modeloCoches.removeRow(i);
        }
        
        try{
            ResultSet rs;
            PreparedStatement consulta;
            consulta = this.con.dameconexion().prepareStatement("SELECT N_Bastidor,Marca,Modelo,Motor,CV,Tipo,Color,Precio FROM Coche WHERE "+filtro+" LIKE '%"+dato+"%';");
            rs = consulta.executeQuery();
            
            while (rs.next()){ 
                modeloCoches.addRow(new Object[]{rs.getString("N_Bastidor"), rs.getString("Marca"), rs.getString("Modelo"), rs.getString("Motor"), rs.getInt("CV"), rs.getString("Tipo"), rs.getString("Color"), Float.toString(rs.getFloat("Precio"))});
            }
            rs.close();
            btnVentaNueva.setEnabled(false);
            btnRevisionNueva.setEnabled(false);
            btnCocheModificar.setEnabled(false);
            btnCocheBorrar.setEnabled(false);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    private void listarCoches2Filtro(String filtro, String dato){
        DefaultTableModel modeloCoches = (DefaultTableModel) jTable3.getModel();
        for(int i=modeloCoches.getRowCount()-1; i>=0; i--) {
            modeloCoches.removeRow(i);
        }
        
        try{
            ResultSet rs;
            PreparedStatement consulta;
            consulta = this.con.dameconexion().prepareStatement("SELECT N_Bastidor,Marca,Modelo FROM Coche WHERE "+filtro+" LIKE '%"+dato+"%';");
            rs = consulta.executeQuery();
            
            while (rs.next()){ 
                modeloCoches.addRow(new Object[]{rs.getString("N_Bastidor"), rs.getString("Marca"), rs.getString("Modelo")});
            }
            rs.close();
            btnCargarCocheVentaModificar.setEnabled(false);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    private void listarRevisiones(){
        DefaultTableModel modeloRevisiones = (DefaultTableModel) tablaRevisiones.getModel();
        for(int i=modeloRevisiones.getRowCount()-1; i>=0; i--){
            modeloRevisiones.removeRow(i);
        }
        
        try {
            ResultSet rs;
            PreparedStatement consulta;
            consulta = this.con.dameconexion().prepareStatement("SELECT N_Revision FROM Revision");
            rs = consulta.executeQuery();
            
            while(rs.next()){
                modeloRevisiones.addRow(new Object[]{rs.getInt("N_Revision")});
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void listarVentas(){
        DefaultTableModel modeloVentas = (DefaultTableModel) tablaVentas.getModel();
        for(int i=modeloVentas.getRowCount()-1; i>=0; i--){
            modeloVentas.removeRow(i);
        }
        
        try {
            ResultSet rs;
            PreparedStatement consulta;
            consulta = this.con.dameconexion().prepareStatement("SELECT N_Bastidor,Dni FROM Venta");
            rs = consulta.executeQuery();
            
            while(rs.next()){
                modeloVentas.addRow(new Object[]{rs.getString("N_Bastidor"),rs.getString("Dni")});
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void listarClientes(){
        DefaultTableModel modeloClientes = (DefaultTableModel) tablaClientes.getModel();
        for(int i=modeloClientes.getRowCount()-1; i>=0; i--){
            modeloClientes.removeRow(i);
        }
        
        try {
            ResultSet rs;
            PreparedStatement consulta;
            consulta = this.con.dameconexion().prepareStatement("SELECT Dni FROM Cliente");
            rs = consulta.executeQuery();
            
            while(rs.next()){
                modeloClientes.addRow(new Object[]{rs.getString("Dni")});
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void listarClientes2(){
        DefaultTableModel modeloClientes = (DefaultTableModel) jTable1.getModel();
        for(int i=modeloClientes.getRowCount()-1; i>=0; i--){
            modeloClientes.removeRow(i);
        }
        
        try {
            ResultSet rs;
            PreparedStatement consulta;
            consulta = this.con.dameconexion().prepareStatement("SELECT Dni,Nombre,Apellidos FROM Cliente");
            rs = consulta.executeQuery();
            
            while(rs.next()){
                modeloClientes.addRow(new Object[]{rs.getString("Dni"),rs.getString("Nombre"),rs.getString("Apellidos")});
            }
            rs.close();
            btnCargarClienteVentaNueva.setEnabled(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void listarClientes2Filtro(String filtro, String dato){
        DefaultTableModel modeloClientes = (DefaultTableModel) jTable1.getModel();
        for(int i=modeloClientes.getRowCount()-1; i>=0; i--){
            modeloClientes.removeRow(i);
        }
        
        try {
            ResultSet rs;
            PreparedStatement consulta;
            consulta = this.con.dameconexion().prepareStatement("SELECT Dni,Nombre,Apellidos FROM Cliente WHERE "+filtro+" LIKE '%"+dato+"%';");
            rs = consulta.executeQuery();
            
            while(rs.next()){
                modeloClientes.addRow(new Object[]{rs.getString("Dni"),rs.getString("Nombre"),rs.getString("Apellidos")});
            }
            rs.close();
            btnCargarClienteVentaNueva.setEnabled(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void listarClientes3Filtro(String filtro, String dato){
        DefaultTableModel modeloClientes = (DefaultTableModel) jTable2.getModel();
        for(int i=modeloClientes.getRowCount()-1; i>=0; i--){
            modeloClientes.removeRow(i);
        }
        
        try {
            ResultSet rs;
            PreparedStatement consulta;
            consulta = this.con.dameconexion().prepareStatement("SELECT Dni,Nombre,Apellidos FROM Cliente WHERE "+filtro+" LIKE '%"+dato+"%';");
            rs = consulta.executeQuery();
            
            while(rs.next()){
                modeloClientes.addRow(new Object[]{rs.getString("Dni"),rs.getString("Nombre"),rs.getString("Apellidos")});
            }
            rs.close();
            btnCargarClienteVentaModificar.setEnabled(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void listarClientes3(){
        DefaultTableModel modeloClientes = (DefaultTableModel) jTable2.getModel();
        for(int i=modeloClientes.getRowCount()-1; i>=0; i--){
            modeloClientes.removeRow(i);
        }
        
        try {
            ResultSet rs;
            PreparedStatement consulta;
            consulta = this.con.dameconexion().prepareStatement("SELECT Dni,Nombre,Apellidos FROM Cliente");
            rs = consulta.executeQuery();
            
            while(rs.next()){
                modeloClientes.addRow(new Object[]{rs.getString("Dni"),rs.getString("Nombre"),rs.getString("Apellidos")});
            }
            rs.close();
            btnCargarClienteVentaModificar.setEnabled(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void listarCoches2(){
        DefaultTableModel modeloCoches = (DefaultTableModel) jTable3.getModel();
        for(int i=modeloCoches.getRowCount()-1; i>=0; i--) {
            modeloCoches.removeRow(i);
        }
        
        try{
            ResultSet rs;
            PreparedStatement consulta;
            consulta = this.con.dameconexion().prepareStatement("SELECT N_Bastidor,Marca,Modelo FROM Coche");
            rs = consulta.executeQuery();
            
            while (rs.next()){ 
                modeloCoches.addRow(new Object[]{rs.getString("N_Bastidor"), rs.getString("Marca"), rs.getString("Modelo")});
            }
            rs.close();
            btnCargarCocheVentaModificar.setEnabled(false);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    private static BufferedImage resize(BufferedImage img, int newW, int newH) {
        //Una función para reescalar una imagen. Se puede reutilizar tal cual
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage dimg = new BufferedImage(newW, newH, img.getType());
        Graphics2D g = dimg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);
        g.dispose();
        return dimg;
    }
    
    private void insertarCoche(){
        Boolean inserta = true;
        String bastidor = txtBastidorCocheNuevo.getText().trim().toUpperCase();
        String marca = txtMarcaCocheNuevo.getText().toUpperCase();
        String modelo = txtModeloCocheNuevo.getText().toUpperCase();
        String tipo = (String)cbTipoCocheNuevo.getSelectedItem();
        String motor = txtMotorCocheNuevo.getText().toUpperCase();
        int cv = 0;
        String color = txtColorCocheNuevo.getText().toUpperCase();
        float precio = 0;
        
        if(bastidor.equals("") || marca.equals("") || modelo.equals("") || cbTipoCocheNuevo.getSelectedIndex()==0 || motor.equals("") || txtCVCocheNuevo.getText().equals("") || color.equals("") || txtPrecioCocheNuevo.getText().equals("") || this.imagenblob==null){
            JOptionPane.showMessageDialog(null, "Ningún campo debe estar vacío", "Aviso", JOptionPane.WARNING_MESSAGE);
        }else{
            try {
                cv = Integer.parseInt(txtCVCocheNuevo.getText());
            } catch (NumberFormatException e) {
                inserta = false;
                JOptionPane.showMessageDialog(null, "Los CV deben ser un número entero", "Aviso", JOptionPane.WARNING_MESSAGE);
            }

            try {
                precio = Float.parseFloat(txtPrecioCocheNuevo.getText());
            } catch (NumberFormatException e) {
                inserta = false;
                JOptionPane.showMessageDialog(null, "El precio debe ser un número real", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
            
            if(inserta){
                try {
                    PreparedStatement ps;
                    ps=this.con.dameconexion().prepareStatement("INSERT INTO Coche VALUES (?,?,?,?,?,?,?,ROUND(?,2),?);");
                    ps.setString(1,bastidor);
                    ps.setString(2,marca);
                    ps.setString(3,modelo);
                    ps.setString(4,motor);
                    ps.setInt(5,cv);
                    ps.setString(6,tipo);
                    ps.setString(7,color);
                    ps.setFloat(8,precio);
                    ps.setBytes(9,this.imagenblob);
                    ps.executeUpdate();
                    listarCoches();
                    listarCoches2();
                    rellenarComboMarca();
                    rellenarComboMarcaInformes();
                    rellenarComboMotor();
                    JOptionPane.showMessageDialog(null, "Coche nuevo introducido correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);
                    this.aniadirCoche.setVisible(false);
                } catch (SQLException ex) {
                    if(ex.getErrorCode()==19){
                        JOptionPane.showMessageDialog(null, "Ya existe un coche con estos datos", "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        }
    }
    
    private void limpiarTodo(){
        //DESACTIVAR BOTONES DE LAS PESTAÑAS
        btnVentaNueva.setEnabled(false);
        btnRevisionNueva.setEnabled(false);
        btnCocheModificar.setEnabled(false);
        btnCocheBorrar.setEnabled(false);
        btnVentaModificar.setEnabled(false);
        btnVentaBorrar.setEnabled(false);
        btnRevisionModificar.setEnabled(false);
        btnRevisionBorrar.setEnabled(false);
        btnClienteModificar.setEnabled(false);
        btnClienteBorrar.setEnabled(false);
        
        //LIMPIAR ETIQUETAS DE LAS PESTAÑAS
        //REVISIONES
        lblNumeroRevisionMain.setText("");
        lblFechaRevisionMain.setText("");
        lblBastidorRevisionMain.setText("");
        lblMarcaRevisionMain.setText("");
        lblModeloRevisionMain.setText("");
        lblFiltroRevisionMain.setText("");
        lblFrenosRevisionMain.setText("");
        lblAceiteRevisionMain.setText("");
        pImagenRevisionMain.removeAll();
        //VENTAS
        lblFechaVentaMain.setText("");
        lblNombreVentaMain.setText("");
        lblApellidosVentaMain.setText("");
        lblCocheVentaMain.setText("");
        lblDniVentaMain.setText("");
        lblPrecioVentaMain.setText("");
        //CLIENTES
        lblDniClienteMain.setText("");
        lblNombreClienteMain.setText("");
        lblApellidosClienteMain.setText("");
        lblTelefonoClienteMain.setText("");
        lblDireccionClienteMain.setText("");
        
        //ELIMINAR SELECCIÓN DE LAS TABLAS
        tablaMain.clearSelection();
        tablaRevisiones.clearSelection();
        tablaVentas.clearSelection();
        tablaClientes.clearSelection();
    }
    
    private void rellenarComboMarca(){
        cbMarcaMain.removeAllItems();
        cbMarcaMain.addItem("");
        try {
            ResultSet rs;
            PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT DISTINCT Marca FROM Coche;");
            rs = ps.executeQuery();
            
            while(rs.next()){
                cbMarcaMain.addItem(rs.getString("Marca"));
            }
            rs.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    private void rellenarComboMarcaInformes(){
        cbMarcasInforme.removeAllItems();
        cbMarcasInforme.addItem("");
        try {
            ResultSet rs;
            PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT DISTINCT Marca FROM Coche;");
            rs = ps.executeQuery();
            
            while(rs.next()){
                cbMarcasInforme.addItem(rs.getString("Marca"));
            }
            rs.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    private void rellenarComboMotor(){
        cbMotorMain.removeAllItems();
        cbMotorMain.addItem("");
        try {
            ResultSet rs;
            PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT DISTINCT Motor FROM Coche;");
            rs = ps.executeQuery();
            
            while(rs.next()){
                cbMotorMain.addItem(rs.getString("Motor"));
            }
            rs.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    private void modificarCoche(){
        modificarCoche.setLocationRelativeTo(null);
        try {
            String bastidor = (String)tablaMain.getValueAt(tablaMain.getSelectedRow(), 0);
            ResultSet rs;
            PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT * FROM Coche WHERE N_Bastidor = ?;");
            ps.setString(1,bastidor);
            rs = ps.executeQuery();
            txtBastidorCocheModificar.setText(rs.getString("N_Bastidor"));
            txtMarcaCocheModificar.setText(rs.getString("Marca"));
            txtModeloCocheModificar.setText(rs.getString("Modelo"));
            
            String tipo = rs.getString("Tipo");
            ComboBoxModel cbm = cbTipoCocheModificar.getModel();
            for(int i=0; i<cbm.getSize(); i++){
                if(tipo.equals((String)cbm.getElementAt(i))){
                    cbm.setSelectedItem(tipo);
                }
            }
            
            txtMotorCocheModificar.setText(rs.getString("Motor"));
            txtCVCocheModificar.setText(Integer.toString(rs.getInt("CV")));
            txtColorCocheModificar.setText(rs.getString("Color"));
            txtPrecioCocheModificar.setText(Float.toString(rs.getFloat("Precio")));

            pImagenCocheModificar.removeAll();
            JPanel PanelImagen = new JPanel();
            byte[] imagen_bytes=rs.getBytes("Img");//Toma el campo img de la base de datos en forma de bytes 

            JLabel picLabel;
            picLabel = new JLabel(new ImageIcon(imagen_bytes));//Se reescala
            PanelImagen.setBounds(5, 5, 180, 150);
            PanelImagen.add(picLabel);//Se añade la imagen al Panel
            PanelImagen.setName("IMAGEN"); //Se añade un NAME para luego poder buscarlo entre todos los componentes
            pImagenCocheModificar.add(PanelImagen);//Se añade el Panel de la Imagen
            modificarCoche.revalidate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        btnImagenCocheModificar.setEnabled(false);
        modificarCoche.setVisible(true);
    }
    
    private void eliminarCoche(){
        int opcion = JOptionPane.showConfirmDialog(null, "¿Desea eliminar el coche seleccionado?", "Eliminar coche", JOptionPane.OK_CANCEL_OPTION);
        //System.out.println(opcion);
        if(opcion==0){
            try {
                PreparedStatement ps = this.con.dameconexion().prepareStatement("DELETE FROM Coche WHERE N_Bastidor = ?;");
                ps.setString(1, (String)tablaMain.getValueAt(tablaMain.getSelectedRow(), 0));
                ps.executeUpdate();
                listarCoches();
                listarCoches2();
                listarRevisiones();
                listarVentas();
                rellenarComboMarca();
                rellenarComboMarcaInformes();
                rellenarComboMotor();
                JOptionPane.showMessageDialog(null, "Coche eliminado correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void modificarRevision(){
        modificarRevision.setLocationRelativeTo(null);
        try {
            ResultSet rs;
            PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT r.N_Revision,r.Fecha,c.Marca,c.Modelo,r.N_Bastidor,r.Frenos,r.Filtro,r.Aceite FROM Revision AS r, Coche AS c WHERE r.N_Revision=? AND r.N_Bastidor=c.N_Bastidor;");
            ps.setInt(1, (int)tablaRevisiones.getValueAt(tablaRevisiones.getSelectedRow(), 0));
            rs = ps.executeQuery();
            lblNumeroRevisionModificar.setText(Integer.toString(rs.getInt("N_Revision")));
            lblFechaRevisionModificar.setText(rs.getString("Fecha"));
            lblMarcaRevisionModificar.setText(rs.getString("Marca"));
            lblModeloRevisionModificar.setText(rs.getString("Modelo"));
            lblBastidorRevisionModificar.setText(rs.getString("N_Bastidor"));
            String frenos = rs.getString("Frenos");
            String filtro = rs.getString("Filtro");
            String aceite = rs.getString("Aceite");
            
            if(frenos.equals("Sí")){
                chkFrenosRevisionModificar.setSelected(true);
            }else{
                chkFrenosRevisionModificar.setSelected(false);
            }
            
            if(filtro.equals("Sí")){
                chkFiltroRevisionModificar.setSelected(true);
            }else{
                chkFiltroRevisionModificar.setSelected(false);
            }
            
            if(aceite.equals("Sí")){
                chkAceiteRevisionModificar.setSelected(true);
            }else{
                chkAceiteRevisionModificar.setSelected(false);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        modificarRevision.setVisible(true);
    }
    
    private void eliminarRevision(){
        int opcion = JOptionPane.showConfirmDialog(null, "¿Desea eliminar la revisión seleccionada?", "Eliminar revision", JOptionPane.OK_CANCEL_OPTION);
        if(opcion==0){
            try {
                PreparedStatement ps = this.con.dameconexion().prepareStatement("DELETE FROM Revision WHERE N_Revision = ?;");
                ps.setInt(1,(int)tablaRevisiones.getValueAt(tablaRevisiones.getSelectedRow(), 0));
                ps.executeUpdate();
                listarRevisiones();
                lblNumeroRevisionMain.setText("");
                lblFechaRevisionMain.setText("");
                lblBastidorRevisionMain.setText("");
                lblMarcaRevisionMain.setText("");
                lblModeloRevisionMain.setText("");
                lblFiltroRevisionMain.setText("");
                lblFrenosRevisionMain.setText("");
                lblAceiteRevisionMain.setText("");
                pImagenRevisionMain.removeAll();
                JOptionPane.showMessageDialog(null, "Revisión eliminada correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }
    
    private void modificarVenta(){
        String[] options = new String[2];
        options[0] = "Cliente";
        options[1] = "Coche";
        int respuesta = JOptionPane.showOptionDialog(null, "¿Qué desea modificar?", "Modificar venta", 0, JOptionPane.QUESTION_MESSAGE, null, options, null);
        //System.out.println(respuesta);
        if(respuesta==0){//MODIFICAR CLIENTE
            modificarVenta.setLocationRelativeTo(null);
            btnBuscarCoche.setEnabled(false);
            btnBuscarCliente.setEnabled(true);
            try {
                ResultSet rs;
                PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT * FROM Venta WHERE N_Bastidor = ? AND Dni = ?;");
                ps.setString(1,(String)tablaVentas.getValueAt(tablaVentas.getSelectedRow(), 0));
                ps.setString(2,(String)tablaVentas.getValueAt(tablaVentas.getSelectedRow(), 1));
                rs = ps.executeQuery();
                jDateChooser.setDate(dateFormat.parse(rs.getString("Fecha")));
                txtBastidorVentaModificar.setText(rs.getString("N_Bastidor"));
                txtDniVentaModificar.setText(rs.getString("Dni"));
                txtPrecioVentaModificar.setText(rs.getString("Precio"));
            } catch (SQLException e) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
            } catch (ParseException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            modificarVenta.setVisible(true);
        }else{//MODIFICAR COCHE
            modificarVenta.setLocationRelativeTo(null);
            btnBuscarCliente.setEnabled(false);
            btnBuscarCoche.setEnabled(true);
            try {
                ResultSet rs;
                PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT * FROM Venta WHERE N_Bastidor = ? AND Dni = ?;");
                ps.setString(1,(String)tablaVentas.getValueAt(tablaVentas.getSelectedRow(), 0));
                ps.setString(2,(String)tablaVentas.getValueAt(tablaVentas.getSelectedRow(), 1));
                rs = ps.executeQuery();
                jDateChooser.setDate(dateFormat.parse(rs.getString("Fecha")));
                txtBastidorVentaModificar.setText(rs.getString("N_Bastidor"));
                txtDniVentaModificar.setText(rs.getString("Dni"));
                txtPrecioVentaModificar.setText(rs.getString("Precio"));
            } catch (SQLException e) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
            } catch (ParseException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            modificarVenta.setVisible(true);
        }
    }
    
    private void eliminarVenta(){
        int opcion = JOptionPane.showConfirmDialog(null, "¿Desea eliminar la venta seleccionada?", "Eliminar venta", JOptionPane.OK_CANCEL_OPTION);
        if(opcion==0){
            try {
                PreparedStatement ps = this.con.dameconexion().prepareStatement("DELETE FROM Venta WHERE N_Bastidor = ? AND Dni = ?;");
                ps.setString(1,(String)tablaVentas.getValueAt(tablaVentas.getSelectedRow(), 0));
                ps.setString(2,(String)tablaVentas.getValueAt(tablaVentas.getSelectedRow(), 1));
                ps.executeUpdate();
                listarVentas();
                lblFechaVentaMain.setText("");
                lblNombreVentaMain.setText("");
                lblApellidosVentaMain.setText("");
                lblCocheVentaMain.setText("");
                lblDniVentaMain.setText("");
                lblPrecioVentaMain.setText("");
                JOptionPane.showMessageDialog(null, "Venta eliminada correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }
    
    private void modificarCliente(){
        modificarCliente.setLocationRelativeTo(null);
        try {
            ResultSet rs;
            PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT * FROM Cliente WHERE Dni = ?;");
            ps.setString(1,(String)tablaClientes.getValueAt(tablaClientes.getSelectedRow(), 0));
            rs = ps.executeQuery();
            txtDniClienteModificar.setText(rs.getString("Dni"));
            txtNombreClienteModificar.setText(rs.getString("Nombre"));
            txtApellidosClienteModificar.setText(rs.getString("Apellidos"));
            txtTelefonoClienteModificar.setText(rs.getString("Telefono"));
            txtDireccionClienteModificar.setText(rs.getString("Domicilio"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        modificarCliente.setVisible(true);
    }
    
    private void eliminarCliente(){
        int opcion = JOptionPane.showConfirmDialog(null, "¿Desea eliminar el cliente seleccionado?", "Eliminar cliente", JOptionPane.OK_CANCEL_OPTION);
        if(opcion==0){
            try {
                PreparedStatement ps = this.con.dameconexion().prepareStatement("DELETE FROM Cliente WHERE Dni=?;");
                ps.setString(1,(String)tablaClientes.getValueAt(tablaClientes.getSelectedRow(), 0));
                ps.executeUpdate();
                listarClientes();
                listarClientes2();
                listarVentas();
                lblDniClienteMain.setText("");
                lblNombreClienteMain.setText("");
                lblApellidosClienteMain.setText("");
                lblTelefonoClienteMain.setText("");
                lblDireccionClienteMain.setText("");
                JOptionPane.showMessageDialog(null, "Cliente eliminado correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }
    
    private void cargarCliente1(){
        buscarClienteVentaNueva.setVisible(false);
        lblFechaVentaNueva.setText(dateFormat.format(date));
        String dni = (String)jTable1.getValueAt(jTable1.getSelectedRow(), 0);
        try {
            DefaultTableModel modelo = (DefaultTableModel) tablaMain.getModel();
            txtBastidorVentaNueva.setText((String)modelo.getValueAt(tablaMain.getSelectedRow(), 0));
            txtMarcaVentaNueva.setText((String)modelo.getValueAt(tablaMain.getSelectedRow(), 1));
            txtModeloVentaNueva.setText((String)modelo.getValueAt(tablaMain.getSelectedRow(), 2));
            lblPrecioVentaNueva.setText(Float.toString((Float)modelo.getValueAt(tablaMain.getSelectedRow(), 7)));
            ResultSet rs;
            PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT * FROM Cliente WHERE Dni=?");
            ps.setString(1, dni);
            rs = ps.executeQuery();
            txtNombreVentaNueva.setText(rs.getString("Nombre"));
            txtNombreVentaNueva.setEditable(false);
            txtApellidosVentaNueva.setText(rs.getString("Apellidos"));
            txtApellidosVentaNueva.setEditable(false);
            txtDniVentaNueva.setText(rs.getString("Dni"));
            txtDniVentaNueva.setEditable(false);
            txtTelefonoVentaNueva.setText(rs.getString("Telefono"));
            txtTelefonoVentaNueva.setEditable(false);
            txtDireccionVentaNueva.setText(rs.getString("Domicilio"));
            txtDireccionVentaNueva.setEditable(false);
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        aniadirVenta.setLocationRelativeTo(null);
        aniadirVenta.setVisible(true);
    }
    
    private void cargarCliente2(){
        txtDniVentaModificar.setText((String)jTable2.getValueAt(jTable2.getSelectedRow(), 0));
        buscarClienteVentaModificar.setVisible(false);
    }
    
    private void cargarCoche(){
        txtBastidorVentaModificar.setText((String)jTable3.getValueAt(jTable3.getSelectedRow(), 0));
        try {
            ResultSet rs;
            PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT Precio FROM Coche WHERE N_Bastidor=?;");
            ps.setString(1,(String)jTable3.getValueAt(jTable3.getSelectedRow(), 0));
            rs = ps.executeQuery();
            txtPrecioVentaModificar.setText(Float.toString(rs.getFloat("Precio")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        buscarCocheVentaModificar.setVisible(false);
    }
    
    private void genera_informe(String tipo, String nombre){
       Connection conBD = con.dameconexion();
       InputStream jasperStream = (InputStream) getClass().getResourceAsStream(nombre);
       Map parametros = new HashMap();
       parametros.put("CONDICION", tipo);
       try {
            JasperReport informeCompilado = JasperCompileManager.compileReport(jasperStream);//Compila
            JasperPrint informeRelleno = JasperFillManager.fillReport(informeCompilado, parametros, conBD );//Rellena
            JasperViewer.viewReport(informeRelleno, false);
            String tipoInforme = nombre.replaceAll("informe","");
            tipoInforme = tipoInforme.replaceAll(".jrxml", "");
            if(!tipo.equals("%")){
                tipoInforme = tipoInforme+" "+tipo;
            }
            //System.out.println(tipoInforme);
            JasperExportManager.exportReportToPdfFile(informeRelleno, "./Informe "+tipoInforme+".pdf");
        } catch (JRException ex) {
            System.out.println(ex.getMessage());
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        aniadirCoche = new javax.swing.JDialog();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        txtModeloCocheNuevo = new javax.swing.JTextField();
        jLabel46 = new javax.swing.JLabel();
        txtMotorCocheNuevo = new javax.swing.JTextField();
        jLabel47 = new javax.swing.JLabel();
        txtColorCocheNuevo = new javax.swing.JTextField();
        jLabel48 = new javax.swing.JLabel();
        txtMarcaCocheNuevo = new javax.swing.JTextField();
        jLabel49 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        btnImagenCocheNuevo = new javax.swing.JButton();
        pImagenCocheNuevo = new javax.swing.JPanel();
        btnCancelarCocheNuevo = new javax.swing.JButton();
        btnAceptarCocheNuevo = new javax.swing.JButton();
        txtBastidorCocheNuevo = new javax.swing.JFormattedTextField();
        txtCVCocheNuevo = new javax.swing.JFormattedTextField();
        txtPrecioCocheNuevo = new javax.swing.JFormattedTextField();
        cbTipoCocheNuevo = new javax.swing.JComboBox<>();
        modificarCoche = new javax.swing.JDialog();
        jLabel53 = new javax.swing.JLabel();
        txtBastidorCocheModificar = new javax.swing.JTextField();
        jLabel54 = new javax.swing.JLabel();
        txtModeloCocheModificar = new javax.swing.JTextField();
        jLabel55 = new javax.swing.JLabel();
        txtMotorCocheModificar = new javax.swing.JTextField();
        jLabel56 = new javax.swing.JLabel();
        txtColorCocheModificar = new javax.swing.JTextField();
        jLabel57 = new javax.swing.JLabel();
        txtMarcaCocheModificar = new javax.swing.JTextField();
        jLabel58 = new javax.swing.JLabel();
        jLabel59 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        jLabel61 = new javax.swing.JLabel();
        btnImagenCocheModificar = new javax.swing.JButton();
        pImagenCocheModificar = new javax.swing.JPanel();
        btnCancelarCocheModificar = new javax.swing.JButton();
        btnAceptarCocheModificar = new javax.swing.JButton();
        txtCVCocheModificar = new javax.swing.JFormattedTextField();
        txtPrecioCocheModificar = new javax.swing.JFormattedTextField();
        cbTipoCocheModificar = new javax.swing.JComboBox<>();
        buscarClienteVentaNueva = new javax.swing.JDialog();
        btnNuevoCliente = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        cbBuscarClienteVentaNueva = new javax.swing.JComboBox<>();
        txtBuscarClienteVentaNueva = new javax.swing.JTextField();
        btnBuscarClienteVentaNueva = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        btnCargarClienteVentaNueva = new javax.swing.JButton();
        buscarClienteVentaModificar = new javax.swing.JDialog();
        cbBuscarClienteVentaModificar = new javax.swing.JComboBox<>();
        txtBuscarClienteVentaModificar = new javax.swing.JTextField();
        btnBuscarClienteVentaModificar = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        btnCargarClienteVentaModificar = new javax.swing.JButton();
        buscarCocheVentaModificar = new javax.swing.JDialog();
        cbBuscarCocheVentaModificar = new javax.swing.JComboBox<>();
        txtBuscarCocheVentaModificar = new javax.swing.JTextField();
        btnBuscarCocheVentaModificar = new javax.swing.JButton();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        btnCargarCocheVentaModificar = new javax.swing.JButton();
        aniadirVenta = new javax.swing.JDialog();
        jLabel16 = new javax.swing.JLabel();
        lblFechaVentaNueva = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        txtNombreVentaNueva = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        txtApellidosVentaNueva = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        txtDireccionVentaNueva = new javax.swing.JTextField();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        txtBastidorVentaNueva = new javax.swing.JTextField();
        jLabel36 = new javax.swing.JLabel();
        txtMarcaVentaNueva = new javax.swing.JTextField();
        jLabel37 = new javax.swing.JLabel();
        txtModeloVentaNueva = new javax.swing.JTextField();
        jLabel38 = new javax.swing.JLabel();
        lblPrecioVentaNueva = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        btnCancelarVentaNueva = new javax.swing.JButton();
        btnAceptarVentaNueva = new javax.swing.JButton();
        txtDniVentaNueva = new javax.swing.JFormattedTextField();
        txtTelefonoVentaNueva = new javax.swing.JFormattedTextField();
        aniadirRevision = new javax.swing.JDialog();
        jLabel23 = new javax.swing.JLabel();
        lblNumeroRevisionNueva = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        lblFechaRevisionNueva = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jLabel62 = new javax.swing.JLabel();
        lblMarcaRevisionNueva = new javax.swing.JLabel();
        jLabel64 = new javax.swing.JLabel();
        lblModeloRevisionNueva = new javax.swing.JLabel();
        jLabel67 = new javax.swing.JLabel();
        lblBastidorRevisionNueva = new javax.swing.JLabel();
        chkFrenosRevisionNueva = new javax.swing.JCheckBox();
        chkFiltroRevisionNueva = new javax.swing.JCheckBox();
        chkAceiteRevisionNueva = new javax.swing.JCheckBox();
        btnCancelarRevisionNueva = new javax.swing.JButton();
        btnAceptarRevisionNueva = new javax.swing.JButton();
        modificarRevision = new javax.swing.JDialog();
        jLabel39 = new javax.swing.JLabel();
        lblNumeroRevisionModificar = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        lblFechaRevisionModificar = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        jLabel65 = new javax.swing.JLabel();
        lblMarcaRevisionModificar = new javax.swing.JLabel();
        jLabel66 = new javax.swing.JLabel();
        lblModeloRevisionModificar = new javax.swing.JLabel();
        jLabel68 = new javax.swing.JLabel();
        lblBastidorRevisionModificar = new javax.swing.JLabel();
        chkFrenosRevisionModificar = new javax.swing.JCheckBox();
        chkFiltroRevisionModificar = new javax.swing.JCheckBox();
        chkAceiteRevisionModificar = new javax.swing.JCheckBox();
        btnCancelarRevisionModificar = new javax.swing.JButton();
        btnAceptarRevisionModificar = new javax.swing.JButton();
        modificarVenta = new javax.swing.JDialog();
        jLabel69 = new javax.swing.JLabel();
        jLabel70 = new javax.swing.JLabel();
        txtDniVentaModificar = new javax.swing.JTextField();
        jLabel71 = new javax.swing.JLabel();
        txtBastidorVentaModificar = new javax.swing.JTextField();
        jLabel72 = new javax.swing.JLabel();
        txtPrecioVentaModificar = new javax.swing.JTextField();
        btnBuscarCliente = new javax.swing.JButton();
        btnBuscarCoche = new javax.swing.JButton();
        jLabel73 = new javax.swing.JLabel();
        btnCancelarVentaModificar = new javax.swing.JButton();
        btnAceptarVentaModificar = new javax.swing.JButton();
        jDateChooser = new com.toedter.calendar.JDateChooser();
        modificarCliente = new javax.swing.JDialog();
        jLabel74 = new javax.swing.JLabel();
        jLabel75 = new javax.swing.JLabel();
        txtNombreClienteModificar = new javax.swing.JTextField();
        txtApellidosClienteModificar = new javax.swing.JTextField();
        jLabel76 = new javax.swing.JLabel();
        txtDniClienteModificar = new javax.swing.JTextField();
        jLabel77 = new javax.swing.JLabel();
        jLabel78 = new javax.swing.JLabel();
        txtDireccionClienteModificar = new javax.swing.JTextField();
        btnCancelarClienteModificar = new javax.swing.JButton();
        btnAceptarClienteModificar = new javax.swing.JButton();
        txtTelefonoClienteModificar = new javax.swing.JFormattedTextField();
        acercaDe = new javax.swing.JDialog();
        jLabel79 = new javax.swing.JLabel();
        jFileChooser1 = new javax.swing.JFileChooser();
        popupCoche = new javax.swing.JPopupMenu();
        nuevocoche = new javax.swing.JMenuItem();
        editarcoche = new javax.swing.JMenuItem();
        borrarcoche = new javax.swing.JMenuItem();
        popupRevision = new javax.swing.JPopupMenu();
        editarRevision = new javax.swing.JMenuItem();
        borrarRevision = new javax.swing.JMenuItem();
        popupVenta = new javax.swing.JPopupMenu();
        editarVenta = new javax.swing.JMenuItem();
        borrarVenta = new javax.swing.JMenuItem();
        popupCliente = new javax.swing.JPopupMenu();
        editarCliente = new javax.swing.JMenuItem();
        borrarCliente = new javax.swing.JMenuItem();
        seleccionMarca = new javax.swing.JDialog();
        cbMarcasInforme = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        btnAceptarInforme = new javax.swing.JButton();
        btnCancelarInforme = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        coches = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cbMarcaMain = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        cbMotorMain = new javax.swing.JComboBox<>();
        cbBuscarMain = new javax.swing.JComboBox<>();
        jSeparator1 = new javax.swing.JSeparator();
        txtBuscarMain = new javax.swing.JTextField();
        btnBuscarMain = new javax.swing.JButton();
        btnReiniciarBusqueda = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaMain = new javax.swing.JTable();
        btnVentaNueva = new javax.swing.JButton();
        btnRevisionNueva = new javax.swing.JButton();
        btnCocheBorrar = new javax.swing.JButton();
        btnCocheModificar = new javax.swing.JButton();
        btnCocheNuevo = new javax.swing.JButton();
        revisiones = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        lblNumeroRevisionMain = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        lblFechaRevisionMain = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        lblBastidorRevisionMain = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        lblMarcaRevisionMain = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        lblModeloRevisionMain = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        lblFrenosRevisionMain = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        lblFiltroRevisionMain = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        lblAceiteRevisionMain = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        pImagenRevisionMain = new javax.swing.JPanel();
        btnRevisionBorrar = new javax.swing.JButton();
        btnRevisionModificar = new javax.swing.JButton();
        jScrollPane9 = new javax.swing.JScrollPane();
        tablaRevisiones = new javax.swing.JTable();
        ventas = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tablaVentas = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        lblFechaVentaMain = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        lblNombreVentaMain = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        lblApellidosVentaMain = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        lblDniVentaMain = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        lblCocheVentaMain = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        lblPrecioVentaMain = new javax.swing.JLabel();
        btnVentaBorrar = new javax.swing.JButton();
        btnVentaModificar = new javax.swing.JButton();
        clientes = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        lblDniClienteMain = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        lblNombreClienteMain = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        lblApellidosClienteMain = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        lblTelefonoClienteMain = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        lblDireccionClienteMain = new javax.swing.JLabel();
        btnClienteBorrar = new javax.swing.JButton();
        btnClienteModificar = new javax.swing.JButton();
        jScrollPane10 = new javax.swing.JScrollPane();
        tablaClientes = new javax.swing.JTable();
        menu = new javax.swing.JMenuBar();
        archivo = new javax.swing.JMenu();
        reiniciarbd = new javax.swing.JMenuItem();
        salir = new javax.swing.JMenuItem();
        informes = new javax.swing.JMenu();
        jMenu1 = new javax.swing.JMenu();
        generaInformeCoches = new javax.swing.JMenuItem();
        generaInformeCochesMarca = new javax.swing.JMenuItem();
        generaInformeRevisiones = new javax.swing.JMenuItem();
        generaInformeVentas = new javax.swing.JMenuItem();
        generaInformeClientes = new javax.swing.JMenuItem();
        ayuda = new javax.swing.JMenu();
        acercade = new javax.swing.JMenuItem();
        manual = new javax.swing.JMenuItem();

        aniadirCoche.setTitle("Añadir coche");
        aniadirCoche.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        aniadirCoche.setModal(true);
        aniadirCoche.setResizable(false);
        aniadirCoche.setSize(new java.awt.Dimension(535, 350));

        jLabel44.setText("Nº BASTIDOR");

        jLabel45.setText("MODELO");

        jLabel46.setText("MOTOR");

        jLabel47.setText("COLOR");

        jLabel48.setText("MARCA");

        jLabel49.setText("TIPO");

        jLabel50.setText("CV");

        jLabel51.setText("PRECIO");

        jLabel52.setText("IMAGEN");

        btnImagenCocheNuevo.setMnemonic('S');
        btnImagenCocheNuevo.setText("Seleccionar imagen");
        btnImagenCocheNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImagenCocheNuevoActionPerformed(evt);
            }
        });

        pImagenCocheNuevo.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout pImagenCocheNuevoLayout = new javax.swing.GroupLayout(pImagenCocheNuevo);
        pImagenCocheNuevo.setLayout(pImagenCocheNuevoLayout);
        pImagenCocheNuevoLayout.setHorizontalGroup(
            pImagenCocheNuevoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 191, Short.MAX_VALUE)
        );
        pImagenCocheNuevoLayout.setVerticalGroup(
            pImagenCocheNuevoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 160, Short.MAX_VALUE)
        );

        btnCancelarCocheNuevo.setMnemonic('C');
        btnCancelarCocheNuevo.setText("Cancelar");
        btnCancelarCocheNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarCocheNuevoActionPerformed(evt);
            }
        });

        btnAceptarCocheNuevo.setMnemonic('A');
        btnAceptarCocheNuevo.setText("Aceptar");
        btnAceptarCocheNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarCocheNuevoActionPerformed(evt);
            }
        });

        try {
            txtBastidorCocheNuevo.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("UUUAAAAAAAA######")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        txtBastidorCocheNuevo.setToolTipText("<html>\n    <p><b>17 caracteres</b></p>\n    <p>3 letras, 8 números o letras y 6 números</p>\n</html>");
        txtBastidorCocheNuevo.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);

        txtCVCocheNuevo.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);

        txtPrecioCocheNuevo.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);

        cbTipoCocheNuevo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { " ", "TURISMO", "SEDÁN", "COMPACTO", "DEPORTIVO", "COMERCIAL", "FAMILIAR", "TODOTERRENO" }));

        javax.swing.GroupLayout aniadirCocheLayout = new javax.swing.GroupLayout(aniadirCoche.getContentPane());
        aniadirCoche.getContentPane().setLayout(aniadirCocheLayout);
        aniadirCocheLayout.setHorizontalGroup(
            aniadirCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aniadirCocheLayout.createSequentialGroup()
                .addGroup(aniadirCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, aniadirCocheLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(aniadirCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(aniadirCocheLayout.createSequentialGroup()
                                .addGroup(aniadirCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel45)
                                    .addComponent(jLabel46))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(aniadirCocheLayout.createSequentialGroup()
                                .addGroup(aniadirCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel44)
                                    .addComponent(jLabel47)
                                    .addGroup(aniadirCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(txtColorCocheNuevo, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                                        .addComponent(txtMotorCocheNuevo, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtModeloCocheNuevo, javax.swing.GroupLayout.Alignment.LEADING))
                                    .addComponent(txtBastidorCocheNuevo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 32, Short.MAX_VALUE)))
                        .addGroup(aniadirCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel48, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel49, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel50, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel51, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtMarcaCocheNuevo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                            .addComponent(txtCVCocheNuevo)
                            .addComponent(txtPrecioCocheNuevo)
                            .addComponent(cbTipoCocheNuevo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(32, 32, 32)
                        .addGroup(aniadirCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel52)
                            .addComponent(btnImagenCocheNuevo)
                            .addComponent(pImagenCocheNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, aniadirCocheLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAceptarCocheNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnCancelarCocheNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(8, 8, 8))
        );
        aniadirCocheLayout.setVerticalGroup(
            aniadirCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aniadirCocheLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(aniadirCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel44)
                    .addComponent(jLabel48)
                    .addComponent(jLabel52))
                .addGap(7, 7, 7)
                .addGroup(aniadirCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMarcaCocheNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnImagenCocheNuevo)
                    .addComponent(txtBastidorCocheNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(aniadirCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(aniadirCocheLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(aniadirCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel45)
                            .addComponent(jLabel49))
                        .addGap(8, 8, 8)
                        .addGroup(aniadirCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtModeloCocheNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbTipoCocheNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(aniadirCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel46)
                            .addComponent(jLabel50))
                        .addGap(8, 8, 8)
                        .addGroup(aniadirCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtMotorCocheNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCVCocheNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(aniadirCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel47)
                            .addComponent(jLabel51))
                        .addGap(8, 8, 8)
                        .addGroup(aniadirCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtColorCocheNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPrecioCocheNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(aniadirCocheLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(pImagenCocheNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(aniadirCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelarCocheNuevo)
                    .addComponent(btnAceptarCocheNuevo))
                .addGap(8, 8, 8))
        );

        modificarCoche.setTitle("Modificar coche");
        modificarCoche.setModal(true);
        modificarCoche.setResizable(false);
        modificarCoche.setSize(new java.awt.Dimension(535, 350));

        jLabel53.setText("Nº BASTIDOR");

        txtBastidorCocheModificar.setEditable(false);

        jLabel54.setText("MODELO");

        jLabel55.setText("MOTOR");

        jLabel56.setText("COLOR");

        jLabel57.setText("MARCA");

        jLabel58.setText("TIPO");

        jLabel59.setText("CV");

        jLabel60.setText("PRECIO");

        jLabel61.setText("IMAGEN");

        btnImagenCocheModificar.setMnemonic('S');
        btnImagenCocheModificar.setText("Seleccionar imagen");
        btnImagenCocheModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImagenCocheModificarActionPerformed(evt);
            }
        });

        pImagenCocheModificar.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout pImagenCocheModificarLayout = new javax.swing.GroupLayout(pImagenCocheModificar);
        pImagenCocheModificar.setLayout(pImagenCocheModificarLayout);
        pImagenCocheModificarLayout.setHorizontalGroup(
            pImagenCocheModificarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 191, Short.MAX_VALUE)
        );
        pImagenCocheModificarLayout.setVerticalGroup(
            pImagenCocheModificarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 160, Short.MAX_VALUE)
        );

        btnCancelarCocheModificar.setMnemonic('C');
        btnCancelarCocheModificar.setText("Cancelar");
        btnCancelarCocheModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarCocheModificarActionPerformed(evt);
            }
        });

        btnAceptarCocheModificar.setMnemonic('A');
        btnAceptarCocheModificar.setText("Aceptar");
        btnAceptarCocheModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarCocheModificarActionPerformed(evt);
            }
        });

        cbTipoCocheModificar.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "TURISMO", "SEDÁN", "COMPACTO", "DEPORTIVO", "COMERCIAL", "FAMILIAR", "TODOTERRENO" }));

        javax.swing.GroupLayout modificarCocheLayout = new javax.swing.GroupLayout(modificarCoche.getContentPane());
        modificarCoche.getContentPane().setLayout(modificarCocheLayout);
        modificarCocheLayout.setHorizontalGroup(
            modificarCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modificarCocheLayout.createSequentialGroup()
                .addGroup(modificarCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, modificarCocheLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(modificarCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(modificarCocheLayout.createSequentialGroup()
                                .addGroup(modificarCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel54)
                                    .addComponent(jLabel55))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(modificarCocheLayout.createSequentialGroup()
                                .addGroup(modificarCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel53)
                                    .addComponent(jLabel56)
                                    .addGroup(modificarCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(txtColorCocheModificar, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                                        .addComponent(txtMotorCocheModificar, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtModeloCocheModificar, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtBastidorCocheModificar, javax.swing.GroupLayout.Alignment.LEADING)))
                                .addGap(0, 32, Short.MAX_VALUE)))
                        .addGroup(modificarCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel57, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel58, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel59, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel60, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtMarcaCocheModificar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                            .addComponent(txtCVCocheModificar)
                            .addComponent(txtPrecioCocheModificar)
                            .addComponent(cbTipoCocheModificar, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(32, 32, 32)
                        .addGroup(modificarCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel61)
                            .addComponent(btnImagenCocheModificar)
                            .addComponent(pImagenCocheModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, modificarCocheLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAceptarCocheModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnCancelarCocheModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(8, 8, 8))
        );
        modificarCocheLayout.setVerticalGroup(
            modificarCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modificarCocheLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(modificarCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel53)
                    .addComponent(jLabel57)
                    .addComponent(jLabel61))
                .addGap(7, 7, 7)
                .addGroup(modificarCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBastidorCocheModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMarcaCocheModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnImagenCocheModificar))
                .addGroup(modificarCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(modificarCocheLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(modificarCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel54)
                            .addComponent(jLabel58))
                        .addGap(8, 8, 8)
                        .addGroup(modificarCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtModeloCocheModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbTipoCocheModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(modificarCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel55)
                            .addComponent(jLabel59))
                        .addGap(8, 8, 8)
                        .addGroup(modificarCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtMotorCocheModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCVCocheModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(modificarCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel56)
                            .addComponent(jLabel60))
                        .addGap(8, 8, 8)
                        .addGroup(modificarCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtColorCocheModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPrecioCocheModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(modificarCocheLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(pImagenCocheModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(modificarCocheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelarCocheModificar)
                    .addComponent(btnAceptarCocheModificar))
                .addGap(8, 8, 8))
        );

        buscarClienteVentaNueva.setTitle("Seleccionar cliente");
        buscarClienteVentaNueva.setModal(true);
        buscarClienteVentaNueva.setResizable(false);
        buscarClienteVentaNueva.setSize(new java.awt.Dimension(426, 270));

        btnNuevoCliente.setMnemonic('N');
        btnNuevoCliente.setText("Nuevo cliente");
        btnNuevoCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoClienteActionPerformed(evt);
            }
        });

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        cbBuscarClienteVentaNueva.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { " ", "DNI", "Nombre", "Apellidos" }));
        cbBuscarClienteVentaNueva.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbBuscarClienteVentaNuevaItemStateChanged(evt);
            }
        });

        txtBuscarClienteVentaNueva.setEditable(false);
        txtBuscarClienteVentaNueva.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBuscarClienteVentaNuevaKeyReleased(evt);
            }
        });

        btnBuscarClienteVentaNueva.setMnemonic('B');
        btnBuscarClienteVentaNueva.setText("Buscar");
        btnBuscarClienteVentaNueva.setEnabled(false);
        btnBuscarClienteVentaNueva.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarClienteVentaNuevaActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "DNI", "Nombre", "Apellidos"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jTable1MouseReleased(evt);
            }
        });
        jScrollPane4.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setResizable(false);
            jTable1.getColumnModel().getColumn(1).setResizable(false);
            jTable1.getColumnModel().getColumn(2).setResizable(false);
        }

        btnCargarClienteVentaNueva.setMnemonic('C');
        btnCargarClienteVentaNueva.setText("Cargar cliente");
        btnCargarClienteVentaNueva.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCargarClienteVentaNuevaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buscarClienteVentaNuevaLayout = new javax.swing.GroupLayout(buscarClienteVentaNueva.getContentPane());
        buscarClienteVentaNueva.getContentPane().setLayout(buscarClienteVentaNuevaLayout);
        buscarClienteVentaNuevaLayout.setHorizontalGroup(
            buscarClienteVentaNuevaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buscarClienteVentaNuevaLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(btnNuevoCliente)
                .addGap(8, 8, 8)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(cbBuscarClienteVentaNueva, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(txtBuscarClienteVentaNueva, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnBuscarClienteVentaNueva, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 15, Short.MAX_VALUE))
            .addGroup(buscarClienteVentaNuevaLayout.createSequentialGroup()
                .addGroup(buscarClienteVentaNuevaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(buscarClienteVentaNuevaLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buscarClienteVentaNuevaLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCargarClienteVentaNueva)))
                .addGap(8, 8, 8))
        );
        buscarClienteVentaNuevaLayout.setVerticalGroup(
            buscarClienteVentaNuevaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buscarClienteVentaNuevaLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(buscarClienteVentaNuevaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(buscarClienteVentaNuevaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cbBuscarClienteVentaNueva, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtBuscarClienteVentaNueva, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnBuscarClienteVentaNueva))
                    .addGroup(buscarClienteVentaNuevaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(btnNuevoCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jSeparator2)))
                .addGap(8, 8, 8)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnCargarClienteVentaNueva)
                .addGap(8, 8, 8))
        );

        buscarClienteVentaModificar.setTitle("Seleccionar cliente");
        buscarClienteVentaModificar.setModal(true);
        buscarClienteVentaModificar.setResizable(false);
        buscarClienteVentaModificar.setSize(new java.awt.Dimension(426, 270));

        cbBuscarClienteVentaModificar.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { " ", "DNI", "Nombre", "Apellidos" }));
        cbBuscarClienteVentaModificar.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbBuscarClienteVentaModificarItemStateChanged(evt);
            }
        });

        txtBuscarClienteVentaModificar.setEditable(false);

        btnBuscarClienteVentaModificar.setMnemonic('B');
        btnBuscarClienteVentaModificar.setText("Buscar");
        btnBuscarClienteVentaModificar.setEnabled(false);
        btnBuscarClienteVentaModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarClienteVentaModificarActionPerformed(evt);
            }
        });

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "DNI", "Nombre", "Apellidos"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable2MouseClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jTable2MouseReleased(evt);
            }
        });
        jScrollPane6.setViewportView(jTable2);
        if (jTable2.getColumnModel().getColumnCount() > 0) {
            jTable2.getColumnModel().getColumn(0).setResizable(false);
            jTable2.getColumnModel().getColumn(1).setResizable(false);
            jTable2.getColumnModel().getColumn(2).setResizable(false);
        }

        btnCargarClienteVentaModificar.setMnemonic('C');
        btnCargarClienteVentaModificar.setText("Cargar cliente");
        btnCargarClienteVentaModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCargarClienteVentaModificarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buscarClienteVentaModificarLayout = new javax.swing.GroupLayout(buscarClienteVentaModificar.getContentPane());
        buscarClienteVentaModificar.getContentPane().setLayout(buscarClienteVentaModificarLayout);
        buscarClienteVentaModificarLayout.setHorizontalGroup(
            buscarClienteVentaModificarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buscarClienteVentaModificarLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(cbBuscarClienteVentaModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(txtBuscarClienteVentaModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnBuscarClienteVentaModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 125, Short.MAX_VALUE))
            .addGroup(buscarClienteVentaModificarLayout.createSequentialGroup()
                .addGroup(buscarClienteVentaModificarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(buscarClienteVentaModificarLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buscarClienteVentaModificarLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCargarClienteVentaModificar)))
                .addGap(8, 8, 8))
        );
        buscarClienteVentaModificarLayout.setVerticalGroup(
            buscarClienteVentaModificarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buscarClienteVentaModificarLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(buscarClienteVentaModificarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbBuscarClienteVentaModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtBuscarClienteVentaModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscarClienteVentaModificar))
                .addGap(8, 8, 8)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnCargarClienteVentaModificar)
                .addGap(8, 8, 8))
        );

        buscarCocheVentaModificar.setTitle("Seleccionar coche");
        buscarCocheVentaModificar.setModal(true);
        buscarCocheVentaModificar.setResizable(false);
        buscarCocheVentaModificar.setSize(new java.awt.Dimension(426, 270));

        cbBuscarCocheVentaModificar.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { " ", "Bastidor", "Marca", "Modelo" }));
        cbBuscarCocheVentaModificar.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbBuscarCocheVentaModificarItemStateChanged(evt);
            }
        });

        txtBuscarCocheVentaModificar.setEditable(false);
        txtBuscarCocheVentaModificar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBuscarCocheVentaModificarKeyReleased(evt);
            }
        });

        btnBuscarCocheVentaModificar.setMnemonic('B');
        btnBuscarCocheVentaModificar.setText("Buscar");
        btnBuscarCocheVentaModificar.setEnabled(false);
        btnBuscarCocheVentaModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarCocheVentaModificarActionPerformed(evt);
            }
        });

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Bastidor", "Marca", "Modelo"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable3MouseClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jTable3MouseReleased(evt);
            }
        });
        jScrollPane7.setViewportView(jTable3);
        if (jTable3.getColumnModel().getColumnCount() > 0) {
            jTable3.getColumnModel().getColumn(0).setResizable(false);
            jTable3.getColumnModel().getColumn(0).setPreferredWidth(150);
            jTable3.getColumnModel().getColumn(1).setResizable(false);
            jTable3.getColumnModel().getColumn(2).setResizable(false);
        }

        btnCargarCocheVentaModificar.setMnemonic('C');
        btnCargarCocheVentaModificar.setText("Cargar coche");
        btnCargarCocheVentaModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCargarCocheVentaModificarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buscarCocheVentaModificarLayout = new javax.swing.GroupLayout(buscarCocheVentaModificar.getContentPane());
        buscarCocheVentaModificar.getContentPane().setLayout(buscarCocheVentaModificarLayout);
        buscarCocheVentaModificarLayout.setHorizontalGroup(
            buscarCocheVentaModificarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buscarCocheVentaModificarLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(cbBuscarCocheVentaModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(txtBuscarCocheVentaModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnBuscarCocheVentaModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 125, Short.MAX_VALUE))
            .addGroup(buscarCocheVentaModificarLayout.createSequentialGroup()
                .addGroup(buscarCocheVentaModificarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(buscarCocheVentaModificarLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buscarCocheVentaModificarLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCargarCocheVentaModificar)))
                .addGap(8, 8, 8))
        );
        buscarCocheVentaModificarLayout.setVerticalGroup(
            buscarCocheVentaModificarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buscarCocheVentaModificarLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(buscarCocheVentaModificarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbBuscarCocheVentaModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtBuscarCocheVentaModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscarCocheVentaModificar))
                .addGap(8, 8, 8)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnCargarCocheVentaModificar)
                .addGap(8, 8, 8))
        );

        aniadirVenta.setTitle("Añadir venta");
        aniadirVenta.setModal(true);
        aniadirVenta.setResizable(false);
        aniadirVenta.setSize(new java.awt.Dimension(358, 528));

        jLabel16.setText("Fecha:");

        lblFechaVentaNueva.setText("05/12/2017");

        jLabel26.setText("DATOS CLIENTE");

        jLabel28.setText("NOMBRE");

        jLabel30.setText("DNI");

        jLabel31.setText("APELLIDOS");

        jLabel32.setText("TELÉFONO");

        jLabel33.setText("DIRECCIÓN");

        jLabel34.setText("DATOS COCHE");

        jLabel35.setText("BASTIDOR");

        txtBastidorVentaNueva.setEditable(false);

        jLabel36.setText("MARCA");

        txtMarcaVentaNueva.setEditable(false);

        jLabel37.setText("MODELO");

        txtModeloVentaNueva.setEditable(false);

        jLabel38.setText("PRECIO:");

        jLabel40.setText("€");

        btnCancelarVentaNueva.setMnemonic('C');
        btnCancelarVentaNueva.setText("Cancelar");
        btnCancelarVentaNueva.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarVentaNuevaActionPerformed(evt);
            }
        });

        btnAceptarVentaNueva.setMnemonic('A');
        btnAceptarVentaNueva.setText("Aceptar");
        btnAceptarVentaNueva.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarVentaNuevaActionPerformed(evt);
            }
        });

        try {
            txtDniVentaNueva.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("########U")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }

        try {
            txtTelefonoVentaNueva.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("#########")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }

        javax.swing.GroupLayout aniadirVentaLayout = new javax.swing.GroupLayout(aniadirVenta.getContentPane());
        aniadirVenta.getContentPane().setLayout(aniadirVentaLayout);
        aniadirVentaLayout.setHorizontalGroup(
            aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aniadirVentaLayout.createSequentialGroup()
                .addGroup(aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(aniadirVentaLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(aniadirVentaLayout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblFechaVentaNueva))
                            .addComponent(jLabel26)
                            .addGroup(aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, aniadirVentaLayout.createSequentialGroup()
                                    .addComponent(jLabel34)
                                    .addGap(17, 17, 17))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, aniadirVentaLayout.createSequentialGroup()
                                    .addGap(10, 10, 10)
                                    .addGroup(aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, aniadirVentaLayout.createSequentialGroup()
                                            .addGroup(aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(jLabel35)
                                                .addComponent(txtBastidorVentaNueva)
                                                .addComponent(jLabel36)
                                                .addComponent(txtMarcaVentaNueva, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE))
                                            .addGap(37, 37, 37)
                                            .addGroup(aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(aniadirVentaLayout.createSequentialGroup()
                                                    .addComponent(jLabel37)
                                                    .addGap(0, 0, Short.MAX_VALUE))
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, aniadirVentaLayout.createSequentialGroup()
                                                    .addComponent(jLabel38)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(lblPrecioVentaNueva, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(jLabel40)
                                                    .addGap(9, 9, 9))
                                                .addComponent(txtModeloVentaNueva)))
                                        .addComponent(jLabel33, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, aniadirVentaLayout.createSequentialGroup()
                                            .addGroup(aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(jLabel28)
                                                .addComponent(txtNombreVentaNueva, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                                                .addComponent(jLabel30)
                                                .addComponent(txtDniVentaNueva))
                                            .addGap(37, 37, 37)
                                            .addGroup(aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(txtApellidosVentaNueva)
                                                .addGroup(aniadirVentaLayout.createSequentialGroup()
                                                    .addGroup(aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel31)
                                                        .addComponent(jLabel32))
                                                    .addGap(0, 0, Short.MAX_VALUE))
                                                .addComponent(txtTelefonoVentaNueva)))))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(aniadirVentaLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAceptarVentaNueva, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnCancelarVentaNueva, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, aniadirVentaLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(txtDireccionVentaNueva)))
                .addGap(8, 8, 8))
        );
        aniadirVentaLayout.setVerticalGroup(
            aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aniadirVentaLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(lblFechaVentaNueva))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel26)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(jLabel31))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNombreVentaNueva, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtApellidosVentaNueva, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(jLabel32))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDniVentaNueva, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTelefonoVentaNueva, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel33)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDireccionVentaNueva, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel34)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel35)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtBastidorVentaNueva, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel36)
                    .addComponent(jLabel37))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMarcaVentaNueva, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtModeloVentaNueva, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPrecioVentaNueva, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel38)
                        .addComponent(jLabel40)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(aniadirVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelarVentaNueva)
                    .addComponent(btnAceptarVentaNueva))
                .addGap(8, 8, 8))
        );

        aniadirRevision.setTitle("Añadir revisión");
        aniadirRevision.setModal(true);
        aniadirRevision.setResizable(false);
        aniadirRevision.setSize(new java.awt.Dimension(332, 296));

        jLabel23.setText("Nº REVISIÓN:");

        jLabel41.setText("FECHA:");

        jLabel43.setText("DATOS COCHE");

        jLabel62.setText("MARCA");

        jLabel64.setText("MODELO");

        jLabel67.setText("Nº BASTIDOR");

        chkFrenosRevisionNueva.setText("REVISIÓN DE FRENOS");

        chkFiltroRevisionNueva.setText("CAMBIO DE FILTRO");

        chkAceiteRevisionNueva.setText("CAMBIO DE ACEITE");

        btnCancelarRevisionNueva.setMnemonic('C');
        btnCancelarRevisionNueva.setText("Cancelar");
        btnCancelarRevisionNueva.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarRevisionNuevaActionPerformed(evt);
            }
        });

        btnAceptarRevisionNueva.setMnemonic('A');
        btnAceptarRevisionNueva.setText("Aceptar");
        btnAceptarRevisionNueva.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarRevisionNuevaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout aniadirRevisionLayout = new javax.swing.GroupLayout(aniadirRevision.getContentPane());
        aniadirRevision.getContentPane().setLayout(aniadirRevisionLayout);
        aniadirRevisionLayout.setHorizontalGroup(
            aniadirRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, aniadirRevisionLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnAceptarRevisionNueva, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnCancelarRevisionNueva, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8))
            .addGroup(aniadirRevisionLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(aniadirRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(aniadirRevisionLayout.createSequentialGroup()
                        .addGroup(aniadirRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(aniadirRevisionLayout.createSequentialGroup()
                                .addComponent(jLabel23)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblNumeroRevisionNueva, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel43)
                            .addGroup(aniadirRevisionLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(aniadirRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblMarcaRevisionNueva, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel62)
                                    .addComponent(jLabel67))))
                        .addGap(23, 23, 23)
                        .addGroup(aniadirRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(aniadirRevisionLayout.createSequentialGroup()
                                .addComponent(jLabel41)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblFechaRevisionNueva, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel64)
                            .addComponent(lblModeloRevisionNueva, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(chkAceiteRevisionNueva)
                    .addGroup(aniadirRevisionLayout.createSequentialGroup()
                        .addComponent(chkFrenosRevisionNueva)
                        .addGap(18, 18, 18)
                        .addComponent(chkFiltroRevisionNueva))
                    .addGroup(aniadirRevisionLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(lblBastidorRevisionNueva, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(6, 36, Short.MAX_VALUE))
        );
        aniadirRevisionLayout.setVerticalGroup(
            aniadirRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aniadirRevisionLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(aniadirRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblNumeroRevisionNueva, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(aniadirRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel23)
                        .addComponent(jLabel41)
                        .addComponent(lblFechaRevisionNueva, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel43)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(aniadirRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel62)
                    .addComponent(jLabel64))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(aniadirRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblMarcaRevisionNueva, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
                    .addComponent(lblModeloRevisionNueva, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jLabel67)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblBastidorRevisionNueva, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(aniadirRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkFrenosRevisionNueva)
                    .addComponent(chkFiltroRevisionNueva))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkAceiteRevisionNueva)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(aniadirRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelarRevisionNueva)
                    .addComponent(btnAceptarRevisionNueva))
                .addGap(8, 8, 8))
        );

        modificarRevision.setTitle("Modificar revisión");
        modificarRevision.setModal(true);
        modificarRevision.setResizable(false);
        modificarRevision.setSize(new java.awt.Dimension(332, 296));

        jLabel39.setText("Nº REVISIÓN:");

        jLabel42.setText("FECHA:");

        jLabel63.setText("DATOS COCHE");

        jLabel65.setText("MARCA");

        jLabel66.setText("MODELO");

        jLabel68.setText("Nº BASTIDOR");

        chkFrenosRevisionModificar.setText("REVISIÓN DE FRENOS");

        chkFiltroRevisionModificar.setText("CAMBIO DE FILTRO");

        chkAceiteRevisionModificar.setText("CAMBIO DE ACEITE");

        btnCancelarRevisionModificar.setMnemonic('C');
        btnCancelarRevisionModificar.setText("Cancelar");
        btnCancelarRevisionModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarRevisionModificarActionPerformed(evt);
            }
        });

        btnAceptarRevisionModificar.setMnemonic('A');
        btnAceptarRevisionModificar.setText("Aceptar");
        btnAceptarRevisionModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarRevisionModificarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout modificarRevisionLayout = new javax.swing.GroupLayout(modificarRevision.getContentPane());
        modificarRevision.getContentPane().setLayout(modificarRevisionLayout);
        modificarRevisionLayout.setHorizontalGroup(
            modificarRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, modificarRevisionLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnAceptarRevisionModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnCancelarRevisionModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8))
            .addGroup(modificarRevisionLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(modificarRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(modificarRevisionLayout.createSequentialGroup()
                        .addGroup(modificarRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(modificarRevisionLayout.createSequentialGroup()
                                .addComponent(jLabel39)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblNumeroRevisionModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel63)
                            .addGroup(modificarRevisionLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(modificarRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblMarcaRevisionModificar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel65)
                                    .addComponent(jLabel68)
                                    .addComponent(lblBastidorRevisionModificar, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE))))
                        .addGap(8, 8, 8)
                        .addGroup(modificarRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(modificarRevisionLayout.createSequentialGroup()
                                .addComponent(jLabel42)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblFechaRevisionModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel66)
                            .addComponent(lblModeloRevisionModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(modificarRevisionLayout.createSequentialGroup()
                        .addComponent(chkFrenosRevisionModificar)
                        .addGap(18, 18, 18)
                        .addComponent(chkFiltroRevisionModificar))
                    .addComponent(chkAceiteRevisionModificar))
                .addGap(6, 52, Short.MAX_VALUE))
        );
        modificarRevisionLayout.setVerticalGroup(
            modificarRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modificarRevisionLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(modificarRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblNumeroRevisionModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(modificarRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel39)
                        .addComponent(jLabel42)
                        .addComponent(lblFechaRevisionModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel63)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(modificarRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel65)
                    .addComponent(jLabel66))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(modificarRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblMarcaRevisionModificar, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
                    .addComponent(lblModeloRevisionModificar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jLabel68)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblBastidorRevisionModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(modificarRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkFrenosRevisionModificar)
                    .addComponent(chkFiltroRevisionModificar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkAceiteRevisionModificar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(modificarRevisionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelarRevisionModificar)
                    .addComponent(btnAceptarRevisionModificar))
                .addGap(8, 8, 8))
        );

        modificarVenta.setTitle("Modificar venta");
        modificarVenta.setModal(true);
        modificarVenta.setResizable(false);
        modificarVenta.setSize(new java.awt.Dimension(270, 332));

        jLabel69.setText("FECHA");

        jLabel70.setText("DNI");

        txtDniVentaModificar.setEditable(false);

        jLabel71.setText("Nº BASTIDOR");

        txtBastidorVentaModificar.setEditable(false);

        jLabel72.setText("PRECIO");

        txtPrecioVentaModificar.setEditable(false);

        btnBuscarCliente.setMnemonic('B');
        btnBuscarCliente.setText("Buscar cliente");
        btnBuscarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarClienteActionPerformed(evt);
            }
        });

        btnBuscarCoche.setMnemonic('B');
        btnBuscarCoche.setText("Buscar coche");
        btnBuscarCoche.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarCocheActionPerformed(evt);
            }
        });

        jLabel73.setText("€");

        btnCancelarVentaModificar.setMnemonic('C');
        btnCancelarVentaModificar.setText("Cancelar");
        btnCancelarVentaModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarVentaModificarActionPerformed(evt);
            }
        });

        btnAceptarVentaModificar.setMnemonic('A');
        btnAceptarVentaModificar.setText("Aceptar");
        btnAceptarVentaModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarVentaModificarActionPerformed(evt);
            }
        });

        jDateChooser.setDateFormatString("dd/MM/yyyy");

        javax.swing.GroupLayout modificarVentaLayout = new javax.swing.GroupLayout(modificarVenta.getContentPane());
        modificarVenta.getContentPane().setLayout(modificarVentaLayout);
        modificarVentaLayout.setHorizontalGroup(
            modificarVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modificarVentaLayout.createSequentialGroup()
                .addGroup(modificarVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(modificarVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(modificarVentaLayout.createSequentialGroup()
                            .addGroup(modificarVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(txtPrecioVentaModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(modificarVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtBastidorVentaModificar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(modificarVentaLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(modificarVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, modificarVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(txtDniVentaModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel71))
                                            .addComponent(jLabel72)))))
                            .addGap(18, 18, 18)
                            .addGroup(modificarVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(modificarVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btnBuscarCliente, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                                    .addComponent(btnBuscarCoche, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(jLabel73)))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, modificarVentaLayout.createSequentialGroup()
                            .addGap(47, 47, 47)
                            .addComponent(btnAceptarVentaModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                            .addComponent(btnCancelarVentaModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(modificarVentaLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jLabel70)))
                    .addGroup(modificarVentaLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(modificarVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel69))))
                .addGap(8, 8, 8))
        );
        modificarVentaLayout.setVerticalGroup(
            modificarVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modificarVentaLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel69)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21)
                .addComponent(jLabel70)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(modificarVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDniVentaModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscarCliente))
                .addGap(18, 18, 18)
                .addComponent(jLabel71)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(modificarVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBastidorVentaModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscarCoche))
                .addGap(18, 18, 18)
                .addComponent(jLabel72)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(modificarVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPrecioVentaModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel73))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(modificarVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelarVentaModificar)
                    .addComponent(btnAceptarVentaModificar))
                .addGap(8, 8, 8))
        );

        modificarCliente.setTitle("Modificar cliente");
        modificarCliente.setModal(true);
        modificarCliente.setResizable(false);
        modificarCliente.setSize(new java.awt.Dimension(328, 266));

        jLabel74.setText("NOMBRE");

        jLabel75.setText("APELLIDOS");

        jLabel76.setText("DNI");

        txtDniClienteModificar.setEditable(false);

        jLabel77.setText("TELÉFONO");

        jLabel78.setText("DIRECCIÓN");

        btnCancelarClienteModificar.setMnemonic('C');
        btnCancelarClienteModificar.setText("Cancelar");
        btnCancelarClienteModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarClienteModificarActionPerformed(evt);
            }
        });

        btnAceptarClienteModificar.setMnemonic('A');
        btnAceptarClienteModificar.setText("Aceptar");
        btnAceptarClienteModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarClienteModificarActionPerformed(evt);
            }
        });

        try {
            txtTelefonoClienteModificar.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("#########")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }

        javax.swing.GroupLayout modificarClienteLayout = new javax.swing.GroupLayout(modificarCliente.getContentPane());
        modificarCliente.getContentPane().setLayout(modificarClienteLayout);
        modificarClienteLayout.setHorizontalGroup(
            modificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modificarClienteLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(modificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel78)
                    .addGroup(modificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(txtDireccionClienteModificar)
                        .addGroup(modificarClienteLayout.createSequentialGroup()
                            .addGroup(modificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel74)
                                .addComponent(jLabel76)
                                .addComponent(txtDniClienteModificar, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                                .addComponent(txtNombreClienteModificar))
                            .addGap(37, 37, 37)
                            .addGroup(modificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel75)
                                .addComponent(jLabel77)
                                .addComponent(txtApellidosClienteModificar, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                                .addComponent(txtTelefonoClienteModificar)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, modificarClienteLayout.createSequentialGroup()
                .addContainerGap(112, Short.MAX_VALUE)
                .addComponent(btnAceptarClienteModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnCancelarClienteModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8))
        );
        modificarClienteLayout.setVerticalGroup(
            modificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modificarClienteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(modificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel74)
                    .addComponent(jLabel75))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(modificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNombreClienteModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtApellidosClienteModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(modificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel76)
                    .addComponent(jLabel77))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(modificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDniClienteModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTelefonoClienteModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel78)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDireccionClienteModificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(modificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelarClienteModificar)
                    .addComponent(btnAceptarClienteModificar))
                .addGap(8, 8, 8))
        );

        acercaDe.setTitle("Acerca de Concesionario");
        acercaDe.setModal(true);
        acercaDe.setResizable(false);
        acercaDe.setSize(new java.awt.Dimension(232, 132));

        jLabel79.setText("<html>\n<p>Concesionario 1.1.4</p>\n<p>Juan Carlos Expósito Romero</p>\n<p>Desarrollo de Interfaces 2DAM</p>\n</html>");

        javax.swing.GroupLayout acercaDeLayout = new javax.swing.GroupLayout(acercaDe.getContentPane());
        acercaDe.getContentPane().setLayout(acercaDeLayout);
        acercaDeLayout.setHorizontalGroup(
            acercaDeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(acercaDeLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabel79, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );
        acercaDeLayout.setVerticalGroup(
            acercaDeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(acercaDeLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel79, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
        );

        nuevocoche.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/New16.gif"))); // NOI18N
        nuevocoche.setText("Nuevo");
        nuevocoche.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nuevococheActionPerformed(evt);
            }
        });
        popupCoche.add(nuevocoche);

        editarcoche.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Edit16.gif"))); // NOI18N
        editarcoche.setText("Editar");
        editarcoche.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editarcocheActionPerformed(evt);
            }
        });
        popupCoche.add(editarcoche);

        borrarcoche.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Delete16.gif"))); // NOI18N
        borrarcoche.setText("Borrar");
        borrarcoche.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                borrarcocheActionPerformed(evt);
            }
        });
        popupCoche.add(borrarcoche);

        editarRevision.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Edit16.gif"))); // NOI18N
        editarRevision.setText("Editar");
        editarRevision.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editarRevisionActionPerformed(evt);
            }
        });
        popupRevision.add(editarRevision);

        borrarRevision.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Delete16.gif"))); // NOI18N
        borrarRevision.setText("Borrar");
        borrarRevision.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                borrarRevisionActionPerformed(evt);
            }
        });
        popupRevision.add(borrarRevision);

        editarVenta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Edit16.gif"))); // NOI18N
        editarVenta.setText("Editar");
        editarVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editarVentaActionPerformed(evt);
            }
        });
        popupVenta.add(editarVenta);

        borrarVenta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Delete16.gif"))); // NOI18N
        borrarVenta.setText("Borrar");
        borrarVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                borrarVentaActionPerformed(evt);
            }
        });
        popupVenta.add(borrarVenta);

        editarCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Edit16.gif"))); // NOI18N
        editarCliente.setText("Editar");
        editarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editarClienteActionPerformed(evt);
            }
        });
        popupCliente.add(editarCliente);

        borrarCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Delete16.gif"))); // NOI18N
        borrarCliente.setText("Borrar");
        borrarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                borrarClienteActionPerformed(evt);
            }
        });
        popupCliente.add(borrarCliente);

        seleccionMarca.setTitle("Generar informe de una marca");
        seleccionMarca.setModal(true);
        seleccionMarca.setResizable(false);
        seleccionMarca.setSize(new java.awt.Dimension(320, 158));

        jLabel4.setText("Seleccione la marca de la que quiere hacer el informe:");

        btnAceptarInforme.setMnemonic('A');
        btnAceptarInforme.setText("Aceptar");
        btnAceptarInforme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarInformeActionPerformed(evt);
            }
        });

        btnCancelarInforme.setMnemonic('C');
        btnCancelarInforme.setText("Cancelar");
        btnCancelarInforme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarInformeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout seleccionMarcaLayout = new javax.swing.GroupLayout(seleccionMarca.getContentPane());
        seleccionMarca.getContentPane().setLayout(seleccionMarcaLayout);
        seleccionMarcaLayout.setHorizontalGroup(
            seleccionMarcaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(seleccionMarcaLayout.createSequentialGroup()
                .addGroup(seleccionMarcaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(seleccionMarcaLayout.createSequentialGroup()
                        .addGap(87, 87, 87)
                        .addComponent(cbMarcasInforme, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(seleccionMarcaLayout.createSequentialGroup()
                        .addGap(73, 73, 73)
                        .addComponent(btnAceptarInforme)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelarInforme))
                    .addGroup(seleccionMarcaLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel4)))
                .addContainerGap(42, Short.MAX_VALUE))
        );
        seleccionMarcaLayout.setVerticalGroup(
            seleccionMarcaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(seleccionMarcaLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbMarcasInforme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 67, Short.MAX_VALUE)
                .addGroup(seleccionMarcaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAceptarInforme)
                    .addComponent(btnCancelarInforme))
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Concesionario");
        setIconImage(getIconImage());
        setResizable(false);

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        jLabel1.setText("Filtro:");

        jLabel2.setText("Marca");

        cbMarcaMain.setToolTipText("Realiza una búsqueda filtrando por marca");
        cbMarcaMain.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbMarcaMainItemStateChanged(evt);
            }
        });

        jLabel3.setText("Motor");

        cbMotorMain.setToolTipText("Realiza una búsqueda filtrando por motor");
        cbMotorMain.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbMotorMainItemStateChanged(evt);
            }
        });

        cbBuscarMain.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecciona un campo", "Bastidor", "Marca", "Modelo", "Motor", "CV", "Tipo", "Color", "Precio" }));
        cbBuscarMain.setToolTipText("Realiza una búsqueda filtrando por el campo seleccionado");
        cbBuscarMain.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbBuscarMainItemStateChanged(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        txtBuscarMain.setEditable(false);
        txtBuscarMain.setToolTipText("");
        txtBuscarMain.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBuscarMainKeyReleased(evt);
            }
        });

        btnBuscarMain.setText("Buscar");
        btnBuscarMain.setEnabled(false);
        btnBuscarMain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarMainActionPerformed(evt);
            }
        });

        btnReiniciarBusqueda.setMnemonic('B');
        btnReiniciarBusqueda.setText("Reiniciar Búsqueda");
        btnReiniciarBusqueda.setToolTipText("Reinicia todas las búsquedas y lista todos los coches");
        btnReiniciarBusqueda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReiniciarBusquedaActionPerformed(evt);
            }
        });

        tablaMain.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Nº BASTIDOR", "MARCA", "MODELO", "MOTOR", "CV", "TIPO", "COLOR", "PRECIO"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaMain.setToolTipText("<html>\n    <p>Para añadir un <b>nuevo</b> coche haga click derecho o utilice el botón \"Nuevo\"</p>\n    <p>Para <b>editar</b> haga doble click, click derecho, pulse la tecla 'e' o utilice el botón \"Modificar\"</p>\n    <p>Para <b>borrar</b> haga click derecho, pulse las teclas 'b' o \"Suprimir\" o utiliza el botón \"Eliminar\"</p>\n</html>");
        tablaMain.getTableHeader().setReorderingAllowed(false);
        tablaMain.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaMainMouseClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tablaMainMouseReleased(evt);
            }
        });
        tablaMain.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tablaMainKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(tablaMain);
        if (tablaMain.getColumnModel().getColumnCount() > 0) {
            tablaMain.getColumnModel().getColumn(0).setResizable(false);
            tablaMain.getColumnModel().getColumn(0).setPreferredWidth(150);
            tablaMain.getColumnModel().getColumn(1).setResizable(false);
            tablaMain.getColumnModel().getColumn(2).setResizable(false);
            tablaMain.getColumnModel().getColumn(3).setResizable(false);
            tablaMain.getColumnModel().getColumn(4).setResizable(false);
            tablaMain.getColumnModel().getColumn(4).setPreferredWidth(50);
            tablaMain.getColumnModel().getColumn(5).setResizable(false);
            tablaMain.getColumnModel().getColumn(6).setResizable(false);
            tablaMain.getColumnModel().getColumn(7).setResizable(false);
        }

        btnVentaNueva.setMnemonic('V');
        btnVentaNueva.setText("VENTA");
        btnVentaNueva.setToolTipText("Realiza una venta del coche seleccionado");
        btnVentaNueva.setEnabled(false);
        btnVentaNueva.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVentaNuevaActionPerformed(evt);
            }
        });

        btnRevisionNueva.setMnemonic('R');
        btnRevisionNueva.setText("REVISIÓN");
        btnRevisionNueva.setToolTipText("Realiza una revisión al coche seleccionado");
        btnRevisionNueva.setEnabled(false);
        btnRevisionNueva.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRevisionNuevaActionPerformed(evt);
            }
        });

        btnCocheBorrar.setMnemonic('E');
        btnCocheBorrar.setText("ELIMINAR");
        btnCocheBorrar.setToolTipText("Elimina el coche seleccionado");
        btnCocheBorrar.setEnabled(false);
        btnCocheBorrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCocheBorrarActionPerformed(evt);
            }
        });

        btnCocheModificar.setMnemonic('M');
        btnCocheModificar.setText("MODIFICAR");
        btnCocheModificar.setToolTipText("Modifica el coche seleccionado");
        btnCocheModificar.setEnabled(false);
        btnCocheModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCocheModificarActionPerformed(evt);
            }
        });

        btnCocheNuevo.setMnemonic('N');
        btnCocheNuevo.setText("NUEVO");
        btnCocheNuevo.setToolTipText("Añade un nuevo coche");
        btnCocheNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCocheNuevoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout cochesLayout = new javax.swing.GroupLayout(coches);
        coches.setLayout(cochesLayout);
        cochesLayout.setHorizontalGroup(
            cochesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cochesLayout.createSequentialGroup()
                .addGroup(cochesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cochesLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1))
                    .addGroup(cochesLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(cochesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(cochesLayout.createSequentialGroup()
                                .addComponent(btnVentaNueva, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8)
                                .addComponent(btnRevisionNueva, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnCocheNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8)
                                .addComponent(btnCocheModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8)
                                .addComponent(btnCocheBorrar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(cochesLayout.createSequentialGroup()
                                .addComponent(btnReiniciarBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 527, Short.MAX_VALUE))
                            .addGroup(cochesLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2)
                                .addGap(8, 8, 8)
                                .addComponent(cbMarcaMain, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel3)
                                .addGap(8, 8, 8)
                                .addComponent(cbMotorMain, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8)
                                .addComponent(cbBuscarMain, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtBuscarMain, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnBuscarMain)
                                .addGap(26, 26, 26)))))
                .addGap(8, 8, 8))
        );
        cochesLayout.setVerticalGroup(
            cochesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cochesLayout.createSequentialGroup()
                .addGroup(cochesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cochesLayout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addGroup(cochesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(cbMarcaMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(cbMotorMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbBuscarMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtBuscarMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnBuscarMain)))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addComponent(btnReiniciarBusqueda)
                .addGap(12, 12, 12)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(cochesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnVentaNueva)
                    .addComponent(btnRevisionNueva)
                    .addComponent(btnCocheBorrar)
                    .addComponent(btnCocheModificar)
                    .addComponent(btnCocheNuevo))
                .addGap(8, 8, 8))
        );

        jTabbedPane1.addTab("COCHES", null, coches, "");

        jLabel5.setText("Nº REVISIÓN");

        lblNumeroRevisionMain.setToolTipText("");

        jLabel7.setText("FECHA");

        jLabel9.setText("Nº BASTIDOR");

        jLabel11.setText("MARCA");

        jLabel13.setText("MODELO");

        jLabel15.setText("REVISIÓN DE FRENOS:");

        jLabel17.setText("CAMBIO DE FILTRO:");

        jLabel19.setText("CAMBIO DE ACEITE:");

        jLabel21.setText("IMAGEN");

        pImagenRevisionMain.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout pImagenRevisionMainLayout = new javax.swing.GroupLayout(pImagenRevisionMain);
        pImagenRevisionMain.setLayout(pImagenRevisionMainLayout);
        pImagenRevisionMainLayout.setHorizontalGroup(
            pImagenRevisionMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 190, Short.MAX_VALUE)
        );
        pImagenRevisionMainLayout.setVerticalGroup(
            pImagenRevisionMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 160, Short.MAX_VALUE)
        );

        btnRevisionBorrar.setMnemonic('E');
        btnRevisionBorrar.setText("ELIMINAR");
        btnRevisionBorrar.setToolTipText("Elimina la revisión seleccionada");
        btnRevisionBorrar.setEnabled(false);
        btnRevisionBorrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRevisionBorrarActionPerformed(evt);
            }
        });

        btnRevisionModificar.setMnemonic('M');
        btnRevisionModificar.setText("MODIFICAR");
        btnRevisionModificar.setToolTipText("Modifica la revisión seleccionada");
        btnRevisionModificar.setEnabled(false);
        btnRevisionModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRevisionModificarActionPerformed(evt);
            }
        });

        tablaRevisiones.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Nº REVISIÓN"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaRevisiones.setToolTipText("<html>\n    <p>Para <b>editar</b> haga doble click, click derecho, pulse la tecla 'e' o utilice el botón \"Modificar\"</p>\n    <p>Para <b>borrar</b> haga click derecho, pulse las teclas 'b' o \"Suprimir\" o utiliza el botón \"Eliminar\"</p>\n</html>");
        tablaRevisiones.getTableHeader().setReorderingAllowed(false);
        tablaRevisiones.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaRevisionesMouseClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tablaRevisionesMouseReleased(evt);
            }
        });
        tablaRevisiones.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tablaRevisionesKeyReleased(evt);
            }
        });
        jScrollPane9.setViewportView(tablaRevisiones);

        javax.swing.GroupLayout revisionesLayout = new javax.swing.GroupLayout(revisiones);
        revisiones.setLayout(revisionesLayout);
        revisionesLayout.setHorizontalGroup(
            revisionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(revisionesLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(47, 47, 47)
                .addGroup(revisionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(revisionesLayout.createSequentialGroup()
                        .addGroup(revisionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(revisionesLayout.createSequentialGroup()
                                .addComponent(jLabel19)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblAceiteRevisionMain, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, revisionesLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btnRevisionModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8)))
                        .addComponent(btnRevisionBorrar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(revisionesLayout.createSequentialGroup()
                        .addGroup(revisionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addGroup(revisionesLayout.createSequentialGroup()
                                .addComponent(jLabel15)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblFrenosRevisionMain, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(revisionesLayout.createSequentialGroup()
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblFiltroRevisionMain, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(revisionesLayout.createSequentialGroup()
                                .addGroup(revisionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel9)
                                    .addComponent(lblBastidorRevisionMain, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                                    .addComponent(lblNumeroRevisionMain, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                                    .addComponent(lblModeloRevisionMain, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE))
                                .addGap(48, 48, 48)
                                .addGroup(revisionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel11)
                                    .addComponent(lblFechaRevisionMain, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                                    .addComponent(lblMarcaRevisionMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(revisionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel21)
                                    .addComponent(pImagenRevisionMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 74, Short.MAX_VALUE)))
                .addGap(8, 8, 8))
        );
        revisionesLayout.setVerticalGroup(
            revisionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(revisionesLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(revisionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(revisionesLayout.createSequentialGroup()
                        .addGroup(revisionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jLabel7)
                            .addComponent(jLabel21))
                        .addGap(8, 8, 8)
                        .addGroup(revisionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(revisionesLayout.createSequentialGroup()
                                .addGroup(revisionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblNumeroRevisionMain, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblFechaRevisionMain, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(24, 24, 24)
                                .addGroup(revisionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel11))
                                .addGap(8, 8, 8)
                                .addGroup(revisionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblBastidorRevisionMain, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblMarcaRevisionMain, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(24, 24, 24)
                                .addComponent(jLabel13)
                                .addGap(8, 8, 8)
                                .addComponent(lblModeloRevisionMain, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(pImagenRevisionMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(revisionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel15)
                            .addComponent(lblFrenosRevisionMain, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(revisionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(lblFiltroRevisionMain, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(revisionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(revisionesLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(revisionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel19)
                            .addComponent(lblAceiteRevisionMain, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(38, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, revisionesLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(revisionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnRevisionBorrar)
                            .addComponent(btnRevisionModificar))
                        .addGap(8, 8, 8))))
        );

        jTabbedPane1.addTab("REVISIONES", null, revisiones, "");

        tablaVentas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Nº BASTIDOR", "DNI"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaVentas.setToolTipText("<html>\n    <p>Para <b>editar</b> haga doble click, click derecho, pulse la tecla 'e' o utilice el botón \"Modificar\"</p>\n    <p>Para <b>borrar</b> haga click derecho, pulse las teclas 'b' o \"Suprimir\" o utiliza el botón \"Eliminar\"</p>\n</html>");
        tablaVentas.getTableHeader().setReorderingAllowed(false);
        tablaVentas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaVentasMouseClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tablaVentasMouseReleased(evt);
            }
        });
        tablaVentas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tablaVentasKeyReleased(evt);
            }
        });
        jScrollPane5.setViewportView(tablaVentas);
        if (tablaVentas.getColumnModel().getColumnCount() > 0) {
            tablaVentas.getColumnModel().getColumn(0).setResizable(false);
            tablaVentas.getColumnModel().getColumn(0).setPreferredWidth(150);
            tablaVentas.getColumnModel().getColumn(1).setResizable(false);
        }

        jLabel6.setText("FECHA");

        jLabel10.setText("NOMBRE");

        jLabel14.setText("APELLIDOS");

        jLabel18.setText("DNI");

        jLabel22.setText("COCHE");

        jLabel24.setText("PRECIO");

        btnVentaBorrar.setMnemonic('E');
        btnVentaBorrar.setText("ELIMINAR");
        btnVentaBorrar.setToolTipText("Elimina la venta seleccionada");
        btnVentaBorrar.setEnabled(false);
        btnVentaBorrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVentaBorrarActionPerformed(evt);
            }
        });

        btnVentaModificar.setMnemonic('M');
        btnVentaModificar.setText("MODIFICAR");
        btnVentaModificar.setToolTipText("Modifica la venta seleccionada");
        btnVentaModificar.setEnabled(false);
        btnVentaModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVentaModificarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ventasLayout = new javax.swing.GroupLayout(ventas);
        ventas.setLayout(ventasLayout);
        ventasLayout.setHorizontalGroup(
            ventasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ventasLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(47, 47, 47)
                .addGroup(ventasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel18)
                    .addGroup(ventasLayout.createSequentialGroup()
                        .addGroup(ventasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lblDniVentaMain, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel22, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCocheVentaMain, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(lblNombreVentaMain, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(lblFechaVentaMain, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                        .addGap(48, 48, 48)
                        .addGroup(ventasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel24)
                            .addComponent(jLabel14)
                            .addComponent(lblApellidosVentaMain, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPrecioVentaMain, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(117, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ventasLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnVentaModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnVentaBorrar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8))
        );
        ventasLayout.setVerticalGroup(
            ventasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ventasLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(ventasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ventasLayout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblFechaVentaMain, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(ventasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(jLabel14))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(ventasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblNombreVentaMain, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblApellidosVentaMain, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDniVentaMain, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(ventasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel22)
                            .addComponent(jLabel24))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(ventasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblCocheVentaMain, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPrecioVentaMain, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                .addGroup(ventasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnVentaBorrar)
                    .addComponent(btnVentaModificar))
                .addGap(8, 8, 8))
        );

        jTabbedPane1.addTab("VENTAS", null, ventas, "");

        jLabel12.setText("DNI");

        jLabel20.setText("Nombre");

        jLabel25.setText("Apellidos");

        jLabel27.setText("Teléfono");

        jLabel29.setText("Dirección");

        btnClienteBorrar.setMnemonic('E');
        btnClienteBorrar.setText("ELIMINAR");
        btnClienteBorrar.setToolTipText("Elimina el cliente seleccionado");
        btnClienteBorrar.setEnabled(false);
        btnClienteBorrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClienteBorrarActionPerformed(evt);
            }
        });

        btnClienteModificar.setMnemonic('M');
        btnClienteModificar.setText("MODIFICAR");
        btnClienteModificar.setToolTipText("Modifica el cliente seleccionado");
        btnClienteModificar.setEnabled(false);
        btnClienteModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClienteModificarActionPerformed(evt);
            }
        });

        tablaClientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "DNI"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaClientes.setToolTipText("<html>\n    <p>Para <b>editar</b> haga doble click, click derecho, pulse la tecla 'e' o utilice el botón \"Modificar\"</p>\n    <p>Para <b>borrar</b> haga click derecho, pulse las teclas 'b' o \"Suprimir\" o utiliza el botón \"Eliminar\"</p>\n</html>");
        tablaClientes.getTableHeader().setReorderingAllowed(false);
        tablaClientes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaClientesMouseClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tablaClientesMouseReleased(evt);
            }
        });
        tablaClientes.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tablaClientesKeyReleased(evt);
            }
        });
        jScrollPane10.setViewportView(tablaClientes);

        javax.swing.GroupLayout clientesLayout = new javax.swing.GroupLayout(clientes);
        clientes.setLayout(clientesLayout);
        clientesLayout.setHorizontalGroup(
            clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(clientesLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(47, 47, 47)
                .addGroup(clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel12)
                    .addGroup(clientesLayout.createSequentialGroup()
                        .addGroup(clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblNombreClienteMain, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                            .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(48, 48, 48)
                        .addGroup(clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel25)
                            .addComponent(lblApellidosClienteMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jLabel27)
                    .addComponent(jLabel29)
                    .addComponent(lblDireccionClienteMain, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDniClienteMain, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTelefonoClienteMain, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(293, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, clientesLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnClienteModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnClienteBorrar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8))
        );
        clientesLayout.setVerticalGroup(
            clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(clientesLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(clientesLayout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDniClienteMain, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel20)
                            .addComponent(jLabel25))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblNombreClienteMain, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblApellidosClienteMain, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel27)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTelefonoClienteMain, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel29)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDireccionClienteMain, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                .addGroup(clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClienteBorrar)
                    .addComponent(btnClienteModificar))
                .addGap(8, 8, 8))
        );

        jTabbedPane1.addTab("CLIENTES", null, clientes, "");

        archivo.setMnemonic('A');
        archivo.setText("Archivo");

        reiniciarbd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Refresh24.gif"))); // NOI18N
        reiniciarbd.setMnemonic('R');
        reiniciarbd.setText("Reiniciar BBDD");
        reiniciarbd.setToolTipText("Borra todos los datos del concesionario");
        reiniciarbd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reiniciarbdActionPerformed(evt);
            }
        });
        archivo.add(reiniciarbd);

        salir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Stop24.gif"))); // NOI18N
        salir.setMnemonic('S');
        salir.setText("Salir");
        salir.setToolTipText("Sal de la aplicación");
        salir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                salirActionPerformed(evt);
            }
        });
        archivo.add(salir);

        menu.add(archivo);

        informes.setMnemonic('I');
        informes.setText("Informes");

        jMenu1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/History24.gif"))); // NOI18N
        jMenu1.setMnemonic('c');
        jMenu1.setText("Informes de coches");

        generaInformeCoches.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/History24.gif"))); // NOI18N
        generaInformeCoches.setMnemonic('I');
        generaInformeCoches.setText("Informe de coches");
        generaInformeCoches.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generaInformeCochesActionPerformed(evt);
            }
        });
        jMenu1.add(generaInformeCoches);

        generaInformeCochesMarca.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/History24.gif"))); // NOI18N
        generaInformeCochesMarca.setMnemonic('m');
        generaInformeCochesMarca.setText("Informe de coches por marca");
        generaInformeCochesMarca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generaInformeCochesMarcaActionPerformed(evt);
            }
        });
        jMenu1.add(generaInformeCochesMarca);

        informes.add(jMenu1);

        generaInformeRevisiones.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/History24.gif"))); // NOI18N
        generaInformeRevisiones.setMnemonic('r');
        generaInformeRevisiones.setText("Informe de revisiones");
        generaInformeRevisiones.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generaInformeRevisionesActionPerformed(evt);
            }
        });
        informes.add(generaInformeRevisiones);

        generaInformeVentas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/History24.gif"))); // NOI18N
        generaInformeVentas.setMnemonic('v');
        generaInformeVentas.setText("Informe de ventas");
        generaInformeVentas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generaInformeVentasActionPerformed(evt);
            }
        });
        informes.add(generaInformeVentas);

        generaInformeClientes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/History24.gif"))); // NOI18N
        generaInformeClientes.setMnemonic('l');
        generaInformeClientes.setText("Informe de clientes");
        generaInformeClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generaInformeClientesActionPerformed(evt);
            }
        });
        informes.add(generaInformeClientes);

        menu.add(informes);

        ayuda.setMnemonic('u');
        ayuda.setText("Ayuda");
        ayuda.setToolTipText("");

        acercade.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/About24.gif"))); // NOI18N
        acercade.setMnemonic('A');
        acercade.setText("Acerca de");
        acercade.setToolTipText("Muestra información sobre la aplicación");
        acercade.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acercadeActionPerformed(evt);
            }
        });
        ayuda.add(acercade);

        manual.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Help24.gif"))); // NOI18N
        manual.setMnemonic('M');
        manual.setText("Manual");
        manual.setToolTipText("Muestra el manual de usuario");
        manual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualActionPerformed(evt);
            }
        });
        ayuda.add(manual);

        menu.add(ayuda);

        setJMenuBar(menu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCocheNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCocheNuevoActionPerformed
        aniadirCoche.setLocationRelativeTo(null);
        txtBastidorCocheNuevo.setText("");
        txtMarcaCocheNuevo.setText("");
        txtModeloCocheNuevo.setText("");
        cbTipoCocheNuevo.setSelectedIndex(0);
        txtMotorCocheNuevo.setText("");
        txtCVCocheNuevo.setText("");
        txtColorCocheNuevo.setText("");
        txtPrecioCocheNuevo.setText("");
        pImagenCocheNuevo.removeAll();
        this.imagenblob=null;
        aniadirCoche.setVisible(true);
    }//GEN-LAST:event_btnCocheNuevoActionPerformed

    private void btnCocheModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCocheModificarActionPerformed
        modificarCoche();
    }//GEN-LAST:event_btnCocheModificarActionPerformed

    private void btnVentaNuevaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVentaNuevaActionPerformed
        buscarClienteVentaNueva.setLocationRelativeTo(null);
        cbBuscarClienteVentaNueva.setSelectedIndex(0);
        txtBuscarClienteVentaNueva.setText("");
        txtBuscarClienteVentaNueva.setEditable(false);
        btnCargarClienteVentaNueva.setEnabled(false);
        buscarClienteVentaNueva.setVisible(true);
    }//GEN-LAST:event_btnVentaNuevaActionPerformed

    private void btnRevisionNuevaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRevisionNuevaActionPerformed
        aniadirRevision.setLocationRelativeTo(null);
        try {
            DefaultTableModel modelo = (DefaultTableModel) tablaMain.getModel();
            String bastidor = (String)modelo.getValueAt(tablaMain.getSelectedRow(), 0);
            String marca = (String)modelo.getValueAt(tablaMain.getSelectedRow(), 1);
            String modelocoche = (String)modelo.getValueAt(tablaMain.getSelectedRow(), 2);
            //System.out.println(bastidor+" "+marca+" "+modelocoche);
            lblMarcaRevisionNueva.setText(marca);
            lblModeloRevisionNueva.setText(modelocoche);
            lblBastidorRevisionNueva.setText(bastidor);
            ResultSet rs;
            PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT MAX(N_Revision) FROM Revision");
            int nrevision;
            rs = ps.executeQuery();
            if(rs.next()==false){
                nrevision = 1;
            }else{
                nrevision = rs.getInt("MAX(N_Revision)")+1;
            }
            lblNumeroRevisionNueva.setText(Integer.toString(nrevision));
            lblFechaRevisionNueva.setText(dateFormat.format(date));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        chkAceiteRevisionNueva.setSelected(false);
        chkFrenosRevisionNueva.setSelected(false);
        chkFiltroRevisionNueva.setSelected(false);
        aniadirRevision.setVisible(true);
    }//GEN-LAST:event_btnRevisionNuevaActionPerformed

    private void btnRevisionModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRevisionModificarActionPerformed
        modificarRevision();
    }//GEN-LAST:event_btnRevisionModificarActionPerformed

    private void btnNuevoClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoClienteActionPerformed
        buscarClienteVentaNueva.setVisible(false);
        aniadirVenta.setLocationRelativeTo(null);
        lblFechaVentaNueva.setText(dateFormat.format(date));
        txtNombreVentaNueva.setText("");
        txtNombreVentaNueva.setEditable(true);
        txtApellidosVentaNueva.setText("");
        txtApellidosVentaNueva.setEditable(true);
        txtDniVentaNueva.setText("");
        txtDniVentaNueva.setEditable(true);
        txtTelefonoVentaNueva.setText("");
        txtTelefonoVentaNueva.setEditable(true);
        txtDireccionVentaNueva.setText("");
        txtDireccionVentaNueva.setEditable(true);
        DefaultTableModel modelo = (DefaultTableModel) tablaMain.getModel();
        txtBastidorVentaNueva.setText((String)modelo.getValueAt(tablaMain.getSelectedRow(), 0));
        txtMarcaVentaNueva.setText((String)modelo.getValueAt(tablaMain.getSelectedRow(), 1));
        txtModeloVentaNueva.setText((String)modelo.getValueAt(tablaMain.getSelectedRow(), 2));
        lblPrecioVentaNueva.setText(Float.toString((Float)modelo.getValueAt(tablaMain.getSelectedRow(), 7)));
        aniadirVenta.setVisible(true);
    }//GEN-LAST:event_btnNuevoClienteActionPerformed

    private void btnCargarClienteVentaNuevaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCargarClienteVentaNuevaActionPerformed
        cargarCliente1();
    }//GEN-LAST:event_btnCargarClienteVentaNuevaActionPerformed

    private void btnVentaModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVentaModificarActionPerformed
        String[] options = new String[2];
        options[0] = "Cliente";
        options[1] = "Coche";
        int respuesta = JOptionPane.showOptionDialog(null, "¿Qué desea modificar?", "Modificar venta", 0, JOptionPane.QUESTION_MESSAGE, null, options, null);
        //System.out.println(respuesta);
        if(respuesta==0){//MODIFICAR CLIENTE
            modificarVenta.setLocationRelativeTo(null);
            btnBuscarCoche.setEnabled(false);
            btnBuscarCliente.setEnabled(true);
            try {
                ResultSet rs;
                PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT * FROM Venta WHERE N_Bastidor = ? AND Dni = ?;");
                ps.setString(1,(String)tablaVentas.getValueAt(tablaVentas.getSelectedRow(), 0));
                ps.setString(2,(String)tablaVentas.getValueAt(tablaVentas.getSelectedRow(), 1));
                rs = ps.executeQuery();
                jDateChooser.setDate(dateFormat.parse(rs.getString("Fecha")));
                txtBastidorVentaModificar.setText(rs.getString("N_Bastidor"));
                txtDniVentaModificar.setText(rs.getString("Dni"));
                txtPrecioVentaModificar.setText(Float.toString(rs.getFloat("Precio")));
            } catch (SQLException e) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
            } catch (ParseException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            modificarVenta.setVisible(true);
        }else{//MODIFICAR COCHE
            modificarVenta.setLocationRelativeTo(null);
            btnBuscarCliente.setEnabled(false);
            btnBuscarCoche.setEnabled(true);
            try {
                ResultSet rs;
                PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT * FROM Venta WHERE N_Bastidor = ? AND Dni = ?;");
                ps.setString(1,(String)tablaVentas.getValueAt(tablaVentas.getSelectedRow(), 0));
                ps.setString(2,(String)tablaVentas.getValueAt(tablaVentas.getSelectedRow(), 1));
                rs = ps.executeQuery();
                jDateChooser.setDate(dateFormat.parse(rs.getString("Fecha")));
                txtBastidorVentaModificar.setText(rs.getString("N_Bastidor"));
                txtDniVentaModificar.setText(rs.getString("Dni"));
                txtPrecioVentaModificar.setText(Float.toString(rs.getFloat("Precio")));
            } catch (SQLException e) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
            } catch (ParseException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            modificarVenta.setVisible(true);
        }
    }//GEN-LAST:event_btnVentaModificarActionPerformed

    private void btnBuscarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarClienteActionPerformed
        buscarClienteVentaModificar.setLocationRelativeTo(null);
        cbBuscarClienteVentaModificar.setSelectedIndex(0);
        txtBuscarClienteVentaModificar.setText("");
        txtBuscarClienteVentaModificar.setEditable(false);
        btnCargarClienteVentaModificar.setEnabled(false);
        buscarClienteVentaModificar.setVisible(true);
    }//GEN-LAST:event_btnBuscarClienteActionPerformed

    private void btnBuscarCocheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarCocheActionPerformed
        buscarCocheVentaModificar.setLocationRelativeTo(null);
        cbBuscarCocheVentaModificar.setSelectedIndex(0);
        txtBuscarCocheVentaModificar.setText("");
        txtBuscarCocheVentaModificar.setEditable(false);
        btnCargarCocheVentaModificar.setEnabled(false);
        buscarCocheVentaModificar.setVisible(true);
    }//GEN-LAST:event_btnBuscarCocheActionPerformed

    private void btnCargarClienteVentaModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCargarClienteVentaModificarActionPerformed
        cargarCliente2();
    }//GEN-LAST:event_btnCargarClienteVentaModificarActionPerformed

    private void btnCargarCocheVentaModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCargarCocheVentaModificarActionPerformed
        cargarCoche();
    }//GEN-LAST:event_btnCargarCocheVentaModificarActionPerformed

    private void btnClienteModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClienteModificarActionPerformed
        modificarCliente();
    }//GEN-LAST:event_btnClienteModificarActionPerformed

    private void acercadeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acercadeActionPerformed
        acercaDe.setLocationRelativeTo(null);
        acercaDe.setVisible(true);
    }//GEN-LAST:event_acercadeActionPerformed

    private void btnCancelarCocheNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarCocheNuevoActionPerformed
        aniadirCoche.setVisible(false);
    }//GEN-LAST:event_btnCancelarCocheNuevoActionPerformed

    private void btnCancelarCocheModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarCocheModificarActionPerformed
        modificarCoche.setVisible(false);
    }//GEN-LAST:event_btnCancelarCocheModificarActionPerformed

    private void btnImagenCocheNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImagenCocheNuevoActionPerformed
        //JOptionPane.showMessageDialog(null, "Abre el JFileChooser");
        //Se crea un filtro para los ficheros a leer
        FileNameExtensionFilter filter;
        filter = new FileNameExtensionFilter("Ficheros de imagen", "bmp", "jpg", "gif", "png");
        jFileChooser1.setFileFilter(filter);

        //Valor que retorna al elegir una opcion en el file chooser
        int retVal = this.jFileChooser1.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) //Si se hace doble click o abrir
        {
            //El path absoluto del archivo elegido
            File f = this.jFileChooser1.getSelectedFile();
            System.out.println(f);
            System.out.println("Abriendo");
            BufferedImage myPicture;
            try {
                myPicture = ImageIO.read(f);
                BufferedImage img_reesc = resize(myPicture,180,150);
                //Se guarda en una variable de clase para luego poder insertarla con facilidad en la base de datos

                JLabel picLabel;
                picLabel = new JLabel(new ImageIcon(img_reesc));//Se reescala

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img_reesc, "jpg", baos);//Utiliza un jpg temporal para mostrar imagen
                this.imagenblob = baos.toByteArray();
                
                //Crea un panel donde poner la imagen
                JPanel PanelImagen = new JPanel();
                //Se establece posición y tamaño
                PanelImagen.setBounds(5, 5, 180, 150);
                PanelImagen.add(picLabel);//Se añade la imagen al Panel
                PanelImagen.setName("IMAGEN");//Le pongo un 'name' para que luego lo pueda buscar y eliminar
                pImagenCocheNuevo.add(PanelImagen);//Se añade el Panel de la Imagen
                aniadirCoche.revalidate();//Si hay algún cambio en su interior repinta todo, JPanel6 pertenece a JPanel1
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btnImagenCocheNuevoActionPerformed

    private void btnImagenCocheModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImagenCocheModificarActionPerformed
        JOptionPane.showMessageDialog(null, "Abre el JFileChooser");
    }//GEN-LAST:event_btnImagenCocheModificarActionPerformed

    private void btnCancelarVentaNuevaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarVentaNuevaActionPerformed
        aniadirVenta.setVisible(false);
    }//GEN-LAST:event_btnCancelarVentaNuevaActionPerformed

    private void btnCancelarRevisionNuevaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarRevisionNuevaActionPerformed
        aniadirRevision.setVisible(false);
    }//GEN-LAST:event_btnCancelarRevisionNuevaActionPerformed

    private void btnCancelarRevisionModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarRevisionModificarActionPerformed
        modificarRevision.setVisible(false);
    }//GEN-LAST:event_btnCancelarRevisionModificarActionPerformed

    private void btnCancelarVentaModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarVentaModificarActionPerformed
        modificarVenta.setVisible(false);
    }//GEN-LAST:event_btnCancelarVentaModificarActionPerformed

    private void btnCancelarClienteModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarClienteModificarActionPerformed
        modificarCliente.setVisible(false);
    }//GEN-LAST:event_btnCancelarClienteModificarActionPerformed

    private void salirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_salirActionPerformed
        System.exit(0);
    }//GEN-LAST:event_salirActionPerformed

    private void btnCocheBorrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCocheBorrarActionPerformed
        eliminarCoche();
    }//GEN-LAST:event_btnCocheBorrarActionPerformed

    private void btnRevisionBorrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRevisionBorrarActionPerformed
        eliminarRevision();
    }//GEN-LAST:event_btnRevisionBorrarActionPerformed

    private void btnVentaBorrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVentaBorrarActionPerformed
        int opcion = JOptionPane.showConfirmDialog(null, "¿Desea eliminar la venta seleccionada?", "Eliminar venta", JOptionPane.OK_CANCEL_OPTION);
        if(opcion==0){
            try {
                PreparedStatement ps = this.con.dameconexion().prepareStatement("DELETE FROM Venta WHERE N_Bastidor = ? AND Dni = ?;");
                ps.setString(1,(String)tablaVentas.getValueAt(tablaVentas.getSelectedRow(), 0));
                ps.setString(2,(String)tablaVentas.getValueAt(tablaVentas.getSelectedRow(), 1));
                ps.executeUpdate();
                listarVentas();
                lblFechaVentaMain.setText("");
                lblNombreVentaMain.setText("");
                lblApellidosVentaMain.setText("");
                lblCocheVentaMain.setText("");
                lblDniVentaMain.setText("");
                lblPrecioVentaMain.setText("");
                JOptionPane.showMessageDialog(null, "Venta eliminada correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }//GEN-LAST:event_btnVentaBorrarActionPerformed

    private void btnClienteBorrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClienteBorrarActionPerformed
        eliminarCliente();
    }//GEN-LAST:event_btnClienteBorrarActionPerformed

    private void reiniciarbdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reiniciarbdActionPerformed
        int respuesta = JOptionPane.showConfirmDialog(null, "Esto borrará todos los datos, ¿está seguro?", "Reiniciar base de datos", JOptionPane.WARNING_MESSAGE);
        //System.out.println(respuesta);
        
        if(respuesta==0){
            con.ReiniciaBBDD();
            listarCoches();
            listarCoches2();
            listarVentas();
            listarRevisiones();
            listarClientes();
            listarClientes2();
            listarClientes3();
            limpiarTodo();
        }
    }//GEN-LAST:event_reiniciarbdActionPerformed

    private void btnAceptarCocheNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarCocheNuevoActionPerformed
        insertarCoche();
    }//GEN-LAST:event_btnAceptarCocheNuevoActionPerformed

    private void tablaMainMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaMainMouseReleased
        int fila=this.tablaMain.rowAtPoint(evt.getPoint());
        if(fila!=-1){
            tablaMain.setRowSelectionInterval(fila, fila);
            btnVentaNueva.setEnabled(true);
            btnRevisionNueva.setEnabled(true);
            btnCocheModificar.setEnabled(true);
            btnCocheBorrar.setEnabled(true);
        }
    }//GEN-LAST:event_tablaMainMouseReleased

    private void jTable1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseReleased
        if(jTable1.getSelectedRow()!=-1){
            btnCargarClienteVentaNueva.setEnabled(true);
        }
    }//GEN-LAST:event_jTable1MouseReleased

    private void btnAceptarVentaNuevaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarVentaNuevaActionPerformed
        if(txtNombreVentaNueva.getText().equals("") || txtApellidosVentaNueva.getText().equals("") || txtDniVentaNueva.getText().equals("") || txtTelefonoVentaNueva.getText().equals("") || txtDireccionVentaNueva.getText().equals("")){
            JOptionPane.showMessageDialog(null, "No puede haber ningún campo vacío", "Aviso", JOptionPane.WARNING_MESSAGE);
        }else{
            try {//INSERCIÓN DEL CLIENTE
                String dni2 = txtDniVentaNueva.getText();
                String nombre = txtNombreVentaNueva.getText();
                String apellidos = txtApellidosVentaNueva.getText();
                String telefono = txtTelefonoVentaNueva.getText();
                String direccion = txtDireccionVentaNueva.getText();
                PreparedStatement ps2 = this.con.dameconexion().prepareStatement("INSERT INTO Cliente VALUES (?,?,?,?,?);");
                ps2.setString(1,dni2);
                ps2.setString(2,nombre);
                ps2.setString(3,apellidos);
                ps2.setString(4,telefono);
                ps2.setString(5,direccion);
                ps2.executeUpdate();
                listarClientes();
                listarClientes2();
                listarClientes3();
                JOptionPane.showMessageDialog(null, "Nuevo cliente introducido correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                //System.out.println(ex.getErrorCode());
                if(ex.getErrorCode()==19){}else
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            try {//INSERCIÓN DE LA VENTA
                PreparedStatement ps = this.con.dameconexion().prepareStatement("INSERT INTO Venta VALUES (?,?,?,ROUND(?,2));");
                ps.setString(1,txtBastidorVentaNueva.getText());
                ps.setString(2,txtDniVentaNueva.getText());
                ps.setString(3,lblFechaVentaNueva.getText());
                ps.setFloat(4, Float.parseFloat(lblPrecioVentaNueva.getText()));
                ps.executeUpdate();
                listarVentas();
                JOptionPane.showMessageDialog(null, "Nueva venta introducida correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                if(ex.getErrorCode()==19){
                    JOptionPane.showMessageDialog(null, "Ya existe una venta con estos datos", "Información", JOptionPane.INFORMATION_MESSAGE);
                }else
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            aniadirVenta.setVisible(false);
        }
    }//GEN-LAST:event_btnAceptarVentaNuevaActionPerformed

    private void btnAceptarRevisionNuevaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarRevisionNuevaActionPerformed
        if(!chkAceiteRevisionNueva.isSelected() && !chkFiltroRevisionNueva.isSelected() && !chkFrenosRevisionNueva.isSelected()){
            JOptionPane.showMessageDialog(null, "Debe haber algún campo seleccionado", "Aviso", JOptionPane.WARNING_MESSAGE);
        }else{
            try {
                PreparedStatement ps = this.con.dameconexion().prepareStatement("INSERT INTO Revision VALUES (?,?,?,?,?,?);");
                ps.setInt(1, Integer.parseInt(lblNumeroRevisionNueva.getText()));
                ps.setString(2,lblFechaRevisionNueva.getText());
                if(chkFrenosRevisionNueva.isSelected())
                    ps.setString(3, "Sí");
                else
                    ps.setString(3, "No");
                
                if(chkAceiteRevisionNueva.isSelected())
                    ps.setString(4, "Sí");
                else
                    ps.setString(4, "No");
                
                if(chkFiltroRevisionNueva.isSelected())
                    ps.setString(5, "Sí");
                else
                    ps.setString(5, "No");
                
                ps.setString(6,lblBastidorRevisionNueva.getText());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(null, "Revisión introducida correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);
                listarRevisiones();
                aniadirRevision.setVisible(false);
            } catch (SQLException ex) {
                if(ex.getErrorCode()==19)
                    JOptionPane.showMessageDialog(null, "Ya existe una revisión con estos datos", "Información", JOptionPane.INFORMATION_MESSAGE);
                else
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btnAceptarRevisionNuevaActionPerformed

    private void btnAceptarCocheModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarCocheModificarActionPerformed
        if(txtMarcaCocheModificar.getText().equals("") || txtModeloCocheModificar.getText().equals("") || txtMotorCocheModificar.getText().equals("") || txtCVCocheModificar.getText().equals("") || txtColorCocheModificar.getText().equals("") || txtPrecioCocheModificar.getText().equals("")){
            JOptionPane.showMessageDialog(null, "Ningún campo debe estar vacío", "Aviso", JOptionPane.WARNING_MESSAGE);
        }else{
            int cv = 0;
            float precio = 0;
            boolean inserta = true;
            
            try {
                cv = Integer.parseInt(txtCVCocheModificar.getText());
            } catch (NumberFormatException e) {
                inserta = false;
                JOptionPane.showMessageDialog(null, "Los CV deben ser un número entero", "Aviso", JOptionPane.WARNING_MESSAGE);
            }

            try {
                precio = Float.parseFloat(txtPrecioCocheModificar.getText());
            } catch (NumberFormatException e) {
                inserta = false;
                JOptionPane.showMessageDialog(null, "El precio debe ser un número real", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
            
            if(inserta){
                try {
                    PreparedStatement ps = this.con.dameconexion().prepareStatement("UPDATE Coche SET Marca=?,Modelo=?,Motor=?,CV=?,Tipo=?,Color=?,Precio=ROUND(?,2) WHERE N_Bastidor = ?;");
                    ps.setString(1,txtMarcaCocheModificar.getText());
                    ps.setString(2,txtModeloCocheModificar.getText());
                    ps.setString(3, txtMotorCocheModificar.getText());
                    ps.setInt(4, cv);
                    ps.setString(5,(String)cbTipoCocheModificar.getSelectedItem());
                    ps.setString(6,txtColorCocheModificar.getText());
                    
                    
                    
                    ps.setFloat(7, precio);
                    ps.setString(8,txtBastidorCocheModificar.getText());
                    ps.executeUpdate();
                    listarCoches();
                    listarCoches2();
                    listarRevisiones();
                    listarVentas();
                    rellenarComboMarca();
                    rellenarComboMarcaInformes();
                    rellenarComboMotor();
                    JOptionPane.showMessageDialog(null, "Coche modificado correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);
                    modificarCoche.setVisible(false);
                } catch (SQLException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_btnAceptarCocheModificarActionPerformed

    private void tablaRevisionesMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaRevisionesMouseReleased
        int fila=this.tablaRevisiones.rowAtPoint(evt.getPoint());
        if(fila!=-1){
            try {
                tablaRevisiones.setRowSelectionInterval(fila,fila);
                ResultSet rs;
                PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT r.N_Revision,r.Fecha,r.N_Bastidor,c.Marca,c.Modelo,r.Frenos,r.Filtro,r.Aceite,c.Img FROM Revision AS r, Coche AS c WHERE N_Revision=? AND r.N_Bastidor=c.N_Bastidor;");
                ps.setInt(1, (int)tablaRevisiones.getValueAt(tablaRevisiones.getSelectedRow(), 0));
                rs = ps.executeQuery();
                lblNumeroRevisionMain.setText(Integer.toString(rs.getInt("N_Revision")));
                lblFechaRevisionMain.setText(rs.getString("Fecha"));
                lblBastidorRevisionMain.setText(rs.getString("N_Bastidor"));
                lblMarcaRevisionMain.setText(rs.getString("Marca"));
                lblModeloRevisionMain.setText(rs.getString("Modelo"));
                lblFrenosRevisionMain.setText(rs.getString("Frenos"));
                lblFiltroRevisionMain.setText(rs.getString("Filtro"));
                lblAceiteRevisionMain.setText(rs.getString("Aceite"));
                
                pImagenRevisionMain.removeAll();
                JPanel PanelImagen = new JPanel();
                byte[] imagen_bytes=rs.getBytes("Img");//Toma el campo img de la base de datos en forma de bytes 

                JLabel picLabel;
                picLabel = new JLabel(new ImageIcon(imagen_bytes));//Se reescala
                PanelImagen.setBounds(5, 5, 180, 150);
                PanelImagen.add(picLabel);//Se añade la imagen al Panel
                PanelImagen.setName("IMAGEN"); //Se añade un NAME para luego poder buscarlo entre todos los componentes
                pImagenRevisionMain.add(PanelImagen);//Se añade el Panel de la Imagen
                revisiones.revalidate();
            } catch (Exception e) {
                e.printStackTrace();
            }
            btnRevisionModificar.setEnabled(true);
            btnRevisionBorrar.setEnabled(true);
        }
    }//GEN-LAST:event_tablaRevisionesMouseReleased

    private void btnAceptarVentaModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarVentaModificarActionPerformed
        try {
            PreparedStatement ps = this.con.dameconexion().prepareStatement("UPDATE Venta SET N_Bastidor=?, Dni=?, Fecha=?, Precio=? WHERE N_Bastidor=? AND Dni=?;");
            ps.setString(1, txtBastidorVentaModificar.getText());
            ps.setString(2,txtDniVentaModificar.getText());
            ps.setString(3, dateFormat.format(jDateChooser.getDate()));
            ps.setString(4,txtPrecioVentaModificar.getText());
            ps.setString(5,(String)tablaVentas.getValueAt(tablaVentas.getSelectedRow(), 0));
            ps.setString(6,(String)tablaVentas.getValueAt(tablaVentas.getSelectedRow(), 1));
            ps.executeUpdate();
            listarVentas();
            lblFechaVentaMain.setText("");
            lblNombreVentaMain.setText("");
            lblApellidosVentaMain.setText("");
            lblCocheVentaMain.setText("");
            lblDniVentaMain.setText("");
            lblPrecioVentaMain.setText("");
            JOptionPane.showMessageDialog(null, "Venta modificada correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);
            modificarVenta.setVisible(false);
        } catch (SQLException ex) {
            if(ex.getErrorCode()==19){
                JOptionPane.showMessageDialog(null, "Ya existe una venta con estos datos", "Información", JOptionPane.INFORMATION_MESSAGE);
            }else
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAceptarVentaModificarActionPerformed

    private void btnAceptarClienteModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarClienteModificarActionPerformed
        if(txtNombreClienteModificar.getText().equals("") || txtApellidosClienteModificar.getText().equals("") || txtTelefonoClienteModificar.getText().equals("") || txtDireccionClienteModificar.getText().equals("")){
            JOptionPane.showMessageDialog(null, "No puede haber ningún campo vacío", "Aviso", JOptionPane.WARNING_MESSAGE);
        }else{
            try {
                PreparedStatement ps = this.con.dameconexion().prepareStatement("UPDATE Cliente SET Nombre=?,Apellidos=?, Telefono=?, Domicilio=? WHERE Dni=?;");
                ps.setString(1,txtNombreClienteModificar.getText());
                ps.setString(2,txtApellidosClienteModificar.getText());
                ps.setString(3,txtTelefonoClienteModificar.getText());
                ps.setString(4,txtDireccionClienteModificar.getText());
                ps.setString(5,txtDniClienteModificar.getText());
                ps.executeUpdate();
                listarClientes();
                listarClientes2();
                listarClientes3();
                listarVentas();
                lblDniClienteMain.setText("");
                lblNombreClienteMain.setText("");
                lblApellidosClienteMain.setText("");
                lblTelefonoClienteMain.setText("");
                lblDireccionClienteMain.setText("");
                JOptionPane.showMessageDialog(null, "Cliente modificado correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);
                modificarCliente.setVisible(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_btnAceptarClienteModificarActionPerformed

    private void btnAceptarRevisionModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarRevisionModificarActionPerformed
        if(!chkAceiteRevisionModificar.isSelected() && !chkFiltroRevisionModificar.isSelected() && !chkFrenosRevisionModificar.isSelected()){
            JOptionPane.showMessageDialog(null, "Debe haber algún campo seleccionado", "Aviso", JOptionPane.WARNING_MESSAGE);
        }else{
            try {
                PreparedStatement ps = this.con.dameconexion().prepareStatement("UPDATE Revision SET Frenos=?,Aceite=?,Filtro=? WHERE N_Revision=?;");
                
                if(chkFrenosRevisionModificar.isSelected())
                    ps.setString(1, "Sí");
                else
                    ps.setString(1, "No");
                
                if(chkAceiteRevisionModificar.isSelected())
                    ps.setString(2, "Sí");
                else
                    ps.setString(2, "No");
                
                if(chkFiltroRevisionModificar.isSelected())
                    ps.setString(3, "Sí");
                else
                    ps.setString(3, "No");
                
                ps.setInt(4, Integer.parseInt(lblNumeroRevisionModificar.getText()));
                
                ps.executeUpdate();
                JOptionPane.showMessageDialog(null, "Revisión modificada correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);
                listarRevisiones();
                lblNumeroRevisionMain.setText("");
                lblFechaRevisionMain.setText("");
                lblBastidorRevisionMain.setText("");
                lblMarcaRevisionMain.setText("");
                lblModeloRevisionMain.setText("");
                lblFiltroRevisionMain.setText("");
                lblFrenosRevisionMain.setText("");
                lblAceiteRevisionMain.setText("");
                pImagenRevisionMain.removeAll();
                modificarRevision.setVisible(false);
            } catch (SQLException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btnAceptarRevisionModificarActionPerformed

    private void tablaVentasMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaVentasMouseReleased
        int fila=this.tablaVentas.rowAtPoint(evt.getPoint());
        if(fila!=-1){
            try {
                tablaVentas.setRowSelectionInterval(fila, fila);
                ResultSet rs;
                PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT v.Fecha,cl.Nombre,cl.Apellidos,cl.Dni,co.Marca || ' ' || co.Modelo AS Coche,v.Precio FROM Venta AS v,Coche AS co,Cliente AS cl WHERE v.N_Bastidor=? AND v.Dni=? AND v.N_Bastidor=co.N_Bastidor AND v.Dni=cl.Dni;");
                ps.setString(1, (String)tablaVentas.getValueAt(tablaVentas.getSelectedRow(), 0));
                ps.setString(2, (String)tablaVentas.getValueAt(tablaVentas.getSelectedRow(), 1));
                rs = ps.executeQuery();
                lblFechaVentaMain.setText(rs.getString("Fecha"));
                lblNombreVentaMain.setText(rs.getString("Nombre"));
                lblApellidosVentaMain.setText(rs.getString("Apellidos"));
                lblDniVentaMain.setText(rs.getString("Dni"));
                lblCocheVentaMain.setText(rs.getString("Coche"));
                lblPrecioVentaMain.setText(Float.toString(rs.getFloat("Precio")));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            btnVentaModificar.setEnabled(true);
            btnVentaBorrar.setEnabled(true);
        }
    }//GEN-LAST:event_tablaVentasMouseReleased

    private void tablaClientesMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaClientesMouseReleased
        int fila = this.tablaClientes.rowAtPoint(evt.getPoint());
        if(fila!=-1){
            try {
                tablaClientes.setRowSelectionInterval(fila, fila);
                ResultSet rs;
                PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT * FROM Cliente WHERE Dni=?;");
                ps.setString(1, (String)tablaClientes.getValueAt(tablaClientes.getSelectedRow(), 0));
                rs = ps.executeQuery();
                lblNombreClienteMain.setText(rs.getString("Nombre"));
                lblApellidosClienteMain.setText(rs.getString("Apellidos"));
                lblDniClienteMain.setText(rs.getString("Dni"));
                lblTelefonoClienteMain.setText(rs.getString("Telefono"));
                lblDireccionClienteMain.setText(rs.getString("Domicilio"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            btnClienteModificar.setEnabled(true);
            btnClienteBorrar.setEnabled(true);
        }
    }//GEN-LAST:event_tablaClientesMouseReleased

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
        limpiarTodo();
    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void cbBuscarMainItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbBuscarMainItemStateChanged
        if(cbBuscarMain.getSelectedIndex()!=0){
            txtBuscarMain.setEditable(true);
            btnBuscarMain.setEnabled(true);
        }else{
            txtBuscarMain.setText("");
            txtBuscarMain.setEditable(false);
            btnBuscarMain.setEnabled(false);
            listarCoches();
        }
    }//GEN-LAST:event_cbBuscarMainItemStateChanged

    private void btnBuscarMainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarMainActionPerformed
        if(txtBuscarMain.getText().equals("")){
            JOptionPane.showMessageDialog(null, "El campo no puede estar vacío", "Aviso", JOptionPane.WARNING_MESSAGE);
        }else{
            String campo = (String) cbBuscarMain.getSelectedItem();
            if(campo.equals("Bastidor"))
                campo = "N_Bastidor";
            
            listarCochesFiltro(campo, txtBuscarMain.getText());
        }
    }//GEN-LAST:event_btnBuscarMainActionPerformed

    private void txtBuscarMainKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarMainKeyReleased
        //System.out.println(evt.getKeyCode());
        if(evt.getKeyCode()==10){
            if(txtBuscarMain.getText().equals("")){
                JOptionPane.showMessageDialog(null, "El campo no puede estar vacío", "Aviso", JOptionPane.WARNING_MESSAGE);
            }else{
                String campo = (String) cbBuscarMain.getSelectedItem();
                if(campo.equals("Bastidor"))
                    campo = "N_Bastidor";

                listarCochesFiltro(campo, txtBuscarMain.getText());
            }
        }
    }//GEN-LAST:event_txtBuscarMainKeyReleased

    private void cbMarcaMainItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbMarcaMainItemStateChanged
        if(cbMarcaMain.getSelectedIndex()>0 && cbMotorMain.getSelectedIndex()>0){
            try {
                ResultSet rs;
                PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT N_Bastidor,Marca,Modelo,Motor,CV,Tipo,Color,Precio FROM Coche WHERE Marca=? AND Motor=?;");
                ps.setString(1, (String)cbMarcaMain.getSelectedItem());
                ps.setString(2, (String)cbMotorMain.getSelectedItem());
                rs = ps.executeQuery();
                
                DefaultTableModel modeloCoches = (DefaultTableModel)tablaMain.getModel();
                for(int i=modeloCoches.getRowCount()-1; i>=0; i--) {
                    modeloCoches.removeRow(i);
                }
                
                while (rs.next()){ 
                    modeloCoches.addRow(new Object[]{rs.getString("N_Bastidor"), rs.getString("Marca"), rs.getString("Modelo"), rs.getString("Motor"), rs.getInt("CV"), rs.getString("Tipo"), rs.getString("Color"), rs.getFloat("Precio")});
                }
                rs.close();
                btnVentaNueva.setEnabled(false);
                btnRevisionNueva.setEnabled(false);
                btnCocheModificar.setEnabled(false);
                btnCocheBorrar.setEnabled(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(cbMarcaMain.getSelectedIndex()>0 && cbMotorMain.getSelectedIndex()<=0){
            try {
                ResultSet rs;
                PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT N_Bastidor,Marca,Modelo,Motor,CV,Tipo,Color,Precio FROM Coche WHERE Marca=?;");
                ps.setString(1, (String)cbMarcaMain.getSelectedItem());
                rs = ps.executeQuery();
                
                DefaultTableModel modeloCoches = (DefaultTableModel)tablaMain.getModel();
                for(int i=modeloCoches.getRowCount()-1; i>=0; i--) {
                    modeloCoches.removeRow(i);
                }
                
                while (rs.next()){ 
                    modeloCoches.addRow(new Object[]{rs.getString("N_Bastidor"), rs.getString("Marca"), rs.getString("Modelo"), rs.getString("Motor"), rs.getInt("CV"), rs.getString("Tipo"), rs.getString("Color"), rs.getFloat("Precio")});
                }
                rs.close();
                btnVentaNueva.setEnabled(false);
                btnRevisionNueva.setEnabled(false);
                btnCocheModificar.setEnabled(false);
                btnCocheBorrar.setEnabled(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(cbMarcaMain.getSelectedIndex()<=0 && cbMotorMain.getSelectedIndex()>0){
            try {
                ResultSet rs;
                PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT N_Bastidor,Marca,Modelo,Motor,CV,Tipo,Color,Precio FROM Coche WHERE Motor=?;");
                ps.setString(1, (String)cbMotorMain.getSelectedItem());
                rs = ps.executeQuery();
                
                DefaultTableModel modeloCoches = (DefaultTableModel)tablaMain.getModel();
                for(int i=modeloCoches.getRowCount()-1; i>=0; i--) {
                    modeloCoches.removeRow(i);
                }
                
                while (rs.next()){ 
                    modeloCoches.addRow(new Object[]{rs.getString("N_Bastidor"), rs.getString("Marca"), rs.getString("Modelo"), rs.getString("Motor"), rs.getInt("CV"), rs.getString("Tipo"), rs.getString("Color"), rs.getFloat("Precio")});
                }
                rs.close();
                btnVentaNueva.setEnabled(false);
                btnRevisionNueva.setEnabled(false);
                btnCocheModificar.setEnabled(false);
                btnCocheBorrar.setEnabled(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            listarCoches();
        }
        cbBuscarMain.setSelectedIndex(0);
    }//GEN-LAST:event_cbMarcaMainItemStateChanged

    private void cbMotorMainItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbMotorMainItemStateChanged
        if(cbMotorMain.getSelectedIndex()>0 && cbMarcaMain.getSelectedIndex()>0){
            try {
                ResultSet rs;
                PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT N_Bastidor,Marca,Modelo,Motor,CV,Tipo,Color,Precio FROM Coche WHERE Marca=? AND Motor=?;");
                ps.setString(1, (String)cbMarcaMain.getSelectedItem());
                ps.setString(2, (String)cbMotorMain.getSelectedItem());
                rs = ps.executeQuery();
                
                DefaultTableModel modeloCoches = (DefaultTableModel)tablaMain.getModel();
                for(int i=modeloCoches.getRowCount()-1; i>=0; i--) {
                    modeloCoches.removeRow(i);
                }
                
                while (rs.next()){ 
                    modeloCoches.addRow(new Object[]{rs.getString("N_Bastidor"), rs.getString("Marca"), rs.getString("Modelo"), rs.getString("Motor"), rs.getInt("CV"), rs.getString("Tipo"), rs.getString("Color"), rs.getFloat("Precio")});
                }
                rs.close();
                btnVentaNueva.setEnabled(false);
                btnRevisionNueva.setEnabled(false);
                btnCocheModificar.setEnabled(false);
                btnCocheBorrar.setEnabled(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(cbMotorMain.getSelectedIndex()>0 && cbMarcaMain.getSelectedIndex()<=0){
            try {
                ResultSet rs;
                PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT N_Bastidor,Marca,Modelo,Motor,CV,Tipo,Color,Precio FROM Coche WHERE Motor=?;");
                ps.setString(1, (String)cbMotorMain.getSelectedItem());
                rs = ps.executeQuery();
                
                DefaultTableModel modeloCoches = (DefaultTableModel)tablaMain.getModel();
                for(int i=modeloCoches.getRowCount()-1; i>=0; i--) {
                    modeloCoches.removeRow(i);
                }
                
                while (rs.next()){ 
                    modeloCoches.addRow(new Object[]{rs.getString("N_Bastidor"), rs.getString("Marca"), rs.getString("Modelo"), rs.getString("Motor"), rs.getInt("CV"), rs.getString("Tipo"), rs.getString("Color"), rs.getFloat("Precio")});
                }
                rs.close();
                btnVentaNueva.setEnabled(false);
                btnRevisionNueva.setEnabled(false);
                btnCocheModificar.setEnabled(false);
                btnCocheBorrar.setEnabled(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(cbMotorMain.getSelectedIndex()<=0 && cbMarcaMain.getSelectedIndex()>0){
            try {
                ResultSet rs;
                PreparedStatement ps = this.con.dameconexion().prepareStatement("SELECT N_Bastidor,Marca,Modelo,Motor,CV,Tipo,Color,Precio FROM Coche WHERE Marca=?;");
                ps.setString(1, (String)cbMarcaMain.getSelectedItem());
                rs = ps.executeQuery();
                
                DefaultTableModel modeloCoches = (DefaultTableModel)tablaMain.getModel();
                for(int i=modeloCoches.getRowCount()-1; i>=0; i--) {
                    modeloCoches.removeRow(i);
                }
                
                while (rs.next()){ 
                    modeloCoches.addRow(new Object[]{rs.getString("N_Bastidor"), rs.getString("Marca"), rs.getString("Modelo"), rs.getString("Motor"), rs.getInt("CV"), rs.getString("Tipo"), rs.getString("Color"), rs.getFloat("Precio")});
                }
                rs.close();
                btnVentaNueva.setEnabled(false);
                btnRevisionNueva.setEnabled(false);
                btnCocheModificar.setEnabled(false);
                btnCocheBorrar.setEnabled(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            listarCoches();
        }
        cbBuscarMain.setSelectedIndex(0);
    }//GEN-LAST:event_cbMotorMainItemStateChanged

    private void btnReiniciarBusquedaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReiniciarBusquedaActionPerformed
        cbBuscarMain.setSelectedIndex(0);
        cbMarcaMain.setSelectedIndex(0);
        cbMotorMain.setSelectedIndex(0);
        listarCoches();
    }//GEN-LAST:event_btnReiniciarBusquedaActionPerformed

    private void btnBuscarClienteVentaNuevaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarClienteVentaNuevaActionPerformed
        if(txtBuscarClienteVentaNueva.getText().equals("")){
            JOptionPane.showMessageDialog(null, "El campo no puede estar vacío", "Aviso", JOptionPane.WARNING_MESSAGE);
        }else{
            String campo = (String) cbBuscarClienteVentaNueva.getSelectedItem();
            
            listarClientes2Filtro(campo, txtBuscarClienteVentaNueva.getText());
        }
    }//GEN-LAST:event_btnBuscarClienteVentaNuevaActionPerformed

    private void cbBuscarClienteVentaNuevaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbBuscarClienteVentaNuevaItemStateChanged
        if(cbBuscarClienteVentaNueva.getSelectedIndex()!=0){
            txtBuscarClienteVentaNueva.setEditable(true);
            btnBuscarClienteVentaNueva.setEnabled(true);
        }else{
            txtBuscarClienteVentaNueva.setText("");
            txtBuscarClienteVentaNueva.setEditable(false);
            btnBuscarClienteVentaNueva.setEnabled(false);
            listarClientes2();
        }
    }//GEN-LAST:event_cbBuscarClienteVentaNuevaItemStateChanged

    private void txtBuscarClienteVentaNuevaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarClienteVentaNuevaKeyReleased
        if(evt.getKeyCode()==10){
            if(txtBuscarClienteVentaNueva.getText().equals("")){
                JOptionPane.showMessageDialog(null, "El campo no puede estar vacío", "Aviso", JOptionPane.WARNING_MESSAGE);
            }else{
                String campo = (String) cbBuscarClienteVentaNueva.getSelectedItem();

                listarClientes2Filtro(campo, txtBuscarClienteVentaNueva.getText());
            }
        }
    }//GEN-LAST:event_txtBuscarClienteVentaNuevaKeyReleased

    private void cbBuscarClienteVentaModificarItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbBuscarClienteVentaModificarItemStateChanged
        if(cbBuscarClienteVentaModificar.getSelectedIndex()!=0){
            txtBuscarClienteVentaModificar.setEditable(true);
            btnBuscarClienteVentaModificar.setEnabled(true);
        }else{
            txtBuscarClienteVentaModificar.setText("");
            txtBuscarClienteVentaModificar.setEditable(false);
            btnBuscarClienteVentaModificar.setEnabled(false);
            listarClientes3();
        }
    }//GEN-LAST:event_cbBuscarClienteVentaModificarItemStateChanged

    private void btnBuscarClienteVentaModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarClienteVentaModificarActionPerformed
        if(txtBuscarClienteVentaModificar.getText().equals("")){
            JOptionPane.showMessageDialog(null, "El campo no puede estar vacío", "Aviso", JOptionPane.WARNING_MESSAGE);
        }else{
            String campo = (String) cbBuscarClienteVentaModificar.getSelectedItem();
            
            listarClientes3Filtro(campo, txtBuscarClienteVentaModificar.getText());
        }
    }//GEN-LAST:event_btnBuscarClienteVentaModificarActionPerformed

    private void jTable2MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable2MouseReleased
        if(jTable2.getSelectedRow()!=-1){
            btnCargarClienteVentaModificar.setEnabled(true);
        }
    }//GEN-LAST:event_jTable2MouseReleased

    private void cbBuscarCocheVentaModificarItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbBuscarCocheVentaModificarItemStateChanged
        if(cbBuscarCocheVentaModificar.getSelectedIndex()!=0){
            txtBuscarCocheVentaModificar.setEditable(true);
            btnBuscarCocheVentaModificar.setEnabled(true);
        }else{
            txtBuscarCocheVentaModificar.setText("");
            txtBuscarCocheVentaModificar.setEditable(false);
            btnBuscarCocheVentaModificar.setEnabled(false);
            listarCoches2();
        }
    }//GEN-LAST:event_cbBuscarCocheVentaModificarItemStateChanged

    private void btnBuscarCocheVentaModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarCocheVentaModificarActionPerformed
        if(txtBuscarCocheVentaModificar.getText().equals("")){
            JOptionPane.showMessageDialog(null, "El campo no puede estar vacío", "Aviso", JOptionPane.WARNING_MESSAGE);
        }else{
            String campo = (String) cbBuscarCocheVentaModificar.getSelectedItem();
            if(campo.equals("Bastidor"))
                campo = "N_Bastidor";

            listarCoches2Filtro(campo, txtBuscarCocheVentaModificar.getText());
        }
    }//GEN-LAST:event_btnBuscarCocheVentaModificarActionPerformed

    private void txtBuscarCocheVentaModificarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarCocheVentaModificarKeyReleased
        if(evt.getKeyCode()==10){
            if(txtBuscarCocheVentaModificar.getText().equals("")){
                JOptionPane.showMessageDialog(null, "El campo no puede estar vacío", "Aviso", JOptionPane.WARNING_MESSAGE);
            }else{
                String campo = (String) cbBuscarCocheVentaModificar.getSelectedItem();
                if(campo.equals("Bastidor"))
                    campo = "N_Bastidor";

                listarCoches2Filtro(campo, txtBuscarCocheVentaModificar.getText());
            }
        }
    }//GEN-LAST:event_txtBuscarCocheVentaModificarKeyReleased

    private void jTable3MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable3MouseReleased
        if(jTable3.getSelectedRow()!=-1){
            btnCargarCocheVentaModificar.setEnabled(true);
        }
    }//GEN-LAST:event_jTable3MouseReleased

    private void nuevococheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nuevococheActionPerformed
        btnCocheNuevoActionPerformed(evt);
    }//GEN-LAST:event_nuevococheActionPerformed

    private void tablaMainMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaMainMouseClicked
        if(evt.getClickCount()==2)
            modificarCoche();
        
        if(evt.getButton()==3)
            this.popupCoche.show(evt.getComponent(),evt.getX(), evt.getY());
    }//GEN-LAST:event_tablaMainMouseClicked

    private void editarcocheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editarcocheActionPerformed
        modificarCoche();
    }//GEN-LAST:event_editarcocheActionPerformed

    private void borrarcocheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_borrarcocheActionPerformed
        eliminarCoche();
    }//GEN-LAST:event_borrarcocheActionPerformed

    private void tablaRevisionesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaRevisionesMouseClicked
        if(evt.getClickCount()==2)
            modificarRevision();
        
        if(evt.getButton()==3)
            this.popupRevision.show(evt.getComponent(),evt.getX(), evt.getY());
    }//GEN-LAST:event_tablaRevisionesMouseClicked

    private void editarRevisionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editarRevisionActionPerformed
        modificarRevision();
    }//GEN-LAST:event_editarRevisionActionPerformed

    private void borrarRevisionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_borrarRevisionActionPerformed
        eliminarRevision();
    }//GEN-LAST:event_borrarRevisionActionPerformed

    private void tablaVentasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaVentasMouseClicked
        if(evt.getClickCount()==2)
            modificarVenta();
        
        if(evt.getButton()==3)
            this.popupVenta.show(evt.getComponent(),evt.getX(), evt.getY());
    }//GEN-LAST:event_tablaVentasMouseClicked

    private void editarVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editarVentaActionPerformed
        modificarVenta();
    }//GEN-LAST:event_editarVentaActionPerformed

    private void borrarVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_borrarVentaActionPerformed
        eliminarVenta();
    }//GEN-LAST:event_borrarVentaActionPerformed

    private void tablaClientesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaClientesMouseClicked
        if(evt.getClickCount()==2)
            modificarCliente();
        
        if(evt.getButton()==3)
            this.popupCliente.show(evt.getComponent(),evt.getX(), evt.getY());
    }//GEN-LAST:event_tablaClientesMouseClicked

    private void editarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editarClienteActionPerformed
        modificarCliente();
    }//GEN-LAST:event_editarClienteActionPerformed

    private void borrarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_borrarClienteActionPerformed
        eliminarCliente();
    }//GEN-LAST:event_borrarClienteActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        if(evt.getClickCount()==2)
            cargarCliente1();
    }//GEN-LAST:event_jTable1MouseClicked

    private void jTable2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable2MouseClicked
        if(evt.getClickCount()==2)
            cargarCliente2();
    }//GEN-LAST:event_jTable2MouseClicked

    private void jTable3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable3MouseClicked
        if(evt.getClickCount()==2)
            cargarCoche();
    }//GEN-LAST:event_jTable3MouseClicked

    private void tablaMainKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tablaMainKeyReleased
        //66='b'    69='e'  127='suprimir'
        //System.out.println(evt.getKeyCode());
        if(evt.getKeyCode()==66 || evt.getKeyCode()==127)
            eliminarCoche();
        
        if(evt.getKeyCode()==69)
            modificarCoche();
    }//GEN-LAST:event_tablaMainKeyReleased

    private void tablaRevisionesKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tablaRevisionesKeyReleased
        if(evt.getKeyCode()==66 || evt.getKeyCode()==127)
            eliminarRevision();
        
        if(evt.getKeyCode()==69)
            modificarRevision();
    }//GEN-LAST:event_tablaRevisionesKeyReleased

    private void tablaVentasKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tablaVentasKeyReleased
        if(evt.getKeyCode()==66 || evt.getKeyCode()==127)
            eliminarVenta();
        
        if(evt.getKeyCode()==69)
            modificarVenta();
    }//GEN-LAST:event_tablaVentasKeyReleased

    private void tablaClientesKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tablaClientesKeyReleased
        if(evt.getKeyCode()==66 || evt.getKeyCode()==127)
            eliminarCliente();
        
        if(evt.getKeyCode()==69)
            modificarCliente();
    }//GEN-LAST:event_tablaClientesKeyReleased

    private void generaInformeCochesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generaInformeCochesActionPerformed
        genera_informe("%", "informeCoches.jrxml");
    }//GEN-LAST:event_generaInformeCochesActionPerformed

    private void generaInformeRevisionesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generaInformeRevisionesActionPerformed
        genera_informe("%", "informeRevisiones.jrxml");
    }//GEN-LAST:event_generaInformeRevisionesActionPerformed

    private void generaInformeVentasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generaInformeVentasActionPerformed
        genera_informe("%", "informeVentas.jrxml");
    }//GEN-LAST:event_generaInformeVentasActionPerformed

    private void generaInformeClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generaInformeClientesActionPerformed
        genera_informe("%", "informeClientes.jrxml");
    }//GEN-LAST:event_generaInformeClientesActionPerformed

    private void btnAceptarInformeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarInformeActionPerformed
        int seleccion = cbMarcasInforme.getSelectedIndex();
        if(seleccion>0){
            genera_informe((String)cbMarcasInforme.getSelectedItem(),"informeCoches.jrxml");
            seleccionMarca.setVisible(false);
        }else{
            JOptionPane.showMessageDialog(null, "Debe seleccionar una marca", "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btnAceptarInformeActionPerformed

    private void btnCancelarInformeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarInformeActionPerformed
        seleccionMarca.setVisible(false);
    }//GEN-LAST:event_btnCancelarInformeActionPerformed

    private void generaInformeCochesMarcaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generaInformeCochesMarcaActionPerformed
        seleccionMarca.setLocationRelativeTo(null);
        cbMarcasInforme.setSelectedIndex(0);
        seleccionMarca.setVisible(true);
    }//GEN-LAST:event_generaInformeCochesMarcaActionPerformed

    private void manualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualActionPerformed
        try {
            File manual = new File("MANUAL.pdf");
            Desktop.getDesktop().open(manual);
        }catch (IOException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_manualActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        JFrame concesionario = new Main();
        concesionario.setLocationRelativeTo(null);
        concesionario.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog acercaDe;
    private javax.swing.JMenuItem acercade;
    private javax.swing.JDialog aniadirCoche;
    private javax.swing.JDialog aniadirRevision;
    private javax.swing.JDialog aniadirVenta;
    private javax.swing.JMenu archivo;
    private javax.swing.JMenu ayuda;
    private javax.swing.JMenuItem borrarCliente;
    private javax.swing.JMenuItem borrarRevision;
    private javax.swing.JMenuItem borrarVenta;
    private javax.swing.JMenuItem borrarcoche;
    private javax.swing.JButton btnAceptarClienteModificar;
    private javax.swing.JButton btnAceptarCocheModificar;
    private javax.swing.JButton btnAceptarCocheNuevo;
    private javax.swing.JButton btnAceptarInforme;
    private javax.swing.JButton btnAceptarRevisionModificar;
    private javax.swing.JButton btnAceptarRevisionNueva;
    private javax.swing.JButton btnAceptarVentaModificar;
    private javax.swing.JButton btnAceptarVentaNueva;
    private javax.swing.JButton btnBuscarCliente;
    private javax.swing.JButton btnBuscarClienteVentaModificar;
    private javax.swing.JButton btnBuscarClienteVentaNueva;
    private javax.swing.JButton btnBuscarCoche;
    private javax.swing.JButton btnBuscarCocheVentaModificar;
    private javax.swing.JButton btnBuscarMain;
    private javax.swing.JButton btnCancelarClienteModificar;
    private javax.swing.JButton btnCancelarCocheModificar;
    private javax.swing.JButton btnCancelarCocheNuevo;
    private javax.swing.JButton btnCancelarInforme;
    private javax.swing.JButton btnCancelarRevisionModificar;
    private javax.swing.JButton btnCancelarRevisionNueva;
    private javax.swing.JButton btnCancelarVentaModificar;
    private javax.swing.JButton btnCancelarVentaNueva;
    private javax.swing.JButton btnCargarClienteVentaModificar;
    private javax.swing.JButton btnCargarClienteVentaNueva;
    private javax.swing.JButton btnCargarCocheVentaModificar;
    private javax.swing.JButton btnClienteBorrar;
    private javax.swing.JButton btnClienteModificar;
    private javax.swing.JButton btnCocheBorrar;
    private javax.swing.JButton btnCocheModificar;
    private javax.swing.JButton btnCocheNuevo;
    private javax.swing.JButton btnImagenCocheModificar;
    private javax.swing.JButton btnImagenCocheNuevo;
    private javax.swing.JButton btnNuevoCliente;
    private javax.swing.JButton btnReiniciarBusqueda;
    private javax.swing.JButton btnRevisionBorrar;
    private javax.swing.JButton btnRevisionModificar;
    private javax.swing.JButton btnRevisionNueva;
    private javax.swing.JButton btnVentaBorrar;
    private javax.swing.JButton btnVentaModificar;
    private javax.swing.JButton btnVentaNueva;
    private javax.swing.JDialog buscarClienteVentaModificar;
    private javax.swing.JDialog buscarClienteVentaNueva;
    private javax.swing.JDialog buscarCocheVentaModificar;
    private javax.swing.JComboBox<String> cbBuscarClienteVentaModificar;
    private javax.swing.JComboBox<String> cbBuscarClienteVentaNueva;
    private javax.swing.JComboBox<String> cbBuscarCocheVentaModificar;
    private javax.swing.JComboBox<String> cbBuscarMain;
    private javax.swing.JComboBox<String> cbMarcaMain;
    private javax.swing.JComboBox<String> cbMarcasInforme;
    private javax.swing.JComboBox<String> cbMotorMain;
    private javax.swing.JComboBox<String> cbTipoCocheModificar;
    private javax.swing.JComboBox<String> cbTipoCocheNuevo;
    private javax.swing.JCheckBox chkAceiteRevisionModificar;
    private javax.swing.JCheckBox chkAceiteRevisionNueva;
    private javax.swing.JCheckBox chkFiltroRevisionModificar;
    private javax.swing.JCheckBox chkFiltroRevisionNueva;
    private javax.swing.JCheckBox chkFrenosRevisionModificar;
    private javax.swing.JCheckBox chkFrenosRevisionNueva;
    private javax.swing.JPanel clientes;
    private javax.swing.JPanel coches;
    private javax.swing.JMenuItem editarCliente;
    private javax.swing.JMenuItem editarRevision;
    private javax.swing.JMenuItem editarVenta;
    private javax.swing.JMenuItem editarcoche;
    private javax.swing.JMenuItem generaInformeClientes;
    private javax.swing.JMenuItem generaInformeCoches;
    private javax.swing.JMenuItem generaInformeCochesMarca;
    private javax.swing.JMenuItem generaInformeRevisiones;
    private javax.swing.JMenuItem generaInformeVentas;
    private javax.swing.JMenu informes;
    private com.toedter.calendar.JDateChooser jDateChooser;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JLabel jLabel79;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JLabel lblAceiteRevisionMain;
    private javax.swing.JLabel lblApellidosClienteMain;
    private javax.swing.JLabel lblApellidosVentaMain;
    private javax.swing.JLabel lblBastidorRevisionMain;
    private javax.swing.JLabel lblBastidorRevisionModificar;
    private javax.swing.JLabel lblBastidorRevisionNueva;
    private javax.swing.JLabel lblCocheVentaMain;
    private javax.swing.JLabel lblDireccionClienteMain;
    private javax.swing.JLabel lblDniClienteMain;
    private javax.swing.JLabel lblDniVentaMain;
    private javax.swing.JLabel lblFechaRevisionMain;
    private javax.swing.JLabel lblFechaRevisionModificar;
    private javax.swing.JLabel lblFechaRevisionNueva;
    private javax.swing.JLabel lblFechaVentaMain;
    private javax.swing.JLabel lblFechaVentaNueva;
    private javax.swing.JLabel lblFiltroRevisionMain;
    private javax.swing.JLabel lblFrenosRevisionMain;
    private javax.swing.JLabel lblMarcaRevisionMain;
    private javax.swing.JLabel lblMarcaRevisionModificar;
    private javax.swing.JLabel lblMarcaRevisionNueva;
    private javax.swing.JLabel lblModeloRevisionMain;
    private javax.swing.JLabel lblModeloRevisionModificar;
    private javax.swing.JLabel lblModeloRevisionNueva;
    private javax.swing.JLabel lblNombreClienteMain;
    private javax.swing.JLabel lblNombreVentaMain;
    private javax.swing.JLabel lblNumeroRevisionMain;
    private javax.swing.JLabel lblNumeroRevisionModificar;
    private javax.swing.JLabel lblNumeroRevisionNueva;
    private javax.swing.JLabel lblPrecioVentaMain;
    private javax.swing.JLabel lblPrecioVentaNueva;
    private javax.swing.JLabel lblTelefonoClienteMain;
    private javax.swing.JMenuItem manual;
    private javax.swing.JMenuBar menu;
    private javax.swing.JDialog modificarCliente;
    private javax.swing.JDialog modificarCoche;
    private javax.swing.JDialog modificarRevision;
    private javax.swing.JDialog modificarVenta;
    private javax.swing.JMenuItem nuevocoche;
    private javax.swing.JPanel pImagenCocheModificar;
    private javax.swing.JPanel pImagenCocheNuevo;
    private javax.swing.JPanel pImagenRevisionMain;
    private javax.swing.JPopupMenu popupCliente;
    private javax.swing.JPopupMenu popupCoche;
    private javax.swing.JPopupMenu popupRevision;
    private javax.swing.JPopupMenu popupVenta;
    private javax.swing.JMenuItem reiniciarbd;
    private javax.swing.JPanel revisiones;
    private javax.swing.JMenuItem salir;
    private javax.swing.JDialog seleccionMarca;
    private javax.swing.JTable tablaClientes;
    private javax.swing.JTable tablaMain;
    private javax.swing.JTable tablaRevisiones;
    private javax.swing.JTable tablaVentas;
    private javax.swing.JTextField txtApellidosClienteModificar;
    private javax.swing.JTextField txtApellidosVentaNueva;
    private javax.swing.JTextField txtBastidorCocheModificar;
    private javax.swing.JFormattedTextField txtBastidorCocheNuevo;
    private javax.swing.JTextField txtBastidorVentaModificar;
    private javax.swing.JTextField txtBastidorVentaNueva;
    private javax.swing.JTextField txtBuscarClienteVentaModificar;
    private javax.swing.JTextField txtBuscarClienteVentaNueva;
    private javax.swing.JTextField txtBuscarCocheVentaModificar;
    private javax.swing.JTextField txtBuscarMain;
    private javax.swing.JFormattedTextField txtCVCocheModificar;
    private javax.swing.JFormattedTextField txtCVCocheNuevo;
    private javax.swing.JTextField txtColorCocheModificar;
    private javax.swing.JTextField txtColorCocheNuevo;
    private javax.swing.JTextField txtDireccionClienteModificar;
    private javax.swing.JTextField txtDireccionVentaNueva;
    private javax.swing.JTextField txtDniClienteModificar;
    private javax.swing.JTextField txtDniVentaModificar;
    private javax.swing.JFormattedTextField txtDniVentaNueva;
    private javax.swing.JTextField txtMarcaCocheModificar;
    private javax.swing.JTextField txtMarcaCocheNuevo;
    private javax.swing.JTextField txtMarcaVentaNueva;
    private javax.swing.JTextField txtModeloCocheModificar;
    private javax.swing.JTextField txtModeloCocheNuevo;
    private javax.swing.JTextField txtModeloVentaNueva;
    private javax.swing.JTextField txtMotorCocheModificar;
    private javax.swing.JTextField txtMotorCocheNuevo;
    private javax.swing.JTextField txtNombreClienteModificar;
    private javax.swing.JTextField txtNombreVentaNueva;
    private javax.swing.JFormattedTextField txtPrecioCocheModificar;
    private javax.swing.JFormattedTextField txtPrecioCocheNuevo;
    private javax.swing.JTextField txtPrecioVentaModificar;
    private javax.swing.JFormattedTextField txtTelefonoClienteModificar;
    private javax.swing.JFormattedTextField txtTelefonoVentaNueva;
    private javax.swing.JPanel ventas;
    // End of variables declaration//GEN-END:variables
}
