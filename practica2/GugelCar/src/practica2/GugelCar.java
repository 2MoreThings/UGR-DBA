/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica2;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.util.Pair;
 

/**
 *
 * @author angelpolo
 * @author antonio morales
 * @author julio rodriguez
 * 
 */
public class GugelCar extends SingleAgent{
    
    private int numsensores = 4;
    private String pass=null;
    private JsonArray resultadoScanner=null;
    private JsonArray resultadoRadar=null;
    private double resultadoBateria= 0.0;
    private int resultadoGpsX=0;
    private int resultadoGpsY=0;
    private Map<Pair<Integer,Integer>,Integer> repetidos= new HashMap<Pair<Integer,Integer>,Integer>();
    
    //Variables heuristica
    ArrayList<Pair<Integer,Integer>> memoria ;
    
    /**
     * Constructor 
     * 
     * @author antonio morales
     * @author irene bejar
     * @author angelpolo
     * @author julio rodriguez
     * @param aid ID del agente
     * @throws Exception
     * 
     */
    public GugelCar(AgentID aid) throws Exception {
        super(aid);
        memoria = new ArrayList<Pair<Integer,Integer>>() ;
    }
    
     /**
     * Iniciar conexion con el agente controlador
     * 
     * @author antonio morales
     * @author irene bejar
     * @throws java.lang.InterruptedException
     * @throws FileNotFoundException
     * @throws IOException
     * 
     */
    public void conexion() throws InterruptedException, FileNotFoundException, IOException{
            
        ACLMessage outbox = new ACLMessage();
        ACLMessage inbox  = new ACLMessage();
            
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID("Furud"));
            
        // Codificar variable en JSON
        // 1. Crear el objeto 
        JsonObject conexion = new JsonObject();
        // 2. Añadir pares <clave,valor>
        conexion.add("command", "login");
        conexion.add("world","map2");
        //Para Angel
        conexion.add("radar","EquipoF");
        //Para Irene/Antonio
        conexion.add("scanner","EquipoF");

        //Para Irene/Antonio
        conexion.add("gps","EquipoF");
            
        //Para julio
        conexion.add("battery","EquipoF");

                      
        outbox.setContent(conexion.toString());
            
        this.send(outbox);
            
        inbox = this.receiveACLMessage();
        
        if(inbox.getContent().toString().indexOf("trace")!=-1){
 
            System.out.println("Recibiendo traza");
            JsonObject injson = Json.parse(inbox.getContent()).asObject();
            JsonArray ja = injson.get("trace").asArray();
            byte data[] = new byte [ja.size()];
            for(int i=0; i<data.length; i++)
                data[i] = (byte) ja.get(i).asInt();
            FileOutputStream fos = new FileOutputStream("mitrazaFallida.png");
            fos.write(data);
            fos.close();
            System.out.print("Traza guardada");
           inbox = this.receiveACLMessage();
        }
        
        // Decodificar variables en JSON
        String fuente = inbox.getContent();
        // 1. Parsear el String original y almacenarlo en un objeto 
        JsonObject recibido= new JsonObject();
        recibido = Json.parse(fuente).asObject();
        // 2. Extraer los valores asociados a cada clave 
        System.out.println(inbox.getContent());
        this.pass=recibido.get("result").asString();
           
