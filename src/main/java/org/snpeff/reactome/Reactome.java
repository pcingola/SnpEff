package org.snpeff.reactome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.snpeff.collections.AutoHashMap;
import org.snpeff.fileIterator.LineFileIterator;
import org.snpeff.gtex.Gtex;
import org.snpeff.gtex.GtexExperiment;
import org.snpeff.reactome.events.BlackBoxEvent;
import org.snpeff.reactome.events.CatalystActivity;
import org.snpeff.reactome.events.Complex;
import org.snpeff.reactome.events.Depolymerisation;
import org.snpeff.reactome.events.Event;
import org.snpeff.reactome.events.Pathway;
import org.snpeff.reactome.events.Polymerisation;
import org.snpeff.reactome.events.Reaction;
import org.snpeff.stats.CountByType;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Load reactome data from TXT files
 *
 * @author pcingola
 *
 */
public class Reactome implements Iterable<Entity> {

	public static final int SHOW_EVERY = 10000;
	public static final double EPSILON = 1E-6;
	public static final double MAX_CONVERGENCE_DIFFERENCE = 1E-3;
	public static final int MAX_ITERATIONS = 1000;

	boolean verbose = false;
	String dirName;
	HashMap<String, Entity> entityById;
	HashMap<String, String> objectType;
	HashMap<String, String> objectName;
	AutoHashMap<String, ArrayList<Entity>> entitiesByGeneId;
	HashSet<String> entitiesGeneId = new HashSet<String>();
	Monitor monitor; // Monitor all nodes in the circuit
	Monitor monitorTrace; // Monitor a specific set of nodes (usually one node and all it's predecessors)

	/**
	 * Find and return first Regexp occurrence (null if nothing is found)
	 * @param pattern
	 * @param str
	 * @return
	 */
	static String findRegexp(Pattern pattern, String str) {
		Matcher m = pattern.matcher(str);
		if (!m.find()) return null;
		return m.group();
	}

	static boolean hasRegexp(Pattern pattern, String str) {
		Matcher m = pattern.matcher(str);
		return m.find();
	}

	/**
	 * Main
	 * @param args
	 */
	public static void main(String[] args) {
		String reactomeDir = Gpr.HOME + "/snpEff/db/reactome/txt/";
		String geneIdsFile = Gpr.HOME + "/snpEff/db/reactome/gene_ids/biomart_query_uniq.txt";
		String gtexDir = Gpr.HOME + "/snpEff/db/GTEx";
		String gtexSamples = gtexDir + "/GTEx_Analysis_Annotations_Sample_DS__Pilot_2013_01_31.txt";
		String gtexData = gtexDir + "/gtex_norm.10.txt";

		// Load reactome data
		Log.info("Loading reactome data");
		Reactome reactome = new Reactome();
		reactome.setVerbose(true);
		reactome.load(reactomeDir, geneIdsFile);

		// Load GTEX data
		Log.info("Loading GTEx data");
		Gtex gtex = new Gtex();
		gtex.setVerbose(true);
		gtex.load(gtexSamples, gtexData);

		reactome.simplifyEntities();

		// Simulate
		Log.info("Running");
		reactome.run(gtex, null);

		// Save results
		String file = Gpr.HOME + "/circuit.txt";
		Log.info("Saving results to '" + file + "'");
		if (reactome.monitor != null) reactome.monitor.save(file);
		if (reactome.monitorTrace != null) reactome.monitorTrace.save(file);
	}

	public Reactome() {
		entityById = new HashMap<String, Entity>();
		entitiesByGeneId = new AutoHashMap<String, ArrayList<Entity>>(new ArrayList<Entity>());
	}

	/**
	 * Add an entity <-> geneId
	 * @param entity
	 * @param geneId
	 */
	public void add(Entity entity, String geneId) {
		String key = geneId + "\t" + entity.getId();
		if (entitiesGeneId.contains(key)) return; // Don't add more then once

		entity.addGeneId(geneId);
		entitiesByGeneId.getOrCreate(geneId).add(entity);
		entitiesGeneId.add(key);
	}

