package experimentseswc;

import java.util.HashMap;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

// This version intends to include views considering the arguments
public class IncludingStreamV2 extends Thread {

    private InputStream is;
    private Model graphUnion;
    private Counter includedViews;
    private Catalog catalog;
    HashMap<String, String> constants;

    public IncludingStreamV2 (InputStream is, Model gu, Counter iv, Catalog c, 
                              HashMap<String, String> cs) {

        this.is = is;
        this.graphUnion = gu;
        this.includedViews = iv;
        this.catalog = c;
        this.constants = cs;
    }

    public void run () {

        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String v = null;
            while ((v=br.readLine())!= null) {
                Predicate view = new Predicate(v);
                String viewName = view.getName();
                graphUnion.enterCriticalSection(Lock.WRITE);
                try {
                    Model tmp =  catalog.getModel(view, constants);
                    graphUnion.add(tmp);
                    includedViews.increase();
                } catch (java.lang.OutOfMemoryError oome) {
                    System.err.println("Error during execution: "
                                      +"out of memory.");
                    return;
                } finally {
                    graphUnion.leaveCriticalSection();
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
