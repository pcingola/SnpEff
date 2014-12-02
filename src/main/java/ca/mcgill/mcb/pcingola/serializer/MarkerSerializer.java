package ca.mcgill.mcb.pcingola.serializer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

import ca.mcgill.mcb.pcingola.fileIterator.LineFileIterator;
import ca.mcgill.mcb.pcingola.interval.Cds;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.MarkerParentId;
import ca.mcgill.mcb.pcingola.interval.MarkerSeq;
import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.interval.Motif;
import ca.mcgill.mcb.pcingola.interval.NextProt;
import ca.mcgill.mcb.pcingola.interval.RareAminoAcid;
import ca.mcgill.mcb.pcingola.interval.SpliceSiteAcceptor;
import ca.mcgill.mcb.pcingola.interval.SpliceSiteBranch;
import ca.mcgill.mcb.pcingola.interval.SpliceSiteBranchU12;
import ca.mcgill.mcb.pcingola.interval.SpliceSiteDonor;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Utr3prime;
import ca.mcgill.mcb.pcingola.interval.Utr5prime;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Serialize markers to (and from) file
 *
 * Note: Marker's children are serialized first (e.g. a transcript get all
 * 		exons serialized first).
 *
 * Note: Since Marker is a tree-like structure, we first load all the markers and then
 * 		assign parents. Markers are assigned a fake parent object (MarkerParentId)
 * 		which is later replaced by the 'real' parent.
 *
 * Note: All 'IDs' used have not meaning outside this serialization process.
 *
 * @author pcingola
 */
public class MarkerSerializer {

	PrintStream outFile;
	int lineNum;
	String line;
	int parsedField;
	String fields[];
	int currId = 0;
	HashMap<Integer, TxtSerializable> byId;
	HashMap<TxtSerializable, Integer> byMarker;

	public MarkerSerializer() {
		byId = new HashMap<Integer, TxtSerializable>();
		byMarker = new HashMap<TxtSerializable, Integer>();
	}

	protected TxtSerializable getById(int id) {
		return byId.get(id);
	}

	public int getIdByMarker(Marker m) {
		Integer id = byMarker.get(m);
		if (id == null) { throw new RuntimeException("Marker has no numeric ID. \n" //
				+ "\tClass    : " + m.getClass().getSimpleName() + "\n" //
				+ "\tMarker ID: '" + m.getId() + "'\n" //
				+ "\t" + m); }
		return id;
	}

	protected Marker getMarkerById(int id) {
		return (Marker) getById(id);
	}

	public String getNextField() {
		if (fields.length <= parsedField) return "";
		return fields[parsedField++];
	}

	public boolean getNextFieldBoolean() {
		return Gpr.parseBoolSafe(getNextField());
	}

	public int getNextFieldInt() {
		return Gpr.parseIntSafe(getNextField());
	}

	public Marker getNextFieldMarker() {
		return getMarkerById(getNextFieldInt());
	}

	public Markers getNextFieldMarkers() {
		Markers markers = new Markers();
		String fieldIdsStr = getNextField();
		if (fieldIdsStr.isEmpty()) return markers;

		String fieldIds[] = fieldIdsStr.split(",");
		for (String idStr : fieldIds) {
			int id = Gpr.parseIntSafe(idStr);
			Marker m = getMarkerById(id);
			if (m != null) markers.add(m);
			else throw new RuntimeException("Marker '" + id + "' not found. This should never happen!");
		}
		return markers;
	}

	protected int getNextId() {
		return ++currId;
	}

