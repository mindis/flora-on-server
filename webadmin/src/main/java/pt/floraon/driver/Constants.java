package pt.floraon.driver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Objects;
import java.util.Set;

import pt.floraon.taxonomy.entities.*;
import pt.floraon.driver.entities.DBEntity;
import pt.floraon.driver.entities.GeneralDBEdge;
import pt.floraon.driver.entities.GeneralDBNode;
import pt.floraon.driver.entities.NamedDBNode;
import pt.floraon.morphology.entities.Attribute;
import pt.floraon.morphology.entities.Character;
import pt.floraon.occurrences.entities.Author;
import pt.floraon.occurrences.entities.SpeciesList;
import pt.floraon.authentication.entities.User;

public final class Constants {
	/*
	 * TODO:
		- centaurea langei rothmaleriana restricted to
		- territories with complete distributions must propagate through synonym
		- update node no grafo remove legacy ID!
	 */
	public static String TAXONOMICGRAPHNAME="taxgraph";

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	public static final String ANSI_CYANBOLD = "\u001B[36;1m";
	public static final String ANSI_GREENBOLD = "\u001B[32;1m";
	public static final String ANSI_BGBLACK = "\u001B[40m";
	public static final String ANSI_BGRED = "\u001B[41m";
	public static final String ANSI_BGGREEN = "\u001B[42m";
	public static final String ANSI_BGYELLOW = "\u001B[43m";
	public static final String ANSI_BGBLUE = "\u001B[44m";
	public static final String ANSI_BGMAGENTA = "\u001B[45m";
	public static final String ANSI_BGCYAN = "\u001B[46m";
	public static final String ANSI_BGWHITE = "\u001B[47m";
	
	public enum StringMatchTypes {
		EXACT,PREFIX,PARTIAL		// NOTE: do not change the order here!
	}

	public static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

	public enum TaxonRanks {
		NORANK("non-taxonomic rank",1000),CLASS("Class",50),FAMILY("Family",100),FORM("Form",240),GENUS("Genus",140),KINGDOM("Kingdom",10)
    	,ORDER("Order",80),PHYLUM("Phylum (Division)",30),SECTION("Section",160),SERIES("Series",180),SPECIES("Species",200),SUBCLASS("Subclass",60)
    	,SUBGENUS("Subgenus",150),SUBFAMILY("Subfamily",110),SUBFORM("Subform",250),SUBKINGDOM("Subkingdom",20),SUBORDER("Suborder",90)
    	,SUBPHYLUM("Subphylum (Subdivision)",40),SUBSECTION("Subsection",170),SUBSERIES("Subseries",190),SUBSPECIES("Subspecies",210)
    	,SUBTRIBE("Subtribe",130),SUBVARIETY("Subvariety",230),SUPERORDER("Superorder",70),TRIBE("Tribe",120),VARIETY("Variety",220);
    
		private final String name;
		private final Integer value;
    	TaxonRanks(String name,Integer value) {
			this.name=name;
			this.value=value;
		}
    	
    	public String getName() {
    		return this.name;
    	}
    	
    	public Integer getValue() {
    		return this.value;
    	}
    	
    	public static TaxonRanks getRankFromValue(Integer value) {
			if(value == null) return null;
    		for(TaxonRanks tr:values()) {
    			if(Objects.equals(tr.value, value)) return tr;
    		}
    		return null;
    	}
	}
	
	public enum Facets {	// supertypes of relationships
		TAXONOMY
		,ECOLOGY
		,OCCURRENCE
		,MORPHOLOGY
		,IMAGE
	}

	public enum PhenologicalStates {
		UNKNOWN((short)0),VEGETATIVE((short)1),FLOWER((short)2),DISPERSION((short)3),FLOWER_DISPERSION((short)4);
		
		private final Short code;
		PhenologicalStates (Short code) {
			this.code=code;
		}
		
		public Short getCode() {
			return code;
		}
		
    	public static PhenologicalStates getStateFromCode(Short code) {
    		for(PhenologicalStates tr:values()) {
    			if(Objects.equals(tr.code, code)) return tr;
    		}
    		return null;
    	}
	}
	
	
	/**************************************************************************
	 * STATUS OF TAXA IN TERRITORIES
	 * These status apply to the EXISTS_IN relationship and intend to describe
	 * the relationship of a taxon with a territory.
	 ***************************************************************************/
	
