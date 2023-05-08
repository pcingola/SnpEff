package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.fileIterator.SeekableBufferedReader;
import org.snpeff.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Seekable file reader test case
 *
 * @author pcingola
 */
public class TestCasesSeekableReader extends TestCasesBase {

    /**
     * Calculate a simple hash using SeekableBufferedReader
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    long calcHash(String fileName) throws IOException {
        SeekableBufferedReader sbr = new SeekableBufferedReader(fileName);
        String line;
        long hash = 0;
        while ((line = sbr.readLine()) != null) {
            hash += line.hashCode();
        }

        sbr.close();
        return hash;
    }

    /**
     * Same as calc hash, but using BufferedReader
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    long calcHashBufferedReader(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
        String line;
        long hash = 0;
        while ((line = br.readLine()) != null) {
            hash += line.hashCode();
        }
        br.close();
        return hash;
    }

    @Test
    public void test_00() throws IOException {
        Log.debug("Test");
        String fileName = path("testLukas.vcf");
        long hashExp = calcHashBufferedReader(fileName);
        long hash = calcHash(fileName);
        System.out.println(String.format("%016x\t%016x\t%s", hashExp, hash, fileName));

        assertEquals(hashExp, hash);
    }

    /**
     * Basic parsing
     */
    @Test
    public void test_01() throws IOException {
        Log.debug("Test");
        String fileName = path("testLukas.vcf");
        long hashExp = calcHashBufferedReader(fileName);
        long hash = calcHash(fileName);
        System.out.printf("%016x\t%016x\t%s%n", hashExp, hash, fileName);
        assertEquals(hashExp, hash);
    }
}
