(ns greffe.markup)


(def markup
  {:body {:type :container
          :contains [:div]
          :html "div"}
   :text {:type :container
          :contains [:div]
          :html "div"}
   :div {:type :container
         :contains [:div :head :lg]
         :html "div"}
   :head {:type :chunk
          :contains [:note]
          :line-token "*"
          :html "h2"}
   :lg {:type :multi-chunk
        :contains [:l]
        :html "ul"}
   :l {:type :chunk
       :contains [:rhyme :caesura :note]
       :line-token "-"
       :html "li"}
   :note {:type :inner
          :begin-token "{{"
          :end-token "}}"
          :contains []
          :html "strong"}
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
