import java.io.*;

public class LdaModel {

	    public static final int NUM_INIT = 1;

		public static double myrand() {
			//Random rand = new Random();
			double  n = Math.random();
			return n;
		}
	
		public static void lda_mle(lda_model model, lda_suffstats ss,int estimate_alpha) {
			   int k;
			   int w;
			   for(k=0;k<model.num_topics;k++) {
				   for(w=0;w<model.num_terms;w++) {
					   if(ss.class_word[k][w]>0) 
						   model.log_prob_w[k][w] = Math.log(ss.class_word[k][w]) - Math.log(ss.class_total[k]);
					   else
							   model.log_prob_w[k][w] = -100;
				   }
			    }
			   if (estimate_alpha == 1)
				{
					model.alpha = lda_alpha.opt_alpha(ss.alpha_suffstats,ss.num_docs, model.num_topics);
	                 System.out.println("new alpha = " + model.alpha + " \n");  
					//printf("new alpha = %5.5f\n", model->alpha);
			    }    
		}

				/*
				 * allocate sufficient statistics
				 *
				 */

		public static lda_suffstats new_lda_suffstats(lda_model model){
				int num_topics = model.num_topics;
				int num_terms = model.num_terms;
			    int i,j;

			    lda_suffstats ss = new lda_suffstats();
			    ss.class_total = new double[num_topics];
			    ss.class_word = new double[num_topics][];
			    for (i = 0; i < num_topics; i++){
				    ss.class_total[i] = 0;
					ss.class_word[i] = new double[num_terms];
					for (j = 0; j < num_terms; j++){
					    ss.class_word[i][j] = 0;
					}
				}
				return(ss);
		}


				/*
				 * various intializations for the sufficient statistics
				 *
				 */

		public static void zero_initialize_ss(lda_suffstats ss, lda_model model)
		{
				int k, w;
				for (k = 0; k < model.num_topics; k++)
				{
				     ss.class_total[k] = 0;
				     for (w = 0; w < model.num_terms; w++)
				     {
				            ss.class_word[k][w] = 0;
				     }
				}
				    ss.num_docs = 0;
				    ss.alpha_suffstats = 0;
		}


		public static void random_initialize_ss(lda_suffstats ss, lda_model  model)
		{
				int num_topics = model.num_topics;
				int num_terms = model.num_terms;
				int k, n;
				for (k = 0; k < num_topics; k++)
			    {
				     for (n = 0; n < num_terms; n++)
				     {
				           ss.class_word[k][n] += 1.0/num_terms + myrand();
				           ss.class_total[k] += ss.class_word[k][n];
				     }
				}
	    }


	    public static void corpus_initialize_ss(lda_suffstats ss, lda_model model, corpus c)
		{
				int num_topics = model.num_topics;
				int i, j, k, d, n;
				document doc;
				int[][] seen= new int[num_topics][NUM_INIT];
				boolean already_selected;
				        
				for (k = 0; k < num_topics; k++){
				      for (i = 0; i < NUM_INIT; i++)
				      {
				            do
				            {
				              d = (int)(myrand() * c.num_docs);

				              already_selected = false;
				              for (j = 0;j < k;j++)
				              {
				                if (seen[j][i] == d)
				                {
				                  already_selected = true;
				                  System.out.println("skipping duplicate seed documnet " + d +"\n");
				                  //printf("skipping duplicate seed document %d\n", d);
				                }
				              }
				            } while (already_selected);
				            seen[k][i] = d;
				            
				            System.out.println("initialized with document "+ d+ "\n");
				            //printf("initialized with document %d\n", d);
				            //doc = &(c.docs[d]);
				             doc = c.docs[d];
				            for (n = 0; n < doc.length; n++)
				            {
				                ss.class_word[k][doc.words[n]] += doc.counts[n];
				            }
				       }
				        for (n = 0; n < model.num_terms; n++)
				        {
				            ss.class_word[k][n] += 1.0;
				            ss.class_total[k] = ss.class_total[k] + ss.class_word[k][n];
				        }
			 }
		}

	    public static void manual_initialize_ss(String seedfile, lda_suffstats ss, lda_model model, corpus c)
	    {
				 int num_topics = model.num_topics;
				 int  k, d, n;
				 document doc;

				// FILE *seeds = fopen(seedfile,"r");
				 try {
					 FileReader seeds = new FileReader(seedfile);  
			         BufferedReader buffer = new BufferedReader(seeds); 
			         if (buffer.equals(null)) {
						  System.out.println("Couldn't find manual seeds in "+ seedfile + "\n");
					      return;
					 }
			         System.out.println("Loading seeds from " + seedfile +"\n");
			         String line;
					 for (k = 0; k < num_topics; k++)
					 { 
						    line =  buffer.readLine();
						    String [] container;
						    container = line.split("\t");
					        //err = fscanf(seeds, "%d\n", &d);
					            if (line.equals(null))
					            {
					                System.out.println("Ran out of seeds "+ k +"  " + num_topics +"\n");
					                return;
					            } 
					            else if (container.length!= 1)
					            {
					                System.out.println("Couldn't read a seed from ldaseeds.txt. It should have one number per line.\n");
					                return;
					            }
                                d = Integer.valueOf(container[0]);
					            System.out.println("initialized with document " + d+ "\n");
					   
					            doc = c.docs[d];
					            for (n = 0; n < doc.length; n++)
					            {
					                ss.class_word[k][doc.words[n]] += doc.counts[n];
					            }
					        for (n = 0; n < model.num_terms; n++)
					        {
					            ss.class_word[k][n] += 1.0;
					            ss.class_total[k] = ss.class_total[k] + ss.class_word[k][n];
					        }
					 }
                  //  buffer.close();
				 }
				 catch(IOException e) {
					 e.printStackTrace();
				 }
				 
				 
		}


