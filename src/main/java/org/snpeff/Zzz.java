package org.snpeff;

import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

enum AmClass {
    NOT_AVAILABLE
    , AMBIGUOUS
    , LIKELY_BENIGN
    , LIKELY_PATHOGENIC
}

public class Zzz {


    String fileName;

    public Zzz(String fileName) {
        this.fileName = fileName;
    }

    public static void main(String args[]) throws Exception {
        Log.info("Start");
        Zzz zzz = new Zzz(Gpr.HOME + "/snpEff/db/GRCh38/AlphaMissense/AlphaMissense_hg38.tsv");
        zzz.readAlphaMissense();
        Log.info("End");
    }

    public void readAlphaMissense() throws IOException {
        Log.info("Reading file: " + fileName);
        SnvScoresAmClass scoresAmClass = new SnvScoresAmClass();
        scoresAmClass.parse(fileName);
    }
}

class SnvScore<T> {
    public String chr;
    public int pos;
    public char ref;
    public char alt;
    public T score;

    public SnvScore() {
        this("", -1, 'N', 'N', null);
    }

    public SnvScore(String chr, int pos, char ref, char alt, T score) {
        this.chr = chr;
        this.pos = pos; // IMPORTANT: Positions are 0-based!
        this.ref = ref;
        this.alt = alt;
        this.score = score;
    }

    public boolean isValid() {
        return pos >= 0;
    }
}

/**
 * Scores that can be indexed using SNV / SNP coordiantes.
 * This means that the scores are indexed by a tuple of (chr, pos, ref, alt).
 * 
 * These scores are not loaded into memory, they can be indexed in a file (i.e. some sort of database)
 */
abstract class SnvScores<T> {
    public static final int MAX_GAP = 10;

    String dbFileName;
    String chromosome;
    Map<String, SnvScoresChr<T>> byChr;
    SnvScoresChr<T> currentChr;
    SnvScoresSegment<T> currentSegment;

    public SnvScores() {
        this.dbFileName = null;
        this.byChr = new HashMap<>();
    }

    public SnvScores(String dbFileName) {
        this();
        this.dbFileName = dbFileName;
    }

    public void add(SnvScoresChr<T> snvScoresChr) {
        if( byChr.containsKey(snvScoresChr.chromosome) ) {
            throw new RuntimeException("Duplicate segemnt for chromosome '" + snvScoresChr.chromosome + "'");
        }
        byChr.put(snvScoresChr.chromosome, snvScoresChr);
    }

    public T getScore(String chr, int pos, char ref, char alt) {
        SnvScoresChr<T> segments = byChr.get(chr);
        if (segments != null) {
            return segments.getScore(pos, ref, alt);
        }
        return null;
    }

    public void load() {
        // This does NOT need to load the scores into memory, only load the indexes that will be used to access the scores
        for (SnvScoresChr<T> segments : byChr.values()) {
            segments.load();
        }
    }

    public abstract SnvScoresChr<T> newScoresChr();

    public abstract SnvScoresSegment<T> newSegment();

