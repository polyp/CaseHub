package schedule;

import java.util.Locale;

/* Currently only support weekdays. */
public enum Day {
	MON, TUES, WED, THURS, FRI;
	
	public String getString() {
		return this.toString().toLowerCase(Locale.getDefault()) + "day";
	}
}
