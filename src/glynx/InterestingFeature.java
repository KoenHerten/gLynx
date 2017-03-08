/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glynx;

import glynx.gtffiles.GtfLine;
import glynx.snpfiles.Snp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

/**
 *
 * @author koen
 */
public class InterestingFeature {
    
    private final GtfLine gtfline;
    private final HashSet<Snp> snpSet;
    
    public InterestingFeature(GtfLine gtfLine, HashSet<Snp> snpSet){
        this.gtfline = gtfLine;
        this.snpSet = snpSet;
    }
    
    public String getChromosome(){
        return this.gtfline.getChromosome();
    }
    
    public int getStart(){
        return this.gtfline.getStart();
    }
    
    public int getEnd(){
        return this.gtfline.getEnd();
    }
    
    public String getStrand(){
        return this.gtfline.getStrand();
    }
    
    public int getNumberOfSnps(){
        return this.snpSet.size();
    }
    
    public HashSet<Snp> getSnps(){
        return this.snpSet;
    }
    
    public String getAttribute(String attributeName){
        return this.gtfline.getAttribute(attributeName);
    }
    
    public String getName(ArrayList<String> attributeNameOrderList){
        for (String name : attributeNameOrderList){
            if (this.gtfline.getAttribute(name) != null){
                return this.gtfline.getAttribute(name);
            }
        }
        return "no_name_found";
    }
    
    public HashSet<String> getLinkageGroups(){
        HashSet<String> groups = new HashSet<>();
        for (Snp snp : this.snpSet){
            groups.add(snp.getLinkageGroup());
        }
        return groups;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.gtfline);
        hash = 67 * hash + Objects.hashCode(this.snpSet);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InterestingFeature other = (InterestingFeature) obj;
        if (!Objects.equals(this.gtfline, other.gtfline)) {
            return false;
        }
        if (!Objects.equals(this.snpSet, other.snpSet)) {
            return false;
        }
        return true;
    }
    
    
}
