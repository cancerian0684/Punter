package org.shunya.crackingjavainterviews;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by munichan on 9/1/2014.
 * Find the first non repeated character in a string : Technical interview question
 */
public class CollectionProblems {

    public static char findFirstNonRepeatableChar(String input) {
        List<Character> list = new ArrayList<>();
        for (char c : input.toCharArray()) {list.add(c);}
        Map<Character, Integer> map = new HashMap<>();
        list.forEach(ch -> map.compute(ch, (k, v) -> v == null ? 1 : v + 1));
//        map.forEach((k, v) -> System.out.println(k + " -> " + v));
        return list.stream().filter(ch -> map.get(ch) == 1).findFirst().get();
    }

    public static char findMostFreqUsedChar(String input) {
        List<Character> list = new ArrayList<>();
        for (char c : input.toCharArray()) {list.add(c);}
        Map<Character, Integer> map = new HashMap<>();
        list.forEach(ch -> map.compute(ch, (k, v) -> v == null ? 1 : v + 1));

//        map.forEach((k, v) -> System.out.println(k + " -> " + v));
        return list.stream().filter(ch -> map.get(ch) == 1).findFirst().get();
    }

    public static void wordsByFreqSorted(){
        List<String> keywords = Arrays.asList("Apple", "Ananas", "Mango", "Banana", "Beer","Apple","Mango","Mango");
        Map<Character, List<String>> result = keywords.stream().sorted()
                .collect(Collectors.groupingBy(it -> it.charAt(0)));
        System.out.println(result);
    }

    public static void wordsByFreqSorted2(){
        List<String> keywords = Arrays.asList("Apple", "Ananas", "Mango", "Banana", "Beer","Apple","Mango","Mango");
        Map<String, List<String>> result = keywords.stream().sorted()
                .collect(Collectors.groupingBy(it -> it.toString()));
        System.out.println(result);
    }

    public static void main(String[] args) {
        CollectionProblems.wordsByFreqSorted2();
        char munishchandel = CollectionProblems.findFirstNonRepeatableChar("munishchandel");
        System.out.println("first non repeatable character = " + munishchandel);
    }
}