        //System.out.println("Pass:" + this.pass);
        
        
    }
    
    /**
     * Recibir informacion del sensor Scanner
     *
     * @author antonio morales
     * @author irene bejar
     * @throws java.lang.InterruptedException
     * 
    */
    public void recibirScanner () throws InterruptedException{
       
        ACLMessage inbox  = new ACLMessage();
        
        inbox = this.receiveACLMessage();
        
        // Decodificar variables en JSON
        String fuenteScanner = inbox.getContent();
        // 1. Parsear el String original y almacenarlo en un objeto 
        JsonObject recibidoScanner= new JsonObject();
        recibidoScanner = Json.parse(fuenteScanner).asObject();
        // 2. Extraer los valores asociados a cada clave 
        this.resultadoScanner=recibidoScanner.get("scanner").asArray();
        System.out.println("Scanner del coche: " + this.resultadoScanner.get(12));
        
        
    }

   /**
    * Recibir todos los sensores del coche
     *
     * @author antonio morales
     * @throws java.lang.InterruptedException
     * @throws IOException
     * 
    */
    public void recibirTodo() throws InterruptedException, IOException{
        this.recibeOK();
        this.recibirRadar();
        this.recibirScanner();
        this.recibirGPS();
        this.recibirBateria();
    }
    
    /**
     * Recibir informacion del sensor GPS
     *
     * @author antonio morales
     * @author irene bejar
     * @throws java.lang.InterruptedException
     * 
    */
    public void recibirGPS () throws InterruptedException{
       
        ACLMessage inbox  = new ACLMessage();
        
        inbox = this.receiveACLMessage();
        
        // Decodificar variables en JSON
        String fuenteGps = inbox.getContent();
        // 1. Parsear el String original y almacenarlo en un objeto 
        JsonObject recibidoGps= new JsonObject();
        recibidoGps = Json.parse(fuenteGps).asObject();
        JsonObject coordGps= recibidoGps.get("gps").asObject();
        // 2. Extraer los valores asociados a cada clave 
        this.resultadoGpsX=coordGps.get("x").asInt();
        this.resultadoGpsY=coordGps.get("y").asInt();
        //System.out.println("GPSX del coche: " + this.resultadoGpsX);
        //System.out.println("GPSY del coche: " + this.resultadoGpsY);
        
        
    }
    
     /**
     * Recibir la informacion del sensor Radar
     * 
     * @author angelpolo
     * @throws java.lang.InterruptedException
     * 
    */
    public void recibirRadar() throws InterruptedException{
       
        ACLMessage inbox  = new ACLMessage();
        
        inbox = this.receiveACLMessage();
        
        // Decodificar variables en JSON
        String fuenteRadar = inbox.getContent();
        // 1. Parsear el String original y almacenarlo en un objeto 
        JsonObject recibidoRadar= new JsonObject();
        recibidoRadar = Json.parse(fuenteRadar).asObject();
        // 2. Extraer los valores asociados a cada clave 
        this.resultadoRadar=recibidoRadar.get("radar").asArray();
        //System.out.println("Radar del coche: " + this.resultadoRadar.get(12));
    }
    
    /**
     * Recibe la informacion del sensor de Bateria
     *
     * @author julio rodriguez
     * @author Antonio Morales
     * @throws java.lang.InterruptedException
     * 
    */
    public void recibirBateria() throws InterruptedException{
       
        ACLMessage inbox  = new ACLMessage();
        
        inbox = this.receiveACLMessage();
        
        // Decodificar variables en JSON
        String fuenteBateria = inbox.getContent();
        // 1. Parsear el String original y almacenarlo en un objeto 
        JsonObject recibidoBateria= new JsonObject();
        recibidoBateria = Json.parse(fuenteBateria).asObject();
        // 2. Extraer los valores asociados a cada clave 
        this.resultadoBateria= recibidoBateria.get("battery").asDouble();
        //System.out.println("Bateria del coche: " + this.resultadoBateria);
    }
    
    /**
     *  Recarga la bateria del coche
     *
     * @author julio rodriguez
     * @author Antonio Morales
     * @throws java.lang.InterruptedException
     * @throws IOException
     * 
    */
    public void refuel() throws InterruptedException, IOException{
        if (this.resultadoBateria<5.0){
         System.out.println("Refueleado");
        this.enviaMovimiento("refuel");
        this.recibeOK();
        this.recibirRadar();
        this.recibirScanner();
        this.recibirGPS();
        this.recibirBateria();
        }
    }
        
    /**
     * Comprueba si nos encontramos encima de la solucion
     * 
     * @author angelpolo
     * @return boolean
     * @throws java.lang.InterruptedException
     * 
    */
    public boolean compruebaSolucion() throws InterruptedException{
            
        return (this.resultadoRadar.get(12).asInt() == 2);
       
    }
    
    /**
     * Funcion que devuelve true si desde la posicion actual el radar ve el
     * objetivo
     *
     * @author julio rodriguez
     * @return boolean
     * 
    */
    public boolean veSolucion(){
        
        for(int i=0; i<=4; i++){
            for(int j=0; j<=4; j++){
                if(this.getPosRadar(i, j)==2)
                    return true;
            }
        }
        
        return false;
    }
    
    /**
     * Analiza si despues de haber explorado varias veces las casillas cercanas
     * a la solucion, el mapa no tiene solución
     *
     * @author julio rodriguez
     * @return boolean
     * 
    */
    public boolean haySolucion(){
        
        int parada=6;  //Variable que decide con qué repeticion de una de las casillas acaba
        
        if(veSolucion()){
            if(!repetidos.containsKey(new Pair<Integer,Integer>(this.resultadoGpsX,this.resultadoGpsY))){
               repetidos.put(new Pair<>(this.resultadoGpsX,this.resultadoGpsY), 1);
               System.out.print("Pasa por " + this.resultadoGpsX + ", " + this.resultadoGpsY + ": " + 1 + "\n");
            }else{
              int num= repetidos.get(new Pair<>(this.resultadoGpsX,this.resultadoGpsY));
              
              if(num < parada){
                  num++;
                  repetidos.put(new Pair<>(this.resultadoGpsX,this.resultadoGpsY) , num);
                  System.out.print("Vuelve a pasar por " + this.resultadoGpsX + ", " + this.resultadoGpsY + ": " + num + "\n");
              }
              else if(num==parada){
                  System.out.println("NO TIENE SOLUCION");
                  return false;
              }
            }
        }
        
        return true;
    }
    
    
    
    /**
     * Envia al controlador la accion que realiza
     *
     * @author angelpolo
     * @param movimiento String de la accion a realizar
     * @throws java.lang.InterruptedException
     * 
    */
    public void enviaMovimiento(String movimiento) throws InterruptedException{
            
        ACLMessage outbox = new ACLMessage();
            
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID("Furud"));
       
        JsonObject conexion = new JsonObject();
        conexion.add("command", movimiento);
        conexion.add("key", this.pass);                     
        outbox.setContent(conexion.toString());
            
        this.send(outbox);
       
    }
    
    /**
     * Realiza una serie de movimientos acumulados en el parametro movimientos
     *
     * @author angelpolo
     * @author irene bejar
     * @author antonio morales
     * @param movimientos ArrayList con movientos 
     * @throws IOException
     * 
    */ 
    public void moverse (ArrayList<String> movimientos) throws IOException{
        for (String mov : movimientos){
            try {
                this.enviaMovimiento(mov) ;
                this.recibeOK();
                this.recibirRadar();
                this.recibirScanner();
                this.recibirGPS();
            } catch (InterruptedException ex) {
                System.out.println("Fallo en función moverse!!!!!!") ;
            }
            
        }
    }
    
 
    /**
     * Para obtener el valor de la posicion del radar (i,j). Primero pasa el 
     * (i,j) a un entero, ya que el sensor esta representado como un vector
     * y no como matriz.
     *
     * @author angelpolo
     * @author irene bejar
     * @author antonio morales
     * @param i fila
     * @param j columna
     * @return valor del radar en la posicion (i,j)
     * 
    */ 
    public int getPosRadar(int i, int j){
        int pos = 5*i + j;
        return this.resultadoRadar.get(pos).asInt();
    }

    
    /**
     * Para obtener el valor de la posicion del scanner (i,j). Primero pasa el 
     * (i,j) a un entero, ya que el sensor esta representado como un vector
     * y no como matriz.
     *
     * @author angelpolo
     * @author irene bejar
     * @author antonio morales
     * @param i fila
     * @param j columna
     * @return valor del scanner en la posicion (i,j)
     * 
    */ 
    public double getPosScanner(int i, int j){
        int pos = 5*i + j;
        return this.resultadoScanner.get(pos).asDouble();
    }
    
    /*
    @author irene bejar
    @author antonio morales
    */
    /**
     * Para pasar de la posicion lineal, a su representacion en (i,j) de una
     * matriz
     *
     * @author irene bejar
     * @author antonio morales
     * @param pos posicion lineal de la matriz
     * @return valor de la posicion en (i,j)
     * 
    */ 
    public Pair getFilCol(int pos){
        int i = pos/5;
        int j = pos%5;
        return new Pair(i,j);
    }

    /**
     *
     * @author angelpolo
     * @throws java.lang.InterruptedException
     * 
    */
    /**
     * Para recibir el result del controlador tras el envio del movimiento
     *
     * @author angelpolo
     * @throws InterruptedException
     * @throws IOException
     * 
    */ 
    public void recibeOK() throws InterruptedException, IOException{
        
        ACLMessage inbox = new ACLMessage();
        inbox = this.receiveACLMessage();
        

        String fuente = inbox.getContent();

        JsonObject recibido= new JsonObject();
        recibido = Json.parse(fuente).asObject();

        String resultado=recibido.get("result").asString(); 
        
        interpretaResultado(resultado);
        
    }
    
    /**
     * Interpreta si ha habido algun error recibiendo el resultado de la 
     * comunicacion con el controlador
     *
     * @author julio rodriguez
     * @param resultado string con el resultado de la comunicacion 
     * 
    */
    public void interpretaResultado(String resultado) throws InterruptedException, IOException{
        switch(resultado){
            case "CRASHED": System.out.print("ERROR: EL COCHE SE HA ESTRELLADO\n"); this.logOut(); System.exit(-1); break;
            case "BAD_COMMAND": System.out.print("ERROR: COMANDO DESCONOCIDO PARA EL SERVIDOR\n"); this.logOut(); System.exit(-1); break;
            case "BAD_PROTOCOL": System.out.print("ERROR: COMANDO JSON MAL FORMADO\n");  this.logOut(); System.exit(-1); break;
            case "BAD_KEY": System.out.print("ERROR: CLAVE INCORRECTA\n"); this.logOut(); System.exit(-1); break;
        }
    }
    
    
    /**
     * Funcion para realizar el logout
     *
     * @author angelpolo
     * @throws InterruptedException
     * @throws FileNotFoundException
     * @throws IOException
     * 
    */ 
    public void logOut() throws InterruptedException, FileNotFoundException, IOException{
            
        ACLMessage outbox = new ACLMessage();
            
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID("Furud"));
       
        JsonObject conexion = new JsonObject();
        conexion.add("command", "logout");
        conexion.add("key", this.pass);                     
        outbox.setContent(conexion.toString());
            
        this.send(outbox);
        
        ACLMessage inbox = new ACLMessage();
        inbox = this.receiveACLMessage();
        
        String fuente = inbox.getContent();

        JsonObject recibido= new JsonObject();
        recibido = Json.parse(fuente).asObject();

        String resultado=recibido.get("result").asString();
           
        System.out.println("Logout:" + resultado);
        
        for(int i=0;i<this.numsensores;i++){
            this.receiveACLMessage();
        }

       
        try{
            System.out.println("Recibiendo traza");
            inbox = this.receiveACLMessage();
            JsonObject injson = Json.parse(inbox.getContent()).asObject();
            JsonArray ja = injson.get("trace").asArray();
            
            byte data[] = new byte [ja.size()];
            for(int i=0; i<data.length; i++)
                data[i] = (byte) ja.get(i).asInt();
        
            FileOutputStream fos = new FileOutputStream("mitraza.png");
            fos.write(data);
            fos.close();
            System.out.print("Traza guardada");
        
        }catch(InterruptedException ex){
            System.err.println("Error procesando traza");
        }
        
    }
    
    

        
    /**
     * Funcion que ejecuta la rutina del agente
     *
     * @author angelpolo
     * @author irene bejar
     * @author antonio morales
     * @author julio rodriguez
     * 
    */ 
    @Override 
    public void execute(){
        try {
            
            this.conexion();
            this.recibirRadar();
            this.recibirScanner();
            this.recibirGPS();
            this.recibirBateria();
  
            while(!this.compruebaSolucion() && haySolucion()){
                this.refuel();
                this.enviaMovimiento(this.heuristica());
                this.recibeOK();
                this.recibirRadar();
                this.recibirScanner();
                this.recibirGPS();
                this.recibirBateria();
            }
            
            System.out.println("Logout");
            this.logOut();
            
        } catch (InterruptedException ex) {
            System.out.println("Ha fallado el execute");
        } catch (IOException ex) {
            Logger.getLogger(GugelCar.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /**
     * Busca si el punto (x,y) esta en la memoria
     *
     * @author angelpolo
     * @author irene bejar
     * @author antonio morales
     * @param x coordenada x
     * @param y coordenada y
     * @return boolean que dice si esta o no en memoria
     * 
    */ 
    public boolean Esta(int x, int y){
        Pair<Integer, Integer> pos = new Pair<Integer,Integer>(x,y) ;
        
        //System.out.println ("Esta: "+ (this.memoria.indexOf(pos) != -1) );
        //System.out.println ("Vector: "+ this.memoria.toString() );
        //System.out.println ("X: "+ x + " y: " +y);
        
        return (this.memoria.indexOf(pos) != -1) ;
    }
   
    
    /**
     * Añade una posicion a la memoria
     *
     * @author angelpolo
     * @author irene bejar
     * @author antonio morales
     * @param x coordenada x
     * @param y coordenada y
     * 
    */ 
    public void setPosicionMemoria(int x, int y){
        Pair<Integer,Integer> pos = new Pair<Integer,Integer>(x,y) ;

        if(this.memoria.size()<1000){
            this.memoria.add(pos);
        }
        
        else{
            this.memoria.remove(0);
            this.memoria.add(pos);
        }        
  
    }

    
    /**
     * Funcion que realiza la heuristica del coche
     *
     * @author angelpolo
     * @author irene bejar
     * @author antonio morales
     * @return String con el movimiento a realizar
    */ 
    private String heuristica() {
        //Añadimos la pos del gps a la memoria
        this.setPosicionMemoria(this.resultadoGpsX,this.resultadoGpsY) ;
        ArrayList<Pair<Integer,Integer>> PosPosibles;
        PosPosibles=new ArrayList<Pair<Integer,Integer>>();
             
        for(int i=0, y=this.resultadoGpsY-2; i<=4; i++, y++){
            for(int j=0, x=this.resultadoGpsX-2; j<=4; j++, x++){
                if(this.getPosRadar(i, j)==0 || this.getPosRadar(i, j)==2)
                    if(!Esta(x,y))
                        PosPosibles.add(new Pair<Integer,Integer>(i,j));
            }
        }
        
        Pair<Integer,Integer> mejorPos = new Pair<Integer,Integer>(2,2);
        double mejorValor = 100000.0;
        for(int i =0;i<PosPosibles.size();i++){ 
            //System.out.println(this.getPosScanner(PosPosibles.get(i).getKey(),PosPosibles.get(i).getValue()));
            if(this.getPosScanner(PosPosibles.get(i).getKey(),PosPosibles.get(i).getValue()) < mejorValor ){
                mejorPos=new Pair(PosPosibles.get(i).getKey(),PosPosibles.get(i).getValue());
                mejorValor=this.getPosScanner(PosPosibles.get(i).getKey(),PosPosibles.get(i).getValue());
            }
        }
        String movimiento= "defecto";
        if(mejorPos.getKey()==1){
            if(mejorPos.getValue()==0){
                if(PosPosibles.contains(new Pair(1,1)))
                    movimiento="moveNW";
                else if(PosPosibles.contains(new Pair(2,1)))
                    movimiento="moveW";
            }
            if(mejorPos.getValue()==1){
                movimiento="moveNW";
            }
            if(mejorPos.getValue()==2){
                movimiento="moveN";
            }
            if(mejorPos.getValue()==3){
                movimiento="moveNE";
            }
            if(mejorPos.getValue()==4){
                if(PosPosibles.contains(new Pair(1,3)))
                    movimiento="moveNE";
                else if(PosPosibles.contains(new Pair(2,3)))
                    movimiento="moveE";
            }
        }
        if(mejorPos.getKey()==2){
            if(mejorPos.getValue()==0){
               if(PosPosibles.contains(new Pair(2,1)))
                    movimiento="moveW";
            }
            if(mejorPos.getValue()==1){
                movimiento="moveW";
            }
            if(mejorPos.getValue()==3){
                movimiento="moveE";
            }
            if(mejorPos.getValue()==4){
               if(PosPosibles.contains(new Pair(2,3)))
                    movimiento="moveE";
            }
        }
        if(mejorPos.getKey()==3){
            if(mejorPos.getValue()==0){
                if(PosPosibles.contains(new Pair(2,1)))
                    movimiento="moveW";
                else if(PosPosibles.contains(new Pair(3,1)))
                    movimiento="moveSW";
            }
            if(mejorPos.getValue()==1){
                movimiento="moveSW";
            }
            if(mejorPos.getValue()==2){
                movimiento="moveS";
            }
            if(mejorPos.getValue()==3){
                movimiento="moveSE";
            }
            if(mejorPos.getValue()==4){
                if(PosPosibles.contains(new Pair(2,3)))
                    movimiento="moveE";
                else if(PosPosibles.contains(new Pair(3,3)))
                    movimiento="moveSE";
            }
        }
        
        if(mejorPos.getKey()==4){
            if(mejorPos.getValue()==0){
                if(PosPosibles.contains(new Pair(3,1)))
                    movimiento="moveSW";
            }
            if(mejorPos.getValue()==1){
                if(PosPosibles.contains(new Pair(3,1)))
                    movimiento="moveSW";
                else if(PosPosibles.contains(new Pair(3,2)))
                    movimiento="moveS";
            }
            if(mejorPos.getValue()==2){
                if(PosPosibles.contains(new Pair(3,2)))
                    movimiento="moveS";
            }
            if(mejorPos.getValue()==3){
                if(PosPosibles.contains(new Pair(3,3)))
                    movimiento="moveSE";
                else if(PosPosibles.contains(new Pair(3,2)))
                    movimiento="moveS";
            }
            if(mejorPos.getValue()==4){
                if(PosPosibles.contains(new Pair(3,3)))
                    movimiento="moveSE";
            }
            
        }
        
        if(mejorPos.getKey()==0){
            if(mejorPos.getValue()==0){
                if(PosPosibles.contains(new Pair(1,1)))
                    movimiento="moveNW";
            }
            if(mejorPos.getValue()==1){
                if(PosPosibles.contains(new Pair(1,2)))
                    movimiento="moveN";
                else if(PosPosibles.contains(new Pair(1,1)))
                    movimiento="moveNW";
            }
            if(mejorPos.getValue()==2){
                if(PosPosibles.contains(new Pair(1,2)))
                    movimiento="moveN";
            }
            if(mejorPos.getValue()==3){
                if(PosPosibles.contains(new Pair(1,2)))
                    movimiento="moveN";
                else if(PosPosibles.contains(new Pair(1,3)))
                    movimiento="moveNE";
            }
            if(mejorPos.getValue()==4){
                if(PosPosibles.contains(new Pair(1,3)))
                    movimiento="moveNE";
            }
            
        }
        
        if(movimiento.equals("defecto")){
            movimiento = this.movimientoAleatorio();
        }
        //System.out.println("Movimiento: " + movimiento);
        return movimiento;
    }

    /**
     * Realiza un movimiento totalmente aleatorio
     *
     * @author angelpolo
     * @return string del movimiento aleatorio
     * 
    */ 
 private String movimientoAleatorio(){
             String movimiento = "defecto";
             int valorDado = (int) Math.floor(Math.random()*8+1);
             //System.out.println("Movimiento por defecto! Valor dado: " + valorDado);
             
            if(valorDado==1){
                if(this.getPosRadar(1, 1)==0 || this.getPosRadar(1, 1)==2)
                    movimiento="moveNW";
                else
                    movimiento = movimientoAleatorio();
             }
             
             if(valorDado==2){
                 if(this.getPosRadar(1, 3)==0 || this.getPosRadar(1, 3)==2)
                     movimiento="moveNE";
                 else
                    movimiento = movimientoAleatorio();
             }
             if(valorDado==3){
                 if(this.getPosRadar(3, 1)==0 || this.getPosRadar(3, 1)==2)
                     movimiento="moveSW";
                 else
                    movimiento = movimientoAleatorio();
             }
              if(valorDado==4){
                 if(this.getPosRadar(3, 3)==0 || this.getPosRadar(3, 3)==2)
                     movimiento="moveSE";
                 else
                     movimiento = movimientoAleatorio();
             }
             
             
             if(valorDado==5){
                if(this.getPosRadar(1, 2)==0 || this.getPosRadar(1, 2)==2)
                    movimiento="moveN";
                else
                    movimiento = movimientoAleatorio();
             }
             
             if(valorDado==6){
                 if(this.getPosRadar(3, 2)==0 || this.getPosRadar(3, 2)==2)
                     movimiento="moveS";
                 else
                    movimiento = movimientoAleatorio();
             }
             if(valorDado==7){
                 if(this.getPosRadar(2, 1)==0 || this.getPosRadar(2, 1)==2)
                     movimiento="moveW";
                 else
                    movimiento = movimientoAleatorio();
             }
              if(valorDado==8){
                 if(this.getPosRadar(2, 3)==0 || this.getPosRadar(2, 3)==2)
                     movimiento="moveE";
                 else
                     movimiento = movimientoAleatorio();
             }
             return movimiento;
    
}
     
}

