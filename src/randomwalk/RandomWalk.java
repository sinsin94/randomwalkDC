package randomwalk;

import java.util.Arrays;

import Util.mergeSort;
import clustering.*;
import matrix.CompressedMatrix;
import matrix.CompressedSymmetricMatrix;
import matrix.Triple;

public class RandomWalk {
	/**
	 * The block information.
	 */
	int[][] blockInformation;

	/**
	 * The cluster number for each node.
	 */
	int[] clusterNumbers;
	/**
	 * Is respective instance already classified? If so, we do not process it
	 * further.
	 */
	boolean[] alreadyClassified;
	/**
	 * Predicted labels.
	 */
	int[] predictedLabels;

	int paraTeachEachBlock = 3;
	int paraTeach;
	int numTeach;

	/**
	 ********************************** 
	 * Compute the maximal of a array.
	 ********************************** 
	 */
	public static int getMax(int[] arr) {

		int max = arr[0];

		for (int x = 1; x < arr.length; x++) {
			if (arr[x] > max)
				max = arr[x];

		}
		return max;

	}

	/**
	 ********************************** 
	 * Compute the maximal index of a array.
	 ********************************** 
	 */

	public int getMaxIndex(int[] paraArray) {
		int maxIndex = 0;
		int tempIndex = 0;
		int max = paraArray[0];

		for (int i = 0; i < paraArray.length; i++) {
			if (paraArray[i] > max) {
				max = paraArray[i];
				tempIndex = i;
			} // of if
		} // of for i
		maxIndex = tempIndex;
		return maxIndex;
	}// of getMaxIndex

	public double getPredictionAccuracy(int[] paraArrary, int[] paraArrarytwo) {
		double tempInCorrect = 0;
		// System.out.println("Incorrectly classified instances:");
		for (int i = 0; i < paraArrary.length; i++) {
			if (paraArrarytwo[i] != paraArrary[i]) {
				tempInCorrect++;
				System.out.print("" + i + ", ");
			} // Of if
		} // Of for i
		System.out.println();
		System.out.println("This is the incorrect:\r\n" + tempInCorrect);
		System.out.println("精度为：" + (paraArrary.length - numTeach - tempInCorrect) / (paraArrary.length - numTeach));
		return (paraArrary.length - numTeach - tempInCorrect) / (paraArrary.length - numTeach);

	}// Of getPredictionAccuracy

	/**
	 ********************************** 
	 * Compute block information
	 ********************************** 
	 */
	public int[][] computeBlockInformation(int[] clusterNumbers) {
		// Compute tempBlocks
		int tempBlocks = Integer.MIN_VALUE;
		for (int i = 0; i < clusterNumbers.length; i++) {
			if (clusterNumbers[i] > tempBlocks) {
				tempBlocks = clusterNumbers[i];
			}
		}

		blockInformation = new int[tempBlocks + 1][];

		for (int i = 0; i <= tempBlocks; i++) {
			// Scan to see how many elements
			int tempElements = 0;
			for (int j = 0; j < clusterNumbers.length; j++) {
				if (clusterNumbers[j] == i) {
					tempElements++;
				} // Of if
			} // Of for k

			// Copy to the list
			blockInformation[i] = new int[tempElements];
			tempElements = 0;
			for (int j = 0; j < clusterNumbers.length; j++) {
				if (clusterNumbers[j] == i) {
					blockInformation[i][tempElements] = j;
					tempElements++;
				} // Of if
			} // Of for k
		} // Of for i

		return blockInformation;
	}// Of computeBlockInformation

