(ns loopitude.settings
  (:refer-clojure :exclude [assoc!]))

(defn- e-val [e]
  (.-value (.-target e)))

(defn- logish [x]
  (* x x x))

; for calculating control values
(defn- delogish [x]
  (.pow js/Math x (/ 1 3)))

(defn- assoc! [map-atom & args]
  (reset! map-atom
          (apply (partial assoc @map-atom) args)))

(defn- range1 "A range input from 0 to 1" [default on-change]
  [:input {:type "range", :min "0", :max "1", :step "0.01", :default-value (str default)
           :on-change #(on-change (js/parseFloat (e-val %)))}])

(defn volume [settings]
  [:div
   "Volume"
   [range1 0.5 #(assoc! settings :vol (logish %))]])

(defn adsr [settings]
  [:div
   "ADSR "
   [:span "A" [range1
               (delogish 0.03)
               #(assoc! settings :a (logish %))]]
   [:span "D" [range1
               (delogish 0.1)
               #(assoc! settings :d (logish %))]]
   [:span "S" [range1
               0.5
               #(assoc! settings :s %)]]
   [:span "R" [range1
               (delogish 0.3)
               #(assoc! settings :r (* 5 (logish %)))]]])
