package fqbinder;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class FqReader {
	BufferedReader fq;
	SeqQ current;
	
	public FqReader(String fname) throws FileNotFoundException, IOException {
		if(fname.endsWith(".gz")) {
			fq = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fname),65536)));
		}else {
			fq = new BufferedReader(new FileReader(fname));
		}
	}
	
	public void next() throws IOException {
		String n = fq.readLine();
		String s = fq.readLine();
		String q = fq.readLine();
		q = fq.readLine();
		if(n == null) {
			fq.close();
			current = null;
		}else
			current = new SeqQ(n,s,q);
	}
	
	@Override
	protected void finalize() throws Throwable {
		fq.close();
	}
}