	/**
	 *********************
	 * The main algorithm.
	 * 
	 * @param paraFilename     The name of the decision table, or triple file.
	 * @param paraNumRounds    The rounds for random walk, each round update the
	 *                         weights, however does not change the topology.
	 * @param paraK            The maximal times for matrix multiplex.
	 * @param paraMinNeighbors For converting decision system into matrix only.
	 * @param paraCutThreshold For final clustering from the result matrix. Links
	 *                         smaller than the threshold will break.
	 *********************
	 */
	public void randomWalk(String paraFilename, int paraNumRounds, int paraK, int paraN, double paraDC, double paraCutThreshold,
			double percent) {

		// Step 1. Read data
		CompressedMatrix tempMatrix = new CompressedMatrix(paraFilename, paraDC, paraN);
		System.out.println("The original matrix is: " + tempMatrix);
		CompressedMatrix tempMultiplexion, tempCombinedTransitionMatrix;
		
		// Step 2. Run a number of rounds to obtain new matrices
		for (int i = 0; i < paraNumRounds; i++) {
			// Step 2.1 Compute probability matrix
			CompressedMatrix tempProbabilityMatrix = tempMatrix.computeTransitionProbabilities();
			System.out.println("\r\nThe probability matrix is:" + tempProbabilityMatrix);
			// Make a copy
			tempMultiplexion = new CompressedMatrix(tempProbabilityMatrix);

			// Step 2.2 Multiply and add
			// Reinitialize
			tempCombinedTransitionMatrix = new CompressedMatrix(tempProbabilityMatrix);
			// paraK step
			for (int j = 2; j <= paraK; j++) {
				System.out.println("j = " + j);
				tempMultiplexion = CompressedMatrix.multiply(tempMultiplexion, tempProbabilityMatrix);
				tempCombinedTransitionMatrix = CompressedMatrix.add(tempCombinedTransitionMatrix, tempMultiplexion);
			} // Of for j
			System.out.println("Find the error!" + tempMatrix);

			// Step 2.3 Distance between adjacent nodes
			for (int j = 0; j < tempMatrix.matrix.length; j++) {
				Triple tempCurrentTriple = tempMatrix.matrix[j].next;
				while (tempCurrentTriple != null) {
					// Update the weight
					tempCurrentTriple.weight = tempCombinedTransitionMatrix.neighborhoodSimilarity(j,
							tempCurrentTriple.column, paraK);

					tempCurrentTriple = tempCurrentTriple.next;
				} // Of while
			} // Of for j
		} // Of for i

		System.out.println("The new matrix is:" + tempMatrix);

		// Step 3. Depth-first clustering and output

		predictedLabels = new int[tempMatrix.matrix.length];
		alreadyClassified = new boolean[tempMatrix.matrix.length];
		System.out.println("predictedLabelspredictedLabels" + predictedLabels.length);
		for (int i = 0; i < predictedLabels.length; i++) {
			predictedLabels[i] = -1;
		} // of for i
		paraTeach = (int) (tempMatrix.matrix.length * percent);
		numTeach = 0;
		int numPredict = 0;
		int numVote = 0;
		int num = 0;
		while (true) {

			clusterNumbers = tempMatrix.depthFirstClustering(paraCutThreshold);
			computeBlockInformation(clusterNumbers);
			System.out.println("The block length :" + blockInformation.length);
			boolean[] tempBlockProcessed = new boolean[blockInformation.length];
			int tempUnProcessedBlocks = 0;
			for (int i = 0; i < blockInformation.length; i++) {
				tempBlockProcessed[i] = true;
				for (int j = 0; j < blockInformation[i].length; j++) {
					if (!alreadyClassified[blockInformation[i][j]]) {
						tempBlockProcessed[i] = false;
						tempUnProcessedBlocks++;
						break;
					} // of if
				} // of for j
			} // of for i
			if (tempUnProcessedBlocks == 0) {
				break;
			} // of if

			for (int i = 0; i < blockInformation.length; i++) {
				// Step 2.3.1
				if (tempBlockProcessed[i]) {
					continue;
				} // of if

				if (blockInformation[i].length < paraTeachEachBlock) {

					for (int j = 0; j < blockInformation[i].length; j++) {
						if (!alreadyClassified[blockInformation[i][j]]) {
							if (numTeach >= paraTeach) {
								break;
							} // of if
							predictedLabels[blockInformation[i][j]] = tempMatrix.reallable[blockInformation[i][j]];
							alreadyClassified[blockInformation[i][j]] = true;
							numTeach++;
						} // of if
					} // of for j
					
				} // of if

				//sort the density
				double[] tempBlockDensity = new double[blockInformation[i].length];
				for (int j = 0; j < blockInformation[i].length; j++) {
					tempBlockDensity[j] = tempMatrix.density[blockInformation[i][j]];
				} // of for j
				
				int[] ordDensity = new int[blockInformation[i].length];
				ordDensity = mergeSort.mergeSortToIndices(tempBlockDensity);

				int[] tempnewblockinfamation = new int[blockInformation[i].length];
				for (int j = 0; j < blockInformation[i].length; j++) {
					tempnewblockinfamation[ordDensity[j]] = blockInformation[i][j];
				} // of for j

				int tempNumTeach = 0;
				for (int j = 0; j < blockInformation[i].length; j++) {

					if (!alreadyClassified[tempnewblockinfamation[j]]) {
						if (numTeach >= paraTeach) {
							break;
						} // of if
						if (tempNumTeach >= paraTeachEachBlock) {
							break;
						} // of if
						predictedLabels[tempnewblockinfamation[j]] = tempMatrix.reallable[tempnewblockinfamation[j]];
						alreadyClassified[tempnewblockinfamation[j]] = true;
						numTeach++;
						tempNumTeach++;
						System.out.println("numTeach first = " + numTeach);
					} // of if
				} // of for j
			} // of for i
			boolean tempPure = true;
			for (int i = 0; i < blockInformation.length; i++) {
				if (tempBlockProcessed[i]) {
					continue;
				} // of if

				boolean tempFirstLable = true;

				int tempCurrentInstance;
				int tempLable = 0;

				for (int j = 0; j < blockInformation[i].length; j++) {
					tempCurrentInstance = blockInformation[i][j];
					if (alreadyClassified[tempCurrentInstance]) {

						if (tempFirstLable) {
							tempLable = predictedLabels[tempCurrentInstance];
							tempFirstLable = false;
						} else {
							if (tempLable != predictedLabels[tempCurrentInstance]) {
								tempPure = false;
								break;
							} // of if
						} // of if
					} // of if
				} // of for j

				if (tempPure) {

					int ClassifiedNum = 0;
					for (int j = 0; j < blockInformation[i].length; j++) {
						if (alreadyClassified[blockInformation[i][j]]) {

							ClassifiedNum++;
						} // of if
					} // of for
					if (ClassifiedNum >= blockInformation[i].length / 2.0) {
						for (int j = 0; j < blockInformation[i].length; j++) {
							if (!alreadyClassified[blockInformation[i][j]]) {
								predictedLabels[blockInformation[i][j]] = tempLable;
								alreadyClassified[blockInformation[i][j]] = true;
								numPredict++;
							} // of if
						} // of for j
					}
				} // of if
			} // of for i
			if (numTeach >= paraTeach) {
				break;
			} // of if
			paraCutThreshold *= 2;
			num += 1;
		} // of while

		System.out.println("vote beginning");
		// vote
		int max = getMax(predictedLabels);
		int[][] vote = new int[blockInformation.length][max + 1];
		int voteIndex = -1;

		for (int i = 0; i < blockInformation.length; i++) {

			for (int j = 0; j < blockInformation[i].length; j++) {

				for (int k = 0; k <= max; k++) {

					if (predictedLabels[blockInformation[i][j]] == k) {
						vote[i][k]++;
					} // of if
				} // of for k
			} // of for j

			voteIndex = getMaxIndex(vote[i]);
			for (int j = 0; j < blockInformation[i].length; j++) {
				if (predictedLabels[blockInformation[i][j]] == -1) {
					predictedLabels[blockInformation[i][j]] = voteIndex;
					numVote++;
				} // of if
			} // of for j
		} // of for i
		System.out.println("predictedLabels" + Arrays.toString(predictedLabels));
		getPredictionAccuracy(predictedLabels, tempMatrix.reallable);
		// Step 3'. Width-first clustering and output
		// try {
		// tempMatrix.widthFirstClustering(paraCutThreshold);
		// } catch (Exception ee) {
		// System.out.println("Error occurred in random walk: " + ee);
		// }//Of try

		//
	}// Of randomWalk

