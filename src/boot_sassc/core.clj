(ns boot-sassc.core
  {:boot/export-tasks true}
  (:require [clojure.java.io   :as io]
            [boot.pod          :as pod]
            [boot.core         :as core]
            [boot.file         :as file]
            [boot.util         :as util]
            [boot.task-helpers :as helpers]))

(defn valid-style? [style]
  (some #{"nested" "compressed"} [style]))

(defn- sassc [args]
  (apply helpers/dosh (concat ["sassc"] args)))

(core/deftask sass
  "Compile Sass/SCSS to CSS files.

   The default output path is target/main.css

   Requires that the sassc executable has been installed."

  [f sass-file FILE      str  "Input file. If not present, all .sass & .scss files will be compiled."
   o output-to PATH      str  "Output CSS file, path is relative to target/"
   t output-style TYPE   str  "Output style. Can be: nested, compressed."
   l line-numbers        bool "Emit comments showing original line numbers."
   g source-maps         bool "Emit source map."]

  (let [tmp-dir     (core/mktmpdir!)
        output-dir  (core/mktgtdir!)
        output-path (or output-to "main.css")]
    (core/with-pre-wrap
      (let [css-out     (io/file tmp-dir output-path)
            smap        (io/file tmp-dir (str output-path ".map"))
            sass-files  (or (->> (core/all-files) (core/by-name sass-file))
                            (->> (core/all-files) (core/by-ext [".sass" ".scss"])))]
        (util/info "Compiling %s...\n" (.getName css-out))
        (io/make-parents css-out)
        (sassc (concat ["-o" css-out]
                       (when sass-file ["-f" sass-file])
                       (when (and output-style
                                  (valid-style? output-style))
                         ["-t" output-style])
                       (when line-numbers ["-l"])
                       (when source-maps ["-g"])
                       sass-files))
        (core/sync! output-dir tmp-dir)))))
