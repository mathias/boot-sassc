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

   The default output path is ./main.css

   Requires that the sassc executable has been installed."

  [o output-to PATH      str  "Write output to specified file. Generates one CSS file at path."
   t output-style TYPE   str  "Output style. Can be: nested, compressed."
   l line-numbers        bool "Emit comments showing original line numbers."
   g source-maps         bool "Emit source map."
   f sass-file           str  ""]

  (let [tmp-dir     (core/mktmpdir!)
        output-dir  (core/mktgtdir!)
        output-path (or output-to "main.css")
        css-out     (io/file tmp-dir output-path)
        smap        (io/file tmp-dir (str output-path ".map"))
        sass-files  (or (->> (core/all-files) (core/by-name sass-file))
                     (->> (core/all-files) (core/by-ext [".sass" ".scss"])))]
    (core/with-pre-wrap
      (util/info "Compiling %s...\n" (.getName output-path))
      (sassc (concat ["-o" css-out]
                     (when (and output-style
                                (valid-style? output-style))
                       ["-t" output-style])
                     (when line-numbers ["-l"])
                     (when source-maps ["-g"])
                     sass-files)))
    (core/sync! output-dir tmp-dir)))
