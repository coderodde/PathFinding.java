package io.github.coderodde.pathfinding.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * This class implements a binary heap providing the most important priority 
 * queue operations running in {@code O(log N)} time.
 * 
 * @param <T> the type of the actual datum being stored in the heap.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Sep 20, 2026)
 * @since 1.0.0 (Sep 20, 2026)
 */
public final class DoublePriorityBinaryHeap<T> {

    /**
     * This class implements the binary heap entry.
     * 
     * @param <T> the type of the actual datum being stored in the heap.
     */
    private static final class BinaryHeapEntry<T> {

        /**
         * The element being described.
         */
        T datum;

        /**
         * The integer priority of {@code datum}.
         */
        double priority;

        /**
         * The index at which this entry is located in the {@code table} array.
         */
        int index;

        BinaryHeapEntry(T datum,
                        double priority, 
                        int index) {
            
            this.datum    = datum;
            this.priority = priority;
            this.index    = index;
        }
    }

    /**
     * Maps the heap element to a heap entry describing its metadata.
     */
    private final Map<T, BinaryHeapEntry<T>> map = new HashMap<>();

    /**
     * The actual heap array.
     */
    private final List<BinaryHeapEntry<T>> table = new ArrayList<>();

    /**
     * Inserts a new datum into this heap only if it is not yet present.
     * 
     * @param datum    the datum to store in this heap.
     * @param priority the priority of the new datum.
     */
    public void insert(T datum, double priority) {
        if (map.containsKey(datum)) {
            throw new IllegalArgumentException("Duplicate datum: " + datum);
        }
        
         BinaryHeapEntry<T> entry = 
            new BinaryHeapEntry<>(datum, 
                                  priority,
                                  table.size());
        
        table.addLast(entry);
        map.put(datum, entry);
        siftUp(entry.index);
    }
    
    /**
     * Returns {@code true} only if this heap contains {@code datum}.
     * 
     * @param datum the query datum.
     * 
     * @return a Boolean flag.
     */
    public boolean containsDatum(T datum) {
        return map.containsKey(datum);
    }
    
    /**
     * Returns but does not remove the highest priority datum.
     * 
     * @return the topmost datum.
     */
    public T top() {
        if (map.isEmpty()) {
            throw new NoSuchElementException("Peeking to empty heap.");
        }
        
        return table.getFirst().datum;
    }
    
    /**
     * If this heap is not empty, removes and returns the datum with highest 
     * priority (lowest priority key; this heap is a min-heap). If this heap is 
     * empty, throws an  instance of {@link NoSuchElementException}.
     * 
     * @return the datum with highest priority. 
     */
    public T extractTop() {
        if (table.isEmpty()) {
            throw new NoSuchElementException("Extracting from empty heap.");
        }
        
        BinaryHeapEntry<T> topEntry  = table.getFirst();
        BinaryHeapEntry<T> lastEntry = table.removeLast();
        
        map.remove(topEntry.datum);
        
        if (!table.isEmpty()) {
            table.set(0, lastEntry);
            lastEntry.index = 0;
            siftDown(0);
        }
        
        return topEntry.datum;
    }
    
    /**
     * If this heap contains {@code datum}, updates its priority.
     * 
     * @param datum    the target datum.
     * @param priority the new priority.
     */
    public void changePriority(T datum, double priority) {
        BinaryHeapEntry<T> entry = map.get(datum);
        
        if (entry == null) {
            throw new NoSuchElementException(
                String.format(
                    "The input datum %s is not stored in this heap.", 
                    datum));
        }
        
        double oldPriority = entry.priority;
        entry.priority  = priority;
        
        if (priority < oldPriority) {
            siftUp(entry.index);
        } else if (priority > oldPriority) {
            siftDown(entry.index);
        }
    }
    
    public void clear() {
        map.clear();
        table.clear();
    }
    
    /**
     * Returns the size of this heap.
     * 
     * @return the number of datums stored in this heap.
     */
    public int size() {
        return map.size();
    }
    
    /**
     * Returns {@code true} only if this heap is empty.
     * 
     * @return {@code true} only if this heap is empty.
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Sifts up the heap the entry with index {@code index}.
     * 
     * @param index the index of the target heap entry.
     */
    private void siftUp(int index) {
        if (index <= 0) {
            return;
        }
        
        BinaryHeapEntry<T> targetEntry = table.get(index);
        double targetEntryPriority = targetEntry.priority;
        
        int parentEntryIndex = getParentIndex(index);
        
        while (true) {
            BinaryHeapEntry<T> parentEntry = table.get(parentEntryIndex);
            double parentEntryPriority = parentEntry.priority;
        
            if (targetEntryPriority < parentEntryPriority) {
                table.set(index, parentEntry);
                parentEntry.index = index;
                index = parentEntryIndex;
                parentEntryIndex = getParentIndex(index);
            } else {
                break;
            }
            
            if (index == 0) {
                break;
            }
        }
        
        table.set(index, targetEntry); 
        targetEntry.index = index;
    }

    /**
     * Sifts down the heap the entry with index {@code index}.
     * 
     * @param index the index of the target heap entry.
     */
    private void siftDown(int index) {
        BinaryHeapEntry<T> targetEntry = table.get(index);
        double targetEntryPriority = targetEntry.priority;

        while (true) {
             int leftChildEntryIndex = getLeftChildIndex(index);

            if (leftChildEntryIndex >= table.size()) {
                break;
            }

             int rightChildEntryIndex = getRightChildIndex(index);

            int minChildEntryIndex = leftChildEntryIndex;

            if (rightChildEntryIndex < table.size()
                    && table.get(rightChildEntryIndex)
                            .priority < table.get(leftChildEntryIndex)
                                              .priority) {
                
                minChildEntryIndex = rightChildEntryIndex;
            }

            BinaryHeapEntry<T> minChildEntry
                    = table.get(minChildEntryIndex);

            if (minChildEntry.priority < targetEntryPriority) {
                table.set(index, minChildEntry);
                minChildEntry.index = index;
                index = minChildEntryIndex;
            } else {
                break;
            }
        }

        table.set(index, targetEntry);
        targetEntry.index = index;
    }

    /**
     * Produces the parent index of {@code index} in the table array.
     *
     * @param index the index of which to compute the parent index.
     *
     * @return the parent index of {@code index}.
     */
    private static int getParentIndex( int index) {
        return (index - 1) >>> 1;
    }

    /**
     * Produces the left child index of {@code index} in the table array.
     *
     * @param index the index of which to compute the left child index.
     *
     * @return the left child index of {@code index}.
     */
    private static int getLeftChildIndex( int index) {
        return (index << 1) + 1;
    }

    /**
     * Produces the right child index of {@code index} in the table array.
     *
     * @param index the index of which to compute the right child index.
     *
     * @return the right child index of {@code index}.
     */
    private static int getRightChildIndex( int index) {
        return getLeftChildIndex(index) + 1;
    }
}