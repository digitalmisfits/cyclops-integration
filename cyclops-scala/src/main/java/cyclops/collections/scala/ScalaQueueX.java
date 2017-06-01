package cyclops.collections.scala;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.aol.cyclops.scala.collections.HasScalaCollection;
import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.data.collections.extensions.lazy.immutable.LazyPQueueX;
import com.aol.cyclops2.types.Unwrapable;
import cyclops.collections.immutable.OrderedSetX;
import cyclops.collections.immutable.PersistentQueueX;
import cyclops.collections.mutable.QueueX;
import cyclops.function.Reducer;
import cyclops.stream.ReactiveSeq;
import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.PQueue;



import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import scala.collection.GenTraversableOnce;
import scala.collection.JavaConversions;
import scala.collection.generic.CanBuildFrom;
import scala.collection.immutable.Queue;
import scala.collection.immutable.Queue$;
import scala.collection.mutable.Builder;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScalaQueueX<T> extends AbstractQueue<T> implements PQueue<T>, HasScalaCollection<T>,Unwrapable {
    public LazyPQueueX<T> plusLoop(int max, IntFunction<T> value) {
        Queue<T> toUse = this.queue;
        final CanBuildFrom<Queue<?>, T, Queue<T>> builder = Queue.<T> canBuildFrom();
        final CanBuildFrom<Queue<T>, T, Queue<T>> builder2 = (CanBuildFrom) builder;
       
        for (int i = 0; i < max; i++) {
            toUse = toUse.$colon$plus(value.apply(i), builder2);
        }
        return lazyQueue(toUse);

    }

    @Override
    public <R> R unwrap() {
        return (R)queue;
    }

    public LazyPQueueX<T> plusLoop(Supplier<Optional<T>> supplier) {
        Queue<T> toUse = this.queue;
        final CanBuildFrom<Queue<?>, T, Queue<T>> builder = Queue.<T> canBuildFrom();
        final CanBuildFrom<Queue<T>, T, Queue<T>> builder2 = (CanBuildFrom) builder;
        Optional<T> next = supplier.get();
        while (next.isPresent()) {
            toUse = toUse.$colon$plus(next.get(), builder2);
            next = supplier.get();
        }
        return lazyQueue(toUse);
    }
    /**
     * Create a LazyPQueueX from a Stream
     * 
     * @param stream to construct a LazyQueueX from
     * @return LazyPQueueX
     */
    public static <T> LazyPQueueX<T> fromStream(Stream<T> stream) {
        return new LazyPQueueX<T>(null,ReactiveSeq.fromStream(stream), toPQueue());
    }

    /**
     * Create a LazyPQueueX that contains the Integers between start and end
     * 
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range QueueX
     */
    public static LazyPQueueX<Integer> range(int start, int end) {
        return fromStream(ReactiveSeq.range(start, end));
    }

    /**
     * Create a LazyPQueueX that contains the Longs between start and end
     * 
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range QueueX
     */
    public static LazyPQueueX<Long> rangeLong(long start, long end) {
        return fromStream(ReactiveSeq.rangeLong(start, end));
    }

    /**
     * Unfold a function into a QueueX
     * 
     * <pre>
     * {@code 
     *  LazyPQueueX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.empty());
     * 
     * //(1,2,3,4,5)
     * 
     * }</pre>
     * 
     * @param seed Initial value 
     * @param unfolder Iteratively applied function, terminated by an empty Optional
     * @return QueueX generated by unfolder function
     */
    public static <U, T> LazyPQueueX<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return fromStream(ReactiveSeq.unfold(seed, unfolder));
    }

    /**
     * Generate a LazyPQueueX from the provided Supplier up to the provided limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param s Supplier to generate QueueX elements
     * @return QueueX generated from the provided Supplier
     */
    public static <T> LazyPQueueX<T> generate(long limit, Supplier<T> s) {

        return fromStream(ReactiveSeq.generate(s)
                                     .limit(limit));
    }

    /**
     * Create a LazyPQueueX by iterative application of a function to an initial element up to the supplied limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return QueueX generated by iterative application
     */
    public static <T> LazyPQueueX<T> iterate(long limit, final T seed, final UnaryOperator<T> f) {
        return fromStream(ReactiveSeq.iterate(seed, f)
                                     .limit(limit));
    }

    /**
     * <pre>
     * {@code 
     * PQueue<Integer> q = JSPQueue.<Integer>toPQueue()
                                     .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer for PQueue
     */
    public static <T> Reducer<PQueue<T>> toPQueue() {
        return Reducer.<PQueue<T>> of(ScalaQueueX.emptyPQueue(), (final PQueue<T> a) -> b -> a.plusAll(b),
                                      (final T x) -> ScalaQueueX.singleton(x));
    }

    public static <T> ScalaQueueX<T> fromQueue(Queue<T> queue) {
        return new ScalaQueueX<>(
                                 queue);
    }
    public static <T> LazyPQueueX<T> lazyQueue(Queue<T> queue){
        return fromPQueue(fromQueue(queue), toPQueue());
    }

    private static <T> LazyPQueueX<T> fromPQueue(PQueue<T> ts, Reducer<PQueue<T>> pQueueReducer) {
        return new LazyPQueueX<T>(ts,null,pQueueReducer);
    }

    public static <T> ScalaQueueX<T> emptyPQueue() {

        return new ScalaQueueX<>(
                                 Queue$.MODULE$.empty());
    }

    public static <T> LazyPQueueX<T> empty() {
        return fromPQueue(new ScalaQueueX<>(
                                                        Queue$.MODULE$.empty()),
                                      toPQueue());
    }

    public static <T> LazyPQueueX<T> singleton(T t) {
        return of(t);
    }

    public static <T> LazyPQueueX<T> of(T... t) {

        Builder<T, Queue<T>> lb = Queue$.MODULE$.newBuilder();
        for (T next : t)
            lb.$plus$eq(next);
        Queue<T> vec = lb.result();
        return fromPQueue(new ScalaQueueX<>(
                                                        vec),
                                      toPQueue());
    }

    public static <T> LazyPQueueX<T> PQueue(Queue<T> q) {
        return fromPQueue(new ScalaQueueX<T>(
                                                         q),
                                      toPQueue());
    }

    @SafeVarargs
    public static <T> LazyPQueueX<T> PQueue(T... elements) {
        return fromPQueue(of(elements), toPQueue());
    }

    @Wither
    private final Queue<T> queue;

    @Override
    public ScalaQueueX<T> plus(T e) {
        final CanBuildFrom<Queue<?>, T, Queue<T>> builder = Queue.<T> canBuildFrom();
        final CanBuildFrom<Queue<T>, T, Queue<T>> builder2 = (CanBuildFrom) builder;
        return withQueue(queue.$colon$plus(e, builder2));
    }

    @Override
    public ScalaQueueX<T> plusAll(Collection<? extends T> l) {
        final CanBuildFrom<Queue<?>, T, Queue<T>> builder = Queue.<T> canBuildFrom();
        final CanBuildFrom<Queue<T>, T, Queue<T>> builder2 = (CanBuildFrom) builder;
        Queue<T> vec = queue;
        if(l instanceof ScalaQueueX){
           
            Queue<T> toUse = ((ScalaQueueX)l).queue;
           
            vec = (Queue<T>)vec.$plus$plus(toUse, (CanBuildFrom) builder);
        }
        else {
  
           
            for (T next : l) {
                vec = vec.$colon$plus(next, builder2);
            }
        }
        return withQueue(vec);
    }

    
   

    @Override
    public PQueue<T> minus(Object e) {
        return fromPQueue(this, toPQueue())
                          .filter(i -> !Objects.equals(i, e));
    }

    @Override
    public PQueue<T> minusAll(Collection<?> queue) {
        return (LazyPQueueX<T>)fromPQueue(this, toPQueue())
                          .removeAllI((Iterable<T>) queue);
    }

    public ScalaQueueX<T> tail() {
        return withQueue((Queue<T>) queue.tail());
    }

    public T head() {
        return queue.head();
    }

    

   

   
    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public T peek() {
        return queue.head();
    }

    @Override
    public org.pcollections.PQueue<T> minus() {
        return withQueue(queue.dequeue()._2);
    }

    @Override
    public boolean offer(T o) {
 
        return false;
    }

    @Override
    public T poll() {
        return queue.head();
    }

    @Override
    public Iterator<T> iterator() {
        return JavaConversions.asJavaIterator(queue.iterator());
    }

    @Override
    public GenTraversableOnce<T> traversable() {
        return queue;
    }

    @Override
    public CanBuildFrom canBuildFrom() {
        return Queue.canBuildFrom();
    }


    public static <T> PersistentQueueX<T> copyFromCollection(CollectionX<T> vec) {

        return ScalaQueueX.<T>empty()
                .plusAll(vec);

    }
}
