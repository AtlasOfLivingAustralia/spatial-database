package org.ala.spatial.analysis.service;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import org.ala.spatial.analysis.index.BoundingBoxes;

import org.ala.spatial.analysis.index.IndexedRecord;
import org.ala.spatial.analysis.index.OccurrencesCollection;
import org.ala.spatial.analysis.index.OccurrencesFilter;
import org.ala.spatial.util.AnalysisJobSampling;
import org.ala.spatial.util.Layers;
import org.ala.spatial.util.OccurrencesFieldsUtil;
import org.ala.spatial.util.SimpleRegion;
import org.ala.spatial.util.SpatialLogger;
import org.ala.spatial.util.TabulationSettings;

/**
 * service for returning occurrences + optional values from layer intersections
 *
 * @author adam
 *
 */
public class SamplingService {

    public static SamplingService newForLSID(String lsid) {
        if (SamplingLoadedPointsService.isLoadedPointsLSID(lsid)) {
            return new SamplingLoadedPointsService();
        } else {
            return new SamplingService();
        }
    }

    /**
     * constructor init
     */
    SamplingService() {
        TabulationSettings.load();
    }

    /**
     * gets samples; occurrences records + optional intersecting layer values,
     *
     *
     * limit output
     *
     * @param filter species name as String
     * @param layers list of layer names of additional data to include as String []
     * @param region region to restrict results as SimpleRegion
     * @param records sorted pool of records to intersect with as ArrayList<Integer>
     * @param max_rows upper limit of records to return as int
     * @return samples as grid, String [][]
     */
    public String sampleSpeciesAsCSV(String filter, String[] layers, SimpleRegion region, int [] records, int max_rows) {
        return sampleSpeciesAsCSV(filter, layers, region, records, max_rows, null);
    }

    /**
     * gets samples; occurrences records + optional intersecting layer values,
     *
     *
     * limit output
     *
     * @param filter species name as String
     * @param layers list of layer names of additional data to include as String []
     * @param region region to restrict results as SimpleRegion
     * @param records sorted pool of records to intersect with as ArrayList<Integer>
     * @param max_rows upper limit of records to return as int
     * @return samples as grid, String [][]
     */
    public String[][] sampleSpecies(String filter, String[] layers, SimpleRegion region, int[] records, int max_rows) {
        return sampleSpecies(filter, layers, region, records, max_rows, null);
    }

    public String getHeader(String[] layers) {
        StringBuffer header = new StringBuffer();
        OccurrencesFieldsUtil ofu = new OccurrencesFieldsUtil();
        for (String s : ofu.getOutputColumnNames()) {
            header.append(s).append(",");
        }
        if (layers != null) {
            for (String l : layers) {
                header.append(Layers.layerNameToDisplayName(l)).append(",");
            }
        }
        header.deleteCharAt(header.length() - 1); //take off end ','

        return header.toString();
    }