	/**
	 * Load data from file
	 */
	public Markers load(String fileName) {
		//---
		// Load data from file
		//---
		LineFileIterator lfi = new LineFileIterator(fileName, true); // File is gzipped
		int lineNum = 0;
		for (String l : lfi) {
			line = l;

			if (lineNum == 0) {
				// First line should be 'header' showing version number
				String fields[] = line.split("\t");
				if (fields.length > 1) {
					String soft = fields[0];
					String versionNumber = fields[1];

					// Check for compatibility
					if (!soft.equals(SnpEff.SOFTWARE_NAME)) throw new RuntimeException("Database file '" + fileName + "' is not compatible with this program version. Try installing the appropriate database.");
					if (!versionNumber.equals(SnpEff.VERSION_MAJOR)) throw new RuntimeException("Database file '" + fileName + "' is not compatible with this program version:"//
							+ "\n\tDatabase version : '" + versionNumber + "'"//
							+ "\n\tProgram version  : '" + SnpEff.VERSION_MAJOR + "'" //
							+ "\nTry installing the appropriate database." //
					);
				}
			} else {
				parsedField = 0;
				fields = line.split("\t", -1);

				// Parse field type
				String typeStr = fields[0];
				EffectType type = EffectType.valueOf(typeStr);

				// Parse serialization id
				String idStr = fields[1];
				int id = Gpr.parseIntSafe(idStr);

				Marker m = null;
				switch (type) {
				case GENOME:
					m = new Genome();
					break;
				case CHROMOSOME:
					m = new Chromosome();
					break;
				case SEQUENCE:
					m = new MarkerSeq();
					break;
				case GENE:
					m = new Gene();
					break;
				case TRANSCRIPT:
					m = new Transcript();
					break;
				case CDS:
					m = new Cds();
					break;
				case EXON:
					m = new Exon();
					break;
				case UTR_3_PRIME:
					m = new Utr3prime();
					break;
				case UTR_5_PRIME:
					m = new Utr5prime();
					break;
				case RARE_AMINO_ACID:
					m = new RareAminoAcid();
					break;
				case SPLICE_SITE_ACCEPTOR:
					m = new SpliceSiteAcceptor();
					break;
				case SPLICE_SITE_BRANCH:
					m = new SpliceSiteBranch();
					break;
				case SPLICE_SITE_BRANCH_U12:
					m = new SpliceSiteBranchU12();
					break;
				case SPLICE_SITE_DONOR:
					m = new SpliceSiteDonor();
					break;
				case NEXT_PROT:
					m = new NextProt();
					break;
				case MOTIF:
					m = new Motif();
					break;

				default:
					throw new RuntimeException("Unimplemented for type '" + type + "'");
				}

				try {
					// Parse line
					m.serializeParse(this);
				} catch (Throwable t) {
					t.printStackTrace();
					throw new RuntimeException("Error parsing line " + (lineNum + 1) + " from file '" + fileName + "'\n\t" + line + "\n\tField [" + parsedField + "] : '" + (parsedField < fields.length ? fields[parsedField] : "-") + "'", t);
				}

				// Add to hash
				byId.put(id, m);
			}

			lineNum++;
		}

		//---
		// Assign parents
		//---
		Markers markers = new Markers();
		for (TxtSerializable tm : byId.values()) {
			if (tm instanceof Marker) {
				Marker m = (Marker) tm;
				// Find parent ID
				MarkerParentId mpid = (MarkerParentId) m.getParent();
				int parentId = mpid.getParentId();

				// Find and set parent
				Marker parent = getMarkerById(parentId);
				m.setParent(parent);

				// Add to markers
				markers.add(m);
			}
		}

		return markers;
	}

	/**
	 * Save all markers
	 */
	public String save(Iterable<Marker> markersCollection) {
		StringBuilder idStr = new StringBuilder();
		for (Marker m : markersCollection) {
			int id = save(m);
			if (idStr.length() > 0) idStr.append(",");
			idStr.append(id);
		}
		return idStr.toString();
	}

	/**
	 * Save a marker
	 */
	public int save(Marker m) {
		if (m == null) return -1;
		if (byMarker.containsKey(m)) return byMarker.get(m); // Already done

		// Store already saved IDs
		int id = getNextId();
		byMarker.put(m, id);

		// Print line
		String line = m.serializeSave(this);
		outFile.print(line + "\n");
		lineNum++;

		return id;
	}

	/**
	 * Save data to file
	 */
	public void save(String fileName, Markers markers) {
		try {
			lineNum = 0;
			currId = 0;
			outFile = new PrintStream(new GZIPOutputStream(new FileOutputStream(fileName)));

			// Write header first
			outFile.print(SnpEff.SOFTWARE_NAME + "\t" + SnpEff.VERSION_MAJOR + "\n");

			// Serialize all markers
			for (Marker m : markers)
				save(m);

			outFile.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
