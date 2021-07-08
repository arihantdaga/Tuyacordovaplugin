#import "AppDelegate+Tuyacordovaplugin.h"
#import "Tuyacordovaplugin.h"
#import <objc/runtime.h>
#import "CameraViewController.h"

@interface AppDelegate () <UNUserNotificationCenterDelegate>
@end

@implementation AppDelegate (Tuyacordovaplugin)


+ (void) load 
{
    Method original = class_getInstanceMethod(self, @selector(application:didFinishLaunchingWithOptions:));
       Method swizzled = class_getInstanceMethod(self, @selector(application:swizzledDidFinishLaunchingWithOptions:));
       method_exchangeImplementations(original, swizzled);
}

- (AppDelegate *)pushPluginSwizzledInit
{
    // [[NSNotificationCenter defaultCenter]
    //  addObserver:self
    //  selector:@selector(applicationDidBecomeActive:)
    //  name:UIApplicationDidBecomeActiveNotification
    //  object:nil];
    
    return [self pushPluginSwizzledInit];
}

- (void)applicationDidBecomeActive:(NSNotification *)notification
{
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
    NSLog(@"test:%@",deviceToken);
    [TuyaSmartSDK sharedInstance].deviceToken = deviceToken;
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void(^)(UIBackgroundFetchResult))completionHandler 
{
    NSLog(@"notification ochindi");
    if (userInfo[@"devId"]) {
    CameraViewController *vc = [[CameraViewController alloc] initWithDeviceId:userInfo[@"devId"]];
    UINavigationController *navigationController = [[UINavigationController alloc] initWithRootViewController:vc];
    [self.viewController presentViewController:navigationController animated:YES completion:nil];
    }
}






@end
