(ns greffe.analyze-test
  (:require
   [cljs.test :refer-macros [deftest testing is]]
   [greffe.analyze :as az]))

(deftest test-predicates
  (testing "attributes-text-editable?"
   (is (az/attributes-text-editable? {:tag :blah
                                      :attrs {}
                                      :content []})
     "No attributes, should be editable")

   (is (az/attributes-text-editable? {:tag :blah
                                      :attrs {:att "stuff"}
                                      :content []})
     "One attribute, should be editable")
   (is (= false
         (az/attributes-text-editable? {:tag :blah
                                        :attrs {:att1 "stuff"
                                                :att2 "more stuff"
                                                :att3 "additional stuff"}
                                        :content []}))
     "Three attributes, not editable"))

  (testing "too-complicated?"
    (is (not (az/too-complicated? {:tag :blah :attrs {} :content []}))
      "single empty node, not complicated")
    (is (az/too-complicated?
          {:tag :blah
           :attrs {}
           :content [{:tag :blah :attrs {}
                      :content [
                                {:tag :blah
                                 :attrs {}
                                 :content [
                                           {:tag :blah
                                            :attrs {}
                                            :content []}]}]}]})
      "three children: too complicated"))

  (testing "known-elem?"
    (is (az/known-elem?
          {:tag :blah :attrs {} :content []}
          {:blah {:stuff "about blah"}
           :foo {:stuff "about foo"}})
      "known element is known")

    (is (not
          (az/known-elem?
           {:tag :blah :attrs {} :content []}
           {:foo {:stuff "about foo"}}))
      "unknown element is unknown")))
