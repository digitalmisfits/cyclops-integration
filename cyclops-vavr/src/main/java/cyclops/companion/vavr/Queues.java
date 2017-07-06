package cyclops.companion.vavr;

import cyclops.monads.VavrWitness.tryType;
import io.vavr.Lazy;
import io.vavr.collection.*;
import io.vavr.concurrent.Future;
import io.vavr.control.*;
import com.aol.cyclops.vavr.hkt.*;
import cyclops.companion.CompletableFutures;
import cyclops.companion.Optionals;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.control.Reader;
import cyclops.control.Xor;
import cyclops.conversion.vavr.FromCyclopsReact;
import cyclops.monads.*;
import cyclops.monads.VavrWitness.*;
import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.anyM.AnyMSeq;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.function.Monoid;
import cyclops.monads.Witness.*;
import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.*;
import com.aol.cyclops.vavr.hkt.ListKind;
import cyclops.monads.VavrWitness;
import cyclops.monads.VavrWitness.queue;
import com.aol.cyclops.vavr.hkt.QueueKind;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;
import cyclops.monads.XorM;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import io.vavr.collection.List;
import io.vavr.collection.Queue;
import lombok.experimental.UtilityClass;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static com.aol.cyclops.vavr.hkt.QueueKind.widen;


public class Queues {