	// these two enums are only for the cases where we need a simple yes-or-no answer to the question!
	public enum Native_Exotic {NATIVE, EXOTIC}
	public enum Present_Absent {PRESENT, ABSENT}
	
	// Thanks to Estevão Portela-Pereira to all discussions and contributions to the *status!
	public enum NativeStatus {
		NATIVE((short)0, "NATIVE to", Native_Exotic.NATIVE, false)
		,ASSUMED_NATIVE((short)0, "ASSUMED to be NATIVE to", Native_Exotic.NATIVE, false)
		,DOUBTFULLY_NATIVE((short)1, "DOUBTFULLY NATIVE to", Native_Exotic.NATIVE, false)			// it might be native, but there are also reasons to suspect the opposite
		,NATIVE_REINTRODUCED((short)1, "NATIVE but REINTRODUCED to", Native_Exotic.EXOTIC, false)
		,CRYPTOGENIC((short)0, "CRYPTOGENIC in", Native_Exotic.EXOTIC, false)
		,DOUBTFULLY_EXOTIC((short)1, "DOUBTFULLY EXOTIC in", Native_Exotic.EXOTIC, false)		// it might be introduced, but there are also reasons to suspect the opposite
		,ASSUMED_EXOTIC((short)1, "ASSUMED to be EXOTIC in", Native_Exotic.EXOTIC, false)
		,EXOTIC((short)2, "EXOTIC in", Native_Exotic.EXOTIC, false)
		,EXOTIC_REINTRODUCED((short)2, "EXOTIC but REINTRODUCED in", Native_Exotic.EXOTIC, false)
		,NEAR_ENDEMIC((short)4, "NEAR ENDEMIC to", Native_Exotic.NATIVE, false)					// native and quasi-endemic (say, more than 80% of its native populations in the territory)
		,MULTIPLE_STATUS((short)0, "has multiple status in", null, true)						// it exists with different status depending on the sub-territory or on the sub-taxa
		,MULTIPLE_EXOTIC_STATUS((short)0, "has multiple exotic status in", Native_Exotic.EXOTIC, true)	// it exists with different status depending on the sub-territory or on the sub-taxa
		,MULTIPLE_NATIVE_STATUS((short)0, "has multiple native status in", Native_Exotic.NATIVE, true)	// it exists with different status depending on the sub-territory or on the sub-taxa
		,EXISTS((short)0, "EXISTS in", null, true)								// it exists but it is not possible to infer status
		,ERROR((short)-1, "ERROR", null, true)
		,NULL((short)-1, "ERROR", null, true);									// to signal when nothing is known
		
		private final Short code;
		private final String verbose;
		private final Native_Exotic nativeExotic;
		private final boolean readOnly;
		
		NativeStatus (Short code,String verbose, Native_Exotic nativeExotic, boolean readOnly) {
			this.code=code;
			this.verbose=verbose;
			this.nativeExotic=nativeExotic;
			this.readOnly = readOnly;
		}
		
		public Short getCode() {
			return code;
		}
		
    	public static NativeStatus fromCode(Short code) {
    		for(NativeStatus tr:values()) {
    			if(Objects.equals(tr.code, code)) return tr;
    		}
    		return NativeStatus.ERROR;
    	}

    	public boolean isNative() {
    		return this.nativeExotic == Native_Exotic.NATIVE;
    	}
    	
    	public boolean isReadOnly() {
    		return this.readOnly;
    	}
    	
    	public Native_Exotic getNativeOrExotic() {
    		return this.nativeExotic;
    	}
    	
    	public String toVerboseString() {
    		return this.verbose;
    	}

	}

