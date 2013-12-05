(ns visualloy.util)

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

(defn interpolate-value
  "Given a minimum and maximum output, a value and a maximum value, returns
  the output that is portion of the way from min-out to max-out"
  ([min-out max-out portion]
     (let [value (+ min-out
                    (* portion
                       (- max-out min-out)))]
       (cond (< value min-out) min-out
             (> value max-out) max-out
             :else value)))
  ([min-out max-out value max-value]
     (interpolate-value min-out max-out (/ value max-value))))

(defn dimensions
  "Returns a vector of the dimensions of a 2D array. Assumes all rows have same
  length"
  [arr]
  (let [rows (count arr)
        cols (count (aget arr 0))]
    [rows cols]))

(defn safe-add
  "Adds a sequence of longs. If the sum is ever a negative number, returns
  Long/MAX_VALUE, so it is advised that only positive numbers be used here.
  The purpose is to create a ceiling of Long/MAX_VALUE"
  [num & more]
  (if (empty? more)
    num
    (let [sum (unchecked-add num (first more))]
      (if (neg? sum)
        Long/MAX_VALUE
        (recur sum (rest more))))))

(defn safe-multiply
  "Works like long-add, but for multiplication"
  [^java.lang.Long l ^java.lang.Double d]
  (cond
    (zero? l) 0
    (>= (/ Long/MAX_VALUE l) d) (* l d)
    :else Long/MAX_VALUE))
