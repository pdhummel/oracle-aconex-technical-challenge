package pdh.interview.aconex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class CustomerProjectReport implements Reportable {
    public static final String CUSTOMER_COUNT_FOR_CONTRACT = "The number of unique customerIds for each contractId:";
    public static final String CUSTOMER_COUNT_FOR_GEO_ZONE = "The number of unique customerIds for each geozone:";
    public static final String BUILD_DURATION_FOR_GEO_ZONE = "The average buildduration for each geozone:";
    public static final String CUSTOMERS_FOR_GEO_ZONE = "The list of unique customerIds for each geozone:";

    private String inputFilePath;
    private List<String> reportData  = new ArrayList<>();
    private CustomerProjectInputHandler inputHandler = new CustomerProjectInputHandler();

    public CustomerProjectReport() {
    }

    public CustomerProjectReport(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    public List<String> getReportData() {
        return this.reportData;
    }

    /**
     * Main entry point for generating the customer project report.
     * @return
     * @throws Exception
     */
    public void generateReport() throws Exception {
        if (inputFilePath != null) {
            processFile(inputFilePath);
        } else {
            throw new Exception("Report not initiated with input data.");
        }
    }

    /**
     * Given a file with lines of customer project data,
     * this function generates the following for our report:
        * The number of unique customerIds for each contractId 
          * countOfUniqueCustomersForContract - Map<Integer, Long>
        * The number of unique customerIds for each geozone 
          * countOfUniqueCustomersForGeoZone - Map<String, Long>
        * The average buildduration for each geozone 
          * avgBuildDurationForGeoZone - Map<String, Double>
        * The list of unique customerIds for each geozone
          * uniqueCustomersForGeoZone - Map<String, List<Long>>
     * @param fileName: path of file with comma separated values
        * customerId,contractId,geozone,teamcode,projectcode,buildduration
        * Ex: 2343225,2345,us_east,RedTeam,ProjectApple,3445s
     * @return List<String> reportData
     * @throws IOException
     */
    protected List<String>  processFile(String fileName) throws IOException {
        try (Stream<String> lines = 
            (Files.newBufferedReader(Paths.get(fileName)).lines())) {
            this.processStream(lines);
        }
        return this.reportData;
    }

    /**
     * Given a stream of customer project data lines,
     * this function creates a map to hold the report output.
     * @param lines Stream<String>: comma separated values
        * customerId,contractId,geozone,teamcode,projectcode,buildduration
        * Ex: 2343225,2345,us_east,RedTeam,ProjectApple,3445s
     * @return List<String> reportData
     */
    protected List<String> processStream(Stream<String> lines) {
        Pattern pattern = Pattern.compile(",");
        Stream<CustomerProject> customerProjectStream = lines.map(line -> {
            CustomerProject customerProject = null;
            try {
                customerProject = inputHandler.parseLineToObject(line, pattern);
            } catch(Exception eWarn) {
                // TODO: Emit a warning that a line has failed processing.
                // It might be interesting to keep track of how many fail and 
                // if some threshold is reached, then make it a full-on error.
                System.out.println("Error with " + line);
            }
            return customerProject;
        });

        // Creates the customerProjectsByContract and customerProjectsByGeoZone maps.
        // This is where the input data is materialized into memory -- assume that the file is not huge and 
        // that we have enough memory to handle things.
        // Otherwise we could keep things in a stream and reprocess the stream multiple times in order
        // to fulfill all the report requirements.
        Map<String, Map<String, List<CustomerProject>>> customerProjectsMap = 
            (Map<String,Map<String,List<CustomerProject>>>)customerProjectStream.filter(cp -> cp != null)
            .collect(Collectors.teeing(
                    Collectors.groupingBy(CustomerProject::getGeoZone, Collectors.toList()),
                    Collectors.groupingBy(cp -> cp.getContractId() + "", Collectors.toList()),
                    (cp1, cp2) -> {
                        Map<String, Map<String, List<CustomerProject>>> map = new HashMap<>();
                        map.put("byGeoZone", cp1);
                        map.put("byContract", cp2);
                        return map;
                    }
            ));
            Map<String, List<CustomerProject>> customerProjectsByContract = customerProjectsMap.get("byContract");
            Map<String, List<CustomerProject>> customerProjectsByGeoZone = customerProjectsMap.get("byGeoZone");

            this.countUniqueCustomersForContract(customerProjectsByContract);

            Map<String, List<Long>> uniqueCustomersForGeoZone = this.getUniqueCustomersForGeoZone(customerProjectsByGeoZone);

            this.countUniqueCustomersForGeoZone(uniqueCustomersForGeoZone);

            this.calculateAvgBuildDurationForGeoZone(customerProjectsByGeoZone);
        
        return this.reportData;
    }

    protected  Map<String, Integer>  countUniqueCustomersForContract(Map<String, List<CustomerProject>> customerProjectsByContract) {
        Map<String, Integer> countOfUniqueCustomersForContract = inputHandler.countUniqueCustomersForContract(customerProjectsByContract);
        reportData.add(CUSTOMER_COUNT_FOR_CONTRACT);
        reportData.add(countOfUniqueCustomersForContract.toString());
        return countOfUniqueCustomersForContract;
    }

    protected Map<String, List<Long>> getUniqueCustomersForGeoZone(Map<String, List<CustomerProject>> customerProjectsByGeoZone) {
        Map<String, List<Long>> uniqueCustomersForGeoZone = inputHandler.getUniqueCustomersForGeoZone(customerProjectsByGeoZone);
        reportData.add(CUSTOMERS_FOR_GEO_ZONE);
        reportData.add(uniqueCustomersForGeoZone.toString());
        return uniqueCustomersForGeoZone;
    }

    protected Map<String, Integer> countUniqueCustomersForGeoZone(Map<String, List<Long>> uniqueCustomersForGeoZone) {
        Map<String, Integer> countOfUniqueCustomersForGeoZone = inputHandler.countUniqueCustomersForGeoZone(uniqueCustomersForGeoZone);
        reportData.add(CUSTOMER_COUNT_FOR_GEO_ZONE);
        reportData.add(countOfUniqueCustomersForGeoZone.toString());
        return countOfUniqueCustomersForGeoZone;
    }
    
    protected Map<String, Double> calculateAvgBuildDurationForGeoZone(Map<String, List<CustomerProject>> customerProjectsByGeoZone) {
        Map<String, Double> avgBuildDurationForGeoZone = inputHandler.calculateAvgBuildDurationForGeoZone(customerProjectsByGeoZone);
        reportData.add(BUILD_DURATION_FOR_GEO_ZONE);
        reportData.add(avgBuildDurationForGeoZone.toString());
        return avgBuildDurationForGeoZone;
    }
    
}
