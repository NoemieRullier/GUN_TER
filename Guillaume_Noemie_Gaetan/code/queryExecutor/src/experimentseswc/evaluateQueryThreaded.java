package experimentseswc;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PipedOutputStream;
import java.io.PipedInputStream;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class evaluateQueryThreaded {
	
    public static void main(String[] args) throws Exception {
		
        String configFile = args[0];
        Properties config = Main.loadConfiguration(configFile);
        String cQueryPath = config.getProperty("cQueryPath");
        String mappingsPath = config.getProperty("mappingsPath");
        ConjunctiveQuery q = loadCQuery(cQueryPath);
        ArrayList<ConjunctiveQuery> ms = loadMappings(mappingsPath);
        //Timer relViewsTimer = new Timer();
        //relViewsTimer.start();
        
        //relViewsTimer.stop();
        
        String sparqlQuery = config.getProperty("sQueryPath");
        String path = config.getProperty("path");
        String queryResults = config.getProperty("queryResults");		
        String queryResultsPath = queryResults + "/";
        String file = path + queryResultsPath;
        Main.makeNewDir(file);		
        String groundTruthFile = config.getProperty("groundTruth");
        String groundTruthPath = path+groundTruthFile;		
        String n3Dir = config.getProperty("n3Dir");
        String sparqlDir = config.getProperty("sparqlDir");
        boolean contactSources = Boolean.parseBoolean(config.getProperty("contactsources"));
        //String tt = path + queryResultsPath + "TimeTable";
        HashMap<String, String> constants
                               = Main.loadConstants(config.getProperty("constants"));
        //BufferedWriter timetable = new BufferedWriter(new FileWriter(tt, true));
        //path : GUNPATH/code/expfiles/ : needed
        //n3Dir : berlinData/restaurant/75views/viewsN3/ : needed but to change
        //sparqlDir : berlinData/restaurant/75views/viewsSparql/ : needed but to change
        //contactSource : contactsources=true : 
        Catalog catalog = Main.loadCatalog(config, path, n3Dir, sparqlDir, contactSources);
        execute(sparqlQuery, path, queryResultsPath, n3Dir, 
                /*timetable, */groundTruthPath, /*, relViewsTimer*/q, ms, constants, 
                catalog);
	}

    private static void execute(String sparqlQuery,
                                String PATH, String QUERY_RESULTS_PATH, String n3Dir, 
                                /*BufferedWriter timetable,*/ String GT_PATH,/*, 
                                Timer relViewsTimer*/ConjunctiveQuery cq, 
                                ArrayList<ConjunctiveQuery> ms, HashMap<String, String> 
                                constants, Catalog catalog) throws Exception {
    
        HashSet<ArrayList<String>> solutionsGathered = new HashSet<ArrayList<String>>();
        Model graphUnion = ModelFactory.createDefaultModel();
        String dir = PATH + QUERY_RESULTS_PATH +"NOTHING";
        Main.makeNewDir(dir);
        Query q = Main.readQuery(sparqlQuery);
        BufferedWriter info = new BufferedWriter(new FileWriter(dir + "/throughput", true));
        info.write("# Time (milliseconds)\t Number of answers \t Number of views considered");
        info.newLine();
        info.flush();
        Timer numberTimer = new Timer();
        numberTimer.start();

        final PipedOutputStream out = new PipedOutputStream();  
        final PipedInputStream in = new PipedInputStream(out);
        Thread tRelViews = new RelevantViewsSelector2(out, cq, ms, constants);
        tRelViews.start();
        Counter includedViews = new Counter();
        //Thread tinput = new IncludingStreamV(in, graphUnion, includedViews, PATH+n3Dir, ".n3");
        Thread tinput = new IncludingStreamV2(in, graphUnion, includedViews, catalog, constants);
        tinput.start();

        Thread tquery = new QueryingStream(graphUnion, null, q, 
                            solutionsGathered, numberTimer, includedViews, info);
        tquery.start();
        tRelViews.join();
        tinput.join();
        tquery.interrupt();
        tquery.join();
        in.close();
        info.flush();
        info.close();
        numberTimer.stop();
    }

    public static void replace (ArrayList<String> list, String prevArg, String newArg) {
    
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(prevArg)) {
                list.set(i, newArg);
            }
        }
    }
    
    public static ArrayList<String> getMapping (ConjunctiveQuery v, Predicate g, 
                                                HashMap<String, String> constants) { 
    	//System.out.println("looking mapping for subgoal "+g+". view: "+v);
    	ArrayList<Predicate> body = v.getBody();
        for (Predicate p : body) {
        	//System.out.println("in view, consider subgoal: "+p);
            if (p.getName().equals(g.getName())
                   && (p.getArguments().size() == g.getArguments().size())) {
                boolean okay = true;
                ArrayList<String> mapping = (ArrayList<String>) v.getHead().getArguments().clone();
                for (int i = 0; i < p.getArguments().size(); i++) {
                    String argV = p.getArguments().get(i);
                    String argQ = g.getArguments().get(i);
                	//System.out.println("argV: "+argV+". argQ: "+argQ);
                    // This a restriction for GUN, if it is existential, it can not be
                    // included in the UNion Graph
                    if (!v.isDistinguished(argV) && !constants.containsKey(argV)) {
                        okay = false;
                    } else if (constants.containsKey(argQ)) {
                        // Only replacing variable by constant has sense in GUN
                        replace(mapping, argV, argQ);
                    }
                }
                if (okay) {
                	//System.out.println("okay! including mapping "+mapping+" for view "+v+" with subgoal of view "+p);
                    return mapping;
                }
            }
        }
        return null;
    }

    // This version of RelevantViewsSelector intends to consider arguments in
    // the covering of views
    private static class RelevantViewsSelector2 extends Thread {

        OutputStream os;
        ConjunctiveQuery q;
        ArrayList<ConjunctiveQuery> ms;
        HashMap<String, String> cs;

        public RelevantViewsSelector2(OutputStream os, ConjunctiveQuery q, 
                                     ArrayList<ConjunctiveQuery> ms, HashMap<String, String> cs) {
            this.os = os;
            this.q = q;
            this.ms = ms;
            this.cs = cs;
        }

        public void run () {
          try {          
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);

            HashMap<Predicate,ArrayList<Predicate>> buckets = new HashMap<Predicate, ArrayList<Predicate>>();
            for (Predicate p : q.getBody()) {
                ArrayList<Predicate> b = new ArrayList<Predicate>();
                for (ConjunctiveQuery v : ms) {
                    ArrayList<String> mapping = getMapping(v, p, cs);
                    if (mapping != null) {
                        b.add(v.getHead().replace(mapping));
                    }
                }
                if (!b.isEmpty()) {
                    buckets.put(p, b);
                }
            }
            //System.out.println("buckets: " + buckets);
            ArrayList<Predicate> res = new ArrayList<Predicate>();
            boolean ready = allEmpty(buckets);
            while (!ready) {
                HashSet<Predicate> toRemove = new HashSet<Predicate>();
                //System.out.println("buckets: " + buckets);
                for (Predicate g : buckets.keySet()) {
                    ArrayList<Predicate> views = buckets.get(g);
                    if (views.size() == 1) {
                        toRemove.add(g);
                    }
                    Predicate v = views.remove(0);

                    include(res, v, cs);
                }
                //System.out.println("toRemove: "+toRemove);
                for (Predicate v : toRemove) {
                    buckets.remove(v);
                }
                ready = buckets.isEmpty();
            }
            //System.out.println("res: "+res);
            for (Predicate v : res) {
                bw.write(v+"\n");
                bw.flush();
            }
            bw.close();
            osw.close();
            os.close();
          } catch (IOException ioe) {
            ioe.printStackTrace();
          }
        }
    }

    private static boolean weaker(String argA, String argB, HashMap<String, String> cs) {
    
    	return cs.containsKey(argA) && (!cs.containsKey(argB) || !argA.equals(argB));
    }
    
    private static void include(ArrayList<Predicate> res, Predicate v, HashMap<String, String> cs) {
    
    	int vsize = v.getArguments().size();
    	ArrayList<String> argsV = v.getArguments();
    	boolean included = false;
        for (int i = 0; i < res.size(); i++) {
    		Predicate iv = res.get(i);
        	ArrayList<String> argsIV = iv.getArguments();
    		if (iv.getName().equals(v.getName()) && (iv.getArguments().size() == vsize)) {
    		    boolean coversIVV = true;
    		    boolean coversVIV = true;
    		    for (int j = 0; j < vsize; j++) {
    		    	
    		    	if (weaker(argsV.get(j), argsIV.get(j), cs)) {
    		    		coversVIV = false;
    		    	}
    		    	if (weaker(argsIV.get(j), argsV.get(j), cs)) {
    		    		coversIVV = false;
    		    	}
    		    }
    		    if (coversIVV) {
    		    	return;
    		    } else if (coversVIV && !included) {
    		    	res.set(i, v);
    		    	included = true;
    		    } else if (coversVIV && included) {
    		    	res.remove(i);
    		    	i--;
    		    }
    	    }
    	}
        if (!included) {
        	res.add(v);
        }
    }

    private static class RelevantViewsSelector extends Thread {

        OutputStream os;
        ConjunctiveQuery q;
        ArrayList<ConjunctiveQuery> ms;

        public RelevantViewsSelector(OutputStream os, ConjunctiveQuery q, 
                                     ArrayList<ConjunctiveQuery> ms) {
            this.os = os;
            this.q = q;
            this.ms = ms;
        }

        public void run () {
          try {          
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);

            HashMap<Predicate,ArrayList<String>> buckets = new HashMap<Predicate, ArrayList<String>>();
            for (Predicate p : q.getBody()) {
                ArrayList<String> b = new ArrayList<String>();
                for (ConjunctiveQuery v : ms) {
                    if (covers(v, p)) {
                        b.add(v.getHead().getName());
                    }
                }
                if (!b.isEmpty()) {
                    buckets.put(p, b);
                }
            }
            //System.out.println("buckets: " + buckets);
            ArrayList<String> res = new ArrayList<String>();
            boolean ready = allEmpty(buckets);
            while (!ready) {
                HashSet<Predicate> toRemove = new HashSet<Predicate>();
                //System.out.println("buckets: " + buckets);
                for (Predicate g : buckets.keySet()) {
                    ArrayList<String> views = buckets.get(g);
                    if (views.size() == 1) {
                        toRemove.add(g);
                    }
                    String v = views.remove(0);

                    if (!res.contains(v)) {
                        res.add(v);
                        bw.write(v+"\n");
                        bw.flush();
                    }
                }
                //System.out.println("toRemove: "+toRemove);
                for (Predicate v : toRemove) {
                    buckets.remove(v);
                }
                ready = buckets.isEmpty();
            }
            //System.out.println("res: "+res);
            bw.close();
            osw.close();
            os.close();
          } catch (IOException ioe) {
            ioe.printStackTrace();
          }
        }
    }

    private static <T> boolean allEmpty(HashMap<Predicate, ArrayList<T>> buckets) {

        boolean areEmpty = true;
        for (Predicate g : buckets.keySet()) {
            areEmpty = areEmpty && buckets.get(g).isEmpty();
        }
        return areEmpty;
    }

    private static boolean covers(ConjunctiveQuery v, Predicate g) {
        
        for (Predicate p : v.getBody()) {
            if (p.getName().equals(g.getName()) 
                   && (p.getArguments().size() == g.getArguments().size())) {
                return true;
            }
        }
        return false;
    }

    private static ArrayList<ConjunctiveQuery> loadMappings(String mappingsPath) 
                                     throws FileNotFoundException, ParseException {

        FileInputStream fis = new FileInputStream(mappingsPath);
        ConjunctiveQueryParser qp = new ConjunctiveQueryParser(fis);
        ArrayList<ConjunctiveQuery> ms = qp.ParseMappings();
        return ms;
    }

    private static ConjunctiveQuery loadCQuery(String queryPath) throws Exception {
        
        FileInputStream fis = new FileInputStream(queryPath);
        ConjunctiveQueryParser qp = new ConjunctiveQueryParser(fis);
        ConjunctiveQuery q = qp.ParseConjunctiveQuery();
        return q;
    }
}
