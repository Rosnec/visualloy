(ns visualloy.util)

(defn daemon
  [function]
  (doto 
    (Thread. function)
    (.setDaemon true)
    (.start)))

(defn nth-deep
  "Returns the value in coll at the N-dimensional index given as a collection."
  [coll index]
  (reduce nth coll index))

(defn random-int-portions
  "Return a sequence of integers which add to total"
  [total length]
  (let [remainder (atom total)]
    (for [i (range length)]
      (if (< i length)
        (let [value (rand-int @remainder)]
          (swap! remainder - value)
          value)
        @remainder))))

(defn random-float-portions
  "Return a sequence of integers which add to total"
  [total length]
  (let [remainder (atom total)]
    (for [i (range length)]
      (if (< (inc i) length)
        (let [value (rand @remainder)]
          (swap! remainder - value)
          value)
        @remainder))))

(defn mean
  "Returns the mean of a collection"
  [coll]
  (/ (apply + coll)
     (count coll)))

(defn interpolate-value
  "Given a minimum and maximum output, a value and a maximum value, returns
  the output that is portion of the way from min-out to max-out"
  ([A B portion]
     (let [min-out (min A B)
           max-out (max A B)
           value (+ A (* portion (- B A)))]
         (cond (< value min-out) min-out
               (> value max-out) max-out
               :else value)))
  ([A B value max-value]
     (interpolate-value A B (/ value max-value))))

(defn dimensions
  "Returns a vector of the dimensions of a 2D collection. Assumes all rows have
  same length"
  [coll-2d]
  (let [height (count coll-2d)
        width  (count (first coll-2d))]
    [height width]))

(defn area
  "Returns the area of a rectangle with the given corner indices"
  [top-left bot-left top-right bot-right]
  (* (- bot-left  top-left)
     (- bot-right top-right)))

(defn midpoint
  "Finds the midpoint between two integers"
  [x y]
  (bit-shift-right (+ x y) 1))

;; (defn safe-add
;;   "Adds a sequence of longs. If the sum is ever a negative number, returns
;;   Long/MAX_VALUE, so it is advised that only positive numbers be used here.
;;   The purpose is to create a ceiling of Long/MAX_VALUE"
;;   [num & more]
;;   (if (empty? more)
;;     num
;;     (let [sum (unchecked-add num (first more))]
;;       (if (neg? sum)
;;         Long/MAX_VALUE
;;         (recur sum (rest more))))))

(defn safe-add
  ""
  [num & more]
  (if (empty? more)
    num
    (let [num2 (first more)]
      (if (if (> num2 0)
            (> num (- Long/MAX_VALUE num2))
            (< num (- Long/MIN_VALUE num2)))
        Long/MAX_VALUE
        (recur (+ num num2) (rest more))))))

(defn safe-multiply
  "Works like long-add, but for multiplication"
  [^java.lang.Long l ^java.lang.Double d]
  (cond
    (zero? l) 0
    (>= (/ Long/MAX_VALUE l) d) (unchecked-multiply l d)
    :else Long/MAX_VALUE))
