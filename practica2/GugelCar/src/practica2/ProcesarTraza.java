/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica2;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import static org.apache.commons.collections.CollectionUtils.index;

/**
 *
 * @author irene bejar
 */
public class ProcesarTraza {
    private BufferedImage traza ;
    private ArrayList<Integer> matriz;
    private int n_filas ;
    private int n_columnas ;
    
    /**
     * Constructor
     *
     * @author irene bejar
     * @param path string con el path para cargar la traza
     * 
    */ 
    public ProcesarTraza(String path) {
        try {
            this.traza = ImageIO.read(new File(path)) ;
        } catch (IOException ex) {
           System.out.println("Error al cargar la traza");
        }
        
        this.n_filas = this.traza.getHeight();
        this.n_columnas = this.traza.getWidth();
        
        this.matriz = new ArrayList() ;
        
        
        Color color ;
        
        for (int i=0 ; i < this.n_filas ; i++){
            for (int j=0 ; j < this.n_columnas ; j++){
                color = new Color(this.traza.getRGB(j, i)) ;
               
                
                if (color == Color.black){
                    this.matriz.add(1) ;
                }
                else if (color == Color.white || color.getGreen() == 200){
                    this.matriz.add(0) ;
                }
                else if (color.getRed()>0 && color.getBlue() == 0 && color.getGreen() == 0 ){
                    this.matriz.add(2) ;
                }
                else {
                    this.matriz.add(1);
                }
            }
        }
    }
    
    /**
     * Obtener la posicion de la posicion (i,j)
     *
     * @author irene bejar
     * @param i fila    
     * @param j columna
     * @return entero con el valor de la posicion (i,j)
     * 
    */ 
    public int get(int i, int j){
        if (i < this.n_filas || j < this.n_columnas){
          int pos = this.n_filas*i + j;
          return this.matriz.get(pos);
        }
        else return -9 ;
    }
    
    /**
     * Establecer un valor al elemento (i,j)
     *
     * @author irene bejar
     * @param i fila    
     * @param j columna
     * @param elem valor a establecer
     * 
    */ 
    public void set(int i, int j, int elem){
        int pos = 5*i + j;
        this.matriz.add(pos, elem);
    }
    
    
    /**
     * Devuelve la matriz almacenada
     *
     * @author irene bejar
     * @return ArrayList con la matriz
     * 
    */ 
    public ArrayList<Integer> getMatriz(){
        return this.matriz ;
    }
    
    /**
     * Genera un String representando el estado de la matriz de una forma gráfica
     *
     * @author irene bejar   
     * @return String con el estado de la matriz
     * 
    */ 
    public String toString(){
        String s= "";
        for (int i=0 ; i < this.n_filas ; i++){
            for (int j=0 ; j < this.n_columnas ; j++){
                s += " "+this.get(i,j)+" ";
            }
            s += "\n" ;
        }
        
        return s;
    }
    
    /**
     * Para obtener el tamaño de la matriz
     *
     * @author irene bejar
     * @return entero con el tamaño de la matriz
     * 
    */ 
    public int size(){
        return this.matriz.size();
    }
    
    /**
     * Main
     *
     * @author irene bejar
     * @param args
     * 
    */ 
    public static void main(String[] args) {
        ProcesarTraza t = new ProcesarTraza("mitraza.png");
        
        System.out.println("Esquina "+t.get(0, 0));
        
        System.out.println(t.size()) ;
        
       System.out.println(t.toString());
 
    }
    
}
