package pt.floraon.results;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import pt.floraon.driver.Constants;
import pt.floraon.driver.Constants.AbundanceLevel;
import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.driver.Constants.Native_Exotic;
import pt.floraon.driver.Constants.OccurrenceStatus;
import pt.floraon.entities.Territory;

public class ListOfTerritoryStatus {
	protected List<TerritoryStatus> territoryStatusList;
	
	public ListOfTerritoryStatus(List<TerritoryStatus> list) {
		this.territoryStatusList = list;
	}

	public class InferredStatus {
		protected NativeStatus nativeStatus;
		protected OccurrenceStatus occurrenceStatus;
		protected AbundanceLevel abundanceLevel; 
		/**
		 * Status is assigned to a parent taxon. Should be read: it is not certain that it is this [sub-]taxon that exists in this territory,
		 * but if it exists, then it is with this nativeStatus.
		 */
		protected Boolean possibly;
		/**
		 * Whether this taxon has been identified with certainty or not
		 */
		protected Boolean uncertainOccurrence;
		/**
		 * Wheter it is endemic in this territory (this can be inferred from the endemism in sub-territories)
		 */
		protected Boolean endemic;
		/**
		 * The long name of the territory this Status pertains to
		 */
		protected String territoryName;
		
		@Deprecated
		protected InferredStatus(NativeStatus nativeStatus, OccurrenceStatus occurrenceStatus, Boolean uncertainOccurrence, Boolean possibly, Boolean endemic, String name) {
			this.nativeStatus = nativeStatus;
			try {
				this.occurrenceStatus = occurrenceStatus==null ? null : occurrenceStatus;
			} catch (IllegalArgumentException e) {		// NOTE: if constant is not found, assume it is PRESENT
				this.occurrenceStatus = OccurrenceStatus.PRESENT;
			}
			this.uncertainOccurrence=uncertainOccurrence;
			this.possibly=possibly;
			this.endemic=endemic;
			this.territoryName = name;
		}
		
		public InferredStatus(TerritoryStatus ts, Boolean endemic) {
			this.nativeStatus = ts.existsIn.getNativeStatus();
			this.abundanceLevel = ts.existsIn.getAbundanceLevel();
			this.occurrenceStatus = ts.existsIn.getOccurrenceStatus();
			this.territoryName = ts.territory.getName();
			this.uncertainOccurrence = ts.existsIn.isUncertainOccurrenceStatus();
			this.possibly = ts.edges.contains(Constants.RelTypes.PART_OF.toString())
				&& ts.direction.get(ts.edges.indexOf(Constants.RelTypes.PART_OF.toString())).equals("OUTBOUND");
			this.endemic = endemic;
		}
		
		public NativeStatus getNativeStatus() {
			return this.nativeStatus;
		}

		public OccurrenceStatus getOccurrenceStatus() {
			return this.occurrenceStatus;
		}

		public AbundanceLevel getAbundanceLevel() {
			return this.abundanceLevel;
		}

		public boolean getPossibly() {
			return this.possibly;
		}

		public boolean getUncertainOccurrence() {
			return this.uncertainOccurrence;
		}

		public boolean getIsEndemic() {
			return this.endemic;
		}

		public String getTerritoryName() {
			return this.territoryName;
		}
		
		public String getVerbatimNativeStatus() {
			StringBuilder sb = new StringBuilder();
			String tmp1=null, tmp2=null;
			if(this.endemic && (this.nativeStatus.isNativeOrExotic() == null || this.nativeStatus.isNativeOrExotic() == Native_Exotic.NATIVE))
				sb.append(this.nativeStatus.toString() + " (ENDEMIC)");
			else
				sb.append(this.nativeStatus.toString());
			if(this.possibly != null && this.possibly) tmp1="if it exists";
			if(this.uncertainOccurrence != null && this.uncertainOccurrence) tmp2="uncertain";
			tmp1 = Constants.implode(", ", tmp1, tmp2);
			if(tmp1 != null) sb.append(" (").append(tmp1).append(")");
			return sb.toString();
		}
	}

