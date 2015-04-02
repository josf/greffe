(ns greffe.doc-comps
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs-xml.core :as cx]
   [gmark.core :as gm]
   [gmark.tei-elems :as gmt]
   [greffe.markup :as mk]))

(def tei (gmt/tagtypes mk/markup))

(defn element-attributes-component [attrs owner]
  (reify
    om/IRender
    (render [_]
      (apply dom/ul #js {:className "attributes"}
       (map #(dom/li nil (str (first %) " " (second %))) attrs)))))

(defn dispatch-on-element-type [el]
  (let [val (om/value el)]
    (cond
      (string? val)
      :text
       
      (and
        (contains? val :content)
        (contains? val :tag))
      (let [el-type (get-in mk/markup [(:tag val) :type])
            edit-type (:edit-type val)]
        (cond
          (= :unknown-element edit-type)
          :unknown-element

          (#{:container :multi-chunk :chunk} el-type)
          (if (= edit-type :no-text-edit)
            :elem-no-edit
            :elem)

          (= :inner el-type)
          :inner-elem
          
          (= :empty el-type)
          :empty-elem))

      true
      :wtf)))


(defmulti element-component dispatch-on-element-type)

(defmethod element-component :text [elem owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (dom/span #js {:className "xml-text-element"} elem))))



(defmethod element-component :elem-no-edit [elem owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className
                    (str "xml-" (name (:tag elem)) " tei-block col-md-12 no-edit")}
        (dom/div #js {:className "row"}
          (dom/div #js {:className "block-data col-md-1"}
            (dom/div #js {:className "name"} (name (:tag elem)))
            (om/build element-attributes-component (:attrs elem)))
          (when (pos? (count (:content elem)))
            (apply dom/div #js {:className "block-contents col-md-10"}
              (om/build-all element-component (:content elem)))))))))


(defmethod element-component :elem [elem owner]
  "This is for editable nodes, assumes :edit-type is :edit."
  (reify
    om/IInitState
    (init-state [_] {:hoverTimeout nil
                     :showButton false
                     :editText false    ; are we in editing mode?
                     :editContent nil}) ; "cache" for editing content
    
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
               (dom/a #js {:onClick
                           (fn [ev]
                             (om/set-state! owner
                               :editContent (gm/to-gmark @elem tei))
                             (om/set-state!
                               owner
                               :editText
                               (not (om/get-state owner :editText))))}
                 "Editer"))))

           (when (:editText state)
             (dom/textarea #js {:value (:editContent state)
                                :cols "50" :rows "3"
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
                                          el)))))}))
         
         (when (pos? (count (:content elem)))
           (apply dom/div #js {:className "block-contents col-md-10"}
             (om/build-all element-component (:content elem)))))))))

(defmethod element-component :inner-elem [elem owner]
  (reify
    om/IRender
    (render [_]
      (apply dom/span #js {:className (str "xml-" (name (:tag elem)) " tei-inner")}
        (om/build-all element-component (:content elem))))))


(defmethod element-component :empty-elem [elem owner]
  (reify
    om/IRender
    (render [_]
      (dom/span #js {:className (str "xml-" (name (:tag elem)) " tei-empty")}
        " "))))

(defmethod element-component :wtf [elem owner]
  (reify
    om/IRender
    (render [_]
      (.log js/console "WTF?")
      (dom/div nil (str "WTF? " (type (om/value elem)) " " elem)))))



(defn xml-attributes-component [atts owner]
  (reify
    om/IRender
    (render [_]
      (apply dom/span #js {:className "attributes"}
        (mapcat
          (fn [[k v]]
            [(dom/span #js {:className "attribute-name"} (name k))
             (dom/span #js {:className "attribute-equals"} "=")
             (dom/span #js {:className "attribute-quote"} "\"")
             (dom/span #js {:className "attribute-value"} v)
             (dom/span #js {:className "attribute-quote"} "\"")])
          atts)))))


(defn dispatch-on-element-type-xml [el]
  (let [t (dispatch-on-element-type el)]
    (if (= :elem-no-edit t)
      :elem
      t)))

(defmulti xml-display dispatch-on-element-type-xml)

(defmethod xml-display :elem [elem owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "xml-output"}
        (dom/span #js {:className "tag"}
          (dom/span #js {:className "angle-bracket"} "<")
          (dom/span #js {:className "tag-name"} (name (:tag elem)))
          (when-not (empty? (:attrs elem))
            (om/build xml-attributes-component (:attrs elem)))
          (dom/span #js {:className "angle-bracket"} ">")

          (apply dom/div #js {:className "block-contents"}
            (om/build-all xml-display (:content elem)))
          (dom/span #js {:className "angle-bracket"} "</")
          (dom/span #js {:className "tag-name"} (name (:tag elem)))
          (dom/span #js {:className "angle-bracket"} ">"))))))

(defmethod xml-display :inner-elem [elem owner]
  (reify
    om/IRender
    (render [_]
      (dom/span #js {:className "xml-output"}
        (dom/span #js {:className "tag"}
          (dom/span #js {:className "angle-bracket"} "<")
          (dom/span #js {:className "tag-name"} (name (:tag elem)))
          (when-not (empty? (:attrs elem))
            (om/build xml-attributes-component (:attrs elem)))
          (dom/span #js {:className "angle-bracket"} ">")

          (apply dom/span nil (om/build-all xml-display (:content elem)))
          (dom/span #js {:className "angle-bracket"} "</")
          (dom/span #js {:className "tag-name"} (name (:tag elem)))
          (dom/span #js {:className "angle-bracket"} ">"))))))

(defmethod xml-display :empty-elem [elem owner]
  (reify
    om/IRender
    (render [_]
      (dom/span #js {:className "xml-output empty"}
        (dom/span #js {:className "tag"}
          (dom/span #js {:className "angle-bracket"} "<")
          (dom/span #js {:className "tag-name"}  (name (:tag elem)))
          (when-not (empty? (:attrs elem))
            (om/build xml-attributes-component (:attrs elem)))
          (dom/span #js {:className "angle-bracket"} "/>"))))))

(defmethod xml-display :text [elem owner]
  (reify
    om/IRender
    (render [_]
      (dom/span #js {:className "xml-output text"}
        elem))))

(defmethod xml-display :wtf [elem owner]
  (reify
    om/IRender
    (render [_]
      (.log js/console "xml WTF?")
      (dom/div nil (str "xml WTF? " (type (om/value elem)) " " elem)))))