    public String[][] sampleSpecies(String filter, String[] layers, SimpleRegion region, int[] records, int max_rows, AnalysisJobSampling job) {
        ArrayList<String> as = OccurrencesCollection.getFullRecords(new OccurrencesFilter(filter, region, records, layers, max_rows));

        //split records and append header
        if(as.size() > 0) {
            String [] header = getHeader(layers).split(",");

            int numCols = header.length;

            String [][] output = new String[as.size() + 1][numCols];

            //header
            for(int j=0;j<header.length && j < numCols;j++){
                output[0][j] = header[j];
            }

            //records
            for(int i = 0;i< as.size();i++){
                String [] s = as.get(i).split(",");
                for(int j=0;j<s.length && j < numCols;j++){
                    output[i+1][j] = s[j];
                }
            }

            return output;
        }

        return null;

        

        /*
        String[][] results = null;

        StringBuffer output = new StringBuffer();
        int number_of_columns = TabulationSettings.occurrences_csv_fields.length;
        OccurrencesFieldsUtil ofu = new OccurrencesFieldsUtil();
        for (String s : ofu.getOutputColumnNames()) {
            output.append(s);
            output.append(",");
        }
        if (layers != null) {
            for (String l : layers) {
                output.append(Layers.layerNameToDisplayName(l));
                output.append(",");
            }
            number_of_columns += layers.length;
        }
        // tidy up header
        output.deleteCharAt(output.length() - 1); //take off end ','
        //output.append("\r\n");

        IndexedRecord[] ir = null;// OccurrencesCollection.filterSpeciesRecords(new OccurrencesFilter(filter);

        if (ir != null && ir.length > 0 && max_rows <= 100) {
            return sampleSpeciesSmall(filter, layers, region, records, max_rows, job);
        }

        int i, j;

        int recordsPos = 0; //for records intersection counter

        ArrayList<String[]> columns = new ArrayList<String[]>();

        if (job != null) {
            job.setProgress(0.1);
        }

        try {
            //for (IndexedRecord r : ir) {
            if (ir != null) { //only expect one result back from oi.filterspr(f);
                IndexedRecord r = ir[0];

                columns.clear();

                //cap the number of records per read

                int step = 1000000; //max characters to read TODO: move to tabulation settings.xml
                long rstart = r.file_start;
                long rend;

                rend = rstart + step;
                if (rend > r.file_end) {
                    rend = r.file_end;
                }

                String[] sortedrecords;
                int recordstart = r.record_start;
                int recordend;

                String lastpart = "";

                //repeat until 20, or so records retrieved
                int rowoffset = 0;
                double rowCount = r.record_end - r.record_start + 1;
                results = null;
                while (rowoffset <= max_rows && rend <= r.file_end) {
                    if (job != null) {
                        job.setProgress(rowoffset / rowCount);
                    }

                    columns.clear();

                    sortedrecords = null;//OccurrencesIndex.getSortedRecords(rstart, rend);

                    if (lastpart.split(",").length == TabulationSettings.occurrences_csv_fields.length
                            && sortedrecords[0].split(",").length == TabulationSettings.occurrences_csv_fields.length) {
                        sortedrecords[0] = lastpart + "\n" + sortedrecords[0];
                    } else {
                        sortedrecords[0] = lastpart + sortedrecords[0];
                    }

                    columns.add(sortedrecords);

                    if (rend == r.file_end) {
                        //do all records
                        recordend = r.record_end;
                        lastpart = "";
                    } else {
                        //do up to last record
                        lastpart = "";
                        recordend = recordstart + sortedrecords.length - 2; //inclusive
                        lastpart = sortedrecords[sortedrecords.length - 1];
                    }

                    if (layers != null) {
                        for (i = 0; i < layers.length; i++) {
                            //columns.add(SamplingIndex.getRecords(
                            //        Layers.layerDisplayNameToName(layers[i]),
                            //        recordstart,
                            //        recordend));
                        }
                    }

                    // format for csv
                    int len;
                    if (columns.size() > 1) {
                        len = columns.get(1).length;
                    } else {
                        len = recordend - recordstart + 1;
                    }

                    String[] row;

                    // output structure
                    if (results == null) {
                        //limited to max_rows output rows by rowoffset in add loop
                        //+1 for header
                        results = new String[max_rows + 1][number_of_columns + 1];

                        //populate header
                        row = output.toString().split(",");
                        for (j = 0; j < row.length; j++) {
                            results[0][j] = row[j];
                        }
                        rowoffset++;
                    }

                    int coloffset = 0;

                    double[] points = null;// OccurrencesIndex.getPoints(recordstart, recordend);

                    for (j = 0; rowoffset <= max_rows && j < len; j++) {
                        //do not add if does not intersect with records list
                        if (records != null) {
                            int currentRecord = j + recordstart;
                            //increment recordsPos as required
                            while (recordsPos < records.size()
                                    && records.get(recordsPos).intValue() < currentRecord) {
                                recordsPos++;
                            }
                            //test for intersect
                            if (recordsPos >= records.size()
                                    || currentRecord != records.get(recordsPos).intValue()) {
                                continue;
                            }
                        }

                        coloffset = 0;
                        //test bounding box
                        if (region == null || region.isWithin(points[j * 2], points[j * 2 + 1])) {
                            for (i = 0; i < columns.size(); i++) {
                                if (columns.get(i) != null && j < columns.get(i).length) {
                                    if (i == 0) {
                                        row = columns.get(i)[j].split(",");

                                        for (int k = 0; k < row.length && k < results[rowoffset].length; k++) {
                                            results[rowoffset][k] = row[k];
                                        }
                                        coloffset = row.length - 1;
                                    } else if (!(columns.get(i)[j] == null) && !columns.get(i)[j].equals("NaN")) {
                                        results[rowoffset][coloffset] = columns.get(i)[j];
                                    } else {
                                        results[rowoffset][coloffset] = "missing";
                                    }
                                    coloffset++;
                                }
                            }
                            rowoffset++;
                        }
                    }

                    // adjust for next loop
                    recordstart = recordend + 1; 		//this was inclusive
                    if (rend < r.file_end) {
                        rstart = rend;				//this is not inclusive
                        rend = rstart + step;
                        if (rend > r.file_end) {
                            rend = r.file_end;
                        }
                    } else {
                        rend = r.file_end + 1;
                    }
                }

                //trim back results
                String[][] results_trim = new String[rowoffset][results[0].length];
                for (i = 0; i < results_trim.length; i++) {
                    for (j = 0; j < results_trim[i].length; j++) {
                        results_trim[i][j] = results[i][j];
                    }
                }
                return results_trim;
            }



            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;*/
    }

