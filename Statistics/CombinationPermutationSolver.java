
// Chris Ricchi
// 9/19/2023
// Combatination and Permutation Solver
// Write methods to solve combinations and permutations using the java API's BigInteger
// These methods are ALSO in the StatsLibrary class

import java.math.BigInteger;

public class CombinationPermutationSolver 
{
	
	// Method to return the factorial of N
    public static BigInteger factorial(int n) 
    {
        if (n < 0)
            throw new IllegalArgumentException("N cannot be a negative number.");
        
        //Using big integer
        BigInteger result = BigInteger.ONE;
        
        for (int j = 2; j <= n; j++)
            result = result.multiply(BigInteger.valueOf(j));
        
        return result;
    }

    // Method to return the combination of n and k (n! / (k!(n-k)!))
    public static BigInteger combinations(int n, int k) 
    {
        if (n < 0 || k < 0 || k > n)
            throw new IllegalArgumentException("N and K cannot be negative. K cannot exceed N.");

        BigInteger numerator = factorial(n);
        BigInteger denominator = factorial(k).multiply(factorial(n - k));
        
        return numerator.divide(denominator);
    }

    // Method to return the permutation of n and k (n! / (n-k)!)
    public static BigInteger permutations(int n, int k) 
    {
        if (n < 0 || k < 0 || k > n)
            throw new IllegalArgumentException("N and K cannot be negative. K cannot exceed N.");
        
        BigInteger numerator = factorial(n);
        BigInteger denominator = factorial(n - k);
        
        return numerator.divide(denominator);
    }

    public static void main(String[] args) 
    {
    	// COMBINATION TEST 
    	// HW example 2.11 on page 46: Find the number of ways of selecting two applicants out of five
    	
        int n = 5;
        int k = 2;

        BigInteger combinationTest = combinations(n, k);
        System.out.println("Combination Test: C(" + n + ", " + k + "): " + combinationTest);
        
        // PERMUTATION TEST 
        // HW example 2.37 on page 48: A businesswoman in Philadelphia is preparing an itinerary for a visit to six major cities. The
        // distance traveled, and hence the cost of the trip, will depend on the order in which she plans her route
        
        n = 6;
        k = 6;
        
        BigInteger permutationTest = permutations(n, k);
        
        System.out.println("Permutation Test: P(" + n + ", " + k + "): " + permutationTest);
    }
}