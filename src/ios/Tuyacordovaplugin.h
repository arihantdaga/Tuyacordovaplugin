#import <UIKit/UIKit.h>
#import <Cordova/CDVPlugin.h>

@interface Tuyacordovaplugin

- (void) user_loginOrRegitserWithUID: (CDVInvokedUrlCommand *)command;

- (void) home_listHomes: (CDVInvokedUrlCommand *)command;
- (void) home_listDevices: (CDVInvokedUrlCommand *)command;


@end