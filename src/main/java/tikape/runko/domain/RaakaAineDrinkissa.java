/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tikape.runko.domain;

/**
 *
 * @author nigosH
 */
public class RaakaAineDrinkissa extends RaakaAine {
    private String maara;
    
    public RaakaAineDrinkissa(Integer id, String nimi, String maara) {
        super(id, nimi);
        this.maara = maara;
    }

    public String getMaara() {
        return maara;
    }
}
