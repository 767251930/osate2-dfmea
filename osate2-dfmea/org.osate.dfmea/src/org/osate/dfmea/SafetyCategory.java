package org.osate.dfmea;

public enum SafetyCategory {

	Emp("NULL"), SG("SG"), FSR("FSR"), TSR("TSR"), HSR("HSR"), SSR("SSR");

	private final String printname;

	private SafetyCategory(String name) {
		this.printname = name;
	}

	public String getPrintname() {
		return printname;
	}

	public static SafetyCategory findName(String name) {
		if (name == null) {
			return SafetyCategory.Emp;
		}
		for (SafetyCategory sc : SafetyCategory.values()) {
			if (sc.getPrintname().equals(name)) {
				// 如果需要直接返回name则更改返回类型为String,return statusEnum.name;
				return sc;
			}
		}
		throw new IllegalArgumentException("code is invalid");
	}
}