	/**
	 * Iterate network until convergence
	 *
	 * @param gtexExperiment
	 */
	boolean calc(GtexExperiment gtexExperiment) {
		boolean changed = true;
		int iteration;

		if (verbose) System.err.print(gtexExperiment.getTissueTypeDetail() + "\t");
		for (iteration = 0; changed && iteration < MAX_ITERATIONS; iteration++) {
			changed = false;
			HashSet<Entity> done = new HashSet<Entity>();

			for (Entity e : this) {
				double outPrev = e.getOutput();
				double out = e.calc(done);

				// Output changed?
				if (Math.abs(outPrev - out) > MAX_CONVERGENCE_DIFFERENCE) changed = true;
			}
			if (verbose) System.err.print(".");
		}
		if (verbose) Log.info(" " + iteration);

		return changed;
	}

	/**
	 * Create a monitor for all nodes in the circuit
	 */
	Monitor createMonitor() {
		Monitor monitor = new Monitor();
		for (Entity e : this) {
			if (!e.isFixed() && e.isReaction()) monitor.add(e);
		}

		monitor.sort();
		return monitor;
	}

	/**
	 * Create a monitor for a subset of nodes that explain "target's" output
	 */
	Monitor createMonitor(String targetNodeId) {
		// Perform 1 iteration to get a set of all nodes required for target's output
		reset();
		Entity target = entityById.get(targetNodeId);
		HashSet<Entity> done = new HashSet<Entity>();
		target.calc(done);

		Monitor monitor = new Monitor();
		for (Entity e : done)
			monitor.add(e);
		monitor.sort();

		return monitor;
	}

	Entity getEntity(int id) {
		return entityById.get(Integer.toString(id));
	}

	public Monitor getMonitor() {
		return monitor;
	}

	public Monitor getMonitorTrace() {
		return monitorTrace;
	}

	/**
	 * Get or create a new entity
	 * @param id
	 * @return
	 */
	Entity getOrCreateEntity(String id) {
		// Get from hash
		Entity e = entityById.get(id);
		if (e != null) return e;

		// Not available? Create entity
		String type = objectType.get(id);
		if (type == null) throw new RuntimeException("Cannot find entity type for ID '" + id + "'");
		String name = objectName.get(id);
		int idNum = Gpr.parseIntSafe(id);

		// Create according to object type
		if (type.equals("Complex")) e = new Complex(idNum, name);
		else if (type.equals("EntityCompartment") || type.equals("Compartment") || type.equals("GO_CellularComponent")) e = new Compartment(idNum, name);
		else if (type.equals("Reaction")) e = new Reaction(idNum, name);
		else if (type.equals("BlackBoxEvent")) e = new BlackBoxEvent(idNum, name);
		else if (type.equals("Pathway")) e = new Pathway(idNum, name);
		else if (type.equals("Depolymerisation")) e = new Depolymerisation(idNum, name);
		else if (type.equals("Polymerisation")) e = new Polymerisation(idNum, name);
		else if (type.equals("CatalystActivity")) e = new CatalystActivity(idNum, name);
		else e = new Entity(idNum, name);

		// Add to maps
		entityById.put(id, e);

		return e;
	}

	@Override
	public Iterator<Entity> iterator() {
		return entityById.values().iterator();
	}

	public void load(String dirName, String geneIdsFile) {
		this.dirName = dirName;
		if (verbose) Log.info("Loading Reactome data from directory '" + dirName + "'");

		loadDatabaseObjects(); // Load a map of all object names and types
		loadComplex2HasComponent(); // Load Complex_2_hasComponent
		loadPhysicalEntity2Compartment(); // Load compartment information
		loadPathway2HasEvent(); // Load pathway data
		loadReactionlikeEvent2Input(); // Load reaction inputs
		loadReactionlikeEvent2Output(); // Load reaction outputs
		loadReactionlikeEvent2CatalystActivity(); // Load reaction catalysts
		loadRegulation(); // Load reaction regulators
		loadCatalystActivity(); // Load catalyst

		// Remove cached data, we don't need it any more
		objectType = null;
		objectName = null;

		// Load Gene IDs data
		loadGeneIds(geneIdsFile);

		if (verbose) Log.info("Loading finished");
	}

