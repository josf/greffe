(ns greffe.doc-comps
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs-xml.core :as cx]
   [gmark.core :as gm]
   [gmark.tei-elems :as gmt]
   [greffe.markup :as mk]))

(defn element-attributes-component [attrs owner]
  (reify
    om/IRender
    (render [_]
      (apply dom/ul #js {:className "attributes"}
       (map #(dom/li nil (str (first %) " " (second %))) attrs)))))


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
    om/IRenderState
    (render-state [_ state]
      (dom/div nil elem))))

(defmethod element-component :elem [elem owner]
  (reify
    om/IInitState
    (init-state [_] {:hoverTimeout nil
                     :showButton false
                     :editText false
                     :editContent nil})
    
    om/IRenderState
    (render-state [_ state]
      (dom/div #js {:className (str "xml-" (name (:tag elem)) " tei-block col-md-12")
                    :onMouseEnter
                    (fn [ev]
                      (om/set-state! owner :hoverTimeout
                        (.setTimeout
                          js/window
                          (fn []
                            (.log js/console "show button true")
                            (om/set-state! owner :showButton true))
                          200)))
                    :onMouseLeave
                    (fn [ev]
                      (when-not (om/get-state owner :editText)
                        (.clearTimeout js/window (om/get-state owner :hoverTimeout))
                        (om/set-state! owner :hoverTimeout nil)
                        (om/set-state! owner :showButton false)))}

        (dom/div #js {:className "row"}
         (dom/div #js {:className "block-data col-md-1"}
           (dom/div #js {:className "name"}
             (name (:tag elem)))
           (om/build element-attributes-component (:attrs elem))
           (when (:showButton state)
             (dom/div #js {:className "controls"}
               (let [elem-as-text (gm/to-gmark elem  gmt/tei)]
                 (dom/a #js {:onClick (fn [ev]
                                        (om/set-state! owner :editContent elem-as-text)
                                        (om/set-state!
                                          owner
                                          :editText
                                          (not (om/get-state owner :editText))))}
                  "Editer"))))
           (when (:editText state)
             (dom/textarea #js {:value (:editContent state)
                                :cols "50" :rows "10"
                             :onChange
                                (fn [ev]
                                  (let [new-val (-> ev .-target .-value)]
                                    (om/set-state! owner :editContent new-val)
                                    (om/transact! elem
                                      (fn [el]
                                        (if-let [new-el
                                                 (gm/parse-gmark-text
                                                   new-val
                                                   mk/inner-tokens
                                                   (assoc el :content []))]
                                          new-el
                                          el)))))})))
         (when (pos? (count (:content elem)))
           (apply dom/div #js {:className "block-contents col-md-10"}
             (om/build-all element-component (:content elem)))))))))

(defmethod element-component :wtf [elem owner]
  (reify
    om/IRender
    (render [_]
      (.log js/console "WTF?")
      (dom/div nil (str "WTF? " (type (om/value elem)) " " elem)))))
