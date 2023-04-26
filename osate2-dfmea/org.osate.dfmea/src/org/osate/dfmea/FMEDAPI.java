package org.osate.dfmea;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osate.aadl2.errormodel.FaultTree.Event;
import org.osate.aadl2.errormodel.FaultTree.FaultTree;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.ui.dialogs.Dialog;

public class FMEDAPI {

	FmeaBuilder fb;
	ComponentInstance ci;

	public FMEDAPI(ComponentInstance ci) {
		this.ci = ci;
		// 准备构建FMEA数据结构
		fb = new FmeaBuilder();
		// 构造结构树
		fb.Construct_structure_tree(ci);
		// 同时构造故障网与功能网
		fb.BuildFailureAndFuncNet();
	}

	public List<String> Get_Safety_Goal() {
		List<String> result = new ArrayList<String>();
		for (Function fi : fb.root_component.functions.values()) {
			result.add(fi.funcname);
		}
		return result;
	}

	// old
	public List<ComponentInstance> Get_Calcul_Instance(ComponentInstance ci, String sg) {

		boolean findsg = false;
		Set<FailureElement> failureset = new HashSet<FailureElement>();
		Set<ComponentInstance> componentset = new HashSet<ComponentInstance>();

		for (Function fci : fb.root_component.functions.values()) {
			if (fci.funcname.equals(sg)) {
				findsg = true;

				for (FailureElement fmi : fci.ref_fail_modes) {
					fmi.getCauseLeaf(failureset);
				}
				for (FailureElement leaffmi : failureset) {
					componentset.add(leaffmi.ref_component.ci);
				}
				break;
			}
		}
		componentset.remove(ci);
		List<ComponentInstance> result = new ArrayList<ComponentInstance>(componentset);

		if (findsg == false) {
			Dialog.showInfo("Get_Calcul_Instance",
					"Can't find Safety Goal \"" + sg + "\" in Component \"" + ci.getName() + "\".");
			return null;
		}
		if (result.size() == 0) {
			Dialog.showInfo("Get_Calcul_Instance", "Can't find related sub component in Safety Goal \"" + sg + "\".");
			return null;
		}
		return result;
	}

	// new 1
	public List<ComponentInstance> Get_Calcul_Instance(String sg) {

		boolean findsg = false;
		Set<FailureElement> failureset = new HashSet<FailureElement>();
		Set<ComponentInstance> componentset = new HashSet<ComponentInstance>();

		for (Function fci : fb.root_component.functions.values()) {
			if (fci.funcname.equals(sg)) {
				findsg = true;

				for (FailureElement fmi : fci.ref_fail_modes) {
					fmi.getCauseLeaf(failureset);
				}
				for (FailureElement leaffmi : failureset) {
					componentset.add(leaffmi.ref_component.ci);
				}
				break;
			}
		}
		List<ComponentInstance> result = new ArrayList<ComponentInstance>(componentset);
		if (findsg == false) {
			Dialog.showInfo("Get_Calcul_Instance",
					"Can't find Safety Goal \"" + sg + "\" in Component \"" + ci.getName() + "\".");
			return null;
		}
		if (result.size() == 0) {
			Dialog.showInfo("Get_Calcul_Instance", "Can't find related sub component in Safety Goal \"" + sg + "\".");
			return null;
		}
		return result;
	}

	// new 2
	public List<Event> Get_Calcul_Event(String sg) {
		boolean findsg = false;
		Set<FailureElement> failureset = new HashSet<FailureElement>();
		Set<Event> eventset = new HashSet<Event>();

		for (Function fci : fb.root_component.functions.values()) {
			if (fci.funcname.equals(sg)) {
				findsg = true;

				for (FailureElement fmi : fci.ref_fail_modes) {
					fmi.getCauseLeaf(failureset);
				}
				for (FailureElement leaffmi : failureset) {
					eventset.add(leaffmi.ref_event);
				}
				break;
			}
		}
		List<Event> result = new ArrayList<Event>(eventset);
		if (findsg == false) {
			Dialog.showInfo("Get_Calcul_Instance",
					"Can't find Safety Goal \"" + sg + "\" in Component \"" + ci.getName() + "\".");
			return null;
		}
		if (result.size() == 0) {
			Dialog.showInfo("Get_Calcul_Instance", "Can't find related sub component in Safety Goal \"" + sg + "\".");
			return null;
		}
		return result;
	}

	// get
	public FaultTree getFaultTree() {
		return fb.ftamodel;
	}

}
