(ns greffe.analyze-test
  (:require
   [cljs.test :refer-macros [deftest testing is]]
   [clojure.zip :as zip]
   [greffe.analyze :as az]))

;;; don't forget that new test files need to added manually to
;;; test-runner.cljs



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



(deftest test-editable-type
  (let [all-editable-zip (zip/xml-zip
                           {:tag :blah :attrs {}
                            :content [{:tag :blah
                                       :attrs {}
                                       :content []}
                                      {:tag :blah
                                       :attrs {}
                                       :content []}]})
        complicated-zip
        (zip/xml-zip
          {:tag :blah :attrs {}
           :content [{:tag :blah
                      :attrs {}
                      :content [{:tag :blah
                                 :attrs {}
                                 :content
                                 [{:tag :blah
                                   :attrs {}
                                   :content
                                   [{:tag :blah
                                     :attrs {}
                                     :content
                                     []}]}]}]}]})
        tag-types {:blah {:stuff "about blah"}}]
    (is (= :edit (az/editable-type all-editable-zip tag-types))
      "simple 3-node zip is editable") 
    (is (= :no-text-edit (az/editable-type complicated-zip tag-types)))))


(deftest editable-check-test
  (let [
        basic-tree {:tag :blah :attrs {}
                            :content [{:tag :blah
                                       :attrs {}
                                       :content []}
                                      {:tag :blah
                                       :attrs {}
                                       :content []}]}
        all-editable-zip (zip/xml-zip basic-tree)
        many-attrs-zip (zip/xml-zip
                         (assoc basic-tree
                           :attrs {:a1 "1" :a2 "2" :a3 "3" :a4 "4"}))
        tag-types {:blah {:stuff "about blah"}
                   :foo {:stuff "about foo"}}
        checked-all-editable (az/edit-check
                               all-editable-zip
                               tag-types)]
    (is (= :edit (:edit-type (zip/root checked-all-editable))))
    (is (= :no-text-edit
          (:edit-type
           (zip/root (az/edit-check many-attrs-zip tag-types)))))))
