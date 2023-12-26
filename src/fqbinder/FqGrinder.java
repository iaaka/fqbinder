package fqbinder;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ThreadPoolExecutor;

import org.anarres.parallelgzip.ParallelGZIPEnvironment;


class SegmentParams{
	String readerName;
	int from;
	int to;
	String fixerName;
	int fixer_mismatches;
	
	public SegmentParams(String pars) {
		String[] pars1 = pars.split(":");
		readerName = pars1[0];
		String[] coors = pars1[1].split("-");
		from = Integer.parseInt(coors[0]);
		to   = Integer.parseInt(coors[1]);
		if(pars1.length>2) { // so it has fixer
			String[] fix = pars1[2].split("-");
			fixerName = fix[0];
			fixer_mismatches = Integer.parseInt(fix[1]);
		}
	}
}

class BinderParams{
	String writerName;
	SegmentParams[] segments;
	
	public BinderParams(String pars) {
		String[] pars1 = pars.split("=");
		writerName = pars1[0];
		String[] segs = pars1[1].split("\\+");
		segments = new SegmentParams[segs.length];
		for(int i =0;i<segs.length;i++)
			segments[i] = new SegmentParams(segs[i]);	
	}
}

class GrinderParams{
	String[][]  params;
	String[] inFileNames;
	private HashMap<String,Integer> inMap;
	BinderParams[] binders;
	String[] wlFileNames;
	HashMap<String,Integer> wlMap;
	int threads = 1;
	
	private void printHelp() {
		System.out.println("FqBinder by Pavel Mazin; iaa.aka@gmail.com");
		System.out.println("Usage:");
		System.out.println("java -jar fqbinder.jar -iR1 R1.fq.gz -i2 -wl1 whitelist.txt R2.fq.gz -o out1.fq.gz=R1:0-10:l1-2+2:20-30+1:50-60");
		System.out.println("Details:");
		System.out.println("Input fastq files can be provided by parameters with names prefixes by '-i', everything that follows\n"
						+ " will be used later to reference this input. Another way to provide input files is by single '-i' parameter followed\n"
						+ " by path to input files with variable name replaced by '*' (it is usually I1/I2/R1/R2/R3 part). Input files then can\n"
						+ " be referenced by this variable part.\n"
						+ "Whitelist files (one sequence per line) can be provided by parameters prefixed with '-w'.\n"
						+ "Output files should  be specified by '-o' option (as many '-o's as needed can be specified). Each -o should\n"
						+ " provide details on how to construct output files. It consist of:\n"
						+ "\t[output file name]=[segment]+<+segment>\n"
						+ "Each segment contains reference to source file (suffix of corresponding '-i'), coordinates and options:\n"
						+ "\t[input reference]:[from]-[to]<:options>.\n"
						+ "For now just one option is avaliable:\n"
						+ " 1. Fixing by whitelist. It can be specified by [whitelist reference]-[number of mismatches], for example 'l1-2'.\n"
						+ "    All sequences within given distance from one of whitelisted sequences will be fixed to whitelisted one.");
	}
	
