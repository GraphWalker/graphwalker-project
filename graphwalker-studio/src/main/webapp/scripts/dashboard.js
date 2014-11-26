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
'use strict';

angular.module('dashboard', [
    'ngAnimate',
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngTouch',
    'ng-breadcrumbs',
    'ui.calendar'
])
.config(function($routeProvider) {
    $routeProvider.
    when('/dashboard', {
        templateUrl: 'views/dashboard.html',
        controller: 'DashboardController',
        label: 'Dashboard',
        icon: 'fa-home'
    }).
    when('/executions', {
        templateUrl: 'views/executions.html',
        controller: 'DashboardController',
        label: 'Executions',
        icon: 'fa-gears'
    }).
    when('/reports', {
        templateUrl: 'views/reports.html',
        controller: 'DashboardController',
        label: 'Reports',
        icon: 'fa-file-text-o'
    }).
    when('/events', {
        templateUrl: 'views/events.html',
        controller: 'EventController',
        label: 'Events',
        icon: 'fa-calendar'
    }).
    otherwise({
        redirectTo: '/dashboard'
    });
})
.controller('DashboardController', function($scope, $route, $location, breadcrumbs) {
    $scope.breadcrumbs = breadcrumbs;
    $scope.routes = [];
    angular.forEach($route.routes, function(config, path) {
        if (config.label) {
            $scope.routes.push({
                label:config.label,
                icon:config.icon,
                url:path
            });
        }
    });
    $scope.isActive = function(route) {
        return route.url === $location.path();
    };
    $scope.navigate = function(path) {
        $location.path(path);
    };
})
.controller('EventController', function($scope) {
    $scope.uiConfig = {
        calendar:{
            width:'100%',
            height:'100%',
            editable: false,
            header:{
                left: 'title',
                center: 'month,agendaWeek,agendaDay',
                right: 'prev,next today'
            }
        }
    };
    $scope.eventSources = [{
        events: function(start, end, timezone, callback) {
        }
    }];
});