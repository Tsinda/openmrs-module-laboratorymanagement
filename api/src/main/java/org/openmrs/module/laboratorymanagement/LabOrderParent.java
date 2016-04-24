package org.openmrs.module.laboratorymanagement;

import java.util.List;

import org.openmrs.Concept;

public class LabOrderParent {
	
	private Concept grandFatherConcept;
	
	private String grandFatherConceptName;
	
	private List<LabOrder> labTests;

	/**
	 * @return the labTests
	 */
	public List<LabOrder> getLabTests() {
		return labTests;
	}

	/**
	 * @param labTests the labTests to set
	 */
	public void setLabTests(List<LabOrder> labTests) {
		this.labTests = labTests;
	}

	/**
	 * @return the grandFatherConcept
	 */
	public Concept getGrandFatherConcept() {
		return grandFatherConcept;
	}

	/**
	 * @param grandFatherConcept the grandFatherConcept to set
	 */
	public void setGrandFatherConcept(Concept grandFatherConcept) {
		this.grandFatherConcept = grandFatherConcept;
		if(getGrandFatherConcept().getName() != null) {
			setGrandFatherConceptName(getGrandFatherConcept().getName().getName());
		}
	}

	public String getGrandFatherConceptName() {
		return grandFatherConceptName;
	}

	public void setGrandFatherConceptName(String grandFatherConceptName) {
		this.grandFatherConceptName = grandFatherConceptName;
	}
}