	/**
	 * Load catalyst activity to molecule mapping
	 */
	protected void loadCatalystActivity() {
		String name = "CatalystActivity";
		String fileName = dirName + name + ".txt";
		if (verbose) Log.info("Loading " + name + " from '" + fileName + "'");

		int i = 1;
		for (String line : Gpr.readFile(fileName).split("\n")) {
			// Parse line
			String rec[] = line.split("\t");
			String id = rec[0];
			String entityId = rec[1];

			if (id.equals("DB_ID")) continue; // Skip title

			// Add event to pathway
			CatalystActivity reaction = (CatalystActivity) entityById.get(id);
			if (reaction == null) continue; // Reaction not found? Skip

			Entity e = getOrCreateEntity(entityId);
			reaction.addInput(e);

			if (verbose) Gpr.showMark(i++, SHOW_EVERY);
		}

		if (verbose) Log.info("");
		if (verbose) Log.info("Total catalyst entities assigned: " + (i - 1));
	}

	/**
	 * Load complexes
	 * @param name
	 * @param fileName
	 * @param map
	 */
	protected void loadComplex2HasComponent() {
		String fileName = dirName + "Complex_2_hasComponent.txt";
		if (verbose) Log.info("Loading Complex_2_hasComponent from '" + fileName + "'");

		int i = 1;
		for (String line : Gpr.readFile(fileName).split("\n")) {
			// Parse line
			String rec[] = line.split("\t");
			String id = rec[0];
			int idNum = Gpr.parseIntSafe(id);
			String componentId = rec[1];

			if (idNum == 0) continue; // Skip title

			// Get complex and add entity
			Complex c = (Complex) getOrCreateEntity(id);
			c.add(getOrCreateEntity(componentId));

			if (verbose) Gpr.showMark(i++, SHOW_EVERY);
		}

		if (verbose) Log.info("");
		if (verbose) Log.info("Total entities added: " + entityById.size());
	}

	/**
	 * Load objects table (populate objectType and objectName maps)
	 */
	protected void loadDatabaseObjects() {
		String fileName = dirName + "DatabaseObject.txt";
		if (verbose) Log.info("Loading objects from '" + fileName + "'");

		// Ensure capacity
		int numObjects = Gpr.countLines(fileName);
		if (verbose) Log.info("Counting lines from '" + fileName + "'. Total lines: " + numObjects);
		objectType = new HashMap<String, String>(numObjects);
		objectName = new HashMap<String, String>(numObjects);

		// Load objects
		int i = 1;
		LineFileIterator lfi = new LineFileIterator(fileName);
		for (String line : lfi) {
			// Parse line
			String recs[] = line.split("\t");
			if (recs.length < 3) continue;
			String id = recs[0];
			String objType = recs[1];
			String objName = recs[2];

			// Add info
			objectType.put(id, objType);
			objectName.put(id, objName);

			if (verbose) Gpr.showMark(i++, SHOW_EVERY);
		}

		if (verbose) Log.info("");
		if (verbose) Log.info("Total objects loaded: " + objectName.size());
	}

	/**
	 * Load Gene IDs data, then map geneIDs <-> Entities
	 *
	 * @param geneIdsFile
	 */
	public void loadGeneIds(String geneIdsFile) {
		//---
		// Load Gene IDs data
		//---
		if (verbose) Log.info("Loading Gene IDs from " + geneIdsFile);
		GeneIds geneIds = new GeneIds(geneIdsFile);

		// Assign to geneIDs
		Pattern patternEnsg = Pattern.compile("ENSG[0-9]*");
		Pattern patternEnst = Pattern.compile("ENST[0-9]*");
		Pattern patternRefSeqNm = Pattern.compile("NM_[0-9]*");
		Pattern patternRefSeqNp = Pattern.compile("NP_[0-9]*");

		// Find matching gene IDs for all entities
		int countMatched = 0, countUnMatched = 0;
		for (Entity e : this) {
			String name = e.getName();

			List<String> gids = null;
			String gname = null;

			// Try to match: From most specific to least specific
			if (hasRegexp(patternEnst, name)) {
				// Found ENSEMBL transcript
				gname = findRegexp(patternEnst, name);
				gids = geneIds.getId2tr().getIds(gname);
			} else if (hasRegexp(patternEnsg, name)) {
				// Found ENSEMBL gene ID
				gname = findRegexp(patternEnsg, name);
				gids = new LinkedList<String>();
				gids.add(gname);
			} else if (hasRegexp(patternRefSeqNm, name)) {
				// Found RefeSeq mRNA ID
				gname = findRegexp(patternRefSeqNm, name);
				gids = geneIds.getId2refseqId().getIds(gname);
			} else if (hasRegexp(patternRefSeqNp, name)) {
				// Found RefeSeq protein ID
				gname = findRegexp(patternRefSeqNp, name);
				gids = geneIds.getId2refseqProtId().getIds(gname);
			} else {
				// May be it's gene name followed by some other stuff
				gname = name.split(" ")[0];
				gids = geneIds.getId2geneName().getIds(gname);
			}

			// Found any GeneIDs
			if (gids != null) {
				countMatched++;

				// Add all gene IDs
				for (String gid : gids)
					add(e, gid);

			} else countUnMatched++;

		}

		if (verbose) Log.info("Done. Entities matched to geneIDs:" + countMatched + " / " + countUnMatched);
	}

