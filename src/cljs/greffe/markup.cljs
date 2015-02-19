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
           :contains []}})


(def inner-tokens
  {"//" {:tag :rhyme
         :closing-tag "//"}
   "|" {:tag :caesura
        :no-content true}})
