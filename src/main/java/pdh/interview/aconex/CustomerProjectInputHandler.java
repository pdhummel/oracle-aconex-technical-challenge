package pdh.interview.aconex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CustomerProjectInputHandler {

    /**
     * Parses a comma separated string into a CustomerProject object.
     * @param line
        * customerId,contractId,geozone,teamcode,projectcode,buildduration
        * Ex: 2343225,2345,us_east,RedTeam,ProjectApple,3445s
     * @param pattern "," delimeter pattern
     * @return CustomerProject
     */
    protected CustomerProject parseLineToObject(String line, Pattern pattern) {
        String[] fields = pattern.split(line);
        long customerId = Long.parseLong(fields[0].trim());
        int contractId = Integer.parseInt(fields[1].trim());
        String geoZone = fields[2];
        String teamCode = fields[3];
        String projectCode = fields[4];
        String buildDurationString = fields[5].trim();
        String suffix = buildDurationString.substring(buildDurationString.length()-1);
        String number = null;
        if ("0123456789".contains(suffix)) {
            number = buildDurationString.substring(0, buildDurationString.length());
        } else {
            number = buildDurationString.substring(0, buildDurationString.length()-1);
        }
        long buildDuration = Long.parseLong(number);
        CustomerProject customerProject = new CustomerProject(customerId, contractId,
            geoZone, teamCode, projectCode, buildDuration);
        return customerProject;
    }


    protected  Map<String, Integer>  countUniqueCustomersForContract(Map<String, List<CustomerProject>> customerProjectsByContract) {
        // countOfUniqueCustomersForContract - Map<String, Integer>
        Map<String, Map<Long, List<CustomerProject>>> customersByContractMap = customerProjectsByContract.entrySet() 
                .stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> 
                    entry.getValue().stream().collect(
                            Collectors.groupingBy(CustomerProject::getCustomerId))));
        Map<String, List<Long>> uniqueCustomersForContract = customersByContractMap.entrySet().stream().collect(
            Collectors.toMap(Map.Entry::getKey, entry -> (List<Long>)new ArrayList<Long>(entry.getValue().keySet()))
        );
        Map<String, Integer> countOfUniqueCustomersForContract = uniqueCustomersForContract.entrySet().stream().collect(
            Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size())
        );
        return countOfUniqueCustomersForContract;
    }

    protected Map<String, List<Long>> getUniqueCustomersForGeoZone(Map<String, List<CustomerProject>> customerProjectsByGeoZone) {
        // uniqueCustomersForGeoZone - Map<String, List<Long>>
        Map<String, Map<Long, List<CustomerProject>>> customersForGeoZoneMap = customerProjectsByGeoZone.entrySet() 
                .stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> 
                    entry.getValue().stream().collect(
                            Collectors.groupingBy(CustomerProject::getCustomerId))));
        Map<String, List<Long>> uniqueCustomersForGeoZone = customersForGeoZoneMap.entrySet().stream().collect(
            Collectors.toMap(Map.Entry::getKey, entry -> (List<Long>)new ArrayList<Long>(entry.getValue().keySet()))
        );
        return uniqueCustomersForGeoZone;
    }

    protected Map<String, Integer> countUniqueCustomersForGeoZone(Map<String, List<Long>> uniqueCustomersForGeoZone) {
        // countOfUniqueCustomersForGeoZone - Map<String, Integer>
        Map<String, Integer> countOfUniqueCustomersForGeoZone = uniqueCustomersForGeoZone.entrySet().stream().collect(
            Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size())
        );
        return countOfUniqueCustomersForGeoZone;
    }

    
    protected Map<String, Double> calculateAvgBuildDurationForGeoZone(Map<String, List<CustomerProject>> customerProjectsByGeoZone) {
        // avgBuildDurationForGeoZone - Map<String, Double>
        Map<String, Double> avgBuildDurationForGeoZone = customerProjectsByGeoZone.entrySet() 
                .stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> 
                    entry.getValue().stream().collect(
                        Collectors.averagingDouble(entry2->entry2.getBuildDuration()))));
        return avgBuildDurationForGeoZone;
    }

}