	/**
	 * Load a two-column file into a Hash
	 * @param name
	 * @param fileName
	 * @param map
	 */
	protected void loadMap(String name, String fileName, HashMap<String, String> map) {
		if (verbose) Log.info("Loading " + name + " from '" + fileName + "'");

		int i = 1;
		for (String line : Gpr.readFile(fileName).split("\n")) {
			String rec[] = line.split("\t");
			String id = rec[0];
			String componentId = rec[1];
			map.put(id, componentId);
			if (verbose) Gpr.showMark(i++, SHOW_EVERY);
		}

		if (verbose) Log.info("");
		if (verbose) Log.info("Total objects loaded: " + map.size());
	}

	/**
	 * Load pathway events
	 * @param name
	 * @param fileName
	 * @param map
	 */
	protected void loadPathway2HasEvent() {
		String name = "Pathway_2_hasEvent";
		String fileName = dirName + name + ".txt";
		if (verbose) Log.info("Loading " + name + " from '" + fileName + "'");

		int i = 1;
		for (String line : Gpr.readFile(fileName).split("\n")) {
			// Parse line
			String rec[] = line.split("\t");
			String id = rec[0];
			String eventId = rec[1];

			if (id.equals("DB_ID")) continue; // Skip title

			// Add event to pathway
			Pathway pathway = (Pathway) getOrCreateEntity(id);
			Event event = (Event) getOrCreateEntity(eventId);
			pathway.add(event);

			if (verbose) Gpr.showMark(i++, SHOW_EVERY);
		}

		if (verbose) Log.info("");
		if (verbose) Log.info("Total events assigned: " + (i - 1));
	}

	/**
	 * Load compartment information
	 * @param name
	 * @param fileName
	 * @param map
	 */
	protected void loadPhysicalEntity2Compartment() {
		String name = "PhysicalEntity_2_compartment";
		String fileName = dirName + name + ".txt";
		if (verbose) Log.info("Loading " + name + " from '" + fileName + "'");

		int i = 1;
		for (String line : Gpr.readFile(fileName).split("\n")) {
			// Parse line
			String rec[] = line.split("\t");
			String id = rec[0];
			String compartmentId = rec[1];

			if (id.equals("DB_ID")) continue; // Skip title

			// Get entity & compartment
			Entity e = getOrCreateEntity(id);
			Compartment compartment = (Compartment) getOrCreateEntity(compartmentId);

			// Assign compartment (if not already assigned)
			if (e.getCompartment() != null) throw new RuntimeException("Compartment already assigned for entity: " + e);
			e.setCompartment(compartment);

			if (verbose) Gpr.showMark(i++, SHOW_EVERY);
		}

		if (verbose) Log.info("");
		if (verbose) Log.info("Total compartments assigned: " + (i - 1));
	}

