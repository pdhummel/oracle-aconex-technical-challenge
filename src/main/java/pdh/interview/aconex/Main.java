package pdh.interview.aconex;

public class Main {
    /**
     * Pass the input file path as an argument.
     * @param args
     */
    public static void main(String args[]) {
        try {
            
            String reportFile = null;
            if (args.length > 0) {
                reportFile = args[1];
            } else {
                System.out.println("No input file path specified, using default test file.");
                reportFile = "./src/test/resources/input.csv";
            }
            Reportable report = new CustomerProjectReport(reportFile);
            report.generateReport();
            new SimpleReportWriter().outputReportData(report);
        } catch(Exception e) {
            System.out.println("Working Directory = " + System.getProperty("user.dir"));
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
}
