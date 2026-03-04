import java.io.*;
import java.util.*;

public class Main {

    // Operating Systems Project 1
    // Student: Amat Ullah, Course/Section: 6320, Date: 03-04-2026

    public static class Process {
        int pid, arrival, burst, priority;
        int remaining;
        int startTime = -1, completionTime, waitingTime, turnaroundTime;

        Process(int pid, int arrival, int burst, int priority) {
            this.pid = pid;
            this.arrival = arrival;
            this.burst = burst;
            this.priority = priority;
            this.remaining = burst;
        }
    }

    public static class Segment {
        String label;
        int start, end;

        Segment(String label, int start, int end) {
            this.label = label;
            this.start = start;
            this.end = end;
        }
    }

    public static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    public static List<Process> readProcesses(String filename) {
        List<Process> processes = new ArrayList<>();
        try (Scanner sc = new Scanner(new File(filename))) {
            if (sc.hasNextLine()) sc.nextLine(); 
            while (sc.hasNextInt()) {
                int pid = sc.nextInt();
                int arrival = sc.nextInt();
                int burst = sc.nextInt();
                int priority = sc.nextInt();

                if (arrival < 0 || burst <= 0) continue;
                processes.add(new Process(pid, arrival, burst, priority));
            }
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: Cannot find file: " + filename);
        }
        return processes;
    }

    public static List<Segment> scheduleFCFS(List<Process> processes) {
        processes.sort(Comparator
                .comparingInt((Process p) -> p.arrival)
                .thenComparingInt(p -> p.pid));

        List<Segment> gantt = new ArrayList<>();
        int time = 0;

        for (Process p : processes) {
            if (time < p.arrival) {
                gantt.add(new Segment("IDLE", time, p.arrival));
                time = p.arrival;
            }
            p.startTime = time;
            p.completionTime = time + p.burst;
            p.waitingTime = p.startTime - p.arrival;
            p.turnaroundTime = p.completionTime - p.arrival;

            gantt.add(new Segment("P" + p.pid, p.startTime, p.completionTime));
            time = p.completionTime;
        }
        return gantt;
    }

    public static List<Segment> schedulePriority(List<Process> processes) {
        processes.sort(Comparator
                .comparingInt((Process p) -> p.arrival)
                .thenComparingInt(p -> p.pid));

        List<Segment> gantt = new ArrayList<>();
        List<Process> ready = new ArrayList<>();
        int time = 0, idx = 0;

        while (idx < processes.size() || !ready.isEmpty()) {
            while (idx < processes.size() && processes.get(idx).arrival <= time) {
                ready.add(processes.get(idx++));
            }

            if (ready.isEmpty()) {
                int nextArr = processes.get(idx).arrival;
                gantt.add(new Segment("IDLE", time, nextArr));
                time = nextArr;
                continue;
            }

            ready.sort(Comparator
                    .comparingInt((Process p) -> p.priority) 
                    .thenComparingInt(p -> p.arrival)
                    .thenComparingInt(p -> p.pid));

            Process p = ready.remove(0);
            p.startTime = time;
            p.completionTime = time + p.burst;
            p.waitingTime = p.startTime - p.arrival;
            p.turnaroundTime = p.completionTime - p.arrival;

            gantt.add(new Segment("P" + p.pid, p.startTime, p.completionTime));
            time = p.completionTime;
        }
        return gantt;
    }

    public static List<Segment> scheduleSJF(List<Process> processes) {
        processes.sort(Comparator
                .comparingInt((Process p) -> p.arrival)
                .thenComparingInt(p -> p.pid));

        List<Segment> gantt = new ArrayList<>();
        List<Process> ready = new ArrayList<>();
        int time = 0, idx = 0;

        while (idx < processes.size() || !ready.isEmpty()) {
            while (idx < processes.size() && processes.get(idx).arrival <= time) {
                ready.add(processes.get(idx++));
            }

            if (ready.isEmpty()) {
                int nextArr = processes.get(idx).arrival;
                gantt.add(new Segment("IDLE", time, nextArr));
                time = nextArr;
                continue;
            }

            ready.sort(Comparator
                    .comparingInt((Process p) -> p.burst)
                    .thenComparingInt(p -> p.arrival)
                    .thenComparingInt(p -> p.pid));

            Process p = ready.remove(0);
            p.startTime = time;
            p.completionTime = time + p.burst;
            p.waitingTime = p.startTime - p.arrival;
            p.turnaroundTime = p.completionTime - p.arrival;

            gantt.add(new Segment("P" + p.pid, p.startTime, p.completionTime));
            time = p.completionTime;
        }
        return gantt;
    }

