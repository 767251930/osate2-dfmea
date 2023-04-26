package org.osate.dfmea;

import java.util.HashMap;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.osate.aadl2.BasicPropertyAssociation;
import org.osate.aadl2.DirectionType;
import org.osate.aadl2.Property;
import org.osate.aadl2.PropertyExpression;
import org.osate.aadl2.RecordValue;
import org.osate.aadl2.errormodel.FaultTree.Event;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.properties.PropertyNotPresentException;
import org.osate.dfmea.fixfta.FaultTreeUtils;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorState;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorPropagation;
import org.osate.xtext.aadl2.properties.util.GetProperties;

public class Structure {

	public ComponentInstance ci;
	public Structure high_level_component;
	public HashMap<String, Structure> low_level_components_map = new HashMap<String, Structure>();

	public HashMap<String, Function> functions = new HashMap<String, Function>();
	public HashMap<String, FailureElement> failure_modes = new HashMap<String, FailureElement>();

	public Structure(Structure high, ComponentInstance ci) {
		System.out.println("new Structure::" + ci.getName());
		this.high_level_component = high;
		this.ci = ci;
	}

	public void Print(String indent) {
		System.out.print(indent + "Component :: " + getName());
		if (high_level_component != null) {
			System.out.print("  | (" + high_level_component.getName() + ")<<---");
		}
		if (low_level_components_map.size() > 0) {
			System.out.print("  |--->>(");
		for (String key : low_level_components_map.keySet()) {
			System.out.print(low_level_components_map.get(key).getName() + ",");
			}
			System.out.print(")");
		}
		System.out.println("");
		if (functions.size() > 0) {
		System.out.println(indent + "\tFunctions::");
		for (String key : functions.keySet()) {
			functions.get(key).Print(indent + "\t\t");
		}
	}
		if (failure_modes.size() > 0) {
		System.out.println(indent + "\tFailure_modes::");
		for (String key : failure_modes.keySet()) {
			failure_modes.get(key).Print(indent + "\t\t");
		}
	}
	System.out.println("");

	}



	public FailureElement creatFailureElement(Event ev) {
		if (FaultTreeUtils.getDescription(ev).toLowerCase().contains("fmedastate")) {
			return null;
		}

		FailureElement fmi = new FailureElement(ev);

		fmi.ref_component = this;
		failure_modes.put(fmi.id, fmi);
		EObject failure_object = ev.getRelatedEMV2Object();
		if (failure_object instanceof ErrorBehaviorState || (failure_object instanceof ErrorPropagation
						&& ((ErrorPropagation) failure_object).getDirection() == DirectionType.OUT)) {
			Function fi = new Function(ev);
			fi.ref_component = this;
			Function reff = null;
			if (functions.containsKey(fi.funcname)) {
				reff = Function.Check_Merge(functions.get(fi.funcname), fi);
				reff.ref_fail_modes.add(fmi);
				functions.put(fi.funcname, reff);
			} else {
				functions.put(fi.funcname, fi);
				reff = fi;
				fi.ref_fail_modes.add(fmi);
				fi.id = "f" + functions.size();
				System.out.println("%% new Function of : " + this.getName() + " ::: " + fi.funcname);
			}
			fmi.ref_func = reff;
		}
		System.out.println("%% new Failure Element of : " + this.getName() + " ::: " + fmi.name);
		return fmi;
	}




	public String getName() {
		return FmeaBuilder.getInstanceName(ci);
	}


	public boolean isIntermediateevent(Event e)
	{
		boolean result=false;
		if(e.getName().length()>=12&&e.getName().substring(0, 12).equals("Intermediate")) {
			result=true;
		}
		return result;
	}

	public FmeaHead getHeadPropertie() {
		FmeaHead head = new FmeaHead();
		head.ref_component = this;

		Property property;
		PropertyExpression propertyValue = null;

		property = GetProperties.lookupPropertyDefinition(ci, "AIAG_VDA", "Head");
		try {
			propertyValue = ci.getSimplePropertyValue(property);
		} catch (PropertyNotPresentException e) {
			propertyValue = null;
//			Dialog.showInfo("getHeadPropertie", e.getLocalizedMessage());
		}

		if (propertyValue != null) {
			RecordValue rv = (RecordValue) propertyValue;
			EList<BasicPropertyAssociation> fields = rv.getOwnedFieldValues();

			head.Company_Name = FmeaBuilder.getRecordStringProperty(fields, "CompanyName");

			head.Engineering_Location = FmeaBuilder.getRecordStringProperty(fields, "EngineeringLocation");

			head.Customer_Name = FmeaBuilder.getRecordStringProperty(fields, "CustomerName");

			head.Model_Year_Program = FmeaBuilder.getRecordStringProperty(fields, "ModelYearProgram");

			head.Subject = FmeaBuilder.getRecordStringProperty(fields, "Subject");

			head.DFMEA_Start_Data = FmeaBuilder.getRecordStringProperty(fields, "DFMEAStartData");

			head.DFMEA_Revision_Data = FmeaBuilder.getRecordStringProperty(fields, "DFMEARevisionData");

			head.Cross_Func_Team = FmeaBuilder.getRecordStringProperty(fields, "CrossFuncTeam");

			head.DFMEA_ID = FmeaBuilder.getRecordStringProperty(fields, "DFMEAID");

			head.Design_Responsibility = FmeaBuilder.getRecordStringProperty(fields, "DesignResponsibility");

			head.Confidentiality_Level = FmeaBuilder.getRecordStringProperty(fields, "ConfidentialityLevel");

			head.Focus_component_name = FmeaBuilder.getRecordStringProperty(fields, "FocusComponent");

		}
		if (head.Focus_component_name == null) {
			head.Focus_component_name = FmeaBuilder.getInstanceName(this.ci);
		}
		return head;
	}
}
