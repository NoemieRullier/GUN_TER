import java.util.*;
import java.io.*;

public class numberRelViews {

    public static void main(String[] args) throws Exception {

        String dirName = args[0]; // "/home/gabriela/data/12marzo2013/300views"
        String outputFile = args[1];
        int givenK = Integer.parseInt(args[2]);
        File f = new File(dirName);
        File[] content = f.listFiles();

        if (content != null)  {
            HashMap<String, int[]> info = new HashMap<String, int[]>();
            for (File g : content) {
                if (!g.isHidden() && g.getName().startsWith("relevantViews_query")) {
                    System.out.println(g.getName()+g.getName().startsWith("relevantViews_query"));
                    //System.out.println(g.getName().substring(19));
                    String queryName = g.getName().substring(19);
                    int[] relevantViews = processFile(g, givenK);
                    info.put(queryName, relevantViews);
                }
            }
            String queries = "";
            Set<String> keys = info.keySet();
            System.out.println("number of keys: "+keys.size());
            for (String k : keys) {
                System.out.println("k: "+k);
                queries = queries + k + "\t";
            }
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(outputFile, true), 
                                                      "UTF-8"));
            output.write("# relevant views in each query/k");
            output.newLine();
            output.write("# k	"+queries);
            output.newLine();
            for (int i = 0; i < givenK; i++) {
                String l = (i+1) + "\t";
                for (String k : keys) {
                    int[] rvs = info.get(k);
                    l = l + rvs[i] + "\t";
                }
                output.write(l);
                output.newLine();
            }
            output.flush();
            output.close();
        }
    }
    public static int[] processFile(File g, int givenK) throws Exception {

        int[] rvs = new int[givenK];
        String path = g.getAbsolutePath();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String l = br.readLine();
        //System.out.println("l: "+l);
        l = br.readLine();
        //System.out.println("l: "+l);
        // Ignoring two lines
        l = br.readLine();
        //System.out.println("l: "+l);
        int i = 0;
        while (i < givenK && l != null) {
            StringTokenizer st = new StringTokenizer(l);
            int k = Integer.parseInt(st.nextToken());
            int nrv = Integer.parseInt(st.nextToken());
            //System.out.println("k: "+k+". nrv: "+nrv+". i: "+i);
            if (k == i + 1) {
                rvs[i++] = nrv;
                l = br.readLine();
                //System.out.println("l: "+l);
            } else if (k > i + 1) {
                rvs[i] = rvs[i-1];
                i++;
            } else {
                System.err.println("error while considering file "+path);
            }
        }
        if (i == 0) {
            rvs[0] = 0;
            i++;
        }
        while(i < givenK) {
            rvs[i] = rvs[i-1];
            i++;
        }
        br.close();

        return rvs;
    }
}