    public static List<Segment> scheduleRR(List<Process> processes, int quantum) {
        processes.sort(Comparator
                .comparingInt((Process p) -> p.arrival)
                .thenComparingInt(p -> p.pid));

        List<Segment> gantt = new ArrayList<>();
        Queue<Process> ready = new ArrayDeque<>();
        int time = 0, idx = 0;

        while (idx < processes.size() || !ready.isEmpty()) {
            while (idx < processes.size() && processes.get(idx).arrival <= time) {
                ready.add(processes.get(idx++));
            }

            if (ready.isEmpty()) {
                int nextArr = processes.get(idx).arrival;
                gantt.add(new Segment("IDLE", time, nextArr));
                time = nextArr;
                continue;
            }

            Process p = ready.poll();
            if (p.startTime == -1) p.startTime = time;

            int slice = Math.min(quantum, p.remaining);
            int start = time;
            int end = time + slice;
            p.remaining -= slice;
            time = end;

            gantt.add(new Segment("P" + p.pid, start, end));

            while (idx < processes.size() && processes.get(idx).arrival <= time) {
                ready.add(processes.get(idx++));
            }

            if (p.remaining > 0) {
                ready.add(p);
            } else {
                p.completionTime = end;
                p.turnaroundTime = p.completionTime - p.arrival;
                p.waitingTime = p.turnaroundTime - p.burst;
            }
        }
        return gantt;
    }

    public static void printGantt(List<Segment> gantt) {
        System.out.println("\nGantt Chart:");
        for (Segment s : gantt) System.out.print("| " + s.label + " ");
        System.out.println("|");

        if (gantt.isEmpty()) return;

        System.out.print(gantt.get(0).start);
        for (Segment s : gantt) {
            int space = ("| " + s.label + " ").length();
            String endStr = String.valueOf(s.end);
            for (int i = 0; i < space - endStr.length(); i++) System.out.print(" ");
            System.out.print(s.end);
        }
        System.out.println();
    }

    public static void printMetrics(List<Process> processes, List<Segment> gantt) {
        if (gantt.isEmpty() || processes.isEmpty()) {
            System.out.println("No data to display.");
            return;
        }
        double totalWT = 0, totalTAT = 0, totalBurst = 0;

        int startTime = gantt.get(0).start;
        int endTime = gantt.get(gantt.size() - 1).end;
        int totalTime = endTime - startTime;

        System.out.println("\nPID\tWT\tTAT");
        for (Process p : processes) {
            System.out.println(p.pid + "\t" + p.waitingTime + "\t" + p.turnaroundTime);
            totalWT += p.waitingTime;
            totalTAT += p.turnaroundTime;
            totalBurst += p.burst;
        }

        System.out.printf("\nAverage WT: %.2f\n", totalWT / processes.size());
        System.out.printf("Average TAT: %.2f\n", totalTAT / processes.size());

        double cpuUtil = (totalTime == 0) ? 0.0 : (100.0 * totalBurst / totalTime);
        System.out.printf("CPU Utilization: %.2f%%\n", cpuUtil);
    }

