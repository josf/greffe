(ns greffe.net
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
   [goog.net.XhrManager :as xhrmgr]
   [goog.structs.Map]
   [cljs.core.async :refer [<! chan put! sliding-buffer]]))


(defn xhr-mgr
  "Return a goog.net.XhrManager object with standard json params."
  []
   (goog.net.XhrManager.
                 1
                 (goog.structs.Map. #js {:accept "application/json"})
                 js/undefined
                 js/undefined
                 js/undefined))


(defn get-xml
  ""
  [url channel mgr]
  (.send mgr
    "getxml"                            ; mgr id
    url
    "GET"
    ""
    nil
    1
    (fn [ev]
      (let [xhr (.-target ev)]
        (if (.isSuccess xhr)
          (put! channel (.getResponseXml xhr))
          (println (str  "Failed to get xml doc. " url)))))))
