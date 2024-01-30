import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    private static ExecutorService executorService;
    public static void main(String[] args) {
        // TODO: Seed your randomizer
        Random rand = new Random();
        rand.setSeed(0);

        // TODO: Get array size and thread count from user
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter array size: ");
        int array_size = scanner.nextInt();
        System.out.print("Enter thread count: ");
        int thread_count = scanner.nextInt();
        scanner.close();

        long start= System.nanoTime();
        // Check if the input is lower than 1, set numThreads to 1
        if (thread_count < 1) {
            System.out.print("Invalid input. Using default value of 1\n");
            thread_count = 1;
        }

        // TODO: Generate a random array of given size
        int[] array = new int[array_size];
        for(int i = 0; i < array_size; i++) {
            array[i] = rand.nextInt(100);
        }

        System.out.print("Unsorted array: ");
        for(int i = 0; i < array_size; i++) {
            System.out.print(array[i] + " ");
        }
        System.out.println();

        executorService = Executors.newFixedThreadPool(thread_count);

        // TODO: Call the generate_intervals method to generate the merge 
        // sequence
        List<Interval> intervals = generate_intervals(0, array_size - 1);

        if (thread_count == 1) {
            // TODO: Call merge on each interval in sequence
            for(Interval interval : intervals) {
                merge(array, interval.getStart(), interval.getEnd());
            }
        }
        else {
            // Concurrent version
            for (Interval interval : intervals) {
                executorService.submit(() -> merge(array, interval.getStart(), interval.getEnd()));
            }

            executorService.shutdown();
            try {
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.print("Sorted array: ");
        for(int i = 0; i < array_size; i++) {
            System.out.print(array[i] + " ");
        }

        long end = System.nanoTime();
        double runTime = end - start;
        runTime = runTime * 1e-9;


        // Sanity check
        if (isSorted(array)) {
            System.out.print("\nArray is sorted.");
        } else {
            System.out.print("\nArray is NOT sorted.");
        }

        System.out.println("\nRUNTIME: "+ runTime);


        // Once you get the single-threaded version to work, it's time to 
        // implement the concurrent version. Good luck :)
    }

    private static boolean isSorted(int[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }
        return true;
    }

    /*
    This function generates all the intervals for merge sort iteratively, given 
    the range of indices to sort. Algorithm runs in O(n).

    Parameters:
    start : int - start of range
    end : int - end of range (inclusive)

    Returns a list of Interval objects indicating the ranges for merge sort.
    */
    public static List<Interval> generate_intervals(int start, int end) {
        List<Interval> frontier = new ArrayList<>();
        frontier.add(new Interval(start,end));

        int i = 0;
        while(i < frontier.size()){
            int s = frontier.get(i).getStart();
            int e = frontier.get(i).getEnd();

            i++;

            // if base case
            if(s == e){
                continue;
            }

            // compute midpoint
            int m = s + (e - s) / 2;

            // add prerequisite intervals
            frontier.add(new Interval(m + 1,e));
            frontier.add(new Interval(s,m));
        }

        List<Interval> retval = new ArrayList<>();
        for(i = frontier.size() - 1; i >= 0; i--) {
            retval.add(frontier.get(i));
        }

        return retval;
    }

    /*
    This function performs the merge operation of merge sort.

    Parameters:
    array : vector<int> - array to sort
    s     : int         - start index of merge
    e     : int         - end index (inclusive) of merge
    */
    public static void merge(int[] array, int s, int e) {
        int m = s + (e - s) / 2;
        int[] left = new int[m - s + 1];
        int[] right = new int[e - m];
        int l_ptr = 0, r_ptr = 0;
        for(int i = s; i <= e; i++) {
            if(i <= m) {
                left[l_ptr++] = array[i];
            } else {
                right[r_ptr++] = array[i];
            }
        }
        l_ptr = r_ptr = 0;

        for(int i = s; i <= e; i++) {
            // no more elements on left half
            if(l_ptr == m - s + 1) {
                array[i] = right[r_ptr];
                r_ptr++;

                // no more elements on right half or left element comes first
            } else if(r_ptr == e - m || left[l_ptr] <= right[r_ptr]) {
                array[i] = left[l_ptr];
                l_ptr++;
            } else {
                array[i] = right[r_ptr];
                r_ptr++;
            }
        }
    }
}

class Interval {
    private int start;
    private int end;

    public Interval(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}