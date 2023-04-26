package org.osate.dfmea;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osate.aadl2.AbstractNamedValue;
import org.osate.aadl2.BasicPropertyAssociation;
import org.osate.aadl2.EnumerationLiteral;
import org.osate.aadl2.IntegerLiteral;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.NamedValue;
import org.osate.aadl2.PropertyExpression;
import org.osate.aadl2.RealLiteral;
import org.osate.aadl2.StringLiteral;
import org.osate.aadl2.errormodel.FaultTree.Event;
import org.osate.aadl2.errormodel.FaultTree.FaultTree;
import org.osate.aadl2.errormodel.PropagationGraph.PropagationGraph;
import org.osate.aadl2.errormodel.PropagationGraph.util.Util;
import org.osate.aadl2.errormodel.faulttree.generation.Activator;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.InstanceObject;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.modelsupport.util.AadlUtil;
import org.osate.dfmea.fixfta.FTAGenerator;
import org.osate.dfmea.fixfta.FaultTreeUtils;
import org.osate.ui.dialogs.Dialog;
import org.osate.xtext.aadl2.errormodel.errorModel.TypeToken;
import org.osate.xtext.aadl2.errormodel.util.EMV2Util;
import org.osate.xtext.aadl2.properties.util.GetProperties;

public class FmeaBuilder {

	public FmeaHead head;
	public Structure root_component;
	public Structure focus_component;
	public HashMap<String, Structure> components = new HashMap<String, Structure>();
	public FaultTree ftamodel;

	public void Construct_structure_tree(ComponentInstance ci) {
		if(ci==null)
		{
			System.out.println("Construct_structure_tree ::  ci=null");
			return;
		}
		root_component = new Structure(null, ci);
		components.put(getInstanceName(ci), root_component);
		travel_structure_tree(root_component);
	}

	public void travel_structure_tree(Structure root_component) {
		if (root_component == null) {
			System.out.println("travel_instance ::   root_component=null");
			return;
		}
		for (ComponentInstance cc : root_component.ci.getComponentInstances()) {
			Structure t = new Structure(root_component, cc);
			components.put(getInstanceName(cc), t);
			root_component.low_level_components_map.put(t.getName(), t);
			travel_structure_tree(t);
		}
	}

	public void Print_Structure(Structure root, String indent) {
		root.Print(indent);
		for (Structure cc : root.low_level_components_map.values()) {
			Print_Structure(cc, indent + "\t\t");
		}
	}


	public void BuildFailureAndFuncNet() {
		// 构造故障网络
		PropagationGraph currentPropagationGraph = Util.generatePropagationGraph(root_component.ci.getSystemInstance(), false);
		FTAGenerator generator = new FTAGenerator(currentPropagationGraph);
		ftamodel = generator.getAllftaModel(root_component.ci);

		// 创建组件的的功能和故障
		int count = 0;
		for (Event ev : ftamodel.getEvents()) {
			if (ev.getName().startsWith("Intermediate")) {
				continue;
			}
			count++;
			getFailureElement(ev);
		}

		System.out.println(
				"$$$$$Creat Failure Element ::" + count + "\t\t Event counts :: " + ftamodel.getEvents().size());

		ArrayList<Event> TopEvents = FTAGenerator.getTopEvent(ftamodel);
		for (Event ev : TopEvents) {
			System.out.println("&&&&&&& Top Event :: " + FaultTreeUtils.getDescription(ev));
		}

		// 遍历故障网络 创建并连接故障
		for (Event ev : TopEvents) {
			for (Event sub : ev.getSubEvents()) {
				linkFailure(ev, sub, getFailureElement(ev).ref_func);
			}

		}

		// 遍历故障网 标记故障类型
		for (Structure si : components.values()) {
			for (FailureElement fei : si.failure_modes.values()) {
				fei.markCause();
				fei.markEffect();
			}
		}


	}

