(ns clojure-distilled.core
  (:gen-class))


;;; Multimethods
(defmulti area :shape)
(defmethod area :circle [{:keys [r]}]
  (* Math/PI r r))
(defmethod area :rectangle [{:keys [l w]}]
  (* l w))
(defmethod area :default [shape]
  (throw (Exception. (str "Unknown shape: " shape))))

(area {:shape :circle :r 10})
(area {:shape :rectangle :l 10 :w 5})
(area {:shape :triangle :a 10 :b 5 :c 6})



;;; Protocols
(defprotocol Foo
  "Foo doc string"
  (bar [this b] "bar doc string")
  (baz [this] [this b] "baz doc string"))
;; Now, we can create a type that implements protocol
(deftype Bar [data] Foo
         (bar [this param] (println data param))
         (baz [this] (println (class this)))
         (baz [this param] (println param)))
;; Create instance of Bar and call its mmethods:
(let [b (Bar. "some data")]
  (.bar b "param")
  (.baz b)
  (.baz b "baz with param"))

;; We can also use protocols to extend functionality of existing types, including java classes
;; E.g. following code extends java.lang.String with Foo protocol
(extend-protocol Foo String
                 (bar [this param] (println this param)))
(bar "hello" "world")



;;; Dealing with Global State

;; atom is used in cases when we need to uncoordinated updates
(def global-val (atom nil))
(println (deref global-val))
(println @global-val)
;; we can use reset! for setting the new value
(reset! global-val 10)
(println @global-val)
;; or use swap! for passing in the function which will be used for computing the new value
(swap! global-val inc)
(println @global-val)

;; ref is used when we might need to do multiple updates as a transaction
(def names (ref []))
;; open transaction via "dosync"
(dosync
 (ref-set names ["John"])
 (alter names #(if (not-empty %)
                 (conj % "Jane"))))


;;; Macros - Writing Code that Writes Code

;; Use case for macro - checking if user is set in session
(def session (atom {:user "Bob"}))
(defn load-content []
  (if (:user @session)
    "Welcome back!"
    "Please, log in."))
;; "if" is quite repetitive => we can template this function as follows
(defmacro defprivate [name args & body]
  `(defn ~(symbol name) ~args
     (if (:user @session)
       (do ~@body)
       "Please, log in.")))
;; then we can define load-content function as follows
(defprivate load-content [] "Welcome back")
(load-content)
(reset! session nil)
(load-content)

;; you can use macroexpand-1 to see to how the macro will be expanded before evaluation
(macroexpand-1 '(defprivate load-content [] "Welcome back"))



;;; Calling out to Java

;; Importing classes
(import java.io.File)
;; creating new instance of File
(def pwd ( new File "."))
;; and the same in a more idiomatic way
(def pwd (File. "."))
;; now we can start calling methods
;; btw. following code can be used for printing all getters

(doseq [method (.getMethods (.getClass pwd))
        :let [name (.getName method)]
        :when (.startsWith name "get")]
  (println name))
(.getAbsolutePath pwd)

;; static methods
(Math/sqrt 256)

;; simplified method call chain
(.. pwd getAbsolutePath getBytes)
