package com.university.philosophers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple test that accepts file with philosophers state in format
 *  Philosopher <id>: <state>
 *
 * For example:
 *  Philosopher 3: HUNGRY
 *  Philosopher 3: EATING
 *  Philosopher 0: HUNGRY
 *  Philosopher 0: EATING
 *  Philosopher 4: HUNGRY
 *  Philosopher 2: HUNGRY
 */
public class DinnerTest {

    private static final String TEST_FILE = "dinner.log";
    private static final int N = 5;
    private static final Pattern p = Pattern.compile("Philosopher ([0-9]+): ([A-Z]+)");
    private final PhilosopherState[] states = new PhilosopherState[N];

    @BeforeEach
    void init() {
        for (int i = 0; i < N; i++) {
            states[i] = PhilosopherState.THINKING;
        }
    }

    @Test
    void testCorrectness() throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(TEST_FILE)))) {
            String line;
            while ((line = br.readLine()) != null) {
                checkStateCorrectness(line);
            }
        }
    }

    private void checkStateCorrectness(String line) {
        Matcher m = p.matcher(line);
        if (!m.find()) return;

        int id = Integer.parseInt(m.group(1));
        PhilosopherState state = PhilosopherState.valueOf(m.group(2));

        states[id] = state;
        if (state == PhilosopherState.THINKING || state == PhilosopherState.HUNGRY) return;
        if (state == PhilosopherState.EATING) {
            int left = getLeftNeighbour(id), right = getRightNeighbour(id);

            Assertions.assertNotEquals(states[left], PhilosopherState.EATING,
                    String.format("Philosopher %d is eating simultaneously with left neighbour", id));

            Assertions.assertNotEquals(states[right], PhilosopherState.EATING,
                    String.format("Philosopher %d is eating simultaneously with right neighbour", id));
        }
    }

    private int getRightNeighbour(int id) {
        return (id + 1) % N;
    }

    private int getLeftNeighbour(int id) {
        return Math.floorMod(id - 1, N);
    }

}
