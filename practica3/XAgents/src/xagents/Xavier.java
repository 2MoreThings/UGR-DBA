/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xagents;

//import DBA.SuperAgent;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

/**
 * @class Xavier
 * 
 * @brief Clase que implementa las comunicaciones del controlador
 * 
 * @author Antonio Morales
 * @author Ángel Polo
 */
public class Xavier extends SingleAgent{
    private String Xavier; //Nombre del controlador
    
    String map="map6"; //Nombre del mapa
    String conversationIDServer=""; //Conversation ID con el server
    Map <String,String> vehiclesConversationID = new HashMap <String,String>(); //Vector con los conversation ID con los coches
    Map <String,Pair<Integer,Integer>> posVehicles =new HashMap <String,Pair<Integer,Integer>>(); // Vector con la posición de cada vehículo
    Map <String,Boolean> stopVehicles =new HashMap <String,Boolean>(); //Vehículos que están parados
    ArrayList<Pair<Integer,Integer> > posicionesSol= new ArrayList<Pair<Integer,Integer> >(); //Posiciones en las que está la solución
    private JsonArray scanner = new JsonArray(); //Scanner con las distancias de todas las casillas a la solución
    private int sol_x=-74; //Coordenada x de la solución
    private int sol_y=-74; //Cooordenada y de la solución
    private int energy; //Energía del mundo 
    ArrayList<String> agentesEnObjetivo= new ArrayList<String>(); //Vector con los vehículos que han llegado a la solución
    
    private Mapa mapa = new Mapa(); //Variable de tipo Mapa para controlar la exploración de los vehículos
    
        Map<String,ArrayList<Pair<Integer,Integer>>> memoria = new HashMap<String,ArrayList<Pair<Integer,Integer>>>(); //Variable que guarda las posiciones por las que han pasado los vehículos
    
    /**
     * @brief Constructor con parámetros
     * @param agentID Agent Id del controlador
     * @throws Exception 
     */
    Xavier(AgentID agentID) throws Exception {
        super(agentID);
        this.Xavier = "Xavier";
    }
    /**
    *  @brief Constructor con parámetros
    *  @param agentID Agent Id del controlador
    *  @param nombre Nombre del controlador
    * 
    *  @author Irene bejar
    */
    Xavier(AgentID agentID, String nombre) throws Exception {
        super(agentID);
        this.Xavier = nombre;
    }
    