    public static void simulateLRU(Scanner sc) {
        int frames = readInt(sc, "\nEnter number of frames: ");
        int n = readInt(sc, "Enter number of page references: ");

        int[] pages = new int[n];
        System.out.println("Enter page reference sequence (one per line OR space separated is fine):");

        for (int i = 0; i < n; i++) {
            pages[i] = readInt(sc, "Page " + (i + 1) + ": ");
        }

        List<Integer> frameList = new ArrayList<>();
        int pageFaults = 0;

        System.out.println("\nPage\tFrames\t\tResult");

        for (int i = 0; i < n; i++) {
            int page = pages[i];

            if (!frameList.contains(page)) {
                pageFaults++;

                if (frameList.size() < frames) {
                    frameList.add(page);
                } else {
                    int lruPage = frameList.get(0);
                    int minLastUsed = Integer.MAX_VALUE;

                    for (int f : frameList) {
                        int lastUsed = -1;
                        for (int j = i - 1; j >= 0; j--) {
                            if (pages[j] == f) { lastUsed = j; break; }
                        }
                        if (lastUsed < minLastUsed) {
                            minLastUsed = lastUsed;
                            lruPage = f;
                        }
                    }

                    frameList.remove(Integer.valueOf(lruPage));
                    frameList.add(page);
                }

                System.out.println(page + "\t" + frameList + "\tFAULT");
            } else {
                System.out.println(page + "\t" + frameList + "\tHIT");
            }
        }

        System.out.println("\nTotal Page Faults: " + pageFaults);
    }

    public static void runFCFS() {
        String filename = "processes.txt";
        List<Process> processes = readProcesses(filename);

        if (processes.isEmpty()) {
            System.out.println("No processes loaded. Make sure processes.txt is in this folder.");
            return;
        }

        System.out.println("\nScheduling Algorithm: FCFS");
        List<Segment> gantt = scheduleFCFS(processes);
        printGantt(gantt);
        printMetrics(processes, gantt);
    }

    public static void runSJF() {
        String filename = "processes.txt";
        List<Process> processes = readProcesses(filename);

        if (processes.isEmpty()) {
            System.out.println("No processes loaded. Make sure processes.txt is in this folder.");
            return;
        }

        System.out.println("\nScheduling Algorithm: SJF (Non-preemptive)");
        List<Segment> gantt = scheduleSJF(processes);
        printGantt(gantt);
        printMetrics(processes, gantt);
    }

    public static void runRR(Scanner sc) {
        String filename = "processes.txt";
        List<Process> processes = readProcesses(filename);

        if (processes.isEmpty()) {
            System.out.println("No processes loaded. Make sure processes.txt is in this folder.");
            return;
        }

        int quantum = readInt(sc, "\nEnter time quantum for Round Robin: ");
        runRRWithQuantum(processes, quantum);
    }

    private static void runRRWithQuantum(List<Process> processes, int quantum) {
        if (quantum <= 0) {
            System.out.println("Quantum must be positive.");
            return;
        }
        List<Process> copy = new ArrayList<>();
        for (Process p : processes) copy.add(new Process(p.pid, p.arrival, p.burst, p.priority));

        System.out.println("\nScheduling Algorithm: Round Robin (q=" + quantum + ")");
        List<Segment> gantt = scheduleRR(copy, quantum);
        printGantt(gantt);
        printMetrics(copy, gantt);
    }

    public static void runPriority() {
        String filename = "processes.txt";
        List<Process> processes = readProcesses(filename);

        if (processes.isEmpty()) {
            System.out.println("No processes loaded. Make sure processes.txt is in this folder.");
            return;
        }

        System.out.println("\nScheduling Algorithm: Priority (Non-preemptive, lower number = higher priority)");
        List<Segment> gantt = schedulePriority(processes);
        printGantt(gantt);
        printMetrics(processes, gantt);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("OS Project 1 - Menu");
            System.out.println("1) Run FCFS Scheduling");
            System.out.println("2) Run SJF Scheduling");
            System.out.println("3) Run Round Robin Scheduling");
            System.out.println("4) Run Priority Scheduling");
            System.out.println("5) Run LRU Page Replacement (Bonus)");
            System.out.println("6) Run ALL CPU schedulers (FCFS, SJF, Priority, RR)");
            System.out.println("7) Exit");

            int choice = readInt(sc, "Choose: ");

            switch (choice) {
                case 1 -> runFCFS();
                case 2 -> runSJF();
                case 3 -> runRR(sc);
                case 4 -> runPriority();
                case 5 -> simulateLRU(sc);
                case 6 -> {
                    int quantum = readInt(sc, "\nEnter time quantum to use for Round Robin in the run-all: ");
                    runFCFS();
                    runSJF();
                    runPriority();
                    runRRWithQuantum(readProcesses("processes.txt"), quantum);
                }
                case 7 -> System.out.println("Goodbye!");
                default -> System.out.println("Please choose 1-7.");
            }
        }
    }
}