	/**
	 * Load reaction catalyst
	 * @param name
	 * @param fileName
	 * @param map
	 */
	protected void loadReactionlikeEvent2CatalystActivity() {
		String name = "ReactionlikeEvent_2_catalystActivity";
		String fileName = dirName + name + ".txt";
		if (verbose) Log.info("Loading " + name + " from '" + fileName + "'");

		int i = 1;
		for (String line : Gpr.readFile(fileName).split("\n")) {
			// Parse line
			String rec[] = line.split("\t");
			String id = rec[0];
			String catalystId = rec[1];

			if (id.equals("DB_ID")) continue; // Skip title

			// Add event to pathway
			Reaction reaction = (Reaction) entityById.get(id);
			if (reaction == null) continue; // Reaction not found? Skip

			Entity e = getOrCreateEntity(catalystId);
			reaction.addCatalyst(e);

			if (verbose) Gpr.showMark(i++, SHOW_EVERY);
		}

		if (verbose) Log.info("");
		if (verbose) Log.info("Total outputs assigned: " + (i - 1));
	}

	/**
	 * Load reaction inputs
	 * @param name
	 * @param fileName
	 * @param map
	 */
	protected void loadReactionlikeEvent2Input() {
		String name = "ReactionlikeEvent_2_input";
		String fileName = dirName + name + ".txt";
		if (verbose) Log.info("Loading " + name + " from '" + fileName + "'");

		int i = 1;
		for (String line : Gpr.readFile(fileName).split("\n")) {
			// Parse line
			String rec[] = line.split("\t");
			String id = rec[0];
			String inputId = rec[1];

			if (id.equals("DB_ID")) continue; // Skip title

			// Add event to pathway
			Reaction reaction = (Reaction) entityById.get(id);
			if (reaction == null) continue; // Reaction not found? Skip

			Entity e = getOrCreateEntity(inputId);
			reaction.addInput(e);

			if (verbose) Gpr.showMark(i++, SHOW_EVERY);
		}

		if (verbose) Log.info("");
		if (verbose) Log.info("Total inputs assigned: " + (i - 1));
	}

	/**
	 * Load reaction outputs
	 * @param name
	 * @param fileName
	 * @param map
	 */
	protected void loadReactionlikeEvent2Output() {
		String name = "ReactionlikeEvent_2_output";
		String fileName = dirName + name + ".txt";
		if (verbose) Log.info("Loading " + name + " from '" + fileName + "'");

		int i = 1;
		for (String line : Gpr.readFile(fileName).split("\n")) {
			// Parse line
			String rec[] = line.split("\t");
			String id = rec[0];
			String outputId = rec[1];

			if (id.equals("DB_ID")) continue; // Skip title

			// Add event to pathway
			Reaction reaction = (Reaction) entityById.get(id);
			if (reaction == null) continue; // Reaction not found? Skip

			Entity e = getOrCreateEntity(outputId);
			reaction.addOutput(e);

			if (verbose) Gpr.showMark(i++, SHOW_EVERY);
		}

		if (verbose) Log.info("");
		if (verbose) Log.info("Total outputs assigned: " + (i - 1));
	}

	/**
	 * Load reaction regulation
	 * @param name
	 * @param fileName
	 * @param map
	 */
	protected void loadRegulation() {
		String name = "Regulation";
		String fileName = dirName + name + ".txt";
		if (verbose) Log.info("Loading " + name + " from '" + fileName + "'");

		int i = 1;
		for (String line : Gpr.readFile(fileName).split("\n")) {
			// Parse line
			String rec[] = line.split("\t");
			String id = rec[0];
			String regulatedEntityId = rec[1];
			String regulatorId = rec[2];

			if (id.equals("DB_ID")) continue; // Skip title

			// Add event to pathway
			//			Log.debug("Adding:\tregulatedEntityId: " + regulatedEntityId + "\t" + objectType.get(regulatedEntityId));
			Reaction reaction = (Reaction) entityById.get(regulatedEntityId);
			if (reaction == null) continue; // Reaction not found? Skip

			Entity e = getOrCreateEntity(regulatorId);
			reaction.addRegulator(e, objectType.get(id));

			if (verbose) Gpr.showMark(i++, SHOW_EVERY);
		}

		if (verbose) Log.info("");
		if (verbose) Log.info("Total regulations assigned: " + (i - 1));
	}

	/**
	 * Reset all nodes in the circuit
	 */
	public void reset() {
		for (Entity e : this)
			e.reset();
	}