    /**
     * Parse scores from a (raw) file. Typilcally this is some sort of TXT file.
     * @param fileName
     */
    public void parse(String fileName) {
        // Read the TXT file, create index and save them to files (index and scores files)
        var file = new File(fileName);
        if (!file.exists()) throw new RuntimeException("File not found: " + fileName);
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)))) {
            // FIXME: Update output file names to be in 'data' directory
            BufferedWriter scoreWriter = new BufferedWriter(new FileWriter(new File(fileName + ".scores")));
            BufferedWriter indexWriter = new BufferedWriter(new FileWriter(new File(fileName + ".index")));
            String line;
            SnvScore<T> prevScore = new SnvScore<T>();
            // Prepare to create indexes
            currentChr = newScoresChr();
            currentSegment = newSegment();
            List<SnvScore<T>> currentSegmentScores = new ArrayList<>();
            for (int lineNum=1; (line = reader.readLine())!= null && lineNum < 11000000; lineNum++) {
                SnvScore<T> score = parseLine(line);
                if (score == null) continue; // Skip empty or comment lines
                if (!score.chr.equals(prevScore.chr) && prevScore.isValid()) {
                    // New chromosome: Creating index structure
                    // Add current Chr and prepare for next chromosome
                    if( currentSegmentScores.size() > 0 ) {
                        currentSegment.setScores(currentSegmentScores);
                        currentSegment.save(indexWriter, scoreWriter);
                        currentChr.add(currentSegment);
                    }
                    add(currentChr);
                    System.out.println("Line: " + lineNum + "\tAdding new chr:" + currentChr);
                    currentChr = newScoresChr();
                    currentSegment = newSegment();
                    currentSegmentScores = new ArrayList<>();
                } else if (((score.pos < prevScore.pos) || (score.pos > prevScore.pos + MAX_GAP)) && prevScore.isValid()) {
                    // New Segment: Create a new SnvSegment
                    currentChr.add(currentSegment);
                    currentSegment = newSegment();
                    currentSegmentScores = new ArrayList<>();
                }
                // Update chr & pos
                currentChr.updateChromosome(score.chr);
                currentSegment.updateCoordinates(score.pos);
                currentSegmentScores.add(score);
                // Prepare for next line: Save current score
                prevScore = score;
            }
            // Finished reading file: Add the remainig Segment and ScoresChr
            if( currentSegmentScores.size() > 0 ) {
                currentSegment.setScores(currentSegmentScores);
                currentSegment.save(indexWriter, scoreWriter);
                currentChr.add(currentSegment);
            }
            add(currentChr);
            currentChr.save();
            // Close all files
            scoreWriter.close();
            indexWriter.close();
            reader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse a line from the file and return an SnvScore; i.e. a tuple of {chromosome, pos, ref, alt, score}
     */
    abstract SnvScore<T> parseLine(String line);

    public void save() {
        this.save(null);
    }

    /**
     * Saves the index and later on saves the scores
     * @param fileName
     */
    public void save(String dbFileName) {
        if( dbFileName != null ) this.dbFileName = dbFileName;
        // Saveds the index and later on saves the scores
        for (SnvScoresChr<T> segments : byChr.values()) {
            segments.save();
        }
    }

    public void setScore(String chr, int pos, char ref, char alt, T value) {
        SnvScoresChr<T> segments = byChr.get(chr);
        segments.setScore(pos, ref, alt, value);
    }
}

class SnvScoresAmClass extends SnvScores<AmClass> {

    @Override
    public SnvScoresChr<AmClass> newScoresChr() {
        return new SnvScoresChrAmClass(this);
    }

    @Override
    public SnvScoresSegment<AmClass> newSegment() {
        return new SnvScoresSegmentAmClass((SnvScoresChrAmClass)this.currentChr);
    }

    @Override
    SnvScore<AmClass> parseLine(String line) {
        if(line.startsWith("#") ) return null;
        String[] tokens = line.split("\t");
        return new SnvScore<>(tokens[0] // Chromosome
                                , Integer.parseInt(tokens[1]) - 1 // Position: Zero-based possitions
                                , tokens[2].charAt(0)   // Ref
                                , tokens[3].charAt(0)   // Alt
                                , AmClass.valueOf(tokens[9].toUpperCase()) // Score
                            );
    }
}

/**
 * These are all the scores for one chromosome
 */
class SnvScoresChr<T> { 
    SnvScores<T> scores;
    String chromosome;
    List<SnvScoresSegment<T>> segments;

    public SnvScoresChr(SnvScores<T> scores) {
        this.scores = scores;
        this.chromosome = null;
        this.segments = new ArrayList<>();
    }

    public void add(SnvScoresSegment<T> segment) {
        this.segments.add(segment);
    }

    public T getScore(int pos, char ref, char alt) {
        SnvScoresSegment<T> segment = getSegment(pos);
        return segment.getScore(pos, ref, alt);
    }

    public SnvScoresSegment<T> getSegment(int pos) {
        return null;
    }
    
    public void load() {
    }

    public void setScore(int pos, char ref, char alt, T value) {

    }

    public void save() {

    }

    public int size() {
        return segments.size();
    }

    public void updateChromosome(String chr) {
        if( this.chromosome!= null &&!this.chromosome.equals(chr) ) {
            throw new RuntimeException("Cannot update chromosome from '" + this.chromosome + "' to '" + chr + "'");
        }
        this.chromosome = chr;
    }

    public String toString() {
        return this.getClass().getName() + "{ chromosome='" + chromosome + "', size=" + size() + "}";
    }

}

class SnvScoresChrAmClass extends SnvScoresChr<AmClass> {

    public SnvScoresChrAmClass(SnvScoresAmClass scores) {
        super(scores);
    }

    @Override
    public AmClass getScore(int pos, char ref, char alt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getScore'");
    }

    @Override
    public void load() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'load'");
    }

    @Override
    public void setScore(int pos, char ref, char alt, AmClass value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setScore'");
    }

    @Override
    public void save() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }
}


abstract class SnvScoresSegment<T> {
    SnvScoresChr<T> segments;
    int start = Integer.MAX_VALUE;  // Start position. Half closed intervals, so this possition is included
    int end = Integer.MIN_VALUE;    // End position.   Half closed intervals, so this possition is NOT included
    int filePosStart, filePosEnd;

