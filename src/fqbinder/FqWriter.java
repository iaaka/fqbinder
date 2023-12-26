package fqbinder;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.GZIPOutputStream;

import org.anarres.parallelgzip.ParallelGZIPOutputStream;

public class FqWriter {
	BufferedWriter fq;
	
	public FqWriter(String fname) throws FileNotFoundException, IOException {
		if(fname.endsWith(".gz")) {
			fq = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(fname),65536)));
		}else {
			fq = new BufferedWriter(new FileWriter(fname));
		}
	}
	
	public FqWriter(String fname,ThreadPoolExecutor  tpe) throws FileNotFoundException, IOException {
		if(fname.endsWith(".gz")) {
			fq = new BufferedWriter(new OutputStreamWriter(new ParallelGZIPOutputStream(new FileOutputStream(fname),tpe)));
		}else {
			fq = new BufferedWriter(new FileWriter(fname));
		}
	}
	
	public void close() throws IOException {
		fq.close();
	}
	
	public void write(SeqQ o) throws IOException {
		fq.write(o.toString());
	}
	
	@Override
	protected void finalize() throws Throwable {
		fq.close();
	}

}
