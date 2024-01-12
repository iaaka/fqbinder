package fqbinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;

public interface SeqFixer {
	public FixedSeq fix(String s);
}


class FixedSeq {
	String seq;
	int mistmatches;
	
	public FixedSeq(String seq,int mistmatches) {
		this.seq = seq;
		this.mistmatches = mistmatches;
	}	
}


class HashSeqFixer implements SeqFixer{
	HashMap<String, FixedSeq> map;
	
	public HashSeqFixer(String whitelistPath,char[] alphabet,int maxMism) throws Exception {
		map = new HashMap<>();
		HashSet<String> blacklist = new HashSet<>();            // to keep sequences that can be fixed to more than one whitelisted barcode 
		BufferedReader br = new BufferedReader(new FileReader(new File(whitelistPath)));
		for(String l = br.readLine();l!=null;l = br.readLine()) {
			HashMap<String,Integer> mseqs = new HashMap<>();	// final list of mutants
			mseqs.put(l, 0);
			HashSet<String> toMutate = new HashSet<String>();   // sequences from previous round
			toMutate.add(l);
			HashSet<String> mcurr = new HashSet<String>();      // sequences from current round
			for(int i=0;i<maxMism;i++) {
				 for(String s : toMutate) 
					 mcurr.addAll(mutate(s,alphabet));
				 for(String s : mcurr) {
					 if(!mseqs.containsKey(s))                  // to retains versions with smallest number of mutations
						 mseqs.put(s, i+1);
				 }
				 toMutate = mcurr;
				 mcurr = new HashSet<String>();
			}
			
			
			for(String s : mseqs.keySet()) {
				if(!blacklist.contains(s)) {
					FixedSeq r = map.put(s, new FixedSeq(l,mseqs.get(s)));
					if(r!=null) {
						// add sequence to blacklist and remove from map if is is ambiguous
						blacklist.add(s);
						map.remove(s);
						// other option is to die...
						//br.close();
						//throw new Exception("Whitelist sequence '"+l+"' and '"+r.seq+"' clashes." );
					}
				}
			}
		}
		br.close();
	}
	
	
	private HashSet<String> mutate(String s,char[] alphabet) {
		HashSet<String> r = new HashSet<String>();
		char[] chars = s.toCharArray();
		for(int i = 0;i<chars.length;i++) {
			for(char a : alphabet) {
				if(a != chars[i]) {
					char[] t = chars.clone();
					t[i] = a;
					r.add(new String(t));
				}
			}
		}
		return r;
	}
	
	@Override
	public FixedSeq fix(String s) {
		FixedSeq r = map.get(s);
		if(r == null)
			r = new FixedSeq(s,-1);
		return r;
	}
	
}