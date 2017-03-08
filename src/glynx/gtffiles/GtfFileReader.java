/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glynx.gtffiles;

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
public class GtfFileReader {
    
    private FileLocker fileLocker = new FileLocker();
    private final BufferedReader gtfBufferedReader;
    
    public GtfFileReader(File file) throws FileNotFoundException{
        this.gtfBufferedReader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(file))));
    }
    
    
    public GtfLine next() throws IOException{
        if (this.fileLocker.lock()){
            String line = this.gtfBufferedReader.readLine();
            while (line != null && line.startsWith("#")){
                line = this.gtfBufferedReader.readLine();
            }
            GtfLine gtfline;
            if (line == null){
                gtfline = null;
            }else{
                String[] linesplit = line.split("\t");
                if (linesplit.length < 9){
                    System.err.println("ERROR in gtf line");
                }
                String chromosome = linesplit[0];
                String feature = linesplit[2];
                int start = Integer.parseInt(linesplit[3]);
                int end = Integer.parseInt(linesplit[4]);
                String strand = linesplit[6];
                String attributeString = linesplit[8];
                gtfline = new GtfLine(chromosome, feature, start, end, strand, attributeString);
            }
            this.fileLocker.unlock();
            return gtfline;
        }
        return null;
    }  
    
    /**
     * closes this buffered reader
     * @throws IOException 
     */
    public void close() throws IOException{
        this.fileLocker.waitTillCompleteUnlock();
        this.gtfBufferedReader.close();
    }
    
    
    /**
     * closes first the file
     * then execute finilize()
     * @throws Throwable
     */
    @Override
    public void finalize() throws Throwable{
        try {
            this.gtfBufferedReader.close();
        } catch (IOException ex) {
            Logger.getLogger(GtfFileReader.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            super.finalize();
        }
    }
    
}
