package pt.floraon.redlistdata.entities;

import com.google.gson.JsonObject;
import pt.floraon.driver.Constants;
import pt.floraon.entities.GeneralDBNode;
import pt.floraon.entities.TaxEnt;
import pt.floraon.results.InferredStatus;

/**
 * Created by miguel on 11-11-2016.
 */
public class RedListDataEntity extends GeneralDBNode {
    /**
     * The full TaxEnt database entity. Note this is not stored in the DB, must be fetched by {@link RedListDataEntity#TaxEntID}
     */
    private transient TaxEnt taxEnt;
    /**
     * The ID of the TaxEnt
     */
    private String TaxEntID;
    /**
     * The inferred statuses of this TaxEnt in this territory. This is computed upon initialization and stored.
     * Changes in the checklist must be synced.
     */
    private InferredStatus inferredStatus;
    /**
     * Arbitrary textual tags used for filtering the whole list
     */
    private String[] tags;
    /**
     * Whether the taxon has taxonomic problems
     */
    private boolean hasTaxonomicProblems;
    /**
     * Textual decription of the taxonomic problems
     */
    private String taxonomicProblemDescription;
    /**
     * The fileds pertaining to the geographical distribution
     */
    private GeographicalDistribution geographicalDistribution = new GeographicalDistribution();
    /**
     * The fileds pertaining to the population
     */
    private Population population = new Population();

    public RedListDataEntity() {
    }

    public RedListDataEntity(String taxEntID, InferredStatus inferredStatus) {
        this.TaxEntID = taxEntID;
        this.inferredStatus = inferredStatus;
    }

    public TaxEnt getTaxEnt() {
        return taxEnt;
    }

    public InferredStatus getInferredStatus() {
        return inferredStatus;
    }

    public String getTaxEntID() {
        return TaxEntID;
    }

    public String[] getTags() {
        return tags;
    }

    public boolean isHasTaxonomicProblems() {
        return hasTaxonomicProblems;
    }

    public String getTaxonomicProblemDescription() {
        return taxonomicProblemDescription;
    }

    public GeographicalDistribution getGeographicalDistribution() {
        return geographicalDistribution;
    }

    public Population getPopulation() {
        return population;
    }

    public void setTaxEntID(String taxEntID) {
        TaxEntID = taxEntID;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public void setHasTaxonomicProblems(boolean hasTaxonomicProblems) {
        this.hasTaxonomicProblems = hasTaxonomicProblems;
    }

    public void setTaxonomicProblemDescription(String taxonomicProblemDescription) {
        this.taxonomicProblemDescription = taxonomicProblemDescription;
    }

    /*******
     * GeographicalDistribution fields
     *******/

    public void setGeographicalDistribution_Description(String description) {
        this.geographicalDistribution.setDescription(description);
    }

    public void setGeographicalDistribution_EOO(Long EOO) {
        this.geographicalDistribution.setEOO(EOO);
    }

    public void setGeographicalDistribution_AOO(Long AOO) {
        this.geographicalDistribution.setAOO(AOO);
    }

    public void setGeographicalDistribution_DeclineDistribution(String declineDistribution) {
        this.geographicalDistribution.setDeclineDistribution(RedListEnums.DeclineDistribution.valueOf(declineDistribution));
    }

    public void setGeographicalDistribution_ElevationRange(Integer[] elevationRange) {
        this.geographicalDistribution.setElevationRange(elevationRange);
    }

    /*******
     * Population fields
     *******/

    public void setPopulation_Description(String description) {
        this.population.setDescription(description);
    }

    public void setPopulation_NrMatureIndividualsCategory(String nrMatureIndividualsCategory) {
        this.population.setNrMatureIndividualsCategory(RedListEnums.NrMatureIndividuals.valueOf(nrMatureIndividualsCategory));
    }

    public void setPopulation_NrMatureIndividualsExact(Long nrMatureIndividualsExact) {
        this.population.setNrMatureIndividualsExact(nrMatureIndividualsExact);
    }

    public void setPopulation_NrMatureIndividualsDescription(String nrMatureIndividualsDescription) {
        this.population.setNrMatureIndividualsDescription(nrMatureIndividualsDescription);
    }

    public void setPopulation_TypeOfEstimate(String typeOfEstimate) {
        this.population.setTypeOfEstimate(RedListEnums.TypeOfPopulationEstimate.valueOf(typeOfEstimate));
    }

    public void setPopulation_PopulationDecline(String populationDecline) {
        this.population.setPopulationDecline(RedListEnums.DeclinePopulation.valueOf(populationDecline));
    }

    public void setPopulation_PopulationTrend(Integer populationTrend) {
        this.population.setPopulationTrend(populationTrend);
    }

    public void setPopulation_SeverelyFragmented(String severelyFragmented) {
        this.population.setSeverelyFragmented(RedListEnums.SeverelyFragmented.valueOf(severelyFragmented));
    }

    public void setPopulation_ExtremeFluctuations(String extremeFluctuations) {
        this.population.setExtremeFluctuations(RedListEnums.ExtremeFluctuations.valueOf(extremeFluctuations));
    }

    @Override
    public Constants.NodeTypes getType() {
        return null;
    }

    @Override
    public String getTypeAsString() {
        return null;
    }

    @Override
    public JsonObject toJson() {
        return null;
    }

    @Override
    public String toJsonString() {
        return null;
    }
}
