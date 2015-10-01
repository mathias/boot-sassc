(set-env!
 :dependencies '[[adzerk/bootlaces "0.1.9" :scope "test"]])

(require '[adzerk.bootlaces :refer :all])

(def +version+ "0.1.7")
(bootlaces! +version+)

(task-options!
 pom  {:project     'djwhitt/boot-sassc
       :version     +version+
       :description "DEPRECATED"
       :url         "https://github.com/mathias/boot-sassc"
       :scm         {:url "https://github.com/mathias/boot-sassc"}
       :license     {"name" "Eclipse Public License"
                     "url"  "http://www.eclipse.org/legal/epl-v10.html"}})
