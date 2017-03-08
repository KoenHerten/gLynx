/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glynx.gtffiles;

import java.util.HashMap;

/**
 *
 * @author koen
 */
public class GtfLine {
    
    private final String chromosome;
    private final String feature;
    private final int start;
    private final int end;
    private final String strand;
    private HashMap<String, String> attributes;
    
    public GtfLine(String chromosome, String feature, int start, int end, String strand, String attributeString){
        this.chromosome = chromosome;
        this.feature = feature;
        this.start = start;
        this.end = end;
        this.strand = strand;
        this.attributes = new HashMap<>();
        String[] attributesArray = attributeString.split(";");
        for (String att : attributesArray){
            String[] attArray = att.split("=");
            this.attributes.put(attArray[0], attArray[1]);
        }
    }
    
    public String getChromosome(){
        return this.chromosome;
    }
    
    public String getFeature(){
        return this.feature;
    }
    
    public int getStart(){
        return this.start;
    }
    
    public int getEnd(){
        return this.end;
    }
    
    public String getStrand(){
        return this.strand;
    }
    
    public HashMap<String, String> getAttributesMap(){
        return this.attributes;
    }
    
    public String getAttribute(String attributeName){
        return this.attributes.get(attributeName);
    }
    
}