    /**
     * gets array of points for species (genus, etc) name matches within
     * a specified region
     *
     * @param filter species (genus, etc) name
     * @param region region to filter results by
     * @param records sorted pool of records to intersect with as int []
     * @return points as double[], first is longitude, every second is latitude.
     */
    public double[] sampleSpeciesPoints(String filter, SimpleRegion region, int [] records) {
        
        //test on bounding box
        double[] bb = BoundingBoxes.getLsidBoundingBoxDouble(filter);
        double[][] regionbb = region.getBoundingBox();
        if (bb[0] <= regionbb[1][0] && bb[2] >= regionbb[0][0]
                && bb[1] <= regionbb[1][1] && bb[3] >= regionbb[0][1]) {

           return OccurrencesCollection.getPoints(new OccurrencesFilter(filter, region, records, TabulationSettings.MAX_RECORD_COUNT_CLUSTER));
        }

        return null;
    }

    /**
     * gets array of points for species (genus, etc) name matches within
     * a specified region
     *
     * can return other field or sampling for points returned
     *
     * @param filter species (genus, etc) name
     * @param region region to filter results by
     * @param records sorted pool of records to intersect with as ArrayList<Integer>
     * @return points as double[], first is longitude, every second is latitude.
     */
    public double[] sampleSpeciesPoints(String filter, SimpleRegion region, int [] records, ArrayList<Object> extra) {
            //test on bounding box
            double[] bb = BoundingBoxes.getLsidBoundingBoxDouble(filter);
            double[][] regionbb = region.getBoundingBox();
            if (bb[0] <= regionbb[1][0] && bb[2] >= regionbb[0][0]
                    && bb[1] <= regionbb[1][1] && bb[3] >= regionbb[0][1]) {

                /* get points */
                return OccurrencesCollection.getPoints(new OccurrencesFilter(filter, region, records, TabulationSettings.MAX_RECORD_COUNT_CLUSTER), extra);

                /* test for region absence
                if (region == null) {
                    return points;
                }

                //TODO: nicer 'get'
                //TODO: caching on 'extra' data
                int[] field = null;
                double[] field_output = null;
                if (extra != null) {
                    for (int i = 0; i < extra.size(); i += 2) {
                        String s = (String) extra.get(i);
                        if (s.equalsIgnoreCase("u")) {//uncertainty
                            field = new int[1];
                            field[0] = 1;   //index in OccurrencesIndex.cluster_records
                            field_output = new double[points.length / 2];
                        }
                    }
                }

                int i;
                int count = 0;

                int recordsPos = 0; //for test on records

                // return all valid points within the region
                double[] output = new double[points.length];
                for (i = 0; i < points.length; i += 2) {
                    //do not add if does not intersect with records list
                    if (records != null) {
                        int currentRecord = i + ir[0].record_start;
                        //increment recordsPos as required
                        while (recordsPos < records.size()
                                && records.get(recordsPos).intValue() < currentRecord) {
                            recordsPos++;
                        }
                        //test for intersect
                        if (recordsPos >= records.size()
                                || currentRecord != records.get(recordsPos).intValue()) {
                            continue;
                        }
                    }
                    //region test
                    if (region.isWithin(points[i], points[i + 1])) {
                        if (field != null) {
                            try {
                                field_output[count / 2] = 0;//Double.parseDouble(OccurrencesIndex.cluster_records[i / 2 + ir[0].record_start][field[0]]);
                            } catch (Exception e) {
                                field_output[count / 2] = 10000; //default 10km
                            }
                        }

                        output[count] = points[i];
                        count++;
                        output[count] = points[i + 1];
                        count++;
                    }
                }

                if (count > 0) {
                    if (field != null) {
                        extra.set(1, java.util.Arrays.copyOf(field_output, count / 2));
                    }

                    return java.util.Arrays.copyOf(output, count);
                } */
            }

        return null;
    }

