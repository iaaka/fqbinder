package fqbinder;

public class SeqQ {
	final String name,seq,qual;

	
	public SeqQ(String name, String seq, String qual) {
		super();
		this.name = name;
		this.seq = seq;
		this.qual = qual;
	}

	
	@Override
	public String toString() {
		return name+"\n"+seq+"\n+\n"+qual+"\n";
	}
}
