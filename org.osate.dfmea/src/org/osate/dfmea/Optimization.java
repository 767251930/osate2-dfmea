package org.osate.dfmea;

public class Optimization {
	public FailureElement ref_mode;
	public String opt_pc;
	public String opt_dc;
	public String respons_person;
	public String target_completion_data;
	public String status;
	public String evidence;
	public String completion_data;
	public Integer opt_occurrence;
	public Integer opt_detection;
	private AP opt_ap = AP.Emp;

	public AP getOpt_ap() {
		return opt_ap;
	}
	public void setOpt_ap(AP opt_ap) {
		this.opt_ap = opt_ap;
	}

	Optimization(FailureElement ref_mode, String opt_pc, String opt_dc, String respons_person,
			String target_completion_data, String status, String evidence, String completion_data,
			Integer opt_occurrence, Integer opt_detection) {
		this.ref_mode = ref_mode;
		this.opt_pc = opt_pc;
		this.opt_dc = opt_dc;
		this.respons_person = respons_person;
		this.target_completion_data = target_completion_data;
		this.status = status;
		this.evidence = evidence;
		this.completion_data = completion_data;
		this.opt_occurrence = opt_occurrence;
		this.opt_detection = opt_detection;
	}

	public void Print(String indent) {
		System.out.print(indent + "<-ref_mode :: " + ref_mode.id);
		if (ref_mode.ref_S != null) {
			System.out.print("  |ref_S:: " + ref_mode.ref_S);
		}
		if (opt_occurrence != null) {
			System.out.print("  |opt_O:: " + opt_occurrence);
		}
		if (opt_detection != null) {
			System.out.print("  |opt_D:: " + opt_detection);
		}
		if (opt_ap != AP.Emp) {
			System.out.print("  |AP:: " + opt_ap);
		}
		if (opt_pc != null) {
			System.out.print("  |opt_PC:: " + opt_pc);
		}
		if (opt_dc != null) {
			System.out.print("  |opt_DC:: " + opt_dc);
		}
		if (respons_person != null) {
			System.out.print("  |respons_person:: " + respons_person);
		}
		if (target_completion_data != null) {
			System.out.print("  |target_completion_data:: " + target_completion_data);
		}
		if (status != null) {
			System.out.print("  |status:: " + status);
		}
		if (evidence != null) {
			System.out.print("  |evidence:: " + evidence);
		}
		if (completion_data != null) {
			System.out.print("  |completion_data:: " + completion_data);
		}

		System.out.println("");
	}

	public void Cal_AP() {
		if (this.opt_occurrence != null && this.opt_detection != null && this.ref_mode.ref_S != null) {
			this.opt_ap = FmeaBuilder.CalculateAp(this.ref_mode.ref_S, opt_occurrence, opt_detection);
		}
	}

}