	public void linkFailure(Event top, Event ev,Function topfunc) {
		if (ev.getName().startsWith("Intermediate")) {
			for (Event sub : ev.getSubEvents()) {
				linkFailure(top, sub,topfunc);
			}
			return;
		}
		FailureElement fm = getFailureElement(top);
		FailureElement tfm = getFailureElement(ev);

		if (fm.ref_func != null && tfm.ref_func != null && tfm.ref_func != fm.ref_func) {
			if (!fm.ref_func.func_cause.contains(tfm.ref_func)) {
				fm.ref_func.func_cause.add(tfm.ref_func);
			}
			if (!tfm.ref_func.func_effect.contains(fm.ref_func)) {
				tfm.ref_func.func_effect.add(fm.ref_func);
			}
		}

		Function ref_func=null;
		if(topfunc==null)
		{
			ref_func = tfm.ref_func;

		} else if (tfm.ref_func == null) {
			ref_func = topfunc;
		} else {
			if (!topfunc.func_cause.contains(tfm.ref_func) && topfunc != tfm.ref_func) {
				topfunc.func_cause.add(tfm.ref_func);
			}
			if (!tfm.ref_func.func_effect.contains(topfunc) && topfunc != tfm.ref_func) {
				tfm.ref_func.func_effect.add(topfunc);
			}
			ref_func = tfm.ref_func;
		}

		if (fm.failure_cause.contains(tfm)) {
			return;
		}
		fm.failure_cause.add(tfm);
		tfm.failure_effect.add(fm);



//		System.out.println("add Error link::  " + fm.ref_component.getName() + ".{" + fm.id + "} \t-->\t"
//				+ tfm.ref_component.getName() + ".{" + tfm.id + "}");
		System.out.println("add Error link::  " + FaultTreeUtils.getDescription(top) + "\t-->\t"
				+ FaultTreeUtils.getDescription(ev));
		for (Event sub : ev.getSubEvents()) {
			linkFailure(ev, sub, ref_func);
		}

	}

	public FailureElement getFailureElement(Event ev) {
		FailureElement result = null;
		if (ev.getName().startsWith("Intermediate")) {
			return result;
		}

		ComponentInstance io = (ComponentInstance) ev.getRelatedInstanceObject();
		String failurename = ev.getName();
		Structure si = components.get(getInstanceName(io));
		if (si.failure_modes.containsKey(failurename)) {
			result = si.failure_modes.get(failurename);
		} else {
			result = si.creatFailureElement(ev);
		}
		return result;

	}


	public void TravelFTARootEvent(Event event, String indent) {
		InstanceObject io = (InstanceObject) event.getRelatedInstanceObject();
		EObject nne = event.getRelatedEMV2Object();

		TypeToken type = (TypeToken) event.getRelatedErrorType();
		System.out.println(indent + "Event Name			:::::::" + event.getName());
		System.out.println(indent + "Sub event Logic			:::::::" + event.getSubEventLogic());
		System.out.println(indent + "RelatedInstanceObject 		:::::::" + io.getName());
		System.out.println(indent + "ComponentInstancePath 		:::::::" + io.getComponentInstancePath());
		if (nne instanceof NamedElement) {
			System.out.println(indent + "RelatedEMV2Object		:::::::" + EMV2Util.getPrintName((NamedElement) nne));
		}
		System.out.println(indent + "RelatedErrorType		:::::::" + EMV2Util.getPrintName(type));
		System.out.println(indent + "Probability 			:::::::" + event.getProbability());

		for (Event ei : event.getSubEvents()) {
			TravelFTARootEvent(ei, indent + "\t");
		}
	}

	public void FillAP(Structure root) {
		for (FailureElement fi : root.failure_modes.values()) {
			fi.SearchMaxrefS();
			fi.Cal_AP();
		}
		for (Structure ci : root.low_level_components_map.values()) {
			FillAP(ci);
		}
	}

	public static AP CalculateAp(Integer S, Integer O, Integer D) {
		if(!(InRange(S,1,10)&&InRange(O,1,10)&&InRange(D,1,10)))
		{
			Dialog.showInfo("Fill_failure_modes", "Range Error::  S-" + S + "  O-" + O + " D-" + D + ".");
			return null;
		}
		if (O == 1 || S == 1) {
			return AP.Low;
		}
		if (InRange(S, 9, 10)) {
			if (InRange(O, 4, 5)) {
				if (D == 1) {
					return AP.Middle;
				}
			}
			if (InRange(O, 2, 3)) {
				if (InRange(D, 5, 6)) {
					return AP.Middle;
				}
				if (InRange(D, 1, 4)) {
					return AP.Low;
				}
			}
			return AP.High;
		}
		if (InRange(S, 7, 8)) {
			if(InRange(O, 8, 10)) {
				return AP.High;
			}
			if (InRange(O, 6, 7)) {
				if (InRange(D, 2, 10)) {
					return AP.High;
				} else {
					return AP.Middle;
				}
			}
			if (InRange(O, 4, 5)) {
				if (InRange(D, 7, 10)) {
					return AP.High;
				} else {
					return AP.Middle;
				}
			}
			if (InRange(O, 2, 3)) {
				if (InRange(D, 5, 10)) {
					return AP.Middle;
				} else {
					return AP.Low;
				}
			}
		}

		if (InRange(S, 4, 6)) {
			if(InRange(O, 8, 10))
			{
				if(InRange(D, 5, 10)) {
					return AP.High;
				} else {
					return AP.Middle;
				}
			}
			if (InRange(O, 6, 7)) {
				if (InRange(D, 2, 10)) {
					return AP.Middle;
				}
			}
			if (InRange(O, 4, 5)) {
				if(InRange(D, 7, 10)) {
					return AP.Middle;
				}
			}
			return AP.Low;
		}

		if (InRange(S, 2, 3)) {
			if (InRange(O, 8, 10)) {
				if (InRange(D, 5, 10)) {
					return AP.Middle;
				}
			}
			return AP.Low;
		}
		Dialog.showInfo("Fill_failure_modes", "No judgment::  S-" + S + "  O-" + O + " D-" + D + ".");
		return null;

	}

