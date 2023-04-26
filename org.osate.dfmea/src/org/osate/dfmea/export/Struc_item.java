package org.osate.dfmea.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.osate.dfmea.Function;
import org.osate.dfmea.Structure;

public class Struc_item {

	Integer suplevel;
	Integer sublevel;
	Structure mystruc;
	List<Structure> supstruc = new ArrayList<Structure>();
	List<Structure> substruc = new ArrayList<Structure>();
	public HashMap<Structure, Integer> levelmap = new HashMap<Structure, Integer>();
	List<Func_item> funcitems = new ArrayList<Func_item>();
	Integer maxrows;

	Struc_item(Structure si) {
		mystruc = si;
		levelmap.put(mystruc, 0);
		Build_supstruc(mystruc.high_level_component, 1);
		for (Structure sii : mystruc.low_level_components_map.values()) {
			Build_substruc(sii, 1);
		}
		Build_Funcitem();
		Calculate_maxrows();
	}

	void Build_supstruc(Structure si, int level) {
		if (not_Layer_overflow(suplevel, level) && si != null) {
			supstruc.add(si);
			levelmap.put(si, level);
			Build_supstruc(si.high_level_component, level + 1);
		}
	}

	void Build_substruc(Structure si, int level) {
		if (not_Layer_overflow(sublevel, level)) {
			substruc.add(si);
			levelmap.put(si, level);
			for (Structure sii : si.low_level_components_map.values()) {
				Build_substruc(sii, level + 1);
			}
		}
	}

	boolean not_Layer_overflow(Integer maxlevel, Integer mylevel) {
		boolean result = true;
		if (maxlevel != null && mylevel > maxlevel) {
			result = false;
		}
		return result;
	}

	void Build_Funcitem() {
		for (Function fi : mystruc.functions.values()) {
			funcitems.add(new Func_item(fi));
		}
	}

	void Calculate_maxrows() {
		Integer funcrows = 0;
		for (Func_item fi : funcitems) {
			funcrows = funcrows + fi.maxrows;
		}
		funcrows = Math.max(funcrows, 1);
		maxrows = 0;
		maxrows = Math.max(funcrows, supstruc.size());
		maxrows = Math.max(maxrows, substruc.size());

		if (maxrows > funcrows && funcitems.size() > 0) {
			Func_item last = funcitems.get(funcitems.size() - 1);
			System.out.println("Struct maxrows::" + maxrows + "     last.maxrows::" + last.maxrows + "  Updatelast::"
					+ (maxrows - (funcrows - last.maxrows)));
			last.maxrows = maxrows - (funcrows - last.maxrows);
			last.Updatemaxrows();
		}
	}

}
