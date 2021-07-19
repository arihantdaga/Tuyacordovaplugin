#import "AppDelegate+Tuyacordovaplugin.h"
#import "Tuyacordovaplugin.h"
#import <objc/runtime.h>
#import "CameraViewController.h"

@interface AppDelegate () <UNUserNotificationCenterDelegate>
@end

static id <UNUserNotificationCenterDelegate> _previousDelegate;
#define kApplicationInBackgroundKey @"applicationInBackground"

@implementation AppDelegate (Tuyacordovaplugin)

- (id) getCommandInstance:(NSString*)className
{
    return [self.viewController getCommandInstance:className];
}

+ (void) load 
{
    Method original = class_getInstanceMethod(self, @selector(application:didFinishLaunchingWithOptions:));
       Method swizzled = class_getInstanceMethod(self, @selector(application:swizzledDidFinishLaunchingWithOptions:));
       method_exchangeImplementations(original, swizzled);
}

- (AppDelegate *)pushPluginSwizzledInit
{
    return [self pushPluginSwizzledInit];
}

- (void)setApplicationInBackground:(NSNumber *)applicationInBackground {
    objc_setAssociatedObject(self, kApplicationInBackgroundKey, applicationInBackground, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (NSNumber *)applicationInBackground {
    return objc_getAssociatedObject(self, kApplicationInBackgroundKey);
}

- (BOOL)application:(UIApplication *)application swizzledDidFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    [self application:application swizzledDidFinishLaunchingWithOptions:launchOptions];
    if ([UNUserNotificationCenter currentNotificationCenter].delegate != nil) {
        _previousDelegate = [UNUserNotificationCenter currentNotificationCenter].delegate;
    }
    [UNUserNotificationCenter currentNotificationCenter].delegate = self;
    self.applicationInBackground = @(YES);
    return YES;
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
    self.applicationInBackground = @(NO);
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
    self.applicationInBackground = @(YES);
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
    NSLog(@"test:%@",deviceToken);
    [TuyaSmartSDK sharedInstance].deviceToken = deviceToken;
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void(^)(UIBackgroundFetchResult))completionHandler 
{
    NSLog(@"notification ochindi");
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center
didReceiveNotificationResponse:(UNNotificationResponse *)response
         withCompletionHandler:(void (^)(void))completionHandler {
    

    NSMutableDictionary *userInfo = [response.notification.request.content.userInfo mutableCopy];
    [userInfo setObject:response.actionIdentifier forKey:@"actionCallback"];
    NSString *devId = userInfo[@"devId"];
    if (devId) {
        if ([self.applicationInBackground  isEqual: @(YES)]) {
            Tuyacordovaplugin *pushHandler = [self getCommandInstance:@"Tuyacordovaplugin"];
            [pushHandler saveBgNotificationData:userInfo];
        } else {
            [Tuyacordovaplugin.tuyacordovaplugin sendForegroundNotification: response.notification.request.content.userInfo];
        }
    }
    completionHandler();
}






@end
