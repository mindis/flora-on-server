package pt.floraon.occurrences.entities;

import com.arangodb.velocypack.annotations.Expose;
import pt.floraon.driver.*;
import pt.floraon.driver.annotations.*;
import pt.floraon.driver.entities.GeneralDBNode;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.geometry.*;
import pt.floraon.occurrences.fields.parsers.LatitudeLongitudeParser;
import pt.floraon.occurrences.fields.parsers.PlainTextParser;

import java.io.Serializable;
import java.util.*;

/**
 * Represents the data associated with an inventory, including unmatched taxa. Those that are matched will be converted
 * to graph links and removed from this entity.
 * Created by miguel on 05-02-2017.
 */
public class Inventory extends GeneralDBNode implements Serializable, DiffableBean, GeoBean {
    /**
     * NOTE: coordinates of the observation have priority over these. Only if observationLatitude and observationLongitude
     * are not set, then we can use these inventory coordinates.
     */
    @SmallField @HideInInventoryView @FieldParser(LatitudeLongitudeParser.class)
    @PrettyName(value = "Latitude do inventário", shortName = "Inv lat")
    private Float latitude;
    @SmallField @HideInInventoryView @FieldParser(LatitudeLongitudeParser.class)
    @PrettyName(value = "Longitude do inventário", shortName = "Inv long")
    private Float longitude;
    private String spatialRS;
    private Float elevation;
    private String geometry;
    @SmallField @HideInInventoryView
    @PrettyName(value = "Ano", shortName = "Ano")
    private Integer year;
    @SmallField @HideInInventoryView
    @PrettyName(value = "Mês", shortName = "Mês")
    private Integer month;
    @SmallField @HideInInventoryView
    @PrettyName(value = "Dia", shortName = "Dia")
    private Integer day;   // TODO: these cannot be erased...
    @SmallField @HideInInventoryView
    @PrettyName(value = "Precisão", shortName = "Prec")
    private Precision precision;
    private Boolean complete;
    @HideInInventoryView @FieldParser(PlainTextParser.class)
    @PrettyName(value = "Habitat", shortName = "Hab")
    private String habitat;
    @HideInCompactView @HideInInventoryView @FieldParser(PlainTextParser.class)
    @PrettyName(value = "Notas públicas do inventário", shortName = "Notas pub")
    private String pubNotes;
    @HideInCompactView @HideInInventoryView @FieldParser(PlainTextParser.class)
    @PrettyName(value = "Notas privadas do inventário", shortName = "Notas priv")
    private String privNotes;
    private String geology;
    private String[] tags;
    @HideInInventoryView
    @PrettyName(value = "Observadores", shortName = "Observadores")
    private String[] observers;
    @HideInCompactView @HideInInventoryView
    @PrettyName(value = "Colectores", shortName = "Colectores")
    private String[] collectors;
    @HideInCompactView @HideInInventoryView
    @PrettyName(value = "Determinadores", shortName = "Dets")
    private String[] dets;
    private String verbLocality;
    @HideInInventoryView @FieldParser(PlainTextParser.class)
    @PrettyName(value = "Local", shortName = "Local")
    private String locality;
    @HideInInventoryView @FieldParser(PlainTextParser.class)
    @PrettyName(value = "Município", shortName = "Município")
    private String municipality;
    @HideInInventoryView @FieldParser(PlainTextParser.class)
    @PrettyName(value = "Distrito", shortName = "Distrito")
    private String province;
    @HideInInventoryView @FieldParser(PlainTextParser.class)
    @PrettyName(value = "Região", shortName = "Região")
    private String county;
    @SmallField @HideInInventoryView
    @PrettyName(value = "Código do inventário", shortName = "Cod")
    private String code;
    @HideInInventoryView @FieldParser(PlainTextParser.class)
    @PrettyName(value = "Ameaças do local", shortName = "Ameaças")
    private String threats;
    @HideInCompactView @ReadOnly @HideInInventoryView
    @PrettyName(value = "Responsável", shortName = "Resp")
    private String maintainer;
    @HideInInventoryView
    @PrettyName(value = "Área do inventário", shortName = "Área")
    private Float area;
    private Float meanHeight;
    private Float totalCover;
    private String aspect;
    private Integer slope;