	/**
	 * Run all experiments on gtex
	 * @param gtex
	 * @return
	 */
	public boolean run(Gtex gtex, String nameMatch) {
		boolean ok = true;

		for (GtexExperiment gtexExperiment : gtex) {
			if ((gtexExperiment.size() > 0) // Do we have data for this experiment?
					&& ((nameMatch == null) || gtexExperiment.getTissueTypeDetail().toLowerCase().indexOf(nameMatch.toLowerCase()) >= 0) // Does the name match (if any)
			) run(gtexExperiment);
		}

		return ok;
	}

	/**
	 * Run some simulations
	 * @param gtex
	 * @param gtexExperiment
	 */
	public boolean run(GtexExperiment gtexExperiment) {
		// Initialize
		if (monitor == null) monitor = createMonitor(); // Create monitor if needed
		reset(); // Reset previous values
		setInputs(gtexExperiment); // Set input nodes (fixed outputs from GTEx values)
		scaleWeights(); // Scale weights

		// Calculate circuit
		calc(gtexExperiment);

		// Add results to monitors
		String experimentLabel = gtexExperiment.getTissueTypeDetail();
		if (monitor != null) monitor.addResults(experimentLabel);
		if (monitorTrace != null) monitorTrace.addResults(experimentLabel);

		return true;
	}

	/**
	 * Scale weights
	 */
	void scaleWeights() {
		for (Entity e : this)
			if (e.isReaction()) ((Reaction) e).scaleWeights();
	}

	/**
	 * Set input nodes (fixed outputs from GTEx values)
	 * @param gtex
	 */
	void setInputs(GtexExperiment gtexExperiment) {
		Gtex gtex = gtexExperiment.getGtex();

		for (String gid : gtex.getGeneIds()) {
			List<Entity> entities = entitiesByGeneId.get(gid);

			if (entities != null) {
				double value = gtexExperiment.getValue(gid);
				if (!Double.isNaN(value)) {
					for (Entity e : entities)
						e.setFixedOutput(value);
				}
			}
		}
	}

	public void setMonitorTrace(Monitor monitorTrace) {
		this.monitorTrace = monitorTrace;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Simplify: Removes entities that are not reachable from any 'gene' entity
	 */
	void simplifyEntities() {
		if (verbose) Log.info("Simplify: Removing unnecesary nodes.");

		//---
		// Select entities to keep or delete
		//---

		// Entities to keep (all other entities will be deleted)
		HashSet<Entity> keep = new HashSet<Entity>();

		// Create a set of all genes
		HashSet<Entity> genes = new HashSet<Entity>();
		for (List<Entity> entities : entitiesByGeneId.values())
			genes.addAll(entities);

		// Analyze each entity
		for (Entity e : entityById.values()) {
			// Calculate
			HashSet<Entity> done = new HashSet<Entity>();
			e.calc(done);

			// Is any of the calculated entities a gene?
			boolean ok = genes.contains(e);
			for (Entity ee : done)
				ok |= genes.contains(ee);

			// OK? Keep all these entities
			if (ok) keep.addAll(done);
		}

		// Entities to delete
		HashSet<Entity> toDelete = new HashSet<Entity>();
		for (Entity e : entityById.values())
			if (!keep.contains(e)) toDelete.add(e);

		//---
		// Delete entities
		//---
		int deleted = 0;
		for (Entity e : toDelete) {
			String id = "" + e.getId();
			if (entityById.remove(id) != null) deleted++;
		}

		// Done
		if (verbose) Log.info("Simplify: done." //
				+ "\n\tGenes              : " + genes.size() //
				+ "\n\tEntities deleted   : " + deleted //
				+ "\n\tEntities remaining : " + entityById.size() //
		);
	}

	@Override
	public String toString() {
		CountByType countByType = new CountByType();

		for (Entity e : entityById.values())
			countByType.inc(e.getClass().getSimpleName());

		return countByType.toString();
	}

	/**
	 * Show details
	 * @return
	 */
	public String toStringDetails() {
		StringBuilder sb = new StringBuilder();

		for (Entity e : entityById.values())
			sb.append(e + "\n");

		return sb.toString();
	}
}
