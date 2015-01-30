(ns greffe.doc-comps
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs-xml.core :as cx]))



(defmulti element-component (fn [el]
                              (let [val (om/value el)]
                               (cond
                                 (string? val)
                                 :text
                                
                                 (and
                                   (contains? val :content)
                                   (contains? val :tag))
                                 :elem

                                 true
                                 :wtf))))


(defmethod element-component :text [elem owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil elem))))

(defmethod element-component :elem [elem owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (dom/div #js {:className (str "xml-" (name (:tag elem)))}
        (name (:tag elem))
        (when (pos? (count (:content elem)))
          (apply dom/div nil
           (om/build-all element-component (:content elem))))))))

(defmethod element-component :wtf [elem owner]
  (reify
    om/IRender
    (render [_]
      (.log js/console "WTF?")
      (dom/div nil (str "WTF? " (type (om/value elem)) " " elem)))))