	public enum OccurrenceStatus {		// this applies to the current status of the taxon in a given territory
		PRESENT(0, Present_Absent.PRESENT)						// Taxon is currently present
		,DOUBT_OVER_PRESENCE(2, Present_Absent.PRESENT)			// There is doubt over the presence of this taxon due to geographic issues (NOTE: doubt because of taxonomic issues is treated in another field)
		,POSSIBLE_OCCURRENCE(5, Present_Absent.ABSENT)			// Taxon might occur in the territory given its distribution and habitat elsewhere, but there is *no* evidence at all of its occurrence
		,ASSUMED_PRESENT(1, Present_Absent.PRESENT)				// Taxon has not been observed recently but there are past unequivocal evidences of its occurrence and there are no reasons to suppose that it might have gone extinct.
		,POSSIBLY_EXTINCT(3, Present_Absent.PRESENT)			// Taxon is possibly extinct: there are no recent observations, so we assume it may be extinct, but it might still exist, according to expert's opinion
		,EXTINCT(4, Present_Absent.ABSENT)						// Taxon is extinct: there are no recent observations, and it is very unlikely that it might still exist, according to expert's opinion
		,ABSENT_BUT_REPORTED_IN_ERROR(6, Present_Absent.ABSENT)	// Taxon is absent, but it has been erroneously reported earlier (e.g. because of mis-identifications)
		,ERROR(0, Present_Absent.ABSENT);						// Some error occurred
		
		private final Integer priority;
		private final Present_Absent presentAbsent;
		
		OccurrenceStatus(Integer priority, Present_Absent PA) {
			this.priority = priority;
			this.presentAbsent = PA;
		}
		
		public OccurrenceStatus merge(OccurrenceStatus o) {
			if(this.priority < o.priority)
				return this;
			else
				return o;
		}
		
		public boolean isPresent() {
			return this.presentAbsent == Present_Absent.PRESENT;
		}
	}

	public enum AbundanceLevel {
		NOT_SPECIFIED(1)
		,VERY_COMMON(0)
		,COMMON(1)
		,OCCASIONAL(2)
		,RARE(3)
		,VERY_RARE(4);						// Few populations with few individuals
		
		private final Integer priority;
		
		AbundanceLevel(Integer priority) {
			this.priority = priority;
		}
		
		public AbundanceLevel merge(AbundanceLevel o) {
			if(this.priority < o.priority)
				return this;
			else
				return o;
		}

	}

	public enum WorldNativeDistributionCompleteness {		// "Whether or not the plant-area records in the DB represent the complete world native distribution for the plant"
		DISTRIBUTION_COMPLETE
		,DISTRIBUTION_INCOMPLETE
		,NOT_KNOWN
	}
	
	// Thanks to Estevão Portela-Pereira to all discussions and contributions to the *status!
	public enum PlantIntroducedStatus {
		NOT_SPECIFIED(null)
		,APOPHYTE(Native_Exotic.NATIVE)			// native but human-dispersed, having a distribution larger than natural
		,ARCHAEOPHYTE(Native_Exotic.EXOTIC)
		,NEOPHYTE(Native_Exotic.EXOTIC)
		,DIAPHYTE(Native_Exotic.EXOTIC)			// casual, adventitious, nearly not established in nature yet
		,ARCHAEOAPOPHYTE(Native_Exotic.NATIVE)	// native but dispersed before 1500
		,NEOAPOPHYTE(Native_Exotic.NATIVE)		// native but dispersed after 1500
		,ARCHAEOEPECOPHYTE(Native_Exotic.EXOTIC)	// introduced before 1500 and living exclusively in ruderal communities
		,ARCHAEOAGRIOPHYTE(Native_Exotic.EXOTIC)	// introduced before 1500 and living in natural or semi-natural communities
		,NEOEPECOPHYTE(Native_Exotic.EXOTIC)		// introduced after 1500 and living exclusively in ruderal communities
		,NEOAGRIOPHYTE(Native_Exotic.EXOTIC)		// introduced after 1500 and living in natural or semi-natural communities
		,MULTIPLE_INTRODUCED_STATUS(null)
		,NOT_APPLICABLE(null);
		
		private final Native_Exotic nativeExotic;
		
		PlantIntroducedStatus (Native_Exotic ne) {
			this.nativeExotic = ne;
		}
		
		public Native_Exotic getNativeOrExotic() {
			return this.nativeExotic;
		}
		
		public PlantIntroducedStatus merge(PlantIntroducedStatus o) {
			if(this != o) return PlantIntroducedStatus.MULTIPLE_INTRODUCED_STATUS;
			return this;
		}
	}