    /**
     * This list holds the occurrencces ({@link OBSERVED_IN}) that are not yet matched to the taxonomic graph.
     * Occurrences that are matched are removed from this list and converted into graph links.
     * All new occurrences shall go in here.
     */
    private List<OBSERVED_IN> unmatchedOccurrences;

    /**
     * This list shall be populated, when needed, with all matched occurrences in this inventory
     * TODO: this is a workaround for now...
     */
    @Expose(serialize = false)
    protected OBSERVED_IN[] taxa;

    /**
     * This list shall be populated, when needed, with the observer names.
     * Remember that only the observer IDs are stored in the field "observers"
     */
    @Expose(serialize = false)
    private String[] observerNames;

    @Expose(serialize = false)
    private Float utmX, utmY;

    public Inventory(Inventory other) {
        super(other);
        this.latitude = other.latitude;
        this.longitude = other.longitude;
        this.spatialRS = other.spatialRS;
        this.elevation = other.elevation;
        this.geometry = other.geometry;
        this.precision = other.precision;
        this.year = other.year;
        this.month = other.month;
        this.day = other.day;
        this.complete = other.complete;
        this.habitat = other.habitat;
        this.pubNotes = other.pubNotes;
        this.privNotes = other.privNotes;
        this.geology = other.geology;
        this.tags = other.tags;
        this.observers = other.observers;
        this.collectors = other.collectors;
        this.dets = other.dets;
        this.verbLocality = other.verbLocality;
        this.locality = other.locality;
        this.municipality = other.municipality;
        this.province = other.province;
        this.county = other.county;
        this.code = other.code;
        this.threats = other.threats;
        this.maintainer = other.maintainer;
        this.area = other.area;
        this.totalCover = other.totalCover;
        this.meanHeight = other.meanHeight;
        this.aspect = other.aspect;
        this.slope = other.slope;
        this.observerNames = other.observerNames;
    }

    public Inventory() { }

    /**
     * Check if latitude-longitude coordinates are set. If not, try to convert from UTM, if set.
     */
    private void checkGeographicCoordinates() {
        if((latitude == null || longitude == null) && utmX != null && utmY != null) {
            // TODO support for UTM zones!!
            LatLongCoordinate llc = CoordinateConversion.UtmToLatLonWGS84(29, 'S', utmX.longValue(), utmY.longValue());
            this.latitude = llc.getLatitude();
            this.longitude = llc.getLongitude();
        }

        if(Constants.isNoData(latitude)) latitude = null;
        if(Constants.isNoData(longitude)) longitude = null;
    }

    /**
     * We should get coordinates from observation whenever there's only one taxon and it has coordinantes.
     * @return
     */
    private boolean shouldGetCoordinatesFromObservation() {
        if(_getTaxa() != null && _getTaxa().length == 1) {
            if(_getTaxa()[0].getObservationLatitude() == null || _getTaxa()[0].getObservationLongitude() == null) return false;
            return true;
        } else return false;
    }

    /**
     * Returns the latitude of the inventory, OR, if there is only one observation, returns latitude of that observation, if set.
     * @return
     */
    public Float _getLatitude() {
        checkGeographicCoordinates();
        if(shouldGetCoordinatesFromObservation()) {
            Float olat = _getTaxa()[0].getObservationLatitude();
            return (Constants.isNoData(olat) ? null : olat);
        } else {
            if(Constants.isNullOrNoData(latitude)) {
                Float olat = 0f;
                int count = 0;
                for(OBSERVED_IN oi : _getTaxa()) {
                    if(Constants.isNullOrNoData(oi.getObservationLatitude())) continue;
                    olat += oi.getObservationLatitude();
                    count ++;
                }
                return olat == 0 ? null : (olat / count);

            } else return latitude;
        }
    }

    public Float _getLongitude() {
        checkGeographicCoordinates();
        if(shouldGetCoordinatesFromObservation()) {
            Float olng = _getTaxa()[0].getObservationLongitude();
            return (Constants.isNoData(olng) ? null : olng);
        } else {
            if(Constants.isNullOrNoData(longitude)) {
                Float olng = 0f;
                int count = 0;
                for(OBSERVED_IN oi : _getTaxa()) {
                    if(Constants.isNullOrNoData(oi.getObservationLongitude())) continue;
                    olng += oi.getObservationLongitude();
                    count ++;
                }
                return olng == 0 ? null : (olng / count);
            } else return longitude;
        }
    }

