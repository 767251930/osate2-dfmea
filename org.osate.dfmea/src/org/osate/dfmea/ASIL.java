package org.osate.dfmea;

public enum ASIL {

	Emp("NULL"), QM("QM"), A("A"), B("B"), C("C"), D("D"), QM_A("QM(A)"), QM_B("QM(B)"), QM_C("QM(C)"), QM_D(
			"QM(D)"), A_A("A(A)"), A_B("A(B)"), A_C(
					"A(C)"), A_D("A(D)"), B_B("B(B)"), B_C("B(C)"), B_D("B(D)"), C_C("C(C)"), C_D("C(D)"), D_D("D(D)");

	private final String printname;

	private ASIL(String name) {
		this.printname = name;
	}

	public String getPrintname() {
		return printname;
	}

	public static ASIL findName(String name) {
		if (name == null) {
			return ASIL.Emp;
		}
		for (ASIL asil : ASIL.values()) {
			if (asil.getPrintname().equals(name)) {
				// 如果需要直接返回name则更改返回类型为String,return statusEnum.name;
				return asil;
			}
		}
		throw new IllegalArgumentException("code is invalid");
	}
}
