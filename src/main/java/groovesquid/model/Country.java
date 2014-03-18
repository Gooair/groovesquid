/*
 * Copyright (C) 2013 Maino
 * 
 * This work is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivs 3.0 Unported License. To view a copy of
 * this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send
 * a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco,
 * California, 94105, USA.
 * 
 */

package groovesquid.model;

/**
 *
 * @author Maino
 */
public class Country {
    private String IPR;
    private String ID;
    private String CC1;
    private String CC2;
    private String CC3;
    private String CC4;
    
    public Country() {
        this.IPR = "1";
        this.ID = "223";
        this.CC1 = "0";
        this.CC2 = "0";
        this.CC3 = "0";
        this.CC4 = "2147483648";
    }
    
    public Country(String IPR, String ID, String CC1, String CC2, String CC3, String CC4) {
        this.IPR = IPR;
        this.ID = ID;
        this.CC1 = CC1;
        this.CC2 = CC2;
        this.CC3 = CC3;
        this.CC4 = CC4;
    }
    
    @Override
    public String toString() {
        return "IPR: " + IPR + ", ID: " + ID + ", CC1: " + CC1 + ", CC2: " + CC2 + ", CC3: " + CC3 + ", CC4: " + CC4;
    }
}
