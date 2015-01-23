(ns mathias.boot-sassc
  {:boot/export-tasks true}
  (:require [clojure.java.io :as io]
            [clojure.string  :as string]
            [boot.core       :as core]
            [boot.file       :as file]
            [boot.util       :as util]))

(defn valid-style? [style]
  (some #{"nested" "compressed"} [style]))

(defn sass-filename->css-filename
  [filename]
  (string/replace filename #".s(c|a)ss$" ".css"))

(defn- sassc [input-file output-dir & {:keys [output-style line-numbers source-maps]}]
  (let [output-file (->> input-file
                         .getName
                         sass-filename->css-filename
                         (io/file output-dir))]
    (apply util/dosh (concat ["sassc"]
                             (when (and output-style
                                        (valid-style? output-style))
                               ["-t" output-style])
                             (when line-numbers ["-l"])
                             (when source-maps ["-m"])
                             [(file/path input-file) (file/path output-file)]))))

(defn by-name-re
  [res files & [negate?]]
  ((core/file-filter #(fn [f] (re-find % (.getName f)))) res files negate?))

(defn not-by-name-re
  [res files]
  (by-name-re res files true))

(defn not-sass-partials
  [files]
  (not-by-name-re [#"^_"] files))

(core/deftask sass
  "Compile SCSS to CSS files.

   The default output path is target/main.css

   Requires that the sassc executable has been installed."
  [f sass-file FILE      str  "Input file. If not present, all .sass & .scss files will be compiled."
   o output-dir PATH     str  "Output CSS directory path, relative to target directory."
   t output-style TYPE   str  "Output style. Can be: nested, compressed."
   l line-numbers        bool "Emit comments showing original line numbers."
   g source-maps         bool "Emit source map."]
  (let [tmp-dir     (core/temp-dir!)
        output-dir  (if output-dir (io/file tmp-dir output-dir) tmp-dir)]
    (core/with-pre-wrap fileset
      (let [sass-files (cond->> fileset
                                true core/input-files
                                true (core/by-ext [".sass" ".scss"])
                                (not sass-file) not-sass-partials
                                sass-file (core/by-name [sass-file]))]
        (core/empty-dir! tmp-dir)
        (util/info "Compiling %d changed SASS files... .\n" (count sass-files))
        (.mkdirs output-dir)
        (doseq [file sass-files]
          (util/dbug "  Compiling SCSS file %s\n" (core/tmppath file))
          (sassc (core/tmpfile file)
                 output-dir
                 :output-style output-style
                 :line-numbers line-numbers
                 :source-maps  source-maps))
        (util/dbug "...done.\n" (count sass-files))
        (-> fileset
            (core/add-resource tmp-dir)
            core/commit!)))))
