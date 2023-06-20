package org.snpeff.collections;

import gnu.trove.map.hash.TLongIntHashMap;

/**
 * A Hash<long, long[]> using primitive types instead or warped object
 * The idea is to be able to add many long values for each key
 * 
 * This could be implemented by simply doing HashMap<Long, Set<Long> > (but it 
 * would consume much more memory)

 * Note: We call each 'long[]' a bucket
 * 
 * WARNING: This collection does NOT allow elements to be deleted! But you can replace values.
 * 
 * @author pcingola
 */
public class HashLongLongArray {

	public static final long EMPTY_VALUE = 0; // Marking an empty value
	static final long EMPTY_VALUE_TROVE = 0; // This is Trove's implementation
	static final int INITIAL_NUMBER_OF_BUCKETS = 1024; // Initial number of buckets
	static final int INITIAL_BUCKET_SIZE = 2; // Initial bucket size

	static final float BUCKET_EXPANSION_FACTOR = 1.3f;// How much do we resize the buckets
	static final int BUCKET_CAPACITY_EXPANSION_FACTOR = 2; // How much do we resize each bucket

	TLongIntHashMap hash; // The hash returns an index to 'values[]'
	long buckets[][]; // This is where the references are stored
	int bucketsUsed;
	int bucketFirstAvailable[];
	int latestBucketLength = 0;

	public HashLongLongArray() {
		// Create and initialize hash
		hash = new TLongIntHashMap();

		// Create and initialize buckets
		buckets = new long[INITIAL_NUMBER_OF_BUCKETS][];
		bucketFirstAvailable = new int[INITIAL_NUMBER_OF_BUCKETS];

		// Bucket number zero is reserved (in the hash zero means 'not found')
		buckets[0] = new long[0];
		bucketsUsed = 1;
	}

	/**
	 * Return true if value is in the hash 
	 * @param key
	 * @param value
	 */
	public boolean contains(long key, long value) {
		int bucketNumber = getBucketNumber(key);
		if( bucketNumber == 0 ) return false; // Bucket not found 

		long bucket[] = buckets[bucketNumber];
		if( bucket == null ) return false; // Null bucket 

		int len = bucketFirstAvailable[bucketNumber];
		for( int i = 0; i < len; i++ )
			if( bucket[i] == value ) return true; // Value changed

		return false; // Value not found 
	}

	/**
	 * Return all values for a given key
	 * 
	 * WARNING: Not all elements in a bucket are used. Use getBucketLength(key) to know 
	 * how many elements are used
	 * 
	 * @param key
	 * @return All associated values, or null if key is not found
	 */
	public long[] getBucket(long key) {
		int bnum = getBucketNumber(key);
		if( bnum == EMPTY_VALUE_TROVE ) return null;
		latestBucketLength = bucketFirstAvailable[bnum];
		return buckets[bnum];
	}

	/**
	 * Return used length of a bucket
	 * @param key
	 * @return
	 */
	public int getBucketLength(long key) {
		int bnum = getBucketNumber(key);
		if( bnum == EMPTY_VALUE_TROVE ) return 0;
		return bucketFirstAvailable[bnum];
	}

	/**
	 * Find a bucket number using a key
	 * @param key
	 * @return Non zero bucket number. Zero if 'not found'
	 */
	int getBucketNumber(long key) {
		return hash.get(key);
	}

	/**
	 * Get bucket length for latest bucket search
	 * WARNING: Obviously this is not a thread safe operation 
	 * @return
	 */
	public int getLatestBucketLength() {
		return latestBucketLength;
	}

	/**
	 * Return an array with all the keys to this hash
	 * @return
	 */
	public long[] keys() {
		return hash.keys();
	}

	/**
	 * Insert a <key, value> pair
	 * 
	 * How does it work?
	 * 	- bucket_number = hash.get( key ) 
	 * 	- bu = bucket[ bucket_number ]
	 * 	- append 'value' to 'bu' 
	 * 
	 * @param key
	 * @param value
	 */
	public void put(long key, long value) {
		// Get reference to 'values'
		int bucketNumber = getBucketNumber(key);

		if( bucketNumber == 0 ) { // Not found?
			bucketNumber = bucketsUsed++;
			hash.put(key, bucketNumber);

			// No more buckets available? => Resize
			if( bucketsUsed > buckets.length ) {
				int newSize = (int) (buckets.length * BUCKET_EXPANSION_FACTOR);
				long newBuckets[][] = new long[newSize][];
				int newBucketFa[] = new int[newSize];
				System.arraycopy(buckets, 0, newBuckets, 0, buckets.length);
				System.arraycopy(bucketFirstAvailable, 0, newBucketFa, 0, buckets.length);
				buckets = newBuckets;
				bucketFirstAvailable = newBucketFa;
			}
		}

		// Get references array
		long bucket[] = buckets[bucketNumber];
		if( bucket == null ) {
			bucket = buckets[bucketNumber] = new long[INITIAL_BUCKET_SIZE];
			// if( EMPTY_VALUE != 0 ) { // Only if this ever changes, we need to initialize
			// 	for( int i = 0; i < bucket.length; i++ )
			// 		bucket[i] = EMPTY_VALUE;
			// }
		}

		// Find first available position in 'bucket'
		int bucketIndex = bucketFirstAvailable[bucketNumber];
		bucketFirstAvailable[bucketNumber]++;

		// Nothing available? => resize
		if( bucketIndex >= bucket.length ) {
			int newSize = bucket.length * BUCKET_CAPACITY_EXPANSION_FACTOR;
			long newRefs[] = new long[newSize];
			System.arraycopy(bucket, 0, newRefs, 0, bucket.length);
			bucket = buckets[bucketNumber] = newRefs;
		}

		// Add new value to bucket
		bucket[bucketIndex] = value;
	}

	/**
	 * Replace a value with newValue 
	 * @param key
	 * @param value
	 */
	public boolean replace(long key, long value, long newValue) {
		int bucketNumber = getBucketNumber(key);
		if( bucketNumber == 0 ) return false; // Bucket not found 

		long bucket[] = buckets[bucketNumber];
		if( bucket == null ) return false; // Null bucket 

		int len = bucketFirstAvailable[bucketNumber];
		for( int i = 0; i < len; i++ )
			if( bucket[i] == value ) {
				bucket[i] = newValue;
				return true; // Value changed
			}

		return false; // Value not found 
	}

	@Override
	public String toString() {
		long total = 0, min = Integer.MAX_VALUE, max = 0, bucketsLen = 0;
		for( int i = 1; i < bucketsUsed; i++ ) {
			// Find used length
			int bucketIndex = bucketFirstAvailable[i];
			int len = bucketIndex;

			// Do some stats about usage
			total += len;
			max = Math.max(max, len);
			min = Math.min(min, len);
			bucketsLen += buckets[i].length;
		}

		double avg = ((double) total) / ((double) bucketsUsed);
		return ("References: " + total + "\tbucketsLen: " + bucketsLen + "\tBuckets: " + bucketsUsed + "\tBucket size: min: " + min + ", max: " + max + ", avg: " + avg + "\tHash.size: " + hash.size());
	}
}
