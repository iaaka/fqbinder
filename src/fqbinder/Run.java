package fqbinder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

import org.anarres.parallelgzip.ParallelGZIPEnvironment;

/*
 * TODO
 * 1. Make proper help
 * 2. Reading/writing from/to std in/out
 * 4. Read filtering by presence of sequence in specified location
 * 5. Read name modification - removal part after space, sequence addition
 */

public class Run {
	public static boolean verbose = false; 
	static long time;
	
	public static void main(String[] args) throws Exception {
//		args = new String[]{"-i1","/home/mazin/work/fqbinder/GBM_SPA14137736/GBM_SPA14137736_S1_L002_R1_001.fastq",
//		                    "-i2","/home/mazin/work/fqbinder/GBM_SPA14137736/GBM_SPA14137736_S1_L002_R2_001.fastq",
//		                    "-o1","/home/mazin/work/fqbinder/GBM_SPA14137736/test1/out1.fastq.gz=1:0-10+2:20-30+1:50-60",
//		                    "-o2","/home/mazin/work/fqbinder/GBM_SPA14137736/test1/out2.fastq.gz=2:0-10+1:20-30+2:50-60"};
//		args = new String[]{"-i1","/home/mazin/work/fqbinder/GBM_SPA14137736/R1_1e6.fq.gz",
//                			"-i2","/home/mazin/work/fqbinder/GBM_SPA14137736/R2_1e6.fq.gz",
//                			"-o1","/home/mazin/work/fqbinder/GBM_SPA14137736/test1/out1_1.fastq.gz=1:0-10+2:20-30+1:50-60",
//                			"-o2","/home/mazin/work/fqbinder/GBM_SPA14137736/test1/out1_2.fastq.gz=2:0-10+1:20-30+2:50-60",
//                			"-t","1"};
//		args = new String[]{"-iR1","/home/mazin/work/fqbinder/GBM_SPA14137736/R1_1e6.fq.gz",
//    			"-iTTT","/home/mazin/work/fqbinder/GBM_SPA14137736/R2_1e6.fq.gz",
//    			"-o1","/home/mazin/work/fqbinder/GBM_SPA14137736/test1/out2_1.fastq.gz=R1:0-10+TTT:20-30+R1:50-60",
//    			"-o2","/home/mazin/work/fqbinder/GBM_SPA14137736/test1/out2_2.fastq.gz=TTT:0-10+R1:20-30+TTT:50-60",
//    			"-t","1"};
//		args = new String[]{"-i","/home/mazin/work/fqbinder/GBM_SPA14137736/*_1e6.fq.gz",
//				"-w1","/home/mazin/work/fqbinder/GBM_SPA14137736/testwl",
//    			"-o1","/home/mazin/work/fqbinder/GBM_SPA14137736/test1/out1_1.fastq.gz=R1:0-10+R2:20-30:1-2+R1:50-60",
//    			"-o2","/home/mazin/work/fqbinder/GBM_SPA14137736/test1/out2_1.fastq.gz=R2:0-10+R1:20-30+R2:50-60",
//    			"-t","1"};
		//time = System.currentTimeMillis();
		new FqGrinder(args).process();
	}
}
