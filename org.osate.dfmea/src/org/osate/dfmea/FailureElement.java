package org.osate.dfmea;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.osate.aadl2.BasicPropertyAssociation;
import org.osate.aadl2.DirectionType;
import org.osate.aadl2.ListValue;
import org.osate.aadl2.ModalPropertyValue;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.PropertyExpression;
import org.osate.aadl2.RecordValue;
import org.osate.aadl2.errormodel.FaultTree.Event;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.dfmea.fixfta.FaultTreeUtils;
import org.osate.xtext.aadl2.errormodel.errorModel.EMV2PropertyAssociation;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorState;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorPropagation;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorTypes;
import org.osate.xtext.aadl2.errormodel.errorModel.TypeToken;
import org.osate.xtext.aadl2.errormodel.util.EMV2Properties;

public class FailureElement {
	public String id;
	public String name;

	public Event ref_event;
	public Structure ref_component;
	public NamedElement failure_object;
	public ErrorTypes failure_mode_typeset;

	public Function ref_func;
	public List<FailureElement> failure_effect = new ArrayList<FailureElement>();
	public List<FailureElement> failure_cause = new ArrayList<FailureElement>();

	public Integer fmea_serverity;
	public Integer fmea_occurrence;
	public Integer fmea_detection;
	public String prevention_control;
	public String detection_control;

	public Boolean cause_haveoneway_nonfailuremode = null;
	public Boolean cause_isfinal = null;
	public Boolean effect_haveoneway_nonfailuremode = null;
	public Boolean effect_isfinal = null;


	private AP myap = AP.Emp;
	public Integer ref_S;

	public List<Optimization> optimizations = new ArrayList<Optimization>();

	public AP getMyap() {
		return myap;
	}

	public void setMyap(AP myap) {
		this.myap = myap;
	};

	FailureElement(Event ev) {

		ComponentInstance ci = (ComponentInstance) ev.getRelatedInstanceObject();
		NamedElement target = (NamedElement) ev.getRelatedEMV2Object();
		ErrorTypes ts = null;
		if ((TypeToken) ev.getRelatedErrorType() != null) {
			ts = ((TypeToken) ev.getRelatedErrorType()).getType().get(0);
		}
		this.ref_event = ev;
		this.failure_object = target;
		this.failure_mode_typeset = ts;
		this.id = ev.getName();

		List<EMV2PropertyAssociation> fm = EMV2Properties.getProperty("AIAG_VDA::DFMEA", ci, target, ts);
		EMV2PropertyAssociation fma = fm.isEmpty() ? null : fm.get(0);
		PropertyExpression fmv = EMV2Properties.getPropertyValue(fma);
//		EList<BasicPropertyAssociation> fields = fmv == null ? null
//				: ((RecordValue) ((ListValue) fmv).getOwnedListElements().get(0)).getOwnedFieldValues();
		EList<BasicPropertyAssociation> fields = fmv == null ? null : ((RecordValue) fmv).getOwnedFieldValues();
		if (fields != null) {
			this.name = FmeaBuilder.getRecordStringProperty(fields, "FailureDescription");
		}
		if (this.name == null) {
			this.name = FaultTreeUtils.getDescription(ev);
		}
		fm = EMV2Properties.getProperty("AIAG_VDA::DFMEA", ci, target, ts);
		fma = fm.isEmpty() ? null : fm.get(0);
		fmv = EMV2Properties.getPropertyValue(fma);
		fields = fmv == null ? null : ((RecordValue) fmv).getOwnedFieldValues();
		if (fields != null) {
			this.fmea_serverity = FmeaBuilder.getRecordIntProperty(fields, "Severity");
			this.fmea_occurrence = FmeaBuilder.getRecordIntProperty(fields, "Occurrence");
			this.fmea_detection = FmeaBuilder.getRecordIntProperty(fields, "Detection");
			this.prevention_control = FmeaBuilder.getRecordStringProperty(fields, "PC");
			this.detection_control = FmeaBuilder.getRecordStringProperty(fields, "DC");
		}
		Fill_Optimizations(ci, target, ts);

	}


	public void Print(String indent) {
		System.out.print(indent);
		System.out.print(this.effect_haveoneway_nonfailuremode + " " + this.effect_isfinal + " "
				+ this.cause_haveoneway_nonfailuremode + " " + this.cause_isfinal);
		System.out.print("  |<-Structure::" + ref_component.getName());
		System.out.print("  |id:: " + id);
		System.out.print("  |name:: " + name);
		if (ref_func != null) {
			System.out.print("  |ref_func:: " + ref_func.id);
		}
		if (fmea_serverity != null) {
			System.out.print("  ||S:: " + fmea_serverity);
		}
		if (ref_S != null) {
			System.out.print("  ||ref_S:: " + ref_S);
		}
		if (fmea_occurrence != null) {
			System.out.print("  ||O:: " + fmea_occurrence);
		}
		if (fmea_detection != null) {
			System.out.print("  ||D:: " + fmea_detection);
		}
		if (myap != AP.Emp) {
			System.out.print("  ||AP:: " + myap);
		}
		if (prevention_control != null) {
			System.out.print("  ||PC:: " + prevention_control);
		}
		if (detection_control != null) {
			System.out.print("  ||DC:: " + detection_control);
		}
		if (failure_effect.size() > 0) {
			System.out.print("  | (");
			for (FailureElement fi : failure_effect) {
				System.out.print(fi.ref_component.getName() + "." + fi.id + " , ");
			}
			System.out.print(")<<---");
		}
		if (failure_cause.size() > 0) {
			System.out.print("  |--->>(");
			for (FailureElement fi : failure_cause) {
				System.out.print(fi.ref_component.getName() + "." + fi.id + " , ");
			}
			System.out.print(")");
		}
		System.out.println("");
		if (optimizations.size() > 0) {
			System.out.println(indent + "\tOptimizations:");
			for (Optimization oi : optimizations) {
				oi.Print(indent + "\t\t");
			}

		}
	}