    public String _getCoordinates() {
        if(this._getLatitude() == null || this._getLongitude() == null)
            return "*";
        else
            return String.format(Locale.ROOT, "%.5f, %.5f", this._getLatitude(), this._getLongitude());
    }

    public Float _getInventoryLatitude() {
        checkGeographicCoordinates();
        return latitude == null ? _getLatitude() : latitude;
    }

    public Float _getInventoryLongitude() {
        checkGeographicCoordinates();
        return longitude == null ? _getLongitude() : longitude;
    }

    /**
     * This gets the coordinates of the Inventory. If they are null, and the inventory has only one observation, returns
     * the coordinates of that observation in parenthesis.
     * @return
     */
    public String _getInventoryCoordinates() {
        if(this.latitude == null || this.longitude == null) {
            if(this._getLatitude() == null || this._getLongitude() == null)
                return "*";
            else
                return "* (" + this._getCoordinates() + ")";
        } else {
            return String.format(Locale.ROOT, "%.5f, %.5f", this._getInventoryLatitude(), this._getInventoryLongitude());
        }
    }

    @Override
    public void _setUTMX(Float x) {
        this.utmX = x;
    }

    @Override
    public Float _getsetUTMX() {
        return this.utmX;
    }

    @Override
    public void _setUTMY(Float y) {
        this.utmY = y;
    }

    @Override
    public Float _getsetUTMY() {
        return this.utmY;
    }

    @Override
    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    @Override
    public Float getLatitude() {
        checkGeographicCoordinates();
        return this.latitude;
    }

    @Override
    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    @Override
    public Float getLongitude() {
        checkGeographicCoordinates();
        return this.longitude;
    }

    public String getSpatialRS() {
        return spatialRS;
    }

    public void setSpatialRS(String spatialRS) {
        this.spatialRS = spatialRS;
    }

    public Float getElevation() {
        return elevation;
    }

    public void setElevation(Float elevation) {
        this.elevation = elevation;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public Precision getPrecision() {
        return precision;
    }

    public void setPrecision(String precision) throws FloraOnException {
        this.precision = new Precision(precision);
    }

    public void setPrecision(Precision precision) {
        this.precision = precision;
    }

    public Integer getYear() {
        return Constants.isNoData(year) ? null : year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return Constants.isNoData(month) ? null : month;
    }

    public void setMonth(Integer month) {
        if(!Constants.isNoData(month) && (month < 1 || month > 12)) {
//            Log.warn("Invalid month " + month);
            return;
        }
        this.month = month;
    }

    public Integer getDay() {
        return Constants.isNoData(day) ? null : day;
    }

    public void setDay(Integer day) {
        if(!Constants.isNoData(day) && (day < 1 || day > 31)) {
//            Log.warn("Invalid day " + day);
            return;
//            throw new IllegalArgumentException("Invalid day " + day);
        }
        this.day = day;
    }

    public String _getDate() {
/*
        Calendar c = new GregorianCalendar();
        if(year != null) c.set(Calendar.YEAR, year);
        if(month != null) c.set(Calendar.MONTH, month);
        if(day != null) c.set(Calendar.DAY_OF_MONTH, day);
        return Constants.dateFormat.format(c.getTime());
*/
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.isNullOrNoData(day) ? "--" : day).append("/")
                .append(Constants.isNullOrNoData(month) ? "--" : month).append("/")
                .append(Constants.isNullOrNoData(year) ? "----" : year);
        return sb.toString();
    }

    public boolean _isDateEmpty() {
        return (Constants.isNullOrNoData(day) || day == 0) && (Constants.isNullOrNoData(month) || month == 0)
                && (Constants.isNullOrNoData(year) || year == 0);
    }

    public String _getDateYMD() {
        return formatDateYMD(this.day, this.month, this.year);
    }

    static public String formatDateYMD(Integer day, Integer month, Integer year) {
        return formatDateYMD(day, month, year, "-");
    }

