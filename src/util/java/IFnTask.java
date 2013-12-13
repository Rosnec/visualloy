package util.java;

import java.util.concurrent.RecursiveTask;
import clojure.lang.IFn;

public class IFnTask extends RecursiveTask<Object> {
    private final IFn fn;

    public IFnTask(IFn fn) {
        this.fn = fn;
    }
    public Object compute() {
        try {
            return fn.invoke(this);
        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new RuntimeException(e);
}   }   }