	public static boolean InRange(int current, int min, int max) {
		return Math.max(min, current) == Math.min(current, max);
	}

	public static String getInstanceName(ComponentInstance ci) {
		String result = ci instanceof SystemInstance
				? ci.getComponentClassifier().getName().replaceAll("::", "_").replaceAll("\\.", "_")
				: ci.getComponentInstancePath();
		return result;

	}

	public static String getRecordStringProperty(EList<BasicPropertyAssociation> fields, String recname) {
		BasicPropertyAssociation xref = GetProperties.getRecordField(fields, recname);
		String result = null;
		;
		if (xref != null) {
			PropertyExpression val = xref.getOwnedValue();
			result = ((StringLiteral) val).getValue();
		}
		return result;
	}

	public static String getRecordEnumerationProperty(EList<BasicPropertyAssociation> fields, String recname) {
		BasicPropertyAssociation xref = GetProperties.getRecordField(fields, recname);
		String result = null;
		if (xref != null) {
			PropertyExpression val = xref.getOwnedValue();
			AbstractNamedValue eval = ((NamedValue) val).getNamedValue();
			result = ((EnumerationLiteral) eval).getName();
		}
		return result;
	}

	public static Double getRecordRealProperty(EList<BasicPropertyAssociation> fields, String recname) {
		BasicPropertyAssociation xref = GetProperties.getRecordField(fields, recname);
		Double result = null;
		;
		if (xref != null) {
			PropertyExpression val = xref.getOwnedValue();
			result = ((RealLiteral) val).getValue();
		}
		return result;
	}

	public static Integer getRecordIntProperty(EList<BasicPropertyAssociation> fields, String recname) {
		BasicPropertyAssociation xref = GetProperties.getRecordField(fields, recname);
		Integer result = null;
		;
		if (xref != null) {
			PropertyExpression val = xref.getOwnedValue();
			if (val instanceof NamedValue) {
				AbstractNamedValue eval = ((NamedValue) val).getNamedValue();
				if (eval instanceof EnumerationLiteral) {
					return Integer.parseInt((((EnumerationLiteral) eval).getName()).substring(1));
				}
			}
			result = (int) ((IntegerLiteral) val).getValue();
		}
		return result;
	}

	public static Double getRecordUnitProperty(EList<BasicPropertyAssociation> fields, String fieldName,
			String unit) {
		PropertyExpression val = null;
		Double result = null;
		BasicPropertyAssociation xref = GetProperties.getRecordField(fields, fieldName);
		if (xref != null) {
			val = xref.getOwnedValue();

		}
		if (val instanceof IntegerLiteral) {
			result = ((IntegerLiteral) val).getScaledValue(unit);
		}
		return result;

	}

	public static void Dialog(String s1, String s2) {
		Dialog.showInfo(s1, s2);
	}

	public void getHead() {
		head = root_component.getHeadPropertie();
		if (!components.containsKey(head.Focus_component_name)) {
			Dialog.showInfo("FmeaHead:FocusComponent",
					"Can't found Instance name : " + head.Focus_component_name + ".");
			head.Focus_component_name = getInstanceName(root_component.ci);
		}
		focus_component = components.get(head.Focus_component_name);
	}

	public void updateFocusComponent(String componentname) {
		if (!components.containsKey(componentname)) {
			Dialog.showInfo("updateFocusComponent", "Can't found Instance name : " + componentname + ".");
			componentname = getInstanceName(root_component.ci);
		}
		focus_component = components.get(componentname);
		head.Focus_component_name = componentname;
	}

	public URI saveFaultTree() {
		URI ftaURI = EcoreUtil.getURI(ftamodel.getInstanceRoot())
				.trimFragment()
				.trimFileExtension()
				.trimSegments(1)
				.appendSegment("reports")
				.appendSegment("fta")
				.appendSegment(ftamodel.getName())
				.appendFileExtension("faulttree");
		AadlUtil.makeSureFoldersExist(new Path(ftaURI.toPlatformString(true)));
		Resource res = ftamodel.getInstanceRoot().eResource().getResourceSet().createResource(ftaURI);
		res.getContents().add(ftamodel);
		try {
			res.save(null);
		} catch (IOException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}
		return EcoreUtil.getURI(ftamodel);
	}



}