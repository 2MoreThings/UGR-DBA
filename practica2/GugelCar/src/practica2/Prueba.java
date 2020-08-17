/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica2;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;

/**
 *
 * @author angelpolo
 */
public class Prueba {
    public static void main(String[] args) {
        
        //AgentsConnection.connect("localhost", 5672, "test", "guest", "guest", false);
        AgentsConnection.connect("isg2.ugr.es", 6000, "Furud", "Delfin", "Eucken", false);
        try{
            GugelCar agente = new GugelCar(new AgentID("EquipoF"));
            agente.start();
            
        }catch(Exception e){
            System.err.println("Error al crear el agente");   
        }
        
    } 
}
