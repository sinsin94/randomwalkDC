package clustering;

import java.util.Arrays;

//import weka.core.Instances;

public class KMeans extends Clustering {

	/**
	 *********************
	 * The constructor. Invoke the constructor of the superclass directly.
	 * 
	 * @param paraFilename
	 *            The data filename.
	 *********************
	 */
	public KMeans(String paraFilename) {
		super(paraFilename);
	}// of the first constructor

	/**
	 *********************
	 * KMeans algorithm.
	 * 
	 * @param paraDistanceMeasure
	 *            The distance measure, Manhattan or Euclidean
	 * @return
	 *********************
	 */
	public int[][] kMeans(int paraK, int paraDistanceMeasure) {
		int[][] tempClusters = new int[paraK][data.numInstances()];
		int[] tempClusterSizes = new int[paraK];
		double[][] tempCenters = new double[paraK][data.numAttributes()];
		// Used to judge whether or not the centers are stable
		double[][] tempLastRoundCenters = new double[paraK][data.numAttributes()];

		// Step 1. Select k centers randomly.
		for (int i = 0; i < paraK; i++) {
			for (int j = 0; j < data.numAttributes(); j++) {
				tempCenters[i][j] = data.instance(i).value(j);
				tempLastRoundCenters[i][j] = tempCenters[i][j];
				System.out.print("" + tempCenters[i][j] + ", ");
			} // Of for j
			System.out.println();
		} // Of for i

		// Step 2. Cluster and compute new centers.
		int tempRound = 0;
		while (true) {
			// Step 2.1 Allocate the points to the new clusters.
			double tempMaxDistance;
			int tempClusterIndex;
			// Reinitialize the cluster sizes
			for (int i = 0; i < tempClusterSizes.length; i++) {
				tempClusterSizes[i] = 0;
			} // Of for i

			// For each instance
			for (int i = 0; i < data.numInstances(); i++) {
				tempMaxDistance = Double.MAX_VALUE;
				tempClusterIndex = -1;
				// For each centers
				for (int j = 0; j < paraK; j++) {
					// Distance selection
					double tempDistance = 0;
					if (paraDistanceMeasure == MANHATTAN) {
						tempDistance = manhattanDistance(i, tempCenters[j]);
					} else {
						tempDistance = euclidianDistance(i, tempCenters[j]);
					} // Of if
					if (tempDistance < tempMaxDistance) {
						tempMaxDistance = tempDistance;
						tempClusterIndex = j;
					} // Of if
				} // Of for j

				tempClusters[tempClusterIndex][tempClusterSizes[tempClusterIndex]] = i;
				tempClusterSizes[tempClusterIndex]++;
			} // Of for i

			// Print the clusters
			System.out.println("\r\n***********Round " + tempRound);
			for (int i = 0; i < paraK; i++) {
				for (int j = 0; j < tempClusterSizes[i]; j++) {
					System.out.print("," + tempClusters[i][j]);
				} // Of for j
				System.out.println();
			} // Of for i

			// Step 2.2 Compute new centers.
			// Reinitialze them
			for (int i = 0; i < tempCenters.length; i++) {
				for (int j = 0; j < tempCenters[i].length; j++) {
					tempCenters[i][j] = 0;
				} // Of for j
			} // Of for j

			for (int i = 0; i < paraK; i++) {
				for (int j = 0; j < tempClusterSizes[i]; j++) {
					for (int k = 0; k < data.numAttributes(); k++) {
						tempCenters[i][k] += data.instance(tempClusters[i][j]).value(k) / tempClusterSizes[i];
					} // Of for k
				} // Of for j

				System.out.println("New center #" + i);
				for (int j = 0; j < tempCenters[i].length; j++) {
					System.out.print("," + tempCenters[i][j]);
				} // Of for j
				System.out.println();
			} // Of for i

			// Is it stable?
			if (matricesEqual(tempCenters, tempLastRoundCenters)) {
				break;
			} // Of if

			// Copy to tempLastRoundCenters
			for (int i = 0; i < paraK; i++) {
				for (int j = 0; j < data.numAttributes(); j++) {
					tempLastRoundCenters[i][j] = tempCenters[i][j];
				} // Of for j
			} // Of for i

			tempRound++;
		} // Of while

		clusters = new int[paraK][];
		for (int i = 0; i < paraK; i++) {
			// Determine the size of this block
			clusters[i] = new int[tempClusterSizes[i]];

			// Copy
			for (int j = 0; j < tempClusterSizes[i]; j++) {
				clusters[i][j] = tempClusters[i][j];
			} // Of for j
		} // Of for i

		// Print the clusters
		System.out.println("This is the final results");
		System.out.println(Arrays.deepToString(clusters));

		return clusters;
	}// Of kMeans

}// Of class KMeans
