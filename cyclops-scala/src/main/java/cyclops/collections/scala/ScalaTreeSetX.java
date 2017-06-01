package cyclops.collections.scala;

import static com.aol.cyclops.scala.collections.Converters.ordering;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.aol.cyclops.scala.collections.Converters;
import com.aol.cyclops.scala.collections.HasScalaCollection;
import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.data.collections.extensions.lazy.immutable.LazyPOrderedSetX;
import com.aol.cyclops2.types.Unwrapable;
import cyclops.collections.immutable.OrderedSetX;
import cyclops.function.Reducer;
import cyclops.stream.ReactiveSeq;
import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.POrderedSet;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import scala.collection.GenTraversableOnce;
import scala.collection.JavaConversions;
import scala.collection.generic.CanBuildFrom;
import scala.collection.immutable.TreeSet;
import scala.collection.immutable.TreeSet$;
import scala.collection.mutable.Builder;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScalaTreeSetX<T> extends AbstractSet<T> implements POrderedSet<T>, HasScalaCollection<T>, Unwrapable {

    @Override
    public <R> R unwrap() {
        return (R)set;
    }

    public LazyPOrderedSetX<T> plusLoop(int max, IntFunction<T> value) {
        TreeSet<T> toUse = set;
        for (int i = 0; i < max; i++) {
            toUse = toUse.$plus(value.apply(i));
        }
        return lazySet(toUse);

    }

    public LazyPOrderedSetX<T> plusLoop(Supplier<Optional<T>> supplier) {
        TreeSet<T> toUse = set;
        Optional<T> next = supplier.get();
        while (next.isPresent()) {
            toUse = toUse.$plus(next.get());
            next = supplier.get();
        }
        return lazySet(toUse);
    }
    /**
     * Create a LazyPOrderedSetX from a Stream
     * 
     * @param stream to construct a LazyQueueX from
     * @return LazyPOrderedSetX
     */
    public static <T extends Comparable<? super T>> LazyPOrderedSetX<T> fromStream(Stream<T> stream) {
        Reducer<POrderedSet<T>> reducer = ScalaTreeSetX.<T>toPOrderedSet(Comparator.naturalOrder());
        return new LazyPOrderedSetX<T>(null, ReactiveSeq.fromStream(stream),
                                  reducer);
    }

    /**
     * Create a LazyPOrderedSetX that contains the Integers between start and end
     * 
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range SetX
     */
    public static LazyPOrderedSetX<Integer> range(int start, int end) {
        return fromStream(ReactiveSeq.range(start, end));
    }

    /**
     * Create a LazyPOrderedSetX that contains the Longs between start and end
     * 
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range SetX
     */
    public static LazyPOrderedSetX<Long> rangeLong(long start, long end) {
        return fromStream(ReactiveSeq.rangeLong(start, end));
    }

    /**
     * Unfold a function into a SetX
     * 
     * <pre>
     * {@code 
     *  LazyPOrderedSetX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.empty());
     * 
     * //(1,2,3,4,5)
     * 
     * }</pre>
     * 
     * @param seed Initial value 
     * @param unfolder Iteratively applied function, terminated by an empty Optional
     * @return SetX generated by unfolder function
     */
    public static <U, T extends Comparable<? super T>> LazyPOrderedSetX<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return fromStream(ReactiveSeq.unfold(seed, unfolder));
    }

    /**
     * Generate a LazyPOrderedSetX from the provided Supplier up to the provided limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param s Supplier to generate SetX elements
     * @return SetX generated from the provided Supplier
     */
    public static <T extends Comparable<? super T>> LazyPOrderedSetX<T> generate(long limit, Supplier<T> s) {

        return fromStream(ReactiveSeq.generate(s)
                                     .limit(limit));
    }

    /**
     * Create a LazyPOrderedSetX by iterative application of a function to an initial element up to the supplied limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return SetX generated by iterative application
     */
    public static <T extends Comparable<? super T>> LazyPOrderedSetX<T> iterate(long limit, final T seed, final UnaryOperator<T> f) {
        return fromStream(ReactiveSeq.iterate(seed, f)
                                     .limit(limit));
    }

    /**
     * <pre>
     * {@code 
     * POrderedSet<Integer> q = JSPOrderedSet.<Integer>toPOrderedSet()
                                     .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer for POrderedSet
     */
    public static <T extends Comparable<? super T>>  Reducer<POrderedSet<T>> toPOrderedSet() {
        return Reducer.<POrderedSet<T>> of(ScalaTreeSetX.emptyPOrderedSet(), (final POrderedSet<T> a) -> b -> a.plusAll(b),
                                      (final T x) -> ScalaTreeSetX.singleton(x));
    }
    
    public static <T>  Reducer<POrderedSet<T>> toPOrderedSet(Comparator<T> ordering) {
        return Reducer.<POrderedSet<T>> of(ScalaTreeSetX.emptyPOrderedSet(ordering),
                                           (final POrderedSet<T> a) -> b -> a.plusAll(b),
                                      (final T x) -> ScalaTreeSetX.singleton(ordering,x));
    }

    public static <T> ScalaTreeSetX<T> fromSet(TreeSet<T> set) {
        return new ScalaTreeSetX<>(
                                 set);
    }
    public static <T> LazyPOrderedSetX<T> lazySet(TreeSet<T> set){
        POrderedSet<T> ordered = fromSet(set);
        return fromPOrderedSet(ordered, (Reducer)toPOrderedSet());
    }

    private static <T> LazyPOrderedSetX<T> fromPOrderedSet(POrderedSet<T> ordered, Reducer<POrderedSet<T>> reducer) {
        return  new LazyPOrderedSetX<T>(ordered,null,reducer);
    }

    public static <T extends Comparable<? super T>> ScalaTreeSetX<T> emptyPOrderedSet() {
        return new ScalaTreeSetX<>(
                                 TreeSet$.MODULE$.empty(Converters.<T>ordering(Comparator.naturalOrder())));
    }
    
    public static <T> ScalaTreeSetX<T> emptyPOrderedSet(Comparator<T> ordering) {
        return new ScalaTreeSetX<>(
                                 TreeSet$.MODULE$.empty(ordering(ordering)));
    }

    public static <T extends Comparable<? super T>> LazyPOrderedSetX<T> empty() {
        
        return fromPOrderedSet(new ScalaTreeSetX<>(
                TreeSet$.MODULE$.empty(Converters.<T>ordering(Comparator.naturalOrder()))),
                                                toPOrderedSet());
    }
    public static <T> LazyPOrderedSetX<T> empty(Comparator<T> comp) {
        
        return fromPOrderedSet(new ScalaTreeSetX<>(
                TreeSet$.MODULE$.empty(Converters.<T>ordering(comp))),
                                                toPOrderedSet(comp));
    }

    public static <T extends Comparable<? super T>> LazyPOrderedSetX<T> singleton(T t) {
        return of(t);
    }
    public static <T> LazyPOrderedSetX<T> singleton(Comparator<T> comp,T t) {
        return of(comp,t);
    }
    public static <T> LazyPOrderedSetX<T> of(Comparator<T> comp,T... t) {

        Builder<T, TreeSet<T>> lb = TreeSet$.MODULE$.newBuilder(Converters.<T>ordering(comp));
       for (T next : t)
           lb.$plus$eq(next);
       TreeSet<T> vec = lb.result();
       return fromPOrderedSet(new ScalaTreeSetX<>(
                                                       vec),
                                     toPOrderedSet(comp));
   }

    public static <T extends Comparable<? super T>> LazyPOrderedSetX<T> of(T... t) {

        return of(Comparator.naturalOrder(),t);
    }
    

    public static <T> LazyPOrderedSetX<T> POrderedSet(TreeSet<T> q) {
        return fromPOrderedSet(new ScalaTreeSetX<T>(
                                                         q),
                                      toPOrderedSet(q.ordering()));
    }

    @SafeVarargs
    public static <T extends Comparable<? super T>> LazyPOrderedSetX<T> POrderedSet(T... elements) {
        return fromPOrderedSet(of(elements), toPOrderedSet());
    }

    @Wither
    private final TreeSet<T> set;

    @Override
    public ScalaTreeSetX<T> plus(T e) {
       
        return withSet(set.$plus(e));
    }

    @Override
    public ScalaTreeSetX<T> plusAll(Collection<? extends T> l) {
        
        TreeSet<T> use =HasScalaCollection.visit(l, scala->set, java->{
            TreeSet<T> vec = set;
            for (T next : l) {
                  vec = vec.$plus(next);
            }
            return vec;
        });
        

        return withSet(use);
       
    }

   

    
  

    @Override
    public POrderedSet<T> minus(Object e) {
        return withSet(set.$minus((T)e));
        
    }

    @Override
    public POrderedSet<T> minusAll(Collection<?> s) {
        
        GenTraversableOnce<T> col = HasScalaCollection.<T>traversable((Collection)s);
        return withSet((TreeSet)set.$minus$minus(col));        
    }

  
   

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public Iterator<T> iterator() {
        return JavaConversions.asJavaIterator(set.iterator());
    }

    @Override
    public T get(int index) {
        return set.toIndexedSeq().toVector().apply(index);
    }

    @Override
    public int indexOf(Object o) {
        return set.toIndexedSeq().toVector().indexOf(o);
    }

    @Override
    public GenTraversableOnce<T> traversable() {
        return set;
    }

    @Override
    public CanBuildFrom canBuildFrom() {
        return TreeSet.newCanBuildFrom(set.ordering());
    }


    public static <T> OrderedSetX<T> copyFromCollection(CollectionX<T> vec, Comparator<T> comp) {

            return ScalaTreeSetX.empty(comp)
                    .plusAll(vec);

    }
}
