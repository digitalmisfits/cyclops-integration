package cyclops.companion.vavr;

import com.aol.cyclops.vavr.hkt.ListKind;
import cyclops.control.Maybe;
import cyclops.conversion.vavr.FromCyclopsReact;
import cyclops.monads.VavrWitness;
import cyclops.monads.VavrWitness.vector;
import cyclops.collections.vavr.VavrVectorX;
import com.aol.cyclops.vavr.hkt.VectorKind;
import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.anyM.AnyMSeq;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;
import cyclops.monads.transformers.ListT;
import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.Active;
import cyclops.typeclasses.InstanceDefinitions;
import cyclops.typeclasses.Nested;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import io.vavr.collection.List;
import io.vavr.collection.Vector;
import lombok.experimental.UtilityClass;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Optional;
import java.util.function.*;


public class Vectors {

    public static <T,W extends WitnessType<W>> ListT<W, T> liftM(Vector<T> opt, W witness) {
        return ListT.ofList(witness.adapter().unit(VavrVectorX.ofAll(opt)));
    }
   
    public static <T> AnyMSeq<vector,T> anyM(Vector<T> option) {
        return AnyM.ofSeq(option, vector.INSTANCE);
    }
    /**
     * Perform a For Comprehension over a Vector, accepting 3 generating functions.
     * This results in a four level nested internal iteration over the provided Publishers.
     *
     *  <pre>
     * {@code
     *
     *   import static cyclops.Vectors.forEach4;
     *
    forEach4(IntVector.range(1,10).boxed(),
    a-> Vector.iterate(a,i->i+1).limit(10),
    (a,b) -> Vector.<Integer>of(a+b),
    (a,b,c) -> Vector.<Integer>just(a+b+c),
    Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Vector
     * @param value2 Nested Vector
     * @param value3 Nested Vector
     * @param value4 Nested Vector
     * @param yieldingFunction  Generates a result per combination
     * @return Vector with an element per combination of nested publishers generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Vector<R> forEach4(Vector<? extends T1> value1,
                                                               Function<? super T1, ? extends Vector<R1>> value2,
                                                               BiFunction<? super T1, ? super R1, ? extends Vector<R2>> value3,
                                                               Fn3<? super T1, ? super R1, ? super R2, ? extends Vector<R3>> value4,
                                                               Fn4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Vector<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Vector<R2> b = value3.apply(in,ina);
                return b.flatMap(inb -> {
                    Vector<R3> c = value4.apply(in,ina,inb);
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });

    }

    /**
     * Perform a For Comprehension over a Vector, accepting 3 generating function.
     * This results in a four level nested internal iteration over the provided Publishers.
     * <pre>
     * {@code
     *
     *  import static com.aol.cyclops2.reactor.Vectores.forEach4;
     *
     *  forEach4(IntVector.range(1,10).boxed(),
    a-> Vector.iterate(a,i->i+1).limit(10),
    (a,b) -> Vector.<Integer>just(a+b),
    (a,b,c) -> Vector.<Integer>just(a+b+c),
    (a,b,c,d) -> a+b+c+d <100,
    Tuple::tuple);
     *
     * }
     * </pre>
     *
     * @param value1 top level Vector
     * @param value2 Nested Vector
     * @param value3 Nested Vector
     * @param value4 Nested Vector
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return Vector with an element per combination of nested publishers generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Vector<R> forEach4(Vector<? extends T1> value1,
                                                                 Function<? super T1, ? extends Vector<R1>> value2,
                                                                 BiFunction<? super T1, ? super R1, ? extends Vector<R2>> value3,
                                                                 Fn3<? super T1, ? super R1, ? super R2, ? extends Vector<R3>> value4,
                                                                 Fn4<? super T1, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                                 Fn4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Vector<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Vector<R2> b = value3.apply(in,ina);
                return b.flatMap(inb -> {
                    Vector<R3> c = value4.apply(in,ina,inb);
                    return c.filter(in2->filterFunction.apply(in,ina,inb,in2))
                            .map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
    }

    /**
     * Perform a For Comprehension over a Vector, accepting 2 generating function.
     * This results in a three level nested internal iteration over the provided Publishers.
     *
     * <pre>
     * {@code
     *
     * import static Vectors.forEach3;
     *
     * forEach(IntVector.range(1,10).boxed(),
    a-> Vector.iterate(a,i->i+1).limit(10),
    (a,b) -> Vector.<Integer>of(a+b),
    Tuple::tuple);
     *
     * }
     * </pre>
     *
     *
     * @param value1 top level Vector
     * @param value2 Nested Vector
     * @param value3 Nested Vector
     * @param yieldingFunction Generates a result per combination
     * @return Vector with an element per combination of nested publishers generated by the yielding function
     */
    public static <T1, T2, R1, R2, R> Vector<R> forEach3(Vector<? extends T1> value1,
                                                         Function<? super T1, ? extends Vector<R1>> value2,
                                                         BiFunction<? super T1, ? super R1, ? extends Vector<R2>> value3,
                                                         Fn3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Vector<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Vector<R2> b = value3.apply(in,ina);
                return b.map(in2 -> yieldingFunction.apply(in, ina, in2));
            });


        });


    }

    /**
     * Perform a For Comprehension over a Vector, accepting 2 generating function.
     * This results in a three level nested internal iteration over the provided Publishers.
     * <pre>
     * {@code
     *
     * import static Vectors.forEach;
     *
     * forEach(IntVector.range(1,10).boxed(),
    a-> Vector.iterate(a,i->i+1).limit(10),
    (a,b) -> Vector.<Integer>of(a+b),
    (a,b,c) ->a+b+c<10,
    Tuple::tuple)
    .toVectorX();
     * }
     * </pre>
     *
     * @param value1 top level Vector
     * @param value2 Nested publisher
     * @param value3 Nested publisher
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return
     */
    public static <T1, T2, R1, R2, R> Vector<R> forEach3(Vector<? extends T1> value1,
                                                         Function<? super T1, ? extends Vector<R1>> value2,
                                                         BiFunction<? super T1, ? super R1, ? extends Vector<R2>> value3,
                                                         Fn3<? super T1, ? super R1, ? super R2, Boolean> filterFunction,
                                                         Fn3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Vector<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Vector<R2> b = value3.apply(in,ina);
                return b.filter(in2->filterFunction.apply(in,ina,in2))
                        .map(in2 -> yieldingFunction.apply(in, ina, in2));
            });



        });
    }

    /**
     * Perform a For Comprehension over a Vector, accepting an additonal generating function.
     * This results in a two level nested internal iteration over the provided Publishers.
     *
     * <pre>
     * {@code
     *
     *  import static Vectors.forEach2;
     *  forEach(IntVector.range(1, 10).boxed(),
     *          i -> Vector.range(i, 10), Tuple::tuple)
    .forEach(System.out::println);

    //(1, 1)
    (1, 2)
    (1, 3)
    (1, 4)
    ...
     *
     * }</pre>
     *
     * @param value1 top level Vector
     * @param value2 Nested publisher
     * @param yieldingFunction Generates a result per combination
     * @return
     */
    public static <T, R1, R> Vector<R> forEach2(Vector<? extends T> value1,
                                                Function<? super T, Vector<R1>> value2,
                                                BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Vector<R1> a = value2.apply(in);
            return a.map(in2 -> yieldingFunction.apply(in,  in2));
        });

    }

    /**
     *
     * <pre>
     * {@code
     *
     *   import static Vectors.forEach2;
     *
     *   forEach(IntVector.range(1, 10).boxed(),
     *           i -> Vector.range(i, 10),
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
     * @param value1 top level Vector
     * @param value2 Nested publisher
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return
     */
    public static <T, R1, R> Vector<R> forEach2(Vector<? extends T> value1,
                                                Function<? super T, ? extends Vector<R1>> value2,
                                                BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                                BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Vector<R1> a = value2.apply(in);
            return a.filter(in2->filterFunction.apply(in,in2))
                    .map(in2 -> yieldingFunction.apply(in,  in2));
        });
    }
    public static <T> Active<vector,T> allTypeclasses(Vector<T> array){
        return Active.of(VectorKind.widen(array), Vectors.Instances.definitions());
    }
    public static <T,W2,R> Nested<vector,W2,R> mapM(Vector<T> array, Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        Vector<Higher<W2, R>> e = array.map(fn);
        VectorKind<Higher<W2, R>> lk = VectorKind.widen(e);
        return Nested.of(lk, Vectors.Instances.definitions(), defs);
    }
    /**
     * Companion class for creating Type Class instances for working with Vectors
     *
     */
    @UtilityClass
    public static class Instances {

        public static InstanceDefinitions<vector> definitions() {
            return new InstanceDefinitions<vector>() {

                @Override
                public <T, R> Functor<vector> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<vector> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<vector> applicative() {
                    return Instances.zippingApplicative();
                }

                @Override
                public <T, R> Monad<vector> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<vector>> monadZero() {
                    return Maybe.just(Instances.monadZero());
                }

                @Override
                public <T> Maybe<MonadPlus<vector>> monadPlus() {
                    return Maybe.just(Instances.monadPlus());
                }

                @Override
                public <T> Maybe<MonadPlus<vector>> monadPlus(Monoid<Higher<vector, T>> m) {
                    return Maybe.just(Instances.monadPlus(m));
                }

                @Override
                public <C2, T> Maybe<Traverse<vector>> traverse() {
                    return Maybe.just(Instances.traverse());
                }

                @Override
                public <T> Maybe<Foldable<vector>> foldable() {
                    return Maybe.just(Instances.foldable());
                }

                @Override
                public <T> Maybe<Comonad<vector>> comonad() {
                    return Maybe.none();
                }

                @Override
                public <T> Maybe<Unfoldable<vector>> unfoldable() {
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
         *  VectorKind<Integer> list = Vectors.functor().map(i->i*2, VectorKind.widen(Arrays.asVector(1,2,3));
         *
         *  //[2,4,6]
         *
         *
         * }
         * </pre>
         *
         * An example fluent api working with Vectors
         * <pre>
         * {@code
         *   VectorKind<Integer> list = Vectors.unit()
        .unit("hello")
        .then(h->Vectors.functor().map((String v) ->v.length(), h))
        .convert(VectorKind::narrowK);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for Vectors
         */
        public static <T,R>Functor<vector> functor(){
            BiFunction<VectorKind<T>,Function<? super T, ? extends R>,VectorKind<R>> map = Instances::map;
            return General.functor(map);
        }
        /**
         * <pre>
         * {@code
         * VectorKind<String> list = Vectors.unit()
        .unit("hello")
        .convert(VectorKind::narrowK);

        //Arrays.asVector("hello"))
         *
         * }
         * </pre>
         *
         *
         * @return A factory for Vectors
         */
        public static <T> Pure<vector> unit(){
            return General.<vector,T>unit(VectorKind::of);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.VectorKind.widen;
         * import static com.aol.cyclops.util.function.Lambda.l1;
         * import static java.util.Arrays.asVector;
         *
        Vectors.zippingApplicative()
        .ap(widen(asVector(l1(this::multiplyByTwo))),widen(asVector(1,2,3)));
         *
         * //[2,4,6]
         * }
         * </pre>
         *
         *
         * Example fluent API
         * <pre>
         * {@code
         * VectorKind<Function<Integer,Integer>> listFn =Vectors.unit()
         *                                                  .unit(Lambda.l1((Integer i) ->i*2))
         *                                                  .convert(VectorKind::narrowK);

        VectorKind<Integer> list = Vectors.unit()
        .unit("hello")
        .then(h->Vectors.functor().map((String v) ->v.length(), h))
        .then(h->Vectors.zippingApplicative().ap(listFn, h))
        .convert(VectorKind::narrowK);

        //Arrays.asVector("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for Vectors
         */
        public static <T,R> Applicative<vector> zippingApplicative(){
            BiFunction<VectorKind< Function<T, R>>,VectorKind<T>,VectorKind<R>> ap = Instances::ap;
            return General.applicative(functor(), unit(), ap);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.VectorKind.widen;
         * VectorKind<Integer> list  = Vectors.monad()
        .flatMap(i->widen(VectorX.range(0,i)), widen(Arrays.asVector(1,2,3)))
        .convert(VectorKind::narrowK);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    VectorKind<Integer> list = Vectors.unit()
        .unit("hello")
        .then(h->Vectors.monad().flatMap((String v) ->Vectors.unit().unit(v.length()), h))
        .convert(VectorKind::narrowK);

        //Arrays.asVector("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for Vectors
         */
        public static <T,R> Monad<vector> monad(){

            BiFunction<Higher<vector,T>,Function<? super T, ? extends Higher<vector,R>>,Higher<vector,R>> flatMap = Instances::flatMap;
            return General.monad(zippingApplicative(), flatMap);
        }
        /**
         *
         * <pre>
         * {@code
         *  VectorKind<String> list = Vectors.unit()
        .unit("hello")
        .then(h->Vectors.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(VectorKind::narrowK);

        //Arrays.asVector("hello"));
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<vector> monadZero(){

            return General.monadZero(monad(), VectorKind.widen(Vector.empty()));
        }
        /**
         * <pre>
         * {@code
         *  VectorKind<Integer> list = Vectors.<Integer>monadPlus()
        .plus(VectorKind.widen(Arrays.asVector()), VectorKind.widen(Arrays.asVector(10)))
        .convert(VectorKind::narrowK);
        //Arrays.asVector(10))
         *
         * }
         * </pre>
         * @return Type class for combining Vectors by concatenation
         */
        public static <T> MonadPlus<vector> monadPlus(){
            Monoid<VectorKind<T>> m = Monoid.of(VectorKind.widen(Vector.empty()), Instances::concat);
            Monoid<Higher<vector,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        /**
         *
         * <pre>
         * {@code
         *  Monoid<VectorKind<Integer>> m = Monoid.of(VectorKind.widen(Arrays.asVector()), (a,b)->a.isEmpty() ? b : a);
        VectorKind<Integer> list = Vectors.<Integer>monadPlus(m)
        .plus(VectorKind.widen(Arrays.asVector(5)), VectorKind.widen(Arrays.asVector(10)))
        .convert(VectorKind::narrowK);
        //Arrays.asVector(5))
         *
         * }
         * </pre>
         *
         * @param m Monoid to use for combining Vectors
         * @return Type class for combining Vectors
         */
        public static <T> MonadPlus<vector> monadPlus(Monoid<Higher<vector,T>> m){
            Monoid<Higher<vector,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        public static <T> MonadPlus<vector> monadPlusK(Monoid<VectorKind<T>> m){
            Monoid<Higher<vector,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }

        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<vector> traverse(){

            BiFunction<Applicative<C2>,VectorKind<Higher<C2, T>>,Higher<C2, VectorKind<T>>> sequenceFn = (ap, list) -> {

                Higher<C2,VectorKind<T>> identity = ap.unit(VectorKind.widen(Vector.empty()));

                BiFunction<Higher<C2,VectorKind<T>>,Higher<C2,T>,Higher<C2,VectorKind<T>>> combineToVector =   (acc, next) -> ap.apBiFn(ap.unit((a, b) -> VectorKind.widen(VectorKind.narrow(a).append(b))),
                        acc,next);

                BinaryOperator<Higher<C2,VectorKind<T>>> combineVectors = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> VectorKind.widen(VectorKind.narrow(l1).appendAll(l2.narrow()))),a,b); ;

                return ReactiveSeq.fromIterable(VectorKind.narrow(list))
                        .reduce(identity,
                                combineToVector,
                                combineVectors);


            };
            BiFunction<Applicative<C2>,Higher<vector,Higher<C2, T>>,Higher<C2, Higher<vector,T>>> sequenceNarrow  =
                    (a,b) -> VectorKind.widen2(sequenceFn.apply(a, VectorKind.narrowK(b)));
            return General.traverse(zippingApplicative(), sequenceNarrow);
        }

        /**
         *
         * <pre>
         * {@code
         * int sum  = Vectors.foldable()
        .foldLeft(0, (a,b)->a+b, VectorKind.widen(Arrays.asVector(1,2,3,4)));

        //10
         *
         * }
         * </pre>
         *
         *
         * @return Type class for folding / reduction operations
         */
        public static <T> Foldable<vector> foldable(){
            BiFunction<Monoid<T>,Higher<vector,T>,T> foldRightFn =  (m, l)-> ReactiveSeq.fromIterable(VectorKind.narrow(l)).foldRight(m);
            BiFunction<Monoid<T>,Higher<vector,T>,T> foldLeftFn = (m, l)-> ReactiveSeq.fromIterable(VectorKind.narrow(l)).reduce(m);
            return General.foldable(foldRightFn, foldLeftFn);
        }

        private static  <T> VectorKind<T> concat(VectorKind<T> l1, VectorKind<T> l2){

            return VectorKind.widen(l1.appendAll(VectorKind.narrow(l2)));

        }

        private static <T,R> VectorKind<R> ap(VectorKind<Function< T, R>> lt, VectorKind<T> list){
            return VectorKind.widen(FromCyclopsReact.fromStream(ReactiveSeq.fromIterable(lt.narrow()).zip(list.narrow(), (a, b)->a.apply(b))).toVector());
        }
        private static <T,R> Higher<vector,R> flatMap(Higher<vector,T> lt, Function<? super T, ? extends  Higher<vector,R>> fn){
            return VectorKind.widen(VectorKind.narrow(lt).flatMap(fn.andThen(VectorKind::narrow)));
        }
        private static <T,R> VectorKind<R> map(VectorKind<T> lt, Function<? super T, ? extends R> fn){
            return VectorKind.widen(VectorKind.narrow(lt).map(in->fn.apply(in)));
        }
        public static Unfoldable<vector> unfoldable(){
            return new Unfoldable<vector>() {
                @Override
                public <R, T> Higher<vector, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn) {
                    return VectorKind.widen(ReactiveSeq.unfold(b,fn).collect(Vector.collector()));

                }
            };
        }
    }



}