/*
 * #%L
 * GraphWalker Dashboard
 * %%
 * Copyright (C) 2011 - 2014 GraphWalker
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
'use strict';

angular.module('dashboard', [
    'ngAnimate',
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngTouch'
])
.config(function($routeProvider) {
    $routeProvider.
    when('/', {
        templateUrl: 'views/welcome.html',
        controller: 'WelcomeController'
    }).
    when('/dashboard', {
        templateUrl: 'views/dashboard.html',
        controller: 'WelcomeController',
        name: 'Dashboard',
        icon: 'fa-home'
    }).
    when('/executions', {
        templateUrl: 'views/executions.html',
        controller: 'WelcomeController',
        name: 'Executions',
        icon: 'fa-gears'
    }).
    when('/reports', {
        templateUrl: 'views/reports.html',
        controller: 'WelcomeController',
        name: 'Reports',
        icon: 'fa-file-text-o'
    }).
    when('/schedule', {
        templateUrl: 'views/schedule.html',
        controller: 'WelcomeController',
        name: 'Schedule',
        icon: 'fa-calendar'
    }).
    otherwise({
        redirectTo: '/'
    });
})
.controller('DashboardController', function($scope, $route, $location) {
    $scope.routes = [];
    angular.forEach($route.routes, function(config, path) {
        if (config.name) {
            $scope.routes.push({
                name:config.name,
                icon:config.icon,
                url:path
            });
        }
    });
    $scope.isActive = function(route) {
        return route.url === $location.path();
    }
})
.controller('WelcomeController', function($scope, $route) {

});