    /**
     * for Sensitive Coordinates
     *
     * gets array of points for species (genus, etc) name matches within
     * a specified region
     *
     * @param filter species (genus, etc) name
     * @param region region to filter results by
     * @param records sorted pool of records to intersect with as ArrayList<Integer>
     * @return points as double[], first is longitude, every second is latitude.
     */
    public double[] sampleSpeciesPointsSensitive(String filter, SimpleRegion region, int [] records) {
        IndexedRecord[] ir = null;// OccurrencesIndex.filterSpeciesRecords(filter);

        if (ir != null && ir.length > 0) {

            /* get points */
            double[] points = null;//OccurrencesIndex.getPointsSensitive(ir[0].record_start, ir[0].record_end);

            /* test for region absence */
            if (region == null) {
                return points;
            }

            int i;
            int count = 0;

            int recordsPos = 0; //for test on records

            /* return all valid points within the region */
            for (i = 0; i < points.length; i += 2) {
                //do not add if does not intersect with records list
                if (records != null) {
                    int currentRecord = i + ir[0].record_start;
                    //increment recordsPos as required
                    while (recordsPos < records.length
                            && records[recordsPos] < currentRecord) {
                        recordsPos++;
                    }
                    //test for intersect
                    if (recordsPos >= records.length
                            || currentRecord != records[recordsPos]) {
                        continue;
                    }
                }
                //region test
                if (region.isWithin(points[i], points[i + 1])) {
                    count += 2;
                } else {
                    points[i] = Double.NaN;
                }
            }
            //move into 'output'
            if (count > 0) {
                double[] output = new double[count];
                int p = 0;
                for (i = 0; i < points.length; i += 2) {
                    if (!Double.isNaN(points[i])) {
                        output[p++] = points[i];
                        output[p++] = points[i + 1];
                    }
                }
                return output;
            }

        }

        return null;
    }

    /**
     * for Sensitive Coordinates
     *
     * gets array of points for species (genus, etc) name matches within
     * a specified region
     *
     * removes points for all species that are sensitive
     *
     * @param filter species (genus, etc) name
     * @param region region to filter results by
     * @param records sorted pool of records to intersect with as ArrayList<Integer>
     * @return points as double[], first is longitude, every second is latitude.
     */
    public double[] sampleSpeciesPointsMinusSensitiveSpecies(String filter, SimpleRegion region, int [] records, StringBuffer removedSpecies) {
        /* get points */
        return OccurrencesCollection.getPointsMinusSensitiveSpecies(new OccurrencesFilter(filter, region, records, TabulationSettings.MAX_RECORD_COUNT_CLUSTER), removedSpecies);
    }

