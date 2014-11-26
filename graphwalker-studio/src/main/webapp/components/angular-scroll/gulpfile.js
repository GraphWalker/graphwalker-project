/*
 * #%L
 * GraphWalker Dashboard
 * %%
 * Copyright (C) 2005 - 2014 GraphWalker
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
var gulp   = require('gulp');
var clean  = require('gulp-rimraf');
var jshint = require('gulp-jshint');
var concat = require('gulp-concat');
var uglify = require('gulp-uglify');
var ngmin  = require('gulp-ng-annotate');
var sourcemaps = require('gulp-sourcemaps');

var karma = require('gulp-karma');
var karmaConfPath = './test/karma.conf.js';
var karmaConf = require(karmaConfPath);

var sources = [
  'src/module.js', 
  'src/helpers.js',
  'src/services/request-animation.js',
  'src/services/spy-api.js',
  'src/services/scroll-container-api.js',
  'src/directives/smooth-scroll.js',
  'src/directives/spy-context.js',
  'src/directives/scroll-container.js',
  'src/directives/scrollspy.js'
];

var targets = 'angular-scroll.{js,min.js,min.js.map}';

gulp.task('clean', function() {
  gulp.src(targets)
    .pipe(clean());
});

gulp.task('lint', function() {
  gulp.src(sources)
    .pipe(jshint())
    .pipe(jshint.reporter('default'));
});

gulp.task('test', function() {
  return gulp.src(karmaConf.testFiles)
    .pipe(karma({
      configFile: karmaConfPath,
      action: 'run'
    }))
    .on('error', function(err) {
      throw err;
    });
});

gulp.task('compress', function() {
  //Development version
  gulp.src(sources)
    .pipe(concat('angular-scroll.js', { newLine: '\n\n' }))
    .pipe(ngmin())
    .pipe(gulp.dest('./'));

  //Minified version
  gulp.src(sources)
    .pipe(sourcemaps.init())
      .pipe(concat('angular-scroll.min.js', { newLine: '\n\n' }))
      .pipe(ngmin())
      .pipe(uglify())
    .pipe(sourcemaps.write('./'))
    .pipe(gulp.dest('./'));
});

gulp.task('default', ['lint', 'test', 'clean', 'compress']);
