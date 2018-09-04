import java.io.*;

public class lda_data {
    public static final int OFFSET =0;
    
    
    //public int max_corpus_length(corpus* c);
    public static corpus read_data(String data_filename)
    {
    	int length, count, word, n, nd, nw;
    	corpus c = null;
    	
    	try {
    		
        	FileReader fileReader = new FileReader(data_filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            c = new corpus();
            c.num_terms = 0;
            c.num_docs = 0;
            nd = 0; nw = 0;
            String line;
            System.out.println("reading data from "+ data_filename + "\n");
            
            while ((line = bufferedReader.readLine()) != null)
            {
            	nd++;
            }
            bufferedReader.close();
            c.num_docs= nd;
            //System.out.println(nd + "  " + c.num_docs + "\n");
            nd=0;
            c.docs = new document[c.num_docs];
             
            	
             fileReader = new FileReader(data_filename);
             bufferedReader = new BufferedReader(fileReader);
             
            while ((line = bufferedReader.readLine()) != null)
            {
              	String[] container;
            	container = line.split(" ");
            	length = Integer.parseInt(container[0]);
            	//System.out.println(nd + "\n" );
            	document d = new document();
            	
            	d.length = length;
        		d.total = 0;
        		d.words = new int[length];
        		d.counts = new int[length];
        		
        		
        		
        		for (n = 0; n < length; n++)
        		{
        			int pos = container[n+1].indexOf(':');
        			word  = Integer.valueOf(container[n+1].substring(0,pos));
        			count = Integer.valueOf(container[n+1].substring(pos+1));
        			word = word - OFFSET;
        			d.words[n] = word;
        			d.counts[n] = count;
        			d.total += count;
        			if (word >= nw) { nw = word + 1; }
        		}
        		c.docs[nd]=d;
        		nd++;
            }
            bufferedReader.close();
            c.num_terms = nw;
            System.out.println("number of docs    : " + nd + "\n");
            System.out.println("number of terms   : "+ nw +"\n");
            System.out.println("Reading complete");
          }
        catch(IOException ex) {
            System.out.println("Error reading file '" + data_filename + "'");                  
        }
        
        return(c);
   }

    public static int max_corpus_length(corpus c)
    {
        int n, max = 0;
        document d = new document();
        for (n = 0; n < c.num_docs; n++) {
        	d = c.docs[n];
        	if (d.length > max) 
        		max = d.length;
        }
    	  
        return(max);
    }
    
}
