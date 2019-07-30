package org.shunya.crackingjavainterviews;

import java.util.LinkedHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

public class NonRepeatingLetter {
    public static void main(String[] args) {
        findFirstNonRepeatingLetter(args[0], System.out::println);
    }

    private static void findFirstNonRepeatingLetter(String s, Consumer<Character> callback) {
        s.chars()
                .mapToObj(i -> (char) i)
                .collect(Collectors.groupingBy(identity(), LinkedHashMap::new, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() == 1L)
                .map(entry -> entry.getKey())
                .findFirst().map(c -> {
            callback.accept(c);
            return c;
        });
    }
}