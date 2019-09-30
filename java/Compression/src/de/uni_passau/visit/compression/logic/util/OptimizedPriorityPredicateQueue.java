package de.uni_passau.visit.compression.logic.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.SortedSet;

import org.apache.commons.math3.util.Pair;

public class OptimizedPriorityPredicateQueue<E> extends OptimizedPriorityQueue<E> {

	private static final long serialVersionUID = 2938040587438510564L;
	
	private final QueueEntryPredicate<E> predicateChecker;

	/**
	 * Creates a {@code PriorityQueue} with the default initial capacity (11) that
	 * orders its elements according to their {@linkplain Comparable natural
	 * ordering}.
	 */
	public OptimizedPriorityPredicateQueue(QueueEntryPredicate<E> predicateChecker) {
		super();
		this.predicateChecker = predicateChecker;
	}

	/**
	 * Creates a {@code PriorityQueue} with the specified initial capacity that
	 * orders its elements according to their {@linkplain Comparable natural
	 * ordering}.
	 *
	 * @param initialCapacity
	 *            the initial capacity for this priority queue
	 * @throws IllegalArgumentException
	 *             if {@code initialCapacity} is less than 1
	 */
	public OptimizedPriorityPredicateQueue(QueueEntryPredicate<E> predicateChecker, int initialCapacity) {
		super(initialCapacity);
		this.predicateChecker = predicateChecker;
	}

	/**
	 * Creates a {@code PriorityQueue} with the specified initial capacity that
	 * orders its elements according to the specified comparator.
	 *
	 * @param initialCapacity
	 *            the initial capacity for this priority queue
	 * @param comparator
	 *            the comparator that will be used to order this priority queue. If
	 *            {@code null}, the {@linkplain Comparable natural ordering} of the
	 *            elements will be used.
	 * @throws IllegalArgumentException
	 *             if {@code initialCapacity} is less than 1
	 */
	public OptimizedPriorityPredicateQueue(QueueEntryPredicate<E> predicateChecker, int initialCapacity,
			Comparator<? super E> comparator) {
		super(initialCapacity, comparator);
		this.predicateChecker = predicateChecker;
	}

	/**
	 * Creates a {@code PriorityQueue} containing the elements in the specified
	 * collection. If the specified collection is an instance of a {@link SortedSet}
	 * or is another {@code PriorityQueue}, this priority queue will be ordered
	 * according to the same ordering. Otherwise, this priority queue will be
	 * ordered according to the {@linkplain Comparable natural ordering} of its
	 * elements.
	 *
	 * @param c
	 *            the collection whose elements are to be placed into this priority
	 *            queue
	 * @throws ClassCastException
	 *             if elements of the specified collection cannot be compared to one
	 *             another according to the priority queue's ordering
	 * @throws NullPointerException
	 *             if the specified collection or any of its elements are null
	 */
	public OptimizedPriorityPredicateQueue(QueueEntryPredicate<E> predicateChecker, Collection<? extends E> c) {
		super(c);
		this.predicateChecker = predicateChecker;
	}

	/**
	 * Creates a {@code PriorityQueue} containing the elements in the specified
	 * priority queue. This priority queue will be ordered according to the same
	 * ordering as the given priority queue.
	 *
	 * @param c
	 *            the priority queue whose elements are to be placed into this
	 *            priority queue
	 * @throws ClassCastException
	 *             if elements of {@code c} cannot be compared to one another
	 *             according to {@code c}'s ordering
	 * @throws NullPointerException
	 *             if the specified priority queue or any of its elements are null
	 */
	public OptimizedPriorityPredicateQueue(QueueEntryPredicate<E> predicateChecker, PriorityQueue<? extends E> c) {
		super(c);
		this.predicateChecker = predicateChecker;
	}

	/**
	 * Creates a {@code PriorityQueue} containing the elements in the specified
	 * sorted set. This priority queue will be ordered according to the same
	 * ordering as the given sorted set.
	 *
	 * @param c
	 *            the sorted set whose elements are to be placed into this priority
	 *            queue
	 * @throws ClassCastException
	 *             if elements of the specified sorted set cannot be compared to one
	 *             another according to the sorted set's ordering
	 * @throws NullPointerException
	 *             if the specified sorted set or any of its elements are null
	 */
	public OptimizedPriorityPredicateQueue(QueueEntryPredicate<E> predicateChecker, SortedSet<? extends E> c) {
		super(c);
		this.predicateChecker = predicateChecker;
	}

	public E pollWithPredicate() {
		int nonPredicateCandidates = 0;
		
		if (size == 0)
			return null;
		int s = --size;
		modCount++;

		int index = 0;
		E result = (E) queue[0];
		if (!predicateChecker.hasPredicate(result)) {
			PriorityQueue<Pair<Integer, E>> tempHeap = new PriorityQueue<>(new Comparator<Pair<Integer, E>>() {
				@Override
				public int compare(Pair<Integer, E> o1, Pair<Integer, E> o2) {
					return ((Comparable<? super E>) o1.getSecond()).compareTo(o2.getSecond());
				}
			});

			Pair<Integer, E> candidate;

			do {
				++nonPredicateCandidates;
				
				int child = (index << 1) + 1;
				if (child < size)
					tempHeap.offer(new Pair<Integer, E>(child, (E) queue[child]));
				if (child + 1 < size)
					tempHeap.offer(new Pair<Integer, E>(child + 1, (E) queue[child + 1]));

				if (!tempHeap.isEmpty()) {
					candidate = tempHeap.poll();
//					System.out.println(index + " -> " + candidate.getFirst() + "; " + predicateChecker.hasPredicate(candidate.getSecond()) + "; " + ((QuadricCollapseInfo) candidate.getSecond()).getCost());
					index = candidate.getFirst();
				} else {
					return null;
				}
			} while (!predicateChecker.hasPredicate((E) candidate.getSecond()));
			
			

			result = (E) candidate.getSecond();
			index = candidate.getFirst();
			

		} 
		
		indices.remove(result);
		E last = (E) queue[s];
		queue[s] = null;
		if (s > index) {
			siftDown(index, last);
		}
		
		lastNonPredicateCandidates = nonPredicateCandidates;
		
		return result;
	}
	
	private int lastNonPredicateCandidates = 0;
	
	public int getLastNonPredicateCandidates() {
		return lastNonPredicateCandidates;
	}

}
