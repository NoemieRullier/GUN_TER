import java.io.*;
import java.util.*;

// java processResultsRV /home/gabriela/data/FiveMillions/300views outputquery /home/gabriela/data/12marzo2013/relViewsInK /home/gabriela/Documents/results
public class processResultsRV {

    public static void main(String args[]) throws Exception {

        String dir = args[0]; // "/home/gabriela/data/12marzo2013/300views";
        String dir2 = args[1]; // "outputquery";
        String answersFile = args[2]; // "/home/gabriela/gun2012/code/expfiles/berlinData/FiveMillions/answersSize";
        String outDir = args[3]; // where the output must go..
        File f = new File(dir);
        File[] content = f.listFiles();
        String setup = dir.substring(dir.lastIndexOf("/")+1);
        HashMap<String, String> answerSize = readAnswerSize(answersFile);

        if (content != null) {

            for (File g : content) {
                if (g.isDirectory() && g.getName().startsWith(dir2)) { //"outputDataset10query")) {
                    String a = getApproach(g.getName());
                    processFolder(g, outDir, ("data"+setup+a), answerSize, a);    
                }
            }
        }
    }

    public static String getApproach(String s) {

        if (s.endsWith("P")) {
            return "LLP";
        } else if (s.endsWith("N")) {
            return "GUN";
        } else if (s.endsWith("A")) {
            return "JENA";
        } else {
            return "ERROR";
        }
    }

    public static void processFolder(File g, String outDir, String name, 
                                     HashMap<String, String> answerSize, String approach) throws Exception {

        String dirName = g.getAbsolutePath();
        //String approach = getApproach(g.getName());
        String timeFile = dirName+"/TimeTable"+approach;
        //System.out.println("timeFile: "+timeFile);
        int[][] timeEC = readTimes(timeFile, "EQUIVALENCECLASSSORT");
        int[][] timeN = readTimes(timeFile, "NOTHING");
        //System.out.println("times de nothing: "+timeN.length);
        File[] content = g.listFiles();
        String y = dirName.substring(dirName.indexOf("q"));
	y = y.substring(5);
        if (content != null) {
            for (File h : content) {
                if (h.isDirectory() && !h.isHidden()) {
                    try {
                        processFolder2(h, timeEC, timeN, outDir, y, approach, name, answerSize);    
                    } catch (Exception e) {
                        System.err.println("Problems with "+g.getAbsolutePath());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static int[][] readTimes(String fileName, String fs) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String l = br.readLine();
        int k = 0;
        while (l != null) {
            if (l.indexOf(fs) >= 0) {
                k++;
            }
            l = br.readLine();
        }
        br.close();
        int[][] times = new int[k][4];
        br = new BufferedReader(new FileReader(fileName));
        k = 0;
        l = br.readLine();
        while (l != null) {
            if (l.indexOf(fs) >= 0) {
                //System.out.println(l);
                //System.out.println(l.lastIndexOf(" "));
                //System.out.println(l.lastIndexOf("\t"));
                StringTokenizer st = new StringTokenizer(l);
                st.nextToken();
                st.nextToken();
                int j = 0;
                while (st.hasMoreTokens() && j < 4) {
                    times[k][j] = Integer.parseInt(st.nextToken());
                    j++;
                }
                //times[k] = l.substring(l.lastIndexOf(" ")+1);
                k++;
            }
            l = br.readLine();            
        }
        br.close();
        return times;
    }

    public static HashMap<String, String> readAnswerSize(String answersFile) throws Exception {

        HashMap<String, String> hm = new HashMap<String, String>();
        BufferedReader br = new BufferedReader(new FileReader(answersFile));
        String l = br.readLine();
        while (l != null) {
            int p = l.lastIndexOf(" ");
            hm.put(l.substring(0, p), l.substring(p+1));
            l = br.readLine();
        }
        br.close();

        return hm;
    }

    public static int[] getAnswerPercentage(String fileName, int k, int answerSize) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String l = br.readLine();
        int[] pas = new int[k];
        int i = 0;
        while (l != null) {
            //System.out.println("l: "+l);
            //System.out.println("last tab in pos: "+l.lastIndexOf("\t"));
            //System.out.println("substring: "+l.substring(l.lastIndexOf("\t")+1));
            int size = Integer.parseInt(l.substring(l.lastIndexOf("\t")+1));
            pas[i++] = (int) ((size*100)/answerSize);
            l = br.readLine();
        }
        br.close();

        return pas;
    }

    public static int[] getAnswerNumber(String fileName, int k) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String l = br.readLine();
        int[] an = new int[k];
        int i = 0;
        while (l != null) {
            //System.out.println("l: "+l);
            //System.out.println("last tab in pos: "+l.lastIndexOf("\t"));
            //System.out.println("substring: "+l.substring(l.lastIndexOf("\t")+1));
            int size = Integer.parseInt(l.substring(l.lastIndexOf("\t")+1));
            an[i++] = size;
            l = br.readLine();
        }
        br.close();

        return an;
    }

    public static String[] getRecall(String fileName, int k) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String l = br.readLine();
        String[] recalls = new String[k];
        int i = 0;
        while (l != null) {
            recalls[i++] = l.substring(l.lastIndexOf(" ")+1);
            l = br.readLine();
        }
        br.close();

        return recalls;
    }

    public static int[] getSortedAnswersSize(String fileName, int k) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String l = br.readLine();
        int[] sizes = new int[k];
        int i = 0;
        while (l != null) {
            sizes[i++] = Integer.parseInt(l);
            l = br.readLine();
        }
        java.util.Arrays.sort(sizes);
        br.close();

        return sizes;
    }