    static public String formatDateYMD(Integer day, Integer month, Integer year, String nullPlaceholder) {
        String yp = new String(new char[4]).replace("\0", nullPlaceholder);
        String dp = new String(new char[2]).replace("\0", nullPlaceholder);
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.isNullOrNoData(year) ? yp : year).append("/")
                .append(Constants.isNullOrNoData(month) ? dp : String.format("%02d", month)).append("/")
                .append(Constants.isNullOrNoData(day) ? dp : String.format("%02d", day));
        return sb.toString();
    }

    public UTMCoordinate _getUTMCoordinates() {
        if(this._getLatitude() == null || this._getLongitude() == null) return null;
        return CoordinateConversion.LatLonToUtmWGS84(this._getLatitude(), this._getLongitude(), 0);
    }

    public String _getMGRSString(long sizeOfSquare) {
        return CoordinateConversion.LatLongToMGRS(this._getLatitude(), this._getLongitude(), sizeOfSquare);
    }

    public Boolean getComplete() {
        return complete;
    }

    public void setComplete(Boolean complete) {
        this.complete = complete;
    }

    public String getHabitat() {
        return habitat;
    }

    public void setHabitat(String habitat) {
        this.habitat = habitat;
    }

    public String getPubNotes() {
        return pubNotes;
    }

    public void setPubNotes(String pubNotes) {
        this.pubNotes = pubNotes;
    }

    public String getPrivNotes() {
        return privNotes;
    }

    public void setPrivNotes(String privNotes) {
        this.privNotes = privNotes;
    }

    public String getGeology() {
        return geology;
    }

    public void setGeology(String geology) {
        this.geology = geology;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String[] getObservers() {
        return StringUtils.isArrayEmpty(observers) ? new String[0] : observers;
    }

    public void setObservers(String[] observers) {
        this.observers = observers;
    }

    public String[] getCollectors() {
        return StringUtils.isArrayEmpty(collectors) ? new String[0] : collectors;
    }

    public void setCollectors(String[] collectors) {
        this.collectors = collectors;
    }

    public String[] getDets() {
        return StringUtils.isArrayEmpty(dets) ? new String[0] : dets;
    }

    public void setDets(String[] dets) {
        this.dets = dets;
    }

    @Override
    public String getVerbLocality() {
        return verbLocality;
    }

    @Override
    public void setVerbLocality(String verbLocality) {
        this.verbLocality = verbLocality;
    }

    @Override
    public String getLocality() {
        return locality;
    }

    @Override
    public void setLocality(String locality) {
        this.locality = locality;
    }

    @Override
    public String getMunicipality() {
        return municipality;
    }

    @Override
    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    @Override
    public String getProvince() {
        return province;
    }

    @Override
    public void setProvince(String province) {
        this.province = province;
    }

    @Override
    public String getCounty() {
        return county;
    }

    @Override
    public void setCounty(String county) {
        this.county = county;
    }

    public String getCode() {
        return code == null ? ((_getTaxa() != null && _getTaxa().length == 1) ? _getTaxa()[0].getGpsCode() : null) : code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getThreats() {
        return threats;
    }

    public void setThreats(String threats) {
        this.threats = threats;
    }

    public String getMaintainer() {
        return maintainer;
    }

    public void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
    }

    public Float getArea() {
        return area;
    }

    public void setArea(Float area) {
        this.area = area;
    }

    public Float getTotalCover() {
        return totalCover;
    }

    public void setTotalCover(Float totalCover) {
        this.totalCover = totalCover;
    }

    public Float getMeanHeight() {
        return meanHeight;
    }

    public void setMeanHeight(Float meanHeight) {
        this.meanHeight = meanHeight;
    }

    public String getAspect() {
        return aspect;
    }

    public void setAspect(String aspect) {
        this.aspect = aspect;
    }

    public Integer getSlope() {
        return slope;
    }

    public void setSlope(Integer slope) {
        this.slope = slope;
    }

    public List<OBSERVED_IN> getUnmatchedOccurrences() {
        return unmatchedOccurrences == null ? (this.unmatchedOccurrences = new ArrayList<>()) : unmatchedOccurrences;
    }

    public void setUnmatchedOccurrences(List<OBSERVED_IN> unmatchedOccurrences) {
        this.unmatchedOccurrences = unmatchedOccurrences;
    }

    /* ****************************************/
    /* Getters for transient fields           */
    /* ****************************************/

    public String[] _getObserverNames() {
        return StringUtils.isArrayEmpty(this.observerNames) ? new String[0] : this.observerNames;
    }

    public void _setObserverNames(String[] observerNames) {
        this.observerNames = observerNames;
    }

    public OBSERVED_IN[] _getTaxa() {
        return StringUtils.isArrayEmpty(this.taxa) ?
                (unmatchedOccurrences == null ?
                        new OBSERVED_IN[0] : unmatchedOccurrences.toArray(new OBSERVED_IN[unmatchedOccurrences.size()]))
                : this.taxa;
    }

    public List<OBSERVED_IN> _getOccurrences() {
        // TODO: this should return the occurrences that are graph links aswell!
        return getUnmatchedOccurrences();
    }

    /**
     * Gets a textual summary of the taxa.
     * @param nTaxa How many taxa to show
     * @return
     */
    public String _getSampleTaxa(int nTaxa) {
        OBSERVED_IN[] tmp = _getTaxa();
        if(tmp.length == 0) return "[sem taxa]";
        List<String> tmp1 = new ArrayList<>();
        int i;
        for (i = 0; i < nTaxa && i < tmp.length; i++) {
            if(tmp[i].getTaxEnt() == null) {
                if(tmp[i].getVerbTaxon() == null || tmp[i].getVerbTaxon().equals(""))
                    tmp1.add("[sem nome]");
                else
                    tmp1.add(tmp[i].getVerbTaxon());
            } else
                tmp1.add("<i>" + tmp[i].getTaxEnt().getName() + "</i>");
        }
        if(i < tmp.length) tmp1.add("... e mais " + (tmp.length - i));
        return StringUtils.implode(", ", tmp1.toArray(new String[tmp1.size()]));
    }

    @Override
    public Constants.NodeTypes getType() {
        return Constants.NodeTypes.inventory;
    }

    @Override
    public String getTypeAsString() {
        return this.getType().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Inventory that = (Inventory) o;

        if(code != null) return code.equals(that.code);
        if ((precision != null && precision._isImprecise()) || (that.precision != null && that.precision._isImprecise())
            || (precision != null ? !precision.equals(that.precision) : that.precision != null) || getLatitude() == null
                || getLongitude() == null || that.getLatitude() == null || that.getLongitude() == null) return false;
        if (getLatitude() != null ? !getLatitude().equals(that.getLatitude()) : that.getLatitude() != null) return false;
        if (getLongitude() != null ? !getLongitude().equals(that.getLongitude()) : that.getLongitude() != null) return false;
        if (getYear() != null ? !getYear().equals(that.getYear()) : that.getYear() != null) return false;
        if (getMonth() != null ? !getMonth().equals(that.getMonth()) : that.getMonth() != null) return false;
        if (getDay() != null ? !getDay().equals(that.getDay()) : that.getDay() != null) return false;
        if ((getYear() == null && getMonth() == null && getDay() == null)  // if any of the dates is null, it's never equal
                || (that.getYear() == null && that.getMonth() == null && that.getDay() == null)) return false;
        if (municipality != null ? !municipality.equals(that.municipality) : that.municipality != null) return false;
        if (county != null ? !county.equals(that.county) : that.county != null) return false;
        if (locality != null ? !locality.equals(that.locality) : that.locality != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(observers, that.observers)) return false;
        return code != null ? code.equals(that.code) : that.code == null;
    }

    /**
     * NOTE that this implementation of hashCode and equals assume that if an inventory is in the same place, same date
     * and same observers, then it is the same inventory, no matter the other fields. If the inventory is imprecise, it
     * will never be equal to another one.
     * @return
     */
    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;    // NOTE: we don't use the getter here cause the getter does some processing to avoid nulls. here we want the real inventory code as is.
        if(code != null && !code.equals("")) return result;     // code rules!
        if(getLatitude() == null || getLongitude() == null) return result;
        result = 31 * result + (precision != null ? precision.hashCode() : 0);
        result = 31 * result + (getLatitude() != null ? getLatitude().hashCode() : 0);
        result = 31 * result + (getLongitude() != null ? getLongitude().hashCode() : 0);
        result = 31 * result + (getYear() != null ? getYear().hashCode() : 0);
        result = 31 * result + (getMonth() != null ? getMonth().hashCode() : 0);
        result = 31 * result + (getDay() != null ? getDay().hashCode() : 0);
        result = 31 * result + (municipality != null ? municipality.hashCode() : 0);
        result = 31 * result + (county != null ? county.hashCode() : 0);
        result = 31 * result + (locality != null ? locality.hashCode() : 0);
        result = 31 * result + (observers != null ? Arrays.hashCode(observers) : 0);
        return result;
    }
}
