(ns greffe.core
  (:require-macros [cljs.core.async.macros :refer [go alt! go-loop]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.zip :as zip]
            [cljs.core.async :refer [<! chan put! sliding-buffer alts! mult tap untap]]
            [cljs-xml.core :as cx]
            [greffe.net :as net]
            [greffe.doc-comps :as dc]))

(defonce app-state (atom {:text "Hello Chestnut!"
                          :head nil
                          :body nil}))

(defn main []
  (om/root
    (fn [app owner]
      (reify
        om/IInitState
        (init-state [_]
          {:xml-chan (chan)
           :xhr-mgr (net/xhr-mgr)})
        
        om/IWillMount
        (will-mount [_]
          (let [xml-chan (om/get-state owner :xml-chan)]

            (go-loop []
              (let [xml-doc (<! xml-chan)
                    data-doc (cx/parse xml-doc)
                    doc-zip (zip/xml-zip data-doc)]
                (om/transact! app
                  (fn [a]
                    (let [nodes (take-while
                                  (complement zip/end?)
                                  (iterate zip/next doc-zip))]
                     (assoc a
                       :body
                       (zip/node
                        (first
                          (filter #(= :body (:tag (zip/node %))) nodes)))
                       :head
                       (zip/node
                        (first
                          (filter #(= :teiHeader (:tag (zip/node %))) nodes)))))))))

           ;; get our doc
           (net/get-xml
             "xml/malherbe-rabel.xml"
             xml-chan
             (om/get-state owner :xhr-mgr))))
        
        om/IRender
        (render [_]
          (dom/div nil
            (dom/h1 nil "Greffe")
            (when (:body app)
              (dom/div #js {:id "edit"}
                (om/build dc/element-component (:body app))))
            
            (when (:body app)
              (dom/div #js {:id "xml"}
                (om/build dc/xml-display (:body app))))))))
    app-state
    {:target (. js/document (getElementById "app"))}))