	public void Fill_Optimizations(NamedElement ci, NamedElement target, ErrorTypes ts) {

		List<EMV2PropertyAssociation> fm = EMV2Properties.getProperty("AIAG_VDA::Optimization", ci, target, ts);

		for (EMV2PropertyAssociation PA : fm) {
			for (ModalPropertyValue modalPropertyValue : PA.getOwnedValues()) {
				PropertyExpression peVal = modalPropertyValue.getOwnedValue();
				ListValue lv = (ListValue) peVal;
				for (PropertyExpression pe : lv.getOwnedListElements()) {
					EList<BasicPropertyAssociation> fields = ((RecordValue) pe).getOwnedFieldValues();
					String OPT_PC = null;
					String OPT_DC = null;
					String Respons_Person = null;
					String Target_Completion_Data = null;
					String Status = null;
					String Evidence = null;
					String Completion_Data = null;
					Integer OPT_Occurrence = null;
					Integer OPT_Detection = null;

					if (fields != null) {
					// get OPT_PC
					OPT_PC = FmeaBuilder.getRecordStringProperty(fields, "OptPC");

					// get OPT_DC
					OPT_DC = FmeaBuilder.getRecordStringProperty(fields, "OptDC");

					// get Respons_Person
					Respons_Person = FmeaBuilder.getRecordStringProperty(fields, "ResponsPerson");

					// get Target_Completion_Data
					Target_Completion_Data = FmeaBuilder.getRecordStringProperty(fields, "TargetCompletionData");

					// get Status
					Status = FmeaBuilder.getRecordStringProperty(fields, "Status");

					// get Evidence
					Evidence = FmeaBuilder.getRecordStringProperty(fields, "Evidence");

					// get Completion_Data
					Completion_Data = FmeaBuilder.getRecordStringProperty(fields, "CompletionData");

					// get OPT_Occurrence
					OPT_Occurrence = FmeaBuilder.getRecordIntProperty(fields, "OPTOccurrence");

					// get OPT_Detection
					OPT_Detection = FmeaBuilder.getRecordIntProperty(fields, "OPTDetection");

					optimizations.add(new Optimization(this, OPT_PC, OPT_DC, Respons_Person, Target_Completion_Data,
							Status, Evidence, Completion_Data, OPT_Occurrence, OPT_Detection));

				}
			}
			}
		}

	}

	public Integer SearchMaxrefS() {
		Integer maxS=0;
		if (ref_S != null) {
			return ref_S;
		}
		if (fmea_serverity != null) {
			maxS = fmea_serverity;
		}
		for(FailureElement fi:failure_effect)
		{
			maxS = Math.max(maxS, fi.SearchMaxrefS());
		}
		if (fmea_occurrence != null && fmea_detection != null && maxS != 0) {
			ref_S=maxS;
		}
		return maxS;
	}

	public void Cal_AP()
	{
		if(this.fmea_occurrence!=null&&this.fmea_detection!=null&&this.ref_S!=null) {
			this.myap=FmeaBuilder.CalculateAp(ref_S, fmea_occurrence, fmea_detection);
		}
			for (Optimization oi : this.optimizations) {
				oi.Cal_AP();
			}

	}

	public void getCauseLeaf(Set<FailureElement> failureset)
	{

		if (!failureset.contains(this)) {
			if (isFinalCause() && isFailureMode()) {
				failureset.add(this);
			}
			for (FailureElement fmi : failure_cause) {
				fmi.getCauseLeaf(failureset);
			}
		}
		return;
	}

	public boolean hasRiskorOptProperty() {
		if (this.fmea_serverity != null || this.fmea_occurrence != null || this.fmea_detection != null
				|| this.prevention_control != null || this.detection_control != null || optimizations.size() > 0) {
			return true;
		}
		return false;
	}

	public boolean isFailureMode() {
		if (failure_object instanceof ErrorBehaviorState || (failure_object instanceof ErrorPropagation
				&& ((ErrorPropagation) failure_object).getDirection() == DirectionType.OUT)) {
			return true;
		}
		return false;
	}

	public boolean isFinalCause() {
		return this.cause_haveoneway_nonfailuremode || this.cause_isfinal;
	}

	public boolean isFinalEffect() {
		return this.effect_haveoneway_nonfailuremode || this.effect_isfinal;
	}

	public void markCause() {
		if (cause_haveoneway_nonfailuremode == null && cause_isfinal == null) {
			cause_haveoneway_nonfailuremode = false;
			cause_isfinal = false;
			for(FailureElement fei:failure_cause)
			{
				fei.markCause();
				cause_haveoneway_nonfailuremode = cause_haveoneway_nonfailuremode
						|| (fei.cause_haveoneway_nonfailuremode && !fei.isFailureMode());
			}
			if (failure_cause.isEmpty()) {
				cause_haveoneway_nonfailuremode = true;
				cause_isfinal = true;
			}
		}
	}

	public void markEffect() {
		if (effect_haveoneway_nonfailuremode == null && effect_isfinal == null) {
			effect_haveoneway_nonfailuremode = false;
			effect_isfinal = false;
			for (FailureElement fei : failure_effect) {
				fei.markEffect();
				effect_haveoneway_nonfailuremode = effect_haveoneway_nonfailuremode
						|| (fei.effect_haveoneway_nonfailuremode && !fei.isFailureMode());
			}
			if (failure_effect.isEmpty()) {
				effect_haveoneway_nonfailuremode = true;
				effect_isfinal = true;
			}
		}
	}
}
