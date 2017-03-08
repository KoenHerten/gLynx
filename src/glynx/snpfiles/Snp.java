/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glynx.snpfiles;

import java.util.Objects;

/**
 *
 * @author koen
 */
public class Snp implements Comparable<Snp>{
    
    private final String chromosome;
    private final int position;
    private final double pvalue;
    private final double fdr;
    private final String linkageGroup;
    
    public Snp(String chromosome, int position, double pvalue, double fdr, String linkageGroup){
        this.chromosome = chromosome;
        this.position = position;
        this.pvalue = pvalue;
        this.fdr = fdr;
        this.linkageGroup = linkageGroup;
    }
    
    public String getChromosome(){
        return this.chromosome;
    }
    
    public int getPosition(){
        return this.position;
    }
    
    public double getPvalue(){
        return this.pvalue;
    }
    
    public double getFDR(){
        return this.fdr;
    }
    
    public String getLinkageGroup(){
        return this.linkageGroup;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.chromosome);
        hash = 37 * hash + this.position;
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.pvalue) ^ (Double.doubleToLongBits(this.pvalue) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.fdr) ^ (Double.doubleToLongBits(this.fdr) >>> 32));
        hash = 37 * hash + Objects.hashCode(this.linkageGroup);
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
        final Snp other = (Snp) obj;
        if (this.position != other.position) {
            return false;
        }
        if (Double.doubleToLongBits(this.pvalue) != Double.doubleToLongBits(other.pvalue)) {
            return false;
        }
        if (Double.doubleToLongBits(this.fdr) != Double.doubleToLongBits(other.fdr)) {
            return false;
        }
        if (!Objects.equals(this.chromosome, other.chromosome)) {
            return false;
        }
        if (!Objects.equals(this.linkageGroup, other.linkageGroup)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Snp o) {
        if (this.getChromosome().equals(o.getChromosome())){
            if (this.getPosition() < o.getPosition()){
                return -1;
            }else if (this.getPosition() == o.getPosition()){
                return 0;
            }else{
                return 1;
            }
        }else{
            return this.getChromosome().compareTo(o.getChromosome());
        }
    }

    
    
    
    
}