 /**
 * @brief Execute del controlador
 * @author Antmordhar
 *  @author Angel Polo
 */
    @Override
    public void execute(){
        try {
            //this.cancel("ANTERIOR");
            this.subscribe(map);
            
            XVehicle wolverine = new XVehicle(new AgentID("Wolverine"), this.getAid().toString());
            XVehicle storm = new XVehicle(new AgentID("Storm"), this.getAid().toString());
            XVehicle cyclops = new XVehicle(new AgentID("Cyclops"), this.getAid().toString());
            XVehicle rogue = new XVehicle(new AgentID("Rogue"), this.getAid().toString());

            wolverine.start();
            storm.start();
            cyclops.start();
            rogue.start();
            
            this.reclutar();
            this.verCapacidades();
            this.comenzarMision();
            
            
           
            while(this.agentesEnObjetivo.size()<4){
                this.recabarInfo();

            }
            
            //System.out.println("Un mutante ha llegado a la solución");
            
            Iterator it = this.vehiclesConversationID.keySet().iterator();
            String tipoAgentes = "";
            while(it.hasNext()){
                String key = (String) it.next();
                tipoAgentes+= "_" + vehiclesConversationID.get(key);
            }
            
            this.cancel(this.map + "-" + this.conversationIDServer + tipoAgentes);
            System.exit(0);
        } catch (InterruptedException ex) {
            Logger.getLogger(Xavier.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Xavier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
/**
 * @brief Envía CANCEL al controlador 
 * @param nombreTraza Nombre de la traza
 * 
 * @author Antmordhar
 *  @author Angel Polo
 */
    public void cancel(String nombreTraza) throws InterruptedException, FileNotFoundException, IOException{
        ACLMessage outbox = new ACLMessage();
        ACLMessage inbox  = new ACLMessage();
        
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID("Furud"));
        outbox.setContent("");
        outbox.setPerformative(ACLMessage.CANCEL);
        this.send(outbox);
        System.out.println("Mando Cancel");

        inbox = this.receiveACLMessage(); //recibe Agree
        System.out.println("Recibo agree");
        inbox = this.receiveACLMessage(); //recibe traza
        
        System.out.println("Recibiendo traza");
            JsonObject injson = Json.parse(inbox.getContent()).asObject();
            JsonArray ja = injson.get("trace").asArray();
            byte data[] = new byte [ja.size()];
            for(int i=0; i<data.length; i++)
                data[i] = (byte) ja.get(i).asInt();
            FileOutputStream fos = new FileOutputStream(nombreTraza + ".png");
            fos.write(data);
            fos.close();
            System.out.print("Traza guardada");
        
        //System.out.println(inbox.getContent());
        
            System.out.println("Mision abortada");
    }
   
    /**
 * @brief Envía SUBSCRIBE para iniciar la comunicación con el servidor 
 * 
 * @author Antmordhar
 *  @author Angel Polo
 */
    public void subscribe(String map) throws InterruptedException{
        ACLMessage outbox = new ACLMessage();
        ACLMessage inbox  = new ACLMessage();
        
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID("Furud"));
    
        JsonObject mapa = new JsonObject();
        mapa.add("world",map);
        
        outbox.setContent(mapa.toString());
        outbox.setPerformative(ACLMessage.SUBSCRIBE);
        
        this.send(outbox);
        System.out.println("Xavier manda " + outbox.getPerformative() + " a " + outbox.getReceiver());
        inbox = this.receiveACLMessage();
        System.out.println("Xavier recibe " + inbox.getPerformative() + " de " + inbox.getSender() + "content: " + inbox.getContent().toString());
        //System.out.println(inbox.getContent());
        
        if(inbox.getPerformativeInt() == ACLMessage.FAILURE || inbox.getPerformativeInt() == ACLMessage.NOT_UNDERSTOOD){
            System.out.println("Soy xavier y fallo: " + inbox.getContent());
            //this.cancel();
        }
        else if(inbox.getPerformativeInt() == ACLMessage.INFORM){
            conversationIDServer=inbox.getConversationId();
            System.out.println("Soy xavier, he recibido el conversationID: " + conversationIDServer);
            //inbox = this.receiveACLMessage();
        }
    }
/**
 * @brief Recibe SUBSCRIBE de los vehículos después manda INFORM con el conversation ID
 * 
 * @author Antmordhar
 *  @author Angel Polo
 */
    public void reclutar() throws InterruptedException{
       
        for(int i=0;i<4;i++){
            
            ACLMessage outbox = new ACLMessage();
            ACLMessage inbox  = new ACLMessage();
             System.out.println("Xavier reclutando, antes de recibir subscribe");
            inbox = this.receiveACLMessage();
            System.out.println("Soy xavier, he recibido " + inbox.getPerformative() + " de " + inbox.getSender().toString() + "content: " + inbox.getContent().toString());
                      
            if(inbox.getPerformativeInt() != ACLMessage.SUBSCRIBE){
                
                outbox.setSender(this.getAid());
                outbox.setReceiver(inbox.getSender());
                outbox.setContent("");
                outbox.setPerformative(ACLMessage.FAILURE);
                System.out.println("Ha fallado al reclutar " + inbox.getSender().toString() + "Content: " + inbox.getContent());
                
                //this.send(outbox);
                //this.cancel();
            }
            else if(inbox.getPerformativeInt() == ACLMessage.SUBSCRIBE){
                this.vehiclesConversationID.put(inbox.getSender().toString(),"null");
                //ponemos una mosicion de memoria base
                this.setPosicionMemoria(-74, -74, inbox.getSender().toString());
                
                System.out.println("Guardado vehicle "+ inbox.getSender().toString()+ " tipo " + vehiclesConversationID.get(inbox.getSender().toString()));
                
                outbox.setSender(this.getAid());
                outbox.setReceiver(inbox.getSender());
                outbox.setConversationId(inbox.getSender().toString());
                
                JsonObject content = new JsonObject();
                content.add("controlador",conversationIDServer);
                
                outbox.setContent(content.toString());
                outbox.setPerformative(ACLMessage.INFORM);
                
                this.send(outbox);
                System.out.println("Xavier manda " + outbox.getPerformative() + " a " + outbox.getReceiver());
            }
        }
    }
    
/**
 * @brief Recibe REQUEST con las capabilities del vehículo
 * 
 * @author Antmordhar
 *  @author Angel Polo
 * @author Irene Béjar
 */
    public void verCapacidades() throws InterruptedException{
        for(int i=0;i<4;i++){
            
            ACLMessage outbox = new ACLMessage();
            ACLMessage inbox  = new ACLMessage();
            
            inbox = this.receiveACLMessage();
            System.out.println("Xavier recibe " + inbox.getPerformative() + " de " + inbox.getSender() + " Contenido " + inbox.getContent());
            if(inbox.getPerformativeInt() != ACLMessage.REQUEST){
                
                outbox.setSender(this.getAid());
                outbox.setReceiver(inbox.getSender());
                outbox.setConversationId(inbox.getSender().toString());
                outbox.setContent("");
                outbox.setPerformative(ACLMessage.FAILURE);
                System.out.println(inbox.getSender() + " Ha fallado las capacidades");
                
                this.send(outbox);
                System.out.println("Xavier manda " + outbox.getPerformative() + " a " + outbox.getReceiver());
                //this.cancel();
            }
            else if(inbox.getPerformativeInt() == ACLMessage.REQUEST){
                
                String contenido = inbox.getContent();
                JsonObject recibido= new JsonObject();
                JsonObject capabilities= new JsonObject();
                recibido = Json.parse(contenido).asObject() ;
                capabilities = recibido.get("capabilities").asObject() ;
                
               // if (capabilities.get("type") == )
                
                vehiclesConversationID.put(inbox.getSender().toString(),capabilities.get("type").asString());
                System.out.println("Guardado vehicle "+ inbox.getSender().toString()+ " tipo " + vehiclesConversationID.get(inbox.getSender().toString()));
            }
        }
       
    }
/**
 * @brief Manda REQUEST a los vehículos para que comiencen a moverse y recibe un INFORM de cada vehículo confirmándolo
 * 
 * @author Antmordhar
 *  @author Angel Polo
 */
    public void comenzarMision() throws InterruptedException{
        Iterator it;
        it = this.vehiclesConversationID.keySet().iterator();
        while(it.hasNext()){
            
            String key = (String) it.next();
            
            ACLMessage outbox = new ACLMessage();
            ACLMessage inbox  = new ACLMessage();
            
            outbox.setSender(this.getAid());
            outbox.setReceiver(new AgentID(key));
            outbox.setContent("start");
            outbox.setPerformative(ACLMessage.REQUEST);
            
            this.send(outbox);
            System.out.println("Xavier manda " + outbox.getPerformative() + " a " + outbox.getReceiver() + " content: " + outbox.getContent().toString());
            
            inbox = this.receiveACLMessage();
            System.out.println("Xavier recibe " + inbox.getPerformative() + " de " + inbox.getSender() + " content: " + inbox.getContent().toString());
            }
        
        }
    /**
    * @brief Recibe REQUEST con los sensores de los vehículos y manda un INFORM con la información del servidor
    * @explain El REQUEST es la infomación de los sensores tal cual les llega a los vehículos del servidor
    *          EL INFORM contiene:
    *               -Posición de la solución
    *               -Si deben parar
    * @author Antmordhar
    *  @author Angel Polo
    * @author Julio Rodriguez
    */
    public void recabarInfo() throws InterruptedException, IOException{
         Iterator it;
        it = this.vehiclesConversationID.keySet().iterator();
        while(it.hasNext()){
             String key = (String) it.next();
             
            if(!this.agentesEnObjetivo.contains(key)){
             
            ACLMessage outbox = new ACLMessage();
            ACLMessage inbox  = new ACLMessage();
            
           
            //System.out.println("Key to String" + key);
            
                outbox.setSender(this.getAid());
                outbox.setReceiver(new AgentID(key));
                
              //  System.out.println("Lo va a recibir: " + outbox.getReceiver());
                
                //La info vendrá rellena de otro método, esto es solo para formar el Json
                JsonObject info = new JsonObject();
                JsonObject sol = new JsonObject();
                sol.add ("sol_x", this.sol_x); 
                sol.add ("sol_y", this.sol_y);
                
                info.add("solucion", sol);
                //info.add("scanner", this.scanner);
                info.add("stop", stopVehicles.getOrDefault(key,false));
                
                String movExploracion;
                String type = this.vehiclesConversationID.get(key);
                int range=0 ;
                boolean volador=false;
                if (type.equals(Tipo.CAMION.toString())){
                    range = 11 ;
                }
                else if (type.equals(Tipo.COCHE.toString())){
                    range = 5 ;
                }
                else if (type.equals(Tipo.MOSCA.toString())){
                    range = 3 ;
                    volador = true;
                }
                
                
                movExploracion=calcularMovExploracion(posVehicles.getOrDefault(key,new Pair<Integer,Integer>(-74,-74)), range, volador,this.memoria.get(key));
                if(this.agentesEnObjetivo.contains(key))
                    movExploracion = "iddle";

                info.add("movimiento", movExploracion);
                
                outbox.setContent(info.toString());
                outbox.setPerformative(ACLMessage.INFORM);
                
                this.send(outbox);
                
                //System.out.println("Xavier manda " + outbox.getPerformative() + " a " + outbox.getReceiver()+ " con contenido: " + outbox.getContent());
            //System.out.println("Xavier esperando REQUEST");
            inbox = this.receiveACLMessage();
            //System.out.println("Xavier recibe " + inbox.getPerformative() + " DE " + inbox.getSender() + " content: " + inbox.getContent().toString());
            if(inbox.getPerformativeInt() != ACLMessage.REQUEST){
                
                outbox.setSender(this.getAid());
                outbox.setReceiver(inbox.getSender());
                outbox.setConversationId(inbox.getSender().toString());
                outbox.setContent("");
                outbox.setPerformative(ACLMessage.FAILURE);
                System.out.println(inbox.getSender() + " Ha fallado las recabar");
                
                this.send(outbox);
                System.out.println("Xavier manda " + outbox.getPerformative() + " a " + outbox.getReceiver());
                this.cancel(this.conversationIDServer + "_" + inbox.getContent());
            }
            else if(inbox.getPerformativeInt() == ACLMessage.REQUEST){
                //System.out.println("Xavier procesando info...");
                //Actualizo mi informacion si es la solucion
                JsonObject vehicleinfo = new JsonObject();
                vehicleinfo = Json.parse(inbox.getContent()).asObject() ;
                
                this.energy = vehicleinfo.get("result").asObject().get("energy").asInt();
                this.posVehicles.put(key.toString(),new Pair<Integer,Integer>(vehicleinfo.get("result").asObject().get("x").asInt(),vehicleinfo.get("result").asObject().get("y").asInt()));
                
               this.actualizarDescubierto(vehicleinfo.get("result").asObject().get("sensor").asArray(),posVehicles.get(key));
                
                //Guardo Mi Posicion
               // System.out.println("///////////////////Guardo mi posicion x: "+ vehicleinfo.get("result").asObject().get("x").asInt() + " y: "+ vehicleinfo.get("result").asObject().get("y").asInt()+"/////////////////////////////////////////" );
                this.setPosicionMemoria(vehicleinfo.get("result").asObject().get("x").asInt(), vehicleinfo.get("result").asObject().get("y").asInt(), key);
               
                //Si es la solucion
                if(vehicleinfo.get("result").asObject().get("goal").asBoolean()){
                   System.out.println("*********************UNAAAAAAAAAAAAAAAA     SOLUCIOOOOOOOOOOOOOOOOON**********************\n\n");
                    this.sol_x = vehicleinfo.get("result").asObject().get("x").asInt();
                    this.sol_y = vehicleinfo.get("result").asObject().get("y").asInt();
                    this.posicionesSol.add(new Pair(this.sol_x, this.sol_y));
                    this.stopVehicles.put(key.toString(),true);
                    //this.llegado++;
                    this.agentesEnObjetivo.add(key);
                    //this.makeScanner();
                }
            }
                

            }
        }
    }

    /**
    * @brief Calcula la distancia desde un origen hasta el destino
    * @param punto1 origen
    * @param punto2 destino
    * 
    * @author Julio Rodriguez
    */
    private double distancia(Pair<Integer,Integer> punto1, Pair<Integer,Integer> punto2){
        
        double solucion= sqrt(pow(punto2.getKey()-punto1.getKey(),2) + pow(punto2.getValue()-punto1.getValue(),2));
        
        return solucion;
    }
    
    /**
     * @brief Decide si el vehículo puede moverse o necesita hacer refuel
     * @param get Punto donde se encuentra el vehículo
     * @param range Tamaño del radar
     * @param volador True si el vehículo puede volar
     * @param memoria Vector con las posiciones donde ha estado el vehículo
     * @return String con el movimiento
     */

    private String calcularMovExploracion(Pair<Integer, Integer> get, int range, boolean volador,ArrayList<Pair<Integer,Integer>> memoria) {
        String mov ;
        
        if(!volador)
            mov=mapa.getMovimiento(get.getKey(), get.getValue(), range,memoria);
        
        else
            mov=mapa.getMovimientoVolador(get.getKey(), get.getValue(),range,memoria);
            
        
        return mov ;
    }
    
    /**
     * @brief Dado una posición de un vector devuelve la fila y la columna que sería en una matriz
     * @param pos Posición del vector
     * @param range Tamaño de la fila
     * @return Pair fila,columna
     */
    
       public Pair getFilCol(int pos, int range){
        int i = pos/range;
        int j = pos%range;
        return new Pair(i,j);
    }
    
       /**
        * @brief Añade la zona descubierta por el radar del coche
        * @explain Esto se añade a la varible de instancia de tipo Mapa
        * @param radar
        * @param gps 
        * 
        *@author Antmordhar
        */
    private void actualizarDescubierto(JsonArray radar, Pair<Integer, Integer> gps) {
       int x = gps.getKey() ;
        int y = gps.getValue();
       int range = (int) sqrt(radar.size());
        
        
        int cont = 0;

        
        for(int i=0, posy=y-(range-1)/2; i<range; i++, posy++){
            for(int j=0, posx=x-(range-1)/2; j<range; j++, posx++){
                mapa.add(posx, posy, radar.get(range*i+j).asInt());
            }
        }
        
    }


    /**
     * @brief Guarda una noeva posición en la memoria del vehículo
     * @param x Coordenada x de la posición
     * @param y Coordenada y de la posición
     * @param agente ConversationID del vehículo
     * 
     *  @author Antmordhar
     */
    public void setPosicionMemoria(int x, int y,String agente){
        Pair<Integer,Integer> pos = new Pair<Integer,Integer>(x,y) ;
        ArrayList<Pair<Integer,Integer>> memaux =new ArrayList<Pair<Integer,Integer>>();
        if(this.memoria.get(agente)!=null)
        memaux= this.memoria.get(agente);

        if(memaux.size()<50){
            memaux.add(pos);
            this.memoria.put(agente, memaux);
        }
        
        else{
            memaux.remove(0);
            memaux.add(pos);
            this.memoria.put(agente, memaux);
        }        
  
        }

}
