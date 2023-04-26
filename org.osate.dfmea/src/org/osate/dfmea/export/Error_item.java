package org.osate.dfmea.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.osate.dfmea.FailureElement;


public class Error_item {
	Integer suplevel;
	Integer sublevel;
	FailureElement myerror;
	List<FailureElement> superror = new ArrayList<FailureElement>();
	List<FailureElement> suberror = new ArrayList<FailureElement>();
	List<Integer> suplevelmap = new ArrayList<Integer>();
	List<Integer> sublevelmap = new ArrayList<Integer>();
	List<Boolean> suprepeated = new ArrayList<Boolean>();
	List<Boolean> subrepeated = new ArrayList<Boolean>();

	List<Pair<Integer, Integer>> rowandrownums = new ArrayList<Pair<Integer, Integer>>();
	Integer subrownums;
	Integer maxrows;

	HashMap<FailureElement, Boolean> causevisitedmap = new HashMap<FailureElement, Boolean>();
	HashMap<FailureElement, Boolean> effectvisitedmap = new HashMap<FailureElement, Boolean>();
	List<Pair<FailureElement, Pair<Integer, Boolean>>> causelist = new ArrayList<Pair<FailureElement, Pair<Integer, Boolean>>>();
	List<Pair<FailureElement, Pair<Integer, Boolean>>> effectlist = new ArrayList<Pair<FailureElement, Pair<Integer, Boolean>>>();


	Error_item(FailureElement fmi) {
		myerror = fmi;
		for (FailureElement fii : myerror.failure_effect) {
			Build_superror(fii, 1);
		}
		for (FailureElement fii : myerror.failure_cause) {
			Build_suberror(fii, 1);
		}
		subrownums = 0;
		rowandrownums.add(new Pair<Integer, Integer>(0, 1));
		Add_superror();
		Add_suberror();
		rowandrownums.remove(0);
		subrownums = Math.max(subrownums, 1);
		maxrows = Math.max(subrownums, superror.size());
	}

	void Add_superror() {

		for (Pair<FailureElement, Pair<Integer, Boolean>> pi : effectlist) {
			FailureElement fi = pi.getKey();
			Integer level = pi.getValue().getKey();
			Boolean visited = pi.getValue().getValue();
			if (AllowDisplay(fi, 0)) {
				superror.add(fi);
				suplevelmap.add(level);
				suprepeated.add(visited);
			}
		}
	}

	void Add_suberror() {

		for (Pair<FailureElement, Pair<Integer, Boolean>> pi : causelist) {
			FailureElement fi = pi.getKey();
			Integer level = pi.getValue().getKey();
			Boolean visited = pi.getValue().getValue();
			if (AllowDisplay(fi, 1)) {
				suberror.add(fi);
				Pair<Integer, Integer> lastPair = getLastPair();
				int rows = Math.max(fi.optimizations.size(), 1);
				rowandrownums.add(new Pair<Integer, Integer>(lastPair.getKey() + lastPair.getValue(), rows));
				subrownums = subrownums + rows;
				sublevelmap.add(level);
				subrepeated.add(visited);
			}
		}
	}

	void Build_superror(FailureElement fi, int level) {
		if (not_Layer_overflow(suplevel, level)) {
			if (!effectvisitedmap.containsKey(fi)) {
				effectvisitedmap.put(fi, true);
				effectlist.add(new Pair<FailureElement, Pair<Integer, Boolean>>(fi,
						(new Pair<Integer, Boolean>(level, false))));
				for (FailureElement fii : fi.failure_effect) {
					Build_superror(fii, level + 1);
				}
			} else {
				effectlist.add(new Pair<FailureElement, Pair<Integer, Boolean>>(fi,
						(new Pair<Integer, Boolean>(level, true))));
			}
		}
	}

	void Build_suberror(FailureElement fi, int level) {
		if (not_Layer_overflow(sublevel, level)) {
			if (!causevisitedmap.containsKey(fi)) {
				causevisitedmap.put(fi, true);
				causelist.add(new Pair<FailureElement, Pair<Integer, Boolean>>(fi,
						(new Pair<Integer, Boolean>(level, false))));
				for (FailureElement fii : fi.failure_cause) {
					Build_suberror(fii, level + 1);
				}
			} else {
				causelist.add(new Pair<FailureElement, Pair<Integer, Boolean>>(fi,
						(new Pair<Integer, Boolean>(level, true))));
			}
		}
	}

	boolean AllowDisplay(FailureElement fi, int t) {
		if (fi.hasRiskorOptProperty()) {
			return true;
		}
		if (FileExport.ONLY_FAILUREMODES && !fi.isFailureMode()) {
			return false;
		}
		if (FileExport.ONLY_FINAL_FAILURE) {
			if ((t == 1 && !fi.isFinalCause())) {
				return false;
			}
			if (t == 0 && !fi.isFinalEffect()) {
				return false;
			}
		}
		return true;

	}

	boolean not_Layer_overflow(Integer maxlevel, Integer mylevel) {
		boolean result = true;
		if (maxlevel != null && mylevel > maxlevel) {
			result = false;
		}
		return result;
	}

	public Pair<Integer, Integer> getLastPair() {
		return rowandrownums.get(rowandrownums.size() - 1);
	}

	void Updatemaxrows() {
		if (rowandrownums.size() > 0) {
			Pair<Integer, Integer> last = getLastPair();
			Integer lastrow = last.getKey();
			rowandrownums.remove(rowandrownums.size() - 1);
			rowandrownums.add(new Pair<Integer, Integer>(lastrow, maxrows - (lastrow - 1)));
		}

	}

}
