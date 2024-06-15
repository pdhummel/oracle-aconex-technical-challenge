package pdh.interview.aconex;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;

public class CustomerProjectReportTest {
    @Test
    public void testParseLineToObject() throws Exception {
        Pattern pattern = Pattern.compile(",");
        String line = "2343225,2345,us_east,RedTeam,ProjectApple,3445s";
        CustomerProjectInputHandler handler = new CustomerProjectInputHandler();
        CustomerProject customerProject = handler.parseLineToObject(line, pattern);
        assertEquals(2343225, customerProject.getCustomerId());
        assertEquals(2345, customerProject.getContractId());
        assertEquals("us_east", customerProject.getGeoZone());
        assertEquals("RedTeam", customerProject.getTeamCode());
        assertEquals("ProjectApple", customerProject.getProjectCode());
        assertEquals(3445, customerProject.getBuildDuration());

        // These should blow up
        try {
            line = "";
            customerProject = handler.parseLineToObject(line, pattern);
            throw new Exception("Empty line should not parse w/NumberFormatException.");
        } catch(NumberFormatException eIgnore) {}
        try {
            line = "2343225,2345,us_east,RedTeam,ProjectApple,x3445";
            customerProject = handler.parseLineToObject(line, pattern);
            throw new Exception("Empty line should not parse w/NumberFormatException.");
        } catch(NumberFormatException eIgnore) {}


        // This should work once we get proper suffix handling.
        line = "2343225,2345,us_east,RedTeam,ProjectApple,3445";        
        customerProject = handler.parseLineToObject(line, pattern);
        assertEquals(3445, customerProject.getBuildDuration());
        line = "2343225,2345,us_east,RedTeam,ProjectApple,3445 ";
        assertEquals(3445, customerProject.getBuildDuration());
        line = "2343225,2345,us_east,RedTeam,ProjectApple,3445d";
        assertEquals(3445, customerProject.getBuildDuration());
        customerProject = handler.parseLineToObject(line, pattern);
    }


    @Test
    public void testProcessStream() {
        List<String> inputData = new ArrayList<>();
        inputData.add("2343225,2345,us_east,RedTeam,ProjectApple,3445s");
        inputData.add("1223456,2345,us_west,BlueTeam,ProjectBanana,2211s");
        inputData.add("3244332,2346,eu_west,YellowTeam3,ProjectCarrot,4322s");
        inputData.add("1233456,2345,us_west,BlueTeam,ProjectDate,2221s");
        inputData.add("3244132,2346,eu_west,YellowTeam3,ProjectEgg,4122s");
        CustomerProjectReport report = new CustomerProjectReport();
        report.processStream(inputData.stream());
        List<String> reportData = report.getReportData();
        assertEquals("{2346=2, 2345=3}", reportData.get(1));
        assertEquals("{eu_west=[3244132, 3244332], us_west=[1233456, 1223456], us_east=[2343225]}", reportData.get(3));
        assertEquals("{eu_west=2, us_west=2, us_east=1}", reportData.get(5));
        assertEquals("{eu_west=4222.0, us_west=2216.0, us_east=3445.0}", reportData.get(7));

        // If there is a bad input line, we should skip it and continue processing the other lines.
        report = new CustomerProjectReport();
        inputData.add("X3244332,X2346,eu_west,YellowTeam3,ProjectCarrot,100000s");
        inputData.add("3244332,2346,eu_west,YellowTeam3,ProjectPurple,0s");
        report.processStream(inputData.stream());
        reportData = report.getReportData();
        assertEquals("{eu_west=2814.6666666666665, us_west=2216.0, us_east=3445.0}", reportData.get(7));
    }

    @Test
    public void testProcessFile() throws Exception {
        CustomerProjectReport report = new CustomerProjectReport();
        String reportFile = "./src/test/resources/input.csv";
        report.processFile(reportFile);
        List<String> reportData = report.getReportData();
        assertEquals("{2346=2, 2345=3}", reportData.get(1));
        assertEquals("{eu_west=[3244132, 3244332], us_west=[1233456, 1223456], us_east=[2343225]}", reportData.get(3));
        assertEquals("{eu_west=2, us_west=2, us_east=1}", reportData.get(5));
        assertEquals("{eu_west=4222.0, us_west=2216.0, us_east=3445.0}", reportData.get(7));

        // one bad record is skipped
        report = new CustomerProjectReport();
        reportFile = "./src/test/resources/onebad-input.csv";
        report.processFile(reportFile);
        reportData = report.getReportData();
        assertEquals("{2346=2, 2345=3}", reportData.get(1));
        assertEquals("{eu_west=[3244132, 3244332], us_west=[1233456, 1223456], us_east=[2343225]}", reportData.get(3));
        assertEquals("{eu_west=2, us_west=2, us_east=1}", reportData.get(5));
        assertEquals("{eu_west=4222.0, us_west=2216.0, us_east=3445.0}", reportData.get(7));

        // all records are bad, report results are empty as expected
        report = new CustomerProjectReport();
        reportFile = "./src/test/resources/allbad-input.csv";
        report.processFile(reportFile);
        reportData = report.getReportData();
        assertEquals("{}", reportData.get(1));
        assertEquals("{}", reportData.get(3));
        assertEquals("{}", reportData.get(5));
        assertEquals("{}", reportData.get(7));
    }

