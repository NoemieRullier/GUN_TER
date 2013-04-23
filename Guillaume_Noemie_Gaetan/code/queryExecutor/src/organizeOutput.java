import java.io.*;
import java.util.ArrayList;

// java organizeOutput $PATH_TO_FOLDERS $FOLDERS_BEGIN_WITH $FOLDER
// java organizeOutput /home/gabriela/gun2012/code/expfiles/berlinOutput/300views/RelViews outputRelViewsquery /home/gabriela/gun2012/code/expfiles/resultsBerlin/chainPaper/data/300views
public class organizeOutput {

    public static void main(String args[]) throws Exception {
        String fullDir = args[0];
        String dir = fullDir;
        int i = dir.lastIndexOf("/");
        dir = dir.substring(dir.lastIndexOf("/")+1);
        String foldersStarting = args[1];
        String outputFolder = args[2];
        File f = new File(fullDir);
        File[] content = f.listFiles();

        if (content != null) {

            for (File g : content) {
                //System.out.println("X");
                if (g.isDirectory() && g.getName().startsWith(foldersStarting)) {
                    //System.out.println("Y");
                    processFolder(g, outputFolder, dir);    
                }
            }
        }
    }

    public static void processFolder(File g, String outputFolder, String approach) throws Exception {

        String dirName = g.getAbsolutePath();
        //String timeFile = dirName+"/TimeTable";
        //System.out.println("timeFile: "+timeFile);
        //int[] timeN = readTimes(timeFile, "NOTHING");
        //System.out.println("times de nothing: "+timeN.length);
        File[] content = g.listFiles();
        String y = dirName.substring(dirName.indexOf("q"));
        y = y.substring(5);
        int i = y.indexOf("GUN");
        if (i > -1) {
            y = y.substring(0, i);
        }
        if (content != null) {
            for (File h : content) {
                //System.out.println("Z");
                if (h.isDirectory() && !h.isHidden()) {
                    try {
                        //System.out.println("W");
                        processFolder2(h, y, outputFolder, approach);
                    } catch (Exception e) {
                        System.err.println("Problems with "+g.getAbsolutePath());
                    }
                }
            }
        }
    }

    public static void copyFile(String fileNameS, String fileNameD) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(fileNameS));
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                               new FileOutputStream(fileNameD, true), "UTF-8"));
        System.out.println("reading from "+fileNameS);
        System.out.println("writing to "+fileNameD);
        String l = br.readLine();
        while (l != null) {
            if (!l.startsWith("#")) {
                int i = l.indexOf("\t");
                int j = l.lastIndexOf("\t");
                int time = Integer.parseInt(l.substring(0, i));
                int answers = Integer.parseInt(l.substring(i+1, j));
                int numberViews = Integer.parseInt(l.substring(j+1));
                l = (time/1000) + "\t" + answers + "\t" + numberViews;
            }
            output.write(l);
            output.newLine();
            l = br.readLine();
        }
        output.flush();
        output.close();
        br.close();
    }

    public static void processFolder2(File g, String y, String outputFolder, String approach) throws Exception {

        String dirName = g.getName();
        String dirPath = g.getAbsolutePath();
        ArrayList<int[]> data = new ArrayList<int[]>();
        String fileName = "throughput_Q"+y+"_"+approach;
        copyFile(dirPath+"/throughput", outputFolder+"/"+fileName);

    }
}
