package fqbinder;


public class Binder {
		FqReader[] readers;
		SeqFixer[] fixers;
		int[] from;
		int[] to;
		int len = 0;
		private int i = 0;
		char[] seq,qual;
		int[][] stat;
		int nreads=0;
		
		public Binder(int n) {
			this.readers = new FqReader[n];
			this.fixers = new SeqFixer[n];
			this.from = new int[n];
			this.to = new int[n];
		}
		
		public void addReader(FqReader r, int from, int to) {
			readers[i] = r;
			this.from[i] = from;
			this.to[i] = to;
			len += to - from;
			i++;
			seq = new char[len];
			qual = new char[len];
			stat = new int[i+1][10];
		}
		
		public void addReader(FqReader r, SeqFixer f,int from, int to) {
			fixers[i] = f;
			addReader(r,from,to);
		}
		
		public SeqQ bind() {	
			nreads++;
			String name = readers[0].current.name;
			int pos = 0;
			int maxmismatches = 0;
			for(int i =0;i<readers.length;i++) {
				SeqQ s = readers[i].current;
				int mismatches = 0;
				if(fixers[i] == null)
					s.seq.getChars(from[i], to[i], seq, pos);
				else {
					FixedSeq fs = fixers[i].fix(s.seq.substring(from[i], to[i]));
					fs.seq.getChars(0,fs.seq.length(),seq, pos);
					mismatches = fs.mistmatches;
				}
				if(mismatches!=-1)
					stat[i][mismatches]++;
				if(maxmismatches != -1)
				maxmismatches = Math.max(maxmismatches, mismatches);
				if(mismatches==-1)
					maxmismatches = -1;
				s.qual.getChars(from[i], to[i], qual, pos);
				pos += to[i]-from[i];
			}
			if(maxmismatches!=-1)
				stat[stat.length-1][maxmismatches]++;
			return  new SeqQ(name,new String(seq),new String(qual));
		}
		
		public String toString() {
			String res = "Segment\tTotal";
			for(int i=0;i<stat[0].length;i++)
				res += "\t" + i + "_mismatches";
			res += "\n";
			for(int i = 0;i<stat.length;i++) {
				String name = ""+i;
				if(i == stat.length - 1)
					name = "Whole";
				res += name+"\t"+nreads;
				for(int j=0;j<stat[i].length;j++)
					res += "\t"+stat[i][j];
				res += "\n";
			}
			return res;
		}
		
}