    @Test
    public void testCalculateAvgBuildDurationForGeoZone() {
        CustomerProjectReport report = new CustomerProjectReport();
        Map<String, List<CustomerProject>> customerProjectsByGeoZone = new HashMap<>();
        List<CustomerProject> customerProjects = new ArrayList<>();
        CustomerProject customerProject = new CustomerProject(0, 0, "us-west", "teamCode", "projectCode", 100);
        customerProjects.add(customerProject);
        customerProject = new CustomerProject(0, 0, "us-west", "teamCode", "projectCode", 50);
        customerProjects.add(customerProject);
        customerProjectsByGeoZone.put("us-west", customerProjects);

        customerProjects = new ArrayList<>();
        customerProject = new CustomerProject(0, 0, "us-east", "teamCode", "projectCode", 0);
        customerProjects.add(customerProject);
        customerProjectsByGeoZone.put("us-east", customerProjects);

        customerProjects = new ArrayList<>();
        customerProject = new CustomerProject(0, 0, "eu-west", "teamCode", "projectCode", 1000);
        customerProjects.add(customerProject);
        customerProjectsByGeoZone.put("eu-west", customerProjects);
        
        Map<String, Double>  resultMap = report.calculateAvgBuildDurationForGeoZone(customerProjectsByGeoZone);
        assertEquals("{eu-west=1000.0, us-east=0.0, us-west=75.0}", resultMap.toString());
    }

    @Test
    public void testCountUniqueCustomersForContract() {
        CustomerProjectReport report = new CustomerProjectReport();
        Map<String, List<CustomerProject>> customerProjectsByContract = new HashMap<>();
        List<CustomerProject> customerProjects = new ArrayList<>();
        CustomerProject customerProject = new CustomerProject(1, 123, "us-west", "teamCode", "projectCode", 100);
        customerProjects.add(customerProject);
        customerProject = new CustomerProject(1, 123, "us-west", "teamCode", "projectCode", 100);
        customerProjects.add(customerProject);
        customerProject = new CustomerProject(1, 123, "us-west", "teamCode", "projectCode", 100);
        customerProjects.add(customerProject);
        customerProject = new CustomerProject(2, 123, "us-west", "teamCode", "projectCode", 100);
        customerProjects.add(customerProject);
        customerProject = new CustomerProject(2, 123, "us-west", "teamCode", "projectCode", 100);
        customerProjects.add(customerProject);
        customerProject = new CustomerProject(3, 123, "us-west", "teamCode", "projectCode", 100);
        customerProjects.add(customerProject);
        customerProjectsByContract.put("123", customerProjects);
        customerProjects = new ArrayList<>();
        customerProject = new CustomerProject(3, 456, "us-west", "teamCode", "projectCode", 100);
        customerProjects.add(customerProject);
        customerProjectsByContract.put("456", customerProjects);
        customerProjects = new ArrayList<>();
        customerProjectsByContract.put("789", customerProjects);
        Map<String, Integer> resultMap = report.countUniqueCustomersForContract(customerProjectsByContract);
        assertEquals("{123=3, 456=1, 789=0}", resultMap.toString());   
    }

    @Test
    public void testCountUniqueCustomersForGeoZone() {
        CustomerProjectReport report = new CustomerProjectReport();
        Map<String, List<Long>> uniqueCustomersForGeoZone = new HashMap<>();
        List<Long> customers = new ArrayList<>();
        customers.add(1L);
        customers.add(1L);
        customers.add(1L);
        customers.add(2L);
        customers.add(3L);
        uniqueCustomersForGeoZone.put("us-west", customers);
        customers = new ArrayList<>();
        customers.add(5L);
        uniqueCustomersForGeoZone.put("us-east", customers);
        customers = new ArrayList<>();
        uniqueCustomersForGeoZone.put("eu-west", customers);
        Map<String,Integer> resultMap = report.countUniqueCustomersForGeoZone(uniqueCustomersForGeoZone);
        assertEquals("{eu-west=0, us-east=1, us-west=5}", resultMap.toString());
    }

    @Test
    public void testGetUniqueCustomersForGeoZone() {
        CustomerProjectReport report = new CustomerProjectReport();
        Map<String, List<CustomerProject>> customerProjectsByGeoZone = new HashMap<>();
        List<CustomerProject> customerProjects = new ArrayList<>();
        CustomerProject customerProject = new CustomerProject(1, 0, "us-west", "teamCode", "projectCode", 0);
        customerProjects.add(customerProject);
        customerProject = new CustomerProject(1, 0, "us-west", "teamCode", "projectCode", 0);
        customerProjects.add(customerProject);
        customerProject = new CustomerProject(1, 0, "us-west", "teamCode", "projectCode", 0);
        customerProjects.add(customerProject);
        customerProject = new CustomerProject(2, 0, "us-west", "teamCode", "projectCode", 0);
        customerProjects.add(customerProject);
        customerProject = new CustomerProject(3, 0, "us-west", "teamCode", "projectCode", 0);
        customerProjects.add(customerProject);
        customerProjectsByGeoZone.put("us-west", customerProjects);
        Map<String,List<Long>> resultMap = report.getUniqueCustomersForGeoZone(customerProjectsByGeoZone);
        assertEquals("{us-west=[1, 2, 3]}", resultMap.toString());
    }

}
