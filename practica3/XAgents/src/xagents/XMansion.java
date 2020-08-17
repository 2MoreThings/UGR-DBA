/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xagents;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;

/**
 * @class XMansion
 * @brief Clase con el main del proyecto
 * 
 */

public class XMansion {
    public static void main(String[] args) {
        
        AgentsConnection.connect("isg2.ugr.es", 6000, "Furud", "Delfin", "Eucken", false);
        try{
            
            System.out.println("Iniciando agente");
       
            
            Xavier xavier = new Xavier(new AgentID("Xavier"));

            
            xavier.start();

            
        }catch(Exception e){
            System.err.println("Error al crear el agente");   
        }
        
    } 
}
