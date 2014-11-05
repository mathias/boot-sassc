(ns boot-sassc.core
  {:boot/export-tasks true}
  (:require [clojure.java.io   :as io]
            [boot.pod          :as pod]
            [boot.core         :as core]
            [boot.file         :as file]
            [boot.util         :as util]
            [boot.task-helpers :as helpers]))

(defn build-sassc-cmd
  [output-path & {:keys [output-style line-numbers source-maps sass-files]}]
  (concat
   ["sassc" "-o" output-path]
   (when output-style ["-t" output-style])
   (when line-numbers ["-l"])
   (when source-maps ["-g"])
   sass-files))

(core/deftask sass
  "Compile Sass/SCSS to CSS files.

   The default output path is ./main.css

   Requires that the sassc executable has been installed."

  [o output-to PATH      str  "The output css file path relative to docroot."
   t output-style TYPE   str  "Output style. Can be: nested, compressed."
   l line-numbers        bool "Emit comments showing original line numbers."
   g source-maps         bool "Emit source map."]

  (let [output-path (or output-to "main.css")
        tmp-dir     (core/mktmpdir!)
        smap        (io/file tmp-dir (str output-path ".map"))
        sass        (->> (core/all-files) (core/by-ext [".sass" ".scss"]))]
    (util/info "Compiling %s...\n" (.getName output-path))
    (apply helpers/dosh
           (build-sassc-cmd output-path {:output-style output-style
                                           :line-numbers line-numbers
                                           :source-maps source-maps
                                           :sass-files sass}))))
