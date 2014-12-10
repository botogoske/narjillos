package org.nusco.narjillos;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.nusco.narjillos.genomics.DNA;
import org.nusco.narjillos.genomics.GenePool;
import org.nusco.narjillos.serializer.JSON;

/**
 * A utility program that reads a gene pool from a file, identifies the most
 * successfull DNA in the pool, and prints out its entire ancestry - from the
 * first randomly generates ancestor onwards.
 */
public class Ancestry {

	public static void main(String[] args) throws IOException {
		printAncestry(args[0]);
	}

	private static void printAncestry(String genePoolFile) throws IOException {
		System.out.println(">Reading file \"" + genePoolFile + "\"...");
		String json = new String(Files.readAllBytes(Paths.get(genePoolFile)), Charset.defaultCharset());

		System.out.println(">Deserializing gene pool from JSON...");
		GenePool genePool = JSON.fromJson(json, GenePool.class);
		System.out.println("  > Current gene pool size: " + genePool.getCurrentPoolSize());
		System.out.println("  > Historical gene pool size: " + genePool.getHistoricalPoolSize());

		System.out.println(">Identifying most successful DNA...");
		DNA mostSuccessfulDNA = genePool.getMostSuccessfulDNA();

		System.out.println(">Extracting ancestry...");
		List<DNA> ancestry = genePool.getAncestry(mostSuccessfulDNA);

		for (DNA dna : ancestry)
			System.out.println(" " + dna);
	}
}
