package cyclops.conversion.vavr;

import com.aol.cyclops2.types.Zippable;
import cyclops.async.Future;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.control.Try;
import cyclops.control.Xor;
import javaslang.Lazy;
import javaslang.control.Either;
import javaslang.control.Option;


public class ToCyclopsReact {

    public static <T> Future[] futures(javaslang.concurrent.Future<T>... futures){

        Future[] array = new Future[futures.length];
        for(int i=0;i<array.length;i++){
            array[i]=future(futures[i]);
        }
        return array;
    }
    public static <T> Future<T> future(javaslang.concurrent.Future<T> future){
        Future<T> res = Future.future();
        future.onSuccess(v->res.complete(v))
                .onFailure(t->res.completeExceptionally(t));
        return res;
    }

    public static <L,R> Xor<L,R> xor(Either<L,R> either){
        return either.fold(Xor::secondary,Xor::primary);
    }
    public static <T> Try<T,Throwable> toTry(javaslang.control.Try<T> t){
        if(t.isFailure()){
            return Try.failure(t.getCause());
        }
        return Try.success(t.get());
    }

    public static <T> Maybe<T> maybe(Option<T> opt){
        return opt.isDefined() ? Maybe.just(opt.get()) : Maybe.none();
    }
    public static <T> Eval<T> eval(Lazy<T> opt){
        return Eval.later(opt);
    }
    public static <R> cyclops.control.lazy.Either<Throwable,R> either(javaslang.concurrent.Future<R> either){
        return cyclops.control.lazy.Either.fromFuture(future(either));

    }

    public static <T> Maybe<T> maybe(javaslang.concurrent.Future<T> opt){
        return Maybe.fromFuture(future(opt));
    }
    public static <T> Eval<T> eval(javaslang.concurrent.Future<T> opt){
        return Eval.fromFuture(future(opt));
    }

}
