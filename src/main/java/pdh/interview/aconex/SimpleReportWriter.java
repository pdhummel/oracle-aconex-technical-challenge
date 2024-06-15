package pdh.interview.aconex;

import java.util.List;

public class SimpleReportWriter {
    public SimpleReportWriter() {
    }

    /**
     * Dumps the report content to stdout.
     * @param report
     * @throws Exception
     */
    public void outputReportData(Reportable report) throws Exception {
        List<String> reportData = report.getReportData();
        for (String line: reportData) {
            System.out.println(line);
        }
    }
}
