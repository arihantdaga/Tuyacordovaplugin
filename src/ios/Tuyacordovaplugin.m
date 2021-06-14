#import "Tuyacordovaplugin.h"
#import "CameraViewController.h"

@interface Tuyacordovaplugin ()
@property(strong, nonatomic) TuyaSmartHomeManager *homeManager;
//@property (nonatomic, strong) UIViewController *viewController;
@end

@implementation Tuyacordovaplugin

- (void) pluginInitialize {
    NSString *appKey = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleTuyaAppKey"];
    NSString *appSecret = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleTuyaAppSecret"];
    [[TuyaSmartSDK sharedInstance] startWithAppKey:appKey secretKey:appSecret];
}

- (void) user_loginOrRegitserWithUID: (CDVInvokedUrlCommand *) command {

	NSString *countryCode = (NSString *)[command argumentAtIndex:0];
 	NSString *uid = (NSString *)[command argumentAtIndex:1];
  	NSString *pass = (NSString *)[command argumentAtIndex:2];

    [[TuyaSmartUser sharedInstance] loginOrRegisterWithCountryCode:countryCode uid:uid password:pass createHome:YES success:^(id result) {
		NSDictionary *resultDict = [NSDictionary dictionaryWithObjectsAndKeys:
        [NSString stringWithFormat:@"%lld", result], @"homeId",
      	nil];
		CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resultDict];
		[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
	} failure:^(NSError *errorMsg) {
        NSLog(@"loginOrRegisterWithCountryCode failure: %@", errorMsg);
        NSDictionary *resultDict = [Tuyacordovaplugin makeError:errorMsg];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resultDict];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
	}];
}

- (void) home_listHomes: (CDVInvokedUrlCommand *) command {
    [self.homeManager getHomeListWithSuccess:^(NSArray<TuyaSmartHomeModel *> *homes) {
        if (!homes) {
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:[[NSMutableArray alloc] init]];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:homes];
		[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        
    } failure:^(NSError *errorMsg) {
        NSDictionary *resultDict = [Tuyacordovaplugin makeError:errorMsg];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resultDict];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void) home_listDevices: (CDVInvokedUrlCommand *) command {

    NSString *homeIdString = (NSString *)[command argumentAtIndex:0];
    long long homeId = [homeIdString longLongValue];


    TuyaSmartHome *home = [TuyaSmartHome homeWithHomeId:homeId];
    home.delegate = self;
    [home getHomeDetailWithSuccess:^(TuyaSmartHomeModel *homeModel) {
        NSArray<TuyaSmartDeviceModel*> *devices = [home.deviceList mutableCopy];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

        } failure:^(NSError *errorMsg) {
            NSDictionary *resultDict = [Tuyacordovaplugin makeError:errorMsg];
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resultDict];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }];
}

- (void) ipc_startCameraLivePlay: (CDVInvokedUrlCommand *) command {
    NSString *devId = (NSString *)[command argumentAtIndex:0];
    CameraViewController *vc = [[CameraViewController alloc] initWithDeviceId:devId];
    //  [self.navigationController pushViewController:vc animated:YES];


//    [self.viewController addChildViewController:vc];
//    [self.webView.superview insertSubview:vc.view aboveSubview:self.webView];
    
    // [self.viewController presentModalViewController:vc  animated:YES];
    UINavigationController *navigationController = [[UINavigationController alloc] initWithRootViewController:vc];
    [self.viewController presentViewController:navigationController animated:YES completion:nil];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (TuyaSmartHomeManager *)homeManager {
    if (!_homeManager) {
        _homeManager = [[TuyaSmartHomeManager alloc] init];
    }
    return _homeManager;
}


//helpers
+ (NSDictionary *)makeError:(NSString *)errorMessage {
  NSDictionary *resultDict = [NSDictionary dictionaryWithObjectsAndKeys:
    errorMessage, @"message",
  nil];
  return resultDict;
}

@end
