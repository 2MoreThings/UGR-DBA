/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xagents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.util.Pair;

/**
 *
 * @class Mapa
 * @brief Clase para controlar el mapa de la exploración y sus movimentos
 * 
 * @author Antonio Morales
 * @author Irene Béjar
 * @author Ángel Polo
 */
public class Mapa {
     /** 
     *Variable que guarda cuanto se ha explorado por cada coche
     */
    private Map<Pair<Integer, Integer>, Pair<Integer, Integer>> mapa ;
    
    /**
     *@brief Constructor por defecto
     * 
     * @author Antonio Morales
     * @author Irene Béjar
     * @author Ángel Polo
     */
    Mapa (){
        mapa = new HashMap<Pair<Integer,Integer>,Pair<Integer, Integer>>() ;
    }
    
      // Métodos públicos

    /**
     * @brief Añade una posición del mapa y su valor del radar
     * @param x Componente x de la coordenada
     * @param y Componente y de la coordenada
     * @param radar Valor del radar para dicha coordenada (0,1,2,3,4)
     * 
     * @author Antonio Morales
     * @author Irene Béjar
     * @author Ángel Polo
     */
    public void add(int x, int y, int radar){
        Pair <Integer, Integer> pos = new Pair <>(x, y);
        int numpisados = this.getValor(x,y);
        if(radar== 3){
            numpisados= -9999;
        }
        else{
            numpisados++;
        }

        Pair <Integer, Integer> casilla = new Pair <>(radar,numpisados);
        mapa.put(pos, casilla);
    }
    
    
    /**
     * @brief Devuelve el valor del radar para una posición del mapa
     * @param x Componente x de la coordenada
     * @param y Componente y de la coordenada
     * @return Valor del radar
     * 
     * @author Antonio Morales
     * @author Irene Béjar
     * @author Ángel Polo
     */
    public int getRadar (int x, int y){
        Pair <Integer, Integer> pos = new Pair <Integer, Integer>(x, y);
        if(this.descubierto(x, y)){
        return mapa.get(pos).getKey();
        }
        return -1;
    }
    
    
    /**
     * @brief Valor de una casilla 
     * @param x Componente x de la coordenada
     * @param y Componente y de la coordenada
     * @return Valor de la casilla
     * 
     * @explain A cada posición se le asociado un valor para determinar cuanto se
     *          descubre con ella. A menor es el valor mejor
     * 
     * @author Antonio Morales
     * @author Irene Béjar
     * @author Ángel Polo
     */
    public int getValor(int x, int y){
        Pair <Integer, Integer> pos = new Pair <Integer, Integer>(x, y);
        if(this.descubierto(x, y)){
        return mapa.get(pos).getValue();
        }
        return 0;
    }
    
    
    /**
     * @bried Indica si una casilla ha sido descubierta
     * @param x Componente x de la coordenada
     * @param y Componente y de la coordenada
     * @return Si está decubierta true, si no false
     * 
     * @author Antonio Morales
     * @author Irene Béjar
     * @author Ángel Polo
     */
    
    public boolean descubierto (int x, int y){
        Pair <Integer, Integer> pos = new Pair <>(x, y);
        return mapa.containsKey(pos);
    }
    
    
        /**
     * @brief Calcula cuanto se descubre en una posición del mapa
     * @param x Componente x de la coordenada
     * @param y Componente x de la coordenada
     * @param range Tamaño del radar del vehículo
     * @return Suma del valor de todas las casillas que se ven desde una posición
     * 
     * @explain Dada una posición se calcula la suma del valor de todas las casillas
     *          que ve el radar desde ahí
     * 
     * @author Antonio Morales
     * @author Irene Béjar
     * @author Ángel Polo
     */
    public int nSinDescubrir(int x, int y, int range){
        
        
        
        int valor = 0;
        
        for ( int i= x - (range-1)/2; i <= (x+(range-1)/2) ; i++){
            for ( int j= y - (range-1)/2 ; j <= (y+(range-1)/2) ; j++){
                if (this.descubierto(i, j)){
                    valor += this.getValor(i, j);
                }
            }
        }
        
        return valor ;
        
    }
    
