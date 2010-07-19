/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ala.spatial.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 *
 * @author ajay
 */
public class CoordinateTransformer {
    public static String transformToGoogleMercator(String imgfilepath) {
        Runtime runtime = Runtime.getRuntime();
        try {

            String gdal_path = TabulationSettings.gdal_apps_dir;
            //if (!gdal_path.endsWith("/")) gdal_path += "/";
            
            System.out.println("Got gdal_path: " + gdal_path);


            String base_command = gdal_path + "gdalwarp -s_srs EPSG:4326 -t_srs EPSG:900913 ";

            File oImg = new File(imgfilepath);


            String command = base_command + imgfilepath + " " + oImg.getParent() + File.separator + "t_" + oImg.getName();

            System.out.println("Exec'ing " + command);
            Process proc = runtime.exec(command);

            System.out.println("Setting up output stream readers");
            InputStreamReader isr = new InputStreamReader(proc.getInputStream());
            InputStreamReader eisr = new InputStreamReader(proc.getErrorStream());
            BufferedReader br = new BufferedReader(isr);
            BufferedReader ebr = new BufferedReader(eisr);
            String line;

            System.out.printf("Output of running %s is:", command);

            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }

            while ((line = ebr.readLine()) != null) {
                System.out.println(line);
            }

            int exitVal = proc.waitFor();

            // any error???
            //return exitVal;

            System.out.println(exitVal);

            // success == 0
            // if successful, then rename to the original filename
            if (exitVal == 0) {
                //oImg.delete();
                //File tImg = new File (oImg.getAbsolutePath() + "/t_" + oImg.getName());
                //tImg.renameTo(oImg);
                return oImg.getParent() + File.separator + "t_" + oImg.getName();
            }
        } catch (Exception e) {
            System.out.println("OOOOPPPSSS: " + e.toString());
            System.out.println("{success: false , responseText: 'Error occurred' + " + e.toString() + "}");
            e.printStackTrace(System.out);
        }

        return null;
    }

    public static void generateWorldFiles(String outputpath, String baseFilename) {
        try {

            if (!outputpath.endsWith(File.separator)) outputpath += File.separator;

            System.out.println("Generating world files for " + baseFilename + " under " + outputpath); 

            StringBuffer sbWorldFile = new StringBuffer();
            //  pixel X size
            // sbWorldFile.append(xRes).append("\n");
            sbWorldFile.append("0.16666666667").append("\n");
            // rotation about the Y axis (usually 0.0)
            sbWorldFile.append("0").append("\n");
            // rotation about the X axis (usually 0.0)
            sbWorldFile.append("0").append("\n");
            // negative pixel Y size
            // sbWorldFile.append(yRes).append("\n");
            sbWorldFile.append("-0.16666666667").append("\n");
            // X coordinate of upper left pixel center
            // sbWorldFile.append(xMin).append("\n");
            sbWorldFile.append("112.083333333335").append("\n");
            // Y coordinate of upper left pixel center
            // sbWorldFile.append(yMin).append("\n");
            sbWorldFile.append("-9.083333333335").append("\n");

            StringBuffer sbProjection = new StringBuffer();
            sbProjection.append("GEOGCS[\"WGS 84\", ").append("\n");
            sbProjection.append("    DATUM[\"WGS_1984\", ").append("\n");
            sbProjection.append("        SPHEROID[\"WGS 84\",6378137,298.257223563, ").append("\n");
            sbProjection.append("            AUTHORITY[\"EPSG\",\"7030\"]], ").append("\n");
            sbProjection.append("        AUTHORITY[\"EPSG\",\"6326\"]], ").append("\n");
            sbProjection.append("    PRIMEM[\"Greenwich\",0, ").append("\n");
            sbProjection.append("        AUTHORITY[\"EPSG\",\"8901\"]], ").append("\n");
            sbProjection.append("    UNIT[\"degree\",0.01745329251994328, ").append("\n");
            sbProjection.append("        AUTHORITY[\"EPSG\",\"9122\"]], ").append("\n");
            sbProjection.append("    AUTHORITY[\"EPSG\",\"4326\"]] ").append("\n");

            PrintWriter pgwout = new PrintWriter(new BufferedWriter(new FileWriter(outputpath + baseFilename + ".pgw")));
            pgwout.write(sbWorldFile.toString());
            pgwout.close();

            PrintWriter prjout = new PrintWriter(new BufferedWriter(new FileWriter(outputpath + baseFilename + ".prj")));
            prjout.write(sbProjection.toString());
            prjout.close();



        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

    }


}
