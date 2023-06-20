package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.reactome.Entity;
import org.snpeff.reactome.Entity.TransferFunction;
import org.snpeff.reactome.events.Reaction;
import org.snpeff.reactome.events.Reaction.RegulationType;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test Reactome circuits
 *
 * @author pcingola
 */
public class TestCasesReactome {

	public static boolean debug = false;
	public static boolean verbose = true;
	public static int SHOW_EVERY = 10;

	public TestCasesReactome() {
		super();
	}

	/**
	 * Reaction with two molecules
	 */
	@Test
	public void test_01() {
		Log.debug("Test");
		int id = 1;
		Entity e1 = new Entity(id++, "input_1");
		Entity e2 = new Entity(id++, "input_2");

		Reaction r = new Reaction(id++, "reaction_1");
		r.addInput(e1);
		r.addInput(e2);

		e1.setFixedOutput(0.4);
		e2.setFixedOutput(0.6);

		Entity.TRANSFER_FUNCTION = TransferFunction.SIGM_PLUS_MINUS;
		Entity.BETA = 3.0;

		double out = r.calc();
		Log.debug("Out: " + out);
		assertEquals(0.9051482536448667, out);
	}

	/**
	 * Reaction with a catalyst
	 */
	@Test
	public void test_02() {
		Log.debug("Test");
		int id = 1;
		Entity e1 = new Entity(id++, "input_1");
		Entity e2 = new Entity(id++, "input_2");
		Entity cat = new Entity(id++, "catalyst");

		Reaction r = new Reaction(id++, "reaction_1");
		r.addInput(e1);
		r.addInput(e2);
		r.addCatalyst(cat);

		e1.setFixedOutput(0.4);
		e2.setFixedOutput(0.6);
		cat.setWeight(-1.0);

		Entity.TRANSFER_FUNCTION = TransferFunction.SIGM_PLUS_MINUS;

		double out = r.calc();
		Log.debug("Out: " + out);
		assertEquals(-0.8192933610763514, out);

		cat.setWeight(0.5);
		out = r.calc();
		Log.debug("Out: " + out);
		assertEquals(2.1152011710898737, out);
	}

	/**
	 * Reaction with positive regulation
	 */
	@Test
	public void test_03() {
		Log.debug("Test");
		int id = 1;
		Entity e1 = new Entity(id++, "input_1");
		Entity e2 = new Entity(id++, "input_2");
		Entity reg = new Entity(id++, "catalyst");

		Reaction r = new Reaction(id++, "reaction_1");
		r.addInput(e1);
		r.addInput(e2);
		r.addRegulator(reg, RegulationType.PositiveRegulation);

		e1.setFixedOutput(0.2);
		e2.setFixedOutput(0.5);
		reg.setFixedOutput(-1.0);

		double out = r.calc();
		Log.debug("Out: " + out);
		assertEquals(0.86631007995171, out);

		reg.setFixedOutput(0.37);
		out = r.calc();
		Log.debug("Out: " + out);
		assertEquals(2.1219547901144384, out);
	}

	/**
	 * Reaction with negative regulation
	 */
	@Test
	public void test_04() {
		Log.debug("Test");
		int id = 1;
		Entity e1 = new Entity(id++, "input_1");
		Entity e2 = new Entity(id++, "input_2");
		Entity reg = new Entity(id++, "catalyst");

		Reaction r = new Reaction(id++, "reaction_1");
		r.addInput(e1);
		r.addInput(e2);
		r.addRegulator(reg, RegulationType.NegativeRegulation);

		e1.setFixedOutput(0.2);
		e2.setFixedOutput(0.5);
		reg.setFixedOutput(-1.0);

		double out = r.calc();
		Log.debug("Out: " + out);
		assertEquals(0.6973026352658382, out);

		reg.setFixedOutput(0.37);
		out = r.calc();
		Log.debug("Out: " + out);
		assertEquals(-0.5583420748968899, out);
	}

	/**
	 * Reaction with requirement
	 */
	@Test
	public void test_05() {
		Log.debug("Test");
		int id = 1;
		Entity e1 = new Entity(id++, "input_1");
		Entity e2 = new Entity(id++, "input_2");
		Entity reg = new Entity(id++, "catalyst");

		Reaction r = new Reaction(id++, "reaction_1");
		r.addInput(e1);
		r.addInput(e2);
		r.addRegulator(reg, RegulationType.Requirement);

		e1.setFixedOutput(0.2);
		e2.setFixedOutput(0.5);
		reg.setFixedOutput(-1.0);

		double out = r.calc();
		Log.debug("Out: " + out);
		assertEquals(-0.9154962776570641, out);

		reg.setFixedOutput(0.37);
		out = r.calc();
		Log.debug("Out: " + out);
		assertEquals(0.34014843250566407, out);
	}

}