   /**
    * @brief Cálcula el mejor moviento para explorar dada una posición
    * @param x Componente x de la coordenada
    * @param y Componente y de la coordenada
    * @param range Tamaño del radar del vehículo
    * @param memoria Casillas por las que ha pasado el vehículo
    * @return Mejor movimiento realizable
    * 
    * @author Antonio Morales
    * @author Irene Béjar
    */
    public String getMovimiento (int x, int y, int range,ArrayList<Pair<Integer,Integer>> memoria){

        int max =Integer.MAX_VALUE ;
        int max_default = Integer.MAX_VALUE;
        String movimiento = "iddle" ;
        String dafault  ="iddle" ;
        
        if(this.nSinDescubrir(x, y-1, range)<=max_default && (this.getRadar(x, y-1)==0 || this.getRadar(x, y-1)==3)&&!Esta(x,y-1,memoria)){
                dafault="moveN";
                max_default = this.nSinDescubrir(x, y-1, range);
        }
        
        if(this.nSinDescubrir(x, y+1, range)<=max_default && (this.getRadar(x, y+1)==0 || this.getRadar(x, y+1)==3)&&!Esta(x,y+1,memoria)){
                dafault="moveS";
                max_default = this.nSinDescubrir(x, y+1, range);
        }
        
        if(this.nSinDescubrir(x+1, y, range)<=max_default && (this.getRadar(x+1, y)==0 || this.getRadar(x+1, y)==3)&&!Esta(x+1,y,memoria)){
                dafault="moveE";
                max_default = this.nSinDescubrir(x+1, y, range);
        }
        
        if(this.nSinDescubrir(x-1, y, range)<=max_default && (this.getRadar(x-1, y)==0 || this.getRadar(x-1, y)==3)&&!Esta(x-1,y,memoria)){
                dafault="moveW";
                max_default = this.nSinDescubrir(x-1, y, range);
        }  
        
        if (this.nSinDescubrir(x-1, y-1, range)<=max_default && (this.getRadar(x-1, y-1)==0 || this.getRadar(x-1, y-1)==3)&&!Esta(x-1,y-1,memoria)){ 
                dafault="moveNW";
                max_default = this.nSinDescubrir(x-1, y-1, range);
        }
        if(this.nSinDescubrir(x+1, y-1, range)<=max_default && (this.getRadar(x+1, y-1)==0 || this.getRadar(x+1, y-1)==3)&&!Esta(x+1,y-1,memoria)){
                dafault="moveNE";
                max_default = this.nSinDescubrir(x+1, y-1, range);
        }

        if(this.nSinDescubrir(x-1, y+1, range)<=max_default && (this.getRadar(x-1, y+1)==0 || this.getRadar(x-1, y+1)==3)&&!Esta(x-1,y+1,memoria)){
                dafault="moveSW";
                max_default = this.nSinDescubrir(x-1, y+1, range);
        }
        
        if(this.nSinDescubrir(x+1, y+1, range)<=max_default && (this.getRadar(x+1, y+1)==0 || this.getRadar(x+1, y+1)==3)&&!Esta(x+1,y+1,memoria)){
                dafault="moveSE";
                max_default = this.nSinDescubrir(x+1, y+1, range);
        }
        
          
        
        if(this.nSinDescubrir(x, y-1, range)<=max && (this.getRadar(x, y-1)==0 || this.getRadar(x, y-1)==3)&&!Esta(x,y-1,memoria)){
            if (this.puedoAvanzar(x, y, "moveN", range)){
                movimiento="moveN";
                max = this.nSinDescubrir(x, y-1, range);
            }
        }
        
        if(this.nSinDescubrir(x, y+1, range)<=max && (this.getRadar(x, y+1)==0 || this.getRadar(x, y+1)==3)&&!Esta(x,y+1,memoria)){
            if (this.puedoAvanzar(x, y, "moveS", range)){
                movimiento="moveS";
                max = this.nSinDescubrir(x, y+1, range);
            }
        }
        
        if(this.nSinDescubrir(x+1, y, range)<=max && (this.getRadar(x+1, y)==0 || this.getRadar(x+1, y)==3)&&!Esta(x+1,y,memoria)){
            if (this.puedoAvanzar(x, y, "moveE", range)){
                movimiento="moveE";
                max = this.nSinDescubrir(x+1, y, range);
            }
        }
        
        if(this.nSinDescubrir(x-1, y, range)<=max && (this.getRadar(x-1, y)==0 || this.getRadar(x-1, y)==3)&&!Esta(x-1,y,memoria)){
            if (this.puedoAvanzar(x, y, "moveW", range)){
                movimiento="moveW";
                max = this.nSinDescubrir(x-1, y, range);
            }
        }
        
        if (this.nSinDescubrir(x-1, y-1, range)<=max && (this.getRadar(x-1, y-1)==0 || this.getRadar(x-1, y-1)==3)&&!Esta(x-1,y-1,memoria)){ 
            if (this.puedoAvanzar(x, y, "moveNW", range) ){
                movimiento="moveNW";
                max = this.nSinDescubrir(x-1, y-1, range);
            }
        }
        if(this.nSinDescubrir(x+1, y-1, range)<=max && (this.getRadar(x+1, y-1)==0 || this.getRadar(x+1, y-1)==3)&&!Esta(x+1,y-1,memoria)){
            if (this.puedoAvanzar(x, y, "moveNE", range)){
                movimiento="moveNE";
                max = this.nSinDescubrir(x+1, y-1, range);
            }
        }

        if(this.nSinDescubrir(x-1, y+1, range)<=max && (this.getRadar(x-1, y+1)==0 || this.getRadar(x-1, y+1)==3)&&!Esta(x-1,y+1,memoria)){
            if(this.puedoAvanzar(x, y, "moveSW", range)){
                movimiento="moveSW";
                max = this.nSinDescubrir(x-1, y+1, range);
            }
        }
        
        if(this.nSinDescubrir(x+1, y+1, range)<=max && (this.getRadar(x+1, y+1)==0 || this.getRadar(x+1, y+1)==3)&&!Esta(x+1,y+1,memoria)){
            if (this.puedoAvanzar(x, y, "moveSE", range)){
                movimiento="moveSE";
                max = this.nSinDescubrir(x+1, y+1, range);
            }
        }

        if (movimiento.equals("iddle")){
            movimiento = dafault ;
        }

        
        if (movimiento.equals("iddle")  && dafault.equals("iddle") ){
            for (int i=x-1 ; i<=x+1 ; i++){
                for (int j=y-1; j<=y+1 ; j++){
                    if (this.getRadar(i, j)==0 || this.getRadar(i, j) == 3){
                        movimiento = this.direccionMovimiento(x, y, i, j);
                    }
                }
            }
        }
        
        
        
        return movimiento ;
    }    
    
    
        /**
         * @brief Cálcula el mejor moviento para explorar dada una posición, para un vehículo volador
         * @param x Componente x de la coordenada
         * @param y Componente y de la coordenada
         * @param range Tamaño del radar del vehículo
         * @param memoria Casillas por las que ha pasado el vehículo
         * @return Mejor movimiento realizable
         * 
         * @author Antonio Morales
         * @author Irene Béjar
         * @author Ángel Polo
        
        */
        public String getMovimientoVolador(int x, int y, int range,ArrayList<Pair<Integer,Integer>> memoria){
        int max = Integer.MAX_VALUE ;
        String movimiento = "iddle" ;
        
        if (this.nSinDescubrir(x-1, y-1, range)<=max && (this.getRadar(x-1, y-1)==0 || this.getRadar(x-1, y-1)==3 || this.getRadar(x-1, y-1)==1)&&!Esta(x-1,y-1,memoria)){         
            movimiento="moveNW";
            max = this.nSinDescubrir(x-1, y-1, range);
        }
        if(this.nSinDescubrir(x+1, y-1, range)<=max && (this.getRadar(x+1, y-1)==0 || this.getRadar(x+1, y-1)==3|| this.getRadar(x+1, y-1)==1)&&!Esta(x+1,y-1,memoria)){
            movimiento="moveNE";
            max = this.nSinDescubrir(x+1, y-1, range);
        }

        if(this.nSinDescubrir(x-1, y+1, range)<=max && (this.getRadar(x-1, y+1)==0 || this.getRadar(x-1, y+1)==3|| this.getRadar(x-1, y+1)==1)&&!Esta(x-1,y+1,memoria)){
            movimiento="moveSW";
            max = this.nSinDescubrir(x-1, y+1, range);
        }
        
        if(this.nSinDescubrir(x+1, y+1, range)<=max && (this.getRadar(x+1, y+1)==0 || this.getRadar(x+1, y+1)==3 || this.getRadar(x+1, y+1)==1)&&!Esta(x+1,y+1,memoria)){
            movimiento="moveSE";
            max = this.nSinDescubrir(x+1, y+1, range);
        }
        
        if(this.nSinDescubrir(x, y-1, range)<max && (this.getRadar(x, y-1)==0 || this.getRadar(x, y-1)==3 || this.getRadar(x, y-1)==1)&&!Esta(x,y-1,memoria)){
            movimiento="moveN";
            max = this.nSinDescubrir(x, y-1, range);
        }
        
        if(this.nSinDescubrir(x, y+1, range)<max && (this.getRadar(x, y+1)==0 || this.getRadar(x, y+1)==3 || this.getRadar(x, y+1)==1)&&!Esta(x,y+1,memoria)){
            movimiento="moveS";
            max = this.nSinDescubrir(x, y+1, range);
        }
        
        if(this.nSinDescubrir(x+1, y, range)<max && (this.getRadar(x+1, y)==0 || this.getRadar(x+1, y)==3 || this.getRadar(x+1, y)==1)&&!Esta(x+1,y,memoria)){
            movimiento="moveE";
            max = this.nSinDescubrir(x+1, y, range);
        }
        
        if(this.nSinDescubrir(x-1, y, range)<max && (this.getRadar(x-1, y)==0 || this.getRadar(x-1, y)==3 || this.getRadar(x-1, y)==1)&&!Esta(x-1,y,memoria)){
            movimiento="moveW";
            max = this.nSinDescubrir(x-1, y, range);
        }
        
        else if (max == Integer.MAX_VALUE){
            //System.out.println("**********ENTRO EN RANDOM*************");
            for (int i=x-1 ; i<=x+1 ; i++){
                for (int j=y-1; j<=y+1 ; j++){
                    if (this.getRadar(i, j)==0 || this.getRadar(i, j) == 3){
                        movimiento = this.direccionMovimiento(x, y, i, j);
                    }
                }
            }
        }
        //System.out.println("Movimiento: " + movimiento);
        
        return movimiento ;
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
                if (this.getRadar(x, y) == 0 || this.getRadar(x, y) == 3){
                    puedo++ ;
                }
            }
            //System.out.println("PUEDO AVANZAR HACIA: "+ mov + puedo);
        }
        if (mov.equals("moveS") ){
            for (int i = 0 ; i < (range-1)/2 ; i++ ){
                y = y+1 ;
                if (this.getRadar(x, y) == 0 || this.getRadar(x, y) == 3){
                    puedo++ ;
                }
            }
            //System.out.println("PUEDO AVANZAR HACIA: "+ mov + puedo);
        }
        if (mov.equals("moveE") ){
            for (int i = 0 ; i < (range-1)/2 ; i++ ){
                x = x+1;
                if (this.getRadar(x, y) == 0 || this.getRadar(x, y) == 3){
                    puedo++ ;
                }
            }
            //System.out.println("PUEDO AVANZAR HACIA: "+ mov + puedo);
        }
        if (mov.equals("moveW") ){
            for (int i = 0 ; i < (range-1)/2 ; i++ ){
                x = x-1;
                if (this.getRadar(x, y) == 0 || this.getRadar(x, y) == 3){
                    puedo++ ;
                }
            }
            //System.out.println("PUEDO AVANZAR HACIA: "+ mov + puedo);
        }
        if (mov.equals("moveNW") ){
            for (int i = 0 ; i < (range-1)/2 ; i++ ){
                y = y-1;
                x = x-1;
                if (this.getRadar(x, y) == 0 || this.getRadar(x, y) == 3){
                    puedo++ ;
                }
            }
            //System.out.println("PUEDO AVANZAR HACIA: "+ mov + puedo);
        }
        if (mov.equals("moveNE") ){
            for (int i = 0 ; i < (range-1)/2 ; i++ ){
                y = y-1;
                x = x+1;
                if (this.getRadar(x, y) == 0 || this.getRadar(x, y) == 3){
                    puedo++ ;
                }
            }
            //System.out.println("PUEDO AVANZAR HACIA: "+ mov + puedo);
        }
        if (mov.equals("moveSW")){
            for (int i = 0 ; i < (range-1)/2 ; i++ ){
                y = y+1;
                x = x-1;
                if (this.getRadar(x, y) == 0 || this.getRadar(x, y) == 3){
                    puedo++ ;
                }
            }
            //System.out.println("PUEDO AVANZAR HACIA: "+ mov + puedo);
        }
        if (mov.equals("moveSE")){
            for (int i = 0 ; i < (range-1)/2 ; i++ ){
                y = y+1;
                x = x+1;
                if (this.getRadar(x, y) == 0 || this.getRadar(x, y) == 3){
                    puedo++ ;
                }
            }
            //System.out.println("PUEDO AVANZAR HACIA: "+ mov + puedo);
        }
        return puedo == (range-1)/2;
        
    }      
    
    
    /**
      * @brief Indica si una posición se encuentra en la memoria del coche
      * @param x Componente x de la coordenada
      * @param y Componente y de la coordenada
      * @param memoria Casillas por las que ha pasado el vehículo
      * @return True si ha pasado por ella, false si no
      * 
      * @author Antonio Morales
    */ 
  
    public boolean Esta(int x, int y,ArrayList<Pair<Integer,Integer>> memoria){
        Pair<Integer, Integer> pos = new Pair<Integer,Integer>(x,y) ;     
        return (memoria.indexOf(pos) != -1) ;
    }
}
