/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xagents;

import es.upv.dsic.gti_ia.core.AgentID;
//import DBA.SuperAgent;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;
import es.upv.dsic.gti_ia.core.ACLMessage;
import static java.lang.System.exit;

import es.upv.dsic.gti_ia.core.SingleAgent;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.System.out;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

/**
 * @class XVehicle
 * 
 * @brief Clase que implementa las comunicaciones del vehículo
 * 
 * @author Antonio Morales
 */
public class XVehicle extends SingleAgent{ 
    // Variables con los Aid de Furud y Xavier
    private String Furud ;
    private String Xavier;
    
    private Tipo tipo ;                 //Tipo de vehículo
    private String converID_xavier ;    // Conversation ID con Xavier
    private String converID_servidor ;  // Conversation ID con Furud
    private String replay ;             // Replay with 
    private int fuelrate ;              // Combustible que gasta por movimiento
    private boolean fly;                // Capacidad de vuelo
    private int range;                  // Tamaño del radar
    
    private JsonArray scanner; // Scanner que se recibe de Furud
    private int sol_x;                  // Componente x de la solución
    private int sol_y;                  // Componente y de la solución
    private boolean stop ;              // ¿Debe parar el vehículo?
    private String movExploracion;      // Movimiento que se va a realizar
    //Variables de prueba
    private int x;
    private int y;
    private int battery;
    private JsonArray sensor;
    private boolean solucion;           // Indica si se ha encontrado una solución
    private JsonObject serverinfo;      // Información que se recibe de Furud
    
    //Variable Greedy
    ArrayList<Pair<Integer,Integer>> memoria ;   //Casillas por las que ha pasado el vehículo
    ArrayList<Pair<Integer,Integer>> soluciones; // Soluciones encontradas
        private JsonArray miScanner;    // Scanner que se recibe de Xavier


    /**
    *
    * @brief Constructor con parámetros
    * @param agentID ID del agente
    * @param nombre_Furud Nombre del servidor
    * @param nombre_Xavier Nombre del controlador
    * 
    *  @author Irene bejar
    */
XVehicle(AgentID agentID, String nombre_Furud, String nombre_Xavier) throws Exception {
        super(agentID);
        //Doy un valor random a la posicion de la solucion para comprobar que aun no se ha descubierto
        this.sol_y = -74;
        this.sol_x = -74;
        
        this.Furud = nombre_Furud ;
        this.Xavier = nombre_Xavier;
        this.stop = false ;
        
        memoria = new ArrayList<Pair<Integer,Integer>>() ;
        soluciones = new ArrayList<Pair<Integer,Integer>>() ;
    }

    /**
    *
    * @brief Constructor con parámetros
    * @param agentID ID del agente
    * @param nombre_Xavier Nombre del controlador
    * 
    * @author Julio Rodriguez 
    *  @author Irene bejar
    */
XVehicle(AgentID agentID, String nombre_Xavier) throws Exception {
        super(agentID);
        //Doy un valor random a la posicion de la solucion para comprobar que aun no se ha descubierto

        this.sol_y = -74;
        this.sol_x = -74;
        
        this.Furud = "Furud";
        this.Xavier = nombre_Xavier;
        this.stop = false ;
        
        memoria = new ArrayList<Pair<Integer,Integer>>() ;
        soluciones = new ArrayList<Pair<Integer,Integer>>() ;
    }

    /**
    * @brief Se encarga de hacer un SUBSCRIBE a Xavier
    * 
    * @author Julio Rodriguez 
    *  @author Irene bejar
    */
    public void suscribe() throws InterruptedException {
        //Enviar suscribe
        ACLMessage outbox = new ACLMessage();
        
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID(this.Xavier));
        
        outbox.setPerformative(ACLMessage.SUBSCRIBE);
        outbox.setContent(this.getAid().toString());
        
        this.send(outbox);
        System.out.println("Agente " + this.getAid() + " envia " + outbox.getPerformative() + " a " + outbox.getReceiver());
        //Recibir inform
        ACLMessage inbox = new ACLMessage();
        inbox = this.receiveACLMessage();
        
