(set-env!
 :dependencies '[[adzerk/bootlaces "0.1.9" :scope "test"]])

(require '[adzerk.bootlaces :refer :all])

(def +version+ "0.1.6-SNAPSHOT")
(bootlaces! +version+)

(task-options!
 pom  {:project     'djwhitt/boot-sassc
       :version     +version+
       :description "Boot task to compile SCSS to CSS."
       :url         "https://github.com/mathias/boot-sassc"
       :scm         {:url "https://github.com/mathias/boot-sassc"}
       :license     {"name" "Eclipse Public License"
                     "url"  "http://www.eclipse.org/legal/epl-v10.html"}})