	public enum PlantNaturalizationDegree {
		NOT_SPECIFIED(2)
		,CASUAL(5)
		,NATURALIZED_OCCASIONAL(4)
		,NATURALIZED_DANGEROUS(3)
		,INVASIVE(1)
		,TRANSFORMER(0)
		,NOT_APPLICABLE(-1);
		
		private final Integer priority;
		
		PlantNaturalizationDegree(Integer priority) {
			this.priority = priority;
		}
		
		public PlantNaturalizationDegree merge(PlantNaturalizationDegree o) {
			if(this.priority < o.priority)
				return this;
			else
				return o;
		}
	}
	
	//public static NativeStatus[] NativeStatuses=NativeStatus.getNatives().toArray(new NativeStatus[0]);	// the NativeStatus which are considered Native.
	public static Map<Facets,RelTypes[]> FacetRelTypes=new EnumMap<Facets,RelTypes[]>(Facets.class);
	public static Map<RelTypes,Facets> RelTypesFacet=new HashMap<RelTypes,Facets>();
	
	public enum TerritoryTypes {
		COUNTRY
		,GEOGRAPHIC_BOUNDARY
		,BIOGEOGRAPHIC_BOUNDARY
		,PROTECTED_AREA
		,ADMINISTRATIVE_BOUNDARY
		,NOT_SET
	}
	
	public enum DocumentType {VERTEX, EDGE, NONE}
	/**
	 * All types of relationships in the graph. These must correspond exactly to the collection names in the DB!
	 * @author miguel
	 *
	 */
	public enum RelTypes {
		PART_OF(Facets.TAXONOMY, PART_OF.class),								// TaxEnt PART_OF TaxEnt
		SYNONYM(Facets.TAXONOMY, pt.floraon.taxonomy.entities.SYNONYM.class),			// TaxEnt SYNONYM TaxEnt
		HYBRID_OF(Facets.TAXONOMY, pt.floraon.taxonomy.entities.HYBRID_OF.class),		// TaxEnt HYBRID_OF TaxEnt
		OBSERVED_IN(Facets.OCCURRENCE, pt.floraon.occurrences.entities.OBSERVED_IN.class),	// TaxEnt OBSERVED_IN SpeciesList
		//IDENTIFIED_AS(Facets.OCCURRENCE, pt.floraon.driver.entities.IDENTIFIED_AS.class),
		OBSERVED_BY(Facets.OCCURRENCE, pt.floraon.occurrences.entities.OBSERVED_BY.class),	// SpeciesList OBSERVED_BY Author
    	HAS_QUALITY(Facets.MORPHOLOGY, pt.floraon.morphology.entities.HAS_QUALITY.class),	// TaxEnt HAS_QUALITY Attribute
    	ATTRIBUTE_OF(Facets.TAXONOMY, pt.floraon.morphology.entities.ATTRIBUTE_OF.class),	// Attribute ATTRIBUTE_OF Character
		EXISTS_IN(Facets.OCCURRENCE, pt.floraon.taxonomy.entities.EXISTS_IN.class),		// TaxEnt EXISTS_IN Territory
		BELONGS_TO(Facets.TAXONOMY, pt.floraon.taxonomy.entities.BELONGS_TO.class);		// Territory BELONGS_TO Territory
    	//IMAGE_OF(Facets.IMAGE,pt.floraon.morphology.entities.ATTRIBUTE_OF.class);
		
		Facets facet;
		Class<? extends GeneralDBEdge> edgeClass;
		
		RelTypes(Facets facet,Class<? extends GeneralDBEdge> cls) {
			this.facet=facet;
			this.edgeClass=cls;
		}
		
		public Class<? extends GeneralDBEdge> getEdgeClass() {
			return this.edgeClass;
		}
		
		public Facets getFacet() {
			return this.facet;
		}

		public static RelTypes[] getRelTypesOfFacet(Facets facet) {
			List<RelTypes> out=new ArrayList<RelTypes>();
			for(RelTypes art:RelTypes.values()) {
				if(art.getFacet().equals(facet)) out.add(art);
			}
			return out.toArray(new RelTypes[0]);
		}

		public static RelTypes[] getRelTypesOfFacets(Facets[] facets) {
			List<Facets> fac=Arrays.asList(facets);
			List<RelTypes> out=new ArrayList<RelTypes>();
			for(RelTypes art:RelTypes.values()) {
				if(fac.contains(art.getFacet())) out.add(art);
			}
			return out.toArray(new RelTypes[0]);
		}

