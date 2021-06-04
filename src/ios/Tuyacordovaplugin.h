#import <UIKit/UIKit.h>
#import <Cordova/CDVPlugin.h>
#import <Cordova/CDV.h>

@interface Tuyacordovaplugin: CDVPlugin

- (void) user_loginOrRegitserWithUID: (CDVInvokedUrlCommand *)command;

- (void) home_listHomes: (CDVInvokedUrlCommand *)command;
- (void) home_listDevices: (CDVInvokedUrlCommand *)command;


@end