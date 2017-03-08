/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glynx.snpfiles;

import glynx.utils.FileLocker;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author koen
 */
public class SnpFileReader {
    
    private FileLocker fileLocker = new FileLocker();
    private final BufferedReader snpBufferedReader;
    
    public SnpFileReader(File file) throws FileNotFoundException{
        this.snpBufferedReader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(file))));
    }
    
    
    public Snp next() throws IOException{
        if (this.fileLocker.lock()){
            String line = this.snpBufferedReader.readLine();
            Snp snp;
            if (line == null){
                snp = null;
            }else{
                String[] linesplit = line.split("\t");
                if (linesplit.length < 5){
                    System.err.println("ERROR");
                }
                String chromosome = linesplit[0];
                int position = Integer.parseInt(linesplit[1]);
                double pvalue = Double.parseDouble(linesplit[2]);
                double fdr = Double.parseDouble(linesplit[3]);
                String linkagegroup = linesplit[4];
                snp = new Snp(chromosome, position, pvalue, fdr, linkagegroup);
            }
            this.fileLocker.unlock();
            return snp;
        }
        return null;
    }  
    
    /**
     * closes this buffered reader
     * @throws IOException 
     */
    public void close() throws IOException{
        this.fileLocker.waitTillCompleteUnlock();
        this.snpBufferedReader.close();
    }
    
    
    /**
     * closes first the file
     * then execute finilize()
     * @throws Throwable
     */
    @Override
    public void finalize() throws Throwable{
        try {
            this.snpBufferedReader.close();
        } catch (IOException ex) {
            Logger.getLogger(SnpFileReader.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            super.finalize();
        }
    }
    
}
