package experimentseswc;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Properties;

class generateMappings {

    public static void main(String args[]) throws Exception {

        Properties config = Main.loadConfiguration("configData.properties");
        String queriesFolder = config.getProperty("queriesFolder");
        String sparqlViewsFolder = config.getProperty("sparqlViewsFolder");
        String mappingsFile = config.getProperty("mappingsFile");
        String conjunctiveQueriesFolder = config.getProperty("conjunctiveQueriesFolder");
        String constantsFile = config.getProperty("constantsFile");
        int factor = Integer.parseInt(config.getProperty("factor"));
        int n = Integer.parseInt(config.getProperty("n"));
        Main.makeNewDir(conjunctiveQueriesFolder);
        File f = new File(sparqlViewsFolder);
        File[] content = f.listFiles();
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(mappingsFile), 
                                                         "UTF-8"));
        HashMap<String, String> vars = new HashMap<String, String>();
        for (int i = 1; i <= n; i++) {
            for (int j = 0; j < factor; j++) {
                String viewName = sparqlViewsFolder+"/view"+i+/*"_"+j+*/".sparql";
                File g = new File(viewName);
                if (g.exists()) {
                    String m = getMapping(g, vars);
                    output.write(m);
                    output.newLine();
                }
            }
        }
        output.flush();
        output.close(); 
        f = new File(queriesFolder);
        content = f.listFiles();
        if (content != null) {
            for (File g : content) {
                if (g.isFile()) {
                    saveConjunctiveQuery(g, conjunctiveQueriesFolder, vars);
                }
            }
        }
        saveVars(vars, constantsFile);
    }

    public static String getMapping(File g, HashMap<String, String> vars) {

        try {
            String fileName = g.getAbsolutePath();
            String name = g.getName();
            int i = name.lastIndexOf(".");
            name = name.substring(0, i);
            FileInputStream fis = new FileInputStream(fileName);
            QueryParser qp = new QueryParser(fis);
            Query q = qp.ParseSparql();
            vars.putAll(q.getConstantsMapping());
            return q.toMapping(name);
        }  catch (Exception e) {
			e.printStackTrace(System.out);
            return null;
        }
    }

    public static void saveVars(HashMap<String, String> vars, String file) {

        try {
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(file), "UTF-8"));
            for (String k : vars.keySet()) {
                output.write(k+"\t"+vars.get(k));
                output.newLine();
            }
            output.flush();
            output.close();
        }  catch (Exception e) {
			e.printStackTrace(System.out);
        }
    }

    public static void saveConjunctiveQuery(File g, String folder, 
                                            HashMap<String, String> vars) {

        try {
            String fileName = g.getAbsolutePath();
            String name = g.getName();
            int i = name.lastIndexOf(".");
            name = name.substring(0, i);
            FileInputStream fis = new FileInputStream(fileName);
            QueryParser qp = new QueryParser(fis);
            Query q = qp.ParseSparql();
            vars.putAll(q.getConstantsMapping());
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(folder+"/"+name), 
                                                         "UTF-8"));
            output.write(q.toMapping(name));
            output.flush();
            output.close();
        }  catch (Exception e) {
			e.printStackTrace(System.out);
        }
    }
}
