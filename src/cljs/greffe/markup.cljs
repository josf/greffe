(ns greffe.markup)


(def markup
  {:body {:type :container
          :contains [:div]}
   :text {:type :container
          :contains [:div]}
   :div {:type :container
         :contains [:div :head :lg]}
   :head {:type :chunk
          :contains [:note]
          :line-token "*"}
   :lg {:type :multi-chunk
        :contains [:l]}
   :l {:type :chunk
       :contains [:rhyme :caesura :note]
       :line-token "-"}
   :note {:type :inner
          :begin-token "{{"
          :end-token "}}"
          :contains []}
   :caesura {:type :empty
             :begin-token "|"
             :end-token "|"
             :contains nil}
   :rhyme {:type :inner
           :begin-token "//"
           :end-token "//"
           :contains []
           :html "em"}})

(def inner-tokens
  (->>  markup
   (filter
     (fn [[_ descrip]]
       (#{:inner :empty} (:type descrip))))
   (map
     (fn [[tag descrip]]
       (let [info {:tag tag}]
        [(:begin-token descrip)
         (if (= :empty (:type descrip))
           (assoc info :no-content true)
           (assoc info :closing-tag
             (or
               (:end-token descrip)
               (:begin-token descrip))))])))
   (into {})))
