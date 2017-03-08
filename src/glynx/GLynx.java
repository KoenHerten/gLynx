/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glynx;

import glynx.gtffiles.GtfFileReader;
import glynx.gtffiles.GtfLine;
import glynx.snpfiles.Snp;
import glynx.snpfiles.SnpFileReader;
import glynx.utils.GLynxArguments;
import glynx.utils.GLynxParameters;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author koen
 */
public class GLynx {

    
    private static final String TOOLNAME = "gLynx";
    public static final boolean DEBUG = true;
    public final static String VERSION = GLynx.TOOLNAME + " v0.1";
    private final static String LICENCE = "GPLv3";
    
    private GLynxParameters parameters;
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        if(GLynx.DEBUG == true){
            String input;
            input = "-snp /home/koen/Downloads/linkage_bed.txt "
                    + "-gtf /home/koen/Downloads/Phalaenopsis_equestris_horse_phalaenopsis_annos1-cds0-id_typename-nu1-upa1-add_chr0.gid25065.gff"
                    + " -o /home/koen/Downloads"
                    + " -f gene "
                    + " -w 1000000 "
                    + " -fn gene_name -fn NAME -fn gene_id -fn ID";
            args = input.split(" ");            
        }
        if (args.length == 0) {
            throw new IllegalArgumentException("\n\nNo arguments given.\n\n");
        }else if (args[0].equals("version") || args[0].equals("-version") || args[0].equals("-v")){
            System.out.println(GLynx.VERSION);
        }else if (args[0].equals("help") || args[0].equals("-help") || args[0].equals("-h")){
            
        }else if (args[0].equals("licence") || args[0].equals("-licence") || args[0].equals("-l")){
            System.out.println("This file is part of GBSX.\n" +
                "\n" +
                GLynx.TOOLNAME + " is free software: you can redistribute it and/or modify\n" +
                "it under the terms of the GNU General Public License as published by\n" +
                "the Free Software Foundation, either version 3 of the License, or\n" +
                "(at your option) any later version.\n" +
                "\n" +
                GLynx.TOOLNAME + " is distributed in the hope that it will be useful,\n" +
                "but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
                "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
                "GNU General Public License for more details.\n" +
                "\n" +
                "You should have received a copy of the GNU General Public License\n" +
                "along with " + GLynx.TOOLNAME + ".  If not, see <http://www.gnu.org/licenses/>.");
        }else{
            //the actual program
            GLynx gLynx = new GLynx(args);
        }
    }
    
    public GLynx(String[] args){
        this.parameters = new GLynxParameters();
            //The actual program
            //parse parameters
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            // An option.
            if (arg.startsWith("--") || arg.startsWith("-")) {
                //check if the option is invalid
                if (i + 1 >= args.length || args[i + 1].startsWith("-")) {
                    //invalid option
                    throw new RuntimeException("Value required for option "
                            + arg);
                }else{
                    //get the argument
                    GLynxArguments argument = GLynxArguments.INVALID_ARGUMENT.getArgument(arg);
                    this.parameters.setParameter(argument, args[++i]);
                }
            }
        }
        try {
            HashMap<String, HashSet<Snp>> variantMap = this.readVariants();
            HashMap<String, HashSet<InterestingFeature>> interestingFeatureMap = this.readGtfFileAndFilter(variantMap);
            this.writeInterestingFeatureList(interestingFeatureMap);
            this.writePerSNPfeatureList(interestingFeatureMap, variantMap);
            this.writeMixedFile(interestingFeatureMap, variantMap);
        } catch (IOException ex) {
            Logger.getLogger(GLynx.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public HashMap<String, HashSet<Snp>> readVariants() throws FileNotFoundException, IOException{
        SnpFileReader snpFileReader = new SnpFileReader(new File(this.parameters.getSNPfile()));
        HashMap<String, HashSet<Snp>> SNPmap = new HashMap<>();
        int count = 0;
        Snp snp;
        while ((snp = snpFileReader.next()) != null){
            count++;
            if (! SNPmap.containsKey(snp.getChromosome())){
                HashSet<Snp> set = new HashSet<>();
                SNPmap.put(snp.getChromosome(), set);                
            }
            SNPmap.get(snp.getChromosome()).add(snp);
        }
        System.out.println("Counted " + count + " SNPs on " + SNPmap.size() + " chromosomes/scaffolds/contigs");
        return SNPmap;
    }
    
    public HashMap<String, HashSet<InterestingFeature>> readGtfFileAndFilter(HashMap<String, HashSet<Snp>> variantMap) throws FileNotFoundException, IOException{
        GtfFileReader gtfFileReader = new GtfFileReader(new File(this.parameters.getGTFfile()));
        int count = 0;
        int possibleInterestingCount = 0;
        int inWindowCount = 0;
        GtfLine gtfline;
        HashMap<String, HashSet<InterestingFeature>> interestingFeatureMap = new HashMap();
        while ((gtfline = gtfFileReader.next()) != null){
            count++;
            if (count%1000 == 0){
                System.out.println("Parsed " + count + " lines in the gtf file");
            }
            if (variantMap.containsKey(gtfline.getChromosome())){
                //this line is on an interesting chromosome/scaffold/contig
                if (this.parameters.containFeature(gtfline.getFeature())){
                    //this line contains a requested feature
                    possibleInterestingCount++;
                    HashSet<Snp> snpSet = new HashSet<>();
                    for (Snp snp : variantMap.get(gtfline.getChromosome())){
                        int windowStart = snp.getPosition() - this.parameters.getWindow();
                        int windowEnd = snp.getPosition() + this.parameters.getWindow();
                        if (//start inside window:
                                (windowStart <= gtfline.getStart() && gtfline.getStart() <= windowEnd)
                                ||
                                //end inside window:
                                (windowStart <= gtfline.getEnd() && gtfline.getEnd() <= windowEnd)){
                            snpSet.add(snp);
                        }
                    }
                    if(! snpSet.isEmpty()){
                        //feature is inside the window around a snp!!
                        inWindowCount++;
                        if (! interestingFeatureMap.containsKey(gtfline.getChromosome())){
                            interestingFeatureMap.put(gtfline.getChromosome(), new HashSet<>());
                        }
                        interestingFeatureMap.get(gtfline.getChromosome()).add(new InterestingFeature(gtfline, snpSet));
                    }
                }
            }
        }
        System.out.println("Counted " + count + " gtf lines");
        System.out.println("Possible interesting: " +  possibleInterestingCount);
        System.out.println("In Window count: " + inWindowCount);
        return interestingFeatureMap;
    }
    
    public void writeInterestingFeatureList(HashMap<String, HashSet<InterestingFeature>> interestingFeatureMap) throws FileNotFoundException, IOException{
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(new FileOutputStream(
                new File(this.parameters.getOutputDirectory() + "/interestingFeatures_w" + this.parameters.getWindow() + ".tsv")))));
        writer.write("##file is 0 based\n");
        writer.write("#chr\tstart\tend\tstrand\tname\tlinkageGroup\tnumberOfSupportingSNPs\n");
        for (String chr : interestingFeatureMap.keySet()){
            for (InterestingFeature inf : interestingFeatureMap.get(chr)){
                //output a file: chr, start, end, strand, name, linkageID, number of snps supporting
                String linkageGroups = "";
                for (String l : inf.getLinkageGroups()){
                    if (! linkageGroups.equals("")){
                        linkageGroups+=",";
                    }
                    linkageGroups+=l;
                }
                writer.write(chr + "\t"
                        + (inf.getStart() -1) + "\t" 
                        + (inf.getEnd()-1) + "\t"
                        + inf.getStrand() + "\t"
                        + inf.getName(this.parameters.getAttributeNameList()) + "\t"
                        + linkageGroups + "\t"
                        + inf.getNumberOfSnps() + "\n");
            }
        }
        writer.close();
    }
    
    public void writePerSNPfeatureList(HashMap<String, HashSet<InterestingFeature>> interestingFeatureMap, HashMap<String, HashSet<Snp>> variantMap) throws FileNotFoundException, IOException{
        HashMap<String, HashMap<Snp, HashSet<InterestingFeature>>> snpFeatureMap = new HashMap<>();
        //create a map for ordered to the snps
        for (String chr : interestingFeatureMap.keySet()){
            snpFeatureMap.put(chr, new HashMap<>());
            for (InterestingFeature inf : interestingFeatureMap.get(chr)){
                for (Snp snp : inf.getSnps()){
                    if(! snpFeatureMap.get(chr).containsKey(snp)){
                        snpFeatureMap.get(chr).put(snp, new HashSet<>());
                    }
                    snpFeatureMap.get(chr).get(snp).add(inf);
                }
            }
        }
        //go over every chromosome, and every snp to generate the file
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(new FileOutputStream(
                new File(this.parameters.getOutputDirectory() + "/interestingSNPs_w" + this.parameters.getWindow() + ".tsv")))));
        writer.write("##file is 1 based\n");
        writer.write("#chr\tposition\tlinkageID\tp-value\tFDR\tfeatureList\n");
        int number = 0;
        for (String chr : variantMap.keySet()){
            number+=variantMap.get(chr).size();
        }
        System.out.println("Number of variants: " + number);
        for (String chr : variantMap.keySet()){
            ArrayList<Snp> snpset = new ArrayList<>(variantMap.get(chr));
            Collections.sort(snpset);
            for(Snp snp : snpset){
                String features = "";
                if (snpFeatureMap.containsKey(chr) && snpFeatureMap.get(chr).containsKey(snp)){
                    for (InterestingFeature inf : snpFeatureMap.get(chr).get(snp)){
                        if (! features.equals("")){
                            features+=",";
                        }
                        features+=inf.getName(this.parameters.getAttributeNameList());
                    }
                }
                writer.write(chr + "\t"
                            + snp.getPosition() + "\t"
                            + snp.getLinkageGroup() + "\t"
                            + snp.getPvalue() + "\t"
                            + snp.getFDR() + "\t"
                            + features + "\n");
            }
        }
        writer.close();        
    }
    
    public void writeMixedFile(HashMap<String, HashSet<InterestingFeature>> interestingFeatureMap, HashMap<String, HashSet<Snp>> variantMap) throws FileNotFoundException, IOException{
        ArrayList<MixedFeature> mixedFeaturesList = new ArrayList<>();
        for (String chr : interestingFeatureMap.keySet()){
            for (InterestingFeature inf : interestingFeatureMap.get(chr)){
                mixedFeaturesList.add(new MixedFeature(inf, this.parameters.getAttributeNameList()));
            }
        }
        for (String chr : variantMap.keySet()){
            for (Snp snp : variantMap.get(chr)){
                mixedFeaturesList.add(new MixedFeature(snp));
            }
        }
        
        Collections.sort(mixedFeaturesList);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(new FileOutputStream(
                new File(this.parameters.getOutputDirectory() + "/interestingMixedFile_w" + this.parameters.getWindow() + ".tsv")))));
        writer.write("##file is 1 based\n");
        writer.write("#chr\tstart\tend\tname\tstrand\tp-value\tFDR\tlinkageGroup\tnumberOfSNPs\n");
        for (MixedFeature mf : mixedFeaturesList){
            writer.write(mf.getChromosome() + "\t" +
                    mf.getStart() + "\t" + mf.getEnd() + "\t" +
                    mf.getName() + "\t" + mf.getStrand() + "\t" +
                    mf.getPvalue() + "\t" + mf.getFDR() + "\t" +
                    mf.getLinkageGroups() + "\t" + mf.getNumberOfSnps() + "\n");
        }
        writer.close();
    }
    
    private class MixedFeature implements Comparable<MixedFeature>{
        
        private final Snp snp;
        private final InterestingFeature interestingFeature;
        private final ArrayList<String> attributeNameOrderList;
        
        public MixedFeature(Snp snp){
            this.snp = snp;
            this.interestingFeature = null;
            this.attributeNameOrderList = null;
        }
        
        public MixedFeature(InterestingFeature interestingFeature, ArrayList<String> attributeNameOrderList){
            this.snp = null;
            this.interestingFeature = interestingFeature;
            this.attributeNameOrderList = attributeNameOrderList;
        }
        
        public String getChromosome(){
            if (this.isSNP()){
                return this.snp.getChromosome();
            }else{
                return this.interestingFeature.getChromosome();
            }
        }
        
        public int getStart(){
            if (this.isSNP()){
                return this.snp.getPosition();
            }else{
                return this.interestingFeature.getStart();
            }
        }
        
        public int getEnd(){
            if (this.isSNP()){
                return this.snp.getPosition() + 1;
            }else{
                return this.interestingFeature.getEnd();
            }
        }
        
        public String getName(){
            if(this.isSNP()){
                return "";
            }else{
                return this.interestingFeature.getName(attributeNameOrderList);
            }
        }
        
        public double getPvalue(){
            if(this.isSNP()){
                return this.snp.getPvalue();
            }else{
                return -1;
            }
        }
        
        public double getFDR(){
            if (this.isSNP()){
                return this.snp.getFDR();
            }else{
                return -1;
            }
        }
        
        public String getLinkageGroups(){
            if (this.isSNP()){
                return this.snp.getLinkageGroup();
            }else{
                String linkage = "";
                for (String s : this.interestingFeature.getLinkageGroups()){
                    if (! linkage.equals("")){
                        linkage+=",";
                    }
                    linkage+=s;
                }
                return linkage;
            }
        }
        
        public int getNumberOfSnps(){
            if (this.isSNP()){
                return -1;
            }else{
                return this.interestingFeature.getNumberOfSnps();
            }
        }
        
        public String getStrand(){
            if (this.isSNP()){
                return ".";
            }else{
                return this.interestingFeature.getStrand();
            }
        }
        
        private boolean isSNP(){
            if (this.snp != null){
                return true;
            }else{
                return false;
            }
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 47 * hash + Objects.hashCode(this.snp);
            hash = 47 * hash + Objects.hashCode(this.interestingFeature);
            hash = 47 * hash + Objects.hashCode(this.attributeNameOrderList);
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
            final MixedFeature other = (MixedFeature) obj;
            if (!Objects.equals(this.snp, other.snp)) {
                return false;
            }
            if (!Objects.equals(this.interestingFeature, other.interestingFeature)) {
                return false;
            }
            if (!Objects.equals(this.attributeNameOrderList, other.attributeNameOrderList)) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(MixedFeature o) {
            if (this.getChromosome().equals(o.getChromosome())){
                if (this.getStart() == o.getStart()){
                    //same start, check end
                    if (this.getEnd() == o.getEnd()){
                        //same end, check name
                        return this.getName().compareTo(o.getName());
                    }else if(this.getEnd() < o.getEnd()){
                        //this ends before
                        return -1;
                    }else{
                        return 1;
                    }
                }else if (this.getStart() < o.getStart()){
                    //this start before
                    return -1;
                }else{
                    //else o starts before
                    return 1;
                }
            }else{
                return this.getChromosome().compareTo(o.getChromosome());
            }
        }
        
        
        
        
    }
    
}
