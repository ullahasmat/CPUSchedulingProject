# OS Project 1 – CPU Scheduling Simulator

Console program that reads a process list from `processes.txt`, runs several CPU schedulers, and prints Gantt charts plus waiting/turnaround metrics. Includes an LRU page-replacement bonus.

## Features
- FCFS, SJF (non-preemptive), Priority (non-preemptive, lower number = higher priority), Round Robin (preemptive, user quantum).
- Text Gantt chart showing execution order and timestamps.
- Per-process WT/TAT, averages, and CPU utilization.
- LRU page replacement simulation (HIT/FAULT per reference, total faults).
- Menu-driven; only dependency is a Java runtime.

## Input Format
`processes.txt` (with header row):
```
PID Arrival_Time Burst_Time Priority
1   0            5           2
2   2            3           1
3   4            2           3
4   6            4           2
```

## Usage
```bash
javac Main.java
java Main
```
- Choose a scheduler from the menu.
- For Round Robin or “run all,” enter the time quantum when prompted.
- Edit `processes.txt` to try other workloads (keep the header).

## Files
- `Main.java` — source code.
- `processes.txt` — sample input.
- `report.txt` — project writeup (fill in your name/course).
- `RoyaHosseini-Operating Systems Project 1-Spring 26.pdf` — assignment handout (optional to keep in repo).

## Notes
- Priority scheduling is non-preemptive; SRTF/preemptive priority can be added if needed.
- Context-switch cost is not modeled; CPU utilization is burst time over total elapsed.
- IDLE segments are inserted if the CPU would be idle between arrivals.

