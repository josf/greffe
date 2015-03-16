(ns greffe.analyze
  (:require [clojure.zip :as zip]
            [gmark.tei-elems :as gmt]
            [greffe.markup :as mk]))

(def max-attr-count 2)
(def max-child-count 2)

(defn zipper? [loc]
  (contains? loc :zip/make-node))

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


(defn known-elem? [node tag-types]
    {:pre [(map? tag-types)]}
    "we obviously can't edit it if we don't know what it is."
  (contains? tag-types (:tag node)))

(defn editable-type [loc tag-types]
  (let [n (zip/node loc)]
   (cond
     (not (attributes-text-editable? n))
     :no-text

     (too-complicated? (zip/node loc))
     :no-text

     (not (known-elem? (zip/node loc) tag-types))
     :no-text
     )))