    public SnvScoresSegment(SnvScoresChr<T> segments) {
        this.segments = segments;
        this.start = Integer.MAX_VALUE;
        this.end = Integer.MIN_VALUE;
    }

    public void checkCoordinates() {
        assertTrue(start <= end, "Error: 'start' coordinate should be before or equal to 'end' coordinate: start = " + start + ", end = " + end);
    }

    /**
     * Create (in memory) structure for score
     */
    public abstract void createScoresSpace();

    public abstract T getScore(int pos, char ref, char alt);
    
    public abstract void loadIndex();

    public abstract void loadScores();

    void save(BufferedWriter indexWriter, BufferedWriter scoreWriter) {
        saveScores(scoreWriter);
        saveIndex(indexWriter);
    }

    abstract void saveIndex(BufferedWriter indexWriter);

    abstract void saveScores(BufferedWriter scoreWriter);

    public abstract void setScore(int pos, char ref, char alt, T score);

    public void setScores(List<SnvScore<T>> snvScores) {
        createScoresSpace();
        for(SnvScore<T> snvScore: snvScores) {
            setScore(snvScore.pos, snvScore.ref, snvScore.alt, snvScore.score);
        }
    }

    public int size() {
        return ( end > start ? (end - start + 1) : 0);
    }

    public void updateCoordinates(int pos) {
        if(start > pos) start = pos;
        if(end <= pos) end = pos + 1;
    }

    public String toString() {
        return "ScoreSegment{" +
                "start=" + start +
                ", end=" + end +
                ", filePosStart=" + filePosStart +
                ", filePosEnd=" + filePosEnd +
                ", size=" + size() +
                '}';
    }
}

/**
 * Scores for an AmClass
 * 
 * Since AmClass has 3 types { AMBIGUOUS, LIKELY_BENIGN, LIKELY_PATHOGENIC }, we code them in two bits
 * Then we can pack the scores for one possition (all alts) into a single byte.
 * 
 * We shift the scores within the byte depending on the ALT value, so the bits are:
 *      Bit Number:    76543210
 *      Base in ALT:   TTGGCCAA
 */
class SnvScoresSegmentAmClass extends SnvScoresSegment<AmClass> {

    byte[] scoresPacked;

    /** 
     * Constructor without any information
     * Usage example: When iterating over the source data file, so we don't the coordinates
     */
    public SnvScoresSegmentAmClass(SnvScoresChr<AmClass> segments) {
        super(segments);
    }

    public void createScoresSpace() {
        assertTrue(start < end, "Error: 'start' coordinate should be before 'end' coordinate: start = " + start + ", end = " + end);
        this.scoresPacked = new byte[end - start + 1];
    }


    private int getShift(char alt) {
        // Get bit rotation value
        return switch (alt) {
            case 'a', 'A' -> 0;
            case 'c', 'C' -> 2;
            case 'g', 'G' -> 4;
            case 't', 'T' -> 6;
            default -> throw new RuntimeException("Unknown ALT value: '" + alt + "'");
        };
    }

    private byte getMask(char alt) {
        // Get bit rotation value
        return switch (alt) {
            case 'a', 'A' -> 0b00000011;
            case 'c', 'C' -> 0b00001100;
            case 'g', 'G' -> 0b00110000;
            case 't', 'T' -> (byte) 0b11000000;
            default -> throw new RuntimeException("Unknown ALT value: '" + alt + "'");
        };
    }

    public AmClass getScore(int pos, char ref, char alt) {
        if(pos < start || pos >= end) return null;
        // Get shift value
        var shift = getShift(alt);
        // Get byte of scores
        byte scoresAlt = scoresPacked[pos - start];
        // Unpack AmClass number
        var amNum = (scoresAlt >> shift) & 0b11;
        // Return AmClass value
        return AmClass.values()[amNum];
    }
    
    public void load() {

    }

    public void setScore(int pos, char ref, char alt, AmClass value) {
        if(pos < start || pos >= end) throw new RuntimeException("Possition out of range: " + pos + ", start = " + start + ", end = " + end);
        // Get shift value
        var shift = getShift(alt);
        var mask = getMask(alt);
        // Get byte of scores
        int offset = pos - start;
        byte scoresAlt = scoresPacked[offset];
        // Set the 'middle bits' to the new value
        scoresAlt |= (scoresAlt & mask) | (value.ordinal() << shift);
        // Set the new scoresPacked
        scoresPacked[offset] = scoresAlt;
    }

    public void saveScores() {


    }

    @Override
    public void loadIndex() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadIndex'");
    }

    @Override
    public void loadScores() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadScores'");
    }

    @Override
    void saveIndex() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveIndex'");
    }
}
