package experimentseswc;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.shared.Lock;

import java.util.concurrent.TimeUnit;
import java.util.HashSet;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.IOException;

public class QueryingStream extends Thread {

    private Model graphUnion;
    private Reasoner reasoner;
    private Query query;
    private HashSet<ArrayList<String>> solutionsGathered;
    private Timer timer;
    private Counter counter;
    private BufferedWriter info;
    private int time = 1000;

    public QueryingStream (Model gu, Reasoner r, Query q, 
                           HashSet<ArrayList<String>> sgs, Timer t, 
                           Counter c, BufferedWriter i) {
        this.graphUnion = gu;
        this.reasoner = r;
        this.query = q;
        this.solutionsGathered = sgs;
        this.timer = t;
        this.counter = c;
        this.info = i;
    }

    private void evaluateQuery() {

        Model m = graphUnion;
        if (reasoner != null) {
            m = ModelFactory.createInfModel (reasoner, m);
        }
        m.enterCriticalSection(Lock.READ);
        QueryExecution result = QueryExecutionFactory.create(query.toString(), m);
        for (ResultSet rs = result.execSelect(); rs.hasNext();) {
            QuerySolution binding = rs.nextSolution();
            ArrayList<String> s = new ArrayList<String>();
            for (String var : query.getVars()) {
                String val = binding.get(var).toString();
                s.add(val);
            }
            if (solutionsGathered.size() == 0) {
                message(TimeUnit.MILLISECONDS.toMillis(timer.getTotalTime())
                       + "\t1\t" + this.counter.getValue());
                time = 10;
            }
            solutionsGathered.add(s);
        }
        m.leaveCriticalSection();
        message(TimeUnit.MILLISECONDS.toMillis(timer.getTotalTime()) + "\t"
                + solutionsGathered.size() + "\t" + this.counter.getValue());
    }

    private void message(String s) {
        synchronized(timer) {
            timer.stop();
            try {
                info.write(s);
                info.newLine();
                info.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            timer.resume();
        }
    }

    public void run () {

        try {
            while (true) {
                Thread.sleep(time);
                evaluateQuery();
            }
        } catch (InterruptedException ie) {
            //System.out.println("Query evaluation ended");
        } finally {
            evaluateQuery();
        }
    }
}