    public static String[] getModelSize(String fileName, int k) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String l = br.readLine();
        String[] sizes = new String[k];
        int i = 0;
        System.out.println(fileName);
        System.out.println(l);
        while (l != null) {
            sizes[i++] = l.replace('+', '\t');
            l = br.readLine();
            System.out.println(l);
        }
        br.close();

        return sizes;
    }

    public static void processFolder2(File g, int[][] timeEC, int[][] timeN, String outDir, String y, String approach, String name, HashMap<String, String> answersSize) throws Exception {

        String dirName = g.getName();
        //System.out.println(dirName);
        String dirPath = g.getAbsolutePath();
        int[][] times = dirName.endsWith("NOTHING") ? timeN : (dirName.endsWith("EQUIVALENCECLASSSORT") ? timeEC : null);
        String x = (dirName.endsWith("EQUIVALENCECLASSSORT")) ? "EC" : (dirName.endsWith("NOTHING") ? "N" : "ERROR");
        //int answerSize[] = getSortedAnswersSize(dirPath+"/AnswersSize", times.length);
        //String modelSize[] = getModelSize(dirPath+"/info2", times.length);
        String numQuery = y.substring(0, y.indexOf(approach));
        //System.out.println(answersSize);
        //System.out.println(numQuery);
        //String recall[] = getRecall(dirPath+"/Recall", times.length);
        //String recallM[] = getRecall(dirPath+"/RecallMains", times.length);
        //String recallD[] = getRecall(dirPath+"/RecallDesserts", times.length);
        String outputName = outDir+"/"+name+"Query"+numQuery+x+".dat";
        File f = new File(outputName);
        boolean exists = f.exists();
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(outputName, true), "UTF-8"));
        if (!exists) {
            output.write("# "+outputName);
            output.newLine();
            output.write("# k\t\"Answer Size\"\t\"Wrapper Time (secs)\"\t\"Graph Creation Time (secs)\"\t\"Execution Time (secs)\"\t\"Total Execution Time (secs)\"\t\"Answer Percentage\"\t\"Throughput\"");
            output.newLine();
        }
        String s = numQuery+"\t";
        int size = Integer.parseInt(answersSize.get(numQuery));
        if (times.length == 0) {
            //String t = "0\t0\t0\t"+((int)(0/size));
            //output.write(s);
            //output.newLine();
        } else {
            int pa[] = getAnswerPercentage(dirPath+"/answerSize", times.length, size);
            int na[] = getAnswerNumber(dirPath+"/answerSize", times.length);
            for (int k = 0; k < times.length; k++) {
                String t = (k+1)+"\t"+na[k]+"\t";
                for (int j = 0; j < 4; j++) {
                    t = t + times[k][j] + "\t";
                }
                double throughput = ((double) na[k]) / times[k][3];
                t = t + pa[k] + "\t" + throughput;
                output.write(t);
                output.newLine();
            }
        }
        output.flush();
        output.close();
    }
}
