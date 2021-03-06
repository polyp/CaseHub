package laundry;

import java.util.Locale;

/**
 * Represents a washer or dryer.
 */
public class LaundryMachine {
	
	public static final String TYPE_WASHER = "Washer";
	public static final String TYPE_DRYER = "Dryer";

	private int machineNumber;
	private int minutesLeft;
	private String type;
	private Status status;
		
	public LaundryMachine(int machineNumber, int minutesLeft, String type, String status) {
		this.machineNumber = machineNumber;
		this.minutesLeft = minutesLeft;
		this.type = type;
		this.status = Status.lookup(status);
	}

	public int getMachineNumber() {
		return machineNumber;
	}

	public int getMinutesLeft() {
		return minutesLeft;
	}
	public String getType() {
		return type;
	}

	public Status getStatus() {
		return status;
	}

	public enum Status {
		AVAILABLE, IN_USE, CYCLE_COMPLETE, UNAVAILABLE;
		
		public String getString() {
			switch (this) {
			case AVAILABLE:
				return "Available";
			case IN_USE:
				return "In Use";
			case CYCLE_COMPLETE:
				return "Cycle Complete";
			default:
				return "Unavailable";
			}
		}
		
		public static Status lookup(String status) {
			if (status.toLowerCase(Locale.getDefault()).contains("available")) {
				return Status.AVAILABLE;
			} else if (status.toLowerCase(Locale.getDefault()).contains("in use")) {
				return Status.IN_USE;
			} else if (status.toLowerCase(Locale.getDefault()).contains("cycle complete")) {
				return Status.CYCLE_COMPLETE;
			} else {
				return Status.UNAVAILABLE;
			}
		}
	}

	@Override
	public String toString() {
		return "LaundryMachine [machineNumber=" + machineNumber
				+ ", minutesLeft=" + minutesLeft + ", type=" + type
				+ ", status=" + status + "]";
	}
	
}
