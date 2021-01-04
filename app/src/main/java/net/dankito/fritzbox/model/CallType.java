package net.dankito.fritzbox.model;

import java.io.Serializable;


/**
 * This enumeration contains all known call types. Every call type contains the
 * value with which they are identified at the FritzBox.
 *
 * @author Ingo Schwarz
 */
public enum CallType implements Serializable {

	INCOMING_CALL(1), MISSED_CALL(2), REJECTED_CALL(3), OUTGOING_CALL(4), ONGOING_INCOMING_CALL(5), ONGOING_OUTGOING_CALL(6), UNKNOWN(99);


	private final int value;

	CallType(final int value) {
		this.value = value;
	}

	/**
	 * Returns the value of the CallType the FritzBox is working with.
	 *
	 * @return The call type value as integer.
	 */
	public int getValue() {
		return this.value;
	}

	/**
	 * Creates a call type object from the given value.
	 *
	 * @param x
	 *            The value to create a CallType for.
	 * @return The created CallType.
	 */
	public static CallType fromInteger(final int x) {

		switch (x) {
		case 1:
			return INCOMING_CALL;
		case 2:
			return MISSED_CALL;
		case 3:
			return REJECTED_CALL;
		case 4:
			return OUTGOING_CALL;
		case 5:
			return ONGOING_INCOMING_CALL;
		case 6:
			return ONGOING_OUTGOING_CALL;
		default:
			return UNKNOWN;
		}
	}

}
