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
    when('/editor', {
        templateUrl: 'views/editor.html',
        controller: 'DashboardController',
        label: 'Editor',
        icon: 'fa-file-o'
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