	/**
	 * Computes the status of this taxon in each territory marked for checklist, by summarizing all the branches that connect the taxon with the territory
	 * @return
	 */
	public Map<String,InferredStatus> computeTerritoryStatus(boolean worldDistributionComplete) {
		Map<String,InferredStatus> out=new HashMap<String,InferredStatus>();
		TerritoryStatus thisStatus, tmp;
		Iterator<TerritoryStatus> it;
		Set<Territory> endemismDegree = this.computeEndemismDegree();
		boolean isEndemic;

		// compile the territories marked for checklist
		Set<Territory> terr = new HashSet<Territory>();
		for(TerritoryStatus ts : this.territoryStatusList) {
			if(ts.territory.getShowInChecklist())
				terr.add(ts.territory);
		}
		
		// Now iterate over all statuses of this territory, to find the most rigorous and direct information, respecting a priority order:
		// 3: not inferred and certain
		// 2: inferred and certain
		// 1: not inferred and uncertain
		// 0: inferred and uncertain
		// When the priority is the same, check whether the statuses are different or equal. If different, assign a general/multiple status
		for(Territory territory : terr) {
			thisStatus = null;
			// first check if it is endemic to this territory or not
			if(worldDistributionComplete)
				isEndemic = isEndemicToTerritory(endemismDegree, territory.getShortName());
			else
				isEndemic = false;

			it = this.territoryStatusList.iterator();
			while(it.hasNext()) {
				tmp=it.next();
				if(!tmp.territory.getShortName().equals(territory.getShortName())) continue;
				if(thisStatus == null)
					thisStatus = tmp;
				else
					thisStatus = thisStatus.merge(tmp);
			}
			out.put(territory.getShortName(), new InferredStatus(thisStatus, isEndemic));
		}
		return out;
	}
	
	/**
	 * Checks if this TaxEnt is endemic to the given territory
	 * @param endemicTerritories
	 * @param territory
	 * @return
	 */
	private boolean isEndemicToTerritory(Set<Territory> endemicTerritories, String territoryShortName) {
		TerritoryStatus tmp;
		Iterator<TerritoryStatus> it;
		Boolean isEndemic = null, chk;
		// check if all the endemic territories are in the path of this territory
//		List<String> eDegs = this.computeEndemismDegreeName();
//		System.out.println("****\nChecking: "+territory);
//		System.out.println("Endemic to: "+Constants.implode(", ", eDegs.toArray(new String[0])));
		for(Territory terr : endemicTerritories) {
			it = this.territoryStatusList.iterator();
//			System.out.println("  Checking endemic terr: "+terr.getName());
			chk = false;
			while(it.hasNext()) {
				tmp = it.next();
				if(!tmp.territory.getShortName().equals(territoryShortName)) continue;
				// for each territory path that leads to the inquired territory
//				System.out.println(tmp.territory.getName()+": "+Constants.implode(", ", tmp.vertices.toArray(new String[0])));
				if(tmp.vertices.contains(terr.getName())) {
					// yes this endemic terr is on the path of the inquired territory
					chk = true;
					break;
				}
			}
			// it is only endemic to the inquired territory if all the endemic territories are in the path of the inquired territory:
			if(isEndemic == null) isEndemic = true;
			isEndemic &= chk;
			if(!isEndemic) break;
		}
		return isEndemic == null ? false : isEndemic;
	}

	/**
	 * Computes the status of this taxon for the given territory, by summarizing all the branches that connect the taxon with the territory
	 * @param territory The shortName of the {@link Territory}
	 * @param worldDistributionComplete
	 * @return
	 */
	public Map<String,InferredStatus> computeTerritoryStatus(String territory, boolean worldDistributionComplete) {
		Map<String,InferredStatus> out=new HashMap<String,InferredStatus>();
		TerritoryStatus thisStatus = null, tmp;
		Iterator<TerritoryStatus> it;

		// first check if it is endemic to this territory or not
		boolean isEndemic = false;
		if(worldDistributionComplete) {
			isEndemic = isEndemicToTerritory(this.computeEndemismDegree(), territory);
		}
		// Now iterate over all statuses of this territory, to find the most rigorous and direct information, respecting a priority order:
		// 3: not inferred and certain
		// 2: inferred and certain
		// 1: not inferred and uncertain
		// 0: inferred and uncertain
		// When the priority is the same, check whether the statuses are different or equal. If different, assign a general/multiple status
		it = this.territoryStatusList.iterator();
		while(it.hasNext()) {
			tmp=it.next();
			if(!tmp.territory.getShortName().equals(territory)) continue;
			if(thisStatus == null)
				thisStatus = tmp;
			else
				thisStatus = thisStatus.merge(tmp);
		}
		out.put(territory, new InferredStatus(thisStatus, isEndemic));
		return out;
	}
	