    /**
     * for Sensitive Records
     *
     * Checks if the records are sensitive within filter range
     *
     * @param filter species (genus, etc) name
     * @param region region to filter results by
     * @param records sorted pool of records to intersect with as ArrayList<Integer>
     * @return int 
     *      0 when non-sensitive and has records,
     *      1 when sensitive or no records,
     *      -1 when cannot be determined
     */
    public static int isSensitiveRecord(String filter, SimpleRegion region, int [] records) {
        StringBuffer sb = new StringBuffer();
        try {
            double [] d = OccurrencesCollection.getPointsMinusSensitiveSpecies(new OccurrencesFilter(filter, region, records, TabulationSettings.MAX_RECORD_COUNT_CLUSTER), sb);
            if(d == null) {
                return 1;
            } else {
                return 0;
        }
        }catch(Exception e){
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * gets samples; occurrences records + optional intersecting layer values,
     *
     *
     * limit output
     *
     * @param filter species name as String
     * @param layers list of layer names of additional data to include as String []
     * @param region region to restrict results as SimpleRegion
     * @param records sorted pool of records to intersect with as ArrayList<Integer>
     * @param max_rows upper limit of records to return as int
     * @return samples as grid, String [][]
     */
    public String sampleSpeciesAsCSV(String species, String[] layers, SimpleRegion region, int [] records, int max_rows, AnalysisJobSampling job) {
        try {

            System.out.println("Limiting sampling to : " + max_rows);

            String[][] results = sampleSpecies(species, layers, region, records, max_rows);
            StringBuilder sbResults = new StringBuilder();

            for (int i = 0; i < results.length; i++) {
                for (int j = 0; j < results[i].length; j++) {
                    if (results[i][j] != null) {
                        sbResults.append(results[i][j]);
                    }
                    if (j < results[i].length - 1) {
                        sbResults.append(",");
                    }
                }
                sbResults.append("\r\n");
            }

            /* open output file */
            File temporary_file = java.io.File.createTempFile("sample", ".csv");
            FileWriter fw = new FileWriter(temporary_file);

            fw.append(sbResults.toString());
            fw.close();
            return temporary_file.getPath();


        } catch (Exception e) {
            System.out.println("error with samplesSpeciesAsCSV:");
            e.printStackTrace(System.out);
        }

        return "";
    }

    public static String getLSIDAsGeoJSON(String lsid, File outputpath) {
        if (SamplingLoadedPointsService.isLoadedPointsLSID(lsid)) {
            return getLSIDAsGeoJSON(lsid, outputpath);
        }

        int i;

        /* get samples records from records indexes */
        String[][] samples = (new SamplingService()).sampleSpecies(lsid, null, null, null, TabulationSettings.MAX_RECORD_COUNT_DOWNLOAD, null);

        StringBuffer sbGeoJSON = new StringBuffer();
        sbGeoJSON.append("{");
        sbGeoJSON.append("  \"type\": \"FeatureCollection\",");
        sbGeoJSON.append("  \"features\": [");
        for (i = 1; i < samples.length; i++) {
            String s = getRecordAsGeoJSON(samples, i);
            if (s != null) {
                sbGeoJSON.append(s);
                if (i < samples.length - 1) {
                    sbGeoJSON.append(",");
                }
            }
        }
        sbGeoJSON.append("  ],");
        sbGeoJSON.append("  \"crs\": {");
        sbGeoJSON.append("    \"type\": \"EPSG\",");
        sbGeoJSON.append("    \"properties\": {");
        sbGeoJSON.append("      \"code\": \"4326\"");
        sbGeoJSON.append("    }");
        sbGeoJSON.append("  }");
        //sbGeoJSON.append(",  \"bbox\": [");
        //sbGeoJSON.append("    ").append(bbox[0][0]).append(",").append(bbox[0][1]).append(",").append(bbox[1][0]).append(",").append(bbox[1][1]);
        //sbGeoJSON.append("  ]");
        sbGeoJSON.append("}");

        /* write samples to a file */
        try {
            File temporary_file = java.io.File.createTempFile("filter_sample", ".csv", outputpath);
            FileWriter fw = new FileWriter(temporary_file);

            fw.write(sbGeoJSON.toString());

            fw.close();

            return temporary_file.getName();	//return location of temp file

        } catch (Exception e) {
            SpatialLogger.log("SamplingService: getLSIDAsGeoJSON()", e.toString());
            e.printStackTrace();
        }
        return "";

    }

    /**
     * creates a file with geojson for lsid at outputpath
     *
     * returns filename (first line), number of parts (2nd line)
     *
     * @param lsid
     * @param outputpath
     * @return
     */
    public static String getLSIDAsGeoJSONIntoParts(String lsid, File outputpath) {
        if (SamplingLoadedPointsService.isLoadedPointsLSID(lsid)) {
            return getLSIDAsGeoJSONIntoParts(lsid, outputpath);
        }

        int i;

        /* get samples records from records indexes */
        String[][] samples = (new SamplingService()).sampleSpecies(lsid, null, null, null, TabulationSettings.MAX_RECORD_COUNT_DOWNLOAD, null);

        int max_parts_size = 2000;

        int count = 0;

        //-1 on samples.length for header
        int partCount = (int) Math.ceil((samples.length - 1) / (double) max_parts_size);

        //test for filename, return if it exists
        File file;
        String filename = outputpath + File.separator + lsid.replace(":", "_").replace(".", "_");
        try {
            file = new File(filename + "_" + (partCount - 1));
            if (file.exists()) {
                return lsid.replace(":", "_").replace(".", "_") + "\n" + partCount;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int j = 1; j < samples.length; j += max_parts_size) {

            StringBuffer sbGeoJSON = new StringBuffer();
            sbGeoJSON.append("{");
            sbGeoJSON.append("\"type\": \"FeatureCollection\",");
            sbGeoJSON.append("\"features\": [");
            int len = j + max_parts_size;
            if (len > samples.length) {
                len = samples.length;
            }
            for (i = j; i < len; i++) {
                String s = getRecordAsGeoJSON(samples, i);
                if (s != null) {
                    sbGeoJSON.append(s);
                    if (i < len - 1) {
                        sbGeoJSON.append(",");
                    }
                }
            }
            sbGeoJSON.append("],");
            sbGeoJSON.append("\"crs\": {");
            sbGeoJSON.append("\"type\": \"EPSG\",");
            sbGeoJSON.append("\"properties\": {");
            sbGeoJSON.append("\"code\": \"4326\"");
            sbGeoJSON.append("}");
            sbGeoJSON.append("}");
            sbGeoJSON.append("}");

            /* write samples to a file */
            try {
                //File temporary_file = java.io.File.createTempFile("filter_sample", ".csv", outputpath);
                FileWriter fw = new FileWriter(
                        filename + "_" + count);
                count++;

                fw.write(sbGeoJSON.toString());

                fw.close();

                //return temporary_file.getName();	//return location of temp file

            } catch (Exception e) {
                SpatialLogger.log("SamplingService: getLSIDAsGeoJSON()", e.toString());
                e.printStackTrace();
            }
        }
        return lsid.replace(":", "_").replace(".", "_") + "\n" + partCount;

    }

    private static String getRecordAsGeoJSON(String[][] rec, int rw) {
        //String[] recdata = rec.split(",");

        if (rec == null || rec.length <= rw || rec[rw].length <= TabulationSettings.geojson_latitude) {
            return null;
        }

        for (int i = 0; i < TabulationSettings.geojson_latitude; i++) {
            if (rec[rw][i] == null) {
                return null;
            }
        }

        StringBuffer sbRec = new StringBuffer();
        sbRec.append("{");
        sbRec.append("  \"type\":\"Feature\",");
        sbRec.append("  \"id\":\"occurrences.data.").append(rec[rw][TabulationSettings.geojson_id]).append("\",");
        sbRec.append("  \"geometry\":{");
        sbRec.append("      \"type\":\"Point\",");
        sbRec.append("      \"coordinates\":[\"").append(rec[rw][TabulationSettings.geojson_longitude]).append("\",\"").append(rec[rw][TabulationSettings.geojson_latitude].trim()).append("\"]");
        sbRec.append("   },");
        sbRec.append("  \"geometry_name\":\"the_geom\",");
        sbRec.append("  \"properties\":{");
        for (int i = 0; i < TabulationSettings.geojson_property_names.length; i++) {
            sbRec.append("      \"").append(TabulationSettings.geojson_property_names[i]).append("\":\"").append(rec[rw][TabulationSettings.geojson_property_fields[i]]).append("\"");
            if (i < TabulationSettings.geojson_property_names.length - 1) {
                sbRec.append(",");
            }
        }
        sbRec.append("  }");
        sbRec.append("}");

        return sbRec.toString();

    }

    private String[][] sampleSpeciesSmall(String filter, String[] layers, SimpleRegion region, ArrayList<Integer> records, int max_rows, AnalysisJobSampling job) {
        IndexedRecord[] ir = null;// OccurrencesIndex.filterSpeciesRecords(filter);

        if (ir != null && ir.length > 0) {
            if (records != null) {
                java.util.Collections.sort(records);
            }

            /* get points */
            double[] points = null;//OccurrencesIndex.getPoints(ir[0].record_start, ir[0].record_end);

            /* test for region absence */
            int i;

            int alen = 0;
            int[] a = new int[max_rows];

            int recordsPos = 0; //for test on records

            /* return all valid points within the region */
            for (i = 0; i < points.length && alen < max_rows; i += 2) {
                int currentRecord = (i / 2) + ir[0].record_start;

                //do not add if does not intersect with records list
                if (records != null) {
                    //increment recordsPos as required
                    while (recordsPos < records.size()
                            && records.get(recordsPos).intValue() < currentRecord) {
                        recordsPos++;
                    }
                    //test for intersect
                    if (recordsPos >= records.size()
                            || currentRecord != records.get(recordsPos).intValue()) {
                        continue;
                    }
                }
                //region test
                if (region == null || region.isWithin(points[i], points[i + 1])) {
                    a[alen++] = currentRecord;
                }
            }

            if (alen == 0) {
                return null;
            }

            //filled a up to alen, get the data
            if (alen < max_rows) {
                a = java.util.Arrays.copyOf(a, alen);
            }
            String[] oi = null;;//OccurrencesIndex.getSortedRecords(a);

            int layerscount = (layers == null) ? 0 : layers.length;
            int headercount = oi[0].split(",").length;
            String[][] output = new String[oi.length + 1][headercount + layerscount];//+1 for header

            //fill
            for (i = 0; i < oi.length; i++) {
                String[] line = oi[i].split(",");
                for (int j = 0; j < line.length && j < headercount; j++) {
                    output[i + 1][j] = line[j];   //+1 for header
                }
            }

            for (i = 0; layers != null && i < layers.length; i++) {
                String[] si = null;//SamplingIndex.getRecords(layers[i], a);
                if (si != null) {
                    for (int j = 0; j < si.length && j < output.length; j++) {
                        output[j][headercount + i] = si[j];
                    }
                }
            }

            //header
            OccurrencesFieldsUtil ofu = new OccurrencesFieldsUtil();
            i = 0;
            for (String s : ofu.getOutputColumnNames()) {
                output[0][i++] = s.trim();
            }
            if (layers != null) {
                for (String l : layers) {
                    output[0][i++] = Layers.layerNameToDisplayName(l).trim();
                }
            }

            return output;
        }

        return null;
    }
}
