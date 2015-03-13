(ns greffe.analyze
  (:require [clojure.zip :as zip]))

(def max-attr-count 2)
(def max-child-count 2)

(defn attributes-text-editable? [node]
  (if (and
        {:attrs node}
        (> (count {:attrs node}) max-attr-count))
    false
    true))


(defn too-complicated? [node]
  (let [new-loc (zip/xml-zip node)]
    (when
        (first
         (keep
           #(when (> (count (zip/path %)) max-child-count) true)
           (take-while (complement zip/end?) (iterate zip/next new-loc))))
      true)))


(defn editable-type [loc]
  (let [n (zip/node loc)]
   (cond
     (not (attributes-text-editable? n))
     :no-text)))



