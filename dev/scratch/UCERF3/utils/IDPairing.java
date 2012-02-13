package scratch.UCERF3.utils;

/**
 * This class represents a pairing between two ID numbers, mostly useful with hashmaps.
 * The order of the numbers does matter here, so (0,1) is different than (1,0)
 * 
 * @author Kevin
 *
 */
public class IDPairing {
	
	private int id1, id2;
	
	public IDPairing(int id1, int id2) {
		this.id1 = id1;
		this.id2 = id2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id1;
		result = prime * result + id2;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IDPairing other = (IDPairing) obj;
		if (id1 != other.id1)
			return false;
		if (id2 != other.id2)
			return false;
		return true;
	}

	public int getID1() {
		return id1;
	}

	public int getID2() {
		return id2;
	}
	
	public IDPairing getReversed() {
		return new IDPairing(id2, id1);
	}

}
