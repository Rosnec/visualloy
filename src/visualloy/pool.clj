(ns visualloy.pool
  "Functional ForkJoinPool interfaces adapted from
  http://tech.puredanger.com/2011/01/04/forkjoin-clojure/"
  (:import [util.java.process IFnTask]
           [java.util.concurrent.ForkJoinPool]))

(defn new-pool
  ([]  (java.util.concurrent.ForkJoinPool.))
  ([p] (java.util.concurrent.ForkJoinPool. p)))

(defn shutdown-pool [pool]
  (.shutdown pool))

(defn submit-task
  "Invoke and return a Future."
  [pool task]
  (.submit pool task))

(defn invoke-task
  "Invoke and wait for a response."
  [pool task]
  (.invoke pool task))

(defn execute-task
  "Invoke task asynchronously."
  [pool task]
  (.execute pool task))

(defn new-task [f]
  (IFnTask. f))

(defn fork-task [task]
  (.fork task))

(defn join-task [task]
  (.join task))

(defn compute-task [task]
  (.compute task))