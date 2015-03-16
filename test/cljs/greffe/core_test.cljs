(ns greffe.core-test
  (:require
   [cljs.test :refer-macros [deftest testing is]]
   [greffe.core :as gc]))

(deftest test-the-test
  (is (= 1 1))
  (is (= 1 5)))