        if (inbox.getPerformativeInt() == ACLMessage.INFORM){
            this.converID_xavier=inbox.getConversationId();
            //System.out.println ("Recibido ConID de Xavier: " + this.converID_xavier);
            
            String contenido = inbox.getContent();
            JsonObject recibido= new JsonObject();
            recibido = Json.parse(contenido).asObject() ;
            this.converID_servidor = recibido.get("controlador").asString() ;
            //System.out.println ("Recibido ConID del Servidor: " + this.converID_servidor);
        }
        
        else if (inbox.getPerformativeInt() == ACLMessage.FAILURE){
            System.out.println("Error al subcribirse el agente\n");
            System.exit(0);
        }
        
    }
    
    /**
    * @brief Realiza el chekin del vehículo con el servidor
    * 
    * @author Julio Rodriguez 
    *  @author Irene bejar
    */
    public void chekin_controlador(){
        ACLMessage outbox = new ACLMessage();
        JsonObject msg = new JsonObject() ;
        msg.add("command", "checkin");
        
        outbox.setSender(this.getAid());
        outbox.setPerformative(ACLMessage.REQUEST);
        outbox.setReceiver(new AgentID(this.Furud));
        outbox.setConversationId(this.converID_servidor);
        outbox.setContent(msg.toString()); 
     
        this.send(outbox);    
        System.out.println("Agente " + this.getAid() + " envia " + outbox.getPerformative() + " a " + outbox.getReceiver());
    }
    
    /**
    * @brief Recibe el INFORM del servidor con las capabilities del vehículo
    * 
    * @author Julio Rodriguez 
    *  @author Irene bejar
    */
    public void recibir_capabilities() throws InterruptedException {
        ACLMessage inbox = new ACLMessage();
        inbox = this.receiveACLMessage();
        this.replay = inbox.getReplyWith() ;
        
        if (inbox.getPerformativeInt() == ACLMessage.INFORM){
            this.converID_xavier=inbox.getConversationId();
            
            String contenido = inbox.getContent();
            JsonObject recibido= new JsonObject();
            JsonObject capabilities= new JsonObject();
            recibido = Json.parse(contenido).asObject() ;
            capabilities = recibido.get("capabilities").asObject() ;
            
            //Asiganamos tipo y capabilities
            if (capabilities.get("range").asInt() == 3){
                this.tipo = Tipo.MOSCA ;
                this.fly = true ;
                this.range = 3 ;
                this.fuelrate = 1 ;
            }
            else if (capabilities.get("range").asInt() == 5){
                this.tipo = Tipo.COCHE ;
                this.fly = false ;
                this.range = 5 ;
                this.fuelrate = 2 ;
            }
            else if (capabilities.get("range").asInt() == 11){
                this.tipo = Tipo.CAMION ;
                this.fly = false ;
                this.range = 11 ;
                this.fuelrate = 4 ;
            }
            
            this.mandar_capabilities_xavier(false);
            //System.out.println("Capabilities enviadas a Xavier\n");
        }
        
        else if (inbox.getPerformativeInt() == ACLMessage.NOT_UNDERSTOOD){
            System.out.println("El servidor no entiende los mensajes\n");
            
            System.out.println(inbox.getContent());
            System.exit(0);
        }
        
        else if (inbox.getPerformativeInt() == ACLMessage.FAILURE){
            System.out.println("El servidor ha indicado un fallo");
            
            System.out.println(inbox.getContent());
            this.mandar_capabilities_xavier(false);
        }
    }
    
    /**
     * @brief Manda un REQUEST a Xavier con las capabilities del vehículo
     * @param error True si ha fallado al hacer chekin, false en caso contrario
    *
    * @author Julio Rodriguez 
    *  @author Irene bejar
    */
    public void mandar_capabilities_xavier(boolean error) {
        //System.out.println("**********MANDAR CAPABILITIES**********");
        if (error == false){

            ACLMessage outbox = new ACLMessage();
            JsonObject msg = new JsonObject() ;
            JsonObject capabilities = new JsonObject() ;

            capabilities.add("fuelrate", this.fuelrate);
            capabilities.add("range", this.range);
            capabilities.add("fly", this.fly);
            capabilities.add("type", this.tipo.toString());

            msg.add("capabilities", capabilities);

            //System.out.println("Se está enviando a Xavier: " + msg.toString());


            outbox.setSender(this.getAid());
            outbox.setPerformative(ACLMessage.REQUEST);
            outbox.setReceiver(new AgentID(this.Xavier));
            outbox.setConversationId(this.converID_xavier);
            outbox.setContent(msg.toString()); 

            this.send(outbox);  
            System.out.println("Las capabilities son: " + outbox.getContent().toString());
            System.out.println("Agente " + this.getAid() + " envia " + outbox.getPerformative() + " a " + outbox.getReceiver());
        }
        
        else {
            this.mandar_failure_xavier();
        }
    }

    /**
     * @brief Manda FAILURE a Xavier
    *
    * @author Julio Rodriguez 
    *  @author Irene bejar
    */
    public void mandar_failure_xavier(){
        //System.out.println("**********MANDAR FAILURE XAVIER**********");
        ACLMessage outbox = new ACLMessage();
        outbox.setPerformative(ACLMessage.FAILURE);
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID(this.Xavier));
        outbox.setConversationId(this.converID_xavier);
        this.send(outbox); 
        System.out.println("Agente " + this.getAid() + " envia " + outbox.getPerformative() + " a " + outbox.getReceiver());
    }
    
    /**
     * @brief Recibe REQUEST de Xavier para empezar a moverse
     * @return True si se ha recibido correctamente, false en caso contrario
    *
    * @author Julio Rodriguez 
    *  @author Irene bejar
    */
    public boolean recibir_start() throws InterruptedException {
        //System.out.println("**********RECIBIR_START**********");
        ACLMessage inbox = new ACLMessage();
        inbox = this.receiveACLMessage();
        System.out.println("Agente " + this.getName() + " recive " + inbox.getPerformative() + " contenido: " + inbox.getContent());
        if (inbox.getPerformativeInt() == ACLMessage.REQUEST && "start".equals(inbox.getContent())){
            ACLMessage outbox = new ACLMessage();
            outbox.setPerformative(ACLMessage.INFORM);
            outbox.setSender(this.getAid());
            outbox.setReceiver(new AgentID(this.Xavier));
            outbox.setConversationId(this.converID_xavier);
            outbox.setContent("ok");
            this.send(outbox);  
            System.out.println("Agente " + this.getAid() + " envia " + outbox.getPerformative() + " a " + outbox.getReceiver());
            return true ;
        }
        
        return false ;
    }
    /**
     * @brief Envia REQUEST de un movimiento al servidor
     * @param move String con el movimiento que se quiere realizar
    *
    * @author Julio Rodriguez 
    *  @author Irene bejar
    */
    public void enviarMovimiento(String move) throws InterruptedException {
       //System.out.println("**********ENVIAR_MOVIMIENTO**********");
       //movimiento = heuristica(this.tipo) ;
       JsonObject msg = new JsonObject() ;
       msg.add ("command", move);
       
       //Enviar movimiento
       ACLMessage outbox = new ACLMessage();
       outbox.setPerformative(ACLMessage.REQUEST);
       outbox.setSender(this.getAid());
       outbox.setReceiver(new AgentID(this.Furud));
       outbox.setConversationId(this.converID_servidor);
       outbox.setInReplyTo(this.replay);
       outbox.setContent(msg.toString());
       
       this.send(outbox); 
       System.out.println("Agente " + this.getAid() + " envia " + outbox.getPerformative() + " a " + outbox.getReceiver() + "con contenido" + outbox.getContent());
       
       //Recibir inform
       ACLMessage inbox = new ACLMessage();
       inbox = this.receiveACLMessage();
       this.replay = inbox.getReplyWith() ;
       System.out.println("Agente " + this.getAid() + " recibe " + inbox.getPerformative() + " de " + inbox.getSender() + "con contenido" + inbox.getContent());
       if (inbox.getPerformativeInt() == ACLMessage.FAILURE){
           System.out.println("Agente " + this.getAid() + " recibe " + inbox.getPerformative() + " de " + inbox.getSender() + "con contenido" + inbox.getContent());
           this.mandar_failure_xavier();
       }
       
       if (inbox.getPerformativeInt() == ACLMessage.NOT_UNDERSTOOD){
           System.out.println("El servidor no entiende los mensajes\n");
            
           System.out.println(inbox.getContent());
           System.exit(0);
       }
       
       if (inbox.getPerformativeInt() == ACLMessage.REFUSE){
           System.out.println("No hay mas energía en el mundo\n") ;
       }
       
    }
    
    /**
    *
    * @brief Manda un QUERY_REF al servidor para recibir los sensores del vehículo
    * 
    * @author Antonio Morales
    * @author Angel Polo
    * @author Irene bejar
    * @author Julio rodriguez
    */
    public void recibir_sensores() throws InterruptedException{
        //System.out.println("**********RECIBIR_SENSORES**********");
       ACLMessage outbox = new ACLMessage();
       outbox.setPerformative(ACLMessage.QUERY_REF);
       outbox.setSender(this.getAid());
       outbox.setReceiver(new AgentID(this.Furud));
       outbox.setConversationId(this.converID_servidor);
       outbox.setInReplyTo(this.replay);
       outbox.setContent("");
       this.send(outbox); 
       //System.out.println("Agente " + this.getAid() + " envia " + outbox.getPerformative() + " a " + outbox.getReceiver());
       
       ACLMessage inbox = new ACLMessage();
       inbox = this.receiveACLMessage(); //inform all
       this.replay = inbox.getReplyWith() ;
       
       System.out.println("Agente " + this.getAid() + " recibe " + inbox.getPerformative() + " de " + inbox.getSender() + " contenido:" +inbox.getContent());
       
       if (inbox.getPerformativeInt() == ACLMessage.INFORM){
           //Guardo mi posicion
           if( inbox.getContent().indexOf("solucion") != -1){
               inbox = this.receiveACLMessage();
           }
           
          System.out.println("Server info es: " + serverinfo.toString());
            this.serverinfo  = Json.parse(inbox.getContent()).asObject() ;
            this.x = serverinfo.get("result").asObject().get("x").asInt();
            this.y = serverinfo.get("result").asObject().get("y").asInt();
            this.battery = serverinfo.get("result").asObject().get("battery").asInt();
            this.sensor=serverinfo.get("result").asObject().get("sensor").asArray();
            this.solucion=serverinfo.get("result").asObject().get("goal").asBoolean();
            
            //this.compartirInfo();
       }
       else if (inbox.getPerformativeInt() == ACLMessage.NOT_UNDERSTOOD){
           System.out.println("El servidor no entiende los mensajes\n");
            
           System.out.println(inbox.getContent());
           System.exit(0);
       }
    }


    /**
    *  @brief Manda REQUEST a Xavier con toda la información de los sensores
    * 
    *  @author Antonio Morales
    *  @author Angel Polo
    */
    //envia request con toda la info
    public void compartirInfo() throws InterruptedException{
        //System.out.println("**********COMPARTIR_INFO**********");
        ACLMessage outbox = new ACLMessage();
            //Se manda el contenido a xavier
           outbox.setReceiver(new AgentID(this.Xavier));
           outbox.setSender(this.getAid());
           outbox.setConversationId(this.converID_xavier);
           outbox.setPerformative(ACLMessage.REQUEST);
           outbox.setContent(this.serverinfo.toString());
           //Faltaba el enviar
           this.send(outbox); 
           //System.out.println("Agente " + this.getAid() + " envia " + outbox.getPerformative() + " a " + outbox.getReceiver() + " contenido: " + outbox.getContent());
        //}
    }
    
    /**
     * @brief Recibe INFORM de Xavier 
     * 
     * @explain EL INFORM se manda después de que Xavier reciba REQUEST con los sensores
     *          Contiene: stop, scanner, posición de la solución y un movimiento
    *
    * @author Antonio Morales
    *  @author Angel Polo
    */
    public void recibeInform() throws InterruptedException{
        //System.out.println("**********RECIBEINFORM**********");
        ACLMessage inbox = new ACLMessage();
      
        
        inbox = this.receiveACLMessage(); 
        System.out.println("Agente " + this.getAid() + " recibe " + inbox.getPerformative() + " de " + inbox.getSender() + "con contenido" + inbox.getContent());
        if (inbox.getPerformativeInt() == ACLMessage.INFORM){
           
           JsonObject recibido = new JsonObject();
           recibido = Json.parse(inbox.getContent()).asObject() ;
           this.stop = recibido.get("stop").asBoolean() ;
           this.movExploracion = recibido.get("movimiento").asString();
           //this.scanner = recibido.get("scanner").asArray();
           this.sol_x = recibido.get("solucion").asObject().get("sol_x").asInt();
           this.sol_y = recibido.get("solucion").asObject().get("sol_y").asInt();
           if(!this.soluciones.contains(new Pair<Integer, Integer>(sol_x, sol_y)) && sol_x!=74 && sol_y!=74){
               this.soluciones.add(new Pair<Integer, Integer>(sol_x, sol_y));
           }
           //System.out.println("Termino de parsear");
        }

        else{
        System.out.println("fallo al recibir request de Xavier");
        }
    }
    
    /**
     * @brief Envía CANCEL a Xavier
    *
    * @author Julio Rodriguez 
    *  @author Irene bejar
    */
    public void cancel(){
        //System.out.println("**********CANCEL**********");
        
       ACLMessage outbox = new ACLMessage();
       outbox.setPerformative(ACLMessage.CANCEL);
       outbox.setSender(this.getAid());
       outbox.setReceiver(new AgentID(this.Xavier));
       outbox.setContent("");
       this.send(outbox);
       System.out.println("Agente " + this.getAid() + " envia " + outbox.getPerformative() + " a " + outbox.getReceiver());
    }
 
        /**
    * @brief Execute del vehículo
    * 
    * @author Antmordhar
    *  @author Angel Polo
    * @author Irene bejar
    * @author Julio rodriguez
    */
    public void execute(){
        try {
            
            this.suscribe();
            this.chekin_controlador();
            this.recibir_capabilities();
            //Si ya me han dicho de empezar
            this.recibir_start();
            //Y aun esta la solucion que damos por defecto
            this.recibeInform();
            
            this.recibir_sensores();
            this.explorar();
            this.compartirInfo();
            
            //System.out.println("**********EXECUTE**********");
            
            while(this.sol_x==-74&&this.sol_y==-74){
                //Empiezo a explorar, recibir sensores y enviarselos a xavier
                //System.out.println(this.getAid() + " entra al bucle de explorar.");
                this.recibeInform();
                this.explorar();
                this.compartirInfo();
                
            }
            
           
            while(!this.solucion){
                System.out.println("******************************entro en bucle greedy******************************************************************S");
                this.recibeInform();
                //System.out.println("******************************RECIBO INFORM******************************************************************S");
                
                this.recibir_sensores();
                
                this.movExploracion = this.heuristica();
                System.out.println("++APLICANDO HEURISTICA: " + this.getAid() + "MOVIMIENTO: " + this.movExploracion);

                this.explorar();
                System.out.println("******************************he explorado******************************************************************S");
               this.compartirInfo(); 
                System.out.println("******************************he compartido, fin bucle******************************************************************S");
            }
            
            
            System.out.println("Estoy en la solución :D");
            
        } catch (InterruptedException ex) {
            Logger.getLogger(XVehicle.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     /**
    * @brief Controla si se debe realizar un movimiento o recargar la batería
    * 
    * @author Antmordhar
    * @author Angel Polo
    */
    private void explorar() throws InterruptedException {
        //System.out.println("**********EXPLORAR**********");
        
        //Hacer heuristica
        //....
        //Envia movimiento
        if(this.battery>5){
            //System.out.println(this.movExploracion + " bool: " + this.movExploracion!="iddle");
            if(!"iddle".equals(this.movExploracion)){
                this.enviarMovimiento(this.movExploracion);}}
        else{
            this.enviarRefuel();
        }
        this.recibir_sensores();
        
    }
    
    /**
    * @brief Envía un REQUEST al servidor para hacer refuel
    * 
    * @author Antmordhar
    */
    private void enviarRefuel() throws InterruptedException {
       System.out.println("**********ENVIAR REFUEL**********");
       
       //movimiento = heuristica(this.tipo) ;
       JsonObject msg = new JsonObject() ;
       msg.add ("command", "refuel");
       
       //Enviar movimiento
       ACLMessage outbox = new ACLMessage();
       outbox.setPerformative(ACLMessage.REQUEST);
       outbox.setSender(this.getAid());
       outbox.setReceiver(new AgentID(this.Furud));
       outbox.setConversationId(this.converID_servidor);
       outbox.setInReplyTo(this.replay);
       outbox.setContent(msg.toString());
       this.send(outbox); 
       System.out.println("Agente " + this.getAid() + " envia " + outbox.getPerformative() + " a " + outbox.getReceiver() + "con contenido" + outbox.getContent());
       
       //Recibir inform
       ACLMessage inbox = new ACLMessage();
       inbox = this.receiveACLMessage();
       this.replay = inbox.getReplyWith() ;
       
       if (inbox.getPerformativeInt() == ACLMessage.FAILURE){
           System.out.println("Agente " + this.getAid() + " recibe " + inbox.getPerformative() + " de " + inbox.getReceiver() + "con contenido" + inbox.getContent());
           this.mandar_failure_xavier();
       }
       
       if (inbox.getPerformativeInt() == ACLMessage.NOT_UNDERSTOOD){
           System.out.println("El servidor no entiende los mensajes\n");
            
           System.out.println(inbox.getContent());
           System.exit(0);
       }
       
       if (inbox.getPerformativeInt() == ACLMessage.REFUSE){
           System.out.println("No hay mas energía en el mundo\n") ;
           this.cancel();
       }
       
    }

    /**
    * @brief Calcula un movimiente Greedy una vez encontrada la solución
    * @return String con el moviento que más acerca a la solución
    * 
    * @author Antonio Morales
    * @author Ángel Polo
    * @author Julio Rodríguez
    * @author Irene Béjar
    * 
    */

 
    //Métodos algoritmo Greedy
    

        private String heuristica() {
        //Añadimos la pos del gps a la memoria
        this.setPosicionMemoria(this.x,this.y) ;
        ArrayList<Pair<Integer,Integer>> PosPosibles;
        PosPosibles=new ArrayList<Pair<Integer,Integer>>();
        ArrayList<Pair<Integer,Integer>> PosPosiblesPeq;
        PosPosiblesPeq=new ArrayList<Pair<Integer,Integer>>();
        
         //Movimiento pequeño   
        for(int i=((range-1)/2) -1, y=this.y-1; i<=((range-1)/2)+1; i++, y++){
            for(int j=((range-1)/2)-1, x=this.x-1; j<=((range-1)/2)+1; j++, x++){
                //System.out.println(this.getAid() + " Miro si puedo moverme Pequeño a: " + i + "," + j);
                if(this.getPosRadar(i, j)==0 || this.getPosRadar(i, j)==3 || (fly && this.getPosRadar(i, j)==1)){
                    //System.out.println(this.getAid() + " Consulto " + i + ","+  j + " :" + this.getPosRadar(i,j));
                    if(!Esta(x,y)){
                        //System.out.println(this.getAid() + " Meto" + x + "," + y);
                        PosPosiblesPeq.add(new Pair<Integer,Integer>(x,y));
                    }
                }
            }
        }
        
        Pair<Integer,Integer> mejorPosPeq = null;
        double mejorValorPeq = 100000.0;
        for(int i =0;i<PosPosiblesPeq.size();i++){ 
            //System.out.println(this.getPosScanner(PosPosibles.get(i).getKey(),PosPosibles.get(i).getValue()));
            if(this.getPosScanner(PosPosiblesPeq.get(i).getKey(),PosPosiblesPeq.get(i).getValue()) < mejorValorPeq ){
                mejorPosPeq=new Pair(PosPosiblesPeq.get(i).getKey(),PosPosiblesPeq.get(i).getValue());
                mejorValorPeq=this.getPosScanner(PosPosiblesPeq.get(i).getKey(),PosPosiblesPeq.get(i).getValue());
            }
        }
        String movimientoPeq = null ;
        if (mejorPosPeq != null)
            movimientoPeq= this.direccionMovimiento(mejorPosPeq.getKey(), mejorPosPeq.getValue(), this.x, this.y);
        String movimiento = null;
        if(movimientoPeq!=null){   
            movimiento= movimientoPeq;
        }
        else{
            //System.out.println("Mi Movimiento es Defecto ");
            movimiento = this.movimientoAleatorio();
        }
        
        for (int i = ((range-1)/2)-1,  y=this.y-1  ; i <= ((range-1)/2)+1 ; i++, y++){
           for (int j = ((range-1)/2)-1, x=this.x-1; j <= ((range-1)/2)+1 ; j++, x++){
               // System.out.println("Estoy en " + this.x + "," + this.y +  " y voy a " + i + " " + j);
                if (this.getPosRadar(i, j) == 3){
                    //System.out.println("Estoy en " + this.x + "," + this.y +  " y voy a " + i + " " + j);
                    movimiento = this.direccionMovimiento(x, y, this.x, this.y);
                            System.out.println("!!!!!!!!!!!!!!!!!Sudo de todo!!!!!!!!!!!!!!!!!!!!!!!!!!!!"+ movimiento);
                }
           }
        }
 
        //System.out.println("Movimiento heuristica: " + movimiento);
        return movimiento;
    }

    /**
    * @brief Inserta un aposición en la memoria del vehículo
    * 
    * @explain La memoria tiene un límite de 1000 si se añade una nueva, se borra la 
    *          más antigua y se añade
    * 
    * @author Antonio Morales 
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
     * @brief Consulta el radar del vehículo como si fuera una matriz
     * @param i Fila de la matriz
     * @param j Columna de la matriz
     * @return  Valor del radar en esa posición del vector
     * 
     */
    
    public int getPosRadar(int i, int j){
        int pos = this.range*i + j;
        return this.sensor.get(pos).asInt();
    }
    
    /**
     * @brief Consulta la dstancia desde una posición hasta la solución
     * @param i Fila de la matriz
     * @param j Columna de la matriz
     * @return Distnacia que hay
     */
    
    public double getPosScanner(int i, int j){
        double distancia = 9999.9;
                distancia = distancia(new Pair<Integer, Integer>(this.sol_x,this.sol_y) , new Pair<Integer, Integer>(i,j));

        return distancia;
    }
    
    /**
     * @brief Devuelve un movimiento aleatorio que se pueda realizar
     * @return Movimiento
     */
    private String movimientoAleatorio(){
             String movimiento = "defecto";
             int valorDado = (int) Math.floor(Math.random()*8+1);
             //System.out.println("Movimiento por defecto! Valor dado: " + valorDado);
             
            if(valorDado==1){
                if(this.getPosRadar(1, 1)==0 || this.getPosRadar(1, 1)==3)
                    movimiento="moveNW";
                else
                    movimiento = movimientoAleatorio();
             }
             
             if(valorDado==2){
                 if(this.getPosRadar(1, 3)==0 || this.getPosRadar(1, 3)==3)
                     movimiento="moveNE";
                 else
                    movimiento = movimientoAleatorio();
             }
             if(valorDado==3){
                 if(this.getPosRadar(3, 1)==0 || this.getPosRadar(3, 1)==3)
                     movimiento="moveSW";
                 else
                    movimiento = movimientoAleatorio();
             }
              if(valorDado==4){
                 if(this.getPosRadar(3, 3)==0 || this.getPosRadar(3, 3)==3)
                     movimiento="moveSE";
                 else
                     movimiento = movimientoAleatorio();
             }
             
             
             if(valorDado==5){
                if(this.getPosRadar(1, 2)==0 || this.getPosRadar(1, 2)==3)
                    movimiento="moveN";
                else
                    movimiento = movimientoAleatorio();
             }
             
             if(valorDado==6){
                 if(this.getPosRadar(3, 2)==0 || this.getPosRadar(3, 2)==3)
                     movimiento="moveS";
                 else
                    movimiento = movimientoAleatorio();
             }
             if(valorDado==7){
                 if(this.getPosRadar(2, 1)==0 || this.getPosRadar(2, 1)==3)
                     movimiento="moveW";
                 else
                    movimiento = movimientoAleatorio();
             }
              if(valorDado==8){
                 if(this.getPosRadar(2, 3)==0 || this.getPosRadar(2, 3)==3)
                     movimiento="moveE";
                 else
                     movimiento = movimientoAleatorio();
             }
             return movimiento;
    
    }
    

    /**
    * @brief Consulta si una posición está en la memoria del coche o no
    * 
    * @author Antonio Morales
    */

    public boolean Esta(int x, int y){
        Pair<Integer, Integer> pos = new Pair<Integer,Integer>(x,y) ;     
        return (this.memoria.indexOf(pos) != -1) ;
    }
    
    /**
     * @brief Calcula la distancia en línea recta de un punto origen hasta un punto destino 
     * @param punto1 Punto origen
     * @param punto2 Punto destino
     * @return Dounble con la distancia
     */
    
    private double distancia(Pair<Integer,Integer> punto1, Pair<Integer,Integer> punto2){
        
        double solucion= sqrt(pow(punto2.getKey()-punto1.getKey(),2) + pow(punto2.getValue()-punto1.getValue(),2));
        
        return solucion;
    }
    
    /**
     * @brief Dado un origen y un destino calcula el movimiento que se ha realizado
     * @param x1 Componente x de la coordenada del origen
     * @param y1 Componente y de la coordenada del origen
     * @param x2 Componente x de la coordenada del destino
     * @param y2 Componente y de la coordenada del destino
     * @return Moviento realizado
     * 
     * @author Antonio Morales
     * @author Irene Béjar
     */
public String direccionMovimiento(int x1, int y1, int x2, int y2){
        int dir_x = x1 - x2 ;
        int dir_y = y1 - y2 ;
        
        if (dir_x < 0 && dir_y < 0){
            return "moveNW" ;
        }
        else if (dir_x == 0 && dir_y < 0){
            return "moveN" ;
        }
        else if (dir_x > 0 && dir_y < 0){
            return "moveNE" ;
        }
        else if (dir_x < 0 && dir_y == 0){
            return "moveW" ;
        }
        else if (dir_x > 0 && dir_y == 0){
            return "moveE" ;
        }
        else if (dir_x < 0 && dir_y > 0){
            return "moveSW" ;
        }
        else if (dir_x == 0 && dir_y > 0){
            return "moveS" ;
        }
        else if (dir_x > 0 && dir_y > 0){
            return "moveSE" ;
        }

        return null ;
    }    
    public boolean puedoAvanzar(int x, int y, String mov, int range){
        int puedo = 0;
        
        //System.out.println("ME HAN PASADO EL MOVIMIENTO: " + mov);
        
        if (mov.equals("moveN") ){
            for (int i = 0 ; i < (range-1)/2 ; i++ ){
                y = y-1 ;
                if (this.getPosRadar(x, y) == 0 || this.getPosRadar(x, y) == 3){
                    puedo++ ;
                }
            }
            //System.out.println("PUEDO AVANZAR HACIA: "+ mov + puedo);
        }
        if (mov.equals("moveS") ){
            for (int i = 0 ; i < (range-1)/2 ; i++ ){
                y = y+1 ;
                if (this.getPosRadar(x, y) == 0 || this.getPosRadar(x, y) == 3){
                    puedo++ ;
                }
            }
            //System.out.println("PUEDO AVANZAR HACIA: "+ mov + puedo);
        }
        if (mov.equals("moveE") ){
            for (int i = 0 ; i < (range-1)/2 ; i++ ){
                x = x+1;
                if (this.getPosRadar(x, y) == 0 || this.getPosRadar(x, y) == 3){
                    puedo++ ;
                }
            }
            //System.out.println("PUEDO AVANZAR HACIA: "+ mov + puedo);
        }
        if (mov.equals("moveW") ){
            for (int i = 0 ; i < (range-1)/2 ; i++ ){
                x = x-1;
                if (this.getPosRadar(x, y) == 0 || this.getPosRadar(x, y) == 3){
                    puedo++ ;
                }
            }
            //System.out.println("PUEDO AVANZAR HACIA: "+ mov + puedo);
        }
        if (mov.equals("moveNW") ){
            for (int i = 0 ; i < (range-1)/2 ; i++ ){
                y = y-1;
                x = x-1;
                if (this.getPosRadar(x, y) == 0 || this.getPosRadar(x, y) == 3){
                    puedo++ ;
                }
            }
            //System.out.println("PUEDO AVANZAR HACIA: "+ mov + puedo);
        }
        if (mov.equals("moveNE") ){
            for (int i = 0 ; i < (range-1)/2 ; i++ ){
                y = y-1;
                x = x+1;
                if (this.getPosRadar(x, y) == 0 || this.getPosRadar(x, y) == 3){
                    puedo++ ;
                }
            }
            //System.out.println("PUEDO AVANZAR HACIA: "+ mov + puedo);
        }
        if (mov.equals("moveSW")){
            for (int i = 0 ; i < (range-1)/2 ; i++ ){
                y = y+1;
                x = x-1;
                if (this.getPosRadar(x, y) == 0 || this.getPosRadar(x, y) == 3){
                    puedo++ ;
                }
            }
            //System.out.println("PUEDO AVANZAR HACIA: "+ mov + puedo);
        }
        if (mov.equals("moveSE")){
            for (int i = 0 ; i < (range-1)/2 ; i++ ){
                y = y+1;
                x = x+1;
                if (this.getPosRadar(x, y) == 0 || this.getPosRadar(x, y) == 3){
                    puedo++ ;
                }
            }
            //System.out.println("PUEDO AVANZAR HACIA: "+ mov + puedo);
        }
        return puedo==((range-1)/2);
    }
}
