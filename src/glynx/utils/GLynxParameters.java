/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glynx.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author koen
 */
public class GLynxParameters implements Parameters{
    
    private HashMap<GLynxArguments, String> arguments;
    private HashSet<String> features;
    private ArrayList<String> attributeNames;

    public GLynxParameters(){
        this.arguments = new HashMap<>();
        this.features = new HashSet<>();
        this.attributeNames = new ArrayList<>();
        this.arguments.put(GLynxArguments.OUTPUT_DIR, System.getProperty("user.dir"));
        this.arguments.put(GLynxArguments.WINDOW, "1000");
    }
    
    @Override
    public boolean containsParameter(Arguments argument) {
        if(argument instanceof GLynxArguments){
            return this.arguments.containsKey((GLynxArguments) argument);
        }else{
            return false;
        }
    }

    @Override
    public String getParameter(Arguments argument) {
        if(argument instanceof GLynxArguments){
            return this.arguments.get((GLynxArguments) argument);
        }else{
            return null;
        }
    }

    @Override
    public void setParameter(Arguments argument, String parameter) {
        if(argument instanceof GLynxArguments){
            if (((GLynxArguments) argument).equals(GLynxArguments.FEATURE)){
                this.features.add(parameter.toLowerCase());
            }else if(((GLynxArguments) argument).equals(GLynxArguments.FEATURE_NAME_ATTRIBUTE)){
                this.attributeNames.add(parameter);
            }else{
                this.arguments.put((GLynxArguments) argument, parameter);
            }
        }
    }

    @Override
    public boolean areRequiredParametersSet() {
        return (this.containsParameter(GLynxArguments.GTF_FILE) 
                && this.containsParameter(GLynxArguments.SNP_FILE) 
                && this.containsParameter(GLynxArguments.WINDOW)
                && ! this.features.isEmpty());
    }

    @Override
    public String getErrorRequiredParametersSet() {
        String error = "";
        if (! this.containsParameter(GLynxArguments.GTF_FILE)){
            error += "The GTF file is missing\n";
        }
        if (! this.containsParameter(GLynxArguments.SNP_FILE)){
            error += "The SNP file is missing\n";
        }
        if (! this.containsParameter(GLynxArguments.WINDOW)){
            error += "The WINDOW size is missing\n";
        }
        if (this.features.isEmpty()){
            error += "There are no features selected\n";
        }
        return error;
    }

    @Override
    public String getParametersLogString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getParametersHelp() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getOutputDirectory() {
        return this.arguments.get(GLynxArguments.OUTPUT_DIR);
    }
    
    public String getSNPfile(){
        return this.arguments.get(GLynxArguments.SNP_FILE);
    }
    
    public String getGTFfile(){
        return this.arguments.get(GLynxArguments.GTF_FILE);
    }
    
    public int getWindow(){
        return Integer.parseInt(this.arguments.get(GLynxArguments.WINDOW));
    }
    
    public HashSet<String> getFeatures(){
        return this.features;
    }
    
    public boolean containFeature(String feature){
        return this.features.contains(feature.toLowerCase());
    }
    
    public ArrayList<String> getAttributeNameList(){
        return this.attributeNames;
    }
    
}
