package pt.floraon.taxonomy.entities;

import jline.internal.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a parsed taxon name without authorship
 * Created by miguel on 06-03-2017.
 */
public class CanonicalName {
    private String genus, specificEpithet;
    private List<InfraRank> infraRanks;
    /**
     * Matches a taxon name, given as a canonical name: no author, no anything else than
     * Genus species (subsp. infrarank)... (or only one word, for taxa higher than species)
     */
    private transient static Pattern canonicalName = Pattern.compile("^ *(?<genus>[a-zA-Z]+)(?: +(?<species>[a-z-]+))?(?: +(?<rest>.*))?$");

    private transient static Pattern infraTaxa = Pattern.compile(" *(?:(?<rank>subsp|var|f)\\.? +)?(?<infra>[a-z-]+)");

    public CanonicalName(String verbatimName) {
        Matcher m = canonicalName.matcher(verbatimName);
        Log.info("    Verb: " + verbatimName);
        if(m.find()) {
            this.genus = m.group("genus");
            this.specificEpithet = m.group("species");
            if(this.specificEpithet == null) {
                Log.info("    Canonical: G=" + genus);
                return;
            }
            String rest = m.group("rest");
            if(rest != null) {
                Log.info("    Canonical: G=" + genus, "; S=", specificEpithet, "; rest=", rest);
                this.infraRanks = new ArrayList<>();
                Matcher m1 = infraTaxa.matcher(rest);
                while (m1.find()) {
                    if(m1.group("infra") != null) {
                        InfraRank tmp = new InfraRank(m1.group("rank"), m1.group("infra"));
                        this.infraRanks.add(tmp);
                        Log.info("Infra: ", tmp.toString());
                    }
                }
            } else
                Log.info("    Canonical: G=" + genus, "; S=", specificEpithet);
        }

    }

    public String getGenus() {
        return genus;
    }

    public String getSpecificEpithet() {
        return specificEpithet;
    }

    public List<InfraRank> getInfraRanks() {
        return infraRanks;
    }

    public String getInfraRanksAsString() {
        if(infraRanks == null)
            return "";
        else {
            StringBuilder sb = new StringBuilder();
            for (InfraRank ir : this.infraRanks) {
                sb.append(" ").append(ir.toString());
            }
            return sb.toString().trim();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.genus);
        if(this.specificEpithet == null) return sb.toString();
        sb.append(" ").append(this.specificEpithet);

        if(this.infraRanks != null) {
            sb.append(" ").append(this.getInfraRanksAsString());
        }
        return sb.toString();
    }

    public class InfraRank {
        private String infraRank, infraTaxon;

        public InfraRank(String infraRank, String infraTaxon) {
            this.infraRank = infraRank;
            this.infraTaxon = infraTaxon;
        }

        public String getInfraRank() {
            return infraRank;
        }

        public String getInfraTaxon() {
            return infraTaxon;
        }

        @Override
        public String toString() {
            if(this.infraRank != null)
                return this.infraRank + (this.infraRank.endsWith(".") ? "" : ".") + " " + this.infraTaxon;
            else
                return this.infraTaxon;
        }
    }
}