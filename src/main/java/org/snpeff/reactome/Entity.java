package org.snpeff.reactome;

import java.util.Collection;
import java.util.HashSet;

import org.snpeff.util.Gpr;

/**
 * A reactome basic entity (e.g. anything in reactome database derives fro this object)
 * 
 * @author pcingola
 *
 */
public class Entity implements Comparable<Entity> {

	public enum TransferFunction {
		LINEAR, TANH, SIGM, SIGM_PLUS_MINUS
	};

	public static boolean debug = false;
	public static TransferFunction TRANSFER_FUNCTION = TransferFunction.SIGM_PLUS_MINUS;
	public static double BETA = 3.0; // Note: Betas of less than 2.2 will make output shrink to zero, because iterated functions tend to zero f(f(f(....f(x)...))) -> 0

	protected int id; // Entity ID
	protected String name; // Entity Name
	protected Compartment compartment; // In which cell's compartment is this entity located?
	protected double output; // Entity output value
	protected double weight; // Weight applied to this entity (NaN means not available)
	protected double fixedOutput; // Fixed output value (external information)
	protected HashSet<String> geneIds; // All gene IDs related to this entity

	public Entity(int id, String name) {
		this.id = id;
		this.name = name;
		reset();
	}

	/**
	 * Add a geneId
	 * @param geneId
	 */
	public void addGeneId(String geneId) {
		if (geneIds == null) geneIds = new HashSet<String>();
		geneIds.add(geneId);
	}

	public double calc() {
		return calc(new HashSet<Entity>());
	}

	/**
	 * Calculate entities.
	 * Make sure we don't calculate twice (keep 'doneEntities' set up to date)
	 * 
	 * @param doneEntities
	 * @return
	 */
	public double calc(HashSet<Entity> doneEntities) {
		// Make sure we don't calculate twice
		if (doneEntities.contains(this)) return output;
		doneEntities.add(this); // Keep 'entities' set up to date

		if (!Double.isNaN(fixedOutput)) output = fixedOutput;
		else output = getWeight(); // Calculate output

		if (debug) System.out.println(output + "\tfixed:" + isFixed() + "\tid:" + id + "\ttype:" + getClass().getSimpleName() + "\tname:" + name);
		return output;
	}

	@Override
	public int compareTo(Entity e) {
		int cmp = getName().compareTo(e.getName());
		if (cmp != 0) return cmp;
		return getId() - e.getId();
	}

	public Compartment getCompartment() {
		return compartment;
	}

	public Collection<String> getGeneIds() {
		return geneIds;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public double getOutput() {
		return output;
	}

	public double getWeight() {
		return weight;
	}

	public boolean hasOutput() {
		return !Double.isNaN(output);
	}

	public boolean isFixed() {
		return !Double.isNaN(fixedOutput);
	}

	public boolean isReaction() {
		return false;
	}

	public void reset() {
		output = 0; // Entity output value
		fixedOutput = Double.NaN; // Fixed output value (external information)
		weight = Double.NaN; // Weight
	}

	public void setCompartment(Compartment compartment) {
		this.compartment = compartment;
	}

	public void setFixedOutput(double fixedOutput) {
		this.fixedOutput = fixedOutput;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return toString(0, new HashSet<Entity>());
	}

	public String toString(int tabs, HashSet<Entity> done) {
		done.add(this);
		return Gpr.tabs(tabs) + getClass().getSimpleName() + "[" + id + "]: " + name;
	}

	public String toStringSimple() {
		return getClass().getSimpleName() + "[" + id + "]: " + name;
	}

	/**
	 * Transfer function
	 * @param x
	 * @return
	 */
	protected double transferFunction(double x) {
		switch (TRANSFER_FUNCTION) {
		case SIGM_PLUS_MINUS:
			return 2.0 / (1.0 + Math.exp(-BETA * x)) - 1;
		case LINEAR:
			return x;
		case SIGM:
			return 1.0 / (1.0 + Math.exp(-BETA * x));
		case TANH:
			return Math.tanh(BETA * x);
		default:
			throw new RuntimeException("Unimplemented transfer function: " + TRANSFER_FUNCTION);
		}
	}

}
