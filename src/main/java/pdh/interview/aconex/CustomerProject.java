package pdh.interview.aconex;

/**
 * This object encapsulates a single row of input data for the CustomerProjectReport.
 * The input data contains the following fields:
 * customerId,contractId,geozone,teamcode,projectcode,buildduration
 * Based on the sample input data specified in Technical Challenge.pdf, 
 * the following assumptions will be made:
    * A contract may involve one or more customers.
    * A contract may have one or more projects.
    * A contract may span one or more geo-zones.
    * A contract may have one or more teams.
    * A contract has a build duration.
    * A project is for a customer.
    * A project is worked-on by a team.
    * A project is in a a geo-zone.
    * A customer is in a geo-zone.
    * A team is allocated to a contract.
    * A team works in a geo-zone.
    * A team may work on multiple projects.
 * In addition, assumptions not directly discerned by the sample data:
    * A customer may be part of multiple contracts.
    * A cutomser may have multiple projects.
    * A buildDuration will always be in seconds with the last character as 's'.
 */

public class CustomerProject {
     
    public CustomerProject(long customerId, int contractId, String geoZone, String teamCode, String projectCode,
            long buildDuration) {
        this.customerId = customerId;
        this.contractId = contractId;
        this.geoZone = geoZone;
        this.teamCode = teamCode;
        this.projectCode = projectCode;
        this.buildDuration = buildDuration;
    }

    // Ex: 2343225
    private long customerId;

    // Ex: 2345
    private int contractId;

    // Ex: us_east
    private String geoZone;

    // Ex: RedTeam
    private String teamCode;

    // Ex: ProjectApple
    private String projectCode;

    // Ex: 3445 seconds
    private long buildDuration;

    public long getCustomerId() {
        return customerId;
    }

    public int getContractId() {
        return contractId;
    }

    public String getGeoZone() {
        return geoZone;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public long getBuildDuration() {
        return buildDuration;
    }

    @Override
    public String toString() {
        return "CustomerProject [customerId=" + customerId + ", contractId=" + contractId + ", geoZone=" + geoZone
                + ", teamCode=" + teamCode + ", projectcode=" + projectCode + ", buildDuration=" + buildDuration + "]";
    }

}