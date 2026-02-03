package org.example;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

public class Main {
    public static void main(String[] args) {
        // Observable emits values
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
        });

        // Observer subscribes and reacts
        Observer<Integer> observer = new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("Subscribed!");
            }

            @Override
            public void onNext(Integer value) {
                System.out.println("Received: " + value);
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Error: " + e.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("Completed!");
            }
        };

        observable.subscribe(observer);

        System.out.println("\n--- Using simplified lambda syntax ---");

        // Simplified version using lambdas
        Observable.just(10, 20, 30)
                .subscribe(
                        value -> System.out.println("Value: " + value),
                        error -> System.out.println("Error: " + error),
                        () -> System.out.println("Done!")
                );
    }
}
