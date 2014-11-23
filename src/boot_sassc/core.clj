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
;;  (util/info "%s" (clojure.string/join " " args))
  (assert (every? string? args) "Args must be strings")
  (apply helpers/dosh (concat ["sassc"] args)))

(defn- tmp-file-for [tmp-dir path]
  (let [file (io/file path)
        relative-path (core/relative-path file)]
    (io/file tmp-dir relative-path)))

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
        stage-dir   (core/mktmpdir!)
        output-dir  (core/mktgtdir!)
        output-path (or output-to "main.css")
        tmp-file-for (partial tmp-file-for tmp-dir)]
    (core/with-pre-wrap
      (let [css-out        (io/file tmp-dir output-path)
            smap-filename  (str output-path ".map")
            smap           (io/file tmp-dir (str output-path ".map"))
            srcs           (core/src-files+)
            sass-files     (if sass-file
                             [(io/file sass-file)]
                             (->> srcs (core/by-ext [".sass" ".scss"])))
            tmp-sass-files (map tmp-file-for sass-files)]
        (util/info "Compiling target/%s...\n" (.getName css-out))
        (io/make-parents css-out)

        ;; make hardlinks for all the scss and sass files into the tempdir
        (doseq [in srcs]
          (let [rel-path (core/relative-path in)
                out      (io/file tmp-dir rel-path)]
            (file/copy-with-lastmod in out)))

        (sassc (concat ["-o" (.getPath css-out)]
                       (when (and output-style
                                  (valid-style? output-style))
                         ["-t" output-style])
                       (when line-numbers ["-l"])
                       (when source-maps ["-g"])
                       (map #(.getPath %) tmp-sass-files)))

        (file/copy-with-lastmod css-out (io/file stage-dir output-path))

        (when source-maps
          (file/copy-with-lastmod smap (io/file stage-dir smap-filename)))

        (core/sync! output-dir stage-dir)))))
