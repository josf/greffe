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
        (:attrs node)
        (> (count (:attrs node)) max-attr-count))
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
  "Returns the edit status of the element."
   (let [n (zip/node loc)]
    (cond
      (not (known-elem? (zip/node loc) tag-types))
      :unknown-element

      
      (not (attributes-text-editable? n))
      :no-text-edit


      (too-complicated? (zip/node loc))
      :no-text-edit

      true
      :edit
     )))



(defn edit-check [loc tag-types]
   "Checks an entire tree"
  (loop [l loc]
    (cond
      (zip/end? l)
      (zip/xml-zip (zip/root l))        ;reinitialize the zipper

      (not (zip/branch? l))
      (recur (zip/next l))

      true
      (let [ed-type (editable-type l tag-types)]
        (recur (zip/next (zip/edit l #(assoc % :edit-type ed-type))))))))
