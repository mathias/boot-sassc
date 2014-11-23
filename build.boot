(set-env!
  :dependencies '[[org.clojure/clojure       "1.6.0"       :scope "provided"]
                  [boot/core                 "2.0.0-pre27" :scope "provided"]
                  [tailrecursion/boot-useful "0.1.3"       :scope "test"]])

(require '[tailrecursion.boot-useful :refer :all])

(def +version+ "0.1.0")

(useful! +version+)

(task-options!
  pom  [:project     'boot-sassc
        :version     +version+
        :description "Boot task to compile Sass/SCSS to CSS."
        :url         "https://github.com/mathias/boot-sassc"
        :scm         {:url "https://github.com/mathias/boot-sassc"}
        :license     {:name "Eclipse Public License"
                      :url  "http://www.eclipse.org/legal/epl-v10.html"}])
