package com.university.philosophers;

import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Dinner {

    int N;
    private final Philosopher[] philosophers;
    private final ReentrantLock m = new ReentrantLock();

    public Dinner(int N) throws InterruptedException {
        this.N = N;
        this.philosophers = new Philosopher[N];
        for (int i = 0; i < N; i++) {
            philosophers[i] = new Philosopher(i);
            philosophers[i].start();
        }
        for (int i = 0; i < N; i++) {
            philosophers[i].join();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new Dinner(5);
    }

    class Philosopher extends Thread {
        private final Random random = new Random();
        private boolean running = true;
        private final int id;
        private final Condition permission = m.newCondition();
        private int outgoing = 0;
        private PhilosopherState state = PhilosopherState.THINKING;

        public Philosopher(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    think();
                    takeForks();
                    eat();
                    putForks();
                } catch (InterruptedException e) {
                    finish();
                }
            }
        }

        private void eat() throws InterruptedException {
            synchronized (m) {
                state = PhilosopherState.EATING;
                logState();
            }
            waitForMillis();
        }

        private void think() throws InterruptedException {
            waitForMillis();
        }

        private void waitForMillis() throws InterruptedException {
            int low = 1000;
            int high = 2000;
            sleep(random.nextInt(high - low) + low);
        }

        private void takeForks() throws InterruptedException {
            requestForksAndBecomeHungry();
            try {
                m.lock();
                if (outgoing != 0) {
                    permission.await(); // wait until all neighbours start thinking
                }
            } finally {
                m.unlock();
            }
        }

        private void requestForksAndBecomeHungry() {
            synchronized (m) {
                int left = getLeftNeighbour(), right = getRightNeighbour();
                if (!philosophers[left].state.equals(PhilosopherState.THINKING)) {
                    outgoing++;
                }
                if (!philosophers[right].state.equals(PhilosopherState.THINKING)) {
                    outgoing++;
                }
                state = PhilosopherState.HUNGRY;
                logState();
            }
        }

        private void logState() {
            System.out.printf("Philosopher %d: %s%n", id, state);
        }

        private int getRightNeighbour() {
            return (id + 1) % N;
        }

        private int getLeftNeighbour() {
            return Math.floorMod(id - 1, N);
        }


        private void putForks() {
            releaseForksAndStartThinking();
        }

        private void releaseForksAndStartThinking() {
            int right = getRightNeighbour(), left = getLeftNeighbour();
            synchronized (m) {
                // neighbours wait for current iff it is not in the thinking state
                // updating "outgoing" counter and philosopher state is atomic
                if (philosophers[right].state != PhilosopherState.THINKING) {
                    philosophers[right].outgoing--;
                    checkAndSignal(right);
                }
                if (philosophers[left].state != PhilosopherState.THINKING) {
                    philosophers[left].outgoing--;
                    checkAndSignal(left);
                }
                state = PhilosopherState.THINKING;
                logState();
            }
        }

        private void checkAndSignal(int id) {
            try {
                m.lock();
                if (philosophers[id].outgoing == 0) {
                    philosophers[id].permission.signal();
                }
            } finally {
                m.unlock();
            }
        }

        private void finish() {
            running = false;
        }
    }

}
