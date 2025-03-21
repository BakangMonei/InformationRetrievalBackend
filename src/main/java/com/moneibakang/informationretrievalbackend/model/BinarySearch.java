package com.moneibakang.informationretrievalbackend.model;

/*
 * @Author: Monei Bakang
 * @Date: 21 March 2025
 * @Time: 01:55 hours
 */

public class BinarySearch {
    public static int search(int[] arr, int target) {
        int left = 0;
        int right = arr.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (arr[mid] == target) return mid;
            if (arr[mid] < target) left = mid + 1;
            else right = mid - 1;
        }

        return -1; // Target not found
    }

    public static void main(String[] args) {
        int[] sortedArray = {1, 3, 5, 7, 9, 11, 13, 15, 17};
        System.out.println(search(sortedArray, 7));  // Output: 3
    }
}