				/*
				 * allocate new lda model
				 *
				 */

	public static lda_model new_lda_model(int num_terms, int num_topics)
    {
				 int i,j;
				 lda_model model;

				 model = new lda_model();
				 model.num_topics = num_topics;
				 model.num_terms = num_terms;
				 model.alpha = 1.0;
				 model.log_prob_w = new double[num_topics][];
				 for (i = 0; i < num_topics; i++)
			     {
					model.log_prob_w[i] = new double[num_terms];
					for (j = 0; j < num_terms; j++)
					    model.log_prob_w[i][j] = 0;
				  }
				  return(model);
	}


				/*
				 * deallocate new lda model
				 *
				 */

	/*public static void free_lda_model(lda_model model)
	{
				 int i;

				 for (i = 0; i < model.num_topics; i++)
				 {
					free(model.log_prob_w[i]);
				 }
				    free(model.log_prob_w);
	}*/


				/*
				 * save an lda model
				 *
				 */

	public static void save_lda_model(lda_model model, String  model_root)
	{
				  String filename;
				  //FILE* fileptr;
				  int i, j;

				  //sprintf(filename, "%s.beta", model_root);
				  filename = model_root.concat(".beta");
				  try {
					  FileWriter writer   =  new FileWriter(filename);
					  BufferedWriter buffer = new BufferedWriter(writer);
					  for (i = 0; i < model.num_topics; i++)
					  {
						for (j = 0; j < model.num_terms; j++)
						{
						    //fprintf(fileptr, " %5.10f", model.log_prob_w[i][j]);
						    buffer.write(String.valueOf(model.log_prob_w[i][j]));
						    buffer.write("\t");
						    
						}
					    buffer.newLine();
					  }
					  buffer.close();
				  }
				  catch(IOException ex) {
					  ex.printStackTrace();
				  }
				  
				  //fileptr = fopen(filename, "w");
				  
				  //fclose(fileptr);

				  //sprintf(filename, "%s.other", model_root);
				  filename=  model_root.concat(".other");
				  try {
					  FileWriter writer   =  new FileWriter(filename);
					  BufferedWriter buffer = new BufferedWriter(writer);
					  
					  buffer.write("num_topics " + model.num_topics + "\n");
					  buffer.write("num_terms " + model.num_terms + "\n");
					  buffer.write("alpha " + model.alpha + "\n");
					  buffer.close();
				  }catch(IOException ex) {
					  ex.printStackTrace();
				  }
				  //fileptr = fopen(filename, "w");
				  //fprintf(fileptr, "num_topics %d\n", model.um_topics);
				  //fprintf(fileptr, "num_terms %d\n", model.num_terms);
				  //fprintf(fileptr, "alpha %5.10f\n", model.alpha);
				  //fclose(fileptr);
	}


	public static lda_model load_lda_model(String  model_root)
	{
				    String filename;
				    int i, j, num_terms =0, num_topics=0;
				    float alpha=0;
                    lda_model model = null;
				    filename=  model_root.concat(".other");
				    System.out.println("loading " + filename + "\n");
				    //printf("loading %s\n", filename);
				    //fileptr = fopen(filename, "r");
				    try {
				    	FileReader fileptr = new FileReader(filename);
				    	BufferedReader bufferedReader = new BufferedReader(fileptr);
				    	String line;
				    	int count=1;
				    	while ((line = bufferedReader.readLine()) != null) {
				    		String[] container;
				    		container = line.split(" ");
				    		if(count==1)
				    		   num_topics = Integer.valueOf(container[1]);
				    		else if(count==2)
				    			num_terms = Integer.valueOf(container[1]);
				    		else
				    			alpha = Float.valueOf(container[1]);
				    		count++;	 

				    	}
//				    	fscanf(fileptr, "num_topics %d\n", &num_topics);
//				    	fscanf(fileptr, "num_terms %d\n", &num_terms);
//				    	fscanf(fileptr, "alpha %f\n", &alpha);
				        model = new_lda_model(num_terms, num_topics);
						model.alpha = alpha;
				    	bufferedReader.close();
				    }catch(IOException ex) {
				    	ex.printStackTrace();
				    }
				   
				    filename = model_root.concat(".beta");
				    System.out.println("loading " + filename + "\n");
				    
				    try {
				    	FileReader fileptr = new FileReader(filename);
				    	BufferedReader bufferedReader = new BufferedReader(fileptr);
				        i=0;
				        String line;
				    	while ((line = bufferedReader.readLine()) != null)
					    {
				    		String [] container ;
				    		container = line.split("\t");
				    		
					        for (j = 0; j < container.length; j++)
					        {
					            model.log_prob_w[i][j] = Double.valueOf(container[j]);
					        }
					        i++;
					    }
					    bufferedReader.close();
					    
				    	
				    }catch(IOException ex) {
				    	ex.printStackTrace();
				    }
				    return(model);
	}
					
}
