(function(define) {
	define([ 'common/navigation/navbar.directive',
	         'common/navigation/sidebar.directive', 
	         'common/navigation/navigation.service', 
	         'common/messages/messages.directive', 
	         'common/messages/message.service', 
	         'common/login/login.directive',
	         'common/login/login.service', 
	         'common/duration.filter',
	         'common/loading/loading.value',
	         'common/loading/loadingAnimation.directive'], 
	         function f(navbarDirective, sidebarDirective, navigationService, messagesDirective, messageService, loginDirective, loginService, durationFilter, loading, loadingDirective) {
		var module = angular.module('wsCommon', []);

		module.directive('wsNavbar', navbarDirective);
		module.directive('wsSidebar', sidebarDirective);
		module.service('navigationService', navigationService);

		module.directive('wsMessages', messagesDirective);
		module.service('messageService', messageService);

		module.directive('wsLogin', loginDirective);
		module.service('loginService', loginService);

		module.filter('duration', durationFilter);
		
		module.value('loading',loading);
		module.directive('wsLoadingAnimation',loadingDirective)
	});
})(adminConsole.define);