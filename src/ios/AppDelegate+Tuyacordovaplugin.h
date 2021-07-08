#import "AppDelegate.h"
#import <UIKit/UIKit.h>

@import UserNotifications;
@import AuthenticationServices;

@interface AppDelegate (Tuyacordovaplugin) <UIApplicationDelegate>
- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
@end