	public GrinderParams(String[] args){
		if(args.length==0 || args[0].startsWith("-h")) {
			printHelp();
			System.exit(0);
		}
			
		params = new String[args.length/2][];
		for(int i = 0;i< args.length;i+=2)
			params[i/2] = new String[] {args[i],args[i+1]};
		threads = getIntArg("-t", threads);
		
		String[][] readersp = getSerialParams("-i");
		String[][] writersp = getSerialParams("-o");
		String[][] whitelistsp = getSerialParams("-w");
			
		wlFileNames = new String[whitelistsp.length];
		// The map can point directly to filenames, but I use indexes for consistency with readers and in case I decide that I'm happy to have just one instance of fixer per each whitelist. 
		wlMap = new HashMap<>();
		for(int i =0;i<whitelistsp.length;i++) {
			wlFileNames[i] = whitelistsp[i][1];
			wlMap.put(whitelistsp[i][0].substring(2), i);
		}
		HashSet<String> readerNames = new HashSet<>(); 
		binders = new BinderParams[writersp.length];
		for(int i =0;i<writersp.length;i++) {
			binders[i] = new BinderParams(writersp[i][1]);
			for(SegmentParams p : binders[i].segments)
				readerNames.add(p.readerName);
		}
		
		inMap = new HashMap<>();
		if(readersp[0][0].equals("-i")) {  // if input file specified by single -i, then take variable name part from binder segment reader names
			String wildcard = readersp[0][1]; 
			inFileNames = readerNames.toArray(new  String[readerNames.size()]);
			for(int i=0;i<inFileNames.length;i++) {
				inMap.put(inFileNames[i], i);
				String[] t = wildcard.split("\\*",2);
				inFileNames[i] = t[0] + inFileNames[i] + t[1]; 
			}
		}else { // each input file is given explicitly
			inFileNames = new String[readersp.length];
			for(int i =0;i<readersp.length;i++) {
				inFileNames[i] = readersp[i][1];
				inMap.put(readersp[i][0].substring(2), i);
			}
		}	
	}
	
	public int getInputIndex(String name) throws Exception {
		if(inMap.containsKey(name))
			return inMap.get(name);
		throw new Exception("Input '"+name+"' is not registered.");
	}
	
	private String[][] getSerialParams(String prefix) {
		ArrayList<String[]> res = new ArrayList<>();
		for(int i =0;i<params.length;i++) {
			if(params[i][0].startsWith(prefix))
				res.add(params[i]);
		}
		return res.toArray(new String[res.size()][]);
	}
	
	private int getIntArg(String name,int def) {
		for(int i =0;i<params.length;i++) {
			if(params[i][0].equals(name))
				def = Integer.parseInt(params[i][1]);
		}
		return def;
	}

}

public class FqGrinder {
	GrinderParams params;
	FqReader[] readers;
	FqWriter[] writers;
	Binder[] binders;
	static char[] alphabet = new char[]{'A','T','G','C','N'};
	
	public FqGrinder(String[] args) throws Exception {
		params = new GrinderParams(args);
	
		ThreadPoolExecutor  tpe = ParallelGZIPEnvironment.newThreadPoolExecutor(params.threads);
		if(params.binders.length==0) {
			System.err.print("no outputs requested");
			System.exit(0);
		}
		readers = new FqReader[params.inFileNames.length];
		for(int i = 0;i<params.inFileNames.length;i++)
			readers[i] = new FqReader(params.inFileNames[i]);
		
		writers = new FqWriter[params.binders.length];
		binders = new Binder[params.binders.length];
		for(int i = 0;i<params.binders.length;i++) {
			writers[i] = new FqWriter(params.binders[i].writerName,tpe);
			binders[i] = new Binder(params.binders[i].segments.length);
			for(SegmentParams sp : params.binders[i].segments) {
				if(sp.fixerName==null)
					binders[i].addReader(readers[params.getInputIndex(sp.readerName)], sp.from, sp.to);
				else {
					SeqFixer fix = new HashSeqFixer(params.wlFileNames[params.wlMap.get(sp.fixerName)], alphabet, sp.fixer_mismatches);
					binders[i].addReader(readers[params.getInputIndex(sp.readerName)],fix, sp.from, sp.to);
				}
			}
		}
	}
	
	public void process() throws IOException {
		while(true){
			for(FqReader r : readers) r.next();
			if(readers[0].current == null) break;
			for(int i = 0;i<binders.length;i++) {
				writers[i].write(binders[i].bind());
			}
		}
		for(FqWriter w : writers) w.close();
		
		// write stat
		for(int i=0;i<params.binders.length;i++) {
			FileWriter fw = new FileWriter(params.binders[i].writerName+"_stat.tab");
			fw.write(binders[i].toString());
			fw.close();
		}
	}
}