	public static void main(String args[]) {
		System.out.println("Let's randomly walk!");
		// KMeans tempMeans = new
		// KMeans("D:/workplace/randomwalk/data/iris.arff");
		// KMeans tempMeans = new
		// KMeans("D:/workspace/randomwalk/data/iris.arff");
		// Walk tempWalk = new Walk("D:/workspace/randomwalk/data/iris.arff");
		// int[] tempIntArray = {1, 2};

		// tempMeans.kMeans(3, KMeans.MANHATTAN);
		// tempMeans.kMeans(3, KMeans.EUCLIDEAN);
		// tempWalk.computeVkS(tempIntArray, 3);
		// double[][] tempMatrix = tempWalk.computeTransitionProbabilities();
		// double[][] tempTransition =
		// tempWalk.computeKStepTransitionProbabilities(100);
		// double[][] tempTransition =
		// tempWalk.computeAtMostKStepTransitionProbabilities(5);

		// double[][] tempNewGraph = tempWalk.ngSeparate(3);

		// System.out.println(Arrays.deepToString(tempMatrix));

		// System.out.println("The new graph is:\r\n" +
		// Arrays.deepToString(tempNewGraph));

		// CompressedSymmetricMatrix tempMatrix = new
		// CompressedSymmetricMatrix("D:/workspace/randomwalk/data/iris.arff",
		// 3);
		// CompressedSymmetricMatrix tempMatrix2 =
		// CompressedSymmetricMatrix.multiply(tempMatrix, tempMatrix);
		// CompressedSymmetricMatrix tempMatrix2 =S
		// CompressedSymmetricMatrix.weightMatrixToTransitionProbabilityMatrix(tempMatrix);

		// System.out.println("The new matrix is: \r\n" + tempMatrix2);
		// System.out.println("The accuracy is: " + tempMeans.computePurity());

		// new
		// RandomWalk().randomWalk("D:/workspace/randomwalk/data/example21.arff",
		// 1, 3);
		// new
		// RandomWalk().randomWalk("/Users/dengsiyu/eclipse/workspace/Coser_Triple/data/aaa/num/flame.arff",
		// 4, 2, 3, 3,0.1);
		new RandomWalk().randomWalk("H:/eclipse/workspace/Coser_Triple/data/aaa/num2/coil2000.arff", 5, 2, 5, 0.5, 0.05, 0.1);
	}// Of main
}// Of class RandomWalk