		public GeneralDBEdge getEdge() throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			Class<?>[] types = {};
			Constructor<?> constructor = this.edgeClass.getConstructor(types);
			Object[] parameters = {};
			return (GeneralDBEdge)constructor.newInstance(parameters);
		}

		public GeneralDBEdge getEdge(String from, String to) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			Class<?>[] types = {String.class, String.class};
			Constructor<?> constructor;
			constructor = this.edgeClass.getConstructor(types);
			Object[] parameters = {from, to};
			return (GeneralDBEdge)constructor.newInstance(parameters);
		}
	}

	public enum NodeTypes {
    	specieslist(SpeciesList.class),		// node representing a species inventory
    	taxent(TaxEnt.class),				// taxonomic node of any rank, or eventually without any formal rank (e.g. Anemone palmata (white-flowered form)
    	//entity,				// an instance of any taxonomic rank and optionally of any organ
    	author(Author.class),				// a data contributor
    	attribute(Attribute.class),			// a morphological attribute (e.g. red flower)
    	character(Character.class),			// a morphological character (e.g. flower color, isLeaf shape...)
    	image(pt.floraon.driver.entities.Image.class),
		user(User.class),					// a database user
    	territory(Territory.class);			// a geographic territory (e.g. country)
    	
    	Class<? extends GeneralDBNode> nodeClass;

    	NodeTypes(Class<? extends GeneralDBNode> cls) {
			this.nodeClass = cls;
		}
    	
    	public Class<? extends DBEntity> getNodeClass() {
    		return this.nodeClass;
    	}
	}

	static {
    	for(Entry<Facets,RelTypes[]> f:FacetRelTypes.entrySet()) {
    		for(RelTypes rt:f.getValue()) {
    			RelTypesFacet.put(rt,f.getKey());
    		}
    	}
    }
    
    public static String CHECKLISTFIELDS=
    		"{rank:"+TaxonRanks.SPECIES.getValue()+"}"
    		+ ",{rank:"+TaxonRanks.FORM.getValue()+"}"
			+ ",{rank:"+TaxonRanks.SUBSPECIES.getValue()+"}"
			+ ",{rank:"+TaxonRanks.VARIETY.getValue()+"}"
			+ ",{rank:"+TaxonRanks.GENUS.getValue()+"}"
			+ ",{rank:"+TaxonRanks.FAMILY.getValue()+"}"
			+ ",{rank:"+TaxonRanks.ORDER.getValue()+"}";

	public static Map<String,String> infraRanks;
    static {
    	infraRanks = new HashMap<String,String>();
        infraRanks.put("subspecies","subsp.");
        infraRanks.put("form","f.");
        infraRanks.put("variety","var.");
    }
    
    @SafeVarargs
	public static <E extends Enum<E>> String implode(String separator, E... data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length - 1; i++) {
            if (!data[i].toString().matches(" *")) {
                sb.append(data[i]);
                sb.append(separator);
            }
        }
        sb.append(data[data.length - 1].toString().trim());
        return sb.toString();
    }
    
    public static String implode(String separator, String... data) {
        StringBuilder sb = new StringBuilder();
        boolean any = false;
        for (int i = 0; i < data.length; i++) {
            if(data[i] != null && !data[i].matches(" *")) {
                sb.append(data[i]);
                any = true;
                if(i < data.length-1 && data[i+1] != null && !data[i+1].matches(" *")) sb.append(separator);
            }
        }
    	return any ? sb.toString() : null;
    }
    
    /**
     * Gets a Set with the names of the given nodes
     * @param nodeList
     * @return
     */
    public static <T extends NamedDBNode> Set<String> getNamesSet(Set<T> nodeList) {
		Iterator<T> it = nodeList.iterator();
		Set<String> out = new HashSet<String>();
		while(it.hasNext()) {
			out.add(it.next().getName());
		}
		return out;
    }
    
    public static <T extends GeneralDBNode> List<String> getIDsList(List<T> nodeList) {
		Iterator<T> it = nodeList.iterator();
		List<String> out = new ArrayList<String>();
		while(it.hasNext()) {
			out.add(it.next().getID());
		}
		return out;
    }
}
