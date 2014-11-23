# boot-sassc

Boot task to compile [Sass & SCSS](http://sass-lang.com/) stylesheets with the [libsass sassc](http://libsass.org/#sassc) compiler.

Provides the `sass` task, which compiles Sass/SCSS to CSS.

## Usage

Typically, you will have many SCSS files in your project, and one main SCSS file that `@import`s things in the correct order. Add your Sass source directory to your project's `build.boot` file:

```clojure
;; in build.boot
(set-env!
  :dependencies '[...]
  :src-paths #{"sass/"})
```

### Terminal

In a terminal you can compile all `.sass` and `.scss` files in your project with:

```
boot sass
```

To compile your main SCSS file only (so that `@import`s happen in the right order), use the `-f` flag:

```
boot sass -f sass/main.scss
```

To change the filename of the output stylesheet is output to, use the `-o` flag:

```
boot sass -o application.css
```

To regenerate the stylesheet on changes you can use boot's generic `watch` task:

```
boot watch sass
```

### build.boot file in your project

In your `build.boot` you could call it like this:

```clojure
(deftask run
  "Generate CSS from SCSS and watch for future changes"
  []
  (comp (watch) (sass)))
```

## Options

See the [boot project](https://github.com/boot-clj/boot) for more information
on how to use these. By default `boot-sassc` will save the compiled CSS file at
`target/main.css`.

```clojure
[f sass-file           str  "Input file. If not present, all .sass & .scss files will be compiled."
 o output-to PATH      str  "Output CSS file, path is relative to target/"
 t output-style TYPE   str  "Output style. Can be: nested, compressed."
 l line-numbers        bool "Emit comments showing original line numbers."
 g source-maps         bool "Emit source map."]
```

## Known issues

The file paths may not be correct for the `source-maps` option. I'm investigating solutions.

## License

Copyright Matt Gauger 2014.

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
