/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glynx.utils;

import glynx.utils.Arguments;

/**
 *
 * @author koen
 */
public enum GLynxArguments implements Arguments{

    /**
     * the snp file
     */
    SNP_FILE ("-snp"),
    /**
     * the gtf file
     */
    GTF_FILE ("-gtf"),
    /**
     * the window around the variants to check
     */
    WINDOW ("-w"),
    /**
     * the output directory
     */
    OUTPUT_DIR ("-o"),
    /**
     * add feature to check
     */
    FEATURE ("-f"),
    /**
     * the name of the attribute value to report
     */
    FEATURE_NAME_ATTRIBUTE ("-fn"),
    /**
     * If the given argument was invalid
     */
    INVALID_ARGUMENT ("ERROR");
    
    private final String name;
    
    private GLynxArguments(String name){
        this.name = name;
    }

    @Override
    public GLynxArguments getArgument(String sortName) {
        for (GLynxArguments arg : GLynxArguments.values()){
            if (sortName.equals(arg.getSortName())){
                return arg;
            }
        }
        return GLynxArguments.INVALID_ARGUMENT;
    }

    @Override
    public String getSortName() {
        return this.name;
    }
    
}