	/**
	 * Gets the smallest geographical area that comprises all native occurrences of this TaxEnt.
	 * This is usually an array of territories, as it does not perform aggregation.
	 * See Constants.NativeStatus
	 * @return
	 */
	public Set<Territory> computeEndemismDegree() {
		// NOTE: we assume here that the TaxEnt has the WorldDistributionCompleteness == COMPLETE !
		Set<Territory> out = new HashSet<Territory>();
		Set<String> terr = new HashSet<String>();
		Map<String,Integer> existsIn = new HashMap<String,Integer>();
		for(TerritoryStatus ts : this.territoryStatusList) {
			// exclude non-native statuses, we just want endemism here
			if(ts.existsIn.getNativeStatus().isNativeOrExotic() != Constants.Native_Exotic.NATIVE) continue;
			// compile the unique EXISTS_IN edges going out from this TaxEnt and the respective depth
			// each EXISTS_IN is a different territory route in the graph
			existsIn.put(ts.existsIn.getID(), ts.edges.indexOf(Constants.RelTypes.EXISTS_IN.toString()));
			// compile the base territories, i.e. those which have a native status directly assigned
			if(!ts.edges.contains(Constants.RelTypes.BELONGS_TO.toString())) terr.add(ts.territory.getName());		// only add direct territory assignements
		}
		if(existsIn.size() == 0) return Collections.emptySet();
		int minExistsInDepth = Collections.min(existsIn.values());
		
		Set<String> exclude = new HashSet<String>();
		// check, for each territory where the taxon is native, if it is contained in any of the others 
		for(Entry<String, Integer> ei : existsIn.entrySet()) {	// for each EXISTS_IN route
			if(!ei.getValue().equals(minExistsInDepth)) continue;	// the best inference is the minimum depth of EXISTS_IN
			
//			System.out.println("EI: "+ei.getKey());
			// for each EXISTS_IN route
			for(TerritoryStatus ts : this.territoryStatusList) {
				if(!ts.existsIn.getID().equals(ei.getKey()) || ts.vertices.size() <= 2) continue;
				// this is one EXISTS_IN route and we only want to test upstream territories (above the base territory)
//				System.out.println(Constants.implode(", ", ts.vertices.subList(2, ts.vertices.size()).toArray(new String[0]) ));
//				System.out.println(Constants.implode(", ", terr.toArray(new String[0])));
				if(!Collections.disjoint(ts.vertices.subList(2, ts.vertices.size()), terr)) exclude.add(ts.existsIn.getID());
			}
		}
//		String[] ex=exclude.toArray(new String[exclude.size()]);
//		System.out.println("Exclu: "+ Constants.implode(", ", ex));
		int min;
		TerritoryStatus mini;
		for(Entry<String, Integer> ei : existsIn.entrySet()) {	// for each EXISTS_IN route
			if(exclude.contains(ei.getKey())) continue;
			
			// for each EXISTS_IN route fetch the nearest territory
			min = 1000;
			mini = null;
			for(TerritoryStatus ts : this.territoryStatusList) {
				if(!ts.existsIn.getID().equals(ei.getKey())) continue;
				if(ts.edges.size() < min) {
					min = ts.edges.size();
					mini = ts;
				}
			}
			if(mini != null) out.add(mini.territory);
		}
		return out;
	}
	
	public Set<String> computeEndemismDegreeName() {
		Iterator<Territory> it = this.computeEndemismDegree().iterator();
		Set<String> out = new HashSet<String>();
		while(it.hasNext()) {
			out.add(it.next().getName());
		}
		return out;
	}
}