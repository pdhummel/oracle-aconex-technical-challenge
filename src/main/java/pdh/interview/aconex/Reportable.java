package pdh.interview.aconex;

import java.util.List;

/**
 * Helps us separate the concerns of generating a report and rendering the report.
 */
public interface Reportable {
    public void generateReport() throws Exception;
    public List<String>  getReportData();
}
