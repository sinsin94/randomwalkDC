package Util;

import java.util.Arrays;


public class mergeSort {

	public static int[] mergeSortToIndices(double[] paraArray) {
		int tempLength = paraArray.length;
		int[][] resultMatrix = new int[2][tempLength];// 两个维度交换存储排序tempIndex控制

		// Initialize
		int tempIndex = 0;
		for (int i = 0; i < tempLength; i++) {
			resultMatrix[tempIndex][i] = i;
		} // Of for i
			// System.out.println("Initialize, resultMatrix = " +
			// Arrays.deepToString(resultMatrix));
		

		// Merge
		int tempCurrentLength = 1;
		// The indices for current merged groups.
		//int tempFirstStart, tempSecondStart, tempSecondEnd;
		while (tempCurrentLength < tempLength) {
			// System.out.println("tempCurrentLength = " + tempCurrentLength);
			// Divide into a number of groups
			// Here the boundary is adaptive to array length not equal to 2^k.
			// ceil是向上取整函数
			// System.out.println("dddddddddddddMath.ceil(tempLength + 0.0 /
			// tempCurrentLength) / 2 "+Math.ceil((tempLength + 0.0) /
			// tempCurrentLength/2.0) +","+tempCurrentLength+"<"+tempLength);
			for (int i = 0; i < Math.ceil((tempLength + 0.0) / tempCurrentLength / 2.0); i++) {// 定位到哪一块

				// Boundaries of the group
				// System.out.println("for循环；第"+i+"块排序"+";当前每块长度tempCurrentLength
				// :"+tempCurrentLength);
				int tempFirstStart = i * tempCurrentLength * 2;
				// tempSecondStart定位第二块开始的位置index
				int tempSecondStart = tempFirstStart + tempCurrentLength;// 可以用于判断是否是最后一小块，并做初始化的工作
				int tempSecondEnd = tempSecondStart + tempCurrentLength - 1;
				if (tempSecondEnd >= tempLength) { // 控制最后一小块。若超过了整体长度，则当tempSecondEnd定位到数组最后
					tempSecondEnd = tempLength - 1;
				} // Of if
					// Merge this group
				int tempFirstIndex = tempFirstStart;
				int tempSecondIndex = tempSecondStart;
				int tempCurrentIndex = tempFirstStart;
				// System.out.println("Before merge");
				if (tempSecondStart >= tempLength) {
					for (int j = tempFirstIndex; j < tempLength; j++) {
						resultMatrix[(tempIndex + 1) % 2][tempCurrentIndex] = resultMatrix[tempIndex % 2][j];
						//tempFirstIndex++;
						tempCurrentIndex++;
					} // Of for j
					break;
				} // Of if

				while ((tempFirstIndex <= tempSecondStart - 1) && (tempSecondIndex <= tempSecondEnd)) {// 真正开始做排序的工作
					if (paraArray[resultMatrix[tempIndex % 2][tempFirstIndex]] >= paraArray[resultMatrix[tempIndex
							% 2][tempSecondIndex]]) {
			
						resultMatrix[(tempIndex + 1) % 2][tempCurrentIndex] = resultMatrix[tempIndex
								% 2][tempFirstIndex];
		
						   tempFirstIndex++;
					} else {
						resultMatrix[(tempIndex + 1) % 2][tempCurrentIndex] = resultMatrix[tempIndex
								% 2][tempSecondIndex];
	
						tempSecondIndex++;
					} // Of if
					tempCurrentIndex++;

				} // Of while
					
				// Remaining part
				// System.out.println("Copying the remaining part");
				for (int j = tempFirstIndex; j < tempSecondStart; j++) {
					resultMatrix[(tempIndex + 1) % 2][tempCurrentIndex] = resultMatrix[tempIndex % 2][j];
					tempCurrentIndex++;

				} // Of for j
				for (int j = tempSecondIndex; j <= tempSecondEnd; j++) {
					resultMatrix[(tempIndex + 1) % 2][tempCurrentIndex] = resultMatrix[tempIndex % 2][j];
					tempCurrentIndex++;
				} // Of for j
					// paraArray=resultMatrix[0];
					// System.out.println("After copying remaining part");
					// System.out.println("Round " + tempIndex + ", resultMatrix = "
					// + Arrays.deepToString(resultMatrix));
				
			} // Of for i
				// System.out.println("Round " + tempIndex + ", resultMatrix = "
				// + Arrays.deepToString(resultMatrix));
			tempCurrentLength *= 2;
			tempIndex++;
		} // Of while
		System.out.println("resultSortedIndices = " + Arrays.toString(resultMatrix[tempIndex % 2]));
		System.out.println("该块的大小"+paraArray.length);
		return resultMatrix[tempIndex % 2];
	}// Of mergeSortToIndices

	public static void main(String args[]) {

		double[] clusterArray = { 123.0, 43, 12, 24, 56, 190, 67, 11};
		mergeSortToIndices(clusterArray);
	
	}// Of main



}