    public static  <W1,T> Coproduct<W1,queue,T> coproduct(Queue<T> type, InstanceDefinitions<W1> def1){
        return Coproduct.of(Xor.primary(widen(type)),def1, Instances.definitions());
    }
    public static  <W1,T> Coproduct<W1,queue,T> coproduct(InstanceDefinitions<W1> def1,T... values){
        return  coproduct(Queue.of(values),def1);
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,queue,T> xorM(Queue<T> type){
        return XorM.right(anyM(type));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,queue,T> xorM(T... values){
        return xorM(Queue.of(values));
    }

    public static <T> AnyMSeq<queue,T> anyM(Queue<T> option) {
        return AnyM.ofSeq(option, queue.INSTANCE);
    }
    /**
     * Perform a For Comprehension over a Queue, accepting 3 generating functions.
     * This results in a four level nested internal iteration over the provided Publishers.
     *
     *  <pre>
     * {@code
     *
     *   import static cyclops.Queues.forEach4;
     *
    forEach4(IntQueue.range(1,10).boxed(),
    a-> Queue.iterate(a,i->i+1).limit(10),
    (a,b) -> Queue.<Integer>of(a+b),
    (a,b,c) -> Queue.<Integer>just(a+b+c),
    Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Queue
     * @param value2 Nested Queue
     * @param value3 Nested Queue
     * @param value4 Nested Queue
     * @param yieldingFunction  Generates a result per combination
     * @return Queue with an element per combination of nested publishers generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Queue<R> forEach4(Queue<? extends T1> value1,
                                                               Function<? super T1, ? extends Queue<R1>> value2,
                                                               BiFunction<? super T1, ? super R1, ? extends Queue<R2>> value3,
                                                               Fn3<? super T1, ? super R1, ? super R2, ? extends Queue<R3>> value4,
                                                               Fn4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Queue<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Queue<R2> b = value3.apply(in,ina);
                return b.flatMap(inb -> {
                    Queue<R3> c = value4.apply(in,ina,inb);
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });

    }

    /**
     * Perform a For Comprehension over a Queue, accepting 3 generating function.
     * This results in a four level nested internal iteration over the provided Publishers.
     * <pre>
     * {@code
     *
     *  import static com.aol.cyclops2.reactor.Queuees.forEach4;
     *
     *  forEach4(IntQueue.range(1,10).boxed(),
    a-> Queue.iterate(a,i->i+1).limit(10),
    (a,b) -> Queue.<Integer>just(a+b),
    (a,b,c) -> Queue.<Integer>just(a+b+c),
    (a,b,c,d) -> a+b+c+d <100,
    Tuple::tuple);
     *
     * }
     * </pre>
     *
     * @param value1 top level Queue
     * @param value2 Nested Queue
     * @param value3 Nested Queue
     * @param value4 Nested Queue
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return Queue with an element per combination of nested publishers generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Queue<R> forEach4(Queue<? extends T1> value1,
                                                                 Function<? super T1, ? extends Queue<R1>> value2,
                                                                 BiFunction<? super T1, ? super R1, ? extends Queue<R2>> value3,
                                                                 Fn3<? super T1, ? super R1, ? super R2, ? extends Queue<R3>> value4,
                                                                 Fn4<? super T1, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                                 Fn4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Queue<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Queue<R2> b = value3.apply(in,ina);
                return b.flatMap(inb -> {
                    Queue<R3> c = value4.apply(in,ina,inb);
                    return c.filter(in2->filterFunction.apply(in,ina,inb,in2))
                            .map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
    }

    /**
     * Perform a For Comprehension over a Queue, accepting 2 generating function.
     * This results in a three level nested internal iteration over the provided Publishers.
     *
     * <pre>
     * {@code
     *
     * import static Queues.forEach3;
     *
     * forEach(IntQueue.range(1,10).boxed(),
    a-> Queue.iterate(a,i->i+1).limit(10),
    (a,b) -> Queue.<Integer>of(a+b),
    Tuple::tuple);
     *
     * }
     * </pre>
     *
     *
     * @param value1 top level Queue
     * @param value2 Nested Queue
     * @param value3 Nested Queue
     * @param yieldingFunction Generates a result per combination
     * @return Queue with an element per combination of nested publishers generated by the yielding function
     */
    public static <T1, T2, R1, R2, R> Queue<R> forEach3(Queue<? extends T1> value1,
                                                         Function<? super T1, ? extends Queue<R1>> value2,
                                                         BiFunction<? super T1, ? super R1, ? extends Queue<R2>> value3,
                                                         Fn3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Queue<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Queue<R2> b = value3.apply(in,ina);
                return b.map(in2 -> yieldingFunction.apply(in, ina, in2));
            });


        });


    }

    /**
     * Perform a For Comprehension over a Queue, accepting 2 generating function.
     * This results in a three level nested internal iteration over the provided Publishers.
     * <pre>
     * {@code
     *
     * import static Queues.forEach;
     *
     * forEach(IntQueue.range(1,10).boxed(),
    a-> Queue.iterate(a,i->i+1).limit(10),
    (a,b) -> Queue.<Integer>of(a+b),
    (a,b,c) ->a+b+c<10,
    Tuple::tuple)
    .toQueueX();
     * }
     * </pre>
     *
     * @param value1 top level Queue
     * @param value2 Nested publisher
     * @param value3 Nested publisher
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return
     */
    public static <T1, T2, R1, R2, R> Queue<R> forEach3(Queue<? extends T1> value1,
                                                         Function<? super T1, ? extends Queue<R1>> value2,
                                                         BiFunction<? super T1, ? super R1, ? extends Queue<R2>> value3,
                                                         Fn3<? super T1, ? super R1, ? super R2, Boolean> filterFunction,
                                                         Fn3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Queue<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Queue<R2> b = value3.apply(in,ina);
                return b.filter(in2->filterFunction.apply(in,ina,in2))
                        .map(in2 -> yieldingFunction.apply(in, ina, in2));
            });



        });
    }

    /**
     * Perform a For Comprehension over a Queue, accepting an additonal generating function.
     * This results in a two level nested internal iteration over the provided Publishers.
     *
     * <pre>
     * {@code
     *
     *  import static Queues.forEach2;
     *  forEach(IntQueue.range(1, 10).boxed(),
     *          i -> Queue.range(i, 10), Tuple::tuple)
    .forEach(System.out::println);

    //(1, 1)
    (1, 2)
    (1, 3)
    (1, 4)
    ...
     *
     * }</pre>
     *
     * @param value1 top level Queue
     * @param value2 Nested publisher
     * @param yieldingFunction Generates a result per combination
     * @return
     */
    public static <T, R1, R> Queue<R> forEach2(Queue<? extends T> value1,
                                                Function<? super T, Queue<R1>> value2,
                                                BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Queue<R1> a = value2.apply(in);
            return a.map(in2 -> yieldingFunction.apply(in,  in2));
        });

    }

    /**
     *
     * <pre>
     * {@code
     *
     *   import static Queues.forEach2;
     *
     *   forEach(IntQueue.range(1, 10).boxed(),
     *           i -> Queue.range(i, 10),
     *           (a,b) -> a>2 && b<10,
     *           Tuple::tuple)
    .forEach(System.out::println);

    //(3, 3)
    (3, 4)
    (3, 5)
    (3, 6)
    (3, 7)
    (3, 8)
    (3, 9)
    ...

     *
     * }</pre>
     *
     *
     * @param value1 top level Queue
     * @param value2 Nested publisher
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return
     */
    public static <T, R1, R> Queue<R> forEach2(Queue<? extends T> value1,
                                                Function<? super T, ? extends Queue<R1>> value2,
                                                BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                                BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Queue<R1> a = value2.apply(in);
            return a.filter(in2->filterFunction.apply(in,in2))
                    .map(in2 -> yieldingFunction.apply(in,  in2));
        });
    }
    public static <T> Active<queue,T> allTypeclasses(Queue<T> array){
        return Active.of(widen(array), Queues.Instances.definitions());
    }
    public static <T,W2,R> Nested<queue,W2,R> mapM(Queue<T> array, Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        Queue<Higher<W2, R>> e = array.map(fn);
        QueueKind<Higher<W2, R>> lk = widen(e);
        return Nested.of(lk, Queues.Instances.definitions(), defs);
    }
    /**
     * Companion class for creating Type Class instances for working with Queues
     *
     */
    @UtilityClass
    public static class Instances {
        public static InstanceDefinitions<queue> definitions() {
            return new InstanceDefinitions<queue>() {

                @Override
                public <T, R> Functor<queue> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<queue> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<queue> applicative() {
                    return Instances.zippingApplicative();
                }

                @Override
                public <T, R> Monad<queue> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<queue>> monadZero() {
                    return Maybe.just(Instances.monadZero());
                }

                @Override
                public <T> Maybe<MonadPlus<queue>> monadPlus() {
                    return Maybe.just(Instances.monadPlus());
                }

                @Override
                public <T> Maybe<MonadPlus<queue>> monadPlus(Monoid<Higher<queue, T>> m) {
                    return Maybe.just(Instances.monadPlus(m));
                }

                @Override
                public <C2, T> Maybe<Traverse<queue>> traverse() {
                    return Maybe.just(Instances.traverse());
                }

                @Override
                public <T> Maybe<Foldable<queue>> foldable() {
                    return Maybe.just(Instances.foldable());
                }

                @Override
                public <T> Maybe<Comonad<queue>> comonad() {
                    return Maybe.none();
                }

                @Override
                public <T> Maybe<Unfoldable<queue>> unfoldable() {
                    return Maybe.just(Instances.unfoldable());
                }
            };
        }

        /**
         *
         * Transform a list, mulitplying every element by 2
         *
         * <pre>
         * {@code
         *  QueueKind<Integer> list = Queues.functor().map(i->i*2, QueueKind.widen(Arrays.asQueue(1,2,3));
         *
         *  //[2,4,6]
         *
         *
         * }
         * </pre>
         *
         * An example fluent api working with Queues
         * <pre>
         * {@code
         *   QueueKind<Integer> list = Queues.unit()
        .unit("hello")
        .then(h->Queues.functor().map((String v) ->v.length(), h))
        .convert(QueueKind::narrowK);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for Queues
         */
        public static <T,R>Functor<queue> functor(){
            BiFunction<QueueKind<T>,Function<? super T, ? extends R>,QueueKind<R>> map = Instances::map;
            return General.functor(map);
        }
        /**
         * <pre>
         * {@code
         * QueueKind<String> list = Queues.unit()
        .unit("hello")
        .convert(QueueKind::narrowK);

        //Arrays.asQueue("hello"))
         *
         * }
         * </pre>
         *
         *
         * @return A factory for Queues
         */
        public static <T> Pure<queue> unit(){
            return General.<queue,T>unit(QueueKind::of);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.QueueKind.widen;
         * import static com.aol.cyclops.util.function.Lambda.l1;
         * import static java.util.Arrays.asQueue;
         *
        Queues.zippingApplicative()
        .ap(widen(asQueue(l1(this::multiplyByTwo))),widen(asQueue(1,2,3)));
         *
         * //[2,4,6]
         * }
         * </pre>
         *
         *
         * Example fluent API
         * <pre>
         * {@code
         * QueueKind<Function<Integer,Integer>> listFn =Queues.unit()
         *                                                  .unit(Lambda.l1((Integer i) ->i*2))
         *                                                  .convert(QueueKind::narrowK);

        QueueKind<Integer> list = Queues.unit()
        .unit("hello")
        .then(h->Queues.functor().map((String v) ->v.length(), h))
        .then(h->Queues.zippingApplicative().ap(listFn, h))
        .convert(QueueKind::narrowK);

        //Arrays.asQueue("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for Queues
         */
        public static <T,R> Applicative<queue> zippingApplicative(){
            BiFunction<QueueKind< Function<T, R>>,QueueKind<T>,QueueKind<R>> ap = Instances::ap;
            return General.applicative(functor(), unit(), ap);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.QueueKind.widen;
         * QueueKind<Integer> list  = Queues.monad()
        .flatMap(i->widen(QueueX.range(0,i)), widen(Arrays.asQueue(1,2,3)))
        .convert(QueueKind::narrowK);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    QueueKind<Integer> list = Queues.unit()
        .unit("hello")
        .then(h->Queues.monad().flatMap((String v) ->Queues.unit().unit(v.length()), h))
        .convert(QueueKind::narrowK);

        //Arrays.asQueue("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for Queues
         */
        public static <T,R> Monad<queue> monad(){

            BiFunction<Higher<queue,T>,Function<? super T, ? extends Higher<queue,R>>,Higher<queue,R>> flatMap = Instances::flatMap;
            return General.monad(zippingApplicative(), flatMap);
        }
        /**
         *
         * <pre>
         * {@code
         *  QueueKind<String> list = Queues.unit()
        .unit("hello")
        .then(h->Queues.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(QueueKind::narrowK);

        //Arrays.asQueue("hello"));
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<queue> monadZero(){

            return General.monadZero(monad(), widen(Queue.empty()));
        }
        /**
         * <pre>
         * {@code
         *  QueueKind<Integer> list = Queues.<Integer>monadPlus()
        .plus(QueueKind.widen(Arrays.asQueue()), QueueKind.widen(Arrays.asQueue(10)))
        .convert(QueueKind::narrowK);
        //Arrays.asQueue(10))
         *
         * }
         * </pre>
         * @return Type class for combining Queues by concatenation
         */
        public static <T> MonadPlus<queue> monadPlus(){
            Monoid<QueueKind<T>> m = Monoid.of(widen(Queue.empty()), Instances::concat);
            Monoid<Higher<queue,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        /**
         *
         * <pre>
         * {@code
         *  Monoid<QueueKind<Integer>> m = Monoid.of(QueueKind.widen(Arrays.asQueue()), (a,b)->a.isEmpty() ? b : a);
        QueueKind<Integer> list = Queues.<Integer>monadPlus(m)
        .plus(QueueKind.widen(Arrays.asQueue(5)), QueueKind.widen(Arrays.asQueue(10)))
        .convert(QueueKind::narrowK);
        //Arrays.asQueue(5))
         *
         * }
         * </pre>
         *
         * @param m Monoid to use for combining Queues
         * @return Type class for combining Queues
         */
        public static <T> MonadPlus<queue> monadPlus(Monoid<Higher<queue,T>> m){
            Monoid<Higher<queue,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        public static <T> MonadPlus<queue> monadPlusK(Monoid<QueueKind<T>> m){
            Monoid<Higher<queue,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }

        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<queue> traverse(){

            BiFunction<Applicative<C2>,QueueKind<Higher<C2, T>>,Higher<C2, QueueKind<T>>> sequenceFn = (ap, list) -> {

                Higher<C2,QueueKind<T>> identity = ap.unit(widen(Queue.empty()));

                BiFunction<Higher<C2,QueueKind<T>>,Higher<C2,T>,Higher<C2,QueueKind<T>>> combineToQueue =   (acc, next) -> ap.apBiFn(ap.unit((a, b) -> widen(QueueKind.narrow(a).append(b))),
                        acc,next);

                BinaryOperator<Higher<C2,QueueKind<T>>> combineQueues = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> widen(QueueKind.narrow(l1).appendAll(l2.narrow()))),a,b); ;

                return ReactiveSeq.fromIterable(QueueKind.narrow(list))
                        .reduce(identity,
                                combineToQueue,
                                combineQueues);


            };
            BiFunction<Applicative<C2>,Higher<queue,Higher<C2, T>>,Higher<C2, Higher<queue,T>>> sequenceNarrow  =
                    (a,b) -> QueueKind.widen2(sequenceFn.apply(a, QueueKind.narrowK(b)));
            return General.traverse(zippingApplicative(), sequenceNarrow);
        }

        /**
         *
         * <pre>
         * {@code
         * int sum  = Queues.foldable()
        .foldLeft(0, (a,b)->a+b, QueueKind.widen(Arrays.asQueue(1,2,3,4)));

        //10
         *
         * }
         * </pre>
         *
         *
         * @return Type class for folding / reduction operations
         */
        public static <T> Foldable<queue> foldable(){
            BiFunction<Monoid<T>,Higher<queue,T>,T> foldRightFn =  (m, l)-> ReactiveSeq.fromIterable(QueueKind.narrow(l)).foldRight(m);
            BiFunction<Monoid<T>,Higher<queue,T>,T> foldLeftFn = (m, l)-> ReactiveSeq.fromIterable(QueueKind.narrow(l)).reduce(m);
            return General.foldable(foldRightFn, foldLeftFn);
        }

        private static  <T> QueueKind<T> concat(QueueKind<T> l1, QueueKind<T> l2){

            return widen(l1.appendAll(QueueKind.narrow(l2)));

        }

        private static <T,R> QueueKind<R> ap(QueueKind<Function< T, R>> lt, QueueKind<T> list){
            return widen(FromCyclopsReact.fromStream(ReactiveSeq.fromIterable(lt.narrow()).zip(list.narrow(), (a, b)->a.apply(b))).toQueue());
        }
        private static <T,R> Higher<queue,R> flatMap(Higher<queue,T> lt, Function<? super T, ? extends  Higher<queue,R>> fn){
            return widen(QueueKind.narrow(lt).flatMap(fn.andThen(QueueKind::narrow)));
        }
        private static <T,R> QueueKind<R> map(QueueKind<T> lt, Function<? super T, ? extends R> fn){
            return widen(QueueKind.narrow(lt).map(in->fn.apply(in)));
        }
        public static Unfoldable<queue> unfoldable(){
            return new Unfoldable<queue>() {
                @Override
                public <R, T> Higher<queue, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn) {
                    return widen(ReactiveSeq.unfold(b,fn).collect(Queue.collector()));

                }
            };
        }
    }


    public static interface QueueNested{


        public static <T> Nested<queue,lazy,T> lazy(Queue<Lazy<T>> type){
            return Nested.of(widen(type.map(LazyKind::widen)),Instances.definitions(),Lazys.Instances.definitions());
        }
        public static <T> Nested<queue,tryType,T> queueTry(Queue<Try<T>> type){
            return Nested.of(widen(type.map(TryKind::widen)),Instances.definitions(),Trys.Instances.definitions());
        }
        public static <T> Nested<queue,VavrWitness.future,T> future(Queue<Future<T>> type){
            return Nested.of(widen(type.map(FutureKind::widen)),Instances.definitions(),Futures.Instances.definitions());
        }
        public static <T> Nested<queue,queue,T> queue(Queue<Queue<T>> nested){
            return Nested.of(widen(nested.map(QueueKind::widen)),Instances.definitions(),Queues.Instances.definitions());
        }
        public static <L, R> Nested<queue,Higher<VavrWitness.either,L>, R> either(Queue<Either<L, R>> nested){
            return Nested.of(widen(nested.map(EitherKind::widen)),Instances.definitions(),Eithers.Instances.definitions());
        }
        public static <T> Nested<queue,VavrWitness.stream,T> stream(Queue<Stream<T>> nested){
            return Nested.of(widen(nested.map(StreamKind::widen)),Instances.definitions(),Streams.Instances.definitions());
        }
        public static <T> Nested<queue,VavrWitness.list,T> list(Queue<List<T>> nested){
            return Nested.of(widen(nested.map(ListKind::widen)), Instances.definitions(),Lists.Instances.definitions());
        }
        public static <T> Nested<queue,array,T> array(Queue<Array<T>> nested){
            return Nested.of(widen(nested.map(ArrayKind::widen)),Instances.definitions(),Arrays.Instances.definitions());
        }
        public static <T> Nested<queue,vector,T> vector(Queue<Vector<T>> nested){
            return Nested.of(widen(nested.map(VectorKind::widen)),Instances.definitions(),Vectors.Instances.definitions());
        }
        public static <T> Nested<queue,hashSet,T> set(Queue<HashSet<T>> nested){
            return Nested.of(widen(nested.map(HashSetKind::widen)),Instances.definitions(), HashSets.Instances.definitions());
        }

        public static <T> Nested<queue,reactiveSeq,T> reactiveSeq(Queue<ReactiveSeq<T>> nested){
            QueueKind<ReactiveSeq<T>> x = widen(nested);
            QueueKind<Higher<reactiveSeq,T>> y = (QueueKind)x;
            return Nested.of(y,Instances.definitions(),ReactiveSeq.Instances.definitions());
        }

        public static <T> Nested<queue,maybe,T> maybe(Queue<Maybe<T>> nested){
            QueueKind<Maybe<T>> x = widen(nested);
            QueueKind<Higher<maybe,T>> y = (QueueKind)x;
            return Nested.of(y,Instances.definitions(),Maybe.Instances.definitions());
        }
        public static <T> Nested<queue,eval,T> eval(Queue<Eval<T>> nested){
            QueueKind<Eval<T>> x = widen(nested);
            QueueKind<Higher<eval,T>> y = (QueueKind)x;
            return Nested.of(y,Instances.definitions(),Eval.Instances.definitions());
        }
        public static <T> Nested<queue,Witness.future,T> cyclopsFuture(Queue<cyclops.async.Future<T>> nested){
            QueueKind<cyclops.async.Future<T>> x = widen(nested);
            QueueKind<Higher<Witness.future,T>> y = (QueueKind)x;
            return Nested.of(y,Instances.definitions(),cyclops.async.Future.Instances.definitions());
        }
        public static <S, P> Nested<queue,Higher<xor,S>, P> xor(Queue<Xor<S, P>> nested){
            QueueKind<Xor<S, P>> x = widen(nested);
            QueueKind<Higher<Higher<xor,S>, P>> y = (QueueKind)x;
            return Nested.of(y,Instances.definitions(),Xor.Instances.definitions());
        }
        public static <S,T> Nested<queue,Higher<reader,S>, T> reader(Queue<Reader<S, T>> nested){
            QueueKind<Reader<S, T>> x = widen(nested);
            QueueKind<Higher<Higher<reader,S>, T>> y = (QueueKind)x;
            return Nested.of(y,Instances.definitions(),Reader.Instances.definitions());
        }
        public static <S extends Throwable, P> Nested<queue,Higher<Witness.tryType,S>, P> cyclopsTry(Queue<cyclops.control.Try<P, S>> nested){
            QueueKind<cyclops.control.Try<P, S>> x = widen(nested);
            QueueKind<Higher<Higher<Witness.tryType,S>, P>> y = (QueueKind)x;
            return Nested.of(y,Instances.definitions(),cyclops.control.Try.Instances.definitions());
        }
        public static <T> Nested<queue,optional,T> queueal(Queue<Optional<T>> nested){
            QueueKind<Optional<T>> x = widen(nested);
            QueueKind<Higher<optional,T>> y = (QueueKind)x;
            return Nested.of(y,Instances.definitions(), Optionals.Instances.definitions());
        }
        public static <T> Nested<queue,completableFuture,T> completableQueue(Queue<CompletableFuture<T>> nested){
            QueueKind<CompletableFuture<T>> x = widen(nested);
            QueueKind<Higher<completableFuture,T>> y = (QueueKind)x;
            return Nested.of(y,Instances.definitions(), CompletableFutures.Instances.definitions());
        }
        public static <T> Nested<queue,Witness.stream,T> javaStream(Queue<java.util.stream.Stream<T>> nested){
            QueueKind<java.util.stream.Stream<T>> x = widen(nested);
            QueueKind<Higher<Witness.stream,T>> y = (QueueKind)x;
            return Nested.of(y,Instances.definitions(), cyclops.companion.Streams.Instances.definitions());
        }



    }

    public static interface NestedQueue{
        public static <T> Nested<reactiveSeq,queue,T> reactiveSeq(ReactiveSeq<Queue<T>> nested){
            ReactiveSeq<Higher<queue,T>> x = nested.map(QueueKind::widenK);
            return Nested.of(x,ReactiveSeq.Instances.definitions(),Instances.definitions());
        }

        public static <T> Nested<maybe,queue,T> maybe(Maybe<Queue<T>> nested){
            Maybe<Higher<queue,T>> x = nested.map(QueueKind::widenK);

            return Nested.of(x,Maybe.Instances.definitions(),Instances.definitions());
        }
        public static <T> Nested<eval,queue,T> eval(Eval<Queue<T>> nested){
            Eval<Higher<queue,T>> x = nested.map(QueueKind::widenK);

            return Nested.of(x,Eval.Instances.definitions(),Instances.definitions());
        }
        public static <T> Nested<Witness.future,queue,T> cyclopsFuture(cyclops.async.Future<Queue<T>> nested){
            cyclops.async.Future<Higher<queue,T>> x = nested.map(QueueKind::widenK);

            return Nested.of(x,cyclops.async.Future.Instances.definitions(),Instances.definitions());
        }
        public static <S, P> Nested<Higher<xor,S>,queue, P> xor(Xor<S, Queue<P>> nested){
            Xor<S, Higher<queue,P>> x = nested.map(QueueKind::widenK);

            return Nested.of(x,Xor.Instances.definitions(),Instances.definitions());
        }
        public static <S,T> Nested<Higher<reader,S>,queue, T> reader(Reader<S, Queue<T>> nested){

            Reader<S, Higher<queue, T>>  x = nested.map(QueueKind::widenK);

            return Nested.of(x,Reader.Instances.definitions(),Instances.definitions());
        }
        public static <S extends Throwable, P> Nested<Higher<Witness.tryType,S>,queue, P> cyclopsTry(cyclops.control.Try<Queue<P>, S> nested){
            cyclops.control.Try<Higher<queue,P>, S> x = nested.map(QueueKind::widenK);

            return Nested.of(x,cyclops.control.Try.Instances.definitions(),Instances.definitions());
        }
        public static <T> Nested<optional,queue,T> queueal(Optional<Queue<T>> nested){
            Optional<Higher<queue,T>> x = nested.map(QueueKind::widenK);

            return  Nested.of(Optionals.OptionalKind.widen(x), Optionals.Instances.definitions(), Instances.definitions());
        }
        public static <T> Nested<completableFuture,queue,T> completableQueue(CompletableFuture<Queue<T>> nested){
            CompletableFuture<Higher<queue,T>> x = nested.thenApply(QueueKind::widenK);

            return Nested.of(CompletableFutures.CompletableFutureKind.widen(x), CompletableFutures.Instances.definitions(),Instances.definitions());
        }
        public static <T> Nested<Witness.stream,queue,T> javaStream(java.util.stream.Stream<Queue<T>> nested){
            java.util.stream.Stream<Higher<queue,T>> x = nested.map(QueueKind::widenK);

            return Nested.of(cyclops.companion.Streams.StreamKind.widen(x), cyclops.companion.Streams.Instances.definitions(),Instances.definitions());
        }
    }

}